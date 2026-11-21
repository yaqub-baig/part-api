package com.autoadda.apis.part.request;

import com.autoadda.apis.part.entity.DeliveryAddress;
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
    /** Required: where the shops deliver. */
    private DeliveryAddress deliveryAddress;
}
