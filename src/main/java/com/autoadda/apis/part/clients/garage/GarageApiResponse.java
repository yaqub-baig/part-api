package com.autoadda.apis.part.clients.garage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** garage-api's GenericResponse envelope. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GarageApiResponse<T> {
    private String status;
    private String code;
    private String message;
    private T body;
}
