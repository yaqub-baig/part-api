package com.autoadda.apis.part.response;

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
public class SupplierResponse {

	private String id;

	private String name;

	private String remarks;

	private String email;

	private String phoneNumber;

	private String website;

	private String timings;

	private String googleMapLink;

	private String locationId;
}
