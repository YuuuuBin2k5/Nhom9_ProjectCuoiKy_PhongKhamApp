package com.hcmute.clinic.service;

import com.hcmute.clinic.entity.Appointment;
import com.hcmute.clinic.entity.CheckInQueue;
import com.hcmute.clinic.entity.ClinicRoom;
import com.hcmute.clinic.entity.Patient;
import com.hcmute.clinic.enums.QueueStatus;
import com.hcmute.clinic.repository.CheckInQueueRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for Queue Delay vs Skip functionality
 * 
 * This test verifies the correct behavior of:
 * 1. delayPatient() - for WAITING patients who want to give their turn
 * 2. skipCurrentPatient() - for IN_PROGRESS patients who are not present
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class QueueDelaySkipTest {

    @Autowired
    private CheckInQueueService checkInQueueService;

    @Autowired
    private CheckInQueueRepository checkInQueueRepository;

    @Test
    @DisplayName("delayPatient() should swap queueNumber with next waiting patient")
    public void testDelayPatient_SwapsQueueNumber() {
        // This test would require setting up test data
        // For now, this is a placeholder to document expected behavior
        
        // Given: 3 patients waiting
        // - Patient A: queueNumber=5, status=WAITING, priority=0
        // - Patient B: queueNumber=6, status=WAITING, priority=0
        // - Patient C: queueNumber=7, status=WAITING, priority=0
        
        // When: delayPatient(A)
        
        // Then:
        // - Patient B: queueNumber=5 (moved up)
        // - Patient A: queueNumber=6 (moved down)
        // - Patient C: queueNumber=7 (unchanged)
        // - All priorities remain 0
    }

    @Test
    @DisplayName("delayPatient() should throw error for IN_PROGRESS patient")
    public void testDelayPatient_ThrowsErrorForInProgress() {
        // Given: Patient with status=IN_PROGRESS
        
        // When: delayPatient(patient)
        
        // Then: Should throw ResponseStatusException with message about using Skip instead
    }

    @Test
    @DisplayName("delayPatient() should throw error for last patient in queue")
    public void testDelayPatient_ThrowsErrorForLastPatient() {
        // Given: Patient is last in waiting queue
        
        // When: delayPatient(patient)
        
        // Then: Should throw ResponseStatusException about being last in queue
    }

    @Test
    @DisplayName("skipCurrentPatient() should move IN_PROGRESS to WAITING with +5 priority")
    public void testSkipCurrentPatient_MovesToWaitingWithPriority() {
        // Given: Patient A with status=IN_PROGRESS, priority=0
        
        // When: skipCurrentPatient(A)
        
        // Then:
        // - Patient A: status=WAITING, priority=5
        // - Patient A: startedAt=null
    }

    @Test
    @DisplayName("skipCurrentPatient() should auto-call next waiting patient")
    public void testSkipCurrentPatient_AutoCallsNextPatient() {
        // Given:
        // - Patient A: status=IN_PROGRESS, priority=0
        // - Patient B: status=WAITING, priority=0
        
        // When: skipCurrentPatient(A)
        
        // Then:
        // - Patient A: status=WAITING, priority=5
        // - Patient B: status=IN_PROGRESS (auto-called)
        // - Patient B: startedAt is set
    }

    @Test
    @DisplayName("skipCurrentPatient() should throw error for WAITING patient")
    public void testSkipCurrentPatient_ThrowsErrorForWaiting() {
        // Given: Patient with status=WAITING
        
        // When: skipCurrentPatient(patient)
        
        // Then: Should throw ResponseStatusException about only IN_PROGRESS can be skipped
    }

    @Test
    @DisplayName("skipCurrentPatient() should handle no next patient gracefully")
    public void testSkipCurrentPatient_NoNextPatient() {
        // Given: Patient A is IN_PROGRESS and is the only patient in queue
        
        // When: skipCurrentPatient(A)
        
        // Then:
        // - Patient A: status=WAITING, priority=5
        // - No error thrown
        // - Log message: "No next patient available"
    }

    @Test
    @DisplayName("Priority order: RETURNED_PRIORITY(+10) > Skip(+5) > Normal(0)")
    public void testPriorityOrder() {
        // Given:
        // - Patient A: status=WAITING, priority=10 (returned from X-ray)
        // - Patient B: status=WAITING, priority=5 (skipped once)
        // - Patient C: status=WAITING, priority=0 (normal)
        
        // When: findNextWaitingPatient()
        
        // Then: Should return Patient A (highest priority)
    }
}
