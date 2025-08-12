package com.autoadda.apis.part.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.autoadda.apis.part.entity.ShoppingCart;

public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Integer> {

	Optional<ShoppingCart> findByVehicleOwnerId(Integer vehicleOwnerId);

	void deleteByVehicleOwnerId(Integer vehicleOwnerId);
}