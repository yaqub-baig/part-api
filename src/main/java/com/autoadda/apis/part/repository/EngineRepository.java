package com.autoadda.apis.part.repository;

import com.autoadda.apis.part.entity.Engine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EngineRepository extends JpaRepository<Engine, String> {

    Engine findByFullName(String fullName);

    Engine findByFullNameAndTransmissionTypeAndFuelType(String fullName, String transmissionType, String fuelType);

    List<Engine> findByModelId(String modelId);

    List<Engine> findByFuelType(String fuelType);

    List<Engine> findByTransmissionType(String transmissionType);

}
