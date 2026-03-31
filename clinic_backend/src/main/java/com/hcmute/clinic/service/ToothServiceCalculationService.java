package com.hcmute.clinic.service;

import com.hcmute.clinic.entity.TreatmentPlan;
import com.hcmute.clinic.entity.TreatmentPlanStep;
import com.hcmute.clinic.entity.Service;
import com.hcmute.clinic.entity.ClinicRoom;
import com.hcmute.clinic.enums.StepStatus;
import com.hcmute.clinic.repository.TreatmentPlanStepRepository;
import com.hcmute.clinic.repository.TreatmentPlanRepository;
import com.hcmute.clinic.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service for managing tooth-specific and general services in treatment plans
 * Handles adding, removing, and calculating costs for services
 * 
 * REFACTORED: Now uses ServiceRoomAssignmentService for centralized room assignment logic
 */
@org.springframework.stereotype.Service
@RequiredArgsConstructor
@Slf4j
public class ToothServiceCalculationService {
    
    private final TreatmentPlanStepRepository stepRepository;
    private final TreatmentPlanRepository planRepository;
    private final ServiceRepository serviceRepository;
    private final ServiceRoomAssignmentService roomAssignmentService;
    
    /**
     * Add a service to a specific tooth
     * @param planId Treatment plan ID
     * @param serviceId Service ID
     * @param toothNumber Tooth number (FDI notation: "8", "16", etc.)
     * @param sequenceOrder Order of this step
     * @return Created TreatmentPlanStep
     */
    @Transactional
    public TreatmentPlanStep addServiceToTooth(
        Long planId,
        Long serviceId,
        String toothNumber,
        Integer sequenceOrder
    ) {
        log.info("Adding service {} to tooth {} in plan {}", serviceId, toothNumber, planId);
        
        TreatmentPlan plan = planRepository.findById(planId)
            .orElseThrow(() -> new RuntimeException("Treatment plan not found: " + planId));
        
        Service service = serviceRepository.findById(serviceId)
            .orElseThrow(() -> new RuntimeException("Service not found: " + serviceId));
        
        // Validate tooth number
        if (toothNumber == null || toothNumber.trim().isEmpty()) {
            throw new RuntimeException("Tooth number cannot be empty");
        }
        
        // REFACTORED: Use centralized room assignment service
        ClinicRoom room = roomAssignmentService.determineRoomForService(service);
        log.info("Room assignment: {}", roomAssignmentService.explainRoomAssignment(service, room));
        
        // Create step for specific tooth
        TreatmentPlanStep step = TreatmentPlanStep.builder()
            .plan(plan)
            .service(service)
            .clinicRoom(room)  // FIXED: Now assigns room via centralized service
            .toothNumber(toothNumber)
            .actualPrice(service.getPrice())
            .sequenceOrder(sequenceOrder)
            .status(StepStatus.PENDING)
            .isGeneralService(false)  // Mark as tooth-specific
            .build();
        
        TreatmentPlanStep savedStep = stepRepository.save(step);
        log.info("Service added successfully. Step ID: {}", savedStep.getId());
        
        return savedStep;
    }
    
    /**
     * Add a general service (not specific to a tooth)
     * @param planId Treatment plan ID
     * @param serviceId Service ID
     * @param sequenceOrder Order of this step
     * @return Created TreatmentPlanStep
     */
    @Transactional
    public TreatmentPlanStep addGeneralService(
        Long planId,
        Long serviceId,
        Integer sequenceOrder
    ) {
        log.info("Adding general service {} to plan {}", serviceId, planId);
        
        TreatmentPlan plan = planRepository.findById(planId)
            .orElseThrow(() -> new RuntimeException("Treatment plan not found: " + planId));
        
        Service service = serviceRepository.findById(serviceId)
            .orElseThrow(() -> new RuntimeException("Service not found: " + serviceId));
        
        // REFACTORED: Use centralized room assignment service
        ClinicRoom room = roomAssignmentService.determineRoomForService(service);
        log.info("Room assignment: {}", roomAssignmentService.explainRoomAssignment(service, room));
        
        // Create step for general service
        TreatmentPlanStep step = TreatmentPlanStep.builder()
            .plan(plan)
            .service(service)
            .clinicRoom(room)  // FIXED: Now assigns room via centralized service
            .toothNumber(null)  // No specific tooth
            .actualPrice(service.getPrice())
            .sequenceOrder(sequenceOrder)
            .status(StepStatus.PENDING)
            .isGeneralService(true)  // Mark as general
            .build();
        
        TreatmentPlanStep savedStep = stepRepository.save(step);
        log.info("General service added successfully. Step ID: {}", savedStep.getId());
        
        return savedStep;
    }
    
    /**
     * Recalculate total cost of a treatment plan
     * @param planId Treatment plan ID
     * @return Total cost
     */
    @Transactional
    public BigDecimal recalculatePlanTotalCost(Long planId) {
        log.info("Recalculating total cost for plan {}", planId);
        
        List<TreatmentPlanStep> steps = stepRepository.findByPlanId(planId);
        
        // CRITICAL FIX: Filter out null prices before summing
        BigDecimal totalCost = steps.stream()
            .map(TreatmentPlanStep::getActualPrice)
            .filter(price -> price != null)  // Filter null values
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        log.info("Total cost for plan {}: {}", planId, totalCost);
        return totalCost;
    }
    
    /**
     * Get all services for a specific tooth
     * @param planId Treatment plan ID
     * @param toothNumber Tooth number
     * @return List of TreatmentPlanSteps for this tooth
     */
    public List<TreatmentPlanStep> getServicesForTooth(Long planId, String toothNumber) {
        log.info("Getting services for tooth {} in plan {}", toothNumber, planId);
        return stepRepository.findByPlanIdAndToothNumber(planId, toothNumber);
    }
    
    /**
     * Get all general services for a plan
     * @param planId Treatment plan ID
     * @return List of general TreatmentPlanSteps
     */
    public List<TreatmentPlanStep> getGeneralServices(Long planId) {
        log.info("Getting general services for plan {}", planId);
        return stepRepository.findByPlanIdAndIsGeneralService(planId, true);
    }
    
    /**
     * Remove a service from a treatment plan
     * @param stepId TreatmentPlanStep ID
     */
    @Transactional
    public void removeService(Long stepId) {
        log.info("Removing service from step {}", stepId);
        
        TreatmentPlanStep step = stepRepository.findById(stepId)
            .orElseThrow(() -> new RuntimeException("Step not found: " + stepId));
        
        Long planId = step.getPlan().getId();
        stepRepository.delete(step);
        
        log.info("Service removed. Recalculating plan cost...");
        recalculatePlanTotalCost(planId);
    }
    
    /**
     * Update price for a step
     * @param stepId TreatmentPlanStep ID
     * @param newPrice New price
     */
    @Transactional
    public void updateStepPrice(Long stepId, BigDecimal newPrice) {
        log.info("Updating price for step {} to {}", stepId, newPrice);
        
        if (newPrice == null || newPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Price must be positive");
        }
        
        TreatmentPlanStep step = stepRepository.findById(stepId)
            .orElseThrow(() -> new RuntimeException("Step not found: " + stepId));
        
        step.setActualPrice(newPrice);
        stepRepository.save(step);
        
        log.info("Price updated. Recalculating plan cost...");
        recalculatePlanTotalCost(step.getPlan().getId());
    }
    
    /**
     * Get all steps of a plan ordered by sequence
     * @param planId Treatment plan ID
     * @return List of TreatmentPlanSteps ordered by sequence
     */
    public List<TreatmentPlanStep> getPlanStepsOrdered(Long planId) {
        return stepRepository.findByPlanIdOrderBySequenceOrder(planId);
    }
}
