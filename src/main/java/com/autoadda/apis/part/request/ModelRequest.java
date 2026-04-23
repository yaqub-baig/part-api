package com.autoadda.apis.part.request;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

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
    @Builder.Default
    private List<Long> imageIds = new ArrayList<> ( );
}
