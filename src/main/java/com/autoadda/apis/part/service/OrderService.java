package com.autoadda.apis.part.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.autoadda.apis.part.entity.Order;
import com.autoadda.apis.part.entity.OrderItem;
import com.autoadda.apis.part.entity.ShoppingCart;
import com.autoadda.apis.part.repository.OrderRepository;
import com.autoadda.apis.part.repository.ShoppingCartRepository;
import com.autoadda.apis.part.util.Status;



@Service
public class OrderService {
	@Autowired
	private ShoppingCartRepository cartRepo;
	@Autowired
	private OrderRepository orderRepo;

	public Order placeOrder(Integer vehicleOwnerId) {
		ShoppingCart cart = cartRepo.findByVehicleOwnerId(vehicleOwnerId).orElseThrow();

		Order order = new Order();
		order.setVehicleOwnerId(vehicleOwnerId);
		order.setOrderDate(LocalDateTime.now());
		order.setStatus(Status.CREATED);

		List<OrderItem> items = cart.getItems().stream().map(ci -> {
			OrderItem oi = new OrderItem();
			oi.setPart(ci.getPart());
			oi.setQuantity(ci.getQuantity());
			oi.setOrder(order);
			return oi;
		}).collect(Collectors.toList());

		order.setItems(items);
		cartRepo.delete(cart);

		return orderRepo.save(order);
	}
}
