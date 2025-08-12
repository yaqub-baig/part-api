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

public class LocationRequest {

	private String id;

	private String streetAddress1;

	private String streetAddress2;

	private String city;

	private String county;

	private String postalCode;

	private String state;

	private String country;

	private String type;
}
