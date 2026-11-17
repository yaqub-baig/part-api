package com.autoadda.apis.part.response;

import com.autoadda.apis.part.util.BuyerType;
import com.autoadda.apis.part.util.PaymentMethod;
import com.autoadda.apis.part.util.PaymentStatus;
import com.autoadda.apis.part.util.Status;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/** An order as one shop sees it: the buyer, where to deliver, and only this shop's lines. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SupplierOrderResponse {
    private Integer orderId;
    private LocalDateTime orderDate;
    private Status orderStatus;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private BuyerType buyerType;
    /** Customer orders. */
    private Integer vehicleOwnerId;
    /** Garage orders: deliver to this garage. */
    private Integer garageId;
    private String garageName;
    private String garagePhone;
    private String garageAddress;
    private String garageMapLink;
    private String notes;
    private List<OrderItemResponse> items;
    /** Sum of this shop's lines that weren't rejected. */
    private Double shopTotal;
}
