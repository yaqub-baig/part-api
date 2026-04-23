package com.autoadda.apis.part.response;

import com.autoadda.apis.part.entity.MediaFile;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Data
public class ModelResponse {
    private String id;
    private String name;
    private String fullName;
    private String vehicleTypeId;
    private String vehicleType;
    private String makeId;
    private String makerName;
    @Builder.Default
    private List<MediaFile> images = new ArrayList<> ( );
}
