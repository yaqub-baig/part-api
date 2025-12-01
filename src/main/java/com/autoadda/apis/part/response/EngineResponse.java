package com.autoadda.apis.part.response;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Data

public class EngineResponse {

    private String id;
    private String engineCapacity;
    private String fuelType;
    private String fullName;
    private String modelId;
    private String transmissionType;
}
