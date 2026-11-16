package com.autoadda.apis.part.response;

import com.autoadda.apis.part.util.ProductCondition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Result of accepting a quote: add inventoryPartId to the cart (or buy now) to order it. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcceptQuoteResponse {
    private Integer enquiryId;
    private Integer quoteId;
    private Integer inventoryPartId;
    private Integer supplierId;
    private String productId;
    private Double unitPrice;
    private Integer quantity;
    private ProductCondition condition;
}
