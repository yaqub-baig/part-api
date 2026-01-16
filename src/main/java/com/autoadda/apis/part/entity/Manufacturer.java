package com.autoadda.apis.part.entity;

import lombok.*;

import jakarta.persistence.*;

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

    @ManyToOne
    private MediaFile logo;

}
