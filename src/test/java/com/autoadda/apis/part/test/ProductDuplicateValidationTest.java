package com.autoadda.apis.part.test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.autoadda.apis.part.entity.Product;
import com.autoadda.apis.part.exception.AlreadyExistException;
import com.autoadda.apis.part.repository.ProductRepository;
import com.autoadda.apis.part.validator.PartsDataValidator;

@ExtendWith(MockitoExtension.class)
class ProductDuplicateValidationTest {

	@Mock
	private ProductRepository productRepository;
	@InjectMocks
	private PartsDataValidator validator;

	private Product product(String id, String partNumber) {
		return Product.builder().id(id).name("Brake Pad").partNumber(partNumber).build();
	}

	@Test
	void newPartNumberIsAllowedEvenIfNameExists() {
		when(productRepository.findByPartNumber("BP-200")).thenReturn(null);
		assertDoesNotThrow(() -> validator.ensurePartNumberAvailable("BP-200", null));
	}

	@Test
	void existingPartNumberIsRejectedOnCreate() {
		when(productRepository.findByPartNumber("BP-100")).thenReturn(product("p1", "BP-100"));
		AlreadyExistException ex = assertThrows(AlreadyExistException.class,
				() -> validator.ensurePartNumberAvailable("BP-100", null));
		assertEquals("Product with part number 'BP-100' already exists", ex.getMessage());
	}

	@Test
	void partNumberIsTrimmedBeforeLookup() {
		when(productRepository.findByPartNumber("BP-100")).thenReturn(product("p1", "BP-100"));
		assertThrows(AlreadyExistException.class, () -> validator.ensurePartNumberAvailable("  BP-100 ", null));
	}

	@Test
	void updateKeepingOwnPartNumberIsAllowed() {
		when(productRepository.findByPartNumber("BP-100")).thenReturn(product("p1", "BP-100"));
		assertDoesNotThrow(() -> validator.ensurePartNumberAvailable("BP-100", "p1"));
	}

	@Test
	void updateToAnotherProductsPartNumberIsRejected() {
		when(productRepository.findByPartNumber("BP-100")).thenReturn(product("p1", "BP-100"));
		assertThrows(AlreadyExistException.class, () -> validator.ensurePartNumberAvailable("BP-100", "p2"));
	}

	@Test
	void blankPartNumberSkipsLookup() {
		assertDoesNotThrow(() -> validator.ensurePartNumberAvailable(null, null));
		assertDoesNotThrow(() -> validator.ensurePartNumberAvailable("  ", null));
		verify(productRepository, never()).findByPartNumber(any());
	}
}
