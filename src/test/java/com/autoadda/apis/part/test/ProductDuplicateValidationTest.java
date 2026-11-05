package com.autoadda.apis.part.test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.autoadda.apis.part.exception.AlreadyExistException;
import com.autoadda.apis.part.repository.ProductRepository;
import com.autoadda.apis.part.validator.PartsDataValidator;

@ExtendWith(MockitoExtension.class)
class ProductDuplicateValidationTest {

	@Mock
	private ProductRepository productRepository;
	@InjectMocks
	private PartsDataValidator validator;

	@Test
	void newPartNumberIsAllowed() {
		when(productRepository.findIdsByPartNumber("BP-200")).thenReturn(List.of());
		assertDoesNotThrow(() -> validator.ensurePartNumberAvailable("BP-200", null));
	}

	@Test
	void existingPartNumberIsRejectedOnCreate() {
		when(productRepository.findIdsByPartNumber("BP-100")).thenReturn(List.of("p1"));
		AlreadyExistException ex = assertThrows(AlreadyExistException.class,
				() -> validator.ensurePartNumberAvailable("BP-100", null));
		assertEquals("Product with part number 'BP-100' already exists", ex.getMessage());
	}

	@Test
	void partNumberIsTrimmedBeforeLookup() {
		when(productRepository.findIdsByPartNumber("BP-100")).thenReturn(List.of("p1"));
		assertThrows(AlreadyExistException.class, () -> validator.ensurePartNumberAvailable("  BP-100 ", null));
	}

	@Test
	void updateKeepingOwnPartNumberIsAllowed() {
		when(productRepository.findIdsByPartNumber("BP-100")).thenReturn(List.of("p1"));
		assertDoesNotThrow(() -> validator.ensurePartNumberAvailable("BP-100", "p1"));
	}

	@Test
	void updateToAnotherProductsPartNumberIsRejected() {
		when(productRepository.findIdsByPartNumber("BP-100")).thenReturn(List.of("p1"));
		assertThrows(AlreadyExistException.class, () -> validator.ensurePartNumberAvailable("BP-100", "p2"));
	}

	@Test
	void blankPartNumberSkipsLookup() {
		assertDoesNotThrow(() -> validator.ensurePartNumberAvailable(null, null));
		assertDoesNotThrow(() -> validator.ensurePartNumberAvailable("  ", null));
		verify(productRepository, never()).findIdsByPartNumber(any());
	}
}
