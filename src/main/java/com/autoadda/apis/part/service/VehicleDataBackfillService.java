package com.autoadda.apis.part.service;

import com.autoadda.apis.part.entity.Product;
import com.autoadda.apis.part.exception.VehicleFitException;
import com.autoadda.apis.part.repository.ProductRepository;
import com.autoadda.apis.part.request.ProductRequest;
import com.autoadda.apis.part.response.VehicleBackfillResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

/**
 * One-time conversion of existing products: reads the make / model / year text, looks up the ids
 * (same rules as saving a product) and stores them. The text stays. Products that already have ids
 * are skipped, so it can be run again after fixing the unresolved ones.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleDataBackfillService {

    private final ProductRepository productRepository;
    private final VehicleFitResolver vehicleFitResolver;

    @Transactional
    public VehicleBackfillResponse backfill(boolean dryRun) {
        List<Product> candidates = productRepository.findAll().stream()
                .filter(VehicleDataBackfillService::needsConversion)
                .toList();

        int resolvable = 0;
        List<Product> toSave = new ArrayList<>();
        List<VehicleBackfillResponse.Unresolved> unresolved = new ArrayList<>();
        for (Product product : candidates) {
            ProductRequest legacy = ProductRequest.builder()
                    .make(product.getMake())
                    .model(product.getModel())
                    .year(product.getYear())
                    .build();
            try {
                VehicleFitResolver.VehicleFit fit = vehicleFitResolver.resolve(legacy, null);
                resolvable++;
                if (!dryRun) {
                    vehicleFitResolver.apply(fit, product);
                    toSave.add(product);
                }
            } catch (VehicleFitException e) {
                unresolved.add(VehicleBackfillResponse.Unresolved.builder()
                        .productId(product.getId())
                        .partNumber(product.getPartNumber())
                        .field(e.getField())
                        .value(e.getValue())
                        .reason(e.getMessage())
                        .build());
            }
        }
        if (!toSave.isEmpty()) {
            productRepository.saveAll(toSave);
        }
        log.info("Vehicle data backfill (dryRun={}): {} candidates, {} resolvable, {} converted, {} unresolved",
                dryRun, candidates.size(), resolvable, toSave.size(), unresolved.size());

        return VehicleBackfillResponse.builder()
                .dryRun(dryRun)
                .candidates(candidates.size())
                .resolvable(resolvable)
                .converted(toSave.size())
                .unresolved(unresolved)
                .build();
    }

    /** Has vehicle text but none of the new id data yet. */
    static boolean needsConversion(Product product) {
        boolean hasText = notBlank(product.getMake()) || notBlank(product.getModel()) || notBlank(product.getYear());
        boolean hasIds = product.getMakeId() != null
                || (product.getModelIds() != null && !product.getModelIds().isEmpty())
                || (product.getYears() != null && !product.getYears().isEmpty());
        return hasText && !hasIds;
    }

    private static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }
}
