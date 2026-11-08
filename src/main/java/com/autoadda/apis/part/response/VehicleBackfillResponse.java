package com.autoadda.apis.part.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** Result of converting products' make / model / year text into ids. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleBackfillResponse {

    private boolean dryRun;
    /** Products with make / model / year text and no vehicle ids yet. */
    private int candidates;
    /** Candidates whose text matched makes and models (saved unless dryRun). */
    private int resolvable;
    /** Products actually updated (0 on a dry run). */
    private int converted;
    /** Candidates left unchanged, with the value that didn't match. */
    private List<Unresolved> unresolved;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Unresolved {
        private String productId;
        private String partNumber;
        private String field;
        private String value;
        private String reason;
    }
}
