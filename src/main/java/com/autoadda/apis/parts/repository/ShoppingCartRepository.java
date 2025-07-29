package com.autoadda.apis.parts.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.autoadda.apis.parts.entity.ShoppingCart;

public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Integer> {
//	Optional<ShoppingCart> findByVehichleOwnerId(Integer ownerId);

	Optional<ShoppingCart> findByVehicleOwnerId(Integer vehicleOwnerId);

	void deleteByVehicleOwnerId(Integer vehicleOwnerId);
}