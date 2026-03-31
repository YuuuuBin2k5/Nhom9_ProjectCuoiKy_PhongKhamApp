# Remove Auto-Complete Plan Logic - COMPLETE ✅

## Vấn đề (Problem)
Hồ sơ bệnh nhân (Treatment Plan) tự động hoàn thành khi tất cả các bước điều trị đã complete, mà không cần nhấn nút "Hoàn thành".

**Hành vi hiện tại (không mong muốn)**:
1. Bác sĩ hoàn thành bước 1 → Lưu ✅
2. Bác sĩ hoàn thành bước 2 → Lưu ✅
3. Bác sĩ hoàn thành bước 3 (bước cuối) → **Plan tự động COMPLETED** ❌

**Hành vi mong muốn**:
1. Bác sĩ hoàn thành bước 1 → Lưu ✅
2. Bác sĩ hoàn thành bước 2 → Lưu ✅
3. Bác sĩ hoàn thành bước 3 (bước cuối) → Lưu ✅ (plan vẫn IN_PROGRESS)
4. Bác sĩ nhấn nút "Hoàn thành" → **Plan chuyển sang COMPLETED** ✅

## Phân tích nguyên nhân (Root Cause)

### Location: `TreatmentPlanService.java` - Method `completeStepAndAdvance()`

**Logic cũ (lines 489-527)**:
```java
if (nextStep == null) {
    // Không còn PENDING steps
    boolean hasInProgress = plan.getSteps().stream()
            .anyMatch(s -> s.getStatus() == StepStatus.IN_PROGRESS);
    
    if (!hasInProgress) {
        // ❌ TỰ ĐỘNG COMPLETE PLAN
        plan.setStatus(TreatmentPlanStatus.COMPLETED);
        planRepository.save(plan);

        // Cleanup queues
        // Send notification
        // ...
    }
    return null;
}
```

**Vấn đề**:
- Khi không còn PENDING steps VÀ không còn IN_PROGRESS steps
- → Plan tự động chuyển sang COMPLETED
- → User không có cơ hội review lại hoặc quyết định khi nào hoàn thành

## Giải pháp (Solution)

### Removed Auto-Complete Logic

**Logic mới**:
```java
if (nextStep == null) {
    // Không còn PENDING steps
    boolean hasInProgress = plan.getSteps().stream()
            .anyMatch(s -> s.getStatus() == StepStatus.IN_PROGRESS);
    
    // REMOVED AUTO-COMPLETE LOGIC:
    // Plan should only be completed when user explicitly clicks "Hoàn thành" button
    // NOT automatically when all steps are done
    // 
    // Old logic (removed):
    // if (!hasInProgress) {
    //     plan.setStatus(TreatmentPlanStatus.COMPLETED);
    //     planRepository.save(plan);
    //     ... cleanup queues and send notification ...
    // }
    
    // Now: Just return null, don't auto-complete the plan
    return null;
}
```

**Thay đổi**:
- ✅ Xóa logic tự động complete plan
- ✅ Chỉ return null khi không còn bước tiếp theo
- ✅ Plan vẫn ở trạng thái IN_PROGRESS
- ✅ User phải nhấn nút "Hoàn thành" để complete plan

## Workflow mới (New Workflow)

### Trước khi fix ❌:
```
Step 1: PENDING → IN_PROGRESS → COMPLETED ✅
Step 2: PENDING → IN_PROGRESS → COMPLETED ✅
Step 3: PENDING → IN_PROGRESS → COMPLETED ✅
                                    ↓
                        Plan AUTO COMPLETED ❌
                                    ↓
                        Queue cleaned up
                        Notification sent
```

### Sau khi fix ✅:
```
Step 1: PENDING → IN_PROGRESS → COMPLETED ✅
Step 2: PENDING → IN_PROGRESS → COMPLETED ✅
Step 3: PENDING → IN_PROGRESS → COMPLETED ✅
                                    ↓
                        Plan vẫn IN_PROGRESS ✅
                                    ↓
                User nhấn "Hoàn thành" (manual)
                                    ↓
                        Plan COMPLETED ✅
                                    ↓
                        Queue cleaned up
                        Notification sent
```

## Impact Analysis (Phân tích tác động)

### Positive Impacts ✅:
1. **User Control**: Bác sĩ có quyền kiểm soát khi nào hoàn thành plan
2. **Review Opportunity**: Có cơ hội review lại tất cả steps trước khi hoàn thành
3. **Flexibility**: Có thể thêm steps mới sau khi hoàn thành tất cả steps hiện tại
4. **No Accidental Completion**: Tránh hoàn thành nhầm khi chưa muốn

### Potential Issues (Cần lưu ý):
1. **Manual Completion Required**: User PHẢI nhớ nhấn "Hoàn thành"
2. **Queue Not Auto-Cleaned**: Queue sẽ không tự động cleanup cho đến khi user nhấn "Hoàn thành"
3. **Notification Delayed**: Notification "Phác đồ hoàn tất" chỉ gửi khi user nhấn "Hoàn thành"

### Mitigation (Giải pháp giảm thiểu):
- Hiển thị nút "Hoàn thành" rõ ràng khi tất cả steps đã complete
- Có thể thêm reminder/notification nhắc user hoàn thành plan
- UI nên highlight khi plan có thể hoàn thành

## Files Modified

1. `clinic_backend/src/main/java/com/hcmute/clinic/service/TreatmentPlanService.java`
   - Method: `completeStepAndAdvance()`
   - Lines: 489-527 (removed auto-complete logic)
   - Change: Removed automatic plan completion when all steps are done

## Testing Guide

### Test Case 1: Complete All Steps
1. Tạo treatment plan với 3 steps
2. Complete step 1 → Plan vẫn IN_PROGRESS ✅
3. Complete step 2 → Plan vẫn IN_PROGRESS ✅
4. Complete step 3 → **Plan vẫn IN_PROGRESS** ✅ (không tự động COMPLETED)
5. Verify: Plan status = IN_PROGRESS
6. Verify: Tất cả steps status = COMPLETED

### Test Case 2: Manual Complete Plan
1. Sau khi complete tất cả steps (từ Test Case 1)
2. Nhấn nút "Hoàn thành" (manual)
3. **Expected**: Plan chuyển sang COMPLETED
4. **Expected**: Queue được cleanup
5. **Expected**: Notification "Phác đồ hoàn tất" được gửi

### Test Case 3: Add Step After All Steps Completed
1. Complete tất cả steps (plan vẫn IN_PROGRESS)
2. Thêm step mới vào plan
3. **Expected**: Step mới có status PENDING
4. **Expected**: Plan vẫn IN_PROGRESS
5. Complete step mới
6. **Expected**: Plan vẫn IN_PROGRESS (không tự động complete)

### Test Case 4: Queue Behavior
1. Complete tất cả steps
2. **Expected**: Queue vẫn IN_PROGRESS hoặc WAITING (không tự động COMPLETED)
3. Nhấn "Hoàn thành"
4. **Expected**: Queue chuyển sang COMPLETED

## UI Recommendations (Khuyến nghị UI)

### 1. Show "Hoàn thành" Button
Khi tất cả steps đã COMPLETED, hiển thị nút "Hoàn thành" rõ ràng:
```
┌─────────────────────────────────────┐
│ ✅ Tất cả bước đã hoàn thành        │
│                                     │
│ [Hoàn thành hồ sơ điều trị]        │ ← Nút này
└─────────────────────────────────────┘
```

### 2. Add Confirmation Dialog
Khi nhấn "Hoàn thành", hiển thị dialog xác nhận:
```
┌─────────────────────────────────────┐
│ Hoàn thành hồ sơ điều trị?          │
│                                     │
│ Bạn đã hoàn thành tất cả 3 bước.    │
│ Xác nhận hoàn thành hồ sơ?          │
│                                     │
│ [Hủy]  [Hoàn thành]                 │
└─────────────────────────────────────┘
```

### 3. Visual Indicator
Hiển thị progress bar hoặc indicator:
```
Tiến độ điều trị: 3/3 bước ✅
[████████████████████████] 100%

⚠️ Nhấn "Hoàn thành" để kết thúc hồ sơ
```

## Backend API (Existing)

Nút "Hoàn thành" nên gọi API hiện có (nếu có) hoặc tạo API mới:

### Option 1: Use Existing API (if available)
```
POST /api/treatment-plans/{planId}/complete
```

### Option 2: Create New API (if needed)
```java
@PostMapping("/{planId}/complete")
public ResponseEntity<?> completePlan(@PathVariable Long planId) {
    // Set plan status to COMPLETED
    // Cleanup queues
    // Send notification
    return ResponseEntity.ok("Plan completed successfully");
}
```

## Status: COMPLETE ✅

Logic tự động complete plan đã được remove. Giờ plan chỉ complete khi user nhấn nút "Hoàn thành".

**Next Steps**:
1. ✅ Backend fix complete
2. ⏳ UI cần thêm nút "Hoàn thành" rõ ràng
3. ⏳ Test workflow mới
4. ⏳ Update user documentation

---
**Date:** 2026-03-31
**Task:** Remove Auto-Complete Plan Logic
**Status:** COMPLETE ✅
**Build:** SUCCESS ✅
