package com.autoadda.apis.part.response;

import com.autoadda.apis.part.entity.MediaFile;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Data
public class MakeResponse {
    private String id;
    private String name;
    private MediaFile logo;
}
