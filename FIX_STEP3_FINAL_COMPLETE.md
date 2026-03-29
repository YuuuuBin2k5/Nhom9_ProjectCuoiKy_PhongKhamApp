# ✅ FIX STEP 3 AUTO-ADVANCE - HOÀN THÀNH

## 🎯 Vấn Đề Đã Fix

Khi edit step 1 (đã COMPLETED) và click "Hoàn thành bước", step 3 tự động chuyển sang IN_PROGRESS (SAI!).

## 🔍 Nguyên Nhân Gốc Rễ

### Vấn Đề 1: Mobile gửi status SAI
Khi re-complete step đã COMPLETED, mobile gửi status=IN_PROGRESS thay vì COMPLETED.

**Log lỗi:**
```
editingPreviouslyCompletedStep: true
✓ Re-completing previously COMPLETED step
→ Step 1: EDITING - status=IN_PROGRESS  ← SAI!
```

### Vấn Đề 2: Backend auto-advance
Backend nhận step với status=IN_PROGRESS, nghĩ là complete lần đầu, nên auto-advance sang step tiếp theo.

## ✅ Giải Pháp 3 Lớp

### Layer 1: Mobile - Set COMPLETED trước khi save

**File:** `DoctorWorkflowActivity.java`
**Method:** `onStepComplete()`

```java
if (editingPreviouslyCompletedStep) {
    android.util.Log.d("DoctorWorkflow", "✓ Re-completing previously COMPLETED step - saving without auto-advance");
    
    // PROFESSIONAL FIX: Set status to COMPLETED BEFORE saving
    step.setStatus("COMPLETED");
    step.setDoctorConclusion(finalData);
    
    // Save data with COMPLETED status
    saveTreatmentPlanInternal(true, () -> {
        // ... UI updates ...
        loadTreatmentPlanForRoom(currentTreatmentPlanId);
    });
}
```

**Logic:**
- Detect step đã COMPLETED trước đó (flag `editingPreviouslyCompletedStep`)
- Set status=COMPLETED TRƯỚC khi gọi save
- Không gọi `/complete` API (tránh auto-advance)

### Layer 2: Backend - Skip auto-advance khi re-complete

**File:** `TreatmentPlanService.java`
**Method:** `completeStepAndAdvance()`

```java
// PROFESSIONAL FIX: Cho phép re-complete một step đã COMPLETED (để edit)
// NHƯNG: Không auto-advance sang step tiếp theo nếu đang re-complete
boolean isReCompleting = (currentStep.getStatus() == StepStatus.COMPLETED);

// ... save step ...

// PROFESSIONAL FIX: Nếu đang re-complete một step đã COMPLETED, không auto-advance
if (isReCompleting) {
    System.out.println("TreatmentPlanService: Re-completing step " + stepId + " - không auto-advance");
    return null; // Không chuyển phòng, không advance
}
```

**Logic:**
- Detect nếu step hiện tại đã COMPLETED → đang re-complete
- Save step nhưng KHÔNG auto-advance
- Return null (không chuyển phòng)

### Layer 3: Mobile - Reset IN_PROGRESS steps không hợp lệ

**File:** `DoctorWorkflowActivity.java`
**Method:** `loadTreatmentPlanForRoom()`

```java
// PROFESSIONAL FIX: Reset các step không đúng về PENDING
// Chỉ step đang được edit (currentStepId) mới được giữ IN_PROGRESS
for (TreatmentPlan.Step step : plan.getSteps()) {
    if ("IN_PROGRESS".equals(step.getStatus())) {
        // Nếu step này KHÔNG phải là step đang edit
        if (currentStepId == null || !currentStepId.equals(step.getId())) {
            android.util.Log.w("DoctorWorkflow", "⚠️ RESET: Step " + step.getId() + 
                " (" + step.getServiceName() + ") có IN_PROGRESS nhưng không phải currentStep → Reset về PENDING");
            step.setStatus("PENDING");
        }
    }
}
```

**Logic:**
- Khi load treatment plan từ backend
- Scan tất cả steps có status=IN_PROGRESS
- Nếu không phải currentStep → Reset về PENDING
- Defensive programming: Ngay cả khi backend sai, mobile vẫn fix được

## 🧪 Test Case

### Setup
```sql
-- Set tất cả steps thành COMPLETED
UPDATE treatment_plan_step 
SET status = 'COMPLETED', completed_at = NOW() 
WHERE plan_id = 1;
```

### Test Steps
1. Load treatment plan (tất cả steps COMPLETED)
2. Click "Sửa" trên step 1
3. Thay đổi dữ liệu
4. Click "Hoàn thành bước"
5. Kiểm tra status

### Kết Quả Mong Đợi
```
Step 1: COMPLETED (updated)
Step 2: COMPLETED (không đổi)
Step 3: COMPLETED (không đổi) ← FIX: Không auto IN_PROGRESS
```

### Log Mong Đợi

**Mobile:**
```
editingPreviouslyCompletedStep: true
✓ Re-completing previously COMPLETED step
→ Step 1: EDITING - status=COMPLETED  ← ĐÚNG!
```

**Backend:**
```
Re-completing step 1 - không auto-advance
```

## 📊 So Sánh Trước/Sau

### Trước Fix

```
1. Edit step 1 (COMPLETED)
2. Click "Hoàn thành bước"
3. Mobile gửi: step 1 = IN_PROGRESS
4. Backend nghĩ: Complete lần đầu
5. Backend auto-advance: step 2 → IN_PROGRESS
6. Kết quả: Step 2 bị IN_PROGRESS (SAI!)
```

### Sau Fix

```
1. Edit step 1 (COMPLETED)
2. Click "Hoàn thành bước"
3. Mobile set: step 1 = COMPLETED
4. Mobile gửi: step 1 = COMPLETED
5. Backend detect: Re-completing
6. Backend skip auto-advance
7. Kết quả: Tất cả steps giữ nguyên status ✓
```

## 📝 Files Đã Sửa

1. **Mobile:** `DoctorWorkflowActivity.java`
   - Method: `onStepComplete()` - Set COMPLETED trước khi save
   - Method: `loadTreatmentPlanForRoom()` - Reset IN_PROGRESS không hợp lệ

2. **Backend:** `TreatmentPlanService.java`
   - Method: `completeStepAndAdvance()` - Skip auto-advance khi re-complete

## 🚀 Cách Build & Test

### Build Mobile
```bash
cd mobile_android
gradlew.bat assembleDebug
```

### Install APK
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Restart Backend
- IntelliJ IDEA: Stop → Run

### Test
1. Setup test data bằng SQL script
2. Edit step 1 đã COMPLETED
3. Click "Hoàn thành bước"
4. Verify step 3 vẫn COMPLETED

## ✅ Hoàn Thành

Fix đã được implement với 3 lớp bảo vệ chuyên nghiệp. Step 3 sẽ không tự động chuyển IN_PROGRESS khi edit step 1.
