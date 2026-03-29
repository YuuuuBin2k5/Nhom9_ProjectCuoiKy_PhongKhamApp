# ✅ FIX: Giữ Step 2 IN_PROGRESS Sau Khi Chuyển Phòng

## 🎯 Vấn Đề

Khi complete step 1:
1. Backend auto-advance step 2 → IN_PROGRESS ✓
2. Thông báo chuyển phòng X-quang ✓
3. Bác sĩ click vào treatment plan lại
4. Step 2 bị reset về PENDING ❌ (SAI!)

## 🔍 Nguyên Nhân

Logic reset IN_PROGRESS steps quá aggressive:

```java
// Logic CŨ (SAI):
if ("IN_PROGRESS".equals(step.getStatus())) {
    if (currentStepId == null || !currentStepId.equals(step.getId())) {
        // Reset TẤT CẢ IN_PROGRESS steps khi currentStepId = null
        step.setStatus("PENDING");  ← SAI!
    }
}
```

**Vấn đề:**
- Khi bác sĩ click vào treatment plan lại, `currentStep = null`
- Logic reset TẤT CẢ IN_PROGRESS steps, kể cả step 2 đang chờ xử lý hợp lệ
- Step 2 bị mất trạng thái IN_PROGRESS

## ✅ Giải Pháp

Chỉ reset khi có NHIỀU HƠN 1 step IN_PROGRESS (trường hợp bất thường):

```java
// Logic MỚI (ĐÚNG):
// Đếm số lượng IN_PROGRESS steps
int inProgressCount = 0;
for (TreatmentPlan.Step step : plan.getSteps()) {
    if ("IN_PROGRESS".equals(step.getStatus())) {
        inProgressCount++;
    }
}

// Nếu có NHIỀU HƠN 1 IN_PROGRESS → reset các step không phải currentStep
if (inProgressCount > 1) {
    for (TreatmentPlan.Step step : plan.getSteps()) {
        if ("IN_PROGRESS".equals(step.getStatus())) {
            if (currentStepId == null || !currentStepId.equals(step.getId())) {
                step.setStatus("PENDING");  // Reset step không hợp lệ
            }
        }
    }
}
// Nếu chỉ có 1 IN_PROGRESS → giữ nguyên (hợp lệ)
else if (inProgressCount == 1) {
    // Không làm gì, giữ nguyên step IN_PROGRESS
}
```

## 📊 So Sánh

### Trước Fix

```
1. Complete step 1
2. Backend: Step 2 → IN_PROGRESS ✓
3. Click vào treatment plan lại
4. Mobile: currentStep = null
5. Mobile reset: Step 2 → PENDING ❌
6. Kết quả: Step 2 mất trạng thái IN_PROGRESS
```

### Sau Fix

```
1. Complete step 1
2. Backend: Step 2 → IN_PROGRESS ✓
3. Click vào treatment plan lại
4. Mobile: currentStep = null
5. Mobile check: Chỉ có 1 IN_PROGRESS step
6. Mobile: Giữ nguyên step 2 = IN_PROGRESS ✓
7. Kết quả: Step 2 vẫn IN_PROGRESS
```

## 🧪 Test Case

### Scenario 1: Complete Step 1 → Step 2 Auto IN_PROGRESS

**Steps:**
1. Complete step 1
2. Nhận thông báo chuyển phòng X-quang
3. Press Back về home
4. Click vào treatment plan lại

**Kết quả mong đợi:**
```
Step 1: COMPLETED ✓
Step 2: IN_PROGRESS ✓ (giữ nguyên)
Step 3: PENDING ✓
```

**Log mong đợi:**
```
Found 1 IN_PROGRESS steps
✓ Single IN_PROGRESS step is valid: Chụp X-quang răng
```

### Scenario 2: Có 2 IN_PROGRESS Steps (Bất thường)

**Setup:**
- Giả sử có bug khiến step 2 và step 3 đều IN_PROGRESS

**Steps:**
1. Load treatment plan

**Kết quả mong đợi:**
```
Found 2 IN_PROGRESS steps
⚠️ Multiple IN_PROGRESS steps detected - resetting invalid ones
⚠️ RESET: Step 3 → PENDING
```

**Kết quả:**
- Step 2: IN_PROGRESS (giữ nếu là currentStep)
- Step 3: PENDING (reset)

## 🎯 Logic Mới

### Rule 1: Chỉ có 1 IN_PROGRESS step
→ Giữ nguyên (đây là step hợp lệ đang chờ xử lý)

### Rule 2: Có nhiều hơn 1 IN_PROGRESS step
→ Reset các step không phải currentStep về PENDING

### Rule 3: Không có IN_PROGRESS step
→ Không làm gì

## 📝 File Đã Sửa

**File:** `mobile_android/app/src/main/java/com/hcmute/mobile_android/ui/activities/staff/DoctorWorkflowActivity.java`

**Method:** `loadTreatmentPlanForRoom()`

**Changes:**
- Đếm số lượng IN_PROGRESS steps
- Chỉ reset khi có > 1 IN_PROGRESS
- Giữ nguyên khi chỉ có 1 IN_PROGRESS

## ✅ Hoàn Thành

Fix đã được implement. Step 2 sẽ giữ nguyên trạng thái IN_PROGRESS sau khi complete step 1 và chuyển phòng.
