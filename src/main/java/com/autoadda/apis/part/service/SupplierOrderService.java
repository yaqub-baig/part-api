package com.autoadda.apis.part.service;

import com.autoadda.apis.part.clients.garage.MarketplaceStep;
import com.autoadda.apis.part.entity.GarageSnapshot;
import com.autoadda.apis.part.entity.Order;
import com.autoadda.apis.part.entity.OrderItem;
import com.autoadda.apis.part.entity.Supplier;
import com.autoadda.apis.part.exception.ConflictException;
import com.autoadda.apis.part.exception.ForbiddenException;
import com.autoadda.apis.part.exception.ResourceNotFoundException;
import com.autoadda.apis.part.exception.ValidationException;
import com.autoadda.apis.part.repository.OrderItemRepository;
import com.autoadda.apis.part.repository.OrderRepository;
import com.autoadda.apis.part.repository.SupplierRepository;
import com.autoadda.apis.part.response.OrderItemResponse;
import com.autoadda.apis.part.response.SupplierOrderResponse;
import com.autoadda.apis.part.util.BuyerType;
import com.autoadda.apis.part.util.CallerIdentity;
import com.autoadda.apis.part.util.DeliveryMethod;
import com.autoadda.apis.part.util.FulfilmentStatus;
import com.autoadda.apis.part.util.PaymentStatus;
import com.autoadda.apis.part.util.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A shop's order inbox (customer and garage orders) and its delivery steps: accept, reject, dispatch, and
 * mark delivered for customer orders (garages confirm receipt themselves). Only the shop owner may act.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierOrderService {

    private static final Set<Status> ACTIONABLE = EnumSet.of(Status.CONFIRMED, Status.SHIPPED, Status.READY_FOR_PICKUP);
    private static final Set<FulfilmentStatus> OPEN = EnumSet.of(FulfilmentStatus.PENDING, FulfilmentStatus.ACCEPTED);
    /** Lines a shop can still reject: not sent, not collected (a pickup line waiting at the shop can be). */
    private static final Set<FulfilmentStatus> REJECTABLE =
            EnumSet.of(FulfilmentStatus.PENDING, FulfilmentStatus.ACCEPTED, FulfilmentStatus.READY_FOR_PICKUP);

    private final SupplierRepository supplierRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final GarageOrderSync garageOrderSync;

    /** This shop's orders, newest first. buyerType / status (of the shop's lines) narrow it down. */
    @Transactional
    public List<SupplierOrderResponse> list(String autoAddaId, Integer supplierId, BuyerType buyerType,
                                            FulfilmentStatus status) {
        ownShop(autoAddaId, supplierId);
        Map<Integer, List<OrderItem>> byOrder = new LinkedHashMap<>();
        for (OrderItem line : orderItemRepository.findForSupplier(String.valueOf(supplierId))) {
            byOrder.computeIfAbsent(line.getOrder().getId(), id -> new java.util.ArrayList<>()).add(line);
        }
        return byOrder.values().stream()
                .filter(lines -> buyerType == null || buyerTypeOf(lines.get(0).getOrder()) == buyerType)
                .filter(lines -> status == null || lines.stream().anyMatch(l -> l.effectiveStatus() == status))
                .map(lines -> view(lines.get(0).getOrder(), lines))
                .toList();
    }

    @Transactional
    public SupplierOrderResponse get(String autoAddaId, Integer supplierId, Integer orderId) {
        ownShop(autoAddaId, supplierId);
        Order order = orderRepository.findById(orderId).orElseThrow(() -> notFound(orderId));
        return view(order, shopLines(order, supplierId));
    }

    @Transactional
    public SupplierOrderResponse accept(String autoAddaId, Integer supplierId, Integer orderId) {
        return act(autoAddaId, supplierId, orderId, (order, lines, now) -> {
            List<OrderItem> pending = lines.stream().filter(l -> l.effectiveStatus() == FulfilmentStatus.PENDING).toList();
            if (pending.isEmpty()) {
                throw new ConflictException("Nothing to accept in order " + orderId);
            }
            pending.forEach(l -> {
                l.setFulfilmentStatus(FulfilmentStatus.ACCEPTED);
                l.setAcceptedAt(now);
            });
        });
    }

    /** Rejects the shop's open lines: stock goes back, the buyer pays less (or is refunded), job cards are freed. */
    @Transactional
    public SupplierOrderResponse reject(String autoAddaId, Integer supplierId, Integer orderId, String reason) {
        if (reason != null && reason.length() > 500) {
            throw new ValidationException("Reason is longer than 500 characters");
        }
        return act(autoAddaId, supplierId, orderId, (order, lines, now) -> {
            List<OrderItem> open = lines.stream().filter(l -> REJECTABLE.contains(l.effectiveStatus())).toList();
            if (open.isEmpty()) {
                throw new ConflictException("Nothing to reject in order " + orderId
                        + "; dispatched or collected lines can't be rejected");
            }
            double amount = 0;
            for (OrderItem l : open) {
                l.setFulfilmentStatus(FulfilmentStatus.REJECTED);
                l.setRejectedAt(now);
                l.setRejectReason(reason == null || reason.isBlank() ? null : reason.trim());
                amount += (l.getUnitPrice() == null ? 0 : l.getUnitPrice()) * l.getQuantity();
            }
            orderService.releaseStock(open, "Order #" + orderId + " rejected by shop " + supplierId);
            if (order.getPaymentStatus() == PaymentStatus.SUCCESS) {
                order.setRefundAmount((order.getRefundAmount() == null ? 0 : order.getRefundAmount()) + amount);
                log.warn("Paid order {}: shop {} rejected lines worth {}; refund required", orderId, supplierId, amount);
            } else {
                order.setTotalAmount((order.getTotalAmount() == null ? 0 : order.getTotalAmount()) - amount);
            }
            OrderService.refreshStatus(order);
            if (order.getStatus() == Status.CANCELLED) {
                order.setPaymentStatus(order.getPaymentStatus() == PaymentStatus.SUCCESS
                        ? PaymentStatus.REFUND_PENDING : PaymentStatus.CANCELLED);
            }
            if (order.getBuyerType() == BuyerType.GARAGE) {
                open.forEach(l -> garageOrderSync.send(l, MarketplaceStep.Stage.CANCELLED, order.getPlacedByUserId()));
            }
        });
    }

    @Transactional
    public SupplierOrderResponse dispatch(String autoAddaId, Integer supplierId, Integer orderId) {
        return act(autoAddaId, supplierId, orderId, (order, lines, now) -> {
            if (order.effectiveDeliveryMethod() == DeliveryMethod.PICKUP) {
                throw new ConflictException("Order " + orderId + " is collected at the shop; mark it ready for pickup");
            }
            List<OrderItem> open = lines.stream().filter(l -> OPEN.contains(l.effectiveStatus())).toList();
            if (open.isEmpty()) {
                throw new ConflictException("Nothing to dispatch in order " + orderId);
            }
            open.forEach(l -> {
                if (l.getAcceptedAt() == null) {
                    l.setAcceptedAt(now);
                }
                l.setFulfilmentStatus(FulfilmentStatus.DISPATCHED);
                l.setDispatchedAt(now);
            });
            OrderService.refreshStatus(order);
        });
    }

    /** Pickup orders: the shop's open lines are packed and waiting at the shop. */
    @Transactional
    public SupplierOrderResponse readyForPickup(String autoAddaId, Integer supplierId, Integer orderId) {
        return act(autoAddaId, supplierId, orderId, (order, lines, now) -> {
            if (order.effectiveDeliveryMethod() != DeliveryMethod.PICKUP) {
                throw new ConflictException("Order " + orderId + " is delivered; use dispatch");
            }
            List<OrderItem> open = lines.stream().filter(l -> OPEN.contains(l.effectiveStatus())).toList();
            if (open.isEmpty()) {
                throw new ConflictException("Nothing to prepare in order " + orderId);
            }
            open.forEach(l -> {
                if (l.getAcceptedAt() == null) {
                    l.setAcceptedAt(now);
                }
                l.setFulfilmentStatus(FulfilmentStatus.READY_FOR_PICKUP);
                l.setDispatchedAt(now);
            });
            OrderService.refreshStatus(order);
        });
    }

    /**
     * Customer pickup orders: the customer collected the shop's ready lines. The shop enters the customer's pickup
     * code (400 when wrong). Garage pickups are confirmed by the garage instead.
     */
    @Transactional
    public SupplierOrderResponse collected(String autoAddaId, Integer supplierId, Integer orderId, String pickupCode) {
        return act(autoAddaId, supplierId, orderId, (order, lines, now) -> {
            if (order.effectiveDeliveryMethod() != DeliveryMethod.PICKUP) {
                throw new ConflictException("Order " + orderId + " is delivered, not collected");
            }
            if (order.getBuyerType() == BuyerType.GARAGE) {
                throw new ConflictException("The garage confirms collection of garage orders");
            }
            if (pickupCode == null || !pickupCode.trim().equals(order.getPickupCode())) {
                throw new ValidationException("Wrong pickup code");
            }
            List<OrderItem> ready = lines.stream().filter(l -> l.effectiveStatus() == FulfilmentStatus.READY_FOR_PICKUP).toList();
            if (ready.isEmpty()) {
                throw new ConflictException("Mark the parts ready for pickup first");
            }
            ready.forEach(l -> {
                l.setFulfilmentStatus(FulfilmentStatus.RECEIVED);
                l.setReceivedAt(now);
            });
            OrderService.refreshStatus(order);
        });
    }

    /** Customer delivery orders: the shop confirms delivery (garages confirm receipt themselves). */
    @Transactional
    public SupplierOrderResponse delivered(String autoAddaId, Integer supplierId, Integer orderId) {
        return act(autoAddaId, supplierId, orderId, (order, lines, now) -> {
            if (order.effectiveDeliveryMethod() == DeliveryMethod.PICKUP) {
                throw new ConflictException("Order " + orderId + " is a pickup order; use collected with the pickup code");
            }
            if (order.getBuyerType() == BuyerType.GARAGE) {
                throw new ConflictException("The garage confirms receipt of garage orders");
            }
            List<OrderItem> sent = lines.stream().filter(l -> l.effectiveStatus() == FulfilmentStatus.DISPATCHED).toList();
            if (sent.isEmpty()) {
                throw new ConflictException("Dispatch the parts before marking them delivered");
            }
            sent.forEach(l -> {
                l.setFulfilmentStatus(FulfilmentStatus.RECEIVED);
                l.setReceivedAt(now);
            });
            OrderService.refreshStatus(order);
        });
    }

    private interface Action {
        void apply(Order order, List<OrderItem> shopLines, LocalDateTime now);
    }

    private SupplierOrderResponse act(String autoAddaId, Integer supplierId, Integer orderId, Action action) {
        ownShop(autoAddaId, supplierId);
        Order order = orderService.lockOrder(orderId);
        List<OrderItem> lines = shopLines(order, supplierId);
        if (!ACTIONABLE.contains(order.getStatus())) {
            throw new ConflictException(order.getStatus() == Status.PENDING_PAYMENT
                    ? "Order " + orderId + " is awaiting online payment" : "Order " + orderId + " is " + order.getStatus());
        }
        action.apply(order, lines, LocalDateTime.now());
        orderRepository.save(order);
        return view(order, lines);
    }

    private static List<OrderItem> shopLines(Order order, Integer supplierId) {
        String id = String.valueOf(supplierId);
        List<OrderItem> lines = order.getItems().stream().filter(l -> id.equals(l.getPart().getSupplierId())).toList();
        if (lines.isEmpty()) {
            throw notFound(order.getId());
        }
        return lines;
    }

    private void ownShop(String autoAddaId, Integer supplierId) {
        Supplier shop = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"));
        if (!CallerIdentity.callerId(autoAddaId).equals(shop.getSparePartShopOwner())) {
            throw new ForbiddenException("You are not the owner of this shop");
        }
    }

    private static ResourceNotFoundException notFound(Integer orderId) {
        return new ResourceNotFoundException("Order not found: " + orderId);
    }

    private static BuyerType buyerTypeOf(Order order) {
        return order.getBuyerType() == null ? BuyerType.CUSTOMER : order.getBuyerType();
    }

    private static SupplierOrderResponse view(Order order, List<OrderItem> lines) {
        List<OrderItemResponse> items = lines.stream().map(OrderService::itemResponse).toList();
        double total = lines.stream().filter(l -> l.effectiveStatus() != FulfilmentStatus.REJECTED)
                .mapToDouble(l -> (l.getUnitPrice() == null ? 0 : l.getUnitPrice()) * l.getQuantity()).sum();
        GarageSnapshot garage = order.getGarage();
        return SupplierOrderResponse.builder()
                .orderId(order.getId())
                .orderDate(order.getOrderDate())
                .orderStatus(order.getStatus())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .buyerType(buyerTypeOf(order))
                .vehicleOwnerId(order.getVehicleOwnerId())
                .garageId(order.getGarageId())
                .garageName(garage == null ? null : garage.getGarageName())
                .garagePhone(garage == null ? null : garage.getGaragePhone())
                .garageAddress(garage == null ? null : garage.getGarageAddress())
                .garageMapLink(garage == null ? null : garage.getGarageMapLink())
                .notes(order.getNotes())
                .deliveryMethod(order.effectiveDeliveryMethod())
                .deliveryAddress(order.effectiveDeliveryMethod() == DeliveryMethod.DELIVERY ? order.getDeliveryAddress() : null)
                .pickupContact(order.effectiveDeliveryMethod() == DeliveryMethod.PICKUP ? order.getDeliveryAddress() : null)
                .items(items)
                .shopTotal(total)
                .build();
    }
}
