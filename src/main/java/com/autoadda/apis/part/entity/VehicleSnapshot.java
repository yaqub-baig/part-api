package com.autoadda.apis.part.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** The car a part is for (garage orders), as names and ids at order time. */
@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleSnapshot {
    @Column(name = "vehicle_make_id")
    private String makeId;
    @Column(name = "vehicle_make_name")
    private String makeName;
    @Column(name = "vehicle_model_id")
    private String modelId;
    @Column(name = "vehicle_model_name")
    private String modelName;
    @Column(name = "vehicle_year")
    private Integer year;
}
