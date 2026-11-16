package com.autoadda.apis.part.entity;

import com.autoadda.apis.part.util.EnquiryRecipientStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** A shop an enquiry was sent to. Fixed when the enquiry is created. */
@Entity
@Table(name = "enquiry_recipient",
        uniqueConstraints = @UniqueConstraint(name = "uk_enquiry_recipient", columnNames = {"enquiry_id", "supplier_id"}),
        indexes = @Index(name = "idx_enquiry_recipient_supplier", columnList = "supplier_id, status"))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnquiryRecipient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "enquiry_id", nullable = false)
    private Integer enquiryId;

    @Column(name = "supplier_id", nullable = false)
    private Integer supplierId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EnquiryRecipientStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
