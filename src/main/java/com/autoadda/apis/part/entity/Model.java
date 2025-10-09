package com.autoadda.apis.part.entity;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "model")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Model {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "name")
    private String name;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "make_id")
    private String makeId;

    @Column(name = "vehicle_type_id")
    private String vehicleTypeId;

}
