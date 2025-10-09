package com.autoadda.apis.part.repository;

import com.autoadda.apis.part.entity.Model;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModelRepository extends JpaRepository<Model, String> {

    Model findByName(String name);

    List<Model> findByMakeId(String makeId);

    List<Model> findByVehicleTypeId(String vehicleTypeId);
}
