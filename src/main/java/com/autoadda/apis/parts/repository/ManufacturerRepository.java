package com.autoadda.apis.parts.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.autoadda.apis.parts.entity.Manufacturer;

@Repository
public interface ManufacturerRepository extends JpaRepository<Manufacturer, String> {

	Manufacturer findByName(String name);
}
