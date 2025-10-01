package com.autoadda.apis.part.controller;

import com.autoadda.apis.part.request.ProductAttributeListPayload;
import com.autoadda.apis.part.request.ProductAttributeRequest;
import com.autoadda.apis.part.response.GenericResponse;
import com.autoadda.apis.part.service.ProductAttributeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/productattributes")
@RequiredArgsConstructor
public class ProductAttributeController {

    private final ProductAttributeService productAttributeService;

    @CrossOrigin
    @Operation(summary = "Retrieving All ProductAttributes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successfull retrieving all productattribues", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "404", description = "retrieve all productattribues api not found", content = @Content)})
    @GetMapping
    public GenericResponse getAllProductAttributes() {
            return productAttributeService.getProductAttributes ( );
    }

    @Operation(summary = "Retrieving ProductAttributes by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successfull retrieving productattribues", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "404", description = "retrieve productattribues api not found", content = @Content)})
    @GetMapping("/{id}")
    public GenericResponse getProductAttributeById(@PathVariable String id) {
        return productAttributeService.getProductAttributeById ( id );
    }

    @Operation(summary = "Retrieving ProductAttributes by ProductId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successfull retrieving productattribues", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "404", description = "retrieve productattribues api not found", content = @Content)})
    @GetMapping("/productid/{productId}")
    public GenericResponse getProductAttributeByName(@PathVariable String productId) {
        return productAttributeService.getProductAttributeByProductId ( productId );
    }

    @Operation(summary = "Creating a ProductAttributeAttribute with basic details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfull sign up process with return success message and registered product details", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "404", description = "register PartProductAttribute api not found", content = @Content)})
    @PostMapping
    public GenericResponse createProductAttribute(@RequestBody ProductAttributeListPayload payload) {
        return productAttributeService.createProductAttribute ( payload.getProductAttributeRequestList ( ) );
    }

    @Operation(summary = "Updating ProductAttributes by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successfull updating productattribues", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "404", description = "update productattribues api not found", content = @Content)})
    @PutMapping
    public GenericResponse updateProductAttributeById(@RequestBody ProductAttributeRequest product) {
        return productAttributeService.updateProductAttribute ( product );
    }

    @Operation(summary = "Deleting ProductAttributes by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successfull deleting productattribues", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "404", description = "delete product api not found", content = @Content)})
    @DeleteMapping("/{id}")
    public GenericResponse deleteProductAttributeById(@PathVariable String id) {
        return productAttributeService.deleteProductAttributeById ( id );
    }
}
