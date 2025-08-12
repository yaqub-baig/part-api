package com.autoadda.apis.part.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.autoadda.apis.part.entity.InventoryPart;
import com.autoadda.apis.part.entity.InventoryTransaction;
import com.autoadda.apis.part.repository.InventoryPartRepository;
import com.autoadda.apis.part.repository.InventoryTransactionRepository;
import com.autoadda.apis.part.service.InventoryService;

class InventoryServiceTest {

	@InjectMocks
	private InventoryService inventoryService;

	@Mock
	private InventoryPartRepository partRepository;

	@Mock
	private InventoryTransactionRepository transactionRepository;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void testAddPart() {
		InventoryPart part = new InventoryPart();
		part.setPartName("Brake Pad");

		when(partRepository.save(part)).thenReturn(part);

		InventoryPart result = inventoryService.addPart(part);

		assertThat(result.getPartName()).isEqualTo("Brake Pad");
		verify(partRepository, times(1)).save(part);
	}

	@Test
	void testGetAllParts() {
		List<InventoryPart> mockParts = List.of(new InventoryPart(), new InventoryPart());
		when(partRepository.findAll()).thenReturn(mockParts);

		List<InventoryPart> result = inventoryService.getAllParts();

		assertThat(result).hasSize(2);
	}

	@Test
	void testGetPartById_Found() {
		InventoryPart part = new InventoryPart();
		part.setId(1);
		when(partRepository.findById(1)).thenReturn(Optional.of(part));

		Optional<InventoryPart> result = inventoryService.getPartById(1);

		assertThat(result).isPresent();
	}

	@Test
	void testGetPartById_NotFound() {
		when(partRepository.findById(2)).thenReturn(Optional.empty());

		Optional<InventoryPart> result = inventoryService.getPartById(2);

		assertThat(result).isEmpty();
	}

	@Test
	void testAddStock() {
		InventoryPart part = new InventoryPart();
		part.setId(1);
		part.setQuantity(10);

		when(partRepository.findById(1)).thenReturn(Optional.of(part));
		when(partRepository.save(any())).thenReturn(part);
		when(transactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		inventoryService.addStock(1, 5, "Restock");

		assertThat(part.getQuantity()).isEqualTo(15);
		verify(transactionRepository, times(1)).save(any(InventoryTransaction.class));
	}

	@Test
	void testRemoveStock_Success() {
		InventoryPart part = new InventoryPart();
		part.setId(1);
		part.setQuantity(10);

		when(partRepository.findById(1)).thenReturn(Optional.of(part));
		when(partRepository.save(any())).thenReturn(part);
		when(transactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		inventoryService.removeStock(1, 4, "Used in job");

		assertThat(part.getQuantity()).isEqualTo(6);
		verify(transactionRepository).save(any(InventoryTransaction.class));
	}

	@Test
	void testRemoveStock_Failure_InsufficientQty() {
		InventoryPart part = new InventoryPart();
		part.setId(1);
		part.setQuantity(2);

		when(partRepository.findById(1)).thenReturn(Optional.of(part));

		Exception ex = assertThrows(IllegalArgumentException.class,
				() -> inventoryService.removeStock(1, 5, "Too much"));

		assertThat(ex.getMessage()).isEqualTo("Insufficient quantity for part ID: 1");
	}

	@Test
	void testGetTransactionsForPart() {
		List<InventoryTransaction> txList = List.of(new InventoryTransaction(), new InventoryTransaction());
		when(transactionRepository.findByPartId(1)).thenReturn(txList);

		List<InventoryTransaction> result = inventoryService.getTransactionsForPart(1);

		assertThat(result).hasSize(2);
	}
}
