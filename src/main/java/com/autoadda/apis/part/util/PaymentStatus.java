package com.autoadda.apis.part.util;

public enum PaymentStatus {
    NOT_INITIATED,
    /** ONLINE: waiting for the gateway webhook. COD: waiting for cash at delivery. */
    PENDING,
    SUCCESS,
    /** Last gateway attempt failed or was cancelled; the order can retry payment until it expires. */
    FAILED,
    /** No successful payment before the reservation window closed; order cancelled, stock released. */
    EXPIRED,
    /** Order was cancelled before any payment was received. */
    CANCELLED,
    /** Money was received for an order that is cancelled; needs a manual refund. */
    REFUND_PENDING
}
