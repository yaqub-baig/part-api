package com.autoadda.apis.part.service;

import com.autoadda.apis.part.clients.AuthClient;
import com.autoadda.apis.part.clients.identity.SavedAddress;
import com.autoadda.apis.part.entity.DeliveryAddress;
import com.autoadda.apis.part.exception.ResourceNotFoundException;
import com.autoadda.apis.part.exception.UpstreamUnavailableException;
import com.autoadda.apis.part.exception.ValidationException;
import com.autoadda.apis.part.request.CheckoutRequest;
import com.autoadda.apis.part.util.DeliveryMethod;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Turns the checkout body into {@link OrderService.Checkout}: a saved address (addressId, read from identity-api
 * for this customer) or one typed at checkout; for pickup, the pickup contact (or the saved address's name and
 * phone when only addressId is sent).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CheckoutAddressResolver {

    private final AuthClient authClient;

    public OrderService.Checkout resolve(Integer vehicleOwnerId, CheckoutRequest body) {
        if (body == null) {
            return new OrderService.Checkout(null, null, null);
        }
        DeliveryMethod method = body.getDeliveryMethod() == null ? DeliveryMethod.DELIVERY : body.getDeliveryMethod();
        if (method == DeliveryMethod.DELIVERY && body.getAddressId() != null && body.getDeliveryAddress() != null) {
            throw new ValidationException("Send addressId or deliveryAddress, not both");
        }
        if (body.getAddressId() == null) {
            return new OrderService.Checkout(method, body.getDeliveryAddress(), body.getPickupContact());
        }
        if (method == DeliveryMethod.PICKUP && body.getPickupContact() != null) {
            return new OrderService.Checkout(method, null, body.getPickupContact());
        }
        DeliveryAddress saved = toDeliveryAddress(load(vehicleOwnerId, body.getAddressId()));
        return method == DeliveryMethod.PICKUP
                ? new OrderService.Checkout(method, null, saved)
                : new OrderService.Checkout(method, saved, null);
    }

    private SavedAddress load(Integer vehicleOwnerId, Long addressId) {
        try {
            SavedAddress address = authClient.getSavedAddress(addressId, vehicleOwnerId);
            if (address == null || !vehicleOwnerId.equals(address.getUserId())) {
                throw new ResourceNotFoundException("Saved address not found");
            }
            return address;
        } catch (FeignException e) {
            if (e.status() == 404) {
                throw new ResourceNotFoundException("Saved address not found");
            }
            log.warn("identity-api address lookup failed: status {} {}", e.status(), e.getMessage());
            throw new UpstreamUnavailableException("Couldn't load the saved address, try again shortly");
        }
    }

    static DeliveryAddress toDeliveryAddress(SavedAddress a) {
        return DeliveryAddress.builder()
                .contactName(a.getContactName())
                .phone(a.getPhone())
                .addressLine1(a.getAddressLine1())
                .addressLine2(a.getAddressLine2())
                .area(a.getArea())
                .city(a.getCity())
                .emirate(a.getEmirate())
                .mapLink(a.getMapLink())
                .latitude(a.getLatitude())
                .longitude(a.getLongitude())
                .notes(a.getNotes())
                .build();
    }
}
