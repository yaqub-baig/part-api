package com.autoadda.apis.part.service;

import com.autoadda.apis.part.entity.Make;
import com.autoadda.apis.part.entity.Model;
import com.autoadda.apis.part.entity.Product;
import com.autoadda.apis.part.exception.VehicleFitException;
import com.autoadda.apis.part.repository.MakeRepository;
import com.autoadda.apis.part.repository.ModelRepository;
import com.autoadda.apis.part.request.ProductRequest;
import com.autoadda.apis.part.request.ProductSearchRequest;
import com.autoadda.apis.part.specification.VehicleFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Turns a product's make / models / years into ids and validates them.
 *
 * <p>Accepts either ids (makeId, modelIds, years) or the legacy text the panel sends
 * (make name, comma-separated model names, comma-separated years, ranges like 2018-2022).
 * Models must belong to the make. The product's text columns are rewritten from the resolved
 * names so they always match the ids.
 */
@Component
@RequiredArgsConstructor
public class VehicleFitResolver {

    public static final int MIN_YEAR = 1950;
    /** Same window as the panel's year picker: up to next model years. */
    public static final int YEARS_AHEAD = 5;
    private static final Pattern SINGLE_YEAR = Pattern.compile("\\d{4}");
    private static final Pattern YEAR_RANGE = Pattern.compile("(\\d{4})\\s*-\\s*(\\d{4})");

    private final MakeRepository makeRepository;
    private final ModelRepository modelRepository;

    /** Resolved vehicle fit. make is null when none is set; models and years are sorted, never null. */
    public record VehicleFit(Make make, List<Model> models, List<Integer> years) {
    }

    /** True when the request touches the vehicle fit at all (otherwise it is left as it is). */
    public static boolean hasVehicleData(ProductRequest request) {
        return request.getMakeId() != null || request.getMake() != null
                || request.getModelIds() != null || request.getModel() != null
                || request.getYears() != null || request.getYear() != null;
    }

    /**
     * Resolve the final vehicle fit. Parts the request doesn't send are taken from {@code existing}
     * (partial update); pass null for a new product.
     *
     * @throws VehicleFitException (400) for an unknown make/model, a model of another make, or a bad year
     */
    public VehicleFit resolve(ProductRequest request, Product existing) {
        Make make = resolveMake(request, existing);
        List<Model> models = resolveModels(request, existing, make);
        List<Integer> years = resolveYears(request, existing);

        if (!models.isEmpty() && make == null) {
            throw new VehicleFitException("make", null, "A make is required when models are given");
        }
        for (Model model : models) {
            if (!make.getId().equals(model.getMakeId())) {
                throw new VehicleFitException("model", model.getName(),
                        "Model '" + model.getName() + "' does not belong to make '" + make.getName()
                                + "'; send the models of the new make");
            }
        }
        return new VehicleFit(make, models, years);
    }

    /** Write the fit into the product: ids for search, plus normalised text columns for display. */
    public void apply(VehicleFit fit, Product product) {
        Make make = fit.make();
        product.setMakeId(make == null ? null : make.getId());
        product.setMake(make == null ? null : make.getName());

        if (product.getModelIds() == null) {
            product.setModelIds(new HashSet<>());
        }
        product.getModelIds().clear();
        fit.models().forEach(model -> product.getModelIds().add(model.getId()));
        product.setModel(fit.models().isEmpty() ? null
                : fit.models().stream().map(Model::getName).collect(Collectors.joining(", ")));

        if (product.getYears() == null) {
            product.setYears(new HashSet<>());
        }
        product.getYears().clear();
        product.getYears().addAll(fit.years());
        product.setYear(fit.years().isEmpty() ? null
                : fit.years().stream().map(String::valueOf).collect(Collectors.joining(", ")));
    }

    // ---------------------------------------------------------------- search

    /** True when the search names a make or model by name (needs a lookup before filtering). */
    public static boolean hasVehicleNames(ProductSearchRequest request) {
        return (isBlank(request.getMakeId()) && !isBlank(request.getMake()))
                || (isBlank(request.getModelId()) && !isBlank(request.getModel()));
    }

    /**
     * Vehicle filter for a search, resolving make / model names to ids (ids win when both are sent).
     *
     * @return empty when a name matches nothing, meaning no product can match
     */
    public Optional<VehicleFilter> searchFilter(ProductSearchRequest request) {
        String makeId = request.getMakeId();
        if (isBlank(makeId) && !isBlank(request.getMake())) {
            Make make = makeRepository.findByName(request.getMake().trim());
            if (make == null) {
                return Optional.empty();
            }
            makeId = make.getId();
        }

        Set<String> modelIds = Set.of();
        if (!isBlank(request.getModelId())) {
            modelIds = Set.of(request.getModelId().trim());
        } else if (!isBlank(request.getModel())) {
            String name = request.getModel().trim();
            List<Model> found = isBlank(makeId)
                    ? modelRepository.findAllByName(name)
                    : modelRepository.findByMakeIdAndNameIn(makeId, List.of(name));
            if (found.isEmpty()) {
                return Optional.empty();
            }
            modelIds = found.stream().map(Model::getId).collect(Collectors.toSet());
        }
        return Optional.of(new VehicleFilter(makeId, modelIds, request.getYear()));
    }

    // ---------------------------------------------------------------- make

    private Make resolveMake(ProductRequest request, Product existing) {
        String makeId;
        String makeName;
        if (request.getMakeId() != null || request.getMake() != null) {
            makeId = request.getMakeId();
            makeName = request.getMake();
        } else if (existing != null) {
            makeId = existing.getMakeId();
            makeName = existing.getMake();
        } else {
            return null;
        }

        if (!isBlank(makeId)) {
            String id = makeId.trim();
            return makeRepository.findById(id)
                    .orElseThrow(() -> new VehicleFitException("makeId", id, "Unknown make id '" + id + "'"));
        }
        if (!isBlank(makeName)) {
            String name = makeName.trim();
            Make make = makeRepository.findByName(name);
            if (make == null) {
                throw new VehicleFitException("make", name, "Unknown make '" + name + "'");
            }
            return make;
        }
        return null; // sent blank: clears the make
    }

    // ---------------------------------------------------------------- models

    private List<Model> resolveModels(ProductRequest request, Product existing, Make make) {
        if (request.getModelIds() != null) {
            return modelsByIds(request.getModelIds());
        }
        if (request.getModel() != null) {
            return modelsByNames(request.getModel(), make);
        }
        if (existing == null) {
            return List.of();
        }
        if (existing.getModelIds() != null && !existing.getModelIds().isEmpty()) {
            return modelsByIds(existing.getModelIds());
        }
        return modelsByNames(existing.getModel(), make); // not converted yet: use its text
    }

    private List<Model> modelsByIds(Collection<String> rawIds) {
        Set<String> ids = rawIds.stream()
                .filter(id -> !isBlank(id))
                .map(String::trim)
                .collect(Collectors.toCollection(TreeSet::new));
        if (ids.isEmpty()) {
            return List.of();
        }
        List<Model> found = modelRepository.findAllById(ids);
        Set<String> foundIds = found.stream().map(Model::getId).collect(Collectors.toSet());
        for (String id : ids) {
            if (!foundIds.contains(id)) {
                throw new VehicleFitException("modelIds", id, "Unknown model id '" + id + "'");
            }
        }
        return sortByName(found);
    }

    private List<Model> modelsByNames(String text, Make make) {
        List<String> names = splitNames(text);
        if (names.isEmpty()) {
            return List.of();
        }
        if (make == null) {
            throw new VehicleFitException("make", null, "A make is required when models are given");
        }
        List<Model> found = modelRepository.findByMakeIdAndNameIn(make.getId(), names);
        Map<String, Model> byId = new LinkedHashMap<>();
        for (String name : names) {
            List<Model> matches = found.stream()
                    .filter(model -> model.getName() != null && model.getName().trim().equalsIgnoreCase(name))
                    .toList();
            if (matches.isEmpty()) {
                throw new VehicleFitException("model", name,
                        "Unknown model '" + name + "' for make '" + make.getName() + "'");
            }
            if (matches.size() > 1) {
                throw new VehicleFitException("model", name, "Model name '" + name + "' matches " + matches.size()
                        + " models of make '" + make.getName() + "'; send modelIds instead");
            }
            byId.put(matches.get(0).getId(), matches.get(0));
        }
        return sortByName(byId.values());
    }

    private static List<String> splitNames(String text) {
        if (text == null) {
            return List.of();
        }
        return Arrays.stream(text.split(","))
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .distinct()
                .toList();
    }

    private static List<Model> sortByName(Collection<Model> models) {
        return models.stream()
                .sorted(Comparator.comparing(model -> model.getName() == null ? "" : model.getName(),
                        String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    // ---------------------------------------------------------------- years

    private List<Integer> resolveYears(ProductRequest request, Product existing) {
        if (request.getYears() != null) {
            return checkedYears(request.getYears());
        }
        if (request.getYear() != null) {
            return parseYears(request.getYear());
        }
        if (existing == null) {
            return List.of();
        }
        if (existing.getYears() != null && !existing.getYears().isEmpty()) {
            return checkedYears(existing.getYears());
        }
        return parseYears(existing.getYear()); // not converted yet: use its text
    }

    /** "2018, 2019" or "2018-2020" or a mix; blank gives no years. */
    static List<Integer> parseYears(String text) {
        if (text == null) {
            return List.of();
        }
        List<Integer> years = new ArrayList<>();
        for (String raw : text.split(",")) {
            String token = raw.trim();
            if (token.isEmpty()) {
                continue;
            }
            Matcher range = YEAR_RANGE.matcher(token);
            if (SINGLE_YEAR.matcher(token).matches()) {
                years.add(Integer.parseInt(token));
            } else if (range.matches()) {
                int from = Integer.parseInt(range.group(1));
                int to = Integer.parseInt(range.group(2));
                if (from > to) {
                    throw new VehicleFitException("year", token, "Year range '" + token + "' ends before it starts");
                }
                checkYear(from, token);
                checkYear(to, token);
                for (int year = from; year <= to; year++) {
                    years.add(year);
                }
            } else {
                throw new VehicleFitException("year", token, "Invalid year '" + token + "'; use e.g. 2020 or 2018-2022");
            }
        }
        return checkedYears(years);
    }

    private static List<Integer> checkedYears(Collection<Integer> years) {
        TreeSet<Integer> result = new TreeSet<>();
        for (Integer year : years) {
            if (year == null) {
                throw new VehicleFitException("years", null, "Years must not contain empty values");
            }
            checkYear(year, String.valueOf(year));
            result.add(year);
        }
        return List.copyOf(result);
    }

    private static void checkYear(int year, String value) {
        int max = Year.now().getValue() + YEARS_AHEAD;
        if (year < MIN_YEAR || year > max) {
            throw new VehicleFitException("year", value,
                    "Year " + year + " is outside " + MIN_YEAR + "-" + max);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
