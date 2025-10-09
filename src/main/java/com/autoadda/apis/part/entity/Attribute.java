package com.autoadda.apis.part.entity;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "attribute")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data

public class Attribute {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "name")
    private String name;
}
