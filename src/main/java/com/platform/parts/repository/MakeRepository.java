package com.platform.parts.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.platform.parts.entity.Make;

@Repository
public interface MakeRepository extends JpaRepository<Make, String> {

	Make findByName(String name);
}
