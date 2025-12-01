package com.autoadda.apis.part.request;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Data

public class EngineRequest {

    private String id;
    private String engineCapacity;
    private String fuelType;
    private String fullName;
    private String modelId;
    private String transmissionType;
}
