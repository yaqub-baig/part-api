package com.autoadda.apis.part.entity;

import lombok.*;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "vehicle_type")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class VehicleType {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "name")
    private String name;

}
