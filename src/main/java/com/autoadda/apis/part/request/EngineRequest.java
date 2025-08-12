package com.autoadda.apis.part.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Data

public class EngineRequest {

	private String id;
	private String engine;
	private String fuelType;
	private String fullName;
	private String modelId;
	private String transmissionType;
}
