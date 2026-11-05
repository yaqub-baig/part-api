package com.autoadda.apis.part.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.autoadda.apis.part.exception.ValidationException;
import com.autoadda.apis.part.repository.MediaFileRepository;
import com.autoadda.apis.part.repository.ProductAttributeRepository;
import com.autoadda.apis.part.repository.ProductRepository;
import com.autoadda.apis.part.response.PartNumberAvailabilityResponse;
import com.autoadda.apis.part.service.CategoryService;
import com.autoadda.apis.part.service.ProductAttributeService;
import com.autoadda.apis.part.service.ProductService;
import com.autoadda.apis.part.validator.PartsDataValidator;

@ExtendWith(MockitoExtension.class)
class PartNumberAvailabilityTest {

	@Mock
	private ProductRepository productRepository;
	@Mock
	private PartsDataValidator partsDataValidator;
	@Mock
	private ProductAttributeRepository productAttributeRepository;
	@Mock
	private ProductAttributeService productAttributeService;
	@Mock
	private MediaFileRepository mediaFileRepository;
	@Mock
	private CategoryService categoryService;
	@InjectMocks
	private ProductService service;

	private PartNumberAvailabilityResponse check(String pn, String exclude) {
		return (PartNumberAvailabilityResponse) service.checkPartNumberAvailability(pn, exclude).getBody();
	}

	@Test
	void freePartNumberIsAvailable() {
		when(partsDataValidator.findProductUsingPartNumber("BP-200", null)).thenReturn(Optional.empty());
		PartNumberAvailabilityResponse res = check(" BP-200 ", null);
		assertTrue(res.isAvailable());
		assertEquals("BP-200", res.getPartNumber());
		assertNull(res.getExistingProductId());
	}

	@Test
	void takenPartNumberReturnsExistingProduct() {
		when(partsDataValidator.findProductUsingPartNumber("BP-100", null)).thenReturn(Optional.of("p1"));
		PartNumberAvailabilityResponse res = check("BP-100", null);
		assertFalse(res.isAvailable());
		assertEquals("p1", res.getExistingProductId());
		assertEquals("Product with part number 'BP-100' already exists", res.getMessage());
	}

	@Test
	void editingPassesExcludedIdThrough() {
		when(partsDataValidator.findProductUsingPartNumber("BP-100", "p1")).thenReturn(Optional.empty());
		assertTrue(check("BP-100", "p1").isAvailable());
	}

	@Test
	void tooLongPartNumberIsNotAvailableWithoutDbLookup() {
		PartNumberAvailabilityResponse res = check("X".repeat(256), null);
		assertFalse(res.isAvailable());
		verify(partsDataValidator, never()).findProductUsingPartNumber(any(), any());
	}

	@Test
	void blankPartNumberIsBadRequest() {
		assertThrows(ValidationException.class, () -> check("  ", null));
	}
}
