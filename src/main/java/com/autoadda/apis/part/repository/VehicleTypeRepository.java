package com.autoadda.apis.part.repository;

import com.autoadda.apis.part.entity.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VehicleTypeRepository extends JpaRepository<VehicleType, String> {

    VehicleType findByName(String name);
}
