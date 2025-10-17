package com.autoadda.apis.part.controller;

import com.autoadda.apis.part.request.ProductSearchRequest;
import com.autoadda.apis.part.response.ProductResponse;
import com.autoadda.apis.part.service.ProductSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Unified Product Search Controller
 * This controller provides product search endpoints that include supplier availability information
 * Use this for customer-facing product search where supplier counts matter
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/products/search")
@RequiredArgsConstructor
@Tag(name = "Product Search with Supplier Availability", description = "Search products and see how many suppliers have them in stock")
public class ProductSearchController {

    private final ProductSearchService productSearchService;

    @Operation(summary = "Search products by name with supplier count",
            description = "Search for products by name and get the number of suppliers that have each product in stock")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Products found successfully"),
            @ApiResponse(responseCode = "404", description = "No products found")
    })
    @GetMapping("/by-name")
    public ResponseEntity<List<ProductResponse>> searchByName(
            @Parameter(description = "Product name to search for", required = true)
            @RequestParam String name) {
        log.info("Searching products by name: {}", name);
        List<ProductResponse> products = productSearchService.searchProductsByName(name);
        return ResponseEntity.ok(products);
    }

    @Operation(summary = "Search product by part number with supplier count",
            description = "Find a specific product by its part number and get supplier availability")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product found successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @GetMapping("/by-part-number")
    public ResponseEntity<ProductResponse> searchByPartNumber(
            @Parameter(description = "Part number to search for", required = true)
            @RequestParam String partNumber) {
        log.info("Searching product by part number: {}", partNumber);
        ProductResponse product = productSearchService.searchProductByPartNumber(partNumber);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(product);
    }

    @Operation(summary = "Search products by vehicle (year/make/model) with supplier count",
            description = "Find products that fit a specific vehicle and see supplier availability")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Products found successfully"),
            @ApiResponse(responseCode = "404", description = "No products found")
    })
    @GetMapping("/by-vehicle")
    public ResponseEntity<List<ProductResponse>> searchByVehicle(
            @Parameter(description = "Vehicle year", required = true) @RequestParam Integer year,
            @Parameter(description = "Vehicle make", required = true) @RequestParam String make,
            @Parameter(description = "Vehicle model", required = true) @RequestParam String model) {
        log.info("Searching products by vehicle: {}/{}/{}", year, make, model);
        List<ProductResponse> products = productSearchService.searchProductsByYearMakeModel(year, make, model);
        return ResponseEntity.ok(products);
    }

    @Operation(summary = "Search products by category with supplier count",
            description = "Get all products in a category with supplier availability")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Products found successfully"),
            @ApiResponse(responseCode = "404", description = "No products found")
    })
    @GetMapping("/by-category")
    public ResponseEntity<List<ProductResponse>> searchByCategory(
            @Parameter(description = "Category ID", required = true)
            @RequestParam String categoryId) {
        log.info("Searching products by category: {}", categoryId);
        List<ProductResponse> products = productSearchService.searchProductsByCategory(categoryId);
        return ResponseEntity.ok(products);
    }

    @Operation(summary = "Search products by manufacturer with supplier count",
            description = "Get all products from a manufacturer with supplier availability")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Products found successfully"),
            @ApiResponse(responseCode = "404", description = "No products found")
    })
    @GetMapping("/by-manufacturer")
    public ResponseEntity<List<ProductResponse>> searchByManufacturer(
            @Parameter(description = "Manufacturer ID", required = true)
            @RequestParam String manufacturerId) {
        log.info("Searching products by manufacturer: {}", manufacturerId);
        List<ProductResponse> products = productSearchService.searchProductsByManufacturer(manufacturerId);
        return ResponseEntity.ok(products);
    }

    @Operation(summary = "Search products by other names with supplier count",
            description = "Search for products using alternative names")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Products found successfully"),
            @ApiResponse(responseCode = "404", description = "No products found")
    })
    @GetMapping("/by-other-names")
    public ResponseEntity<List<ProductResponse>> searchByOtherNames(
            @Parameter(description = "Alternative name to search for", required = true)
            @RequestParam String otherNames) {
        log.info("Searching products by other names: {}", otherNames);
        List<ProductResponse> products = productSearchService.searchProductsByOtherNames(otherNames);
        return ResponseEntity.ok(products);
    }

    @Operation(summary = "Search products by tag with supplier count",
            description = "Find products with a specific tag")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Products found successfully"),
            @ApiResponse(responseCode = "404", description = "No products found")
    })
    @GetMapping("/by-tag")
    public ResponseEntity<List<ProductResponse>> searchByTag(
            @Parameter(description = "Tag to search for", required = true)
            @RequestParam String tag) {
        log.info("Searching products by tag: {}", tag);
        List<ProductResponse> products = productSearchService.searchProductsByTag(tag);
        return ResponseEntity.ok(products);
    }

    @Operation(summary = "Get product by ID with supplier count",
            description = "Retrieve a specific product by its ID with supplier availability")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product found successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(
            @Parameter(description = "Product ID", required = true)
            @PathVariable String id) {
        log.info("Getting product by ID: {}", id);
        ProductResponse product = productSearchService.getProductById(id);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(product);
    }

    @Operation(summary = "Get all products with supplier counts",
            description = "Retrieve all products in the catalog with supplier availability")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Products retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        log.info("Getting all products with supplier counts");
        List<ProductResponse> products = productSearchService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @Operation(summary = "Advanced product search with multiple filters",
            description = "Search products using multiple optional filters including name, part number, category, " +
                    "manufacturer, vehicle info, tags, and supplier availability. All filters are optional and can be combined.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Products found successfully"),
            @ApiResponse(responseCode = "404", description = "No products found")
    })
    @PostMapping("/advanced")
    public ResponseEntity<List<ProductResponse>> advancedSearch(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Search criteria with multiple optional filters",
                    required = true
            )
            @RequestBody ProductSearchRequest searchRequest) {
        log.info("Advanced product search with filters: {}", searchRequest);
        List<ProductResponse> products = productSearchService.advancedSearch(searchRequest);
        return ResponseEntity.ok(products);
    }
}