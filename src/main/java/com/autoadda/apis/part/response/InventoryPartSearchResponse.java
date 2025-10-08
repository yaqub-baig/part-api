package com.autoadda.apis.part.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryPartSearchResponse {

    private Integer inventoryId;
    private String productId;
    private String partName;
    private String partNumber;
    private String category;
    private String manufacturer;
    private Integer quantityInStock;
    private Double unitPrice;
    private String location;
    private Boolean isActive;

    // Supplier information
    private String supplierId;
    private String supplierName;
    private String supplierEmail;
    private String supplierPhone;
    private String supplierWebsite;
    private String supplierLocation;
}