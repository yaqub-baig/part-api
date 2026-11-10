package com.autoadda.apis.part.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.autoadda.apis.part.entity.InventoryPart;
import com.autoadda.apis.part.entity.Supplier;
import com.autoadda.apis.part.repository.InventoryPartRepository;
import com.autoadda.apis.part.repository.MediaFileRepository;
import com.autoadda.apis.part.repository.SupplierRepository;
import com.autoadda.apis.part.response.SupplierAvailabilityResponse;
import com.autoadda.apis.part.service.LocationService;
import com.autoadda.apis.part.service.MediaFileCleanupService;
import com.autoadda.apis.part.service.SupplierService;
import com.autoadda.apis.part.util.ProductCondition;

@ExtendWith(MockitoExtension.class)
class SupplierAvailabilityTest {

	@Mock
	private SupplierRepository supplierRepository;
	@Mock
	private MediaFileRepository mediaFileRepository;
	@Mock
	private LocationService locationService;
	@Mock
	private InventoryPartRepository inventoryPartRepository;
	@Mock
	private MediaFileCleanupService mediaFileCleanupService;
	@InjectMocks
	private SupplierService service;

	@Test
	void eachListingCarriesItsInventoryPartIdForTheCart() {
		InventoryPart listing = InventoryPart.builder().id(77).productId("p1").supplierId("5")
				.quantityInStock(3).unitPrice(120.0).productCondition(ProductCondition.GENUINE).isActive(true).build();
		InventoryPart orphan = InventoryPart.builder().id(78).productId("p1").supplierId("999")
				.quantityInStock(1).unitPrice(90.0).isActive(true).build();
		when(inventoryPartRepository.findByProductIdAndIsActiveAndQuantityInStockGreaterThan("p1", true, 0))
				.thenReturn(List.of(listing, orphan));
		when(supplierRepository.findById(5)).thenReturn(Optional.of(Supplier.builder().id(5).name("Shop A").build()));
		when(supplierRepository.findById(999)).thenReturn(Optional.empty());

		List<SupplierAvailabilityResponse> result = service.getSuppliersByProductId("p1");

		assertEquals(77, result.get(0).getInventoryPartId());
		assertEquals("Shop A", result.get(0).getSupplierName());
		assertEquals(78, result.get(1).getInventoryPartId()); // supplier missing: still purchasable listing
	}
}
