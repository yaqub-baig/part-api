package com.autoadda.apis.parts.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.autoadda.apis.parts.entity.Location;

@Repository
public interface LocationRepository extends JpaRepository<Location, Integer> {

}
