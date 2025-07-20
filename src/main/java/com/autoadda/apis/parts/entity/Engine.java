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
@Table(name = "engine")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Engine {

	@Id
	@Column(name = "id")
	private String id;

	@Column(name = "engine")
	private String engine;

	@Column(name = "fuel_type")
	private String fuelType;
	
	@Column(name = "full_name")
	private String fullName;

	@Column(name = "model_id")
	private String modelId;
	
	@Column(name = "transmission_type")
	private String transmissionType;

}
