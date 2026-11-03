package com.autoadda.apis.part.entity;

import lombok.*;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private InventoryPart part;

    private int quantity;

    /** Price per unit at the moment the order was placed (inventory price may change later). */
    private Double unitPrice;

    @ManyToOne
    private Order order;
}
