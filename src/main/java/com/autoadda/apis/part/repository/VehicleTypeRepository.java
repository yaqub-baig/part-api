package com.autoadda.apis.part.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.autoadda.apis.part.entity.VehicleType;

@Repository
public interface VehicleTypeRepository extends JpaRepository<VehicleType, String> {

	VehicleType findByName(String name);
}
