package com.autoadda.apis.parts.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "location")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data

public class Location {
	@Id
	@Column(name = "id")
	private String id;

	@Column(name = "street_address_1")
	private String streetAddress1;

	@Column(name = "street_address_2")
	private String streetAddress2;

	@Column(name = "city")
	private String city;

	@Column(name = "county")
	private String county;

	@Column(name = "postal_code")
	private String postalCode;

	@Column(name = "state")
	private String state;

	@Column(name = "country")
	private String country;

	@Column(name = "type")
	private String type;

}
