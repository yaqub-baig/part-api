package com.platform.parts.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.platform.parts.entity.Attribute;

@Repository
public interface AttributeRepository extends JpaRepository<Attribute, String> {

	Attribute findByName(String name);
}
