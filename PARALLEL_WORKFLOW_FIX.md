# FIX: PARALLEL WORKFLOW - CHO PHÉP COMPLETE STEPS ĐỘC LẬP

## 🐛 VẤN ĐỀ

Khi bác sĩ X-quang complete step 2 trước khi bác sĩ Phòng 01 complete step 1:
- Step 1: IN_PROGRESS (chưa complete)
- Step 2: COMPLETED (đã complete)
- Plan: COMPLETED (tự động set vì không còn PENDING)
- Mobile auto-save bị reject: "Hồ sơ đã hoàn tất và bị khóa"

## 🔍 NGUYÊN NHÂN

Backend logic cũ:
```java
if (nextStep == null) {
    // Không còn PENDING → Complete plan ngay
    plan.setStatus(TreatmentPlanStatus.COMPLETED);
}
```

Điều này gây ra vấn đề khi:
1. Step 2 complete → Không còn PENDING
2. Plan set = COMPLETED
3. Nhưng step 1 vẫn IN_PROGRESS!
4. Mobile auto-save step 1 → Backend reject vì plan đã COMPLETED

## ✅ GIẢI PHÁP

### 1. Fix `completeStepAndAdvance()` - Chỉ complete plan khi TẤT CẢ steps done

```java
if (nextStep == null) {
    // Không còn PENDING steps
    // CHECK: Có step nào còn IN_PROGRESS không?
    boolean hasInProgress = plan.getSteps().stream()
            .anyMatch(s -> s.getStatus() == StepStatus.IN_PROGRESS);
    
    if (!hasInProgress) {
        // Tất cả steps đều COMPLETED hoặc CANCELLED → Complete plan
        plan.setStatus(TreatmentPlanStatus.COMPLETED);
        planRepository.save(plan);
        // Send notification...
    }
    // Nếu còn IN_PROGRESS → Không complete plan
    return null;
}
```

### 2. Fix `updateSteps()` - Cho phép update khi plan COMPLETED nhưng có IN_PROGRESS steps

```java
@Transactional
public TreatmentPlan updateSteps(Long planId, UpdatePlanStepsRequest request) {
    TreatmentPlan plan = planRepository.findById(planId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Phác đồ không tồn tại"));

    // Allow updates if plan is COMPLETED but has IN_PROGRESS steps (parallel workflow)
    boolean hasInProgressSteps = plan.getSteps() != null && plan.getSteps().stream()
            .anyMatch(s -> s.getStatus() == StepStatus.IN_PROGRESS);
    
    if (plan.getStatus() == TreatmentPlanStatus.COMPLETED && !hasInProgressSteps) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Hồ sơ đã hoàn tất và bị khóa, không thể chỉnh sửa");
    }
    
    // ... rest of logic
}
```

### 3. Fix Controller - Tương tự

```java
@PutMapping("/{id}")
public ResponseEntity<?> updateSteps(@PathVariable Long id, @RequestBody UpdatePlanStepsRequest request) {
    try {
        TreatmentPlan plan = treatmentPlanService.getById(id);
        
        // Allow updates if plan is COMPLETED but has IN_PROGRESS steps (parallel workflow)
        boolean hasInProgressSteps = plan.getSteps() != null && plan.getSteps().stream()
                .anyMatch(s -> s.getStatus() == com.hcmute.clinic.enums.StepStatus.IN_PROGRESS);
        
        if (plan.getStatus() == com.hcmute.clinic.enums.TreatmentPlanStatus.COMPLETED && !hasInProgressSteps) {
            return ResponseEntity.badRequest().body(Map.of("message", "Hồ sơ đã hoàn tất và bị khóa, không thể cập nhật"));
        }
        
        treatmentPlanService.updateSteps(id, request);
        return ResponseEntity.ok(Map.of("message", "Đã cập nhật phác đồ"));
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }
}
```

## 📊 WORKFLOW MỚI

### Scenario 1: Sequential (Đúng thứ tự)
1. Bác sĩ Phòng 01 complete step 1
   - Step 1: PENDING → IN_PROGRESS → COMPLETED ✅
   - Step 2: PENDING → IN_PROGRESS
   - Plan: IN_PROGRESS
   - Chuyển bệnh nhân sang X-quang

2. Bác sĩ X-quang complete step 2
   - Step 2: IN_PROGRESS → COMPLETED ✅
   - Không còn PENDING, không còn IN_PROGRESS
   - Plan: IN_PROGRESS → COMPLETED ✅

### Scenario 2: Parallel (Không đúng thứ tự)
1. Bác sĩ X-quang complete step 2 TRƯỚC
   - Step 1: IN_PROGRESS (chưa complete)
   - Step 2: PENDING → IN_PROGRESS → COMPLETED ✅
   - Không còn PENDING, NHƯNG còn step 1 IN_PROGRESS
   - Plan: IN_PROGRESS (KHÔNG complete) ✅

2. Bác sĩ Phòng 01 complete step 1 SAU
   - Step 1: IN_PROGRESS → COMPLETED ✅
   - Mobile auto-save → Backend ACCEPT (vì còn IN_PROGRESS) ✅
   - Không còn PENDING, không còn IN_PROGRESS
   - Plan: IN_PROGRESS → COMPLETED ✅

## 🎯 LỢI ÍCH

1. ✅ Cho phép các bác sĩ khác phòng làm việc độc lập
2. ✅ Không bị reject khi auto-save
3. ✅ Plan chỉ complete khi TẤT CẢ steps thực sự done
4. ✅ Linh hoạt hơn trong workflow thực tế

## 🧪 TEST CASES

### Test 1: Complete step 2 trước step 1
```
GIVEN: Step 1 IN_PROGRESS, Step 2 IN_PROGRESS
WHEN: Bác sĩ X-quang complete step 2
THEN: 
  - Step 2 = COMPLETED
  - Plan = IN_PROGRESS (vì step 1 còn IN_PROGRESS)
  - Mobile auto-save step 1 → SUCCESS
```

### Test 2: Complete step 1 sau khi step 2 đã complete
```
GIVEN: Step 1 IN_PROGRESS, Step 2 COMPLETED, Plan IN_PROGRESS
WHEN: Bác sĩ Phòng 01 complete step 1
THEN:
  - Step 1 = COMPLETED
  - Không còn IN_PROGRESS
  - Plan = COMPLETED
  - Notification sent
```

### Test 3: Auto-save khi plan COMPLETED nhưng có IN_PROGRESS
```
GIVEN: Step 1 IN_PROGRESS, Step 2 COMPLETED, Plan COMPLETED (bug cũ)
WHEN: Mobile auto-save step 1
THEN:
  - Backend ACCEPT (vì step 1 còn IN_PROGRESS)
  - Update thành công
```

## 📝 FILES MODIFIED

1. `clinic_backend/src/main/java/com/hcmute/clinic/service/TreatmentPlanService.java`
   - Method: `completeStepAndAdvance()` - Line ~295
   - Method: `updateSteps()` - Line ~145

2. `clinic_backend/src/main/java/com/hcmute/clinic/controller/TreatmentPlanController.java`
   - Method: `updateSteps()` - Line ~95

## ✅ STATUS

- [x] Backend compiled successfully
- [ ] Test scenario 1 (sequential)
- [ ] Test scenario 2 (parallel)
- [ ] Verify database state
- [ ] Test mobile auto-save

## 🚀 NEXT STEPS

1. Restart backend
2. Test workflow đầy đủ:
   - Complete step 2 trước
   - Verify plan vẫn IN_PROGRESS
   - Complete step 1 sau
   - Verify plan chuyển sang COMPLETED
3. Kiểm tra mobile auto-save không bị reject
