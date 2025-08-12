package com.autoadda.apis.part.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.autoadda.apis.part.entity.InventoryPart;

public interface InventoryPartRepository extends JpaRepository<InventoryPart, Integer> {
		
	// Custom query methods can be added here if needed
	// For example, to find parts by category or manufacturer
	 List<InventoryPart> findByCategory(String category);
	 List<InventoryPart> findByManufacturer(String manufacturer);
	
	// You can also add methods for custom queries based on your requirements
}