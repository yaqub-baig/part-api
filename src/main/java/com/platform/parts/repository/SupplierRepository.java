package com.platform.parts.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.platform.parts.entity.Supplier;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, String> {

	Supplier findByName(String name);

	List<Supplier> findByNameContaining(String name);
}
