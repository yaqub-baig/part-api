package com.autoadda.apis.part.entity;

import com.autoadda.apis.part.util.ProductCondition;
import com.autoadda.apis.part.util.Status;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "supplier")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Supplier {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Integer id;
    @Column(name = "supplier_brand_name")
    private String supplierBrandName;
    @Column(name = "supplier_arabic_name")
    private String supplierArabicName;
    @Column(name = "supplier_name")
    private String name;
    @Column(name = "trade_licence_number", unique = true)
    private String tradeLicenceNumber;
    @Column(name = "trn_number")
    private String trnNumber;
    @Column(name = "website")
    private String website;
    @Column(name = "email_id")
    private String email;
    @Column(name = "phone_number")
    private String phoneNumber;
    @Column(name = " mobile_number")
    private String mobileNumber;
    @OneToOne
    @JoinColumn(name = "shop_logo")
    private MediaFile shopLogo;
    @Enumerated(EnumType.STRING)
    @Column(name = "account_status")
    private Status accountStatus;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    /** Display text of the makes covered ("Nissan, Toyota"), rewritten from makeIds. */
    @Column(name = "brands", length = 2000)
    private String brands;
    /** Display text of the models covered, rewritten from modelIds. */
    @Column(name = "models",length = 2000)
    private String models;

    // Shop coverage, used to route part enquiries. Empty models / categories / conditions mean "all".
    @ElementCollection
    @CollectionTable(name = "supplier_make", joinColumns = @JoinColumn(name = "supplier_id"),
            indexes = @Index(name = "idx_supplier_make_make_id", columnList = "make_id"))
    @Column(name = "make_id", nullable = false)
    @BatchSize(size = 50)
    @Builder.Default
    private Set<String> makeIds = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "supplier_model", joinColumns = @JoinColumn(name = "supplier_id"),
            indexes = @Index(name = "idx_supplier_model_model_id", columnList = "model_id"))
    @Column(name = "model_id", nullable = false)
    @BatchSize(size = 50)
    @Builder.Default
    private Set<String> modelIds = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "supplier_category", joinColumns = @JoinColumn(name = "supplier_id"),
            indexes = @Index(name = "idx_supplier_category_category_id", columnList = "category_id"))
    @Column(name = "category_id", nullable = false)
    @BatchSize(size = 50)
    @Builder.Default
    private Set<String> categoryIds = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "supplier_condition", joinColumns = @JoinColumn(name = "supplier_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "product_condition", nullable = false, length = 30)
    @BatchSize(size = 50)
    @Builder.Default
    private Set<ProductCondition> conditions = new HashSet<>();
    @Column(name = "shop_owner_id")
    private Integer sparePartShopOwner;
    @OneToOne
    private Location location;
    @Column(name = "working_days")
    private String workingDays;
    @Column(name = "opening_hours")
    private String openingHours;
    @Column(name = "closing_hours")
    private String closingHours;
    @Column(name = "is_active")
    private boolean isActive;
    @OneToMany(orphanRemoval = true)
    @JoinColumn(name = "shop_trade_license")
    @Builder.Default
    private List<MediaFile> tradeLicenseAttachment = new ArrayList<> ( );
    @OneToMany(orphanRemoval = true)
    @JoinColumn(name = "shop_vat_certificate")
    @Builder.Default
    private List<MediaFile> vatCertificate = new ArrayList<> ( );
    @OneToMany(orphanRemoval = true)
    @JoinColumn
    @Builder.Default
    private List<MediaFile> shopImages = new ArrayList<> ( );


    @PrePersist
    public void prePersist() {
        isActive = true;
        createdAt = LocalDateTime.now ( );
    }
}
