package com.autoadda.apis.part.entity;

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
    @PrePersist
    public void prePersist() {
        isChecked = false;
        createdAt = LocalDateTime.now ( );
    }

}
