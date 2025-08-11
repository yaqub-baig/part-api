package com.service.part.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.service.part.entity.Make;

@Repository
public interface MakeRepository extends JpaRepository<Make, String> {

	Make findByName(String name);
}
