# FIX: BẮT BUỘC THEO TRÌNH TỰ - SEQUENTIAL WORKFLOW

## 🐛 VẤN ĐỀ

Bác sĩ có thể bắt đầu step 2 (X-quang) mà chưa hoàn thành step 1 (Khám và tư vấn), gây ra workflow không đúng thứ tự.

## ✅ GIẢI PHÁP

Thêm validation vào 2 methods:

### 1. `startStep()` - Không cho phép bắt đầu step nếu có step trước chưa complete

```java
@Transactional
public void startStep(Long stepId) {
    TreatmentPlanStep step = stepRepository.findById(stepId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bước điều trị không tồn tại"));
    
    if (step.getPlan() != null && step.getPlan().getStatus() == TreatmentPlanStatus.COMPLETED) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Hồ sơ đã hoàn tất và bị khóa");
    }

    if (step.getStatus() != StepStatus.PENDING) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bước này không ở trạng thái chờ");
    }
    
    // VALIDATION: Kiểm tra các bước trước đó đã hoàn thành chưa
    TreatmentPlan plan = step.getPlan();
    boolean hasPreviousIncomplete = plan.getSteps().stream()
            .filter(s -> s.getSequenceOrder() != null && step.getSequenceOrder() != null)
            .filter(s -> s.getSequenceOrder() < step.getSequenceOrder())
            .anyMatch(s -> s.getStatus() != StepStatus.COMPLETED && s.getStatus() != StepStatus.SKIPPED);
    
    if (hasPreviousIncomplete) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
            "Không thể bắt đầu bước này. Vui lòng hoàn thành các bước trước đó theo thứ tự.");
    }
    
    step.setStatus(StepStatus.IN_PROGRESS);
    stepRepository.save(step);
}
```

### 2. `completeStepAndAdvance()` - Không cho phép complete step nếu có step trước chưa complete

```java
@Transactional
public String completeStepAndAdvance(...) {
    TreatmentPlanStep currentStep = stepRepository.findById(stepId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bước không tồn tại"));

    TreatmentPlan plan = currentStep.getPlan();
    
    // ... existing validations ...
    
    // VALIDATION: Kiểm tra các bước trước đó đã hoàn thành chưa
    boolean hasPreviousIncomplete = plan.getSteps().stream()
            .filter(s -> s.getSequenceOrder() != null && currentStep.getSequenceOrder() != null)
            .filter(s -> s.getSequenceOrder() < currentStep.getSequenceOrder())
            .anyMatch(s -> s.getStatus() != StepStatus.COMPLETED && s.getStatus() != StepStatus.SKIPPED);
    
    if (hasPreviousIncomplete) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
            "Không thể hoàn thành bước này. Vui lòng hoàn thành các bước trước đó theo thứ tự.");
    }
    
    // ... rest of logic ...
}
```

## 📊 WORKFLOW MỚI

### ✅ Đúng thứ tự (Allowed)
```
Step 1: PENDING → IN_PROGRESS (Bác sĩ nhấn "Bắt đầu") ✅
Step 1: IN_PROGRESS → COMPLETED (Bác sĩ nhấn "Hoàn thành") ✅
Step 2: PENDING → IN_PROGRESS (Bác sĩ nhấn "Bắt đầu") ✅
Step 2: IN_PROGRESS → COMPLETED (Bác sĩ nhấn "Hoàn thành") ✅
```

### ❌ Sai thứ tự (Blocked)
```
Step 1: PENDING (chưa bắt đầu)
Step 2: PENDING → IN_PROGRESS ❌ 
Error: "Không thể bắt đầu bước này. Vui lòng hoàn thành các bước trước đó theo thứ tự."
```

```
Step 1: IN_PROGRESS (chưa complete)
Step 2: PENDING → IN_PROGRESS ❌
Error: "Không thể bắt đầu bước này. Vui lòng hoàn thành các bước trước đó theo thứ tự."
```

```
Step 1: IN_PROGRESS (chưa complete)
Step 2: IN_PROGRESS → COMPLETED ❌
Error: "Không thể hoàn thành bước này. Vui lòng hoàn thành các bước trước đó theo thứ tự."
```

## 🎯 LOGIC VALIDATION

```java
boolean hasPreviousIncomplete = plan.getSteps().stream()
    .filter(s -> s.getSequenceOrder() != null && currentStep.getSequenceOrder() != null)
    .filter(s -> s.getSequenceOrder() < currentStep.getSequenceOrder())  // Chỉ check steps trước
    .anyMatch(s -> s.getStatus() != StepStatus.COMPLETED && s.getStatus() != StepStatus.SKIPPED);
```

**Giải thích:**
- Lọc các steps có `sequenceOrder` < `currentStep.sequenceOrder`
- Kiểm tra xem có step nào KHÔNG phải COMPLETED hoặc SKIPPED
- Nếu có → Reject với error message

## 🧪 TEST CASES

### Test 1: Bắt đầu step 2 khi step 1 chưa complete
```
GIVEN: 
  - Step 1: IN_PROGRESS (sequenceOrder=0)
  - Step 2: PENDING (sequenceOrder=1)
  
WHEN: Bác sĩ X-quang nhấn "Bắt đầu" step 2

THEN:
  - API /api/treatment-plans/steps/{step2Id}/start
  - Response: 400 Bad Request
  - Message: "Không thể bắt đầu bước này. Vui lòng hoàn thành các bước trước đó theo thứ tự."
  - Step 2 vẫn PENDING
```

### Test 2: Complete step 2 khi step 1 chưa complete
```
GIVEN:
  - Step 1: IN_PROGRESS (sequenceOrder=0)
  - Step 2: IN_PROGRESS (sequenceOrder=1) - somehow started

WHEN: Bác sĩ X-quang nhấn "Hoàn thành" step 2

THEN:
  - API /api/treatment-plans/steps/{step2Id}/complete
  - Response: 400 Bad Request
  - Message: "Không thể hoàn thành bước này. Vui lòng hoàn thành các bước trước đó theo thứ tự."
  - Step 2 vẫn IN_PROGRESS
```

### Test 3: Workflow đúng thứ tự
```
GIVEN:
  - Step 1: PENDING (sequenceOrder=0)
  - Step 2: PENDING (sequenceOrder=1)

WHEN: 
  1. Bác sĩ Phòng 01 nhấn "Bắt đầu" step 1 → SUCCESS
  2. Bác sĩ Phòng 01 nhấn "Hoàn thành" step 1 → SUCCESS
  3. Bác sĩ X-quang nhấn "Bắt đầu" step 2 → SUCCESS
  4. Bác sĩ X-quang nhấn "Hoàn thành" step 2 → SUCCESS

THEN:
  - Step 1: COMPLETED
  - Step 2: COMPLETED
  - Plan: COMPLETED
  - Workflow hoàn tất
```

## 📝 FILES MODIFIED

1. `clinic_backend/src/main/java/com/hcmute/clinic/service/TreatmentPlanService.java`
   - Method: `startStep()` - Added sequential validation
   - Method: `completeStepAndAdvance()` - Added sequential validation

## ✅ STATUS

- [x] Backend compiled successfully
- [ ] Test scenario 1: Block start step 2 when step 1 incomplete
- [ ] Test scenario 2: Block complete step 2 when step 1 incomplete
- [ ] Test scenario 3: Allow sequential workflow
- [ ] Verify mobile shows error message correctly

## 🚀 NEXT STEPS

1. Restart backend
2. Test với mobile:
   - Tạo plan với 2 steps
   - Thử bắt đầu step 2 trước step 1 → Expect error
   - Complete step 1 trước
   - Bắt đầu step 2 → Expect success
3. Verify error message hiển thị đúng trên mobile

## 💡 LƯU Ý

- Validation này áp dụng cho TẤT CẢ steps, không chỉ step 1 và 2
- Nếu có 3+ steps, phải complete theo thứ tự: 1 → 2 → 3
- SKIPPED steps được coi là "done" (không block steps sau)
- Validation dựa trên `sequenceOrder`, không phải `id`
