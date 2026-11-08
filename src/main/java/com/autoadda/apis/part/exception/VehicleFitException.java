package com.autoadda.apis.part.exception;

import lombok.Getter;

/**
 * Invalid make / model / year on a product. Returned as 400 (it is a ValidationException);
 * field and value let the vehicle-data backfill report exactly what didn't match.
 */
@Getter
public class VehicleFitException extends ValidationException {

    private final String field;
    private final String value;

    public VehicleFitException(String field, String value, String message) {
        super(message);
        this.field = field;
        this.value = value;
    }
}
