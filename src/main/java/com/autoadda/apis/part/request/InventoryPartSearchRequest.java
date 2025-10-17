package com.autoadda.apis.part.request;

import com.autoadda.apis.part.util.ProductCondition;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryPartSearchRequest {

    // Basic search filters
    private String partNumber;
    private String partName;
    private String category;
    private String manufacturer;
    private String productId;

    // Location and condition filters
    private String location; // Search by warehouse/shelf location
    private ProductCondition productCondition; // Filter by NEW, USED, REFURBISHED
    private String supplierId; // Filter by specific supplier

    // Price and quantity filters
    private Double minPrice;
    private Double maxPrice;
    private Integer minQuantity;

    // Boolean filters
    private Boolean inStockOnly; // Only show items with quantityInStock > 0
    private Boolean activeOnly; // Only show active inventory items

    // Sorting options
    private String sortBy; // "price", "quantity", "supplierName"
    private String sortDirection; // "ASC" or "DESC"
}