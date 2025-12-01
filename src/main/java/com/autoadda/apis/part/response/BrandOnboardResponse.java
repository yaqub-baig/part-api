package com.autoadda.apis.part.response;

import com.autoadda.apis.part.util.BrandRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BrandOnboardResponse {
    private Integer id;
    private String brandName;
    private String modelName;
    private String engineType;
    private String vehicleType;
    private String requestReferenceId;
    private Boolean isChecked;
    private BrandRequestStatus requestStatus;
    private Integer requestBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer approvedBy;
    private LocalDateTime approvedAt;
    private Integer rejectedBy;
    private LocalDateTime rejectedAt;
    private String rejectionReason;

}
