package com.autoadda.apis.part.repository;

import com.autoadda.apis.part.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findByVehicleOwnerId(Integer vehicleOwnerId);
}