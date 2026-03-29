# Fix Bảo Toàn Quy Trình Khi Chỉnh Sửa Bước Cũ

## Vấn Đề

Khi bác sĩ đang xử lý bước 2 (IN_PROGRESS), nhưng cần chỉnh sửa bước 1 (COMPLETED), sau khi lưu bước 1, hệ thống nên quay lại bước 2 (không phải bước 1).

### Kịch Bản

1. **Trạng thái ban đầu:**
   - Bước 1: Khám và tư vấn - **COMPLETED**
   - Bước 2: X-quang - **IN_PROGRESS** (đang xử lý)
   - Bước 3: Nhổ răng khôn - **PENDING**
   - `currentStep = Bước 2` (quy trình đang ở đây)

2. **Bác sĩ chỉnh sửa bước 1:**
   - Nhấn "Chỉnh sửa" bước 1
   - Sửa dữ liệu
   - Nhấn "Lưu"

3. **Kết quả mong đợi:**
   - ✅ Bước 1 được lưu thành công
   - ✅ Quy trình quay lại bước 2 (vẫn IN_PROGRESS)
   - ✅ `currentStep = Bước 2` (không thay đổi)

4. **Kết quả thực tế (trước fix):**
   - ✅ Bước 1 được lưu
   - ❌ `currentStep = Bước 1` (sai!)
   - ❌ Quy trình bị gián đoạn

## Nguyên Nhân

### 1. Không Phân Biệt "Editing Step" và "Current Step"

Code cũ chỉ có 1 biến `currentStep`:

```java
@Override
public void onStepEdit(TreatmentPlan.Step step) {
    this.currentStep = step; // ❌ Ghi đè currentStep!
    // ...
}
```

**Vấn đề:**
- Khi edit bước 1, `currentStep` bị ghi đè thành bước 1
- Mất track của bước 2 (bước đang trong quy trình)
- Sau khi lưu, không biết phải quay lại bước nào

### 2. Logic Restore Không Đúng

Code cũ restore `currentStep` thành bước vừa edit:

```java
@Override
public void onStepSave(TreatmentPlan.Step step) {
    final Long savedStepId = step.getId();
    saveTreatmentPlanInternal(false, () -> {
        // Restore to edited step
        currentStep = findStepById(savedStepId); // ❌ Sai!
    });
}
```

**Vấn đề:**
- Luôn restore về bước vừa edit
- Không restore về bước đang trong quy trình

## Giải Pháp

### 1. Thêm Biến `editingStep`

Phân biệt 2 khái niệm:
- `currentStep`: Bước đang trong quy trình (IN_PROGRESS)
- `editingStep`: Bước đang được chỉnh sửa (có thể là COMPLETED)

```java
private TreatmentPlan.Step currentStep;      // Bước đang trong quy trình
private TreatmentPlan.Step editingStep;      // Bước đang được chỉnh sửa
```

### 2. Lưu `editingStep` Khi Edit

```java
@Override
public void onStepEdit(TreatmentPlan.Step step) {
    // CRITICAL: Lưu bước đang được chỉnh sửa (có thể khác currentStep)
    this.editingStep = step;
    android.util.Log.d("DoctorWorkflow", "onStepEdit: editingStep = " + step.getServiceName());
    android.util.Log.d("DoctorWorkflow", "onStepEdit: currentStep = " + 
        (currentStep != null ? currentStep.getServiceName() : "null"));
    
    // ... rest of logic
}
```

**Cải tiến:**
- ✅ Không ghi đè `currentStep`
- ✅ Lưu `editingStep` riêng
- ✅ Có logging để debug

### 3. Restore Đúng Bước Sau Khi Lưu

```java
@Override
public void onStepSave(TreatmentPlan.Step step) {
    // CRITICAL: Lưu currentStep ban đầu (bước đang trong quy trình)
    final Long originalCurrentStepId = (currentStep != null) ? currentStep.getId() : null;
    final Long editingStepId = step.getId();
    
    saveTreatmentPlanInternal(false, () -> {
        Toast.makeText(this, "Đã lưu thay đổi", Toast.LENGTH_SHORT).show();
        loadTreatmentPlanForRoom(currentTreatmentPlanId);
        
        // CRITICAL FIX: Restore currentStep to the ORIGINAL step (not the edited step)
        if (originalCurrentStepId != null && !originalCurrentStepId.equals(editingStepId)) {
            // Editing a different step than currentStep
            // Restore currentStep to the original workflow step
            for (TreatmentPlan.Step s : treatmentSteps) {
                if (originalCurrentStepId.equals(s.getId())) {
                    currentStep = s;
                    android.util.Log.d("DoctorWorkflow", "✓ Restored currentStep to original: " + s.getServiceName());
                    break;
                }
            }
        } else if (editingStepId != null) {
            // Editing the same step as currentStep, or no original currentStep
            // Keep currentStep as the edited step
            for (TreatmentPlan.Step s : treatmentSteps) {
                if (editingStepId.equals(s.getId())) {
                    currentStep = s;
                    android.util.Log.d("DoctorWorkflow", "✓ Kept currentStep as edited step: " + s.getServiceName());
                    break;
                }
            }
        }
        
        // Clear editingStep
        editingStep = null;
    });
}
```

**Logic:**
1. Lưu `originalCurrentStepId` (bước đang trong quy trình)
2. Lưu `editingStepId` (bước đang được chỉnh sửa)
3. Sau khi reload:
   - Nếu `originalCurrentStepId != editingStepId` → Restore về original
   - Nếu `originalCurrentStepId == editingStepId` → Giữ nguyên
4. Clear `editingStep`

## Logic Hoạt Động

### Trường Hợp 1: Chỉnh Sửa Bước Khác

**Trạng thái:**
- `currentStep = Bước 2` (IN_PROGRESS)
- User edit bước 1 (COMPLETED)

**Luồng:**
1. `onStepEdit(Bước 1)`:
   - `editingStep = Bước 1`
   - `currentStep = Bước 2` (không đổi)

2. User sửa và lưu

3. `onStepSave(Bước 1)`:
   - `originalCurrentStepId = 2` (ID của bước 2)
   - `editingStepId = 1` (ID của bước 1)
   - Save data
   - Reload
   - `originalCurrentStepId != editingStepId` → Restore về bước 2
   - `currentStep = Bước 2` ✅

**Kết quả:**
- ✅ Bước 1 được lưu
- ✅ Quy trình quay lại bước 2
- ✅ Không gián đoạn workflow

### Trường Hợp 2: Chỉnh Sửa Bước Hiện Tại

**Trạng thái:**
- `currentStep = Bước 2` (IN_PROGRESS)
- User edit bước 2 (chính nó)

**Luồng:**
1. `onStepEdit(Bước 2)`:
   - `editingStep = Bước 2`
   - `currentStep = Bước 2`

2. User sửa và lưu

3. `onStepSave(Bước 2)`:
   - `originalCurrentStepId = 2`
   - `editingStepId = 2`
   - Save data
   - Reload
   - `originalCurrentStepId == editingStepId` → Giữ nguyên
   - `currentStep = Bước 2` ✅

**Kết quả:**
- ✅ Bước 2 được lưu
- ✅ Quy trình vẫn ở bước 2
- ✅ Workflow tiếp tục bình thường

### Trường Hợp 3: Chưa Có Current Step

**Trạng thái:**
- `currentStep = null` (chưa bắt đầu workflow)
- User edit bước 1 (COMPLETED)

**Luồng:**
1. `onStepEdit(Bước 1)`:
   - `editingStep = Bước 1`
   - `currentStep = null`

2. User sửa và lưu

3. `onStepSave(Bước 1)`:
   - `originalCurrentStepId = null`
   - `editingStepId = 1`
   - Save data
   - Reload
   - `originalCurrentStepId == null` → Set currentStep = bước 1
   - `currentStep = Bước 1` ✅

**Kết quả:**
- ✅ Bước 1 được lưu
- ✅ currentStep được set (vì chưa có)

## Cách Test

### Test 1: Chỉnh Sửa Bước Cũ Khi Đang Xử Lý Bước Khác

1. **Setup:**
   - Bước 1: COMPLETED
   - Bước 2: IN_PROGRESS (đang xử lý)
   - Bước 3: PENDING

2. **Thao tác:**
   - Nhấn "Chỉnh sửa" bước 1
   - Sửa dữ liệu
   - Nhấn "Lưu"

3. **Kỳ vọng:**
   - ✅ Toast "Đã lưu thay đổi"
   - ✅ Quy trình quay lại bước 2
   - ✅ Bước 2 vẫn IN_PROGRESS
   - ✅ Bước 3 vẫn PENDING (không tự động start)

### Test 2: Kiểm Tra Log

```bash
adb logcat | grep DoctorWorkflow
```

**Log mong đợi:**
```
D/DoctorWorkflow: onStepEdit: editingStep = Khám và tư vấn (ID: 1)
D/DoctorWorkflow: onStepEdit: currentStep = X-quang
D/DoctorWorkflow: onStepSave called for step: Khám và tư vấn (ID: 1)
D/DoctorWorkflow:   - editingStep: Khám và tư vấn
D/DoctorWorkflow:   - currentStep: X-quang
D/DoctorWorkflow: ✓ Restored currentStep to original workflow step: X-quang
```

### Test 3: Chỉnh Sửa Nhiều Bước

1. Đang xử lý bước 2
2. Chỉnh sửa bước 1 → Lưu
3. Chỉnh sửa bước 3 → Lưu
4. Chỉnh sửa bước 1 lại → Lưu

**Kỳ vọng**: Mỗi lần đều quay lại bước 2

### Test 4: Chỉnh Sửa Bước Hiện Tại

1. Đang xử lý bước 2
2. Chỉnh sửa bước 2 (chính nó) → Lưu

**Kỳ vọng**: Vẫn ở bước 2

## Files Đã Sửa

- `mobile_android/app/src/main/java/com/hcmute/mobile_android/ui/activities/staff/DoctorWorkflowActivity.java`
  - Thêm biến `editingStep`
  - Sửa `onStepEdit()`: Lưu editingStep, không ghi đè currentStep
  - Sửa `onStepSave()`: Restore đúng currentStep ban đầu

## So Sánh Trước và Sau

| Tính Năng | Trước | Sau |
|-----------|-------|-----|
| Số biến tracking | 1 (currentStep) | 2 (currentStep + editingStep) |
| Ghi đè currentStep | ✅ Có | ❌ Không |
| Restore logic | Luôn về edited step | Về original workflow step |
| Bảo toàn workflow | ❌ Không | ✅ Có |
| Logging | ⚠️ Ít | ✅ Chi tiết |

## Lợi Ích

1. ✅ **Bảo toàn quy trình**: Không gián đoạn workflow khi edit bước cũ
2. ✅ **Trải nghiệm tốt hơn**: Bác sĩ có thể sửa bước cũ mà không lo mất track
3. ✅ **Linh hoạt**: Có thể edit bất kỳ bước nào mà không ảnh hưởng quy trình
4. ✅ **Dễ debug**: Logging chi tiết giúp theo dõi state

## Lưu Ý

- `editingStep` chỉ được set khi user nhấn "Chỉnh sửa"
- `currentStep` là bước đang trong quy trình chính
- Sau khi lưu, `editingStep` được clear
- Luôn restore về `currentStep` ban đầu (trừ khi edit chính nó)

## Kết Luận

Fix này đảm bảo:
- ✅ Quy trình không bị gián đoạn khi chỉnh sửa bước cũ
- ✅ `currentStep` luôn trỏ đúng bước đang trong workflow
- ✅ Bác sĩ có thể tự do chỉnh sửa bất kỳ bước nào
- ✅ Workflow tiếp tục đúng sau khi lưu


---

## Update: Diagnostic APK for Step 3 Status Bug

### New Issue Discovered
While testing the workflow preservation fix, we discovered that step 3 is incorrectly becoming IN_PROGRESS when editing step 1.

**Scenario:**
- Step 1: COMPLETED
- Step 2: IN_PROGRESS
- Step 3: PENDING
- Edit and save step 1 → Step 3 becomes IN_PROGRESS (WRONG!)

### Root Cause Investigation
The backend `updateSteps` method accepts ANY status sent by mobile. If mobile sends wrong status, backend applies it.

Need to determine:
1. Is mobile sending wrong status for step 3?
2. Is backend modifying step 3 status?

### Diagnostic Solution
Added comprehensive logging to track:
- Status of each step BEFORE sending to backend
- Backend response
- Status of each step AFTER reloading from backend

### New APK
- File: `app-debug-fix-step3-status.apk`
- Purpose: Diagnose step 3 status bug with enhanced logging

### Testing Guide
See: `HUONG_DAN_TEST_FIX_STEP3.md`

### Expected Logs
```
=== saveTreatmentPlanInternal: Preparing request ===
editingStep: Khám và tư vấn (ID: 1)
currentStep: Chụp X-quang (ID: 2)
→ Step 1 (Khám và tư vấn): EDITING - status=IN_PROGRESS
→ Step 2 (Chụp X-quang): CURRENT - status=IN_PROGRESS
→ Step 3 (Nhổ răng khôn): OTHER - status=PENDING (preserving)
```

**CRITICAL**: Step 3 must show `status=PENDING` with `(preserving)` note

### Status
✅ Diagnostic APK built
✅ Testing guide created
⏳ Waiting for user testing and log collection

### Related Documents
- `FIX_STEP3_STATUS_BUG.md` - Technical analysis
- `HUONG_DAN_TEST_FIX_STEP3.md` - Vietnamese testing guide
- `TASK4_DIAGNOSTIC_APK_READY.md` - Summary document
