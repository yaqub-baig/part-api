package com.autoadda.apis.parts.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.autoadda.apis.parts.entity.Cart;

public interface CartRepository extends JpaRepository<Cart, Integer> {
    List<Cart> findByVehicleOwnerId(Integer vehicleOwnerId);
    void deleteByVehicleOwnerId(Integer vehicleOwnerId);
}