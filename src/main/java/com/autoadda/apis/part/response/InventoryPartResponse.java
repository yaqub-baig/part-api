package com.autoadda.apis.part.response;

import com.autoadda.apis.part.entity.MediaFile;
import com.autoadda.apis.part.util.ProductCondition;
import lombok.*;

import java.util.List;

/**
 * Complete response for single inventory part retrieval
 * Contains full inventory, product, and supplier details including images
 * Use this for GET by ID operations where complete information is needed
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryPartResponse {

    // Inventory Part fields
    private Integer inventoryId;
    private String productId;
    private ProductCondition productCondition;
    private String supplierId;
    private String sku;
    private String partName;
    private String partNumber;
    private String category;
    private String manufacturer;
    private Integer quantityInStock;
    private Double unitPrice;
    private String location;
    private Boolean isActive;

    // Complete Product details (additional to what's in SearchResponse)
    private String productName;
    private String productDescription;
    private String productTag;
    private String productOtherNames;
    private Integer productYear;
    private String productMake;
    private String productModel;
    private List<MediaFile> productImages;

    // Supplier details (same as SearchResponse for consistency)
    private String supplierName;
    private String supplierEmail;
    private String supplierPhone;
    private String supplierWebsite;
    private String supplierLocation;
}
