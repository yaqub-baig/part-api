package com.autoadda.apis.part.util;

/** Delivery progress of one order line, set by the shop (accept / reject / dispatch) and the buyer (received). */
public enum FulfilmentStatus {
    PENDING, ACCEPTED, REJECTED, DISPATCHED, RECEIVED
}
