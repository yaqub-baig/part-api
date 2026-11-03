package com.autoadda.apis.part.service;

import com.autoadda.apis.part.entity.InventoryPart;
import com.autoadda.apis.part.entity.InventoryTransaction;
import com.autoadda.apis.part.entity.Order;
import com.autoadda.apis.part.entity.OrderItem;
import com.autoadda.apis.part.entity.ShoppingCart;
import com.autoadda.apis.part.exception.ConflictException;
import com.autoadda.apis.part.exception.ResourceNotFoundException;
import com.autoadda.apis.part.repository.InventoryPartRepository;
import com.autoadda.apis.part.repository.InventoryTransactionRepository;
import com.autoadda.apis.part.repository.OrderRepository;
import com.autoadda.apis.part.repository.ShoppingCartRepository;
import com.autoadda.apis.part.response.OrderItemResponse;
import com.autoadda.apis.part.response.OrderResponse;
import com.autoadda.apis.part.response.PaymentInitiationResponse;
import com.autoadda.apis.part.util.PaymentMethod;
import com.autoadda.apis.part.util.PaymentStatus;
import com.autoadda.apis.part.util.Status;
import com.autoadda.apis.part.util.TransactionType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    /** Orders in these states can still be cancelled (stock is returned to inventory). */
    private static final Set<Status> CANCELLABLE = Set.of ( Status.CREATED, Status.CONFIRMED, Status.PENDING_PAYMENT );

    private final ShoppingCartRepository cartRepo;
    private final OrderRepository orderRepo;
    private final InventoryPartRepository partRepo;
    private final InventoryTransactionRepository txnRepo;
    private final EntityManager entityManager;
    private final PaymentService paymentService;

    /**
     * Checks out the whole cart: validates and deducts stock, creates the order, empties the cart.
     * ONLINE orders also start a gateway payment; if that fails everything rolls back and the cart is kept.
     */
    @Transactional
    public OrderResponse placeOrder(Integer vehicleOwnerId, PaymentMethod paymentMethod) {
        ShoppingCart cart = cartRepo.findByVehicleOwnerId ( vehicleOwnerId )
                .orElseThrow ( () -> new ResourceNotFoundException ( "Cart not found for vehicle owner " + vehicleOwnerId ) );
        if ( cart.getItems ( ).isEmpty ( ) ) {
            throw new ConflictException ( "Cart is empty" );
        }

        Map<Integer, Integer> quantities = new LinkedHashMap<> ( );
        cart.getItems ( ).forEach ( ci -> quantities.merge ( ci.getPart ( ).getId ( ), ci.getQuantity ( ), Integer::sum ) );

        Order order = createOrder ( vehicleOwnerId, quantities, paymentMethod );
        PaymentInitiationResponse payment = startPaymentIfOnline ( order );

        cart.getItems ( ).clear ( );
        cart.setUpdatedAt ( LocalDateTime.now ( ) );
        cartRepo.save ( cart );
        return withPayment ( toResponse ( order ), payment );
    }

    /** "Buy now": orders a single part directly without touching the cart. */
    @Transactional
    public OrderResponse buyNow(Integer vehicleOwnerId, Integer partId, int quantity, PaymentMethod paymentMethod) {
        ShoppingCartService.requirePositive ( quantity );
        Order order = createOrder ( vehicleOwnerId, Map.of ( partId, quantity ), paymentMethod );
        return withPayment ( toResponse ( order ), startPaymentIfOnline ( order ) );
    }

    /** Starts a new gateway attempt for an unpaid ONLINE order whose previous attempt failed. */
    @Transactional
    public OrderResponse retryPayment(Integer orderId, Integer vehicleOwnerId) {
        Order order = lockOrder ( orderId );
        checkOwner ( order, vehicleOwnerId );
        if ( order.getStatus ( ) != Status.PENDING_PAYMENT ) {
            throw new ConflictException ( "Order " + orderId + " is not awaiting payment (status " + order.getStatus ( ) + ")" );
        }
        if ( order.getPaymentStatus ( ) == PaymentStatus.PENDING ) {
            // A link is still open; a second one could lead to paying twice.
            throw new ConflictException ( "A payment is already in progress for order " + orderId
                    + ". Complete it or wait until " + order.getPaymentExpiresAt ( ) );
        }
        if ( order.getPaymentExpiresAt ( ) != null && order.getPaymentExpiresAt ( ).isBefore ( LocalDateTime.now ( ) ) ) {
            throw new ConflictException ( "Payment window for order " + orderId + " has expired" );
        }
        return withPayment ( toResponse ( order ), paymentService.initiate ( order ) );
    }

    /** Records that cash was collected for a COD order (delivery staff / admin). */
    @Transactional
    public OrderResponse markCodCollected(Integer orderId) {
        Order order = lockOrder ( orderId );
        if ( order.getPaymentMethod ( ) != PaymentMethod.COD ) {
            throw new ConflictException ( "Order " + orderId + " is not a Cash on Delivery order" );
        }
        if ( order.getStatus ( ) == Status.CANCELLED || order.getPaymentStatus ( ) != PaymentStatus.PENDING ) {
            throw new ConflictException ( "Cannot record payment for order " + orderId + " in status "
                    + order.getStatus ( ) + "/" + order.getPaymentStatus ( ) );
        }
        order.setPaymentStatus ( PaymentStatus.SUCCESS );
        order.setPaymentCompletedAt ( LocalDateTime.now ( ) );
        return toResponse ( orderRepo.save ( order ) );
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Integer orderId) {
        return toResponse ( loadOrder ( orderId ) );
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersForOwner(Integer vehicleOwnerId) {
        return orderRepo.findByVehicleOwnerIdOrderByOrderDateDesc ( vehicleOwnerId ).stream ( ).map ( this::toResponse ).toList ( );
    }

    /** Cancels an order that has not shipped yet and puts the stock back. */
    @Transactional
    public OrderResponse cancelOrder(Integer orderId, Integer vehicleOwnerId) {
        Order order = lockOrder ( orderId );
        checkOwner ( order, vehicleOwnerId );
        if ( !CANCELLABLE.contains ( order.getStatus ( ) ) ) {
            throw new ConflictException ( "Order " + orderId + " cannot be cancelled in status " + order.getStatus ( ) );
        }

        releaseStock ( order, "Order #" + orderId + " cancelled" );
        order.setStatus ( Status.CANCELLED );
        order.setCancelledAt ( LocalDateTime.now ( ) );
        if ( order.getPaymentMethod ( ) == PaymentMethod.ONLINE && order.getPaymentStatus ( ) == PaymentStatus.SUCCESS ) {
            order.setPaymentStatus ( PaymentStatus.REFUND_PENDING );
            log.warn ( "Paid order {} cancelled; refund required", orderId );
        } else if ( order.getPaymentMethod ( ) != null ) {
            order.setPaymentStatus ( PaymentStatus.CANCELLED );
        }
        return toResponse ( orderRepo.save ( order ) );
    }

    /** IDs of ONLINE orders whose payment window has passed. Used by the expiry job. */
    @Transactional(readOnly = true)
    public List<Integer> findOverduePendingPaymentOrderIds() {
        return orderRepo.findIdsByStatusAndPaymentExpiresAtBefore ( Status.PENDING_PAYMENT, LocalDateTime.now ( ) );
    }

    /** Cancels one unpaid ONLINE order and releases its stock. No-op if it was paid or changed meanwhile. */
    @Transactional
    public void expireOrder(Integer orderId) {
        Order order = lockOrder ( orderId );
        if ( order.getStatus ( ) != Status.PENDING_PAYMENT || order.getPaymentStatus ( ) == PaymentStatus.SUCCESS
                || order.getPaymentExpiresAt ( ) == null || order.getPaymentExpiresAt ( ).isAfter ( LocalDateTime.now ( ) ) ) {
            return;
        }
        releaseStock ( order, "Order #" + orderId + " payment expired" );
        order.setStatus ( Status.CANCELLED );
        order.setPaymentStatus ( PaymentStatus.EXPIRED );
        order.setCancelledAt ( LocalDateTime.now ( ) );
        orderRepo.save ( order );
        log.info ( "Order {} expired without payment; stock released", orderId );
    }

    // ---- helpers ----

    /**
     * Locks each inventory row, verifies stock, deducts it and builds the order. Runs inside the caller's
     * transaction, so any failure (e.g. one item out of stock) rolls back every deduction.
     */
    private Order createOrder(Integer vehicleOwnerId, Map<Integer, Integer> quantitiesByPart, PaymentMethod paymentMethod) {
        PaymentMethod method = paymentMethod == null ? PaymentMethod.COD : paymentMethod;
        Order order = Order.builder ( ).vehicleOwnerId ( vehicleOwnerId ).orderDate ( LocalDateTime.now ( ) )
                .paymentMethod ( method )
                .status ( method == PaymentMethod.COD ? Status.CONFIRMED : Status.PENDING_PAYMENT )
                .paymentStatus ( method == PaymentMethod.COD ? PaymentStatus.PENDING : PaymentStatus.NOT_INITIATED )
                .build ( );

        double total = 0;
        // Sorted lock order prevents deadlocks between concurrent checkouts
        for ( Integer partId : quantitiesByPart.keySet ( ).stream ( ).sorted ( ).toList ( ) ) {
            int qty = quantitiesByPart.get ( partId );
            InventoryPart part = lockPart ( partId );
            if ( !Boolean.TRUE.equals ( part.getIsActive ( ) ) ) {
                throw new ConflictException ( "Part " + partId + " is not available for sale" );
            }
            ShoppingCartService.ensureStock ( part, qty );

            part.setQuantityInStock ( stockOf ( part ) - qty );
            partRepo.save ( part );

            double price = part.getUnitPrice ( ) == null ? 0.0 : part.getUnitPrice ( );
            total += price * qty;
            order.getItems ( ).add ( OrderItem.builder ( ).order ( order ).part ( part ).quantity ( qty ).unitPrice ( price ).build ( ) );
        }
        order.setTotalAmount ( total );
        Order saved = orderRepo.save ( order );

        saved.getItems ( ).forEach ( oi -> recordTxn ( oi.getPart ( ), oi.getQuantity ( ), TransactionType.OUT,
                "Order #" + saved.getId ( ) ) );
        return saved;
    }

    private PaymentInitiationResponse startPaymentIfOnline(Order order) {
        return order.getPaymentMethod ( ) == PaymentMethod.ONLINE ? paymentService.initiate ( order ) : null;
    }

    /** Returns every item's quantity to stock. Locks parts in id order (same as checkout) to avoid deadlocks. */
    private void releaseStock(Order order, String note) {
        order.getItems ( ).stream ( ).sorted ( Comparator.comparing ( oi -> oi.getPart ( ).getId ( ) ) ).forEach ( oi -> {
            InventoryPart part = lockPart ( oi.getPart ( ).getId ( ) );
            part.setQuantityInStock ( stockOf ( part ) + oi.getQuantity ( ) );
            partRepo.save ( part );
            recordTxn ( part, oi.getQuantity ( ), TransactionType.IN, note );
        } );
    }

    /**
     * Loads the part and takes a row lock (SELECT ... FOR UPDATE), refreshing its state from the DB so the
     * stock check sees the latest committed quantity even if the entity was already loaded (e.g. via the cart).
     */
    private InventoryPart lockPart(Integer partId) {
        InventoryPart part = partRepo.findById ( partId )
                .orElseThrow ( () -> new ResourceNotFoundException ( "Part not found: " + partId ) );
        entityManager.refresh ( part, LockModeType.PESSIMISTIC_WRITE );
        return part;
    }

    private void recordTxn(InventoryPart part, int qty, TransactionType type, String note) {
        txnRepo.save ( InventoryTransaction.builder ( ).part ( part ).quantity ( qty ).transactionType ( type )
                .referenceNote ( note ).timestamp ( LocalDateTime.now ( ) ).build ( ) );
    }

    private Order loadOrder(Integer orderId) {
        return orderRepo.findById ( orderId ).orElseThrow ( () -> new ResourceNotFoundException ( "Order not found: " + orderId ) );
    }

    private Order lockOrder(Integer orderId) {
        return orderRepo.findByIdForUpdate ( orderId ).orElseThrow ( () -> new ResourceNotFoundException ( "Order not found: " + orderId ) );
    }

    private static void checkOwner(Order order, Integer vehicleOwnerId) {
        if ( vehicleOwnerId != null && !vehicleOwnerId.equals ( order.getVehicleOwnerId ( ) ) ) {
            throw new ResourceNotFoundException ( "Order not found: " + order.getId ( ) );
        }
    }

    private static int stockOf(InventoryPart part) {
        return part.getQuantityInStock ( ) == null ? 0 : part.getQuantityInStock ( );
    }

    private static OrderResponse withPayment(OrderResponse response, PaymentInitiationResponse payment) {
        response.setPayment ( payment );
        return response;
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getItems ( ).stream ( ).map ( oi -> {
            InventoryPart p = oi.getPart ( );
            double price = oi.getUnitPrice ( ) == null ? 0.0 : oi.getUnitPrice ( );
            return OrderItemResponse.builder ( ).orderItemId ( oi.getId ( ) ).partId ( p.getId ( ) ).productId ( p.getProductId ( ) )
                    .supplierId ( p.getSupplierId ( ) ).sku ( p.getSku ( ) ).partName ( p.getPartName ( ) )
                    .partNumber ( p.getPartNumber ( ) ).unitPrice ( price ).quantity ( oi.getQuantity ( ) )
                    .lineTotal ( price * oi.getQuantity ( ) ).build ( );
        } ).toList ( );

        return OrderResponse.builder ( ).orderId ( order.getId ( ) ).vehicleOwnerId ( order.getVehicleOwnerId ( ) )
                .status ( order.getStatus ( ) ).orderDate ( order.getOrderDate ( ) ).cancelledAt ( order.getCancelledAt ( ) )
                .paymentMethod ( order.getPaymentMethod ( ) ).paymentStatus ( order.getPaymentStatus ( ) )
                .paymentExpiresAt ( order.getPaymentExpiresAt ( ) ).paymentCompletedAt ( order.getPaymentCompletedAt ( ) )
                .items ( items ).totalItems ( items.stream ( ).mapToInt ( OrderItemResponse::getQuantity ).sum ( ) )
                .totalAmount ( order.getTotalAmount ( ) ).build ( );
    }
}
