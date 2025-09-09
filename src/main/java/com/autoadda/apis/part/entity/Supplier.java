package com.autoadda.apis.part.entity;

import com.autoadda.apis.part.util.Status;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    @Column(name = "trade_licence_number",unique = true)
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
    @Column(name = "account_status")
    private Status accountStatus;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "brands")
    private String brands;
    @Column(name = "models")
    private String models;
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
    private List<MediaFile> tradeLicenseAttachment = new ArrayList<> ( );
    @OneToMany(orphanRemoval = true)
    @JoinColumn(name = "shop_vat_certificate")
    private List<MediaFile> vatCertificate = new ArrayList<> ( );
    @OneToMany(orphanRemoval = true)
    @JoinColumn
    private List<MediaFile> shopImages = new ArrayList<> ( );


    @PrePersist
    public void prePersist() {
        isActive = true;
        createdAt = LocalDateTime.now ( );
    }
}
