package com.autoadda.apis.part.entity;

import lombok.*;

import jakarta.persistence.*;

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

    @ManyToOne
    @JoinColumn(name = "logo")
    private MediaFile logo;

}
