package com.autoadda.apis.part.service;

import com.autoadda.apis.part.clients.garage.GaragePartContext;
import com.autoadda.apis.part.clients.garage.MarketplaceStep;
import com.autoadda.apis.part.entity.Make;
import com.autoadda.apis.part.entity.Model;
import com.autoadda.apis.part.entity.Order;
import com.autoadda.apis.part.entity.OrderItem;
import com.autoadda.apis.part.entity.VehicleSnapshot;
import com.autoadda.apis.part.exception.ConflictException;
import com.autoadda.apis.part.exception.ValidationException;
import com.autoadda.apis.part.repository.MakeRepository;
import com.autoadda.apis.part.repository.ModelRepository;
import com.autoadda.apis.part.repository.OrderRepository;
import com.autoadda.apis.part.request.GarageOrderRequest;
import com.autoadda.apis.part.response.OrderResponse;
import com.autoadda.apis.part.response.PaymentInitiationResponse;
import com.autoadda.apis.part.service.OrderService.OrderBuyer;
import com.autoadda.apis.part.service.OrderService.OrderLine;
import com.autoadda.apis.part.util.BuyerType;
import com.autoadda.apis.part.util.FulfilmentStatus;
import com.autoadda.apis.part.util.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Orders placed in a garage's name. Every call needs PARTS_PURCHASE in the garage (garage-api checks it).
 * Lines for a part request item carry the car and the job, and keep the garage's job card in step.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GarageOrderService {

    public static final int MAX_ITEMS = 50;
    private static final Set<FulfilmentStatus> RECEIVABLE = EnumSet.of(FulfilmentStatus.ACCEPTED, FulfilmentStatus.DISPATCHED);
    private static final Pattern YEAR = Pattern.compile("(\\d{4})");

    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final GarageBuyerResolver garageBuyerResolver;
    private final GarageOrderSync garageOrderSync;
    private final MakeRepository makeRepository;
    private final ModelRepository modelRepository;

    @Transactional
    public OrderResponse place(String autoAddaId, Integer garageId, GarageOrderRequest request) {
        GarageBuyerResolver.GarageBuyer buyer = garageBuyerResolver.requirePurchaser(autoAddaId, garageId);
        List<GarageOrderRequest.Item> items = request.getItems() == null ? List.of() : request.getItems();
        if (items.isEmpty()) {
            throw new ValidationException("Add at least one item");
        }
        if (items.size() > MAX_ITEMS) {
            throw new ValidationException("At most " + MAX_ITEMS + " items per order");
        }
        String notes = request.getNotes() == null || request.getNotes().isBlank() ? null : request.getNotes().trim();
        if (notes != null && notes.length() > 1000) {
            throw new ValidationException("Notes are longer than 1000 characters");
        }

        Set<Integer> jobItems = new HashSet<>();
        List<OrderLine> lines = new ArrayList<>();
        for (GarageOrderRequest.Item item : items) {
            if (item.getInventoryPartId() == null) {
                throw new ValidationException("Each item needs an inventoryPartId");
            }
            GaragePartContext job = null;
            if (item.getPartRequestItemId() != null) {
                if (!jobItems.add(item.getPartRequestItemId())) {
                    throw new ValidationException("Part request item " + item.getPartRequestItemId() + " is in the order twice");
                }
                job = garageBuyerResolver.itemContext(autoAddaId, garageId, item.getPartRequestItemId());
                if (!job.isPurchasable()) {
                    throw new ConflictException("Part request item " + job.getItemId() + " can't be ordered now ("
                            + job.getProcurementStatus() + ", request " + job.getPartRequestStatus()
                            + "); the customer must first agree that the garage buys it");
                }
            }
            int quantity = item.getQuantity() != null ? item.getQuantity()
                    : job != null && job.getQuantity() != null ? job.getQuantity() : 1;
            if (quantity < 1) {
                throw new ValidationException("Quantity must be at least 1");
            }
            VehicleSnapshot vehicle = job != null ? vehicleFromJob(job) : vehicleFromIds(item);
            GaragePartContext forJob = job;
            lines.add(new OrderLine(item.getInventoryPartId(), quantity, line -> {
                line.setVehicle(vehicle);
                if (forJob != null) {
                    line.setWorkOrderId(forJob.getWorkOrderId());
                    line.setJobCardId(forJob.getJobCardId());
                    line.setPartRequestItemId(forJob.getItemId());
                }
            }));
        }

        Order order = orderService.createOrder(new OrderBuyer(BuyerType.GARAGE, null, garageId, buyer.userId(),
                buyer.snapshot(), notes), lines, request.getPaymentMethod());
        PaymentInitiationResponse payment = orderService.startPaymentIfOnline(order);
        order.getItems().forEach(line -> garageOrderSync.send(line, MarketplaceStep.Stage.ORDERED, buyer.userId()));
        orderRepository.save(order);
        log.info("Garage {} order {} placed by user {} ({} lines)", garageId, order.getId(), buyer.userId(), lines.size());

        OrderResponse response = orderService.toResponse(order);
        response.setPayment(payment);
        return response;
    }

    @Transactional
    public List<OrderResponse> list(String autoAddaId, Integer garageId, Status status) {
        garageBuyerResolver.requirePurchaser(autoAddaId, garageId);
        List<Order> orders = status == null
                ? orderRepository.findByGarageIdOrderByOrderDateDesc(garageId)
                : orderRepository.findByGarageIdAndStatusOrderByOrderDateDesc(garageId, status);
        return orders.stream().map(orderService::toResponse).toList();
    }

    @Transactional
    public OrderResponse get(String autoAddaId, Integer garageId, Integer orderId) {
        garageBuyerResolver.requirePurchaser(autoAddaId, garageId);
        return orderService.getOrderFor(orderId, OrderService.garageOwns(garageId));
    }

    @Transactional
    public OrderResponse pay(String autoAddaId, Integer garageId, Integer orderId) {
        garageBuyerResolver.requirePurchaser(autoAddaId, garageId);
        return orderService.retryPaymentFor(orderId, OrderService.garageOwns(garageId));
    }

    /** Cancels before any shop has sent it; job card items go back to "to be procured". */
    @Transactional
    public OrderResponse cancel(String autoAddaId, Integer garageId, Integer orderId) {
        GarageBuyerResolver.GarageBuyer buyer = garageBuyerResolver.requirePurchaser(autoAddaId, garageId);
        Order order = orderService.cancelOrderFor(orderId, OrderService.garageOwns(garageId));
        order.getItems().stream().filter(line -> line.effectiveStatus() != FulfilmentStatus.REJECTED)
                .forEach(line -> garageOrderSync.send(line, MarketplaceStep.Stage.CANCELLED, buyer.userId()));
        return orderService.toResponse(orderRepository.save(order));
    }

    /**
     * The garage received these lines (all accepted / dispatched lines when itemIds is empty). Lines for a job
     * card become a procurement there, which also adds them to the job's invoice.
     */
    @Transactional
    public OrderResponse received(String autoAddaId, Integer garageId, Integer orderId, Collection<Long> itemIds) {
        GarageBuyerResolver.GarageBuyer buyer = garageBuyerResolver.requirePurchaser(autoAddaId, garageId);
        Order order = orderService.lockOrder(orderId);
        OrderService.checkOwner(order, OrderService.garageOwns(garageId));
        if (order.getStatus() == Status.PENDING_PAYMENT || order.getStatus() == Status.CANCELLED) {
            throw new ConflictException("Order " + orderId + " is " + order.getStatus());
        }
        List<OrderItem> chosen = itemIds == null || itemIds.isEmpty()
                ? order.getItems().stream().filter(line -> RECEIVABLE.contains(line.effectiveStatus())).toList()
                : order.getItems().stream().filter(line -> itemIds.contains(line.getId())).toList();
        if (chosen.isEmpty()) {
            throw new ConflictException("Nothing to receive: no line has been accepted or dispatched yet");
        }
        if (itemIds != null && chosen.size() != new HashSet<>(itemIds).size()) {
            throw new ValidationException("Some itemIds are not in order " + orderId);
        }
        LocalDateTime now = LocalDateTime.now();
        for (OrderItem line : chosen) {
            if (!RECEIVABLE.contains(line.effectiveStatus())) {
                throw new ConflictException("Line " + line.getId() + " is " + line.effectiveStatus()
                        + "; only accepted or dispatched lines can be received");
            }
            line.setFulfilmentStatus(FulfilmentStatus.RECEIVED);
            line.setReceivedAt(now);
        }
        OrderService.refreshStatus(order);
        chosen.forEach(line -> garageOrderSync.send(line, MarketplaceStep.Stage.RECEIVED, buyer.userId()));
        return orderService.toResponse(orderRepository.save(order));
    }

    /** The job's car: brand / model names looked up as makes and models, the text kept when they don't match. */
    private VehicleSnapshot vehicleFromJob(GaragePartContext job) {
        if (job.getBrand() == null && job.getModel() == null && job.getYear() == null) {
            return null;
        }
        Make make = job.getBrand() == null ? null : makeRepository.findByName(job.getBrand().trim());
        Model model = null;
        if (make != null && job.getModel() != null) {
            List<Model> found = modelRepository.findByMakeIdAndNameIn(make.getId(), List.of(job.getModel().trim()));
            model = found.size() == 1 ? found.get(0) : null;
        }
        Integer year = null;
        if (job.getYear() != null) {
            Matcher m = YEAR.matcher(job.getYear());
            year = m.find() ? Integer.valueOf(m.group(1)) : null;
        }
        return VehicleSnapshot.builder()
                .makeId(make == null ? null : make.getId())
                .makeName(make == null ? job.getBrand() : make.getName())
                .modelId(model == null ? null : model.getId())
                .modelName(model == null ? job.getModel() : model.getName())
                .year(year)
                .build();
    }

    /** Standalone purchase: optional make / model / year from the dropdowns. */
    private VehicleSnapshot vehicleFromIds(GarageOrderRequest.Item item) {
        if (item.getMakeId() == null && item.getModelId() == null && item.getYear() == null) {
            return null;
        }
        Make make = item.getMakeId() == null ? null : makeRepository.findById(item.getMakeId())
                .orElseThrow(() -> new ValidationException("Unknown make id '" + item.getMakeId() + "'"));
        Model model = item.getModelId() == null ? null : modelRepository.findById(item.getModelId())
                .orElseThrow(() -> new ValidationException("Unknown model id '" + item.getModelId() + "'"));
        if (model != null && (make == null || !Objects.equals(make.getId(), model.getMakeId()))) {
            throw new ValidationException("Model '" + model.getName() + "' does not belong to the selected make");
        }
        Integer year = item.getYear();
        int maxYear = Year.now().getValue() + VehicleFitResolver.YEARS_AHEAD;
        if (year != null && (year < VehicleFitResolver.MIN_YEAR || year > maxYear)) {
            throw new ValidationException("Year " + year + " is outside " + VehicleFitResolver.MIN_YEAR + "-" + maxYear);
        }
        return VehicleSnapshot.builder()
                .makeId(make == null ? null : make.getId())
                .makeName(make == null ? null : make.getName())
                .modelId(model == null ? null : model.getId())
                .modelName(model == null ? null : model.getName())
                .year(year)
                .build();
    }
}
