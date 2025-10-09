package com.autoadda.apis.part.entity;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

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

    @Column(name = "engine")
    private String engine;

    @Column(name = "fuel_type")
    private String fuelType;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "model_id")
    private String modelId;

    @Column(name = "transmission_type")
    private String transmissionType;

}
