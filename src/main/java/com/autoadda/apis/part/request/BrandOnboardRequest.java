package com.autoadda.apis.part.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BrandOnboardRequest {

    private String brandName;
    private String modelName;
    private String engineType;
    private String vehicleType;

}
