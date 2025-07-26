package com.autoadda.apis.parts.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.autoadda.apis.parts.entity.Attribute;

@Repository
public interface AttributeRepository extends JpaRepository<Attribute, String> {

	Optional<Attribute> findByName(String name);
	
	boolean existsByName(String name);
}
