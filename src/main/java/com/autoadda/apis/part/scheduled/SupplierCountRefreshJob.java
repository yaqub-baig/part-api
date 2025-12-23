package com.autoadda.apis.part.scheduled;

import com.autoadda.apis.part.entity.Product;
import com.autoadda.apis.part.repository.InventoryPartRepository;
import com.autoadda.apis.part.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Scheduled job to refresh supplier counts for all products
 * Runs daily at 2 AM to ensure data accuracy
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SupplierCountRefreshJob {

    private final ProductRepository productRepository;
    private final InventoryPartRepository inventoryPartRepository;

    /**
     * Refresh supplier counts for all products
     * Scheduled to run daily at 2:00 AM
     * Cron expression: second minute hour day month day-of-week
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void refreshSupplierCounts() {
        log.info("Starting supplier count refresh job");

        long startTime = System.currentTimeMillis();
        int updatedCount = 0;
        int totalProducts = 0;

        try {
            // Get all products
            List<Product> products = productRepository.findAll();
            totalProducts = products.size();

            log.info("Processing {} products for supplier count refresh", totalProducts);

            // Update supplier count for each product
            for (Product product : products) {
                try {
                    Long count = inventoryPartRepository
                            .countByProductIdAndIsActiveAndQuantityInStockGreaterThan(
                                    product.getId(), true, 0);

                    // Only update if count has changed
                    Integer currentCount = product.getSupplierCount() != null ? product.getSupplierCount() : 0;
                    if (!count.equals(currentCount.longValue())) {
                        product.setSupplierCount(count.intValue());
                        productRepository.save(product);
                        updatedCount++;
                        log.info("Updated supplier count for product :: {}", updatedCount);
                    }
                } catch (Exception e) {
                    log.error("Error updating supplier count for product {}: {}",
                            product.getId(), e.getMessage());
                }
            }

            long duration = System.currentTimeMillis() - startTime;
            log.info("Supplier count refresh completed: {} out of {} products updated in {}ms",
                    updatedCount, totalProducts, duration);

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Supplier count refresh job failed after {}ms: {}",
                    duration, e.getMessage(), e);
        }
    }

    /**
     * Manual trigger for supplier count refresh (can be called via API or admin tool)
     * This method allows manual refresh without waiting for the scheduled run
     */
    @Transactional
    public void refreshSupplierCountsManually() {
        log.info("Manual supplier count refresh triggered");
        refreshSupplierCounts();
    }
}
