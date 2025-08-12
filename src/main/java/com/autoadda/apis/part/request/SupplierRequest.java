package com.autoadda.apis.part.request;

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
    private String brands;
    private String models;
    private String website;
    private String openingHours;
    private String closingHours;
    private String workingDays;
    private Long shopLogoId;
    private List<Long> shopImageIds;
    private List<Long> tradeLicenseIds;
    private List<Long> vatCertificateIds;

}
