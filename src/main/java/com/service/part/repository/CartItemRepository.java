package com.service.part.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.service.part.entity.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
	List<CartItem> findByCartId(Integer cartId);
}