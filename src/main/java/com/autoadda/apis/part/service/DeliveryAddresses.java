package com.autoadda.apis.part.service;

import com.autoadda.apis.part.entity.DeliveryAddress;
import com.autoadda.apis.part.exception.ValidationException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Checks and cleans a checkout delivery address. */
public final class DeliveryAddresses {

    /** UAE mobile (5XXXXXXXX) or landline (2-9 + 7 digits), with +971 / 00971 / 971 / 0 in front. */
    private static final Pattern UAE_PHONE = Pattern.compile("^(?:\\+971|00971|971|0)(5\\d{8}|[2-9]\\d{7})$");

    private DeliveryAddresses() {
    }

    /** A trimmed copy with the phone as +971...; 400 when something required is missing or invalid. */
    public static DeliveryAddress validated(DeliveryAddress in) {
        if (in == null) {
            throw new ValidationException("deliveryAddress is required");
        }
        DeliveryAddress out = DeliveryAddress.builder()
                .contactName(required(in.getContactName(), "contactName", 100))
                .phone(phone(in.getPhone()))
                .addressLine1(required(in.getAddressLine1(), "addressLine1", 200))
                .addressLine2(optional(in.getAddressLine2(), "addressLine2", 200))
                .area(required(in.getArea(), "area", 100))
                .city(required(in.getCity(), "city", 100))
                .emirate(optional(in.getEmirate(), "emirate", 50))
                .mapLink(optional(in.getMapLink(), "mapLink", 1000))
                .notes(optional(in.getNotes(), "notes", 500))
                .latitude(in.getLatitude())
                .longitude(in.getLongitude())
                .build();
        if ((out.getLatitude() == null) != (out.getLongitude() == null)) {
            throw new ValidationException("deliveryAddress: send both latitude and longitude, or neither");
        }
        if (out.getLatitude() != null && (Math.abs(out.getLatitude()) > 90 || Math.abs(out.getLongitude()) > 180)) {
            throw new ValidationException("deliveryAddress: latitude / longitude out of range");
        }
        return out;
    }

    /** Pickup orders: just who collects (name and phone required, notes optional). */
    public static DeliveryAddress validatedContact(DeliveryAddress in) {
        if (in == null) {
            throw new ValidationException("pickupContact is required for pickup orders");
        }
        return DeliveryAddress.builder()
                .contactName(required(in.getContactName(), "contactName", 100))
                .phone(phone(in.getPhone()))
                .notes(optional(in.getNotes(), "notes", 500))
                .build();
    }

    private static String phone(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new ValidationException("deliveryAddress.phone is required");
        }
        Matcher m = UAE_PHONE.matcher(raw.replaceAll("[\\s()-]", ""));
        if (!m.matches()) {
            throw new ValidationException("deliveryAddress.phone must be a UAE number, e.g. 0501234567 or +971501234567");
        }
        return "+971" + m.group(1);
    }

    private static String required(String value, String field, int max) {
        String v = optional(value, field, max);
        if (v == null) {
            throw new ValidationException("deliveryAddress." + field + " is required");
        }
        return v;
    }

    private static String optional(String value, String field, int max) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String v = value.trim();
        if (v.length() > max) {
            throw new ValidationException("deliveryAddress." + field + " is longer than " + max + " characters");
        }
        return v;
    }
}
