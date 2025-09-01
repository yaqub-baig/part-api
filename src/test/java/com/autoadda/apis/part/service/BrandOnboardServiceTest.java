package com.autoadda.apis.part.service;

import com.autoadda.apis.part.entity.BrandOnboard;
import com.autoadda.apis.part.entity.Make;
import com.autoadda.apis.part.exception.ResourceNotFoundException;
import com.autoadda.apis.part.mapper.BrandOnboardMapper;
import com.autoadda.apis.part.repository.BrandOnboardRepository;
import com.autoadda.apis.part.repository.MakeRepository;
import com.autoadda.apis.part.request.BrandOnboardRequest;
import com.autoadda.apis.part.response.BrandOnboardResponse;
import com.autoadda.apis.part.response.GenericResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BrandOnboardServiceTest {

    @Mock
    private BrandOnboardRepository brandOnboardRepository;

    @Mock
    private BrandOnboardMapper brandOnboardMapper;

    @Mock
    private MakeRepository makeRepository;

    @InjectMocks
    private BrandOnboardService brandOnboardService;

    private BrandOnboardRequest brandOnboardRequest;
    private BrandOnboard brandOnboard;
    private Make make;
    private final String autoAddaId = "123";

    @BeforeEach
    void setUp() {
        brandOnboardRequest = new BrandOnboardRequest();
        brandOnboardRequest.setBrandName("TestBrand");
        // Set other required fields

        brandOnboard = new BrandOnboard();
        brandOnboard.setId(1);
        brandOnboard.setBrandName("TestBrand");
        brandOnboard.setModelName("TestModel");
        brandOnboard.setEngineType("Petrol");
        brandOnboard.setRequestBy(123);
        brandOnboard.setRequestReferenceId("TESTBRAND_TESTMODEL_PETROL_BR-2401-0001");

        make = new Make();
        make.setId("1");
        make.setName("TestBrand");
    }

    @Test
    void brandOnboard_WhenBrandDoesNotExist_ShouldSaveAndReturnSuccess() {
        // Arrange
        when(makeRepository.findByName(anyString())).thenReturn(null);
        when(brandOnboardMapper.toEntity(any(BrandOnboardRequest.class))).thenReturn(brandOnboard);
        when(brandOnboardRepository.save(any(BrandOnboard.class))).thenReturn(brandOnboard);
        when(brandOnboardMapper.toResponse(any(BrandOnboard.class))).thenReturn(new BrandOnboardResponse());

        // Act
        GenericResponse response = brandOnboardService.brandOnboard(brandOnboardRequest, autoAddaId);

        // Assert
        assertNotNull(response);
        assertEquals("success", response.getStatus());
        assertEquals("200", response.getCode());
        assertEquals("Your request is successfully taken our team will look into it and add brand if needed", response.getMessage());

        verify(makeRepository, times(1)).findByName(brandOnboardRequest.getBrandName());
        verify(brandOnboardMapper, times(1)).toEntity(brandOnboardRequest);
        verify(brandOnboardRepository, times(1)).save(brandOnboard);
        verify(brandOnboardMapper, times(1)).toResponse(brandOnboard);
    }

    @Test
    void brandOnboard_WhenBrandAlreadyExists_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(makeRepository.findByName(anyString())).thenReturn(make);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> brandOnboardService.brandOnboard(brandOnboardRequest, autoAddaId));

        assertEquals("brand already exists", exception.getMessage());

        verify(makeRepository, times(1)).findByName(brandOnboardRequest.getBrandName());
        verify(brandOnboardMapper, never()).toEntity(any());
        verify(brandOnboardRepository, never()).save(any());
    }

    @Test
    void updateBrand_WhenBrandExists_ShouldUpdateAndReturnSuccess() {
        // Arrange
        Integer brandId = 1;
        when(brandOnboardRepository.findById(brandId)).thenReturn(Optional.of(brandOnboard));
        when(brandOnboardRepository.save(any(BrandOnboard.class))).thenReturn(brandOnboard);
        when(brandOnboardMapper.toResponse(any(BrandOnboard.class))).thenReturn(new BrandOnboardResponse());

        // Act
        GenericResponse response = brandOnboardService.updateBrand(brandId, brandOnboardRequest);

        // Assert
        assertNotNull(response);
        assertEquals("success", response.getStatus());
        assertEquals("200", response.getCode());
        assertEquals("Brand updated Successfully", response.getMessage()); // Fixed the expected message

        verify(brandOnboardRepository, times(1)).findById(brandId);
        verify(brandOnboardRepository, times(1)).save(brandOnboard);
        verify(brandOnboardMapper, times(1)).toResponse(brandOnboard);
    }

    @Test
    void updateBrand_WhenBrandNotFound_ShouldThrowResourceNotFoundException() {
        // Arrange
        Integer brandId = 999;
        when(brandOnboardRepository.findById(brandId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> brandOnboardService.updateBrand(brandId, brandOnboardRequest));

        assertEquals("update Brand request not found", exception.getMessage());

        verify(brandOnboardRepository, times(1)).findById(brandId);
        verify(brandOnboardRepository, never()).save(any());
        verify(brandOnboardMapper, never()).toResponse(any());
    }

    @Test
    void generateRequestReferenceId_WhenNoExistingCodes_ShouldGenerateFirstSequence() {
        // Arrange
        String globalPrefix = "BR-" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyMM")) + "-";
        when(brandOnboardRepository.findAllCodesForGlobalSequence(globalPrefix)).thenReturn(Collections.emptyList());

        BrandOnboard entity = new BrandOnboard();
        entity.setBrandName("TestBrand");
        entity.setModelName("TestModel");
        entity.setEngineType("Petrol");

        // Act
        String result = brandOnboardService.generateRequestReferenceId(entity);

        // Assert
        assertNotNull(result);
        assertTrue(result.startsWith("TESTBRAND_TESTMODEL_PETROL_BR-"));
        assertTrue(result.endsWith("0001"));
        verify(brandOnboardRepository, times(1)).findAllCodesForGlobalSequence(anyString());
    }

    @Test
    void generateRequestReferenceId_WithExistingCodes_ShouldGenerateNextSequence() {
        // Arrange
        String currentYearMonth = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyMM"));
        String globalPrefix = "BR-" + currentYearMonth + "-";
        String existingCode = "TESTBRAND_TESTMODEL_PETROL_BR-" + currentYearMonth + "-0005";

        when(brandOnboardRepository.findAllCodesForGlobalSequence(globalPrefix)).thenReturn(Arrays.asList(existingCode));

        BrandOnboard entity = new BrandOnboard();
        entity.setBrandName("TestBrand");
        entity.setModelName("TestModel");
        entity.setEngineType("Petrol");

        // Act
        String result = brandOnboardService.generateRequestReferenceId(entity);

        // Assert
        assertNotNull(result);
        assertTrue(result.startsWith("TESTBRAND_TESTMODEL_PETROL_BR-" + currentYearMonth + "-"));
        assertTrue(result.endsWith("0006"));
        verify(brandOnboardRepository, times(1)).findAllCodesForGlobalSequence(anyString());
    }

    @Test
    void generateRequestReferenceId_WhenParsingFails_ShouldStartFromOne() {
        // Arrange
        String currentYearMonth = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyMM"));
        String globalPrefix = "BR-" + currentYearMonth + "-";
        String invalidCode = "TESTBRAND_TESTMODEL_PETROL_BR-" + currentYearMonth + "-INVALID";

        when(brandOnboardRepository.findAllCodesForGlobalSequence(globalPrefix)).thenReturn(Arrays.asList(invalidCode));

        BrandOnboard entity = new BrandOnboard();
        entity.setBrandName("TestBrand");
        entity.setModelName("TestModel");
        entity.setEngineType("Petrol");

        // Act
        String result = brandOnboardService.generateRequestReferenceId(entity);

        // Assert
        assertNotNull(result);
        assertTrue(result.startsWith("TESTBRAND_TESTMODEL_PETROL_BR-" + currentYearMonth + "-"));
        assertTrue(result.endsWith("0001"));
        verify(brandOnboardRepository, times(1)).findAllCodesForGlobalSequence(anyString());
    }

    @Test
    void generateRequestReferenceId_WithMultipleExistingCodes_ShouldUseLatestOne() {
        // Arrange
        String currentYearMonth = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyMM"));
        String globalPrefix = "BR-" + currentYearMonth + "-";
        List<String> existingCodes = Arrays.asList(
                "TESTBRAND_TESTMODEL_PETROL_BR-" + currentYearMonth + "-0001",
                "TESTBRAND_TESTMODEL_PETROL_BR-" + currentYearMonth + "-0003",
                "TESTBRAND_TESTMODEL_PETROL_BR-" + currentYearMonth + "-0002"
        );
        when(brandOnboardRepository.findAllCodesForGlobalSequence(globalPrefix)).thenReturn(existingCodes);

        BrandOnboard entity = new BrandOnboard();
        entity.setBrandName("TestBrand");
        entity.setModelName("TestModel");
        entity.setEngineType("Petrol");

        // Act
        String result = brandOnboardService.generateRequestReferenceId(entity);

        // Assert
        assertNotNull(result);
        assertTrue(result.startsWith("TESTBRAND_TESTMODEL_PETROL_BR-" + currentYearMonth + "-"));
        // Should be the next sequence after the highest (0003 + 1 = 0004)
        assertTrue(result.endsWith("0004"));
        verify(brandOnboardRepository, times(1)).findAllCodesForGlobalSequence(anyString());
    }
}