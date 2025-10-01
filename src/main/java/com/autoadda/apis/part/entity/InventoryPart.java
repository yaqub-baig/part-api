package com.autoadda.apis.part.entity;

import lombok.*;

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

    private String partName;

    private String partNumber;

    private String category;

    private String manufacturer;

    private Integer quantityInStock;

    private Double unitPrice;

    private Integer quantity;

    private String location; // shelf/bin location

    private Boolean isActive;
}
