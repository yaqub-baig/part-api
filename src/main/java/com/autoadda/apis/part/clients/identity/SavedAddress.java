package com.autoadda.apis.part.clients.identity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** identity-api GET /addresses/internal/{id}: one of a user's saved addresses. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavedAddress {
    private Long id;
    private Integer userId;
    private String label;
    private String contactName;
    private String phone;
    private String addressLine1;
    private String addressLine2;
    private String area;
    private String city;
    private String emirate;
    private String mapLink;
    private Double latitude;
    private Double longitude;
    private String notes;
}
