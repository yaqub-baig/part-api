package com.autoadda.apis.part.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.autoadda.apis.part.entity.InventoryPart;
import com.autoadda.apis.part.entity.ShoppingCart;
import com.autoadda.apis.part.entity.ShoppingCartItem;
import com.autoadda.apis.part.repository.InventoryPartRepository;
import com.autoadda.apis.part.repository.ShoppingCartRepository;

@Service
public class ShoppingCartService {
	@Autowired
	private ShoppingCartRepository cartRepo;
	@Autowired
	private InventoryPartRepository partRepo;

	public ShoppingCart addToCart(Integer vehicleOwnerId, Integer partId, int quantity) {
		ShoppingCart cart = cartRepo.findByVehicleOwnerId(vehicleOwnerId)
				.orElseGet(() -> new ShoppingCart(vehicleOwnerId, quantity, null));

		InventoryPart part = partRepo.findById(partId).orElseThrow();
		ShoppingCartItem item = new ShoppingCartItem();
		item.setPart(part);
		item.setQuantity(quantity);
		item.setCart(cart);
		cart.getItems().add(item);

		return cartRepo.save(cart);
	}

	public void clearCart(Integer vehicleOwnerId) {
		cartRepo.deleteByVehicleOwnerId(vehicleOwnerId);
	}

	public ShoppingCart getCart(Integer vehicleOwnerId) {
		return cartRepo.findByVehicleOwnerId(vehicleOwnerId).orElseThrow();
	}
}
