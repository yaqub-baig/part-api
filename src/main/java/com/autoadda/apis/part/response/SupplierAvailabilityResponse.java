package com.autoadda.apis.part.response;

import com.autoadda.apis.part.util.ProductCondition;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Data
public class SupplierAvailabilityResponse {

    private String supplierId;

    private String supplierName;

    private String supplierBrandName;

    private String email;

    private String phoneNumber;

    private String website;

    private String locationId;

    // Inventory details
    private Integer quantityInStock;

    private ProductCondition productCondition;

    private Double unitPrice;

    private String sku;

    private Boolean isActive;
}
