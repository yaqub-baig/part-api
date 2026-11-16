package com.autoadda.apis.part.request;

import com.autoadda.apis.part.util.ProductCondition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** A shop's quote. productId is the catalogue product offered (create it first if it isn't in the catalogue). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartQuoteRequest {
    private String productId;
    private Double unitPrice;
    private ProductCondition condition;
    private Integer quantity;
    private String notes;
}
