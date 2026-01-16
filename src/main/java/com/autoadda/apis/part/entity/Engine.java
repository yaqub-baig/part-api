package com.autoadda.apis.part.entity;

import lombok.*;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "engine")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Engine {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "engine_capacity")
    private String engineCapacity;

    @Column(name = "fuel_type")
    private String fuelType;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "model_id")
    private String modelId;

    @Column(name = "transmission_type")
    private String transmissionType;

}
