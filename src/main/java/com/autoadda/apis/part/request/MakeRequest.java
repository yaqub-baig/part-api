package com.autoadda.apis.part.request;

import com.autoadda.apis.part.entity.MediaFile;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Data

public class MakeRequest {

    private String id;
    private String name;
    private Long logoId;
}
