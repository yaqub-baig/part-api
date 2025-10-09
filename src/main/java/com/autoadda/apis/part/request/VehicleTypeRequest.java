package com.autoadda.apis.part.request;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Data

public class VehicleTypeRequest {

    private String id;
    private String name;
}
