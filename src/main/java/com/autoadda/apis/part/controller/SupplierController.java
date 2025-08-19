package com.autoadda.apis.part.controller;

import com.autoadda.apis.part.entity.Supplier;
import com.autoadda.apis.part.request.SupplierRequest;
import com.autoadda.apis.part.service.SupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @Operation(summary = "Creating a Supplier with basic details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfull sign up process with return success message and registered supplier details"),
            @ApiResponse(responseCode = "404", description = "register Supplier api not found", content = @Content)})
    @PostMapping
    public ResponseEntity<Supplier> createSupplier(
            @RequestHeader(value = "X-Auto-Adda-Id") String autoAddaId,
            @RequestBody SupplierRequest payload) {
        return supplierService.createSupplier ( payload, autoAddaId );
    }

    @Operation(summary = "Retrieving All suppliers")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successfull retrieving all suppliers"),
            @ApiResponse(responseCode = "404", description = "retrieve all suppliers api not found")})
    @GetMapping("/all")
    public ResponseEntity<List<Supplier>> getAllCategories() {
        return supplierService.getAllSuppliers ( );
    }

    @Operation(summary = "Retrieving suppliers by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successfull retrieving all suppliers"),
            @ApiResponse(responseCode = "404", description = "retrieve all suppliers api not found", content = @Content)})
    @GetMapping("/{id}")
    public ResponseEntity<List<Supplier>> getSupplierById(@PathVariable Integer id) {
        return supplierService.getSupplier ( id );
    }


    @Operation(summary = "Retrieving logged in Suppliers ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successfull retrieving suppliers"),
            @ApiResponse(responseCode = "404", description = "retrieve suppliers api not found", content = @Content)})
    @GetMapping
    public ResponseEntity<List<Supplier>> getSupplierByName(
            @RequestHeader(value = "X-Auto-Adda-Id") String autoAddaId) {
        return supplierService.getSupplier ( autoAddaId );
    }

    @Operation(summary = "Deleting Suppliers by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful deleting suppliers")})
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteSupplierById(@PathVariable Integer id,
                                                     @RequestHeader(value = "X-Auto-Adda-Id") String autoAddaId) {
        return supplierService.deleteSupplierById ( id, autoAddaId );
    }


    @Operation(summary = "Updating Suppliers by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successfull updating suppliers"),
            @ApiResponse(responseCode = "404", description = "update suppliers api not found", content = @Content)})
    @PutMapping("/{id}")
    public ResponseEntity<Supplier> updateSupplierById(@PathVariable Integer id,
                                                       @RequestBody SupplierRequest supplier,
                                                       @RequestHeader(value = "X-Auto-Adda-Id") String autoAddaId) {
        return supplierService.updateSupplier ( supplier, autoAddaId, id );

    }

}
