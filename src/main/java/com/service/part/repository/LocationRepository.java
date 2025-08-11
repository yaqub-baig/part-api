package com.service.part.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.service.part.entity.Location;

@Repository
public interface LocationRepository extends JpaRepository<Location, Integer> {

}
