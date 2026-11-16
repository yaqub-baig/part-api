package com.autoadda.apis.part.response;

import com.autoadda.apis.part.util.ProductCondition;
import com.autoadda.apis.part.util.QuoteStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartQuoteResponse {
    private Integer id;
    private Integer supplierId;
    private String supplierName;
    private String productId;
    private String productName;
    private Double unitPrice;
    private ProductCondition condition;
    private Integer quantity;
    private String notes;
    private LocalDateTime validUntil;
    private QuoteStatus status;
    /** Listing to add to the cart; set once accepted. */
    private Integer inventoryPartId;
    private LocalDateTime createdAt;
}
