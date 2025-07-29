package com.autoadda.apis.parts.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.autoadda.apis.parts.entity.Engine;

@Repository
public interface EngineRepository extends JpaRepository<Engine, String> {

	Engine findByFullName(String fullName);

	List<Engine> findByModelId(String modelId);

	List<Engine> findByFuelType(String fuelType);

	List<Engine> findByTransmissionType(String TransmissionType);

}
