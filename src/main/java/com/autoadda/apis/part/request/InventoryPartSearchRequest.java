package com.autoadda.apis.part.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryPartSearchRequest {

    private String partNumber;
    private String partName;
    private String category;
    private String manufacturer;
    private String productId;
    private Double minPrice;
    private Double maxPrice;
    private Integer minQuantity;
    private Boolean inStockOnly; // Only show items with quantityInStock > 0
    private Boolean activeOnly; // Only show active inventory items
}