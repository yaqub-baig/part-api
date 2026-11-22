package com.autoadda.apis.part.entity;

import com.autoadda.apis.part.util.BuyerType;
import com.autoadda.apis.part.util.DeliveryMethod;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.autoadda.apis.part.util.PaymentMethod;
import com.autoadda.apis.part.util.PaymentStatus;
import com.autoadda.apis.part.util.Status;
import lombok.*;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** Set for customer orders. */
    private Integer vehicleOwnerId;

    /** CUSTOMER or GARAGE; null on old rows = CUSTOMER. */
    @Enumerated(EnumType.STRING)
    @Column(name = "buyer_type", length = 20)
    private BuyerType buyerType;
    /** Garage orders: the garage (the buyer), who placed it (audit) and where to deliver. */
    @Column(name = "garage_id")
    private Integer garageId;
    @Column(name = "placed_by_user_id")
    private Integer placedByUserId;
    @Embedded
    private GarageSnapshot garage;
    @Column(name = "notes", length = 1000)
    private String notes;
    /** DELIVERY (default; null on old rows) or PICKUP at the shops. */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "delivery_method", length = 20)
    private DeliveryMethod deliveryMethod;
    /**
     * Customer orders: where to deliver; for PICKUP only the contact name, phone and notes are set.
     * Garage orders deliver to the garage snapshot.
     */
    @Embedded
    private DeliveryAddress deliveryAddress;
    /** Customer PICKUP orders: code the customer shows at the shop; the shop enters it to hand over the parts. */
    @Column(name = "pickup_code", length = 6)
    private String pickupCode;

    public DeliveryMethod effectiveDeliveryMethod() {
        return deliveryMethod == null ? DeliveryMethod.DELIVERY : deliveryMethod;
    }
    /** Paid online, then lines rejected by a shop: amount to refund to the buyer. */
    @Column(name = "refund_amount")
    private Double refundAmount;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<> ( );

    private LocalDateTime orderDate;

    /** Sum of (unitPrice * quantity) across all items, captured at order time. */
    private Double totalAmount;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(length = 30)
    private Status status; // PENDING_PAYMENT -> CONFIRMED -> SHIPPED -> DELIVERED (pickup: READY_FOR_PICKUP -> COLLECTED), or CANCELLED / PENDING_REFUND

    private LocalDateTime cancelledAt;

    // ---- payment ----

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    /** Our reference for the current gateway attempt (a new one per retry). */
    @Column(unique = true)
    private String gatewayOrderReference;

    private String gatewayTransactionId;

    private LocalDateTime paymentInitiatedAt;

    private LocalDateTime paymentCompletedAt;

    /** ONLINE orders: stock is held until this time; after it the order is cancelled if still unpaid. */
    private LocalDateTime paymentExpiresAt;
}
