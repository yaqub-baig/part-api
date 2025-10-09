package com.autoadda.apis.part.response;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Data
public class ManufacturerResponse {
    private String id;
    private String name;
}
