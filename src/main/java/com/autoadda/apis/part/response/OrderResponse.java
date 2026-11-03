package com.autoadda.apis.part.response;

import com.autoadda.apis.part.util.PaymentMethod;
import com.autoadda.apis.part.util.PaymentStatus;
import com.autoadda.apis.part.util.Status;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    private Integer orderId;
    private Integer vehicleOwnerId;
    private Status status;
    private LocalDateTime orderDate;
    private LocalDateTime cancelledAt;
    @Builder.Default
    private List<OrderItemResponse> items = new ArrayList<> ( );
    private int totalItems;
    private Double totalAmount;

    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    /** ONLINE orders: stock is held until this time. */
    private LocalDateTime paymentExpiresAt;
    private LocalDateTime paymentCompletedAt;
    /** Present only right after an online payment is started: send the customer to paymentUrl / show the QR. */
    private PaymentInitiationResponse payment;
}
