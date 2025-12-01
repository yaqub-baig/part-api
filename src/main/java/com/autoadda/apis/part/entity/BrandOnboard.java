package com.autoadda.apis.part.entity;

import com.autoadda.apis.part.util.BrandRequestStatus;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class BrandOnboard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String brandName;
    private String modelName;
    private String engineType;
    private String vehicleType;
    private Integer requestBy;
    private String requestReferenceId;
    private Boolean isChecked;
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_status")
    private BrandRequestStatus requestStatus;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "approved_by")
    private Integer approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejected_by")
    private Integer rejectedBy;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @PrePersist
    public void prePersist() {
        isChecked = false;
        requestStatus = BrandRequestStatus.PENDING;
        createdAt = LocalDateTime.now ( );
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now ( );
    }

}
