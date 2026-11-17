package com.autoadda.apis.part.request;

import com.autoadda.apis.part.util.ProductCondition;

import com.autoadda.apis.part.entity.Location;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for creating or updating a Supplier
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierRequest {

    private String name;
    private String supplierBrandName;
    private String supplierArabicName;
    private String trnNumber;
    private String tradeLicenceNumber;
    private String email;
    private String phoneNumber;
    private String mobileNumber;
    private Location location;
    /** Legacy: make names, comma separated. Ignored when makeIds is sent. */
    private String brands;
    /** Legacy: model names (of the chosen makes), comma separated. Ignored when modelIds is sent. */
    private String models;
    /** Makes the shop covers. On update, null keeps the current ones. */
    private List<String> makeIds;
    /** Models covered (each must belong to one of the makes); empty = all models of the makes. */
    private List<String> modelIds;
    /** Part categories covered (a parent covers its subcategories); empty = all. */
    private List<String> categoryIds;
    /** Part conditions the shop sells; empty = all. */
    private List<ProductCondition> conditions;
    /** First model year covered (inclusive); null = no lower limit. On update, sending either year replaces both. */
    private Integer yearFrom;
    /** Last model year covered (inclusive); null = no upper limit. */
    private Integer yearTo;
    /** true clears the year range (shop covers all years); yearFrom / yearTo are then ignored. */
    private Boolean allYears;
    private String website;
    private String openingHours;
    private String closingHours;
    private String workingDays;
    private Long shopLogoId;
    private List<Long> shopImageIds;
    private List<Long> tradeLicenseIds;
    private List<Long> vatCertificateIds;

}
