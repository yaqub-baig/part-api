package com.autoadda.apis.part.service;

import com.autoadda.apis.part.entity.Category;
import com.autoadda.apis.part.entity.Make;
import com.autoadda.apis.part.entity.Model;
import com.autoadda.apis.part.entity.Supplier;
import com.autoadda.apis.part.exception.VehicleFitException;
import com.autoadda.apis.part.repository.CategoryRepository;
import com.autoadda.apis.part.repository.MakeRepository;
import com.autoadda.apis.part.repository.ModelRepository;
import com.autoadda.apis.part.request.SupplierRequest;
import com.autoadda.apis.part.util.ProductCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Year;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Turns the range a spare-part shop covers (makes, models, part categories, conditions) into ids and validates it.
 *
 * <p>Accepts ids (makeIds, modelIds, categoryIds, conditions) or the legacy text sent at registration
 * (brands = make names, models = model names, comma separated). A shop covers several makes; every model must
 * belong to one of them. Empty models / categories / conditions mean "all". The brands / models text columns are
 * rewritten from the resolved names so they always match the ids.
 */
@Component
@RequiredArgsConstructor
public class ShopCoverageResolver {

    /** Length of the supplier.brands / supplier.models columns. */
    static final int TEXT_LIMIT = 2000;

    private final MakeRepository makeRepository;
    private final ModelRepository modelRepository;
    private final CategoryRepository categoryRepository;

    /** Resolved coverage; lists are sorted by name, never null. */
    public record ShopCoverage(List<Make> makes, List<Model> models, List<Category> categories,
                               Set<ProductCondition> conditions, Integer yearFrom, Integer yearTo) {
    }

    /** True when the request touches the coverage at all (otherwise it is left as it is). */
    public static boolean hasCoverageData(SupplierRequest request) {
        return request.getMakeIds() != null || request.getBrands() != null
                || request.getModelIds() != null || request.getModels() != null
                || request.getCategoryIds() != null || request.getConditions() != null
                || request.getYearFrom() != null || request.getYearTo() != null || request.getAllYears() != null;
    }

    /**
     * Resolve the final coverage. Parts the request doesn't send are taken from {@code existing}
     * (partial update); pass null for a new shop. When only the makes change, existing models of makes that
     * are no longer covered are dropped.
     *
     * @throws VehicleFitException (400) for an unknown make / model / category or a model of a make not covered
     */
    public ShopCoverage resolve(SupplierRequest request, Supplier existing) {
        List<Make> makes = resolveMakes(request, existing);
        Set<String> makeIds = makes.stream().map(Make::getId).collect(Collectors.toSet());

        boolean modelsSent = request.getModelIds() != null || request.getModels() != null;
        List<Model> models = resolveModels(request, existing, makes);
        if (!modelsSent) {
            models = models.stream().filter(model -> makeIds.contains(model.getMakeId())).toList();
        }
        if (!models.isEmpty() && makes.isEmpty()) {
            throw new VehicleFitException("makeIds", null, "Makes are required when models are given");
        }
        for (Model model : models) {
            if (!makeIds.contains(model.getMakeId())) {
                throw new VehicleFitException("modelIds", model.getId(),
                        "Model '" + model.getName() + "' does not belong to any of the shop's makes");
            }
        }

        List<Category> categories = resolveCategories(request, existing);
        Set<ProductCondition> conditions = resolveConditions(request, existing);
        Integer[] years = resolveYears(request, existing);
        return new ShopCoverage(makes, models, categories, conditions, years[0], years[1]);
    }

    /** Write the coverage into the shop: ids for matching, plus normalised brands / models text for display. */
    public void apply(ShopCoverage coverage, Supplier supplier) {
        replace(supplier.getMakeIds(), supplier::setMakeIds, coverage.makes().stream().map(Make::getId).toList());
        replace(supplier.getModelIds(), supplier::setModelIds, coverage.models().stream().map(Model::getId).toList());
        replace(supplier.getCategoryIds(), supplier::setCategoryIds,
                coverage.categories().stream().map(Category::getId).toList());
        replace(supplier.getConditions(), supplier::setConditions, coverage.conditions());

        supplier.setYearFrom(coverage.yearFrom());
        supplier.setYearTo(coverage.yearTo());
        supplier.setBrands(joinNames(coverage.makes().stream().map(Make::getName).toList()));
        supplier.setModels(joinNames(coverage.models().stream().map(Model::getName).toList()));
    }

    // ---------------------------------------------------------------- makes

    private List<Make> resolveMakes(SupplierRequest request, Supplier existing) {
        if (request.getMakeIds() != null) {
            return makesByIds(request.getMakeIds());
        }
        if (request.getBrands() != null) {
            return makesByNames(request.getBrands());
        }
        if (existing == null) {
            return List.of();
        }
        if (notEmpty(existing.getMakeIds())) {
            return makesByIds(existing.getMakeIds());
        }
        return makesByNames(existing.getBrands()); // not converted yet: use its text
    }

    private List<Make> makesByIds(Collection<String> rawIds) {
        return byIds(rawIds, makeRepository::findAllById, Make::getId, "makeIds", "make", Make::getName);
    }

    private List<Make> makesByNames(String text) {
        Map<String, Make> byId = new LinkedHashMap<>();
        for (String name : splitNames(text)) {
            Make make = makeRepository.findByName(name);
            if (make == null) {
                throw new VehicleFitException("brands", name, "Unknown make '" + name + "'");
            }
            byId.put(make.getId(), make);
        }
        return sortByName(byId.values(), Make::getName);
    }

    // ---------------------------------------------------------------- models

    private List<Model> resolveModels(SupplierRequest request, Supplier existing, List<Make> makes) {
        if (request.getModelIds() != null) {
            return modelsByIds(request.getModelIds());
        }
        if (request.getModels() != null) {
            return modelsByNames(request.getModels(), makes);
        }
        if (existing == null) {
            return List.of();
        }
        if (notEmpty(existing.getModelIds())) {
            return modelsByIds(existing.getModelIds());
        }
        return modelsByNames(existing.getModels(), makes); // not converted yet: use its text
    }

    private List<Model> modelsByIds(Collection<String> rawIds) {
        return byIds(rawIds, modelRepository::findAllById, Model::getId, "modelIds", "model", Model::getName);
    }

    private List<Model> modelsByNames(String text, List<Make> makes) {
        List<String> names = splitNames(text);
        if (names.isEmpty()) {
            return List.of();
        }
        if (makes.isEmpty()) {
            throw new VehicleFitException("makeIds", null, "Makes are required when models are given");
        }
        List<String> makeIds = makes.stream().map(Make::getId).toList();
        List<Model> found = modelRepository.findByMakeIdInAndNameIn(makeIds, names);
        Map<String, Model> byId = new LinkedHashMap<>();
        for (String name : names) {
            List<Model> matches = found.stream()
                    .filter(model -> model.getName() != null && model.getName().trim().equalsIgnoreCase(name))
                    .toList();
            if (matches.isEmpty()) {
                throw new VehicleFitException("models", name, "Unknown model '" + name + "' for the shop's makes");
            }
            if (matches.size() > 1) {
                throw new VehicleFitException("models", name, "Model name '" + name + "' matches " + matches.size()
                        + " models of the shop's makes; send modelIds instead");
            }
            byId.put(matches.get(0).getId(), matches.get(0));
        }
        return sortByName(byId.values(), Model::getName);
    }

    // ---------------------------------------------------------------- categories, conditions

    private List<Category> resolveCategories(SupplierRequest request, Supplier existing) {
        Collection<String> ids = request.getCategoryIds() != null ? request.getCategoryIds()
                : existing != null ? existing.getCategoryIds() : null;
        if (ids == null) {
            return List.of();
        }
        return byIds(ids, categoryRepository::findAllById, Category::getId, "categoryIds", "category",
                Category::getName);
    }

    private static Set<ProductCondition> resolveConditions(SupplierRequest request, Supplier existing) {
        Collection<ProductCondition> conditions = request.getConditions() != null ? request.getConditions()
                : existing != null ? existing.getConditions() : null;
        if (conditions == null || conditions.isEmpty()) {
            return Set.of();
        }
        if (conditions.stream().anyMatch(Objects::isNull)) {
            throw new VehicleFitException("conditions", null, "Conditions must not contain empty values");
        }
        return EnumSet.copyOf(conditions);
    }

    /** {yearFrom, yearTo}: allYears clears, either year sent replaces both, otherwise the shop's current range. */
    private static Integer[] resolveYears(SupplierRequest request, Supplier existing) {
        if (Boolean.TRUE.equals(request.getAllYears())) {
            return new Integer[] {null, null};
        }
        if (request.getYearFrom() == null && request.getYearTo() == null) {
            return existing == null ? new Integer[] {null, null}
                    : new Integer[] {existing.getYearFrom(), existing.getYearTo()};
        }
        Integer from = request.getYearFrom();
        Integer to = request.getYearTo();
        checkYear("yearFrom", from);
        checkYear("yearTo", to);
        if (from != null && to != null && from > to) {
            throw new VehicleFitException("yearTo", String.valueOf(to),
                    "Year range " + from + "-" + to + " ends before it starts");
        }
        return new Integer[] {from, to};
    }

    private static void checkYear(String field, Integer year) {
        int max = Year.now().getValue() + VehicleFitResolver.YEARS_AHEAD;
        if (year != null && (year < VehicleFitResolver.MIN_YEAR || year > max)) {
            throw new VehicleFitException(field, String.valueOf(year),
                    "Year " + year + " is outside " + VehicleFitResolver.MIN_YEAR + "-" + max);
        }
    }

    // ---------------------------------------------------------------- helpers

    private static <T> List<T> byIds(Collection<String> rawIds, Function<Set<String>, List<T>> loader,
                                     Function<T, String> idOf, String field, String label,
                                     Function<T, String> nameOf) {
        Set<String> ids = rawIds.stream()
                .filter(id -> id != null && !id.isBlank())
                .map(String::trim)
                .collect(Collectors.toCollection(TreeSet::new));
        if (ids.isEmpty()) {
            return List.of();
        }
        List<T> found = loader.apply(ids);
        Set<String> foundIds = found.stream().map(idOf).collect(Collectors.toSet());
        for (String id : ids) {
            if (!foundIds.contains(id)) {
                throw new VehicleFitException(field, id, "Unknown " + label + " id '" + id + "'");
            }
        }
        return sortByName(found, nameOf);
    }

    /**
     * "Toyota, Nissan"; also tolerates a JSON-style list ("[\"Toyota\",\"Nissan\"]") in case a client sent one.
     */
    static List<String> splitNames(String text) {
        if (text == null) {
            return List.of();
        }
        String cleaned = text.trim();
        if (cleaned.startsWith("[") && cleaned.endsWith("]")) {
            cleaned = cleaned.substring(1, cleaned.length() - 1);
        }
        return Arrays.stream(cleaned.split(","))
                .map(name -> name.trim().replaceAll("^[\"']+|[\"']+$", "").trim())
                .filter(name -> !name.isEmpty())
                .distinct()
                .toList();
    }

    private static <T> List<T> sortByName(Collection<T> items, Function<T, String> nameOf) {
        return items.stream()
                .sorted(Comparator.comparing(item -> Objects.requireNonNullElse(nameOf.apply(item), ""),
                        String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private static String joinNames(List<String> names) {
        if (names.isEmpty()) {
            return null;
        }
        String joined = String.join(", ", names);
        return joined.length() <= TEXT_LIMIT ? joined : joined.substring(0, TEXT_LIMIT - 3) + "...";
    }

    private static <T> void replace(Set<T> current, java.util.function.Consumer<Set<T>> setter, Collection<T> values) {
        if (current == null) {
            setter.accept(new HashSet<>(values));
            return;
        }
        current.clear();
        current.addAll(values);
    }

    private static boolean notEmpty(Collection<?> values) {
        return values != null && !values.isEmpty();
    }
}
