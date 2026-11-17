package com.autoadda.apis.part.service;

import com.autoadda.apis.part.clients.GarageClient;
import com.autoadda.apis.part.clients.garage.GarageApiResponse;
import com.autoadda.apis.part.clients.garage.GarageBuyerInfo;
import com.autoadda.apis.part.clients.garage.GaragePartContext;
import com.autoadda.apis.part.clients.garage.MarketplaceStep;
import com.autoadda.apis.part.entity.GarageSnapshot;
import com.autoadda.apis.part.exception.ConflictException;
import com.autoadda.apis.part.exception.ForbiddenException;
import com.autoadda.apis.part.exception.ResourceNotFoundException;
import com.autoadda.apis.part.exception.UpstreamUnavailableException;
import com.autoadda.apis.part.exception.ValidationException;
import com.autoadda.apis.part.util.CallerIdentity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

/**
 * Who may buy for a garage and what a garage part request item is, answered by garage-api (it owns garages,
 * teams and the PARTS_PURCHASE permission). The user's X-Auto-Adda-Id is forwarded on every call.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GarageBuyerResolver {

    private static final ObjectMapper JSON = new ObjectMapper();

    private final GarageClient garageClient;

    /** The garage as buyer, after garage-api checked the caller holds PARTS_PURCHASE there. */
    public GarageBuyer requirePurchaser(String autoAddaId, Integer garageId) {
        Integer caller = CallerIdentity.callerId(autoAddaId);
        GarageBuyerInfo info = call(() -> garageClient.purchasingAccess(garageId, autoAddaId), "Garage not found");
        if (info == null || !garageId.equals(info.getGarageId())) {
            throw new UpstreamUnavailableException("Garage service returned no garage");
        }
        return new GarageBuyer(garageId, caller, GarageSnapshot.builder()
                .garageName(info.getName())
                .garagePhone(info.getPhone())
                .garageAddress(info.getAddress())
                .garageMapLink(info.getMapLink())
                .build());
    }

    /** A part request item of this garage (404 when it belongs to another garage). */
    public GaragePartContext itemContext(String autoAddaId, Integer garageId, Integer itemId) {
        GaragePartContext context = call(() -> garageClient.purchaseContext(itemId, autoAddaId),
                "Part request item not found");
        if (context == null || !garageId.equals(context.getGarageId())) {
            throw new ResourceNotFoundException("Part request item not found");
        }
        return context;
    }

    /** Tells garage-api about a marketplace order step for a part request item. */
    public GaragePartContext marketplace(String autoAddaId, Integer itemId, MarketplaceStep step) {
        return call(() -> garageClient.marketplace(itemId, step, autoAddaId), "Part request item not found");
    }

    /** The garage, the user acting for it, and the garage's name / address at that moment. */
    public record GarageBuyer(Integer garageId, Integer userId, GarageSnapshot snapshot) {
    }

    private static <T> T call(Supplier<GarageApiResponse<T>> request, String notFound) {
        try {
            GarageApiResponse<T> response = request.get();
            return response == null ? null : response.getBody();
        } catch (FeignException e) {
            String message = message(e);
            switch (e.status()) {
                case 400 -> throw new ValidationException(message != null ? message : "Invalid garage request");
                case 401, 403 -> throw new ForbiddenException("You don't have permission to buy parts for this garage");
                case 404 -> throw new ResourceNotFoundException(notFound);
                case 409 -> throw new ConflictException(message != null ? message : "The garage can't do this now");
                default -> {
                    log.warn("garage-api call failed: status {} {}", e.status(), e.getMessage());
                    throw new UpstreamUnavailableException("Garage service is unavailable, try again shortly");
                }
            }
        }
    }

    /** garage-api error bodies are {status, code, message}. */
    private static String message(FeignException e) {
        try {
            String body = e.contentUTF8();
            if (body == null || body.isBlank()) {
                return null;
            }
            JsonNode node = JSON.readTree(body).get("message");
            return node == null || node.isNull() ? null : node.asText();
        } catch (Exception ignored) {
            return null;
        }
    }
}
