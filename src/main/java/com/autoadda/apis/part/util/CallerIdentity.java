package com.autoadda.apis.part.util;

import com.autoadda.apis.part.exception.ForbiddenException;

/** The logged-in user from the gateway's X-Auto-Adda-Id header. */
public final class CallerIdentity {

    public static final String HEADER = "X-Auto-Adda-Id";

    private CallerIdentity() {
    }

    /** The caller's user id; 403 when the header isn't a number. */
    public static Integer callerId(String autoAddaId) {
        try {
            return Integer.valueOf(autoAddaId.trim());
        } catch (RuntimeException e) {
            throw new ForbiddenException("Invalid user id");
        }
    }

    /** 403 unless the caller is this customer. Returns the customer id. */
    public static Integer requireSelf(String autoAddaId, Integer vehicleOwnerId) {
        if (!callerId(autoAddaId).equals(vehicleOwnerId)) {
            throw new ForbiddenException("You can only access your own account");
        }
        return vehicleOwnerId;
    }
}
