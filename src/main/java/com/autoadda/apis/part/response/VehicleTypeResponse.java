package com.autoadda.apis.part.response;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Data
public class VehicleTypeResponse {
    private String id;
    private String name;
}
