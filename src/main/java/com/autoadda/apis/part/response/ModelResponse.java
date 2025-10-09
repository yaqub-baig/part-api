package com.autoadda.apis.part.response;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Data
public class ModelResponse {
    private String id;
    private String name;
    private String fullName;
    private String vehicleTypeId;
    private String vehicleType;
    private String makeId;
    private String makerName;
}
