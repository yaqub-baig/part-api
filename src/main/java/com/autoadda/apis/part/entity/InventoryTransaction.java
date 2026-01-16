package com.autoadda.apis.part.entity;

import com.autoadda.apis.part.util.TransactionType;
import lombok.*;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    private InventoryPart part;

    private int quantity;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    private String referenceNote; // e.g., "Purchase Order #123", "Used in JobCard 456"

    private LocalDateTime timestamp;

}
