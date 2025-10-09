package com.autoadda.apis.part.entity;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

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
