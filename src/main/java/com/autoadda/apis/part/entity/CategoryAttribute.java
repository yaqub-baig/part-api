package com.autoadda.apis.part.entity;

import lombok.*;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "category_attribute")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data

public class CategoryAttribute {

    @Id
    @Column(name = "id")
    private String id;

    //	@ManyToOne
    @Column(name = "category_id")
    private String categoryId;

    //    @ManyToOne
    @Column(name = "attribute_id")
    private String attributeId;
}
