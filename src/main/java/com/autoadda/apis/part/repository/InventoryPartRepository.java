package com.autoadda.apis.part.repository;

import com.autoadda.apis.part.entity.InventoryPart;
import com.autoadda.apis.part.util.ProductCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface InventoryPartRepository extends JpaRepository<InventoryPart, Integer>, JpaSpecificationExecutor<InventoryPart> {

    // Find by Product
    Optional<InventoryPart> findByProductId(String productId);

    // Find active parts by Product
    Optional<InventoryPart> findByProductIdAndIsActive(String productId, Boolean isActive);

    // Find by Supplier
    List<InventoryPart> findBySupplierId(String supplierId);

    // Find active parts by Supplier
    List<InventoryPart> findBySupplierIdAndIsActive(String supplierId, Boolean isActive);

    // Find by Category
    List<InventoryPart> findByCategory(String category);

    // Find by Manufacturer
    List<InventoryPart> findByManufacturer(String manufacturer);

    // Find by Product and Supplier (for multi-supplier scenarios)
    List<InventoryPart> findByProductIdAndSupplierIdAndProductCondition(String productId, String supplierId, ProductCondition productCondition);

    // Find by location (warehouse/shelf)
    List<InventoryPart> findByLocation(String location);

    // Find active inventory
    List<InventoryPart> findByIsActive(Boolean isActive);

    // Find low stock items (custom query would be better, but this helps)
    List<InventoryPart> findByQuantityInStockLessThan(Integer threshold);

    // Find low stock items that are active
    List<InventoryPart> findByQuantityInStockLessThanAndIsActive(Integer threshold, Boolean isActive);

    // Count suppliers for a specific product (active and in stock)
    Long countByProductIdAndIsActiveAndQuantityInStockGreaterThan(String productId, Boolean isActive, Integer minStock);

    // Get all active inventory parts for a product with stock available
    List<InventoryPart> findByProductIdAndIsActiveAndQuantityInStockGreaterThan(String productId, Boolean isActive, Integer minStock);

    // Get distinct product IDs with available inventory
    @Query("SELECT DISTINCT ip.productId FROM InventoryPart ip WHERE ip.isActive = true AND ip.quantityInStock > 0")
    List<String> findDistinctProductIdsWithAvailableInventory();
}