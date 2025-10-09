package com.autoadda.apis.part.repository;

import com.autoadda.apis.part.entity.Manufacturer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ManufacturerRepository extends JpaRepository<Manufacturer, String> {

    Manufacturer findByName(String name);
}
