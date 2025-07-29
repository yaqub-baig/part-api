package com.autoadda.apis.parts.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "supplier")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data

public class Supplier {

	@Id
	@Column(name = "id")
	private String id;

	@Column(name = "name")
	private String name;

	@Column(name = "remarks")
	private String remarks;

	@Column(name = "email")
	private String email;

	@Column(name = "phone_number")
	private String phoneNumber;

	@Column(name = "website")
	private String website;

	@Column(name = "timings")
	private String timings;

	@Column(name = "is_active")
	private Boolean isActive;

	@Column(name = "google_map_link")
	private String googleMapLink;

//	@OneToOne(cascade = CascadeType.ALL)
//	@JoinColumn(name = "location_id", referencedColumnName = "id")
	@Column(name = "location_id")
	private String locationId;

}
