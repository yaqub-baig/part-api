package com.autoadda.apis.part.repository;

import com.autoadda.apis.part.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, String> {

    Product findByName(String name);

    List<Product> findByNameContaining(String name);

    List<Product> findByOtherNamesContaining(String name);

    List<Product> findByCategoryId(String categoryId);

    List<Product> findByTagContaining(String tag);

//	List<Product> findBySkuContaining(String sku);

    List<Product> findByYearAndMakeAndModel(Integer year, String make, String model);

    List<Product> findByManufacturerId(String manufacturerId);

    Product findByPartNumber(String partNumber);

    // Advanced search query with multiple optional filters
    @Query("SELECT p FROM Product p WHERE " +
            "(:name IS NULL OR p.name LIKE %:name%) AND " +
            "(:partNumber IS NULL OR p.partNumber = :partNumber) AND " +
            "(:otherNames IS NULL OR p.otherNames LIKE %:otherNames%) AND " +
            "(:tag IS NULL OR p.tag LIKE %:tag%) AND " +
            "(:categoryId IS NULL OR p.categoryId = :categoryId) AND " +
            "(:manufacturerId IS NULL OR p.manufacturerId = :manufacturerId) AND " +
            "(:year IS NULL OR p.year = :year) AND " +
            "(:make IS NULL OR p.make = :make) AND " +
            "(:model IS NULL OR p.model = :model) " +
            "ORDER BY " +
            "CASE WHEN :sortBy = 'name' AND :sortDirection = 'ASC' THEN p.name END ASC, " +
            "CASE WHEN :sortBy = 'name' AND :sortDirection = 'DESC' THEN p.name END DESC, " +
            "CASE WHEN :sortBy = 'partNumber' AND :sortDirection = 'ASC' THEN p.partNumber END ASC, " +
            "CASE WHEN :sortBy = 'partNumber' AND :sortDirection = 'DESC' THEN p.partNumber END DESC, " +
            "CASE WHEN :sortBy = 'year' AND :sortDirection = 'ASC' THEN p.year END ASC, " +
            "CASE WHEN :sortBy = 'year' AND :sortDirection = 'DESC' THEN p.year END DESC, " +
            "CASE WHEN :sortBy IS NULL THEN p.id END ASC")
    List<Product> advancedSearch(
            @Param("name") String name,
            @Param("partNumber") String partNumber,
            @Param("otherNames") String otherNames,
            @Param("tag") String tag,
            @Param("categoryId") String categoryId,
            @Param("manufacturerId") String manufacturerId,
            @Param("year") Integer year,
            @Param("make") String make,
            @Param("model") String model,
            @Param("sortBy") String sortBy,
            @Param("sortDirection") String sortDirection
    );
}
