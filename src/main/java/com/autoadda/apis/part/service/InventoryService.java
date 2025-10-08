package com.autoadda.apis.part.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.autoadda.apis.part.entity.InventoryPart;
import com.autoadda.apis.part.entity.InventoryTransaction;
import com.autoadda.apis.part.entity.Product;
import com.autoadda.apis.part.entity.Supplier;
import com.autoadda.apis.part.repository.InventoryPartRepository;
import com.autoadda.apis.part.repository.InventoryTransactionRepository;
import com.autoadda.apis.part.repository.ProductRepository;
import com.autoadda.apis.part.repository.SupplierRepository;
import com.autoadda.apis.part.request.InventoryPartExcelRow;
import com.autoadda.apis.part.request.InventoryPartRequest;
import com.autoadda.apis.part.request.InventoryPartSearchRequest;
import com.autoadda.apis.part.response.InventoryPartExcelUploadResponse;
import com.autoadda.apis.part.response.InventoryPartSearchResponse;
import com.autoadda.apis.part.util.TransactionType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventoryService {

	private final InventoryPartRepository partRepo;
	private final InventoryTransactionRepository txnRepo;
	private final ProductRepository productRepo;
	private final SupplierRepository supplierRepo;
	private final ExcelParserService excelParserService;

	private InventoryPart prepareInventoryPart(InventoryPartRequest request) {
		return InventoryPart.builder()
								.productId(request.getProductId())
								.supplierId(request.getSupplierId())
								.partName(request.getPartName())
								.partNumber(request.getPartNumber())
								.category(request.getCategory())
								.manufacturer(request.getManufacturer())
								.quantityInStock(request.getQuantityInStock() != null ? request.getQuantityInStock() : 0)
								.unitPrice(request.getUnitPrice())
								.location(request.getLocation())
								.isActive(request.getIsActive() != null ? request.getIsActive() : true)
							.build();
	}

	public InventoryPart addPart(InventoryPartRequest request) {
		InventoryPart part = prepareInventoryPart(request);
		return partRepo.save(part);
	}

	public List<InventoryPart> getAllParts() {
		return partRepo.findAll();
	}

	public Optional<InventoryPart> getPartById(Integer id) {
		return partRepo.findById(id);
	}

	public Optional<InventoryPart> getPartByProductId(String productId) {
		return partRepo.findByProductId(productId);
	}

	public List<InventoryPart> getPartsBySupplierId(String supplierId) {
		return partRepo.findBySupplierId(supplierId);
	}

	public List<InventoryPart> getLowStockParts(Integer threshold) {
		return partRepo.findByQuantityInStockLessThan(threshold);
	}

	@Transactional
	public void addStock(Integer partId, int quantity, String note) {
		InventoryPart part = partRepo.findById(partId).orElseThrow(() -> new RuntimeException("Part not found"));

		part.setQuantityInStock(part.getQuantityInStock() + quantity);
		partRepo.save(part);

		InventoryTransaction txn = InventoryTransaction
									.builder()
										.part(part)
										.quantity(quantity)
										.transactionType(TransactionType.ADD)
										.referenceNote(note)
										.timestamp(LocalDateTime.now())
									.build();
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

	/**
	 * Syncs Product stock with InventoryPart stock
	 * Useful for reconciliation or initial setup
	 */
	@Transactional
	public void syncProductStock(String productId) {
		Product product = productRepo.findById(productId)
				.orElseThrow(() -> new RuntimeException("Product not found"));

		Optional<InventoryPart> inventoryPart = partRepo.findByProductId(productId);

//		if (inventoryPart.isPresent()) {
//			product.setStock(inventoryPart.get().getQuantityInStock());
//			productRepo.save(product);
//		}
	}

	public List<InventoryTransaction> getTransactionsForPart(Integer partId) {
		InventoryPart part = partRepo.findById(partId).orElseThrow(() -> new RuntimeException("Part not found"));
		return txnRepo.findByPart(part);
	}

	/**
	 * Bulk import inventory parts from Excel file
	 * Expected columns: Part Number, SKU, Quantity, Price/PC, Condition
	 */
	@Transactional
	public InventoryPartExcelUploadResponse bulkImportFromExcel(MultipartFile file, String supplierId) {
		InventoryPartExcelUploadResponse response = new InventoryPartExcelUploadResponse(0, 0, 0);
		List<InventoryPartExcelRow> failedRows = new ArrayList<>();

		try {
			// Parse Excel file
			List<InventoryPartExcelRow> excelRows = excelParserService.parseExcelFile(file);
			response.setTotalRows(excelRows.size());

			int successCount = 0;
			int failedCount = 0;

			// Process each row
			for (InventoryPartExcelRow row : excelRows) {
				if ( Boolean.FALSE.equals (row.getIsValid())) {
					failedCount++;
					failedRows.add(row);
					continue;
				}

				try {
					// Find product by part number or SKU
					List<Product> products = productRepo.findByPartNumber(row.getPartNumber());

					if (products == null || products.isEmpty()) {
						// Try finding by SKU
						products = productRepo.findBySkuContaining(row.getSku());
					}

					if (products == null || products.isEmpty()) {
						row.setIsValid(false);
						row.setErrorMessage("Product not found with Part Number: " + row.getPartNumber() + " or SKU: " + row.getSku());
						failedCount++;
						failedRows.add(row);
						continue;
					}

					Product product = products.get(0); // Take first match

					// Check if inventory part already exists for this supplier and product
					Optional<InventoryPart> existingPart = partRepo.findByProductIdAndSupplierId(product.getId(), supplierId);

					if (existingPart.isPresent()) {
						// Update existing inventory
						InventoryPart part = existingPart.get();
						part.setQuantityInStock(part.getQuantityInStock() + row.getQuantity());
						part.setUnitPrice(row.getPricePerPiece());
						part.setIsActive(true);
						partRepo.save(part);

						// Update product stock


						successCount++;
					} else {
						// Create new inventory part
						InventoryPart newPart = InventoryPart.builder()
								.productId(product.getId())
								.supplierId(supplierId)
								.partName(product.getName())
								.partNumber(row.getPartNumber())
								.category(product.getCategoryId())
								.manufacturer(product.getManufacturerId())
								.quantityInStock(row.getQuantity())
								.unitPrice(row.getPricePerPiece())
								.location(null)
								.isActive(true)
								.build();

						partRepo.save(newPart);

						// Update product stock


						successCount++;
					}

				} catch (Exception e) {
					row.setIsValid(false);
					row.setErrorMessage("Error processing row: " + e.getMessage());
					failedCount++;
					failedRows.add(row);
				}
			}

			response.setSuccessfulRows(successCount);
			response.setFailedRows(failedCount);
			response.setFailedRowDetails(failedRows);
			response.setMessage("Import completed: " + successCount + " successful, " + failedCount + " failed");

		} catch (Exception e) {
			response.setMessage("Error reading Excel file: " + e.getMessage());
		}

		return response;
	}

	/**
	 * Search inventory parts with multiple filter criteria
	 * Returns parts with supplier information grouped by part
	 */
	public List<InventoryPartSearchResponse> searchInventoryParts(InventoryPartSearchRequest request) {
		// Set default values for boolean filters
		Boolean inStockOnly = request.getInStockOnly() != null ? request.getInStockOnly() : false;
		Boolean activeOnly = request.getActiveOnly() != null ? request.getActiveOnly() : true;

		// Execute search query
		List<InventoryPart> inventoryParts = partRepo.searchInventoryParts(
				request.getPartNumber(),
				request.getPartName(),
				request.getCategory(),
				request.getManufacturer(),
				request.getProductId(),
				request.getMinPrice(),
				request.getMaxPrice(),
				request.getMinQuantity(),
				inStockOnly,
				activeOnly
		);

		// Map to response with supplier information
		return inventoryParts.stream().map(part -> {
			// Fetch supplier information
			Supplier supplier = null;
			if (part.getSupplierId() != null) {
				try {
					supplier = supplierRepo.findById(Integer.parseInt(part.getSupplierId())).orElse(null);
				} catch (NumberFormatException e) {
					// Handle invalid supplier ID format
				}
			}

			// Build response
			InventoryPartSearchResponse.InventoryPartSearchResponseBuilder builder = InventoryPartSearchResponse.builder()
					.inventoryId(part.getId())
					.productId(part.getProductId())
					.partName(part.getPartName())
					.partNumber(part.getPartNumber())
					.category(part.getCategory())
					.manufacturer(part.getManufacturer())
					.quantityInStock(part.getQuantityInStock())
					.unitPrice(part.getUnitPrice())
					.location(part.getLocation())
					.isActive(part.getIsActive())
					.supplierId(part.getSupplierId());

			// Add supplier details if available
			if (supplier != null) {
				builder.supplierName(supplier.getName())
						.supplierEmail(supplier.getEmail())
						.supplierPhone(supplier.getPhoneNumber())
						.supplierWebsite(supplier.getWebsite())
						.supplierLocation(supplier.getLocation() != null ? supplier.getLocation().toString() : null);
			}

			return builder.build();
		}).collect(Collectors.toList());
	}
}
