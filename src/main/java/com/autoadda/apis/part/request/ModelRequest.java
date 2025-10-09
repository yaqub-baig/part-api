package com.autoadda.apis.part.request;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Data

public class ModelRequest {

    private String id;
    private String name;
    private String fullName;
    private String vehicleTypeId;
    private String makeId;
}
