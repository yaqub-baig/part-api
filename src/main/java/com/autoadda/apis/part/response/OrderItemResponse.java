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
}
