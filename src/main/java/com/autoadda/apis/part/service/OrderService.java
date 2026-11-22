package com.autoadda.apis.part.service;

import com.autoadda.apis.part.entity.DeliveryAddress;
import com.autoadda.apis.part.entity.GarageSnapshot;
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
import com.autoadda.apis.part.repository.SupplierRepository;
import com.autoadda.apis.part.entity.Supplier;
import com.autoadda.apis.part.response.PickupPointResponse;
import com.autoadda.apis.part.util.DeliveryMethod;
import java.security.SecureRandom;
import com.autoadda.apis.part.response.OrderItemResponse;
import com.autoadda.apis.part.response.OrderResponse;
import com.autoadda.apis.part.response.PaymentInitiationResponse;
import com.autoadda.apis.part.util.BuyerType;
import com.autoadda.apis.part.util.FulfilmentStatus;
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
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    /** Orders in these states can still be cancelled (stock is returned to inventory). */
    private static final Set<Status> CANCELLABLE = Set.of ( Status.CREATED, Status.CONFIRMED, Status.PENDING_PAYMENT,
            Status.READY_FOR_PICKUP );
    /** Once a shop has sent a line (or it was collected), the order can no longer be cancelled by the buyer. */
    private static final Set<FulfilmentStatus> ON_THE_WAY = EnumSet.of ( FulfilmentStatus.DISPATCHED, FulfilmentStatus.RECEIVED );
    /** Lines that are with the buyer or waiting for them: drives SHIPPED / READY_FOR_PICKUP. */
    private static final Set<FulfilmentStatus> HANDED_OVER_OR_READY =
            EnumSet.of ( FulfilmentStatus.DISPATCHED, FulfilmentStatus.READY_FOR_PICKUP, FulfilmentStatus.RECEIVED );
    private static final SecureRandom RANDOM = new SecureRandom ( );

    /** Who the order is for. Customer: vehicleOwnerId. Garage: garageId, the user acting for it and its snapshot. */
    public record OrderBuyer(BuyerType type, Integer vehicleOwnerId, Integer garageId, Integer placedByUserId,
                             GarageSnapshot garage, String notes, DeliveryMethod deliveryMethod,
                             DeliveryAddress deliveryAddress) {
        /** A customer order delivered to {@code address} (validated and cleaned here). */
        public static OrderBuyer customer(Integer vehicleOwnerId, DeliveryAddress address) {
            return customer ( vehicleOwnerId, new Checkout ( DeliveryMethod.DELIVERY, address, null ) );
        }

        /** A customer order: delivery needs the address, pickup needs who collects (validated here). */
        public static OrderBuyer customer(Integer vehicleOwnerId, Checkout checkout) {
            DeliveryMethod method = checkout.method ( ) == null ? DeliveryMethod.DELIVERY : checkout.method ( );
            DeliveryAddress where = method == DeliveryMethod.PICKUP
                    ? DeliveryAddresses.validatedContact ( checkout.pickupContact ( ) )
                    : DeliveryAddresses.validated ( checkout.deliveryAddress ( ) );
            return new OrderBuyer ( BuyerType.CUSTOMER, vehicleOwnerId, null, null, null, null, method, where );
        }
    }

    /** How the customer gets the parts: method, delivery address or pickup contact. */
    public record Checkout(DeliveryMethod method, DeliveryAddress deliveryAddress, DeliveryAddress pickupContact) {
    }

    /** One order line: the listing, how many, and anything extra to record on the line (car, garage job). */
    public record OrderLine(Integer partId, int quantity, Consumer<OrderItem> decorate) {
        public OrderLine(Integer partId, int quantity) {
            this ( partId, quantity, item -> { } );
        }
    }

    private final ShoppingCartRepository cartRepo;
    private final OrderRepository orderRepo;
    private final InventoryPartRepository partRepo;
    private final InventoryTransactionRepository txnRepo;
    private final EntityManager entityManager;
    private final PaymentService paymentService;
    private final SupplierRepository supplierRepo;

    /**
     * Checks out the whole cart: validates and deducts stock, creates the order, empties the cart.
     * ONLINE orders also start a gateway payment; if that fails everything rolls back and the cart is kept.
     */
    @Transactional
    public OrderResponse placeOrder(Integer vehicleOwnerId, PaymentMethod paymentMethod, DeliveryAddress deliveryAddress) {
        return placeOrder ( vehicleOwnerId, paymentMethod, new Checkout ( DeliveryMethod.DELIVERY, deliveryAddress, null ) );
    }

    @Transactional
    public OrderResponse placeOrder(Integer vehicleOwnerId, PaymentMethod paymentMethod, Checkout checkout) {
        OrderBuyer buyer = OrderBuyer.customer ( vehicleOwnerId, checkout );
        ShoppingCart cart = cartRepo.findByVehicleOwnerId ( vehicleOwnerId )
                .orElseThrow ( () -> new ResourceNotFoundException ( "Cart not found for vehicle owner " + vehicleOwnerId ) );
        if ( cart.getItems ( ).isEmpty ( ) ) {
            throw new ConflictException ( "Cart is empty" );
        }

        Map<Integer, Integer> quantities = new LinkedHashMap<> ( );
        cart.getItems ( ).forEach ( ci -> quantities.merge ( ci.getPart ( ).getId ( ), ci.getQuantity ( ), Integer::sum ) );

        Order order = createOrder ( buyer, lines ( quantities ), paymentMethod );
        PaymentInitiationResponse payment = startPaymentIfOnline ( order );

        cart.getItems ( ).clear ( );
        cart.setUpdatedAt ( LocalDateTime.now ( ) );
        cartRepo.save ( cart );
        return withPayment ( toResponse ( order ), payment );
    }

    /** "Buy now": orders a single part directly without touching the cart. */
    @Transactional
    public OrderResponse buyNow(Integer vehicleOwnerId, Integer partId, int quantity, PaymentMethod paymentMethod,
                                DeliveryAddress deliveryAddress) {
        return buyNow ( vehicleOwnerId, partId, quantity, paymentMethod,
                new Checkout ( DeliveryMethod.DELIVERY, deliveryAddress, null ) );
    }

    @Transactional
    public OrderResponse buyNow(Integer vehicleOwnerId, Integer partId, int quantity, PaymentMethod paymentMethod,
                                Checkout checkout) {
        OrderBuyer buyer = OrderBuyer.customer ( vehicleOwnerId, checkout );
        ShoppingCartService.requirePositive ( quantity );
        Order order = createOrder ( buyer, List.of ( new OrderLine ( partId, quantity ) ), paymentMethod );
        return withPayment ( toResponse ( order ), startPaymentIfOnline ( order ) );
    }

    /** Starts a new gateway attempt for an unpaid ONLINE order whose previous attempt failed. */
    @Transactional
    public OrderResponse retryPayment(Integer orderId, Integer vehicleOwnerId) {
        return retryPaymentFor ( orderId, customerOwns ( vehicleOwnerId ) );
    }

    /** Same as {@link #retryPayment(Integer, Integer)} for any buyer; {@code owns} decides whose order it is. */
    @Transactional
    public OrderResponse retryPaymentFor(Integer orderId, Predicate<Order> owns) {
        Order order = lockOrder ( orderId );
        checkOwner ( order, owns );
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
    public OrderResponse getOrder(Integer orderId, Integer vehicleOwnerId) {
        return getOrderFor ( orderId, customerOwns ( vehicleOwnerId ) );
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderFor(Integer orderId, Predicate<Order> owns) {
        Order order = loadOrder ( orderId );
        checkOwner ( order, owns );
        return toResponse ( order );
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersForOwner(Integer vehicleOwnerId) {
        return orderRepo.findByVehicleOwnerIdOrderByOrderDateDesc ( vehicleOwnerId ).stream ( ).map ( this::toResponse ).toList ( );
    }

    /** Cancels an order that has not shipped yet and puts the stock back. */
    @Transactional
    public OrderResponse cancelOrder(Integer orderId, Integer vehicleOwnerId) {
        return toResponse ( cancelOrderFor ( orderId, customerOwns ( vehicleOwnerId ) ) );
    }

    /** Cancels any buyer's order (stock back, payment status updated) and returns it. */
    @Transactional
    public Order cancelOrderFor(Integer orderId, Predicate<Order> owns) {
        Order order = lockOrder ( orderId );
        checkOwner ( order, owns );
        if ( !CANCELLABLE.contains ( order.getStatus ( ) ) ) {
            throw new ConflictException ( "Order " + orderId + " cannot be cancelled in status " + order.getStatus ( ) );
        }
        if ( order.getItems ( ).stream ( ).anyMatch ( oi -> ON_THE_WAY.contains ( oi.effectiveStatus ( ) ) ) ) {
            throw new ConflictException ( "Order " + orderId + " cannot be cancelled: a shop has already sent part of it" );
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
        return orderRepo.save ( order );
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
    public Order createOrder(OrderBuyer buyer, List<OrderLine> lines, PaymentMethod paymentMethod) {
        if ( lines.isEmpty ( ) ) {
            throw new ConflictException ( "Order has no items" );
        }
        PaymentMethod method = paymentMethod == null ? PaymentMethod.COD : paymentMethod;
        Order order = Order.builder ( ).vehicleOwnerId ( buyer.vehicleOwnerId ( ) ).orderDate ( LocalDateTime.now ( ) )
                .buyerType ( buyer.type ( ) ).garageId ( buyer.garageId ( ) ).placedByUserId ( buyer.placedByUserId ( ) )
                .garage ( buyer.garage ( ) ).notes ( buyer.notes ( ) ).deliveryAddress ( buyer.deliveryAddress ( ) )
                .deliveryMethod ( buyer.deliveryMethod ( ) == null ? DeliveryMethod.DELIVERY : buyer.deliveryMethod ( ) )
                .pickupCode ( buyer.deliveryMethod ( ) == DeliveryMethod.PICKUP && buyer.type ( ) == BuyerType.CUSTOMER
                        ? String.format ( "%06d", RANDOM.nextInt ( 1_000_000 ) ) : null )
                .paymentMethod ( method )
                .status ( method == PaymentMethod.COD ? Status.CONFIRMED : Status.PENDING_PAYMENT )
                .paymentStatus ( method == PaymentMethod.COD ? PaymentStatus.PENDING : PaymentStatus.NOT_INITIATED )
                .build ( );

        // Stock is checked and deducted per listing (several lines may share one); sorted lock order prevents
        // deadlocks between concurrent checkouts
        Map<Integer, Integer> quantitiesByPart = new LinkedHashMap<> ( );
        lines.forEach ( line -> quantitiesByPart.merge ( line.partId ( ), line.quantity ( ), Integer::sum ) );
        Map<Integer, InventoryPart> parts = new LinkedHashMap<> ( );
        for ( Integer partId : quantitiesByPart.keySet ( ).stream ( ).sorted ( ).toList ( ) ) {
            int qty = quantitiesByPart.get ( partId );
            InventoryPart part = lockPart ( partId );
            if ( !Boolean.TRUE.equals ( part.getIsActive ( ) ) ) {
                throw new ConflictException ( "Part " + partId + " is not available for sale" );
            }
            ShoppingCartService.ensureStock ( part, qty );
            part.setQuantityInStock ( stockOf ( part ) - qty );
            partRepo.save ( part );
            parts.put ( partId, part );
        }

        double total = 0;
        for ( OrderLine line : lines ) {
            InventoryPart part = parts.get ( line.partId ( ) );
            double price = part.getUnitPrice ( ) == null ? 0.0 : part.getUnitPrice ( );
            total += price * line.quantity ( );
            OrderItem item = OrderItem.builder ( ).order ( order ).part ( part ).quantity ( line.quantity ( ) ).unitPrice ( price )
                    .fulfilmentStatus ( FulfilmentStatus.PENDING ).build ( );
            line.decorate ( ).accept ( item );
            order.getItems ( ).add ( item );
        }
        order.setTotalAmount ( total );
        Order saved = orderRepo.save ( order );

        saved.getItems ( ).forEach ( oi -> recordTxn ( oi.getPart ( ), oi.getQuantity ( ), TransactionType.OUT,
                "Order #" + saved.getId ( ) ) );
        return saved;
    }

    private static List<OrderLine> lines(Map<Integer, Integer> quantities) {
        return quantities.entrySet ( ).stream ( ).map ( e -> new OrderLine ( e.getKey ( ), e.getValue ( ) ) ).toList ( );
    }

    public PaymentInitiationResponse startPaymentIfOnline(Order order) {
        return order.getPaymentMethod ( ) == PaymentMethod.ONLINE ? paymentService.initiate ( order ) : null;
    }

    /** Returns every line's quantity to stock (lines a shop rejected were returned already). */
    private void releaseStock(Order order, String note) {
        releaseStock ( order.getItems ( ).stream ( ).filter ( oi -> oi.effectiveStatus ( ) != FulfilmentStatus.REJECTED ).toList ( ), note );
    }

    /** Returns these lines' quantities to stock. Locks parts in id order (same as checkout) to avoid deadlocks. */
    public void releaseStock(Collection<OrderItem> items, String note) {
        items.stream ( ).sorted ( Comparator.comparing ( oi -> oi.getPart ( ).getId ( ) ) ).forEach ( oi -> {
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

    public Order lockOrder(Integer orderId) {
        return orderRepo.findByIdForUpdate ( orderId ).orElseThrow ( () -> new ResourceNotFoundException ( "Order not found: " + orderId ) );
    }

    /** Someone else's order answers 404, so order ids can't be probed. */
    public static void checkOwner(Order order, Predicate<Order> owns) {
        if ( !owns.test ( order ) ) {
            throw new ResourceNotFoundException ( "Order not found: " + order.getId ( ) );
        }
    }

    public static Predicate<Order> customerOwns(Integer vehicleOwnerId) {
        return order -> vehicleOwnerId != null && order.getBuyerType ( ) != BuyerType.GARAGE
                && vehicleOwnerId.equals ( order.getVehicleOwnerId ( ) );
    }

    public static Predicate<Order> garageOwns(Integer garageId) {
        return order -> garageId != null && order.getBuyerType ( ) == BuyerType.GARAGE && garageId.equals ( order.getGarageId ( ) );
    }

    /**
     * Order status from its lines: all rejected -> CANCELLED, all others received -> DELIVERED (pickup: COLLECTED),
     * all others dispatched / ready / received -> SHIPPED (pickup: READY_FOR_PICKUP). Unpaid orders are left alone.
     */
    public static void refreshStatus(Order order) {
        if ( order.getStatus ( ) == Status.PENDING_PAYMENT || order.getStatus ( ) == Status.CANCELLED ) {
            return;
        }
        List<FulfilmentStatus> live = order.getItems ( ).stream ( ).map ( OrderItem::effectiveStatus )
                .filter ( st -> st != FulfilmentStatus.REJECTED ).toList ( );
        if ( live.isEmpty ( ) ) {
            order.setStatus ( Status.CANCELLED );
            order.setCancelledAt ( LocalDateTime.now ( ) );
        } else if ( live.stream ( ).allMatch ( st -> st == FulfilmentStatus.RECEIVED ) ) {
            order.setStatus ( order.effectiveDeliveryMethod ( ) == DeliveryMethod.PICKUP ? Status.COLLECTED : Status.DELIVERED );
        } else if ( live.stream ( ).allMatch ( HANDED_OVER_OR_READY::contains ) ) {
            order.setStatus ( order.effectiveDeliveryMethod ( ) == DeliveryMethod.PICKUP ? Status.READY_FOR_PICKUP : Status.SHIPPED );
        }
    }

    private static int stockOf(InventoryPart part) {
        return part.getQuantityInStock ( ) == null ? 0 : part.getQuantityInStock ( );
    }

    private static OrderResponse withPayment(OrderResponse response, PaymentInitiationResponse payment) {
        response.setPayment ( payment );
        return response;
    }

    public OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getItems ( ).stream ( ).map ( OrderService::itemResponse ).toList ( );
        GarageSnapshot garage = order.getGarage ( );
        return OrderResponse.builder ( ).orderId ( order.getId ( ) ).vehicleOwnerId ( order.getVehicleOwnerId ( ) )
                .buyerType ( order.getBuyerType ( ) == null ? BuyerType.CUSTOMER : order.getBuyerType ( ) )
                .garageId ( order.getGarageId ( ) )
                .garageName ( garage == null ? null : garage.getGarageName ( ) )
                .garagePhone ( garage == null ? null : garage.getGaragePhone ( ) )
                .garageAddress ( garage == null ? null : garage.getGarageAddress ( ) )
                .garageMapLink ( garage == null ? null : garage.getGarageMapLink ( ) )
                .notes ( order.getNotes ( ) ).refundAmount ( order.getRefundAmount ( ) )
                .deliveryMethod ( order.effectiveDeliveryMethod ( ) )
                .deliveryAddress ( order.effectiveDeliveryMethod ( ) == DeliveryMethod.DELIVERY ? order.getDeliveryAddress ( ) : null )
                .pickupContact ( order.effectiveDeliveryMethod ( ) == DeliveryMethod.PICKUP ? order.getDeliveryAddress ( ) : null )
                .pickupCode ( order.getPickupCode ( ) )
                .pickupPoints ( order.effectiveDeliveryMethod ( ) == DeliveryMethod.PICKUP ? pickupPoints ( order ) : null )
                .status ( order.getStatus ( ) ).orderDate ( order.getOrderDate ( ) ).cancelledAt ( order.getCancelledAt ( ) )
                .paymentMethod ( order.getPaymentMethod ( ) ).paymentStatus ( order.getPaymentStatus ( ) )
                .paymentExpiresAt ( order.getPaymentExpiresAt ( ) ).paymentCompletedAt ( order.getPaymentCompletedAt ( ) )
                .items ( items ).totalItems ( items.stream ( ).mapToInt ( OrderItemResponse::getQuantity ).sum ( ) )
                .totalAmount ( order.getTotalAmount ( ) ).build ( );
    }

    /** One pickup point per shop in the order, with its address and opening hours. */
    private List<PickupPointResponse> pickupPoints(Order order) {
        Set<String> shopIds = new java.util.LinkedHashSet<> ( );
        order.getItems ( ).stream ( ).filter ( oi -> oi.effectiveStatus ( ) != FulfilmentStatus.REJECTED )
                .forEach ( oi -> shopIds.add ( oi.getPart ( ).getSupplierId ( ) ) );
        return shopIds.stream ( ).map ( id -> {
            Supplier shop = null;
            try {
                shop = id == null ? null : supplierRepo.findById ( Integer.valueOf ( id ) ).orElse ( null );
            } catch ( NumberFormatException ignored ) {
                // unknown shop id format: show the id only
            }
            if ( shop == null ) {
                return PickupPointResponse.builder ( ).supplierId ( id ).build ( );
            }
            var loc = shop.getLocation ( );
            String address = loc == null ? null : java.util.stream.Stream.of ( loc.getStreetAddress1 ( ), loc.getStreetAddress2 ( ),
                            loc.getArea ( ), loc.getCity ( ), loc.getState ( ) )
                    .filter ( v -> v != null && !v.isBlank ( ) ).map ( String::trim )
                    .collect ( java.util.stream.Collectors.joining ( ", " ) );
            return PickupPointResponse.builder ( ).supplierId ( id ).name ( shop.getName ( ) )
                    .phone ( shop.getMobileNumber ( ) != null ? shop.getMobileNumber ( ) : shop.getPhoneNumber ( ) )
                    .address ( address == null || address.isEmpty ( ) ? null : address )
                    .mapLink ( loc == null ? null : loc.getGmap ( ) )
                    .workingDays ( shop.getWorkingDays ( ) ).openingHours ( shop.getOpeningHours ( ) )
                    .closingHours ( shop.getClosingHours ( ) ).build ( );
        } ).toList ( );
    }

    public static OrderItemResponse itemResponse(OrderItem oi) {
        InventoryPart p = oi.getPart ( );
        double price = oi.getUnitPrice ( ) == null ? 0.0 : oi.getUnitPrice ( );
        var vehicle = oi.getVehicle ( );
        return OrderItemResponse.builder ( ).orderItemId ( oi.getId ( ) ).partId ( p.getId ( ) ).productId ( p.getProductId ( ) )
                .supplierId ( p.getSupplierId ( ) ).sku ( p.getSku ( ) ).partName ( p.getPartName ( ) )
                .partNumber ( p.getPartNumber ( ) ).unitPrice ( price ).quantity ( oi.getQuantity ( ) )
                .lineTotal ( price * oi.getQuantity ( ) ).condition ( p.getProductCondition ( ) )
                .fulfilmentStatus ( oi.effectiveStatus ( ) ).acceptedAt ( oi.getAcceptedAt ( ) )
                .dispatchedAt ( oi.getDispatchedAt ( ) ).receivedAt ( oi.getReceivedAt ( ) )
                .rejectedAt ( oi.getRejectedAt ( ) ).rejectReason ( oi.getRejectReason ( ) )
                .vehicleMakeId ( vehicle == null ? null : vehicle.getMakeId ( ) )
                .vehicleMakeName ( vehicle == null ? null : vehicle.getMakeName ( ) )
                .vehicleModelId ( vehicle == null ? null : vehicle.getModelId ( ) )
                .vehicleModelName ( vehicle == null ? null : vehicle.getModelName ( ) )
                .vehicleYear ( vehicle == null ? null : vehicle.getYear ( ) )
                .workOrderId ( oi.getWorkOrderId ( ) ).jobCardId ( oi.getJobCardId ( ) )
                .partRequestItemId ( oi.getPartRequestItemId ( ) )
                .pendingGarageSync ( oi.getPendingGarageSync ( ) == null ? null : oi.getPendingGarageSync ( ).name ( ) )
                .garageSyncError ( oi.getGarageSyncError ( ) )
                .build ( );
    }
}
