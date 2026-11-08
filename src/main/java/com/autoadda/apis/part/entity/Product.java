package com.autoadda.apis.part.entity;

import lombok.*;
import org.hibernate.annotations.BatchSize;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "product", indexes = @Index(name = "idx_product_make_id", columnList = "make_id"))
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

    // TEXT (up to 64 KB): product descriptions regularly exceed the default VARCHAR(255).
    // ddl-auto=update does not widen existing columns, so existing DBs need:
    //   ALTER TABLE product MODIFY description TEXT;
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "part_number", unique = true)
    private String partNumber;

    @Column(name = "tag")
    private String tag;

    @Column(name = "other_names")
    private String otherNames;

    // make / model / year text columns: display copies of the vehicle fit below, kept for existing clients.
    // Search uses makeId / modelIds / years. Widen existing DBs manually (ddl-auto=update won't):
    //   ALTER TABLE product MODIFY model VARCHAR(2000), MODIFY year VARCHAR(1000);
    @Column(name = "year", length = 1000)
    private String year;

    @Column(name = "make")
    private String make;

    @Column(name = "model", length = 2000)
    private String model;

    /** Vehicle fit: the make (make.id) this part fits. */
    @Column(name = "make_id")
    private String makeId;

    /** Vehicle fit: models (model.id) of that make, one row each in product_model. */
    @ElementCollection
    @CollectionTable(name = "product_model", joinColumns = @JoinColumn(name = "product_id"),
            indexes = @Index(name = "idx_product_model_model_id", columnList = "model_id"))
    @Column(name = "model_id", nullable = false)
    @BatchSize(size = 50)
    @Builder.Default
    private Set<String> modelIds = new HashSet<>();

    /** Vehicle fit: model years, one row each in product_year. */
    @ElementCollection
    @CollectionTable(name = "product_year", joinColumns = @JoinColumn(name = "product_id"),
            indexes = @Index(name = "idx_product_year_year", columnList = "year"))
    @Column(name = "year", nullable = false)
    @BatchSize(size = 50)
    @Builder.Default
    private Set<Integer> years = new HashSet<>();

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
