package com.autoadda.apis.part.response;

import com.autoadda.apis.part.util.ProductCondition;
import lombok.*;

/**
 * Lightweight response for search and listing operations
 * Contains essential inventory and supplier information without full product details
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryPartSearchResponse {

    // Core inventory fields
    private Integer inventoryId;
    private String productId;
    private ProductCondition productCondition;
    private String partName;
    private String partNumber;
    private String category;
    private String manufacturer;
    private Integer quantityInStock;
    private Double unitPrice;
    private String location;
    private Boolean isActive;


    // Supplier information (basic details)
    private String supplierId;
    private String supplierName;
    private String supplierEmail;
    private String supplierPhone;
    private String supplierWebsite;
    private String supplierLocation;
}