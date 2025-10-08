package com.autoadda.apis.part.entity;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryPart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "product_id")
    private String productId; // Link to Product entity

    @Column(name = "supplier_id")
    private String supplierId; // Link to Supplier entity

    private String partName;

    private String partNumber;

    private String category;

    private String manufacturer;

    private Integer quantityInStock;

    private Double unitPrice;

    private String location; // shelf/bin location

    private Boolean isActive;
}
