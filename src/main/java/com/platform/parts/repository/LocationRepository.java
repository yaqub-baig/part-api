package com.platform.parts.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.platform.parts.entity.Location;

@Repository
public interface LocationRepository extends JpaRepository<Location, String> {

}
