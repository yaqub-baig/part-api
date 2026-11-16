package com.autoadda.apis.part.util;

/** OPEN = waiting for quotes, QUOTED = has at least one quote, then ACCEPTED / CANCELLED / EXPIRED */
public enum EnquiryStatus {
    OPEN, QUOTED, ACCEPTED, CANCELLED, EXPIRED
}
