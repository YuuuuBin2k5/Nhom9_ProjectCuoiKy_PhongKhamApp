# KIỂM TRA DATABASE - STEP 1 STATUS

## VẤN ĐỀ PHÁT HIỆN

Từ log mobile:
```json
{
  "id":1,
  "status":"COMPLETED",  // ← Plan đã COMPLETED
  "steps":[
    {
      "id":1,
      "status":"IN_PROGRESS",  // ← Step 1 vẫn IN_PROGRESS
      "serviceName":"Khám và tư vấn răng miệng"
    },
    {
      "id":2,
      "status":"COMPLETED",  // ← Step 2 đã COMPLETED
      "serviceName":"Chụp X-quang răng"
    }
  ]
}
```

## NGUYÊN NHÂN

Backend logic trong `completeStepAndAdvance()`:
1. Complete step 2 (X-quang) → status = COMPLETED
2. Tìm step tiếp theo PENDING → Không tìm thấy
3. Set plan status = COMPLETED
4. **NHƯNG step 1 vẫn là IN_PROGRESS!**

## CÂU HỎI

**Tại sao step 1 không được complete trước khi chuyển sang step 2?**

Có 2 khả năng:
1. Bác sĩ Phòng 01 CHƯA nhấn "Hoàn thành" cho step 1
2. Bác sĩ X-quang complete step 2 trước khi step 1 được complete

## KIỂM TRA DATABASE

Chạy query sau để xem trạng thái thực tế:

```sql
-- Xem treatment plan
SELECT id, patient_id, status, is_draft, created_at 
FROM treatment_plans 
WHERE id = 1;

-- Xem các steps
SELECT id, treatment_plan_id, service_id, sequence_order, status, completed_at, doctor_conclusion
FROM treatment_plan_steps 
WHERE treatment_plan_id = 1
ORDER BY sequence_order;

-- Xem queue status
SELECT id, patient_id, clinic_room_id, status, priority_level, original_room_id
FROM check_in_queue
WHERE patient_id = 1 
AND DATE(check_in_time) = CURDATE();
```

## EXPECTED vs ACTUAL

### Expected (nếu workflow đúng):
```
Step 1: COMPLETED (Phòng 01 complete trước)
Step 2: COMPLETED (X-quang complete sau)
Plan: COMPLETED
```

### Actual (từ log):
```
Step 1: IN_PROGRESS (Chưa complete!)
Step 2: COMPLETED (Đã complete)
Plan: COMPLETED (Tự động set vì không còn PENDING)
```

## GIẢI PHÁP

### Option 1: Fix backend logic
Không cho phép complete step nếu có step trước đó chưa complete:

```java
@Transactional
public String completeStepAndAdvance(Long stepId, ...) {
    TreatmentPlanStep currentStep = stepRepository.findById(stepId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bước không tồn tại"));
    
    TreatmentPlan plan = currentStep.getPlan();
    
    // CHECK: Có step nào trước đó chưa complete không?
    boolean hasPreviousIncomplete = plan.getSteps().stream()
            .filter(s -> s.getSequenceOrder() < currentStep.getSequenceOrder())
            .anyMatch(s -> s.getStatus() != StepStatus.COMPLETED && s.getStatus() != StepStatus.CANCELLED);
    
    if (hasPreviousIncomplete) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
            "Không thể hoàn thành bước này khi còn bước trước đó chưa hoàn thành");
    }
    
    // ... rest of logic
}
```

### Option 2: Allow parallel completion
Cho phép các bác sĩ khác phòng complete step của họ độc lập, nhưng chỉ set plan = COMPLETED khi TẤT CẢ steps đều COMPLETED:

```java
// Tìm bước tiếp theo (chỉ PENDING, không IN_PROGRESS)
TreatmentPlanStep nextStep = plan.getSteps().stream()
        .filter(s -> s.getStatus() == StepStatus.PENDING)
        .min(Comparator.comparingInt(s -> s.getSequenceOrder() != null ? s.getSequenceOrder() : 0))
        .orElse(null);

if (nextStep == null) {
    // CHECK: Có step nào còn IN_PROGRESS không?
    boolean hasInProgress = plan.getSteps().stream()
            .anyMatch(s -> s.getStatus() == StepStatus.IN_PROGRESS);
    
    if (!hasInProgress) {
        // Tất cả steps đều COMPLETED hoặc CANCELLED → Complete plan
        plan.setStatus(TreatmentPlanStatus.COMPLETED);
        planRepository.save(plan);
    }
    // Nếu còn IN_PROGRESS → Không complete plan
}
```

## WORKFLOW ĐÚNG NÊN LÀ

1. Bác sĩ Phòng 01: Nhấn "Hoàn thành" step 1
   - Step 1: PENDING → IN_PROGRESS → COMPLETED
   - Step 2: PENDING → IN_PROGRESS
   - Chuyển bệnh nhân sang Phòng X-quang

2. Bác sĩ X-quang: Nhấn "Hoàn thành" step 2
   - Step 2: IN_PROGRESS → COMPLETED
   - Không còn step PENDING
   - Plan: IN_PROGRESS → COMPLETED

## ACTION ITEMS

1. ✅ Kiểm tra database xem step 1 có thực sự IN_PROGRESS không
2. ⏳ Xác định: Bác sĩ Phòng 01 có nhấn "Hoàn thành" step 1 chưa?
3. ⏳ Quyết định: Dùng Option 1 (sequential) hay Option 2 (parallel)?
4. ⏳ Implement fix
5. ⏳ Test lại workflow đầy đủ
