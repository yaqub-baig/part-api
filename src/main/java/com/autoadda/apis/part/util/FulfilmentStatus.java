package com.autoadda.apis.part.util;

/** Delivery progress of one order line, set by the shop (accept / reject / dispatch) and the buyer (received). */
public enum FulfilmentStatus {
    PENDING, ACCEPTED, REJECTED, DISPATCHED, RECEIVED,
    /** Pickup orders: packed and waiting at the shop (RECEIVED = collected). */
    READY_FOR_PICKUP
}
