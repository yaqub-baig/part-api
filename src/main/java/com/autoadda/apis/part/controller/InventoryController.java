package com.autoadda.apis.part.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.autoadda.apis.part.entity.InventoryPart;
import com.autoadda.apis.part.entity.InventoryTransaction;
import com.autoadda.apis.part.service.InventoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

	private final InventoryService inventoryService;

	// Create a new inventory part
	@PostMapping("/parts")
	public ResponseEntity<InventoryPart> createPart(@RequestBody InventoryPart part) {
		return ResponseEntity.ok(inventoryService.addPart(part));
	}

	// Get all parts
	@GetMapping("/parts")
	public ResponseEntity<List<InventoryPart>> getAllParts() {
		return ResponseEntity.ok(inventoryService.getAllParts());
	}

	// Get part by ID
	@GetMapping("/parts/{id}")
	public ResponseEntity<InventoryPart> getPartById(@PathVariable Integer id) {
		return inventoryService.getPartById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
	}

	// Add stock to a part
	@PostMapping("/parts/{id}/add-stock")
	public ResponseEntity<String> addStock(@PathVariable Integer id, @RequestParam int quantity,
			@RequestParam(required = false, defaultValue = "Stock added") String note) {
		inventoryService.addStock(id, quantity, note);
		return ResponseEntity.ok("Stock added successfully.");
	}

	// Remove stock from a part
	@PostMapping("/parts/{id}/remove-stock")
	public ResponseEntity<String> removeStock(@PathVariable Integer id, @RequestParam int quantity,
			@RequestParam(required = false, defaultValue = "Stock used") String note) {
		inventoryService.removeStock(id, quantity, note);
		return ResponseEntity.ok("Stock removed successfully.");
	}

	// Get all transactions for a part
	@GetMapping("/parts/{id}/transactions")
	public ResponseEntity<List<InventoryTransaction>> getTransactions(@PathVariable Integer id) {
		return ResponseEntity.ok(inventoryService.getTransactionsForPart(id));
	}
}
