package com.autoadda.apis.part.entity;

import com.autoadda.apis.part.clients.garage.MarketplaceStep;
import com.autoadda.apis.part.util.FulfilmentStatus;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;

import lombok.*;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private InventoryPart part;

    private int quantity;

    /** Price per unit at the moment the order was placed (inventory price may change later). */
    private Double unitPrice;

    @ManyToOne
    private Order order;

    /** Delivery progress of this line; null on old rows = PENDING. */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "fulfilment_status", length = 20)
    private FulfilmentStatus fulfilmentStatus;
    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;
    @Column(name = "dispatched_at")
    private LocalDateTime dispatchedAt;
    @Column(name = "received_at")
    private LocalDateTime receivedAt;
    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;
    @Column(name = "reject_reason", length = 500)
    private String rejectReason;

    /** Garage orders: the car and the garage job this part is for. */
    @Embedded
    private VehicleSnapshot vehicle;
    @Column(name = "work_order_id")
    private Integer workOrderId;
    @Column(name = "job_card_id")
    private Integer jobCardId;
    @Column(name = "part_request_item_id")
    private Integer partRequestItemId;

    /** A step garage-api still has to be told about (retried hourly), and as which user. Null when in sync. */
    @Enumerated(EnumType.STRING)
    @Column(name = "pending_garage_sync", length = 20)
    private MarketplaceStep.Stage pendingGarageSync;
    @Column(name = "garage_sync_user_id")
    private Integer garageSyncUserId;
    /** Why garage-api refused the last update (not retried); shown so the garage can fix the job card by hand. */
    @Column(name = "garage_sync_error", length = 500)
    private String garageSyncError;

    public FulfilmentStatus effectiveStatus() {
        return fulfilmentStatus == null ? FulfilmentStatus.PENDING : fulfilmentStatus;
    }
}
