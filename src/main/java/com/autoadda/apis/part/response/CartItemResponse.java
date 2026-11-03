package com.autoadda.apis.part.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemResponse {
    private Integer cartItemId;
    private Integer partId;
    private String productId;
    private String supplierId;
    private String sku;
    private String partName;
    private String partNumber;
    private Double unitPrice;
    private Integer quantity;
    private Double lineTotal;
    private Integer availableStock;
    /** False when the part went inactive or stock dropped below the cart quantity. */
    private boolean available;
}
