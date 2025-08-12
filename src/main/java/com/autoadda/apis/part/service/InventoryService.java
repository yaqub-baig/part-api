package com.autoadda.apis.part.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.autoadda.apis.part.entity.InventoryPart;
import com.autoadda.apis.part.entity.InventoryTransaction;
import com.autoadda.apis.part.repository.InventoryPartRepository;
import com.autoadda.apis.part.repository.InventoryTransactionRepository;
import com.autoadda.apis.part.util.TransactionType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventoryService {

	private final InventoryPartRepository partRepo;
	private final InventoryTransactionRepository txnRepo;

	public InventoryPart addPart(InventoryPart part) {
		part.setQuantityInStock(part.getQuantityInStock() == 0 ? 0 : part.getQuantityInStock());
		return partRepo.save(part);
	}

	public List<InventoryPart> getAllParts() {
		return partRepo.findAll();
	}

	public Optional<InventoryPart> getPartById(Integer id) {
		return partRepo.findById(id);
	}

	@Transactional
	public void addStock(Integer partId, int quantity, String note) {
		InventoryPart part = partRepo.findById(partId).orElseThrow(() -> new RuntimeException("Part not found"));

		part.setQuantityInStock(part.getQuantityInStock() + quantity);
		partRepo.save(part);

		InventoryTransaction txn = InventoryTransaction.builder().part(part).quantity(quantity)
				.transactionType(TransactionType.ADD).referenceNote(note).timestamp(LocalDateTime.now()).build();

		txnRepo.save(txn);
	}

	@Transactional
	public void removeStock(Integer partId, int quantity, String note) {
		InventoryPart part = partRepo.findById(partId).orElseThrow(() -> new RuntimeException("Part not found"));

		if (part.getQuantityInStock() < quantity) {
			throw new IllegalArgumentException("Not enough stock to remove");
		}

		part.setQuantityInStock(part.getQuantityInStock() - quantity);
		partRepo.save(part);

		InventoryTransaction txn = InventoryTransaction.builder().part(part).quantity(quantity)
				.transactionType(TransactionType.REMOVE).referenceNote(note).timestamp(LocalDateTime.now()).build();

		txnRepo.save(txn);
	}

	public List<InventoryTransaction> getTransactionsForPart(Integer partId) {
		InventoryPart part = partRepo.findById(partId).orElseThrow(() -> new RuntimeException("Part not found"));
		return txnRepo.findByPart(part);
	}
}
