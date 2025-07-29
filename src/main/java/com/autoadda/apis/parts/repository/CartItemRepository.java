package com.autoadda.apis.parts.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.autoadda.apis.parts.entity.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
	List<CartItem> findByCartId(Integer cartId);
}