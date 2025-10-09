package com.autoadda.apis.part.entity;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "make")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Make {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "name")
    private String name;

}
