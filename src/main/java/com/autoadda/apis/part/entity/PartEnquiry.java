package com.autoadda.apis.part.entity;

import com.autoadda.apis.part.util.BuyerType;
import com.autoadda.apis.part.util.EnquiryStatus;
import com.autoadda.apis.part.util.ProductCondition;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/** A customer's request for a part, routed to the shops whose coverage matches. */
@Entity
@Table(name = "part_enquiry", indexes = {
        @Index(name = "idx_part_enquiry_owner", columnList = "vehicle_owner_id"),
        @Index(name = "idx_part_enquiry_garage", columnList = "garage_id"),
        @Index(name = "idx_part_enquiry_status_expires", columnList = "status, expires_at")})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartEnquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** CUSTOMER (a vehicle owner) or GARAGE (bought in the garage's name). Null on old rows = CUSTOMER. */
    @Enumerated(EnumType.STRING)
    @Column(name = "buyer_type", length = 20)
    private BuyerType buyerType;

    /** Set for customer enquiries. */
    @Column(name = "vehicle_owner_id")
    private Integer vehicleOwnerId;

    /** Set for garage enquiries. */
    @Column(name = "garage_id")
    private Integer garageId;
    /** Garage team member who sent it (audit only; shops see the garage). */
    @Column(name = "placed_by_user_id")
    private Integer placedByUserId;
    @Embedded
    private GarageSnapshot garage;
    /** The garage job this part is for, when sent from a part request. */
    @Column(name = "work_order_id")
    private Integer workOrderId;
    @Column(name = "job_card_id")
    private Integer jobCardId;
    @Column(name = "part_request_item_id")
    private Integer partRequestItemId;

    @Column(name = "make_id", nullable = false)
    private String makeId;
    @Column(name = "model_id")
    private String modelId;
    @Column(name = "vehicle_year")
    private Integer year;

    @Column(name = "category_id")
    private String categoryId;
    /** Set when the enquiry starts from a catalogue product no shop lists. */
    @Column(name = "product_id")
    private String productId;
    @Column(name = "part_number")
    private String partNumber;
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /** Conditions the customer accepts; empty = any. */
    @ElementCollection
    @CollectionTable(name = "part_enquiry_condition", joinColumns = @JoinColumn(name = "enquiry_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "product_condition", nullable = false, length = 30)
    @BatchSize(size = 50)
    @Builder.Default
    private Set<ProductCondition> conditions = new HashSet<>();

    /** Photos uploaded through /uploads (media_file ids). */
    @ElementCollection
    @CollectionTable(name = "part_enquiry_image", joinColumns = @JoinColumn(name = "enquiry_id"))
    @Column(name = "media_file_id", nullable = false)
    @BatchSize(size = 50)
    @Builder.Default
    private Set<Long> imageIds = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EnquiryStatus status;

    @Column(name = "recipient_count")
    private Integer recipientCount;
    /** True when the buyer chose the shops; false / null when they were matched by coverage. */
    @Column(name = "shops_chosen")
    private Boolean shopsChosen;
    @Column(name = "accepted_quote_id")
    private Integer acceptedQuoteId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    /** Shops can quote until this time. */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    @Column(name = "closed_at")
    private LocalDateTime closedAt;
}
