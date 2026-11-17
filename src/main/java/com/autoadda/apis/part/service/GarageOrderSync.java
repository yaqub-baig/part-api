package com.autoadda.apis.part.service;

import com.autoadda.apis.part.clients.garage.MarketplaceStep;
import com.autoadda.apis.part.entity.OrderItem;
import com.autoadda.apis.part.entity.Supplier;
import com.autoadda.apis.part.exception.ConflictException;
import com.autoadda.apis.part.exception.ForbiddenException;
import com.autoadda.apis.part.exception.ResourceNotFoundException;
import com.autoadda.apis.part.exception.ValidationException;
import com.autoadda.apis.part.repository.OrderItemRepository;
import com.autoadda.apis.part.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Keeps the garage's job card in step with a garage order line (ordered / cancelled / received) through garage-api.
 * Never fails the order action: when garage-api is down the step is kept on the line and retried hourly; when
 * garage-api refuses it (e.g. the part was bought elsewhere) the reason is stored on the line instead.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GarageOrderSync {

    private final GarageBuyerResolver garageBuyerResolver;
    private final SupplierRepository supplierRepository;
    private final OrderItemRepository orderItemRepository;

    /** Sends the step for a line that belongs to a garage job; does nothing for other lines. */
    public void send(OrderItem item, MarketplaceStep.Stage stage, Integer userId) {
        if (item.getPartRequestItemId() == null) {
            return;
        }
        Supplier shop = shopOf(item);
        MarketplaceStep step = MarketplaceStep.builder()
                .stage(stage)
                .orderId(item.getOrder().getId())
                .orderItemId(item.getId())
                .supplierName(shop == null ? "AutoAdda shop" : shop.getName())
                .supplierContact(shop == null ? null
                        : shop.getMobileNumber() != null ? shop.getMobileNumber() : shop.getPhoneNumber())
                .unitPrice(item.getUnitPrice())
                .condition(item.getPart().getProductCondition())
                .build();
        try {
            garageBuyerResolver.marketplace(String.valueOf(userId), item.getPartRequestItemId(), step);
            item.setPendingGarageSync(null);
            item.setGarageSyncUserId(null);
            item.setGarageSyncError(null);
        } catch (ConflictException | ForbiddenException | ResourceNotFoundException | ValidationException e) {
            log.warn("garage-api refused {} for order item {}: {}", stage, item.getId(), e.getMessage());
            item.setPendingGarageSync(null);
            item.setGarageSyncUserId(null);
            item.setGarageSyncError(stage + ": " + e.getMessage());
        } catch (RuntimeException e) {
            log.warn("garage-api unavailable for {} on order item {}, will retry: {}", stage, item.getId(), e.getMessage());
            item.setPendingGarageSync(stage);
            item.setGarageSyncUserId(userId);
        }
    }

    /** Hourly: resend steps garage-api missed. Returns how many lines are now in sync. */
    @Transactional
    public int retryPending() {
        List<OrderItem> pending = orderItemRepository.findByPendingGarageSyncIsNotNull();
        int done = 0;
        for (OrderItem item : pending) {
            send(item, item.getPendingGarageSync(), item.getGarageSyncUserId());
            if (item.getPendingGarageSync() == null) {
                done++;
            }
        }
        if (!pending.isEmpty()) {
            orderItemRepository.saveAll(pending);
            log.info("Garage job sync retry: {} of {} lines sent", done, pending.size());
        }
        return done;
    }

    private Supplier shopOf(OrderItem item) {
        try {
            return supplierRepository.findById(Integer.valueOf(item.getPart().getSupplierId())).orElse(null);
        } catch (RuntimeException e) {
            return null;
        }
    }
}
