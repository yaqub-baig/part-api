package com.autoadda.apis.part.service;

import com.autoadda.apis.part.entity.Category;
import com.autoadda.apis.part.entity.EnquiryRecipient;
import com.autoadda.apis.part.entity.InventoryPart;
import com.autoadda.apis.part.entity.Make;
import com.autoadda.apis.part.entity.MediaFile;
import com.autoadda.apis.part.entity.Model;
import com.autoadda.apis.part.entity.PartEnquiry;
import com.autoadda.apis.part.entity.PartQuote;
import com.autoadda.apis.part.entity.Product;
import com.autoadda.apis.part.entity.Supplier;
import com.autoadda.apis.part.exception.ConflictException;
import com.autoadda.apis.part.exception.ForbiddenException;
import com.autoadda.apis.part.exception.ResourceNotFoundException;
import com.autoadda.apis.part.exception.ValidationException;
import com.autoadda.apis.part.repository.CategoryRepository;
import com.autoadda.apis.part.repository.EnquiryRecipientRepository;
import com.autoadda.apis.part.repository.InventoryPartRepository;
import com.autoadda.apis.part.repository.MakeRepository;
import com.autoadda.apis.part.repository.MediaFileRepository;
import com.autoadda.apis.part.repository.ModelRepository;
import com.autoadda.apis.part.repository.PartEnquiryRepository;
import com.autoadda.apis.part.repository.PartQuoteRepository;
import com.autoadda.apis.part.repository.ProductRepository;
import com.autoadda.apis.part.repository.SupplierRepository;
import com.autoadda.apis.part.request.PartEnquiryRequest;
import com.autoadda.apis.part.request.PartQuoteRequest;
import com.autoadda.apis.part.response.AcceptQuoteResponse;
import com.autoadda.apis.part.response.PartEnquiryResponse;
import com.autoadda.apis.part.response.PartQuoteResponse;
import com.autoadda.apis.part.specification.ShopMatchSpecification;
import com.autoadda.apis.part.clients.garage.GaragePartContext;
import com.autoadda.apis.part.util.BuyerType;
import com.autoadda.apis.part.util.CallerIdentity;
import com.autoadda.apis.part.util.EnquiryRecipientStatus;
import com.autoadda.apis.part.util.EnquiryStatus;
import com.autoadda.apis.part.util.ProductCondition;
import com.autoadda.apis.part.util.QuoteStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Part enquiries: a customer asks for a part, the shops covering that vehicle / category / condition get it,
 * they quote, the customer accepts one quote and the accepted quote becomes a normal listing for the cart.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PartEnquiryService {

    public static final int MAX_RECIPIENTS = 20;
    public static final int ENQUIRY_OPEN_HOURS = 48;
    public static final int QUOTE_VALID_HOURS = 72;
    public static final int MAX_OPEN_ENQUIRIES = 5;
    /** Garages work on many cars at once. */
    public static final int MAX_OPEN_GARAGE_ENQUIRIES = 20;
    public static final int MAX_IMAGES = 5;
    public static final int MAX_DESCRIPTION = 2000;
    private static final Set<EnquiryStatus> ACTIVE = EnumSet.of(EnquiryStatus.OPEN, EnquiryStatus.QUOTED);
    private static final String ENQUIRY_NOT_FOUND = "Enquiry not found";

    private final PartEnquiryRepository enquiryRepository;
    private final EnquiryRecipientRepository recipientRepository;
    private final PartQuoteRepository quoteRepository;
    private final SupplierRepository supplierRepository;
    private final MakeRepository makeRepository;
    private final ModelRepository modelRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final MediaFileRepository mediaFileRepository;
    private final InventoryPartRepository inventoryPartRepository;
    private final CategoryHierarchy categoryHierarchy;
    private final GarageBuyerResolver garageBuyerResolver;

    // ================================================================ matching

    /** How many shops an enquiry with these filters would reach (capped at MAX_RECIPIENTS when sent). */
    public long countMatchingShops(String makeId, String modelId, Integer year, String categoryId,
                                   Collection<ProductCondition> conditions) {
        if (isBlank(makeId)) {
            throw new ValidationException("makeId is required");
        }
        return supplierRepository.count(matching(makeId.trim(), modelId, year, categoryId, conditions));
    }

    private Specification<Supplier> matching(String makeId, String modelId, Integer year, String categoryId,
                                             Collection<ProductCondition> conditions) {
        Set<String> categories = isBlank(categoryId) ? Set.of() : categoryHierarchy.withAncestors(categoryId);
        return ShopMatchSpecification.covering(makeId, trimToNull(modelId), categories, conditions, year);
    }

    // ================================================================ customer

    @Transactional
    public PartEnquiryResponse create(String autoAddaId, Integer vehicleOwnerId, PartEnquiryRequest request) {
        ownCustomer(autoAddaId, vehicleOwnerId);
        if (request.getPartRequestItemId() != null) {
            throw new ValidationException("partRequestItemId is only for garage enquiries");
        }
        PartEnquiry enquiry = validateNewEnquiry(request);
        long open = enquiryRepository.countByVehicleOwnerIdAndStatusIn(vehicleOwnerId, ACTIVE);
        if (open >= MAX_OPEN_ENQUIRIES) {
            throw new ConflictException("You already have " + open + " open enquiries; close one before sending another");
        }
        enquiry.setBuyerType(BuyerType.CUSTOMER);
        enquiry.setVehicleOwnerId(vehicleOwnerId);
        return send(enquiry);
    }

    /**
     * A garage's enquiry, in the garage's name. The caller needs PARTS_PURCHASE in the garage (checked by garage-api).
     * With partRequestItemId the car, part number and description come from the garage's job when not sent.
     */
    @Transactional
    public PartEnquiryResponse createForGarage(String autoAddaId, Integer garageId, PartEnquiryRequest request) {
        GarageBuyerResolver.GarageBuyer buyer = garageBuyerResolver.requirePurchaser(autoAddaId, garageId);
        GaragePartContext job = request.getPartRequestItemId() == null ? null
                : garageBuyerResolver.itemContext(autoAddaId, garageId, request.getPartRequestItemId());
        if (job != null) {
            fillFromJob(request, job);
        }
        PartEnquiry enquiry = validateNewEnquiry(request);
        long open = enquiryRepository.countByGarageIdAndStatusIn(garageId, ACTIVE);
        if (open >= MAX_OPEN_GARAGE_ENQUIRIES) {
            throw new ConflictException("The garage already has " + open + " open enquiries; close one before sending another");
        }
        enquiry.setBuyerType(BuyerType.GARAGE);
        enquiry.setGarageId(garageId);
        enquiry.setPlacedByUserId(buyer.userId());
        enquiry.setGarage(buyer.snapshot());
        if (job != null) {
            enquiry.setWorkOrderId(job.getWorkOrderId());
            enquiry.setJobCardId(job.getJobCardId());
            enquiry.setPartRequestItemId(job.getItemId());
        }
        return send(enquiry);
    }

    /** Fills what the garage didn't send from its part request item: vehicle (names looked up), part number, text. */
    private void fillFromJob(PartEnquiryRequest request, GaragePartContext job) {
        if (isBlank(request.getMakeId()) && !isBlank(job.getBrand())) {
            Make make = makeRepository.findByName(job.getBrand().trim());
            if (make == null) {
                throw new ValidationException("The car's brand '" + job.getBrand().trim()
                        + "' isn't in the make list; choose the make");
            }
            request.setMakeId(make.getId());
            if (isBlank(request.getModelId()) && !isBlank(job.getModel())) {
                List<Model> models = modelRepository.findByMakeIdAndNameIn(make.getId(), List.of(job.getModel().trim()));
                if (models.size() == 1) {
                    request.setModelId(models.get(0).getId());
                }
            }
        }
        if (request.getYear() == null && job.getYear() != null) {
            java.util.regex.Matcher year = java.util.regex.Pattern.compile("(\\d{4})").matcher(job.getYear());
            if (year.find()) {
                request.setYear(Integer.valueOf(year.group(1)));
            }
        }
        if (isBlank(request.getPartNumber()) && !isBlank(job.getPartNumber())) {
            request.setPartNumber(job.getPartNumber());
        }
        if (isBlank(request.getDescription()) && !isBlank(job.getPartName())) {
            request.setDescription(job.getPartName() + (job.getQuantity() == null ? "" : " x" + job.getQuantity())
                    + (isBlank(job.getDescription()) ? "" : " - " + job.getDescription()));
        }
    }

    /** Routes a validated enquiry to the matching shops and saves it. */
    private PartEnquiryResponse send(PartEnquiry enquiry) {

        List<Supplier> shops = supplierRepository.findAll(
                matching(enquiry.getMakeId(), enquiry.getModelId(), enquiry.getYear(), enquiry.getCategoryId(),
                        enquiry.getConditions()),
                PageRequest.of(0, MAX_RECIPIENTS, Sort.by("id"))).getContent();
        if (shops.isEmpty()) {
            throw new ConflictException("No shop covers this vehicle and part yet");
        }

        LocalDateTime now = LocalDateTime.now();
        enquiry.setStatus(EnquiryStatus.OPEN);
        enquiry.setRecipientCount(shops.size());
        enquiry.setCreatedAt(now);
        enquiry.setExpiresAt(now.plusHours(ENQUIRY_OPEN_HOURS));
        PartEnquiry saved = enquiryRepository.save(enquiry);

        recipientRepository.saveAll(shops.stream()
                .map(shop -> EnquiryRecipient.builder()
                        .enquiryId(saved.getId())
                        .supplierId(shop.getId())
                        .status(EnquiryRecipientStatus.NEW)
                        .createdAt(now)
                        .build())
                .toList());
        log.info("Enquiry {} from {} {} sent to {} shops", saved.getId(), saved.getBuyerType(),
                saved.getBuyerType() == BuyerType.GARAGE ? saved.getGarageId() : saved.getVehicleOwnerId(), shops.size());
        return customerView(saved, List.of());
    }

    private PartEnquiry validateNewEnquiry(PartEnquiryRequest request) {
        if (isBlank(request.getMakeId())) {
            throw new ValidationException("makeId is required");
        }
        String makeId = request.getMakeId().trim();
        if (!makeRepository.existsById(makeId)) {
            throw new ValidationException("Unknown make id '" + makeId + "'");
        }
        String modelId = trimToNull(request.getModelId());
        if (modelId != null) {
            Model model = modelRepository.findById(modelId)
                    .orElseThrow(() -> new ValidationException("Unknown model id '" + modelId + "'"));
            if (!makeId.equals(model.getMakeId())) {
                throw new ValidationException("Model '" + model.getName() + "' does not belong to the selected make");
            }
        }
        Integer year = request.getYear();
        int maxYear = Year.now().getValue() + VehicleFitResolver.YEARS_AHEAD;
        if (year != null && (year < VehicleFitResolver.MIN_YEAR || year > maxYear)) {
            throw new ValidationException("Year " + year + " is outside " + VehicleFitResolver.MIN_YEAR + "-" + maxYear);
        }
        String categoryId = trimToNull(request.getCategoryId());
        if (categoryId != null && !categoryRepository.existsById(categoryId)) {
            throw new ValidationException("Unknown category id '" + categoryId + "'");
        }
        String productId = trimToNull(request.getProductId());
        if (productId != null && !productRepository.existsById(productId)) {
            throw new ValidationException("Unknown product id '" + productId + "'");
        }
        String partNumber = trimToNull(request.getPartNumber());
        String description = trimToNull(request.getDescription());
        if (productId == null && partNumber == null && description == null) {
            throw new ValidationException("Describe the part: send productId, partNumber or description");
        }
        if (description != null && description.length() > MAX_DESCRIPTION) {
            throw new ValidationException("Description is longer than " + MAX_DESCRIPTION + " characters");
        }
        Set<ProductCondition> conditions = conditionSet(request.getConditions());
        Set<Long> imageIds = validImageIds(request.getImageIds());

        return PartEnquiry.builder()
                .makeId(makeId)
                .modelId(modelId)
                .year(year)
                .categoryId(categoryId)
                .productId(productId)
                .partNumber(partNumber)
                .description(description)
                .conditions(new HashSet<>(conditions))
                .imageIds(new HashSet<>(imageIds))
                .build();
    }

    private Set<Long> validImageIds(List<Long> raw) {
        if (raw == null || raw.isEmpty()) {
            return Set.of();
        }
        Set<Long> ids = raw.stream().filter(Objects::nonNull).collect(Collectors.toCollection(LinkedHashSet::new));
        if (ids.size() > MAX_IMAGES) {
            throw new ValidationException("At most " + MAX_IMAGES + " images per enquiry");
        }
        Set<Long> found = mediaFileRepository.findAllById(ids).stream().map(MediaFile::getId).collect(Collectors.toSet());
        for (Long id : ids) {
            if (!found.contains(id)) {
                throw new ValidationException("Unknown image id " + id);
            }
        }
        return ids;
    }

    @Transactional
    public List<PartEnquiryResponse> listForOwner(String autoAddaId, Integer vehicleOwnerId, EnquiryStatus status) {
        ownCustomer(autoAddaId, vehicleOwnerId);
        return buyerViews(status == null
                ? enquiryRepository.findByVehicleOwnerIdOrderByCreatedAtDesc(vehicleOwnerId)
                : enquiryRepository.findByVehicleOwnerIdAndStatusOrderByCreatedAtDesc(vehicleOwnerId, status));
    }

    @Transactional
    public List<PartEnquiryResponse> listForGarage(String autoAddaId, Integer garageId, EnquiryStatus status) {
        garageBuyerResolver.requirePurchaser(autoAddaId, garageId);
        return buyerViews(status == null
                ? enquiryRepository.findByGarageIdOrderByCreatedAtDesc(garageId)
                : enquiryRepository.findByGarageIdAndStatusOrderByCreatedAtDesc(garageId, status));
    }

    private List<PartEnquiryResponse> buyerViews(List<PartEnquiry> enquiries) {
        Map<Integer, List<PartQuote>> quotes = quotesByEnquiry(enquiries);
        Lookups lookups = lookups(enquiries, quotes.values().stream().flatMap(List::stream).toList());
        return enquiries.stream()
                .map(e -> customerView(e, quotes.getOrDefault(e.getId(), List.of()), lookups))
                .toList();
    }

    @Transactional
    public PartEnquiryResponse getForOwner(String autoAddaId, Integer vehicleOwnerId, Integer enquiryId) {
        ownCustomer(autoAddaId, vehicleOwnerId);
        PartEnquiry enquiry = ownedEnquiry(customerOwns(vehicleOwnerId), enquiryId);
        return customerView(enquiry, quoteRepository.findByEnquiryIdOrderByUnitPriceAsc(enquiryId));
    }

    @Transactional
    public PartEnquiryResponse getForGarage(String autoAddaId, Integer garageId, Integer enquiryId) {
        garageBuyerResolver.requirePurchaser(autoAddaId, garageId);
        PartEnquiry enquiry = ownedEnquiry(garageOwns(garageId), enquiryId);
        return customerView(enquiry, quoteRepository.findByEnquiryIdOrderByUnitPriceAsc(enquiryId));
    }

    @Transactional
    public PartEnquiryResponse cancel(String autoAddaId, Integer vehicleOwnerId, Integer enquiryId) {
        ownCustomer(autoAddaId, vehicleOwnerId);
        return cancel(ownedEnquiry(customerOwns(vehicleOwnerId), enquiryId));
    }

    @Transactional
    public PartEnquiryResponse cancelForGarage(String autoAddaId, Integer garageId, Integer enquiryId) {
        garageBuyerResolver.requirePurchaser(autoAddaId, garageId);
        return cancel(ownedEnquiry(garageOwns(garageId), enquiryId));
    }

    private PartEnquiryResponse cancel(PartEnquiry enquiry) {
        Integer enquiryId = enquiry.getId();
        if (!ACTIVE.contains(enquiry.getStatus())) {
            throw new ConflictException("Enquiry is already " + enquiry.getStatus());
        }
        List<PartQuote> quotes = quoteRepository.findByEnquiryIdOrderByUnitPriceAsc(enquiryId);
        close(enquiry, EnquiryStatus.CANCELLED, quotes);
        return customerView(enquiry, quotes);
    }

    /**
     * Accept one quote: creates an active listing for the shop at the quoted price, condition and quantity,
     * rejects the other quotes and closes the enquiry. The returned inventoryPartId goes into the cart.
     */
    @Transactional
    public AcceptQuoteResponse accept(String autoAddaId, Integer vehicleOwnerId, Integer enquiryId, Integer quoteId) {
        ownCustomer(autoAddaId, vehicleOwnerId);
        return accept(customerOwns(vehicleOwnerId), enquiryId, quoteId);
    }

    /** A garage accepts a quote; the returned inventoryPartId goes into a garage order. */
    @Transactional
    public AcceptQuoteResponse acceptForGarage(String autoAddaId, Integer garageId, Integer enquiryId, Integer quoteId) {
        garageBuyerResolver.requirePurchaser(autoAddaId, garageId);
        return accept(garageOwns(garageId), enquiryId, quoteId);
    }

    private AcceptQuoteResponse accept(java.util.function.Predicate<PartEnquiry> owns, Integer enquiryId, Integer quoteId) {
        PartEnquiry enquiry = enquiryRepository.findByIdForUpdate(enquiryId)
                .filter(owns)
                .orElseThrow(() -> new ResourceNotFoundException(ENQUIRY_NOT_FOUND));
        if (enquiry.getStatus() != EnquiryStatus.QUOTED) {
            throw new ConflictException(enquiry.getStatus() == EnquiryStatus.OPEN
                    ? "This enquiry has no quotes yet" : "Enquiry is already " + enquiry.getStatus());
        }
        List<PartQuote> quotes = quoteRepository.findByEnquiryIdOrderByUnitPriceAsc(enquiryId);
        PartQuote quote = quotes.stream().filter(q -> q.getId().equals(quoteId)).findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Quote not found"));
        LocalDateTime now = LocalDateTime.now();
        if (quote.getStatus() != QuoteStatus.SENT || !quote.getValidUntil().isAfter(now)) {
            throw new ConflictException("This quote is no longer available");
        }
        Supplier shop = supplierRepository.findById(quote.getSupplierId())
                .filter(Supplier::isActive)
                .orElseThrow(() -> new ConflictException("This shop is no longer active"));
        Product product = productRepository.findById(quote.getProductId())
                .orElseThrow(() -> new ConflictException("The quoted product no longer exists"));

        InventoryPart listing = inventoryPartRepository.save(InventoryPart.builder()
                .productId(product.getId())
                .supplierId(String.valueOf(shop.getId()))
                .partName(product.getName())
                .partNumber(product.getPartNumber())
                .category(product.getCategoryId())
                .manufacturer(product.getManufacturerId())
                .productCondition(quote.getCondition())
                .quantityInStock(quote.getQuantity())
                .unitPrice(quote.getUnitPrice())
                .isActive(true)
                .build());

        quote.setStatus(QuoteStatus.ACCEPTED);
        quote.setInventoryPartId(listing.getId());
        quote.setUpdatedAt(now);
        enquiry.setAcceptedQuoteId(quote.getId());
        close(enquiry, EnquiryStatus.ACCEPTED, quotes);
        log.info("Enquiry {}: quote {} from shop {} accepted, listing {} created",
                enquiryId, quoteId, shop.getId(), listing.getId());

        return AcceptQuoteResponse.builder()
                .enquiryId(enquiryId)
                .quoteId(quote.getId())
                .inventoryPartId(listing.getId())
                .supplierId(shop.getId())
                .productId(product.getId())
                .unitPrice(quote.getUnitPrice())
                .quantity(quote.getQuantity())
                .condition(quote.getCondition())
                .build();
    }

    /** Someone else's enquiry answers 404. */
    private PartEnquiry ownedEnquiry(java.util.function.Predicate<PartEnquiry> owns, Integer enquiryId) {
        return enquiryRepository.findById(enquiryId)
                .filter(owns)
                .orElseThrow(() -> new ResourceNotFoundException(ENQUIRY_NOT_FOUND));
    }

    private static java.util.function.Predicate<PartEnquiry> customerOwns(Integer vehicleOwnerId) {
        return e -> e.getBuyerType() != BuyerType.GARAGE && vehicleOwnerId.equals(e.getVehicleOwnerId());
    }

    private static java.util.function.Predicate<PartEnquiry> garageOwns(Integer garageId) {
        return e -> e.getBuyerType() == BuyerType.GARAGE && garageId.equals(e.getGarageId());
    }

    /** Close the enquiry; quotes still SENT become REJECTED. */
    private void close(PartEnquiry enquiry, EnquiryStatus status, List<PartQuote> quotes) {
        LocalDateTime now = LocalDateTime.now();
        enquiry.setStatus(status);
        enquiry.setClosedAt(now);
        enquiryRepository.save(enquiry);
        quotes.stream().filter(q -> q.getStatus() == QuoteStatus.SENT).forEach(q -> {
            q.setStatus(QuoteStatus.REJECTED);
            q.setUpdatedAt(now);
        });
        if (!quotes.isEmpty()) {
            quoteRepository.saveAll(quotes);
        }
    }

    // ================================================================ shop

    @Transactional
    public List<PartEnquiryResponse> listForShop(String autoAddaId, Integer supplierId, EnquiryRecipientStatus status) {
        return listForShop(autoAddaId, supplierId, status, null);
    }

    /** A shop's enquiries; buyerType (CUSTOMER / GARAGE) shows only those, null shows all. */
    @Transactional
    public List<PartEnquiryResponse> listForShop(String autoAddaId, Integer supplierId, EnquiryRecipientStatus status,
                                                 BuyerType buyerType) {
        ownShop(autoAddaId, supplierId);
        List<EnquiryRecipient> received = status == null
                ? recipientRepository.findBySupplierIdOrderByCreatedAtDesc(supplierId)
                : recipientRepository.findBySupplierIdAndStatusOrderByCreatedAtDesc(supplierId, status);
        Map<Integer, PartEnquiry> enquiries = enquiryRepository
                .findAllById(received.stream().map(EnquiryRecipient::getEnquiryId).toList()).stream()
                .collect(Collectors.toMap(PartEnquiry::getId, Function.identity()));
        Map<Integer, PartQuote> myQuotes = quoteRepository.findByEnquiryIdIn(enquiries.keySet()).stream()
                .filter(q -> q.getSupplierId().equals(supplierId))
                .collect(Collectors.toMap(PartQuote::getEnquiryId, Function.identity()));
        Lookups lookups = lookups(enquiries.values(), myQuotes.values());
        return received.stream()
                .filter(r -> enquiries.containsKey(r.getEnquiryId()))
                .filter(r -> buyerType == null || buyerTypeOf(enquiries.get(r.getEnquiryId())) == buyerType)
                .map(r -> shopView(enquiries.get(r.getEnquiryId()), r, myQuotes.get(r.getEnquiryId()), lookups))
                .toList();
    }

    /** One enquiry for a shop; marks it VIEWED the first time. */
    @Transactional
    public PartEnquiryResponse getForShop(String autoAddaId, Integer supplierId, Integer enquiryId) {
        ownShop(autoAddaId, supplierId);
        EnquiryRecipient recipient = recipient(enquiryId, supplierId);
        if (recipient.getStatus() == EnquiryRecipientStatus.NEW) {
            recipient.setStatus(EnquiryRecipientStatus.VIEWED);
            recipient.setUpdatedAt(LocalDateTime.now());
            recipientRepository.save(recipient);
        }
        PartEnquiry enquiry = enquiryRepository.findById(enquiryId)
                .orElseThrow(() -> new ResourceNotFoundException(ENQUIRY_NOT_FOUND));
        PartQuote myQuote = quoteRepository.findByEnquiryIdAndSupplierId(enquiryId, supplierId).orElse(null);
        return shopView(enquiry, recipient, myQuote, lookups(List.of(enquiry), myQuote == null ? List.of() : List.of(myQuote)));
    }

    /** Send or replace this shop's quote. Allowed while the enquiry is open (before expiresAt). */
    @Transactional
    public PartEnquiryResponse quote(String autoAddaId, Integer supplierId, Integer enquiryId, PartQuoteRequest request) {
        ownShop(autoAddaId, supplierId);
        EnquiryRecipient recipient = recipient(enquiryId, supplierId);
        PartEnquiry enquiry = enquiryRepository.findByIdForUpdate(enquiryId)
                .orElseThrow(() -> new ResourceNotFoundException(ENQUIRY_NOT_FOUND));
        LocalDateTime now = LocalDateTime.now();
        if (!ACTIVE.contains(enquiry.getStatus()) || !enquiry.getExpiresAt().isAfter(now)) {
            throw new ConflictException("This enquiry is no longer open for quotes");
        }
        validateQuote(enquiry, request);

        PartQuote quote = quoteRepository.findByEnquiryIdAndSupplierId(enquiryId, supplierId).orElse(null);
        if (quote == null) {
            quote = PartQuote.builder().enquiryId(enquiryId).supplierId(supplierId).createdAt(now).build();
        } else {
            quote.setUpdatedAt(now);
        }
        quote.setProductId(request.getProductId().trim());
        quote.setUnitPrice(request.getUnitPrice());
        quote.setCondition(request.getCondition());
        quote.setQuantity(request.getQuantity());
        quote.setNotes(trimToNull(request.getNotes()));
        quote.setValidUntil(now.plusHours(QUOTE_VALID_HOURS));
        quote.setStatus(QuoteStatus.SENT);
        PartQuote saved = quoteRepository.save(quote);

        recipient.setStatus(EnquiryRecipientStatus.QUOTED);
        recipient.setUpdatedAt(now);
        recipientRepository.save(recipient);
        if (enquiry.getStatus() == EnquiryStatus.OPEN) {
            enquiry.setStatus(EnquiryStatus.QUOTED);
            enquiryRepository.save(enquiry);
        }
        log.info("Shop {} quoted {} for enquiry {}", supplierId, request.getUnitPrice(), enquiryId);
        return shopView(enquiry, recipient, saved, lookups(List.of(enquiry), List.of(saved)));
    }

    private void validateQuote(PartEnquiry enquiry, PartQuoteRequest request) {
        if (isBlank(request.getProductId())) {
            throw new ValidationException("productId is required; add the part to the catalogue first if it isn't there");
        }
        if (!productRepository.existsById(request.getProductId().trim())) {
            throw new ValidationException("Unknown product id '" + request.getProductId().trim() + "'");
        }
        if (request.getUnitPrice() == null || request.getUnitPrice() <= 0) {
            throw new ValidationException("unitPrice must be greater than 0");
        }
        if (request.getQuantity() == null || request.getQuantity() < 1) {
            throw new ValidationException("quantity must be at least 1");
        }
        if (request.getCondition() == null) {
            throw new ValidationException("condition is required");
        }
        if (!enquiry.getConditions().isEmpty() && !enquiry.getConditions().contains(request.getCondition())) {
            throw new ValidationException("The customer asked for " + enquiry.getConditions().stream()
                    .map(Enum::name).sorted().collect(Collectors.joining(", ")) + " parts");
        }
        if (request.getNotes() != null && request.getNotes().length() > 1000) {
            throw new ValidationException("Notes are longer than 1000 characters");
        }
    }

    @Transactional
    public PartEnquiryResponse decline(String autoAddaId, Integer supplierId, Integer enquiryId) {
        ownShop(autoAddaId, supplierId);
        EnquiryRecipient recipient = recipient(enquiryId, supplierId);
        PartQuote myQuote = quoteRepository.findByEnquiryIdAndSupplierId(enquiryId, supplierId).orElse(null);
        if (myQuote != null) {
            throw new ConflictException("You already quoted for this enquiry");
        }
        recipient.setStatus(EnquiryRecipientStatus.DECLINED);
        recipient.setUpdatedAt(LocalDateTime.now());
        recipientRepository.save(recipient);
        PartEnquiry enquiry = enquiryRepository.findById(enquiryId)
                .orElseThrow(() -> new ResourceNotFoundException(ENQUIRY_NOT_FOUND));
        return shopView(enquiry, recipient, null, lookups(List.of(enquiry), List.of()));
    }

    /** The logged-in user (X-Auto-Adda-Id) must be the customer in the path. */
    private static void ownCustomer(String autoAddaId, Integer vehicleOwnerId) {
        CallerIdentity.requireSelf(autoAddaId, vehicleOwnerId);
    }

    private void ownShop(String autoAddaId, Integer supplierId) {
        Supplier shop = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"));
        if (!CallerIdentity.callerId(autoAddaId).equals(shop.getSparePartShopOwner())) {
            throw new ForbiddenException("You are not the owner of this shop");
        }
    }

    private EnquiryRecipient recipient(Integer enquiryId, Integer supplierId) {
        return recipientRepository.findByEnquiryIdAndSupplierId(enquiryId, supplierId)
                .orElseThrow(() -> new ResourceNotFoundException(ENQUIRY_NOT_FOUND));
    }

    // ================================================================ expiry

    /**
     * OPEN enquiries past expiresAt expire. QUOTED ones stay acceptable until their last quote's validUntil,
     * then expire. Returns how many were expired.
     */
    @Transactional
    public int expireEnquiries() {
        LocalDateTime now = LocalDateTime.now();
        int expired = 0;
        for (PartEnquiry enquiry : enquiryRepository.findByStatusInAndExpiresAtBefore(ACTIVE, now)) {
            boolean quoteStillValid = enquiry.getStatus() == EnquiryStatus.QUOTED
                    && quoteRepository.existsByEnquiryIdAndStatusAndValidUntilAfter(enquiry.getId(), QuoteStatus.SENT, now);
            if (!quoteStillValid) {
                close(enquiry, EnquiryStatus.EXPIRED, quoteRepository.findByEnquiryIdOrderByUnitPriceAsc(enquiry.getId()));
                expired++;
            }
        }
        if (expired > 0) {
            log.info("Expired {} part enquiries", expired);
        }
        return expired;
    }

    // ================================================================ views

    /** Names and image paths for a batch of enquiries and quotes, loaded with one query per table. */
    private record Lookups(Map<String, String> makes, Map<String, String> models, Map<String, String> categories,
                           Map<String, String> products, Map<Integer, String> shops, Map<Long, String> images) {
    }

    private Lookups lookups(Collection<PartEnquiry> enquiries, Collection<PartQuote> quotes) {
        Set<String> makeIds = ids(enquiries, PartEnquiry::getMakeId);
        Set<String> modelIds = ids(enquiries, PartEnquiry::getModelId);
        Set<String> categoryIds = ids(enquiries, PartEnquiry::getCategoryId);
        Set<String> productIds = ids(quotes, PartQuote::getProductId);
        Set<Integer> shopIds = ids(quotes, PartQuote::getSupplierId);
        Set<Long> imageIds = enquiries.stream().flatMap(e -> e.getImageIds().stream()).collect(Collectors.toSet());
        return new Lookups(
                makeIds.isEmpty() ? Map.of() : names(makeRepository.findAllById(makeIds), Make::getId, Make::getName),
                modelIds.isEmpty() ? Map.of() : names(modelRepository.findAllById(modelIds), Model::getId, Model::getName),
                categoryIds.isEmpty() ? Map.of() : names(categoryRepository.findAllById(categoryIds), Category::getId, Category::getName),
                productIds.isEmpty() ? Map.of() : names(productRepository.findAllById(productIds), Product::getId, Product::getName),
                shopIds.isEmpty() ? Map.of() : names(supplierRepository.findAllById(shopIds), Supplier::getId, Supplier::getName),
                imageIds.isEmpty() ? Map.of() : names(mediaFileRepository.findAllById(imageIds), MediaFile::getId, MediaFile::getFilePath));
    }

    private PartEnquiryResponse customerView(PartEnquiry enquiry, List<PartQuote> quotes) {
        return customerView(enquiry, quotes, lookups(List.of(enquiry), quotes));
    }

    private PartEnquiryResponse customerView(PartEnquiry enquiry, List<PartQuote> quotes, Lookups lookups) {
        PartEnquiryResponse response = base(enquiry, lookups);
        response.setQuotes(quotes.stream()
                .sorted(Comparator.comparing(PartQuote::getUnitPrice))
                .map(q -> quoteView(q, lookups))
                .toList());
        response.setAcceptedQuoteId(enquiry.getAcceptedQuoteId());
        withGarageContact(response, enquiry);
        return response;
    }

    private static void withGarageContact(PartEnquiryResponse response, PartEnquiry enquiry) {
        if (enquiry.getGarage() != null) {
            response.setGaragePhone(enquiry.getGarage().getGaragePhone());
            response.setGarageAddress(enquiry.getGarage().getGarageAddress());
            response.setGarageMapLink(enquiry.getGarage().getGarageMapLink());
        }
    }

    private static BuyerType buyerTypeOf(PartEnquiry enquiry) {
        return enquiry.getBuyerType() == null ? BuyerType.CUSTOMER : enquiry.getBuyerType();
    }

    private PartEnquiryResponse shopView(PartEnquiry enquiry, EnquiryRecipient recipient, PartQuote myQuote, Lookups lookups) {
        PartEnquiryResponse response = base(enquiry, lookups);
        response.setMyStatus(recipient.getStatus());
        response.setMyQuote(myQuote == null ? null : quoteView(myQuote, lookups));
        if (myQuote != null && myQuote.getStatus() == QuoteStatus.ACCEPTED) {
            // contact shared only with the chosen shop
            response.setVehicleOwnerId(enquiry.getVehicleOwnerId());
            withGarageContact(response, enquiry);
        }
        return response;
    }

    private static PartEnquiryResponse base(PartEnquiry e, Lookups lookups) {
        return PartEnquiryResponse.builder()
                .id(e.getId())
                .status(e.getStatus())
                .buyerType(buyerTypeOf(e))
                .garageId(e.getGarageId())
                .garageName(e.getGarage() == null ? null : e.getGarage().getGarageName())
                .workOrderId(e.getWorkOrderId())
                .jobCardId(e.getJobCardId())
                .partRequestItemId(e.getPartRequestItemId())
                .makeId(e.getMakeId())
                .makeName(lookups.makes().get(e.getMakeId()))
                .modelId(e.getModelId())
                .modelName(e.getModelId() == null ? null : lookups.models().get(e.getModelId()))
                .year(e.getYear())
                .categoryId(e.getCategoryId())
                .categoryName(e.getCategoryId() == null ? null : lookups.categories().get(e.getCategoryId()))
                .productId(e.getProductId())
                .partNumber(e.getPartNumber())
                .description(e.getDescription())
                .conditions(e.getConditions().stream().sorted().toList())
                .images(e.getImageIds().stream().sorted()
                        .map(id -> PartEnquiryResponse.Image.builder().id(id).filePath(lookups.images().get(id)).build())
                        .toList())
                .recipientCount(e.getRecipientCount())
                .createdAt(e.getCreatedAt())
                .expiresAt(e.getExpiresAt())
                .closedAt(e.getClosedAt())
                .build();
    }

    private static PartQuoteResponse quoteView(PartQuote q, Lookups lookups) {
        return PartQuoteResponse.builder()
                .id(q.getId())
                .supplierId(q.getSupplierId())
                .supplierName(lookups.shops().get(q.getSupplierId()))
                .productId(q.getProductId())
                .productName(lookups.products().get(q.getProductId()))
                .unitPrice(q.getUnitPrice())
                .condition(q.getCondition())
                .quantity(q.getQuantity())
                .notes(q.getNotes())
                .validUntil(q.getValidUntil())
                .status(q.getStatus())
                .inventoryPartId(q.getInventoryPartId())
                .createdAt(q.getCreatedAt())
                .build();
    }

    private Map<Integer, List<PartQuote>> quotesByEnquiry(List<PartEnquiry> enquiries) {
        if (enquiries.isEmpty()) {
            return Map.of();
        }
        return quoteRepository.findByEnquiryIdIn(enquiries.stream().map(PartEnquiry::getId).toList()).stream()
                .collect(Collectors.groupingBy(PartQuote::getEnquiryId));
    }

    // ================================================================ helpers

    private static <T, K> Set<K> ids(Collection<T> items, Function<T, K> idOf) {
        return items.stream().map(idOf).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    private static <T, K> Map<K, String> names(Collection<T> items, Function<T, K> idOf, Function<T, String> nameOf) {
        return items.stream().filter(item -> nameOf.apply(item) != null)
                .collect(Collectors.toMap(idOf, nameOf, (a, b) -> a));
    }

    private static Set<ProductCondition> conditionSet(Collection<ProductCondition> conditions) {
        if (conditions == null || conditions.isEmpty()) {
            return Set.of();
        }
        if (conditions.stream().anyMatch(Objects::isNull)) {
            throw new ValidationException("Conditions must not contain empty values");
        }
        return EnumSet.copyOf(conditions);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String trimToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }
}
