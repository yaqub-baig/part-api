package com.autoadda.apis.part.repository;

import com.autoadda.apis.part.entity.InventoryPart;
import com.autoadda.apis.part.util.ProductCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
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

    // Same as above, limited to the given conditions (product search condition filter)
    Long countByProductIdAndIsActiveAndQuantityInStockGreaterThanAndProductConditionIn(
            String productId, Boolean isActive, Integer minStock, Collection<ProductCondition> conditions);

    List<InventoryPart> findByProductIdAndIsActiveAndQuantityInStockGreaterThanAndProductConditionIn(
            String productId, Boolean isActive, Integer minStock, Collection<ProductCondition> conditions);

    // Live supplier counts for a page of products in one query: rows of [productId, count]
    @Query("SELECT ip.productId, COUNT(ip) FROM InventoryPart ip WHERE ip.productId IN :productIds "
            + "AND ip.isActive = true AND ip.quantityInStock > 0 GROUP BY ip.productId")
    List<Object[]> countAvailableListingsByProductIds(@Param("productIds") Collection<String> productIds);

    @Query("SELECT ip.productId, COUNT(ip) FROM InventoryPart ip WHERE ip.productId IN :productIds "
            + "AND ip.isActive = true AND ip.quantityInStock > 0 AND ip.productCondition IN :conditions "
            + "GROUP BY ip.productId")
    List<Object[]> countAvailableListingsByProductIdsAndConditions(@Param("productIds") Collection<String> productIds,
                                                                  @Param("conditions") Collection<ProductCondition> conditions);

    // Get distinct product IDs with available inventory
    @Query("SELECT DISTINCT ip.productId FROM InventoryPart ip WHERE ip.isActive = true AND ip.quantityInStock > 0")
    List<String> findDistinctProductIdsWithAvailableInventory();
}