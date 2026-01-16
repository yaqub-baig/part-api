package com.autoadda.apis.part.entity;

import lombok.*;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShoppingCartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    private InventoryPart part;

    private Integer quantity;

    @ManyToOne
    private ShoppingCart cart;
}
