package com.autoadda.apis.part.entity;

import com.autoadda.apis.part.util.ProductCondition;
import com.autoadda.apis.part.util.QuoteStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** A shop's offer for an enquiry. One per shop per enquiry; quoting again replaces it. */
@Entity
@Table(name = "part_quote",
        uniqueConstraints = @UniqueConstraint(name = "uk_part_quote_enquiry_supplier", columnNames = {"enquiry_id", "supplier_id"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartQuote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "enquiry_id", nullable = false)
    private Integer enquiryId;
    @Column(name = "supplier_id", nullable = false)
    private Integer supplierId;
    /** Catalogue product offered; becomes the listing's product on accept. */
    @Column(name = "product_id", nullable = false)
    private String productId;

    @Column(name = "unit_price", nullable = false)
    private Double unitPrice;
    @Enumerated(EnumType.STRING)
    @Column(name = "product_condition", nullable = false, length = 30)
    private ProductCondition condition;
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    @Column(name = "notes", length = 1000)
    private String notes;
    @Column(name = "valid_until", nullable = false)
    private LocalDateTime validUntil;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private QuoteStatus status;
    /** Listing created when the customer accepted this quote. */
    @Column(name = "inventory_part_id")
    private Integer inventoryPartId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
