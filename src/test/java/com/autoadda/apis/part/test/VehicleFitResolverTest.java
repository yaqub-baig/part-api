package com.autoadda.apis.part.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Year;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.autoadda.apis.part.entity.Make;
import com.autoadda.apis.part.entity.Model;
import com.autoadda.apis.part.entity.Product;
import com.autoadda.apis.part.exception.VehicleFitException;
import com.autoadda.apis.part.repository.MakeRepository;
import com.autoadda.apis.part.repository.ModelRepository;
import com.autoadda.apis.part.request.ProductRequest;
import com.autoadda.apis.part.request.ProductSearchRequest;
import com.autoadda.apis.part.service.VehicleFitResolver;
import com.autoadda.apis.part.service.VehicleFitResolver.VehicleFit;
import com.autoadda.apis.part.specification.VehicleFilter;

@ExtendWith(MockitoExtension.class)
class VehicleFitResolverTest {

	@Mock
	private MakeRepository makeRepository;
	@Mock
	private ModelRepository modelRepository;
	@InjectMocks
	private VehicleFitResolver resolver;

	private static final Make TOYOTA = Make.builder().id("mk-1").name("Toyota").build();
	private static final Make HONDA = Make.builder().id("mk-2").name("Honda").build();
	private static final Model CAMRY = Model.builder().id("md-7").name("Camry").makeId("mk-1").build();
	private static final Model COROLLA = Model.builder().id("md-9").name("Corolla").makeId("mk-1").build();
	private static final Model CIVIC = Model.builder().id("md-3").name("Civic").makeId("mk-2").build();

	@Test
	void legacyTextFromPanelResolvesToIdsAndNormalisedText() {
		when(makeRepository.findByName("Toyota")).thenReturn(TOYOTA);
		when(modelRepository.findByMakeIdAndNameIn("mk-1", List.of("Corolla", "camry")))
				.thenReturn(List.of(CAMRY, COROLLA)); // DB collation matches case-insensitively
		ProductRequest request = ProductRequest.builder()
				.make(" Toyota ").model("Corolla, camry,, Corolla").year("2020, 2018, 2019, 2020").build();

		VehicleFit fit = resolver.resolve(request, null);
		Product product = new Product();
		resolver.apply(fit, product);

		assertEquals("mk-1", product.getMakeId());
		assertEquals(Set.of("md-7", "md-9"), product.getModelIds());
		assertEquals(Set.of(2018, 2019, 2020), product.getYears());
		assertEquals("Toyota", product.getMake());
		assertEquals("Camry, Corolla", product.getModel());
		assertEquals("2018, 2019, 2020", product.getYear());
	}

	@Test
	void idsAreUsedAsGivenWithoutNameLookups() {
		when(makeRepository.findById("mk-1")).thenReturn(Optional.of(TOYOTA));
		when(modelRepository.findAllById(any())).thenReturn(List.of(COROLLA));
		ProductRequest request = ProductRequest.builder()
				.makeId("mk-1").modelIds(List.of("md-9")).years(List.of(2021, 2019)).make("ignored").model("ignored").build();

		VehicleFit fit = resolver.resolve(request, null);

		assertEquals(TOYOTA, fit.make());
		assertEquals(List.of(COROLLA), fit.models());
		assertEquals(List.of(2019, 2021), fit.years());
		verify(makeRepository, never()).findByName(any());
		verify(modelRepository, never()).findByMakeIdAndNameIn(any(), any());
	}

	@Test
	void yearRangesAreExpanded() {
		VehicleFit fit = resolver.resolve(ProductRequest.builder().year("2015, 2018 - 2020").build(), null);
		assertEquals(List.of(2015, 2018, 2019, 2020), fit.years());
		assertNull(fit.make());
	}

	@Test
	void unknownMakeIsRejectedWithFieldAndValue() {
		when(makeRepository.findByName("Toyta")).thenReturn(null);
		VehicleFitException e = assertThrows(VehicleFitException.class,
				() -> resolver.resolve(ProductRequest.builder().make("Toyta").build(), null));
		assertEquals("make", e.getField());
		assertEquals("Toyta", e.getValue());
	}

	@Test
	void unknownModelNameIsRejected() {
		when(makeRepository.findByName("Toyota")).thenReturn(TOYOTA);
		when(modelRepository.findByMakeIdAndNameIn("mk-1", List.of("Camry", "Camry Hybrid"))).thenReturn(List.of(CAMRY));
		VehicleFitException e = assertThrows(VehicleFitException.class, () -> resolver.resolve(
				ProductRequest.builder().make("Toyota").model("Camry, Camry Hybrid").build(), null));
		assertEquals("model", e.getField());
		assertEquals("Camry Hybrid", e.getValue());
	}

	@Test
	void ambiguousModelNameIsRejectedInsteadOfGuessing() {
		Model camrySedan = Model.builder().id("md-70").name("Camry").makeId("mk-1").build();
		when(makeRepository.findByName("Toyota")).thenReturn(TOYOTA);
		when(modelRepository.findByMakeIdAndNameIn("mk-1", List.of("Camry"))).thenReturn(List.of(CAMRY, camrySedan));
		VehicleFitException e = assertThrows(VehicleFitException.class,
				() -> resolver.resolve(ProductRequest.builder().make("Toyota").model("Camry").build(), null));
		assertTrue(e.getMessage().contains("send modelIds"));
	}

	@Test
	void modelOfAnotherMakeIsRejected() {
		when(makeRepository.findById("mk-1")).thenReturn(Optional.of(TOYOTA));
		when(modelRepository.findAllById(any())).thenReturn(List.of(CIVIC));
		VehicleFitException e = assertThrows(VehicleFitException.class, () -> resolver.resolve(
				ProductRequest.builder().makeId("mk-1").modelIds(List.of("md-3")).build(), null));
		assertEquals("Civic", e.getValue());
	}

	@Test
	void unknownModelIdIsRejected() {
		when(makeRepository.findById("mk-1")).thenReturn(Optional.of(TOYOTA));
		when(modelRepository.findAllById(any())).thenReturn(List.of(CAMRY));
		VehicleFitException e = assertThrows(VehicleFitException.class, () -> resolver.resolve(
				ProductRequest.builder().makeId("mk-1").modelIds(List.of("md-7", "md-404")).build(), null));
		assertEquals("md-404", e.getValue());
	}

	@Test
	void modelsWithoutMakeAreRejected() {
		assertThrows(VehicleFitException.class,
				() -> resolver.resolve(ProductRequest.builder().model("Camry").build(), null));
	}

	@Test
	void badYearsAreRejected() {
		int tooLate = Year.now().getValue() + VehicleFitResolver.YEARS_AHEAD + 1;
		assertThrows(VehicleFitException.class, () -> resolver.resolve(ProductRequest.builder().year("20x0").build(), null));
		assertThrows(VehicleFitException.class, () -> resolver.resolve(ProductRequest.builder().year("2022-2018").build(), null));
		assertThrows(VehicleFitException.class, () -> resolver.resolve(ProductRequest.builder().year("1949").build(), null));
		assertThrows(VehicleFitException.class,
				() -> resolver.resolve(ProductRequest.builder().years(List.of(tooLate)).build(), null));
	}

	@Test
	void partialUpdateKeepsPartsNotSent() {
		Product existing = Product.builder().makeId("mk-1").modelIds(Set.of("md-7")).years(Set.of(2018)).build();
		when(makeRepository.findById("mk-1")).thenReturn(Optional.of(TOYOTA));
		when(modelRepository.findAllById(any())).thenReturn(List.of(CAMRY));

		VehicleFit fit = resolver.resolve(ProductRequest.builder().years(List.of(2019, 2020)).build(), existing);

		assertEquals(TOYOTA, fit.make());
		assertEquals(List.of(CAMRY), fit.models());
		assertEquals(List.of(2019, 2020), fit.years());
	}

	@Test
	void changingTheMakeWithoutNewModelsIsRejected() {
		Product existing = Product.builder().makeId("mk-1").modelIds(Set.of("md-7")).build();
		when(makeRepository.findById("mk-2")).thenReturn(Optional.of(HONDA));
		when(modelRepository.findAllById(any())).thenReturn(List.of(CAMRY));

		assertThrows(VehicleFitException.class,
				() -> resolver.resolve(ProductRequest.builder().makeId("mk-2").build(), existing));
	}

	@Test
	void blankMakeClearsTheFit() {
		VehicleFit fit = resolver.resolve(ProductRequest.builder().make("").model("").year("").build(),
				Product.builder().makeId("mk-1").build());
		Product product = Product.builder().makeId("mk-1").make("Toyota").model("Camry").year("2018").build();
		resolver.apply(fit, product);
		assertNull(product.getMakeId());
		assertNull(product.getMake());
		assertNull(product.getModel());
		assertNull(product.getYear());
		assertTrue(product.getModelIds().isEmpty());
	}

	@Test
	void searchNamesResolveToIdsAndUnknownNamesMatchNothing() {
		when(makeRepository.findByName("Toyota")).thenReturn(TOYOTA);
		when(modelRepository.findByMakeIdAndNameIn("mk-1", List.of("Camry"))).thenReturn(List.of(CAMRY));
		Optional<VehicleFilter> filter = resolver.searchFilter(
				ProductSearchRequest.builder().make("Toyota").model("Camry").year(2020).build());
		assertEquals(new VehicleFilter("mk-1", Set.of("md-7"), 2020), filter.orElseThrow());

		when(makeRepository.findByName("Nope")).thenReturn(null);
		assertTrue(resolver.searchFilter(ProductSearchRequest.builder().make("Nope").build()).isEmpty());
	}

	@Test
	void searchModelNameWithoutMakeMatchesThatNameAcrossMakes() {
		Model civicOther = Model.builder().id("md-33").name("Civic").makeId("mk-5").build();
		when(modelRepository.findAllByName("Civic")).thenReturn(List.of(CIVIC, civicOther));
		VehicleFilter filter = resolver.searchFilter(ProductSearchRequest.builder().model("Civic").build()).orElseThrow();
		assertNull(filter.makeId());
		assertEquals(Set.of("md-3", "md-33"), filter.modelIds());
	}
}
