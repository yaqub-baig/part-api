package com.autoadda.apis.part.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.autoadda.apis.part.entity.InventoryPart;
import com.autoadda.apis.part.entity.InventoryTransaction;

public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Integer> {
	// Custom query methods can be added here if needed
	List<InventoryTransaction> findByPart(InventoryPart part);

	List<InventoryTransaction> findByPartId(Integer partId);
}