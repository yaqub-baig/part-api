package com.autoadda.apis.part.repository;

import com.autoadda.apis.part.entity.Model;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ModelRepository extends JpaRepository<Model, String> {

    Model findByName(String name);

    List<Model> findByMakeId(String makeId);

    List<Model> findByVehicleTypeId(String vehicleTypeId);

    /** Models of one make with any of these names (name match follows the DB collation, case-insensitive by default). */
    List<Model> findByMakeIdAndNameIn(String makeId, Collection<String> names);

    /** Every model with this name, across makes (model names are not unique). */
    List<Model> findAllByName(String name);
}
