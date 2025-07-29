package com.autoadda.apis.parts.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.autoadda.apis.parts.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Integer> {
	List<Order> findByVehicleOwnerId(Integer vehicleOwnerId);
}