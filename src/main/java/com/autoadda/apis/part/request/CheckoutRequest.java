package com.autoadda.apis.part.request;

import com.autoadda.apis.part.entity.DeliveryAddress;
import com.autoadda.apis.part.util.DeliveryMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Body of POST /orders/place/{id} and /orders/buy-now/{id}. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutRequest {
    /** DELIVERY (default) or PICKUP at the shops. */
    private DeliveryMethod deliveryMethod;
    /** DELIVERY: one of the customer's saved addresses (identity-api /addresses). Use this or deliveryAddress. */
    private Long addressId;
    /** DELIVERY: an address typed at checkout, when not using a saved one (addressId). */
    private DeliveryAddress deliveryAddress;
    /** Required for PICKUP: who collects (contactName, phone; notes optional). Other fields are ignored. */
    private DeliveryAddress pickupContact;
}
