package com.autoadda.apis.part.repository;

import com.autoadda.apis.part.entity.InventoryPart;
import com.autoadda.apis.part.util.ProductCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InventoryPartRepository extends JpaRepository<InventoryPart, Integer> {

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

    // Advanced search query with multiple optional filters
    @Query("SELECT ip FROM InventoryPart ip WHERE " +
            "(:partNumber IS NULL OR ip.partNumber LIKE %:partNumber%) AND " +
            "(:partName IS NULL OR ip.partName LIKE %:partName%) AND " +
            "(:category IS NULL OR ip.category = :category) AND " +
            "(:manufacturer IS NULL OR ip.manufacturer = :manufacturer) AND " +
            "(:productId IS NULL OR ip.productId = :productId) AND " +
            "(:minPrice IS NULL OR ip.unitPrice >= :minPrice) AND " +
            "(:maxPrice IS NULL OR ip.unitPrice <= :maxPrice) AND " +
            "(:minQuantity IS NULL OR ip.quantityInStock >= :minQuantity) AND " +
            "(:inStockOnly = false OR ip.quantityInStock > 0) AND " +
            "(:activeOnly = false OR ip.isActive = true)")
    List<InventoryPart> searchInventoryParts(
            @Param("partNumber") String partNumber,
            @Param("partName") String partName,
            @Param("category") String category,
            @Param("manufacturer") String manufacturer,
            @Param("productId") String productId,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("minQuantity") Integer minQuantity,
            @Param("inStockOnly") Boolean inStockOnly,
            @Param("activeOnly") Boolean activeOnly
    );
}