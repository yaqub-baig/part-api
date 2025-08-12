package com.autoadda.apis.part.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.autoadda.apis.part.entity.Location;

@Repository
public interface LocationRepository extends JpaRepository<Location, Integer> {

}
