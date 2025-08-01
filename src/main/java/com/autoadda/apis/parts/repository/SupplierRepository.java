package com.autoadda.apis.parts.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.autoadda.apis.parts.entity.Supplier;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, String> {

	Supplier findByName(String name);

	List<Supplier> findByNameContaining(String name);

	List<Supplier> findBySparePartShopOwner(Integer i);
}
