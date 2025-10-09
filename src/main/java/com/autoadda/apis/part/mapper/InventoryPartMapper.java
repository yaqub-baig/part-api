package com.autoadda.apis.part.mapper;

import com.autoadda.apis.part.entity.InventoryPart;
import com.autoadda.apis.part.entity.Product;
import com.autoadda.apis.part.entity.Supplier;
import com.autoadda.apis.part.request.InventoryPartRequest;
import com.autoadda.apis.part.response.InventoryPartResponse;
import com.autoadda.apis.part.response.InventoryPartSearchResponse;
import org.springframework.stereotype.Component;

@Component
public class InventoryPartMapper {

    /**
     * Maps InventoryPartRequest and Product to InventoryPart entity
     * This matches the flow in InventoryService.prepareInventoryPart()
     */
    public InventoryPart toEntity(InventoryPartRequest request, Product product) {
        return InventoryPart.builder()
                .productId(request.getProductId())
                .supplierId(request.getSupplierId())
                .sku(request.getSku())
                .productCondition ( request.getCondition () )
                .quantityInStock(request.getQuantityInStock() != null ? request.getQuantityInStock() : 0)
                .unitPrice(request.getUnitPrice())
                .location(request.getLocation())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)

                .partName(product.getName())
                .partNumber(product.getPartNumber())
                .category(product.getCategoryId())
                .manufacturer(product.getManufacturerId())
                .build();
    }

    /**
     * Maps InventoryPart to InventoryPartSearchResponse
     * This matches the flow in InventoryService.searchInventoryParts()
     */
    public InventoryPartSearchResponse toSearchResponse(InventoryPart part, Supplier supplier) {
        InventoryPartSearchResponse.InventoryPartSearchResponseBuilder builder = InventoryPartSearchResponse.builder()
                .inventoryId(part.getId())
                .productId(part.getProductId())
                .partName(part.getPartName())
                .productCondition ( part.getProductCondition () )
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
    }

    /**
     * Overloaded method for mapping without supplier information
     */
    public InventoryPartSearchResponse toSearchResponse(InventoryPart part) {
        return toSearchResponse(part, null);
    }

    /**
     * Maps InventoryPart, Product, and Supplier to InventoryPartResponse with full details
     */
    public InventoryPartResponse toResponse(InventoryPart part, Product product, Supplier supplier) {
        InventoryPartResponse.InventoryPartResponseBuilder builder = InventoryPartResponse.builder()
                .inventoryId(part.getId())
                .productId(part.getProductId())
                .supplierId(part.getSupplierId())
                .sku(part.getSku())
                .partName(part.getPartName())
                .partNumber(part.getPartNumber())
                .productCondition ( part.getProductCondition () )
                .category(part.getCategory())
                .manufacturer(part.getManufacturer())
                .quantityInStock(part.getQuantityInStock())
                .unitPrice(part.getUnitPrice())
                .location(part.getLocation())
                .isActive(part.getIsActive());

        // Add product details if available
        if (product != null) {
            builder.productName(product.getName())
                    .productDescription(product.getDescription())
                    .productTag(product.getTag())
                    .productOtherNames(product.getOtherNames())
                    .productYear(product.getYear())
                    .productMake(product.getMake())
                    .productModel(product.getModel())
                    .productImages(product.getImages());
        }

        // Add supplier details if available
        if (supplier != null) {
            builder.supplierName(supplier.getName())
                    .supplierEmail(supplier.getEmail())
                    .supplierPhone(supplier.getPhoneNumber())
                    .supplierWebsite(supplier.getWebsite())
                    .supplierLocation(supplier.getLocation() != null ? supplier.getLocation().toString() : null);
        }

        return builder.build();
    }

    /**
     * Overloaded method for mapping without supplier and product information
     */
    public InventoryPartResponse toResponse(InventoryPart part) {
        return toResponse(part, null, null);
    }

    /**
     * Updates existing InventoryPart entity from request
     * Only updates non-null fields from the request
     */
    public void updateEntityFromRequest(InventoryPart entity, InventoryPartRequest request) {
        if (request.getQuantityInStock() != null) {
            entity.setQuantityInStock(request.getQuantityInStock());
        }
        if (request.getUnitPrice() != null) {
            entity.setUnitPrice(request.getUnitPrice());
        }
        if (request.getLocation() != null) {
            entity.setLocation(request.getLocation());
        }
        if (request.getSku() != null) {
            entity.setSku(request.getSku());
        }
        if (request.getIsActive() != null) {
            entity.setIsActive(request.getIsActive());
        }
    }
}
