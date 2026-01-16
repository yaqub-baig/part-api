package com.autoadda.apis.part.entity;

import lombok.*;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "product_attribute")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data

public class ProductAttribute {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "attribute_value")
    private String attributeValue;

    @Column(name = "product_id")
    private String productId;

    @Column(name = "attribute_id")
    private String attributeId;
}
