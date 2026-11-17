package com.autoadda.apis.part.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** The buying garage as it was when the order / enquiry was placed (name and delivery address). */
@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GarageSnapshot {
    @Column(name = "garage_name")
    private String garageName;
    @Column(name = "garage_phone")
    private String garagePhone;
    @Column(name = "garage_address", length = 1000)
    private String garageAddress;
    @Column(name = "garage_map_link", length = 1000)
    private String garageMapLink;
}
