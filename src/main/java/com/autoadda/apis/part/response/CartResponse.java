package com.autoadda.apis.part.response;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartResponse {
    private Integer cartId;
    private Integer vehicleOwnerId;
    @Builder.Default
    private List<CartItemResponse> items = new ArrayList<> ( );
    private int totalItems;
    private Double totalAmount;
}
