package com.autoadda.apis.part.entity;

import lombok.*;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Product {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "part_number", unique = true)
    private String partNumber;

    @Column(name = "tag")
    private String tag;

    @Column(name = "other_names")
    private String otherNames;

    @Column(name = "year")
    private String year;

    @Column(name = "make")
    private String make;

    @Column(name = "model")
    private String model;

    @Column(name = "category_id")
    private String categoryId;

    @Column(name = "manufacturer_id")
    private String manufacturerId;

    @Column(name = "supplier_count")
    @Builder.Default
    private Integer supplierCount = 0;

    @OneToMany(orphanRemoval = true)
    @JoinColumn(name = "product_id")
    @Builder.Default
    private List<MediaFile> images = new ArrayList<> ( );
}
