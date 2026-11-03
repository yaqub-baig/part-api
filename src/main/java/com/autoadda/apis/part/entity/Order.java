package com.autoadda.apis.part.entity;

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

    private Integer vehicleOwnerId;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<> ( );

    private LocalDateTime orderDate;

    /** Sum of (unitPrice * quantity) across all items, captured at order time. */
    private Double totalAmount;

    @Enumerated(EnumType.STRING)
    private Status status; // PENDING_PAYMENT -> CONFIRMED -> SHIPPED -> DELIVERED, or CANCELLED / PENDING_REFUND

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
