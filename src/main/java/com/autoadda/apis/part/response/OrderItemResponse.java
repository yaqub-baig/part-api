package com.autoadda.apis.part.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponse {
    private Long orderItemId;
    private Integer partId;
    private String productId;
    private String supplierId;
    private String sku;
    private String partName;
    private String partNumber;
    private Double unitPrice;
    private Integer quantity;
    private Double lineTotal;
    private com.autoadda.apis.part.util.ProductCondition condition;
    /** PENDING, ACCEPTED, REJECTED, DISPATCHED, RECEIVED. */
    private com.autoadda.apis.part.util.FulfilmentStatus fulfilmentStatus;
    private java.time.LocalDateTime acceptedAt;
    private java.time.LocalDateTime dispatchedAt;
    private java.time.LocalDateTime receivedAt;
    private java.time.LocalDateTime rejectedAt;
    private String rejectReason;
    /** Garage orders: the car and the job the part is for. */
    private String vehicleMakeId;
    private String vehicleMakeName;
    private String vehicleModelId;
    private String vehicleModelName;
    private Integer vehicleYear;
    private Integer workOrderId;
    private Integer jobCardId;
    private Integer partRequestItemId;
    /** A job card update that hasn't reached the garage system yet (retried automatically). */
    private String pendingGarageSync;
    private String garageSyncError;
}
