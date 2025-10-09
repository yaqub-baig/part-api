package com.autoadda.apis.part.repository;

import com.autoadda.apis.part.entity.ShoppingCart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Integer> {

    Optional<ShoppingCart> findByVehicleOwnerId(Integer vehicleOwnerId);

    void deleteByVehicleOwnerId(Integer vehicleOwnerId);
}