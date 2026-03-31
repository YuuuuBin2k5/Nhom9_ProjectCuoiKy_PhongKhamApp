package com.hcmute.clinic.config;

import com.hcmute.clinic.entity.*;
import com.hcmute.clinic.enums.AppointmentStatus;
import com.hcmute.clinic.enums.TreatmentPlanStatus;
import com.hcmute.clinic.enums.StepStatus;
import com.hcmute.clinic.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.LocalDateTime;

/**
 * Development Data Seeder
 * Tự động tạo test data khi server khởi động (chỉ trong môi trường dev)
 * 
 * Mục đích: Đảm bảo appointment 1 luôn có treatment plan và steps để test prescription
 */
@Configuration
@Profile({"dev", "local"}) // Chỉ chạy trong môi trường dev/local
@RequiredArgsConstructor
@Slf4j
public class DevelopmentDataSeeder {

    private final AppointmentRepository appointmentRepository;
    private final TreatmentPlanRepository treatmentPlanRepository;
    private final TreatmentPlanStepRepository treatmentPlanStepRepository;
    private final ServiceRepository serviceRepository;
    private final PatientRepository patientRepository;

    @Bean
    CommandLineRunner seedDevelopmentData() {
        return args -> {
            log.info("=== Development Data Seeder Started ===");
            
            try {
                seedAppointment1Data();
                log.info("=== Development Data Seeder Completed Successfully ===");
            } catch (Exception e) {
                log.error("=== Development Data Seeder Failed ===", e);
            }
        };
    }

    private void seedAppointment1Data() {
        // Kiểm tra appointment 1 có tồn tại không
        Appointment appointment = appointmentRepository.findById(1L).orElse(null);
        
        if (appointment == null) {
            log.warn("Appointment ID 1 không tồn tại. Skip seeding.");
            return;
        }

        log.info("Found Appointment ID 1: Patient={}, Doctor={}, Service={}", 
            appointment.getPatient().getFirstName() + " " + appointment.getPatient().getLastName(),
            appointment.getDoctor().getFirstName() + " " + appointment.getDoctor().getLastName(),
            appointment.getService().getName());

        // Kiểm tra đã có treatment plan chưa
        TreatmentPlan existingPlan = treatmentPlanRepository.findFirstByAppointmentIdOrderByCreatedAtDesc(1L).orElse(null);
        
        if (existingPlan != null) {
            log.info("Treatment plan already exists for Appointment 1 (Plan ID: {})", existingPlan.getId());
            
            // Kiểm tra đã có steps chưa
            long stepCount = treatmentPlanStepRepository.countByPlanId(existingPlan.getId());
            
            if (stepCount > 0) {
                log.info("Treatment plan already has {} steps. No seeding needed.", stepCount);
                logExistingSteps(existingPlan.getId());
                return;
            } else {
                log.info("Treatment plan exists but has no steps. Creating steps...");
                createStepsForPlan(existingPlan, appointment);
            }
        } else {
            log.info("No treatment plan found for Appointment 1. Creating new plan and steps...");
            TreatmentPlan newPlan = createTreatmentPlan(appointment);
            createStepsForPlan(newPlan, appointment);
        }
    }

    private TreatmentPlan createTreatmentPlan(Appointment appointment) {
        TreatmentPlan plan = TreatmentPlan.builder()
                .appointment(appointment)
                .patient(appointment.getPatient())
                .status(TreatmentPlanStatus.IN_PROGRESS)
                .isDraft(false)
                .createdAt(LocalDateTime.now())
                .build();
        
        plan = treatmentPlanRepository.save(plan);
        log.info("✅ Created Treatment Plan ID: {} for Appointment 1", plan.getId());
        
        return plan;
    }

    private void createStepsForPlan(TreatmentPlan plan, Appointment appointment) {
        // Lấy service từ appointment hoặc service đầu tiên có sẵn
        Service service = appointment.getService();
        
        if (service == null) {
            service = serviceRepository.findAll().stream()
                    .filter(Service::isActive)
                    .findFirst()
                    .orElse(null);
        }
        
        if (service == null) {
            log.warn("No active service found. Cannot create steps.");
            return;
        }

        // Tạo 3 steps mẫu để test
        createStep(plan, service, 1, "PENDING", appointment);
        createStep(plan, service, 2, "PENDING", appointment);
        createStep(plan, service, 3, "PENDING", appointment);
        
        log.info("✅ Created 3 treatment plan steps for Plan ID: {}", plan.getId());
        logExistingSteps(plan.getId());
    }

    private void createStep(TreatmentPlan plan, Service service, int sequenceOrder, 
                           String status, Appointment appointment) {
        TreatmentPlanStep step = TreatmentPlanStep.builder()
                .plan(plan)
                .service(service)
                .sequenceOrder(sequenceOrder)
                .status(StepStatus.valueOf(status))
                .appointmentId(appointment.getId())
                .isGeneralService(false)
                .build();
        
        step = treatmentPlanStepRepository.save(step);
        log.info("  → Created Step ID: {}, Sequence: {}, Service: {}", 
            step.getId(), sequenceOrder, service.getName());
    }

    private void logExistingSteps(Long planId) {
        var steps = treatmentPlanStepRepository.findByPlanIdOrderBySequenceOrder(planId);
        
        log.info("📋 Treatment Plan Steps Summary:");
        log.info("   Total steps: {}", steps.size());
        
        for (TreatmentPlanStep step : steps) {
            log.info("   - Step ID: {}, Sequence: {}, Service: {}, Status: {}", 
                step.getId(), 
                step.getSequenceOrder(), 
                step.getService().getName(),
                step.getStatus());
        }
        
        log.info("💡 Use these Step IDs for testing prescription feature in mobile app");
    }
}
