package com.autoadda.apis.part.controller;

import com.autoadda.apis.part.request.ProductListPayload;
import com.autoadda.apis.part.request.ProductRequest;
import com.autoadda.apis.part.response.GenericResponse;
import com.autoadda.apis.part.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
@CrossOrigin("*")
@Slf4j
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;


    @Operation(summary = "Retrieving All Top Level Products")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successfull retrieving all products", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "404", description = "retrieve all products api not found", content = @Content)})
    @GetMapping
    public GenericResponse getAllTopLevelProducts() {
        return productService.getAllProducts ( );
    }

    @Operation(summary = "Retrieving Products by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successfull retrieving products", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "404", description = "retrieve products api not found", content = @Content)})
    @GetMapping("/{id}")
    public GenericResponse getProductByName(@PathVariable String id) {
        return productService.getProductsById ( id );
    }

    @Operation(summary = "Creating a Product with basic details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfull sign up process with return success message and registered product details", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "404", description = "register PartProduct api not found", content = @Content)})
    @PostMapping
    public GenericResponse createPartProduct(@RequestBody ProductListPayload payload) {
        return productService.createProduct ( payload.getProductRequestList ( ) );
    }

    @Operation(summary = "Updating Products by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successfull updating products", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "404", description = "update products api not found", content = @Content)})
    @PutMapping
    public GenericResponse updateProductById(@RequestBody ProductRequest product) {
        return productService.updateProduct ( product );
    }

    @Operation(summary = "Deleting Products by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successfull deleting products", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "404", description = "delete product api not found", content = @Content)})
    @DeleteMapping("/{id}")
    public GenericResponse deleteProductById(@PathVariable String id) {
        return productService.deleteProductById ( id );
    }

    @Operation(summary = "Searching Products by Name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successfull retrieving products", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "404", description = "retrieve products api not found", content = @Content)})
    @GetMapping("/name/{name}")
    public GenericResponse searchProductsByName(@PathVariable String name) {
        return productService.searchProductsByName ( name );
    }

    @Operation(summary = "Searching Products by Other Names")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successfull Searching products", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Search products api not found", content = @Content)})
    @GetMapping("/othernames/{otherNames}")
    public GenericResponse searchProductsByOtherNames(@PathVariable String otherNames) {
        return productService.searchProductsByOtherNames ( otherNames );
    }

    @Operation(summary = "Searching Products by Tag")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successfull searching products", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Search products api not found", content = @Content)})
    @GetMapping("/tag/{tag}")
    public GenericResponse searchProductsByTag(@PathVariable String tag) {
        return productService.searchProductsByTag ( tag );
    }


    @Operation(summary = "Searching Products by Category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successfull searching products", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Search products api not found", content = @Content)})
    @GetMapping("/category/{category}")
    public GenericResponse searchProductsByCategory(@PathVariable String category) {
        return productService.searchProductsByCategory ( category );
    }

    @Operation(summary = "Searching Products by Supplier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successfull searching products", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Search products api not found", content = @Content)})
    @GetMapping("/supplier/{supplierId}")
    public GenericResponse searchProductsBySupplier(@PathVariable String supplierId) {
        return productService.searchProductsBySupplier ( supplierId );
    }

    @Operation(summary = "Searching Products by PartNumber")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successfull searching products", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Search products api not found", content = @Content)})
    @GetMapping("/partnumber/{partNumber}")
    public GenericResponse searchProductsByPartNumber(@PathVariable String partNumber) {
        return productService.searchProductsByPartNumber ( partNumber );
    }

    @Operation(summary = "Searching Products by Year Make Model")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successfull searching products", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Search products api not found", content = @Content)})
    @GetMapping("/year/{year}/make/{make}/model/{model}")
    public GenericResponse searchProductsByPartNumber(@PathVariable Integer year, @PathVariable String make,
                                                      @PathVariable String model) {
        return productService.searchProductsByYearMakeModel ( year, make, model );
    }

}
