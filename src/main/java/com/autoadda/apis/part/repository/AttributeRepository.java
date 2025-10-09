package com.autoadda.apis.part.repository;

import com.autoadda.apis.part.entity.Attribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AttributeRepository extends JpaRepository<Attribute, String> {

    Optional<Attribute> findByName(String name);

    boolean existsByName(String name);
}
