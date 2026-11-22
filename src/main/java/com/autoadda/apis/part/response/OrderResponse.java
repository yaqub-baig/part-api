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
    /** CUSTOMER or GARAGE. */
    private com.autoadda.apis.part.util.BuyerType buyerType;
    /** Garage orders: the buyer and the delivery address. */
    private Integer garageId;
    private String garageName;
    private String garagePhone;
    private String garageAddress;
    private String garageMapLink;
    private String notes;
    /** DELIVERY or PICKUP. */
    private com.autoadda.apis.part.util.DeliveryMethod deliveryMethod;
    /** DELIVERY customer orders: where to deliver (contact name, phone, address, map link, notes). */
    private com.autoadda.apis.part.entity.DeliveryAddress deliveryAddress;
    /** PICKUP customer orders: who collects (contactName, phone, notes). */
    private com.autoadda.apis.part.entity.DeliveryAddress pickupContact;
    /** PICKUP customer orders: show it at the shop; the shop enters it to hand over the parts. */
    private String pickupCode;
    /** PICKUP orders: the shops to collect from (one per shop in the order). */
    private List<PickupPointResponse> pickupPoints;
    /** Paid online and then lines rejected by a shop: amount to refund. */
    private Double refundAmount;
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
