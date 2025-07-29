package com.autoadda.apis.parts.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.autoadda.apis.parts.entity.VehicleType;

@Repository
public interface VehicleTypeRepository extends JpaRepository<VehicleType, String> {

	VehicleType findByName(String name);
}
