package com.autoadda.apis.part.util;

public enum PaymentMethod {
    /** Paid through the payment gateway (card / QR) before the order is confirmed. */
    ONLINE,
    /** Cash on Delivery: order is confirmed immediately and paid when delivered. */
    COD
}
