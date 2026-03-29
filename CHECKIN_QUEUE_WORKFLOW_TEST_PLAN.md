# COMPREHENSIVE TEST PLAN: Check-in, Queue, and Workflow Order

## TEST SCOPE
Testing the complete flow from patient check-in → queue management → treatment workflow → room transfers

## DOCUMENTATION REFERENCES
- UC13: Patient Check-in (QR scan, queue number assignment)
- UC18: Doctor Queue Management (FIFO ordering, priority handling)
- UC14: Patient Follow Treatment Path (sequential steps, room transfers)
- UC20: Doctor Create Treatment Plan (template-based workflow)

## CRITICAL REQUIREMENTS TO VERIFY

### 1. QUEUE NUMBER ASSIGNMENT (FIFO)
**Requirement**: Queue numbers must be assigned in FIFO order (First In, First Out)
**Implementation**: `CheckInQueueService.getNextQueueNumber()`
```java
// Line 258-268
private int getNextQueueNumber(Long roomId) {
    LocalDate today = LocalDate.now();
    var todayQueues = checkInQueueRepository.findByClinicRoomIdAndStatusInAndCheckInTimeBetweenOrderByPriorityLevelDescQueueNumberAsc(
            roomId,
            List.of(QueueStatus.WAITING, QueueStatus.IN_PROGRESS, QueueStatus.PAUSED_FOR_TEST, QueueStatus.RETURNED_PRIORITY, QueueStatus.COMPLETED),
            today.atStartOfDay(),
            today.plusDays(1).atStartOfDay());
    int max = todayQueues.stream()
            .mapToInt(CheckInQueue::getQueueNumber)
            .max()
            .orElse(0);
    return max + 1;
}
```

**✅ ANALYSIS**: CORRECT
- Finds max queue number for today in the room
- Returns max + 1 (sequential numbering)
- FIFO is guaranteed by incrementing counter

### 2. QUEUE ORDERING (Priority + Queue Number)
**Requirement**: Queue must be ordered by priority DESC, then queue_number ASC
**Implementation**: `CheckInQueueRepository.findByRoomAndDateRange()`

```java
// CheckInQueueRepository.java Line 42-52
@Query("""
        SELECT q FROM CheckInQueue q
        JOIN FETCH q.appointment a
        JOIN FETCH a.patient
        JOIN FETCH q.clinicRoom
        WHERE q.clinicRoom.id = :roomId
        AND q.checkInTime >= :start
        AND q.checkInTime < :end
        AND q.status IN :statuses
        ORDER BY q.priorityLevel DESC, q.queueNumber ASC
        """)
```

**✅ ANALYSIS**: CORRECT
- Orders by `priorityLevel DESC` first (higher priority first)
- Then by `queueNumber ASC` (FIFO within same priority)
- Priority patients (returned from X-ray) get priorityLevel + 10

### 3. SEQUENTIAL STEP ENFORCEMENT
**Requirement**: Steps must be completed in sequence_order, no skipping allowed
**Implementation**: `TreatmentPlanService.startStep()` and `completeStepAndAdvance()`

```java
// Line 263-275 - startStep() validation
boolean hasPreviousIncomplete = plan.getSteps().stream()
        .filter(s -> s.getSequenceOrder() != null && step.getSequenceOrder() != null)
        .filter(s -> s.getSequenceOrder() < step.getSequenceOrder())
        .anyMatch(s -> s.getStatus() != StepStatus.COMPLETED && s.getStatus() != StepStatus.SKIPPED);

if (hasPreviousIncomplete) {
    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
        "Không thể bắt đầu bước này. Vui lòng hoàn thành các bước trước đó theo thứ tự.");
}
```

**✅ ANALYSIS**: CORRECT
- Checks all previous steps (sequenceOrder < current)
- Blocks if any previous step is not COMPLETED or SKIPPED
- Same validation in `completeStepAndAdvance()` (Line 371-379)

### 4. ROOM TRANSFER ON STEP COMPLETION
**Requirement**: When completing a step, if next step is in different room, transfer patient
**Implementation**: `TreatmentPlanService.completeStepAndAdvance()` Line 430-470

```java
// Line 430-470
ClinicRoom nextRoom = nextStep.getClinicRoom();
if (nextRoom != null && !isNextStepFirst) {
    // Find patient's current queue
    java.util.List<com.hcmute.clinic.entity.CheckInQueue> queues = queueRepo.findTodayForPatient(...);
    com.hcmute.clinic.entity.CheckInQueue activeQueue = queues.stream()
        .filter(q -> q.getStatus() == QueueStatus.IN_PROGRESS || q.getStatus() == QueueStatus.WAITING)
        .findFirst()
        .orElse(null);
    
    if (activeQueue != null && !nextRoom.getId().equals(activeQueue.getClinicRoom().getId())) {
        // Save original room if not set
        if (activeQueue.getOriginalRoomId() == null) {
            activeQueue.setOriginalRoomId(oldRoomId);
        }
        
        // Transfer to next room
        activeQueue.setClinicRoom(nextRoom);
        activeQueue.setStatus(QueueStatus.WAITING);
        activeQueue.setPriorityLevel(activeQueue.getPriorityLevel() + 5); 
        queueRepo.save(activeQueue);
        
        // Send notification
        // Broadcast to both rooms
    }
}
```

**✅ ANALYSIS**: CORRECT
- Finds patient's active queue entry
- Checks if next room is different
- Transfers queue to new room with WAITING status
- Increases priority by +5 (so they get served faster)
- Preserves originalRoomId for tracking
- Broadcasts to both old and new rooms

### 5. PRIORITY HANDLING (X-ray Return)
**Requirement**: Patients returning from X-ray get priority in original room
**Implementation**: `CheckInQueueService.completeXRay()` Line 465-485

```java
@Transactional
public void completeXRay(Long queueId) {
    CheckInQueue q = checkInQueueRepository.findById(queueId)...;
    q.setStatus(QueueStatus.RETURNED_PRIORITY);
    q.setPriorityLevel((q.getPriorityLevel() != null ? q.getPriorityLevel() : 0) + 10);
    var examRoom = q.getAppointment() != null && q.getAppointment().getDoctor() != null
            ? q.getAppointment().getDoctor().getClinicRoom()
            : null;
    if (examRoom != null) {
        q.setClinicRoom(examRoom);
    }
    checkInQueueRepository.save(q);
    // Broadcast priority return event
}
```

**✅ ANALYSIS**: CORRECT
- Sets status to RETURNED_PRIORITY
- Increases priorityLevel by +10 (higher than room transfer +5)
- Returns to original exam room (from appointment.doctor.clinicRoom)
- Broadcasts priority return event to notify doctor


---

## TEST CASES

### CATEGORY A: CHECK-IN AND QUEUE NUMBER ASSIGNMENT

#### TC-A1: Normal Check-in (First Patient)
**Steps**:
1. Patient01 checks in at 8:00 AM
2. Verify queue number = 1
3. Verify status = WAITING
4. Verify priorityLevel = 0

**Expected**: Queue number 1, FIFO order maintained

#### TC-A2: Multiple Patients Check-in (FIFO)
**Steps**:
1. Patient01 checks in at 8:00 AM → Queue #1
2. Patient02 checks in at 8:05 AM → Queue #2
3. Patient03 checks in at 8:10 AM → Queue #3
4. Verify queue order: [1, 2, 3]

**Expected**: Sequential queue numbers, FIFO order

#### TC-A3: Simultaneous Check-in (Race Condition)
**Steps**:
1. Patient01 and Patient02 check in at exactly same time
2. Verify both get unique queue numbers
3. Verify no duplicate numbers

**Expected**: Database transaction isolation prevents duplicates

#### TC-A4: Check-in with Existing Appointment
**Steps**:
1. Patient has appointment for today
2. Scan QR code "CHECKIN:123"
3. Verify queue created with appointment link

**Expected**: Queue linked to appointment, doctor's room assigned

#### TC-A5: Walk-in Patient (No Appointment)
**Steps**:
1. Patient has no appointment today
2. Scan static QR "CLINIC_CHECKIN_RECEPTION"
3. Verify walk-in appointment created
4. Verify queue created with default doctor/room

**Expected**: Auto-create walk-in appointment, assign to default doctor

#### TC-A6: Already Checked-in
**Steps**:
1. Patient checks in → Queue #5
2. Patient scans QR again
3. Verify returns existing queue info
4. Verify no duplicate queue entry

**Expected**: Return existing queue, no new entry created

---

### CATEGORY B: QUEUE ORDERING AND PRIORITY

#### TC-B1: Normal Queue Order (No Priority)
**Setup**: 3 patients waiting, all priorityLevel = 0
**Expected Order**: Queue #1, #2, #3 (FIFO)

#### TC-B2: Priority Patient Returns from X-ray
**Steps**:
1. Patient02 (Queue #2) is sent to X-ray
2. Patient02 completes X-ray
3. Patient02 returns to exam room with priorityLevel = 10
4. Verify queue order: Patient02, Patient03, Patient04

**Expected**: Priority patient jumps to front of queue


#### TC-B3: Multiple Priority Patients
**Setup**:
- Patient01: Queue #1, priorityLevel = 0
- Patient02: Queue #2, priorityLevel = 10 (returned from X-ray)
- Patient03: Queue #3, priorityLevel = 5 (room transfer)
- Patient04: Queue #4, priorityLevel = 0

**Expected Order**: Patient02 (10), Patient03 (5), Patient01 (0), Patient04 (0)
**Reasoning**: Priority DESC, then queue number ASC

#### TC-B4: Delay Patient Functionality
**Steps**:
1. Queue: [Patient01 #1, Patient02 #2, Patient03 #3]
2. Doctor delays Patient01
3. Verify queue order: [Patient02 #2, Patient01 #1, Patient03 #3]

**Expected**: Patient01 swaps position with Patient02

#### TC-B5: Cannot Delay Last Patient
**Steps**:
1. Queue: [Patient01 #1] (only one patient)
2. Doctor tries to delay Patient01
3. Verify no change (no one to swap with)

**Expected**: No error, but no position change

---

### CATEGORY C: SEQUENTIAL WORKFLOW ENFORCEMENT

#### TC-C1: Cannot Skip Steps
**Setup**: Treatment plan with 3 steps (Khám → X-quang → Nhổ răng)
**Steps**:
1. Doctor tries to start Step 3 (Nhổ răng) directly
2. Verify error: "Vui lòng hoàn thành các bước trước đó theo thứ tự"

**Expected**: Blocked, must complete steps in order

#### TC-C2: Cannot Complete Out of Order
**Setup**: Step 1 PENDING, Step 2 PENDING
**Steps**:
1. Doctor tries to complete Step 2 first
2. Verify error: "Không thể hoàn thành bước này..."

**Expected**: Blocked, must complete Step 1 first

#### TC-C3: Normal Sequential Flow
**Steps**:
1. Start Step 1 (Khám) → IN_PROGRESS
2. Complete Step 1 → COMPLETED
3. Step 2 (X-quang) auto-starts → IN_PROGRESS
4. Complete Step 2 → COMPLETED
5. Step 3 (Nhổ răng) auto-starts → IN_PROGRESS

**Expected**: Each step auto-starts after previous completes

#### TC-C4: Skip Step Allowed
**Steps**:
1. Step 1 COMPLETED
2. Doctor skips Step 2 (sets status = SKIPPED)
3. Step 3 can now start

**Expected**: SKIPPED steps don't block next steps

#### TC-C5: Parallel Workflow (Multiple IN_PROGRESS)
**Note**: Current implementation does NOT support parallel steps
**Expected**: Only one step can be IN_PROGRESS at a time


---

### CATEGORY D: ROOM TRANSFER ON STEP COMPLETION

#### TC-D1: Transfer to X-ray Room
**Setup**: Patient in Exam Room 01, next step is X-quang
**Steps**:
1. Doctor completes Step 1 (Khám)
2. Verify patient queue transferred to X-ray room
3. Verify queue status = WAITING
4. Verify priorityLevel increased by +5
5. Verify originalRoomId = Exam Room 01

**Expected**: Patient appears in X-ray room queue with priority

#### TC-D2: Return from X-ray to Original Room
**Steps**:
1. Patient in X-ray room completes X-ray
2. Call `completeXRay(queueId)`
3. Verify patient returns to originalRoomId (Exam Room 01)
4. Verify status = RETURNED_PRIORITY
5. Verify priorityLevel increased by +10

**Expected**: Patient returns to original room with high priority

#### TC-D3: Multi-Room Workflow
**Setup**: Khám (Room 01) → X-quang (X-ray) → Nhổ răng (Surgery)
**Steps**:
1. Complete Khám → Transfer to X-ray (+5 priority)
2. Complete X-quang → Return to Room 01 (+10 priority)
3. Complete consultation → Transfer to Surgery (+5 priority)

**Expected**: Patient moves through 3 rooms with correct priority

#### TC-D4: No Transfer if Same Room
**Setup**: Step 1 and Step 2 both in Room 01
**Steps**:
1. Complete Step 1
2. Verify no room transfer
3. Verify patient stays in Room 01 queue

**Expected**: No transfer, no priority change

#### TC-D5: First Step No Transfer
**Setup**: Treatment plan starts, Step 1 (sequenceOrder = 0)
**Steps**:
1. Complete Step 1
2. Verify `isNextStepFirst` check prevents transfer
3. Patient stays in current room

**Expected**: No transfer on first step completion

---

### CATEGORY E: EDGE CASES AND ERROR HANDLING

#### TC-E1: Invalid QR Code
**Steps**:
1. Scan invalid QR "INVALID_DATA"
2. Verify error: "Mã không hợp lệ"

**Expected**: 400 Bad Request

#### TC-E2: Appointment Not Today
**Steps**:
1. Patient has appointment for tomorrow
2. Try to check in today
3. Verify error: "Lịch hẹn không phải hôm nay"

**Expected**: 400 Bad Request

#### TC-E3: Appointment Belongs to Different Patient
**Steps**:
1. Patient01 tries to check in with Patient02's appointment ID
2. Verify error: "Mã này không thuộc về bạn"

**Expected**: 403 Forbidden


#### TC-E4: No Rooms Configured
**Steps**:
1. Delete all clinic rooms
2. Patient tries to check in
3. Verify error: "Chưa cấu hình phòng khám"

**Expected**: 500 Internal Server Error

#### TC-E5: Complete Already Completed Step
**Steps**:
1. Step 1 status = COMPLETED
2. Doctor tries to complete Step 1 again
3. Verify error: "Bước này đã hoàn thành"

**Expected**: 400 Bad Request

#### TC-E6: Doctor Completes Step in Wrong Room
**Setup**: Doctor in Room 01, Step belongs to Room 02
**Steps**:
1. Doctor tries to complete step
2. Verify error: "Bạn không có quyền hoàn thành bước này"

**Expected**: 403 Forbidden (Line 362-366 in TreatmentPlanService)

#### TC-E7: Plan Already Completed
**Steps**:
1. Complete all steps in plan
2. Plan status = COMPLETED
3. Try to modify any step
4. Verify error: "Hồ sơ đã hoàn tất và bị khóa"

**Expected**: 403 Forbidden

---

## BUGS FOUND

### 🐛 BUG 1: Missing originalRoomId in processSelfScan()
**Location**: `CheckInQueueService.processSelfScan()` Line 630
**Issue**: When creating queue entry, `originalRoomId` is NOT set
```java
CheckInQueue queue = CheckInQueue.builder()
        .appointment(appointment)
        .clinicRoom(room)
        .queueNumber(nextNumber)
        .checkInTime(LocalDateTime.now())
        .status(QueueStatus.WAITING)
        .priorityLevel(0)
        .build(); // ❌ Missing .originalRoomId(room.getId())
```

**Impact**: When patient is transferred to X-ray and returns, `completeXRay()` tries to return to `appointment.doctor.clinicRoom` instead of `originalRoomId`. This could cause issues if doctor's room changed.

**Fix**: Add `.originalRoomId(room.getId())` to builder

---

### 🐛 BUG 2: Missing originalRoomId in processScan()
**Location**: `CheckInQueueService.processScan()` Line 95
**Issue**: Same as Bug 1, but in the reception desk scan method
```java
CheckInQueue queue = CheckInQueue.builder()
        .appointment(appointment)
        .clinicRoom(room)
        .originalRoomId(room.getId()) // ✅ This one is CORRECT
        .queueNumber(nextNumber)
        ...
```

**Status**: ✅ ALREADY FIXED in processScan(), but NOT in processSelfScan()

---

### 🐛 BUG 3: Race Condition in getNextQueueNumber()
**Location**: `CheckInQueueService.getNextQueueNumber()` Line 258
**Issue**: If two patients check in simultaneously, they might get the same queue number
**Scenario**:
1. Patient01 calls getNextQueueNumber() → reads max = 5
2. Patient02 calls getNextQueueNumber() → reads max = 5 (before Patient01 saves)
3. Both get queue number 6

**Fix**: Use database-level sequence or add PESSIMISTIC_WRITE lock


---

## CRITICAL FIXES REQUIRED

### FIX 1: Add originalRoomId to processSelfScan()
**File**: `clinic_backend/src/main/java/com/hcmute/clinic/service/CheckInQueueService.java`
**Line**: 630
**Change**:
```java
CheckInQueue queue = CheckInQueue.builder()
        .appointment(appointment)
        .clinicRoom(room)
        .originalRoomId(room.getId()) // ✅ ADD THIS LINE
        .queueNumber(nextNumber)
        .checkInTime(LocalDateTime.now())
        .status(QueueStatus.WAITING)
        .priorityLevel(0)
        .build();
```

### FIX 2: Add Database Constraint for Queue Number Uniqueness
**File**: `clinic_backend/src/main/java/com/hcmute/clinic/entity/CheckInQueue.java`
**Add**:
```java
@Table(name = "check_in_queue", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"clinic_room_id", "queue_number", "check_in_time"}))
```

This prevents duplicate queue numbers in the same room on the same day.

### FIX 3: Use Pessimistic Lock for Queue Number Generation
**File**: `CheckInQueueRepository.java`
**Add**:
```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT MAX(q.queueNumber) FROM CheckInQueue q WHERE q.clinicRoom.id = :roomId AND q.checkInTime >= :start AND q.checkInTime < :end")
Integer findMaxQueueNumberForToday(@Param("roomId") Long roomId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
```

Then use this in `getNextQueueNumber()` to prevent race conditions.

---

## TEST EXECUTION PLAN

### Phase 1: Unit Tests (Backend)
1. Test `getNextQueueNumber()` with concurrent requests
2. Test queue ordering with different priority levels
3. Test sequential step validation
4. Test room transfer logic

### Phase 2: Integration Tests (API)
1. Test check-in flow end-to-end
2. Test queue management APIs
3. Test treatment plan completion flow
4. Test room transfer notifications

### Phase 3: Manual Testing (Mobile + Backend)
1. Test patient check-in on mobile app
2. Test doctor queue view and call patient
3. Test treatment workflow with room transfers
4. Test X-ray return priority

### Phase 4: Load Testing
1. Simulate 10 patients checking in simultaneously
2. Verify no duplicate queue numbers
3. Verify correct FIFO ordering

---

## SUMMARY

### ✅ CORRECT IMPLEMENTATIONS
1. Queue number FIFO assignment (sequential increment)
2. Queue ordering (priority DESC, queue number ASC)
3. Sequential step enforcement (blocks out-of-order completion)
4. Room transfer on step completion (with priority boost)
5. X-ray return priority (priorityLevel + 10)

### 🐛 BUGS FOUND
1. Missing `originalRoomId` in `processSelfScan()` - CRITICAL
2. Race condition in `getNextQueueNumber()` - HIGH PRIORITY
3. No database constraint for queue number uniqueness - MEDIUM

### 📋 RECOMMENDATIONS
1. Fix Bug 1 immediately (originalRoomId)
2. Add database constraint for queue number uniqueness
3. Consider using database sequence for queue numbers
4. Add integration tests for concurrent check-ins
5. Add monitoring for duplicate queue numbers in production
