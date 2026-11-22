package com.autoadda.apis.part.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** A shop the customer collects part of a pickup order from. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PickupPointResponse {
    private String supplierId;
    private String name;
    private String phone;
    private String address;
    private String mapLink;
    private String workingDays;
    private String openingHours;
    private String closingHours;
}
