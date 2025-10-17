package com.autoadda.apis.part.controller;

import com.autoadda.apis.part.entity.InventoryTransaction;
import com.autoadda.apis.part.request.InventoryPartRequest;
import com.autoadda.apis.part.request.InventoryPartSearchRequest;
import com.autoadda.apis.part.response.InventoryPartExcelUploadResponse;
import com.autoadda.apis.part.response.InventoryPartResponse;
import com.autoadda.apis.part.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    // Create a new inventory part
    @PostMapping("/parts")
    public ResponseEntity<InventoryPartResponse> createPart(@RequestBody InventoryPartRequest request) {
        return ResponseEntity.ok ( inventoryService.addPart ( request ) );
    }

    // Get all parts
    @GetMapping("/parts")
    public ResponseEntity<List<InventoryPartResponse>> getAllParts() {
        return ResponseEntity.ok ( inventoryService.getAllParts ( ) );
    }

    // Get part by ID
    @GetMapping("/parts/{id}")
    public ResponseEntity<InventoryPartResponse> getPartById(@PathVariable Integer id) {
        return ResponseEntity.ok ( inventoryService.getPartById ( id ) );
    }

    // Get parts by supplier ID
    @GetMapping("/parts/supplier/{supplierId}")
    public ResponseEntity<List<InventoryPartResponse>> getPartsBySupplierId(@PathVariable String supplierId) {
        return ResponseEntity.ok ( inventoryService.getPartsBySupplierId ( supplierId ) );
    }

    // Search inventory parts with filters and supplier information
    @PostMapping("/parts/search")
    public ResponseEntity<List<InventoryPartResponse>> searchParts(@RequestBody InventoryPartSearchRequest request) {
        return ResponseEntity.ok ( inventoryService.searchInventoryParts ( request ) );
    }

    // Add stock to a part
    @PostMapping("/parts/{id}/add-stock")
    public ResponseEntity<String> addStock(@PathVariable Integer id, @RequestParam int quantity,
                                           @RequestParam(required = false, defaultValue = "Stock added") String note) {
        inventoryService.addStock ( id, quantity, note );
        return ResponseEntity.ok ( "Stock added successfully." );
    }

    // Remove stock from a part
    @PostMapping("/parts/{id}/remove-stock")
    public ResponseEntity<String> removeStock(@PathVariable Integer id, @RequestParam int quantity,
                                              @RequestParam(required = false, defaultValue = "Stock used") String note) {
        inventoryService.removeStock ( id, quantity, note );
        return ResponseEntity.ok ( "Stock removed successfully." );
    }

    // Get all transactions for a part
    @GetMapping("/parts/{id}/transactions")
    public ResponseEntity<List<InventoryTransaction>> getTransactions(@PathVariable Integer id) {
        return ResponseEntity.ok ( inventoryService.getTransactionsForPart ( id ) );
    }

    // Update an existing inventory part
    @PutMapping("/parts/{id}")
    public ResponseEntity<InventoryPartResponse> updatePart(@PathVariable Integer id,
                                                             @RequestBody InventoryPartRequest request) {
        return ResponseEntity.ok ( inventoryService.updatePart ( id, request ) );
    }

    // Soft delete an inventory part (mark as inactive)
    @DeleteMapping("/parts/{id}")
    public ResponseEntity<String> deletePart(@PathVariable Integer id) {
        inventoryService.softDeletePart ( id );
        return ResponseEntity.ok ( "Inventory part deleted successfully." );
    }

    // Restore a soft-deleted inventory part (mark as active)
    @PostMapping("/parts/{id}/restore")
    public ResponseEntity<String> restorePart(@PathVariable Integer id) {
        inventoryService.restorePart ( id );
        return ResponseEntity.ok ( "Inventory part restored successfully." );
    }

    // Bulk upload inventory parts from Excel
    @PostMapping(value = "/parts/bulk-upload", consumes = {"multipart/form-data"})
    public ResponseEntity<InventoryPartExcelUploadResponse> bulkUploadInventory(
            @RequestParam("file") MultipartFile file,
            @RequestParam("supplierId") String supplierId) {

        if ( file.isEmpty ( ) ) {
            return ResponseEntity.badRequest ( ).body (
                    InventoryPartExcelUploadResponse.builder ( )
                            .message ( "Please upload a valid Excel file" )
                            .build ( )
            );
        }

        // Validate file type
        String fileName = file.getOriginalFilename ( );
        if ( fileName == null || ! fileName.endsWith ( ".xlsx" ) ) {
            return ResponseEntity.badRequest ( ).body (
                    InventoryPartExcelUploadResponse.builder ( )
                            .message ( "Please upload an Excel file (.xlsx format)" )
                            .build ( )
            );
        }

        InventoryPartExcelUploadResponse response = inventoryService.bulkImportFromExcel ( file, supplierId );
        return ResponseEntity.ok ( response );
    }

    // Get supplier count for a specific product
    @GetMapping("/supplier-count/{productId}")
    public ResponseEntity<Long> getSupplierCount(@PathVariable String productId) {
        Long count = inventoryService.getSupplierCountByProductId ( productId );
        return ResponseEntity.ok ( count );
    }

    // Get supplier counts for multiple products
    @PostMapping("/supplier-counts")
    public ResponseEntity<java.util.Map<String, Long>> getSupplierCounts(@RequestBody List<String> productIds) {
        java.util.Map<String, Long> counts = inventoryService.getSupplierCountsByProductIds ( productIds );
        return ResponseEntity.ok ( counts );
    }

}
