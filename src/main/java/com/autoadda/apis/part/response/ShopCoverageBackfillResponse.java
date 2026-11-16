package com.autoadda.apis.part.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopCoverageBackfillResponse {

    private boolean dryRun;
    /** Shops with brands / models text and no make ids yet. */
    private int candidates;
    /** Candidates whose text matched makes and models (saved unless dryRun). */
    private int resolvable;
    /** Shops actually updated (0 on a dry run). */
    private int converted;
    /** Candidates left unchanged, with the value that didn't match. */
    private List<Unresolved> unresolved;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Unresolved {
        private Integer supplierId;
        private String supplierName;
        private String field;
        private String value;
        private String reason;
    }
}
