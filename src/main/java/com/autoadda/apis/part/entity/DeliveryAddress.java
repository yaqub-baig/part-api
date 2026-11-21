package com.autoadda.apis.part.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Where a customer order is delivered, as entered at checkout (kept with the order; later changes to the
 * customer's saved addresses don't alter past orders). Also the checkout request body's "deliveryAddress".
 */
@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryAddress {
    @Column(name = "delivery_contact_name", length = 100)
    private String contactName;
    /** Normalised to +971XXXXXXXXX. */
    @Column(name = "delivery_phone", length = 20)
    private String phone;
    @Column(name = "delivery_address_line1", length = 200)
    private String addressLine1;
    @Column(name = "delivery_address_line2", length = 200)
    private String addressLine2;
    @Column(name = "delivery_area", length = 100)
    private String area;
    @Column(name = "delivery_city", length = 100)
    private String city;
    /** e.g. Dubai, Abu Dhabi, Sharjah. */
    @Column(name = "delivery_emirate", length = 50)
    private String emirate;
    @Column(name = "delivery_map_link", length = 1000)
    private String mapLink;
    @Column(name = "delivery_latitude")
    private Double latitude;
    @Column(name = "delivery_longitude")
    private Double longitude;
    /** e.g. "Villa 12, call on arrival". */
    @Column(name = "delivery_notes", length = 500)
    private String notes;
}
