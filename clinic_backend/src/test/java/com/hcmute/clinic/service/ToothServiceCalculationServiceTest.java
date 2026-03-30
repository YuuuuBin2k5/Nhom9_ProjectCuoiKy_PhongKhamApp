package com.hcmute.clinic.service;

import com.hcmute.clinic.entity.*;
import com.hcmute.clinic.enums.StepStatus;
import com.hcmute.clinic.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ToothServiceCalculationService
 * Tests all methods for adding, removing, and calculating costs for services
 */
@DisplayName("ToothServiceCalculationService Tests")
class ToothServiceCalculationServiceTest {
    
    @Mock
    private TreatmentPlanStepRepository stepRepository;
    
    @Mock
    private TreatmentPlanRepository planRepository;
    
    @Mock
    private ServiceRepository serviceRepository;
    
    @InjectMocks
    private ToothServiceCalculationService toothService;
    
    private TreatmentPlan testPlan;
    private Service testService;
    private TreatmentPlanStep testStep;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Create test data
        testPlan = new TreatmentPlan();
        testPlan.setId(1L);
        
        testService = new Service();
        testService.setId(7L);
        testService.setName("Nhổ răng khôn");
        testService.setPrice(new BigDecimal("2000000"));
        
        testStep = new TreatmentPlanStep();
        testStep.setId(1L);
        testStep.setPlan(testPlan);
        testStep.setService(testService);
        testStep.setToothNumber("8");
        testStep.setActualPrice(new BigDecimal("2000000"));
        testStep.setSequenceOrder(1);
        testStep.setStatus(StepStatus.PENDING);
        testStep.setGeneralService(false);
    }
    
    @Test
    @DisplayName("Should add service to specific tooth successfully")
    void testAddServiceToTooth_Success() {
        // Arrange
        when(planRepository.findById(1L)).thenReturn(Optional.of(testPlan));
        when(serviceRepository.findById(7L)).thenReturn(Optional.of(testService));
        when(stepRepository.save(any(TreatmentPlanStep.class))).thenReturn(testStep);
        
        // Act
        TreatmentPlanStep result = toothService.addServiceToTooth(1L, 7L, "8", 1);
        
        // Assert
        assertNotNull(result);
        assertEquals("8", result.getToothNumber());
        assertEquals(false, result.isGeneralService());
        assertEquals(new BigDecimal("2000000"), result.getActualPrice());
        verify(stepRepository, times(1)).save(any(TreatmentPlanStep.class));
    }
    
    @Test
    @DisplayName("Should throw exception when plan not found")
    void testAddServiceToTooth_PlanNotFound() {
        // Arrange
        when(planRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            toothService.addServiceToTooth(999L, 7L, "8", 1);
        });
    }
    
    @Test
    @DisplayName("Should throw exception when service not found")
    void testAddServiceToTooth_ServiceNotFound() {
        // Arrange
        when(planRepository.findById(1L)).thenReturn(Optional.of(testPlan));
        when(serviceRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            toothService.addServiceToTooth(1L, 999L, "8", 1);
        });
    }
    
    @Test
    @DisplayName("Should throw exception when tooth number is empty")
    void testAddServiceToTooth_EmptyToothNumber() {
        // Arrange
        when(planRepository.findById(1L)).thenReturn(Optional.of(testPlan));
        when(serviceRepository.findById(7L)).thenReturn(Optional.of(testService));
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            toothService.addServiceToTooth(1L, 7L, "", 1);
        });
    }
    
    @Test
    @DisplayName("Should add general service successfully")
    void testAddGeneralService_Success() {
        // Arrange
        Service generalService = new Service();
        generalService.setId(1L);
        generalService.setName("Khám và tư vấn");
        generalService.setPrice(new BigDecimal("100000"));
        
        TreatmentPlanStep generalStep = new TreatmentPlanStep();
        generalStep.setId(2L);
        generalStep.setPlan(testPlan);
        generalStep.setService(generalService);
        generalStep.setToothNumber(null);
        generalStep.setActualPrice(new BigDecimal("100000"));
        generalStep.setSequenceOrder(1);
        generalStep.setStatus(StepStatus.PENDING);
        generalStep.setGeneralService(true);
        
        when(planRepository.findById(1L)).thenReturn(Optional.of(testPlan));
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(generalService));
        when(stepRepository.save(any(TreatmentPlanStep.class))).thenReturn(generalStep);
        
        // Act
        TreatmentPlanStep result = toothService.addGeneralService(1L, 1L, 1);
        
        // Assert
        assertNotNull(result);
        assertNull(result.getToothNumber());
        assertEquals(true, result.isGeneralService());
        assertEquals(new BigDecimal("100000"), result.getActualPrice());
        verify(stepRepository, times(1)).save(any(TreatmentPlanStep.class));
    }
    
    @Test
    @DisplayName("Should calculate total cost correctly")
    void testRecalculatePlanTotalCost_Success() {
        // Arrange
        List<TreatmentPlanStep> steps = new ArrayList<>();
        steps.add(testStep); // 2,000,000
        
        TreatmentPlanStep step2 = new TreatmentPlanStep();
        step2.setActualPrice(new BigDecimal("100000"));
        steps.add(step2); // 100,000
        
        when(stepRepository.findByPlanId(1L)).thenReturn(steps);
        
        // Act
        BigDecimal result = toothService.recalculatePlanTotalCost(1L);
        
        // Assert
        assertEquals(new BigDecimal("2100000"), result);
    }
    
    @Test
    @DisplayName("Should return zero when no steps exist")
    void testRecalculatePlanTotalCost_NoSteps() {
        // Arrange
        when(stepRepository.findByPlanId(1L)).thenReturn(new ArrayList<>());
        
        // Act
        BigDecimal result = toothService.recalculatePlanTotalCost(1L);
        
        // Assert
        assertEquals(BigDecimal.ZERO, result);
    }
    
    @Test
    @DisplayName("Should get services for specific tooth")
    void testGetServicesForTooth_Success() {
        // Arrange
        List<TreatmentPlanStep> steps = new ArrayList<>();
        steps.add(testStep);
        
        when(stepRepository.findByPlanIdAndToothNumber(1L, "8")).thenReturn(steps);
        
        // Act
        List<TreatmentPlanStep> result = toothService.getServicesForTooth(1L, "8");
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("8", result.get(0).getToothNumber());
    }
    
    @Test
    @DisplayName("Should get general services")
    void testGetGeneralServices_Success() {
        // Arrange
        List<TreatmentPlanStep> steps = new ArrayList<>();
        TreatmentPlanStep generalStep = new TreatmentPlanStep();
        generalStep.setGeneralService(true);
        steps.add(generalStep);
        
        when(stepRepository.findByPlanIdAndIsGeneralService(1L, true)).thenReturn(steps);
        
        // Act
        List<TreatmentPlanStep> result = toothService.getGeneralServices(1L);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(true, result.get(0).isGeneralService());
    }
    
    @Test
    @DisplayName("Should remove service successfully")
    void testRemoveService_Success() {
        // Arrange
        when(stepRepository.findById(1L)).thenReturn(Optional.of(testStep));
        when(stepRepository.findByPlanId(1L)).thenReturn(new ArrayList<>());
        
        // Act
        toothService.removeService(1L);
        
        // Assert
        verify(stepRepository, times(1)).delete(testStep);
    }
    
    @Test
    @DisplayName("Should throw exception when step not found for removal")
    void testRemoveService_StepNotFound() {
        // Arrange
        when(stepRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            toothService.removeService(999L);
        });
    }
    
    @Test
    @DisplayName("Should update step price successfully")
    void testUpdateStepPrice_Success() {
        // Arrange
        BigDecimal newPrice = new BigDecimal("1500000");
        when(stepRepository.findById(1L)).thenReturn(Optional.of(testStep));
        when(stepRepository.findByPlanId(1L)).thenReturn(new ArrayList<>());
        
        // Act
        toothService.updateStepPrice(1L, newPrice);
        
        // Assert
        assertEquals(newPrice, testStep.getActualPrice());
        verify(stepRepository, times(1)).save(testStep);
    }
    
    @Test
    @DisplayName("Should throw exception when price is negative")
    void testUpdateStepPrice_NegativePrice() {
        // Arrange
        BigDecimal negativePrice = new BigDecimal("-1000");
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            toothService.updateStepPrice(1L, negativePrice);
        });
    }
    
    @Test
    @DisplayName("Should throw exception when price is null")
    void testUpdateStepPrice_NullPrice() {
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            toothService.updateStepPrice(1L, null);
        });
    }
    
    @Test
    @DisplayName("Should get plan steps ordered by sequence")
    void testGetPlanStepsOrdered_Success() {
        // Arrange
        List<TreatmentPlanStep> steps = new ArrayList<>();
        steps.add(testStep);
        
        when(stepRepository.findByPlanIdOrderBySequenceOrder(1L)).thenReturn(steps);
        
        // Act
        List<TreatmentPlanStep> result = toothService.getPlanStepsOrdered(1L);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
