package com.platform.parts.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.platform.parts.entity.Model;

@Repository
public interface ModelRepository extends JpaRepository<Model, String> {

	Model findByName(String name);

	List<Model> findByMakeId(String makeId);

	List<Model> findByVehicleTypeId(String vehicleTypeId);
}
