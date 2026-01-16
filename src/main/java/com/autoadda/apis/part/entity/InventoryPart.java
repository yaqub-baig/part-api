package com.autoadda.apis.part.entity;

import com.autoadda.apis.part.util.ProductCondition;
import lombok.*;

import jakarta.persistence.*;

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

    private String sku;

    private String partName;
    @Enumerated(EnumType.STRING)
    private ProductCondition productCondition;

    private String partNumber;

    private String category;

    private String manufacturer;

    private Integer quantityInStock;

    private Double unitPrice;

    private String location; // shelf/bin location

    private Boolean isActive;
}
