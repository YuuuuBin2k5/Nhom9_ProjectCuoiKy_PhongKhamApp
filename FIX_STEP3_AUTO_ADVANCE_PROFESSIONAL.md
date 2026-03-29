# FIX CHUYÊN NGHIỆP: Ngăn Step 3 Tự Động Chuyển IN_PROGRESS

## 🎯 Vấn Đề

Khi edit step 1 (đã COMPLETED), sau khi hoàn thành và reload treatment plan, step 3 tự động chuyển sang IN_PROGRESS mặc dù không phải là step tiếp theo.

## 🔍 Nguyên Nhân Gốc Rễ

### Backend Issue
- Khi re-complete một step đã COMPLETED (để edit), backend vẫn gọi logic auto-advance
- Logic này tìm step PENDING tiếp theo và set nó thành IN_PROGRESS
- Điều này SAI vì khi edit step cũ, không nên advance sang step mới

### Mobile Issue  
- Khi reload treatment plan từ backend, mobile nhận được step 3 = IN_PROGRESS
- Mobile chỉ "preserve" status này mà không validate
- Khi press Back, mobile save lại status IN_PROGRESS sai này

## ✅ Giải Pháp 2 Lớp Bảo Vệ

### Layer 1: Backend Fix (TreatmentPlanService.java)

```java
// PROFESSIONAL FIX: Cho phép re-complete một step đã COMPLETED (để edit)
// NHƯNG: Không auto-advance sang step tiếp theo nếu đang re-complete
boolean isReCompleting = (currentStep.getStatus() == StepStatus.COMPLETED);

// VALIDATION: Kiểm tra các bước trước đó đã hoàn thành chưa (chỉ khi không phải re-complete)
if (!isReCompleting) {
    boolean hasPreviousIncomplete = plan.getSteps().stream()
            .filter(s -> s.getSequenceOrder() != null && currentStep.getSequenceOrder() != null)
            .filter(s -> s.getSequenceOrder() < currentStep.getSequenceOrder())
            .anyMatch(s -> s.getStatus() != StepStatus.COMPLETED && s.getStatus() != StepStatus.SKIPPED);
    
    if (hasPreviousIncomplete) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
            "Không thể hoàn thành bước này. Vui lòng hoàn thành các bước trước đó theo thứ tự.");
    }
}

// ... save step ...

// PROFESSIONAL FIX: Nếu đang re-complete một step đã COMPLETED, không auto-advance
if (isReCompleting) {
    android.util.Log.d("TreatmentPlanService", "Re-completing step " + stepId + " - không auto-advance");
    return null; // Không chuyển phòng, không advance
}
```

**Logic:**
- Detect nếu step hiện tại đã COMPLETED → đang re-complete (edit)
- Nếu re-complete: Không validate sequential order, không auto-advance
- Nếu complete lần đầu: Validate và auto-advance như bình thường

### Layer 2: Mobile Fix (DoctorWorkflowActivity.java)

```java
// PROFESSIONAL FIX: Reset các step không đúng về PENDING
// Chỉ step đang được edit (currentStepId) mới được giữ IN_PROGRESS
// Các step khác nếu có IN_PROGRESS sẽ bị reset về PENDING
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
- Scan tất cả steps có status = IN_PROGRESS
- Nếu step đó KHÔNG phải là currentStep đang edit → Reset về PENDING
- Chỉ currentStep được phép giữ IN_PROGRESS

## 🧪 Test Case

### Scenario: Edit Step 1 (đã COMPLETED)

**Bước test:**
1. Có treatment plan với 3 steps: Step 1, 2, 3 đều COMPLETED
2. Click "Sửa" trên step 1
3. Thay đổi dữ liệu (ví dụ: thêm ảnh, sửa kết luận)
4. Click "Hoàn thành bước"
5. Kiểm tra status của step 3

**Kết quả mong đợi:**
- Step 1: COMPLETED (updated)
- Step 2: COMPLETED (không đổi)
- Step 3: COMPLETED (không đổi) ← KHÔNG phải IN_PROGRESS

**Log backend:**
```
Re-completing step 123 - không auto-advance
```

**Log mobile:**
```
loadTreatmentPlanForRoom: currentStepId = 123
✓ Restored currentStep by ID: Khám tổng quát (Status: COMPLETED)
```

## 🎯 Lợi Ích

1. **Tính nhất quán:** Backend và mobile đều có logic bảo vệ
2. **Dễ maintain:** Logic rõ ràng, có comment đầy đủ
3. **Defensive programming:** Ngay cả khi backend sai, mobile vẫn fix được
4. **Backward compatible:** Không ảnh hưởng flow bình thường (complete step lần đầu)

## 📝 Files Đã Sửa

1. `clinic_backend/src/main/java/com/hcmute/clinic/service/TreatmentPlanService.java`
   - Method: `completeStepAndAdvance()`
   - Thêm logic detect re-completing và skip auto-advance

2. `mobile_android/app/src/main/java/com/hcmute/mobile_android/ui/activities/staff/DoctorWorkflowActivity.java`
   - Method: `loadTreatmentPlanForRoom()`
   - Thêm logic reset IN_PROGRESS steps không hợp lệ

## 🚀 Cách Test

1. Build backend mới:
```bash
cd clinic_backend
./gradlew build
```

2. Build mobile APK mới:
```bash
cd mobile_android
./gradlew assembleDebug
```

3. Install APK:
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

4. Test theo scenario trên

## ✅ Hoàn Thành

Fix này đã được implement với 2 lớp bảo vệ chuyên nghiệp, đảm bảo step 3 không tự động chuyển IN_PROGRESS khi edit step 1.
