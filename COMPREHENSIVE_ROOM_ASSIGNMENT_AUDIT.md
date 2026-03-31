# Comprehensive Room Assignment Audit Report

## 🔍 Executive Summary

Sau khi kiểm tra kỹ lưỡng toàn bộ codebase, tôi xác nhận:

**✅ GOOD NEWS:** Hệ thống ĐÃ CÓ logic room assignment ở một số nơi  
**❌ BAD NEWS:** Logic KHÔNG NHẤT QUÁN và CÓ LỖ HỔNG NGHIÊM TRỌNG

---

## 📊 Detailed Findings

### 1. Room Assignment Coverage Matrix

```
┌─────────────────────────────────────────────────────────────────┐
│ Creation Method          │ Room Assignment │ Status             │
├─────────────────────────────────────────────────────────────────┤
│ createFromTemplate()     │ ✅ YES          │ Works (from template) │
│ updateSteps() - new step │ ✅ YES          │ Works (findRoomForService) │
│ addServiceToTooth()      │ ❌ NO           │ BROKEN - NULL room │
│ addGeneralService()      │ ❌ NO           │ BROKEN - NULL room │
└─────────────────────────────────────────────────────────────────┘
```

### 2. Code Analysis by Location

#### A. ✅ TreatmentPlanService.createFromTemplate()

**File:** `TreatmentPlanService.java` (Line 113-120)

```java
for (TreatmentPlanTemplateStep ts : sorted) {
    TreatmentPlanStep step = TreatmentPlanStep.builder()
        .plan(plan)
        .service(ts.getService())
        .clinicRoom(ts.getClinicRoom())  // ✅ Room from template
        .sequenceOrder(ts.getSequenceOrder())
        .status(StepStatus.PENDING)
        .build();
}
```

**Status:** ✅ WORKS  
**Reason:** Template steps already have room assigned

---

#### B. ✅ TreatmentPlanService.updateSteps()

**File:** `TreatmentPlanService.java` (Line 207-223)

```java
// Auto-assign clinic room based on service if not provided
ClinicRoom room = null;
if (item.getClinicRoomId() != null) {
    room = clinicRoomRepository.findById(item.getClinicRoomId()).orElse(null);
} else {
    // Try to find appropriate room based on service name
    room = findRoomForService(svc);  // ✅ Auto-assign
}

TreatmentPlanStep step = TreatmentPlanStep.builder()
    .plan(plan)
    .service(svc)
    .clinicRoom(room)  // ✅ Room assigned
    .sequenceOrder(seq)
    .status(...)
    .build();
```

**Status:** ✅ WORKS  
**Reason:** Has fallback to `findRoomForService()`

**findRoomForService() Implementation:**
```java
private ClinicRoom findRoomForService(Service service) {
    String serviceName = service.getName().toLowerCase();
    
    // X-Quang
    if (serviceName.contains("x-quang") || ...) {
        return clinicRoomRepository.findAll().stream()
            .filter(r -> r.getName().toLowerCase().contains("x-quang"))
            .findFirst().orElse(null);
    }
    
    // Phẫu thuật
    if (serviceName.contains("nhổ răng") || serviceName.contains("phẫu thuật")) {
        return clinicRoomRepository.findAll().stream()
            .filter(r -> r.getName().toLowerCase().contains("phẫu"))
            .findFirst().orElse(null);
    }
    
    // Chỉnh nha
    if (serviceName.contains("niềng") || serviceName.contains("chỉnh nha")) {
        return clinicRoomRepository.findAll().stream()
            .filter(r -> r.getName().toLowerCase().contains("chỉnh nha"))
            .findFirst().orElse(null);
    }
    
    return null; // Default: no room
}
```

---

#### C. ❌ ToothServiceCalculationService.addServiceToTooth()

**File:** `ToothServiceCalculationService.java` (Line 59-70)

```java
TreatmentPlanStep step = TreatmentPlanStep.builder()
    .plan(plan)
    .service(service)
    .toothNumber(toothNumber)
    .actualPrice(service.getPrice())
    .sequenceOrder(sequenceOrder)
    .status(StepStatus.PENDING)
    .isGeneralService(false)
    // ❌ MISSING: .clinicRoom(???)
    .build();
```

**Status:** ❌ BROKEN  
**Impact:** HIGH - Odontogram services have NULL room

---

#### D. ❌ ToothServiceCalculationService.addGeneralService()

**File:** `ToothServiceCalculationService.java` (Line 97-110)

```java
TreatmentPlanStep step = TreatmentPlanStep.builder()
    .plan(plan)
    .service(service)
    .toothNumber(null)
    .actualPrice(service.getPrice())
    .sequenceOrder(sequenceOrder)
    .status(StepStatus.PENDING)
    .isGeneralService(true)
    // ❌ MISSING: .clinicRoom(???)
    .build();
```

**Status:** ❌ BROKEN  
**Impact:** HIGH - General services from odontogram have NULL room

---

### 3. Workflow Impact Analysis

#### Scenario 1: Template-based Treatment Plan ✅

```
Doctor creates plan from template
        ↓
createFromTemplate()
        ↓
Steps have room from template
        ↓
completeStepAndAdvance() checks:
if (nextStep.clinicRoom != null) {
    // ✅ Move patient to next room
}
        ↓
✅ WORKS: Patient auto-moves to X-Ray, Surgery, etc.
```

#### Scenario 2: Odontogram-based Services ❌

```
Doctor clicks tooth on odontogram
        ↓
addServiceToTooth()
        ↓
Step created with NULL room
        ↓
completeStepAndAdvance() checks:
if (nextStep.clinicRoom != null) {  // ❌ NULL!
    // This block NEVER executes
}
        ↓
❌ BROKEN: Patient STUCK, no room transfer
```

#### Scenario 3: Mixed Workflow ⚠️

```
Template step 1: Khám tổng quát (Room 1) ✅
Template step 2: X-Quang (Room X) ✅
Odontogram step 3: Nhổ răng 18 (NULL room) ❌
        ↓
Step 1 complete → Move to Room X ✅
Step 2 complete → Try to move to step 3 room...
        ↓
if (nextStep.clinicRoom != null) {  // ❌ NULL!
    // NEVER executes
}
        ↓
❌ BROKEN: Patient stuck after X-Ray
```

---

### 4. Critical Issues Identified

#### Issue #1: Inconsistent Abstraction ⚠️ CRITICAL

```
Problem: Two different ways to create steps
- TreatmentPlanService: HAS room assignment logic
- ToothServiceCalculationService: NO room assignment logic

Impact: Unpredictable behavior depending on creation method
```

#### Issue #2: Missing Dependency Injection ⚠️ HIGH

```
Problem: ToothServiceCalculationService doesn't have:
- ClinicRoomRepository
- findRoomForService() method

Impact: Cannot assign rooms even if we want to
```

#### Issue #3: Duplicate Logic ⚠️ MEDIUM

```
Problem: findRoomForService() only exists in TreatmentPlanService
- Uses string matching on service names
- Not reusable by other services

Impact: Code duplication if we fix ToothServiceCalculationService
```

#### Issue #4: No Room Type Field ⚠️ MEDIUM

```
Problem: ClinicRoom entity has no roomType field
- Only has name and description
- findRoomForService() uses string matching on names

Impact: Fragile logic, breaks if room names change
```

#### Issue #5: No Service-Room Mapping ⚠️ HIGH

```
Problem: No explicit mapping between Service and ClinicRoom
- Service has category, but no room reference
- ServiceCategory has no room reference
- Mapping is implicit via string matching

Impact: No single source of truth for service-to-room mapping
```

---

### 5. Data Model Analysis

#### Current Schema

```sql
-- services table
CREATE TABLE services (
    id BIGINT PRIMARY KEY,
    category_id BIGINT,  -- FK to service_categories
    name VARCHAR(255),
    -- ❌ NO clinic_room_id
);

-- service_categories table
CREATE TABLE service_categories (
    id BIGINT PRIMARY KEY,
    name VARCHAR(255),
    -- ❌ NO clinic_room_id
);

-- clinic_rooms table
CREATE TABLE clinic_rooms (
    id BIGINT PRIMARY KEY,
    name VARCHAR(255),
    description TEXT,
    -- ❌ NO room_type field
);

-- treatment_plan_steps table
CREATE TABLE treatment_plan_steps (
    id BIGINT PRIMARY KEY,
    service_id BIGINT,
    clinic_room_id BIGINT,  -- ✅ Has room reference
    -- But room can be NULL!
);
```

#### Recommended Schema Enhancement

```sql
-- Add room_type to clinic_rooms
ALTER TABLE clinic_rooms 
ADD COLUMN room_type VARCHAR(50);
-- Values: GENERAL, XRAY, SURGERY, ORTHODONTICS, COSMETIC

-- Add default_room_id to service_categories
ALTER TABLE service_categories 
ADD COLUMN default_room_id BIGINT;
-- FK to clinic_rooms

-- Or add default_room_id to services
ALTER TABLE services 
ADD COLUMN default_room_id BIGINT;
-- FK to clinic_rooms
```

---

### 6. Comparison with Best Practices

#### Current Implementation vs Industry Standard

```
┌────────────────────────────────────────────────────────────┐
│ Aspect              │ Current      │ Best Practice         │
├────────────────────────────────────────────────────────────┤
│ Room Assignment     │ Implicit     │ Explicit mapping      │
│ Service-Room Link   │ String match │ Database FK           │
│ Consistency         │ Inconsistent │ Single source of truth│
│ Extensibility       │ Hard-coded   │ Configuration-driven  │
│ Maintainability     │ Fragile      │ Robust                │
└────────────────────────────────────────────────────────────┘
```

---

## 🎯 Gap Analysis

### What's Missing

1. **Room assignment in ToothServiceCalculationService** ❌
   - addServiceToTooth() doesn't assign room
   - addGeneralService() doesn't assign room

2. **Centralized room assignment logic** ❌
   - findRoomForService() only in TreatmentPlanService
   - Not reusable by other services

3. **Explicit service-to-room mapping** ❌
   - No database field linking service to default room
   - Relies on string matching (fragile)

4. **Room type enumeration** ❌
   - ClinicRoom has no roomType field
   - Cannot query rooms by type

5. **Validation** ❌
   - No validation that step has room before completing
   - No warning if room is NULL

6. **Fallback mechanism** ❌
   - If findRoomForService() returns null, step has no room
   - No fallback to current doctor's room

---

## 🔧 Recommended Fixes

### Priority 1: IMMEDIATE (This Sprint)

#### Fix 1.1: Add Room Assignment to ToothServiceCalculationService

```java
@Service
public class ToothServiceCalculationService {
    
    private final ClinicRoomRepository clinicRoomRepository;
    
    public TreatmentPlanStep addServiceToTooth(...) {
        Service service = serviceRepository.findById(serviceId)...;
        
        // ✅ ADD: Determine room
        ClinicRoom room = findRoomForService(service);
        
        TreatmentPlanStep step = TreatmentPlanStep.builder()
            .service(service)
            .clinicRoom(room)  // ✅ FIXED
            .build();
    }
    
    // ✅ ADD: Copy from TreatmentPlanService
    private ClinicRoom findRoomForService(Service service) {
        // Same logic as TreatmentPlanService
    }
}
```

**Effort:** 2 hours  
**Risk:** Low  
**Impact:** HIGH - Fixes immediate problem

---

### Priority 2: SHORT-TERM (Next Sprint)

#### Fix 2.1: Create ServiceRoomAssignmentService

```java
@Service
public class ServiceRoomAssignmentService {
    
    private final ClinicRoomRepository roomRepository;
    
    /**
     * Single source of truth for room assignment
     */
    public ClinicRoom determineRoomForService(Service service) {
        // Centralized logic
        // Can be enhanced with database mapping later
    }
}
```

#### Fix 2.2: Refactor Existing Services

```java
// TreatmentPlanService
@Service
public class TreatmentPlanService {
    private final ServiceRoomAssignmentService roomAssignmentService;
    
    private ClinicRoom findRoomForService(Service service) {
        return roomAssignmentService.determineRoomForService(service);
    }
}

// ToothServiceCalculationService
@Service
public class ToothServiceCalculationService {
    private final ServiceRoomAssignmentService roomAssignmentService;
    
    public TreatmentPlanStep addServiceToTooth(...) {
        ClinicRoom room = roomAssignmentService
            .determineRoomForService(service);
    }
}
```

**Effort:** 1 day  
**Risk:** Medium  
**Impact:** HIGH - Centralizes logic

---

### Priority 3: LONG-TERM (Future)

#### Fix 3.1: Add Database Schema Enhancements

```sql
-- Migration V4
ALTER TABLE clinic_rooms 
ADD COLUMN room_type VARCHAR(50);

UPDATE clinic_rooms 
SET room_type = 'XRAY' 
WHERE name LIKE '%X-Quang%';

UPDATE clinic_rooms 
SET room_type = 'SURGERY' 
WHERE name LIKE '%Phẫu%';

-- Add default room to service categories
ALTER TABLE service_categories 
ADD COLUMN default_room_id BIGINT;

ALTER TABLE service_categories 
ADD CONSTRAINT fk_category_room 
FOREIGN KEY (default_room_id) REFERENCES clinic_rooms(id);
```

#### Fix 3.2: Update ServiceRoomAssignmentService

```java
public ClinicRoom determineRoomForService(Service service) {
    // Priority 1: Service category default room
    if (service.getCategory().getDefaultRoom() != null) {
        return service.getCategory().getDefaultRoom();
    }
    
    // Priority 2: Query by room type
    String roomType = mapCategoryToRoomType(service.getCategory());
    return roomRepository.findFirstByRoomType(roomType)
        .orElse(null);
    
    // Priority 3: Fallback to string matching (legacy)
    return findRoomByNameMatching(service);
}
```

**Effort:** 1 week  
**Risk:** Medium-High  
**Impact:** HIGH - Robust solution

---

## 📋 Testing Checklist

### Unit Tests Needed

```java
@Test
void testOdontogramServiceHasRoom() {
    // Given
    Service service = createService("Nhổ răng khôn", "Phẫu thuật");
    
    // When
    TreatmentPlanStep step = toothService.addServiceToTooth(...);
    
    // Then
    assertNotNull(step.getClinicRoom());
    assertTrue(step.getClinicRoom().getName().contains("Phẫu"));
}

@Test
void testGeneralServiceHasRoom() {
    // Given
    Service service = createService("X-Quang", "X-Quang");
    
    // When
    TreatmentPlanStep step = toothService.addGeneralService(...);
    
    // Then
    assertNotNull(step.getClinicRoom());
    assertTrue(step.getClinicRoom().getName().contains("X-Quang"));
}

@Test
void testRoomAssignmentConsistency() {
    // Given: Same service
    Service service = createService("X-Quang", "X-Quang");
    
    // When: Create via different methods
    TreatmentPlanStep step1 = createFromTemplate(...);
    TreatmentPlanStep step2 = toothService.addServiceToTooth(...);
    TreatmentPlanStep step3 = planService.updateSteps(...);
    
    // Then: All should have same room type
    assertEquals(
        step1.getClinicRoom().getRoomType(),
        step2.getClinicRoom().getRoomType()
    );
    assertEquals(
        step2.getClinicRoom().getRoomType(),
        step3.getClinicRoom().getRoomType()
    );
}
```

### Integration Tests Needed

```java
@Test
void testOdontogramWorkflowWithRoomTransfer() {
    // 1. Create plan
    // 2. Add service via odontogram
    // 3. Complete step
    // 4. Verify patient moved to correct room
}

@Test
void testMixedWorkflow() {
    // 1. Create plan from template
    // 2. Add odontogram service
    // 3. Complete all steps
    // 4. Verify all room transfers work
}
```

---

## 🎯 Summary & Recommendations

### Current State

```
✅ Template-based: Works (room from template)
✅ Manual add via updateSteps: Works (findRoomForService)
❌ Odontogram tooth service: BROKEN (NULL room)
❌ Odontogram general service: BROKEN (NULL room)
```

### Critical Path Forward

**Week 1: Quick Fix**
- Add room assignment to ToothServiceCalculationService
- Copy findRoomForService() logic
- Test thoroughly

**Week 2-3: Proper Refactoring**
- Create ServiceRoomAssignmentService
- Centralize all room assignment logic
- Update all services to use it

**Month 2: Schema Enhancement**
- Add room_type to clinic_rooms
- Add default_room_id to service_categories
- Migrate from string matching to explicit mapping

### Success Metrics

```
✅ 100% of steps have room assigned
✅ Workflow auto-advance works for all creation methods
✅ No manual intervention needed
✅ Consistent behavior across all entry points
```

---

**Audit Completed By:** Technical Leader  
**Date:** 31/03/2026  
**Priority:** CRITICAL  
**Estimated Total Effort:** 2-3 days (Quick Fix) + 1-2 weeks (Proper Solution)
