package com.hcmute.clinic.controller;

import com.hcmute.clinic.dto.*;
import com.hcmute.clinic.entity.TreatmentPlanStep;
import com.hcmute.clinic.service.ToothServiceCalculationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for managing tooth-specific and general services in treatment plans
 * Handles adding, removing, and calculating costs for services
 */
@RestController
@RequestMapping("/api/treatment-plans/{planId}/services")
@RequiredArgsConstructor
@Slf4j
public class ToothServiceController {
    
    private final ToothServiceCalculationService toothService;
    
    /**
     * Add a service to a specific tooth
     * POST /api/treatment-plans/1/services/teeth/8
     */
    @PostMapping("/teeth/{toothNumber}")
    public ResponseEntity<?> addServiceToTooth(
        @PathVariable Long planId,
        @PathVariable String toothNumber,
        @RequestBody AddToothServiceRequest request
    ) {
        log.info("POST /api/treatment-plans/{}/services/teeth/{}", planId, toothNumber);
        
        try {
            TreatmentPlanStep step = toothService.addServiceToTooth(
                planId,
                request.getServiceId(),
                toothNumber,
                request.getSequenceOrder()
            );
            
            BigDecimal totalCost = toothService.recalculatePlanTotalCost(planId);
            
            ToothServiceResponse response = new ToothServiceResponse(
                step.getId(),
                toothNumber,
                step.getService().getName(),
                step.getActualPrice(),
                totalCost
            );
            
            log.info("Service added successfully. Step ID: {}", step.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error adding service to tooth", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Add a service to multiple teeth at once
     * POST /api/treatment-plans/1/services/teeth/bulk
     * 
     * Used for services like crown, extraction that can be applied to multiple teeth
     */
    @PostMapping("/teeth/bulk")
    public ResponseEntity<?> addServiceToMultipleTeeth(
        @PathVariable Long planId,
        @RequestBody AddMultipleTeethServiceRequest request
    ) {
        log.info("POST /api/treatment-plans/{}/services/teeth/bulk - {} teeth", 
            planId, request.getToothNumbers() != null ? request.getToothNumbers().size() : 0);
        
        try {
            List<TreatmentPlanStep> steps = toothService.addServiceToMultipleTeeth(
                planId,
                request.getServiceId(),
                request.getToothNumbers(),
                request.getStartingSequenceOrder(),
                request.getNotes(),
                request.getCustomPrice()  // Pass custom price to service
            );
            
            BigDecimal totalCost = toothService.recalculatePlanTotalCost(planId);
            
            // Build response
            List<MultipleTeethServiceResponse.StepInfo> stepInfos = steps.stream()
                .map(step -> MultipleTeethServiceResponse.StepInfo.builder()
                    .stepId(step.getId())
                    .toothNumber(step.getToothNumber())
                    .serviceName(step.getService().getName())
                    .price(step.getActualPrice())
                    .build())
                .collect(java.util.stream.Collectors.toList());
            
            MultipleTeethServiceResponse response = MultipleTeethServiceResponse.builder()
                .createdSteps(stepInfos)
                .totalPlanCost(totalCost)
                .message("Successfully added service to " + steps.size() + " teeth")
                .build();
            
            log.info("Service added to {} teeth successfully", steps.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error adding service to multiple teeth", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Add a general service (not specific to a tooth)
     * POST /api/treatment-plans/1/services/general
     */
    @PostMapping("/general")
    public ResponseEntity<?> addGeneralService(
        @PathVariable Long planId,
        @RequestBody AddGeneralServiceRequest request
    ) {
        log.info("POST /api/treatment-plans/{}/services/general", planId);
        
        try {
            TreatmentPlanStep step = toothService.addGeneralService(
                planId,
                request.getServiceId(),
                request.getSequenceOrder()
            );
            
            BigDecimal totalCost = toothService.recalculatePlanTotalCost(planId);
            
            GeneralServiceResponse response = new GeneralServiceResponse(
                step.getId(),
                step.getService().getName(),
                step.getActualPrice(),
                totalCost
            );
            
            log.info("General service added successfully. Step ID: {}", step.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error adding general service", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get all services for a specific tooth
     * GET /api/treatment-plans/1/services/teeth/8
     */
    @GetMapping("/teeth/{toothNumber}")
    public ResponseEntity<?> getServicesForTooth(
        @PathVariable Long planId,
        @PathVariable String toothNumber
    ) {
        log.info("GET /api/treatment-plans/{}/services/teeth/{}", planId, toothNumber);
        
        try {
            List<TreatmentPlanStep> steps = toothService.getServicesForTooth(planId, toothNumber);
            log.info("Found {} services for tooth {}", steps.size(), toothNumber);
            return ResponseEntity.ok(steps);
        } catch (Exception e) {
            log.error("Error getting services for tooth", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get all general services for a plan
     * GET /api/treatment-plans/1/services/general
     */
    @GetMapping("/general")
    public ResponseEntity<?> getGeneralServices(@PathVariable Long planId) {
        log.info("GET /api/treatment-plans/{}/services/general", planId);
        
        try {
            List<TreatmentPlanStep> steps = toothService.getGeneralServices(planId);
            log.info("Found {} general services", steps.size());
            return ResponseEntity.ok(steps);
        } catch (Exception e) {
            log.error("Error getting general services", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Remove a service from a treatment plan
     * DELETE /api/treatment-plans/1/services/steps/123
     */
    @DeleteMapping("/steps/{stepId}")
    public ResponseEntity<?> removeService(
        @PathVariable Long planId,
        @PathVariable Long stepId
    ) {
        log.info("DELETE /api/treatment-plans/{}/services/steps/{}", planId, stepId);
        
        try {
            toothService.removeService(stepId);
            BigDecimal totalCost = toothService.recalculatePlanTotalCost(planId);
            
            log.info("Service removed successfully");
            return ResponseEntity.ok(Map.of(
                "message", "Service removed successfully",
                "totalPlanCost", totalCost
            ));
        } catch (Exception e) {
            log.error("Error removing service", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Update price for a step
     * PUT /api/treatment-plans/1/services/steps/123/price
     */
    @PutMapping("/steps/{stepId}/price")
    public ResponseEntity<?> updateStepPrice(
        @PathVariable Long planId,
        @PathVariable Long stepId,
        @RequestBody UpdatePriceRequest request
    ) {
        log.info("PUT /api/treatment-plans/{}/services/steps/{}/price", planId, stepId);
        
        try {
            toothService.updateStepPrice(stepId, request.getNewPrice());
            BigDecimal totalCost = toothService.recalculatePlanTotalCost(planId);
            
            log.info("Price updated successfully");
            return ResponseEntity.ok(Map.of(
                "message", "Price updated successfully",
                "newPrice", request.getNewPrice(),
                "totalPlanCost", totalCost
            ));
        } catch (Exception e) {
            log.error("Error updating price", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get all steps of a plan ordered by sequence
     * GET /api/treatment-plans/1/services/all
     */
    @GetMapping("/all")
    public ResponseEntity<?> getAllSteps(@PathVariable Long planId) {
        log.info("GET /api/treatment-plans/{}/services/all", planId);
        
        try {
            List<TreatmentPlanStep> steps = toothService.getPlanStepsOrdered(planId);
            BigDecimal totalCost = toothService.recalculatePlanTotalCost(planId);
            
            log.info("Found {} steps", steps.size());
            return ResponseEntity.ok(Map.of(
                "steps", steps,
                "totalCost", totalCost
            ));
        } catch (Exception e) {
            log.error("Error getting all steps", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
