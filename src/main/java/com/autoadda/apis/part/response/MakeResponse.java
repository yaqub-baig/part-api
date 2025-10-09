package com.autoadda.apis.part.response;

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
}
