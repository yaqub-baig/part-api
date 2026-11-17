package com.autoadda.apis.part.clients.garage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** garage-api GET /garages/{id}/purchasing-access: the garage as a parts buyer. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GarageBuyerInfo {
    private Integer garageId;
    private String name;
    private String phone;
    private String email;
    private String trnNumber;
    private String address;
    private String city;
    private String mapLink;
}
