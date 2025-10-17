package com.autoadda.apis.part.service;

import com.autoadda.apis.part.entity.InventoryPart;
import com.autoadda.apis.part.entity.InventoryTransaction;
import com.autoadda.apis.part.entity.Product;
import com.autoadda.apis.part.entity.Supplier;
import com.autoadda.apis.part.exception.ResourceNotFoundException;
import com.autoadda.apis.part.mapper.InventoryPartMapper;
import com.autoadda.apis.part.repository.InventoryPartRepository;
import com.autoadda.apis.part.repository.InventoryTransactionRepository;
import com.autoadda.apis.part.repository.ProductRepository;
import com.autoadda.apis.part.repository.SupplierRepository;
import com.autoadda.apis.part.request.InventoryPartExcelRow;
import com.autoadda.apis.part.request.InventoryPartRequest;
import com.autoadda.apis.part.request.InventoryPartSearchRequest;
import com.autoadda.apis.part.response.InventoryPartExcelUploadResponse;
import com.autoadda.apis.part.response.InventoryPartResponse;
import com.autoadda.apis.part.util.ProductCondition;
import com.autoadda.apis.part.util.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryPartRepository partRepo;
    private final InventoryTransactionRepository txnRepo;
    private final ProductRepository productRepo;
    private final SupplierRepository supplierRepo;
    private final ExcelParserService excelParserService;
    private final InventoryPartMapper inventoryPartMapper;

    public InventoryPartResponse addPart(InventoryPartRequest request) {
        Product product = productRepo.findByPartNumber ( request.getPartNumber ( ) );
        InventoryPart part = inventoryPartMapper.toEntity ( request, product );
        InventoryPart savedPart = partRepo.save ( part );

        // Fetch supplier for complete response
        Supplier supplier = null;
        if ( request.getSupplierId ( ) != null ) {
            try {
                supplier = supplierRepo.findById ( Integer.parseInt ( request.getSupplierId ( ) ) ).orElse ( null );
            }
            catch ( NumberFormatException e ) {
                // Handle invalid supplier ID format
            }
        }

        return inventoryPartMapper.toResponse ( savedPart, product, supplier );
    }

    @Transactional
    public InventoryPartResponse updatePart(Integer id, InventoryPartRequest request) {
        // Find existing inventory part
        InventoryPart existingPart = partRepo.findById ( id )
                .orElseThrow ( () -> new ResourceNotFoundException ( "Inventory part not found with id: " + id ) );

        // Get product information
        Product product = productRepo.findByPartNumber ( request.getPartNumber ( ) );
        if ( product == null ) {
            throw new ResourceNotFoundException ( "Product not found with part number: " + request.getPartNumber ( ) );
        }

        // Update fields from request
        existingPart.setProductId ( product.getId ( ) );
        existingPart.setSupplierId ( request.getSupplierId ( ) );
        existingPart.setSku ( request.getSku ( ) );
        existingPart.setPartName ( product.getName ( ) );
        existingPart.setPartNumber ( request.getPartNumber ( ) );
        existingPart.setProductCondition ( request.getCondition ( ) );
        existingPart.setCategory ( product.getCategoryId ( ) );
        existingPart.setManufacturer ( product.getManufacturerId ( ) );
        existingPart.setUnitPrice ( request.getUnitPrice ( ) );
        existingPart.setLocation ( request.getLocation ( ) );
        existingPart.setIsActive ( request.getIsActive ( ) );

        // Save updated part
        InventoryPart updatedPart = partRepo.save ( existingPart );

        // Fetch supplier for complete response
        Supplier supplier = null;
        if ( request.getSupplierId ( ) != null ) {
            try {
                supplier = supplierRepo.findById ( Integer.parseInt ( request.getSupplierId ( ) ) ).orElse ( null );
            }
            catch ( NumberFormatException e ) {
                // Handle invalid supplier ID format
            }
        }

        return inventoryPartMapper.toResponse ( updatedPart, product, supplier );
    }

    public List<InventoryPartResponse> getAllParts() {
        List<InventoryPart> parts = partRepo.findByIsActive ( true );
        return parts.stream ( ).map ( part -> {
            Supplier supplier = null;
            Product product = null;
            if ( part.getSupplierId ( ) != null ) {
                try {
                    supplier = supplierRepo.findById ( Integer.parseInt ( part.getSupplierId ( ) ) ).orElse ( null );
                    product = productRepo.findById ( part.getProductId ( ) ).orElse ( null );
                }
                catch ( NumberFormatException e ) {
                    // Handle invalid supplier ID format
                }
            }
            return inventoryPartMapper.toResponse ( part, product, supplier );
        } ).toList ( );
    }

    @Transactional
    public void softDeletePart(Integer id) {
        InventoryPart part = partRepo.findById ( id )
                .orElseThrow ( () -> new ResourceNotFoundException ( "Inventory part not found with id: " + id ) );

        part.setIsActive ( false );
        partRepo.save ( part );
    }

    @Transactional
    public void restorePart(Integer id) {
        InventoryPart part = partRepo.findById ( id )
                .orElseThrow ( () -> new ResourceNotFoundException ( "Inventory part not found with id: " + id ) );

        part.setIsActive ( true );
        partRepo.save ( part );
    }

    public InventoryPartResponse getPartById(Integer id) {
        InventoryPart part = partRepo.findById ( id ).orElseThrow ( () -> new ResourceNotFoundException ( "Inventory part not found" ) );

        Product product = productRepo.findById ( part.getProductId ( ) ).orElse ( null );
        Supplier supplier = null;
        if ( part.getSupplierId ( ) != null ) {
            try {
                supplier = supplierRepo.findById ( Integer.parseInt ( part.getSupplierId ( ) ) ).orElse ( null );
            }
            catch ( NumberFormatException e ) {
                // Handle invalid supplier ID format
            }
        }

        return inventoryPartMapper.toResponse ( part, product, supplier );
    }

    public InventoryPartResponse getPartByProductId(String productId) {
        InventoryPart part = partRepo.findByProductIdAndIsActive ( productId, true )
                .orElseThrow ( () -> new ResourceNotFoundException ( "Inventory part not found" ) );

        Product product = productRepo.findById ( part.getProductId ( ) ).orElse ( null );
        Supplier supplier = null;
        if ( part.getSupplierId ( ) != null ) {
            try {
                supplier = supplierRepo.findById ( Integer.parseInt ( part.getSupplierId ( ) ) ).orElse ( null );
            }
            catch ( NumberFormatException e ) {
                // Handle invalid supplier ID format
            }
        }

        return inventoryPartMapper.toResponse ( part, product, supplier );
    }

    public List<InventoryPartResponse> getPartsBySupplierId(String supplierId) {
        List<InventoryPart> parts = partRepo.findBySupplierIdAndIsActive ( supplierId, true );
        return parts.stream ( ).map ( part -> {
            Supplier supplier = null;
            Product product = null;
            try {
                supplier = supplierRepo.findById ( Integer.parseInt ( supplierId ) ).orElse ( null );
                product = productRepo.findById ( part.getProductId ( ) ).orElse ( null );
            }
            catch ( NumberFormatException e ) {
                // Handle invalid supplier ID format
            }
            return inventoryPartMapper.toResponse ( part, product, supplier );
        } ).toList ( );
    }

    public List<InventoryPartResponse> getLowStockParts(Integer threshold) {
        List<InventoryPart> parts = partRepo.findByQuantityInStockLessThanAndIsActive ( threshold, true );
        return parts.stream ( ).map ( part -> {
            Supplier supplier = null;
            Product product = null;
            if ( part.getSupplierId ( ) != null ) {
                try {
                    supplier = supplierRepo.findById ( Integer.parseInt ( part.getSupplierId ( ) ) ).orElse ( null );
                    product = productRepo.findById ( part.getProductId ( ) ).orElse ( null );
                }
                catch ( NumberFormatException e ) {
                    // Handle invalid supplier ID format
                }
            }
            return inventoryPartMapper.toResponse ( part, product, supplier );
        } ).toList ( );
    }

    @Transactional
    public void addStock(Integer partId, int quantity, String note) {
        InventoryPart part = partRepo.findById ( partId ).orElseThrow ( () -> new RuntimeException ( "Part not found" ) );

        part.setQuantityInStock ( part.getQuantityInStock ( ) + quantity );
        partRepo.save ( part );

        InventoryTransaction txn = InventoryTransaction
                .builder ( )
                .part ( part )
                .quantity ( quantity )
                .transactionType ( TransactionType.ADD )
                .referenceNote ( note )
                .timestamp ( LocalDateTime.now ( ) )
                .build ( );
        txnRepo.save ( txn );
    }

    @Transactional
    public void removeStock(Integer partId, int quantity, String note) {
        InventoryPart part = partRepo.findById ( partId ).orElseThrow ( () -> new RuntimeException ( "Part not found" ) );

        if ( part.getQuantityInStock ( ) < quantity ) {
            throw new IllegalArgumentException ( "Not enough stock to remove" );
        }

        part.setQuantityInStock ( part.getQuantityInStock ( ) - quantity );
        partRepo.save ( part );

        InventoryTransaction txn = InventoryTransaction.builder ( ).part ( part ).quantity ( quantity )
                .transactionType ( TransactionType.REMOVE ).referenceNote ( note ).timestamp ( LocalDateTime.now ( ) ).build ( );

        txnRepo.save ( txn );
    }

    /**
     * Syncs Product stock with InventoryPart stock
     * Useful for reconciliation or initial setup
     */
    @Transactional
    public void syncProductStock(String productId) {
        Product product = productRepo.findById ( productId )
                .orElseThrow ( () -> new RuntimeException ( "Product not found" ) );

        Optional<InventoryPart> inventoryPart = partRepo.findByProductId ( productId );

//		if (inventoryPart.isPresent()) {
//			product.setStock(inventoryPart.get().getQuantityInStock());
//			productRepo.save(product);
//		}
    }

    public List<InventoryTransaction> getTransactionsForPart(Integer partId) {
        InventoryPart part = partRepo.findById ( partId ).orElseThrow ( () -> new RuntimeException ( "Part not found" ) );
        return txnRepo.findByPart ( part );
    }

    /**
     * Bulk import inventory parts from Excel file
     * Expected columns: Part Number, SKU, Quantity, Price/PC, Condition
     */
    @Transactional
    public InventoryPartExcelUploadResponse bulkImportFromExcel(MultipartFile file, String supplierId) {
        InventoryPartExcelUploadResponse response = new InventoryPartExcelUploadResponse ( 0, 0, 0 );
        List<InventoryPartExcelRow> failedRows = new ArrayList<> ( );

        try {
            // Parse Excel file
            List<InventoryPartExcelRow> excelRows = excelParserService.parseExcelFile ( file );
            response.setTotalRows ( excelRows.size ( ) );

            int successCount = 0;
            int failedCount = 0;

            // Process each row
            for (InventoryPartExcelRow row : excelRows) {
                if ( Boolean.FALSE.equals ( row.getIsValid ( ) ) ) {
                    failedCount++;
                    failedRows.add ( row );
                    continue;
                }

                try {
                    // Find product by part number or SKU
                    Product products = productRepo.findByPartNumber ( row.getPartNumber ( ) );

//
                    if ( products == null ) {
                        row.setIsValid ( false );
                        row.setErrorMessage ( "Product not found with Part Number: " + row.getPartNumber ( ) + " or SKU: " + row.getSku ( ) );
                        failedCount++;
                        failedRows.add ( row );
                        continue;
                    }

                    Product product = products; // Take first match

                    // Check if inventory part already exists for this supplier and product
                    List<InventoryPart> existingPart = partRepo.findByProductIdAndSupplierIdAndProductCondition ( product.getId ( ), supplierId, ProductCondition.valueOf ( row.getCondition ( ) ) );

                    if ( ! existingPart.isEmpty ( ) ) {
                        // Update existing inventory
                        InventoryPart part = existingPart.get ( 0 );
                        part.setQuantityInStock ( part.getQuantityInStock ( ) + row.getQuantity ( ) );
                        part.setUnitPrice ( row.getPricePerPiece ( ) );
                        part.setSku ( row.getSku ( ) );
                        part.setIsActive ( true );
                        partRepo.save ( part );
                        // Update product stock
                        successCount++;
                    } else {
                        // Create new inventory part
                        InventoryPart newPart = InventoryPart.builder ( )
                                .productId ( product.getId ( ) )
                                .supplierId ( supplierId )
                                .partName ( product.getName ( ) )
                                .partNumber ( row.getPartNumber ( ) )
                                .productCondition ( ProductCondition.valueOf ( row.getCondition ( ) ) )
                                .category ( product.getCategoryId ( ) )
                                .manufacturer ( product.getManufacturerId ( ) )
                                .quantityInStock ( row.getQuantity ( ) )
                                .unitPrice ( row.getPricePerPiece ( ) )
                                .location ( null )
                                .isActive ( true )
                                .build ( );

                        partRepo.save ( newPart );

                        // Update product stock


                        successCount++;
                    }

                }
                catch ( Exception e ) {
                    row.setIsValid ( false );
                    row.setErrorMessage ( "Error processing row: " + e.getMessage ( ) );
                    failedCount++;
                    failedRows.add ( row );
                }
            }
            response.setStatusCode ( failedCount < 1 ? "200" : "400" );
            response.setSuccessfulRows ( successCount );
            response.setFailedRows ( failedCount );
            response.setFailedRowDetails ( failedRows );
            response.setMessage ( "Import completed: " + successCount + " successful, " + failedCount + " failed" );

        }
        catch ( Exception e ) {
            response.setMessage ( "Error reading Excel file: " + e.getMessage ( ) );
        }

        return response;
    }

    /**
     * Search inventory parts with multiple filter criteria
     * Returns lightweight search responses with supplier information
     */
    public List<InventoryPartResponse> searchInventoryParts(InventoryPartSearchRequest request) {
        // Set default values for boolean filters
        Boolean inStockOnly = request.getInStockOnly ( ) != null ? request.getInStockOnly ( ) : false;
        Boolean activeOnly = request.getActiveOnly ( ) != null ? request.getActiveOnly ( ) : true;

        // Set default sorting (default to id ascending if not specified)
        String sortBy = request.getSortBy ( );
        String sortDirection = request.getSortDirection ( ) != null ? request.getSortDirection ( ).toUpperCase ( ) : "ASC";

        // Execute search query with new filters
        List<InventoryPart> inventoryParts = partRepo.searchInventoryParts (
                request.getPartNumber ( ),
                request.getPartName ( ),
                request.getCategory ( ),
                request.getManufacturer ( ),
                request.getProductId ( ),
                request.getLocation ( ),
                request.getProductCondition ( ),
                request.getSupplierId ( ),
                request.getMinPrice ( ),
                request.getMaxPrice ( ),
                request.getMinQuantity ( ),
                inStockOnly,
                activeOnly,
                sortBy,
                sortDirection
        );

        // Map to lightweight search response with supplier information using mapper
        return inventoryParts.stream ( ).map ( part -> {
            Supplier supplier = null;
            Product product = null;
            if ( part.getSupplierId ( ) != null ) {
                try {
                    supplier = supplierRepo.findById ( Integer.parseInt ( part.getSupplierId ( ) ) ).orElse ( null );
                    product = productRepo.findById ( part.getProductId ( ) ).orElse ( null );
                }
                catch ( NumberFormatException e ) {
                    // Handle invalid supplier ID format
                }
            }
            return inventoryPartMapper.toResponse ( part, product, supplier );
        } ).toList ( );
    }

    /**
     * Get supplier count for a specific product
     * Returns the number of suppliers that have this product in stock
     */
    public Long getSupplierCountByProductId(String productId) {
        return partRepo.countByProductIdAndIsActiveAndQuantityInStockGreaterThan ( productId, true, 0 );
    }

    /**
     * Get supplier counts for multiple products
     * Returns a map of productId -> supplier count
     */
    public Map<String, Long> getSupplierCountsByProductIds(List<String> productIds) {
        Map<String, Long> supplierCounts = new HashMap<> ( );
        for (String productId : productIds) {
            Long count = partRepo.countByProductIdAndIsActiveAndQuantityInStockGreaterThan ( productId, true, 0 );
            supplierCounts.put ( productId, count );
        }
        return supplierCounts;
    }
}
