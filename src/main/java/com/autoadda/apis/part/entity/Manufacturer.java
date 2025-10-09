package com.autoadda.apis.part.entity;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "manufacturer")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data

public class Manufacturer {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "name")
    private String name;

}
