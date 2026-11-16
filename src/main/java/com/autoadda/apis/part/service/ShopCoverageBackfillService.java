package com.autoadda.apis.part.service;

import com.autoadda.apis.part.entity.Supplier;
import com.autoadda.apis.part.exception.VehicleFitException;
import com.autoadda.apis.part.repository.SupplierRepository;
import com.autoadda.apis.part.request.SupplierRequest;
import com.autoadda.apis.part.response.ShopCoverageBackfillResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/** One-time conversion of the brands / models text shops registered with into make and model ids. */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShopCoverageBackfillService {

    private final SupplierRepository supplierRepository;
    private final ShopCoverageResolver shopCoverageResolver;

    @Transactional
    public ShopCoverageBackfillResponse backfill(boolean dryRun) {
        List<Supplier> candidates = supplierRepository.findAll().stream()
                .filter(ShopCoverageBackfillService::needsConversion)
                .toList();

        int resolvable = 0;
        List<Supplier> toSave = new ArrayList<>();
        List<ShopCoverageBackfillResponse.Unresolved> unresolved = new ArrayList<>();
        for (Supplier supplier : candidates) {
            SupplierRequest legacy = SupplierRequest.builder()
                    .brands(supplier.getBrands())
                    .models(supplier.getModels())
                    .build();
            try {
                // existing passed so categories / conditions already set on the shop are kept
                ShopCoverageResolver.ShopCoverage coverage = shopCoverageResolver.resolve(legacy, supplier);
                resolvable++;
                if (!dryRun) {
                    shopCoverageResolver.apply(coverage, supplier);
                    toSave.add(supplier);
                }
            } catch (VehicleFitException e) {
                unresolved.add(ShopCoverageBackfillResponse.Unresolved.builder()
                        .supplierId(supplier.getId())
                        .supplierName(supplier.getName())
                        .field(e.getField())
                        .value(e.getValue())
                        .reason(e.getMessage())
                        .build());
            }
        }
        if (!toSave.isEmpty()) {
            supplierRepository.saveAll(toSave);
        }
        log.info("Shop coverage backfill (dryRun={}): {} candidates, {} resolvable, {} converted, {} unresolved",
                dryRun, candidates.size(), resolvable, toSave.size(), unresolved.size());

        return ShopCoverageBackfillResponse.builder()
                .dryRun(dryRun)
                .candidates(candidates.size())
                .resolvable(resolvable)
                .converted(toSave.size())
                .unresolved(unresolved)
                .build();
    }

    /** Has brands / models text but no make ids yet. */
    static boolean needsConversion(Supplier supplier) {
        boolean hasText = notBlank(supplier.getBrands()) || notBlank(supplier.getModels());
        boolean hasIds = supplier.getMakeIds() != null && !supplier.getMakeIds().isEmpty();
        return hasText && !hasIds;
    }

    private static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }
}
