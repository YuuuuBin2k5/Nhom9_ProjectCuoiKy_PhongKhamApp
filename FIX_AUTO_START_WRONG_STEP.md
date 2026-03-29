# Fix Lỗi Tự Động Kích Hoạt Sai Bước Sau Khi Chỉnh Sửa

## Vấn Đề

Khi bác sĩ chỉnh sửa bước đã hoàn thành (COMPLETED), sau khi lưu, hệ thống tự động kích hoạt (start) bước PENDING khác thay vì quay lại bước đang chỉnh sửa.

### Kịch Bản Lỗi

1. Bước 1: Khám và tư vấn - **COMPLETED**
2. Bước 2: X-quang - **IN_PROGRESS** (đang đợi ở phòng X-quang)
3. Bước 3: Nhổ răng khôn - **PENDING**
4. Bác sĩ nhấn "Chỉnh sửa" bước 1
5. Sau khi lưu → **Bước 3 tự động được kích hoạt** ❌

### Kết Quả Mong Đợi

Sau khi lưu bước 1, hệ thống nên:
- Quay lại bước 1 (vẫn ở trạng thái IN_PROGRESS hoặc COMPLETED)
- KHÔNG tự động start bước 3

## Nguyên Nhân

### 1. Logic Restore currentStep Sai

Trong `loadTreatmentPlanForRoom`, code cũ restore `currentStep` bằng **index**:

```java
int originalIndex = -1;
if (currentStep != null) {
    originalIndex = treatmentSteps.indexOf(currentStep);
}

treatmentSteps.clear();
treatmentSteps.addAll(plan.getSteps());

if (originalIndex != -1 && originalIndex < treatmentSteps.size()) {
    currentStep = treatmentSteps.get(originalIndex); // ❌ SAI!
}
```

**Vấn đề**: 
- `indexOf()` so sánh object reference, không phải ID
- Sau khi `clear()` và `addAll()`, các object mới được tạo
- `indexOf()` trả về -1 → Fallback sang logic match by name
- Logic match by name có thể match nhầm step khác

### 2. Không Lưu Step ID Trước Khi Reload

Trong `onStepSave`, code cũ không lưu step ID:

```java
public void onStepSave(TreatmentPlan.Step step) {
    saveTreatmentPlanInternal(false, () -> {
        Toast.makeText(this, "Đã lưu thay đổi", Toast.LENGTH_SHORT).show();
        loadTreatmentPlanForRoom(currentTreatmentPlanId); // ❌ Mất track của step
    });
}
```

## Giải Pháp

### 1. Restore currentStep Bằng ID

**Trước:**
```java
int originalIndex = -1;
if (currentStep != null) {
    originalIndex = treatmentSteps.indexOf(currentStep);
}

treatmentSteps.clear();
treatmentSteps.addAll(plan.getSteps());

if (originalIndex != -1 && originalIndex < treatmentSteps.size()) {
    currentStep = treatmentSteps.get(originalIndex);
}
```

**Sau:**
```java
// Save currentStep ID before clearing list
Long currentStepId = (currentStep != null) ? currentStep.getId() : null;
android.util.Log.d("DoctorWorkflow", "loadTreatmentPlanForRoom: currentStepId = " + currentStepId);

treatmentSteps.clear();
treatmentSteps.addAll(plan.getSteps());

// Restore currentStep by ID, not by index
if (currentStepId != null) {
    currentStep = null; // Reset first
    for (TreatmentPlan.Step s : treatmentSteps) {
        if (currentStepId.equals(s.getId())) {
            currentStep = s;
            android.util.Log.d("DoctorWorkflow", "✓ Restored currentStep by ID: " + s.getServiceName());
            break;
        }
    }
}
```

**Cải tiến:**
- ✅ Lưu ID trước khi clear list
- ✅ Restore bằng ID (unique và stable)
- ✅ Thêm logging để debug
- ✅ Reset currentStep trước khi restore

### 2. Lưu Step ID Trong onStepSave

**Trước:**
```java
public void onStepSave(TreatmentPlan.Step step) {
    saveTreatmentPlanInternal(false, () -> {
        Toast.makeText(this, "Đã lưu thay đổi", Toast.LENGTH_SHORT).show();
        loadTreatmentPlanForRoom(currentTreatmentPlanId);
    });
}
```

**Sau:**
```java
public void onStepSave(TreatmentPlan.Step step) {
    android.util.Log.d("DoctorWorkflow", "onStepSave called for step: " + step.getServiceName() + " (ID: " + step.getId() + ")");
    
    // Save the step ID to restore after reload
    final Long savedStepId = step.getId();
    
    saveTreatmentPlanInternal(false, () -> {
        Toast.makeText(this, "Đã lưu thay đổi", Toast.LENGTH_SHORT).show();
        loadTreatmentPlanForRoom(currentTreatmentPlanId);
        
        // CRITICAL FIX: After reload, restore currentStep to the edited step
        if (savedStepId != null) {
            for (TreatmentPlan.Step s : treatmentSteps) {
                if (savedStepId.equals(s.getId())) {
                    currentStep = s;
                    android.util.Log.d("DoctorWorkflow", "✓ Restored currentStep to edited step: " + s.getServiceName());
                    break;
                }
            }
        }
    });
}
```

**Cải tiến:**
- ✅ Lưu step ID trước khi save
- ✅ Restore currentStep sau khi reload
- ✅ Thêm logging chi tiết

## Logic Hoạt Động Sau Fix

### Khi Chỉnh Sửa Bước COMPLETED:

1. **User nhấn "Chỉnh sửa" bước 1**
   - `onStepEdit(step1)` được gọi
   - API `cancelTreatmentStep(step1.id)` được gọi
   - Backend: step1.status = COMPLETED → IN_PROGRESS
   - `currentStep = step1`

2. **User chỉnh sửa và nhấn "Lưu"**
   - `onStepSave(step1)` được gọi
   - Lưu `savedStepId = step1.getId()` (ví dụ: 123)
   - `saveTreatmentPlanInternal()` được gọi
   - Backend lưu dữ liệu

3. **Sau khi save xong (callback)**
   - `loadTreatmentPlanForRoom()` được gọi
   - Backend trả về list steps mới
   - `treatmentSteps.clear()` và `addAll(newSteps)`
   - **Restore currentStep bằng ID 123**
   - Tìm step có ID = 123 trong list mới
   - `currentStep = step1` (restored) ✅

4. **Kết quả**
   - currentStep vẫn trỏ đúng bước 1
   - Bước 3 KHÔNG bị tự động start
   - UI hiển thị đúng bước đang chỉnh sửa

## Tại Sao Dùng ID Thay Vì Index?

| Phương Pháp | Ưu Điểm | Nhược Điểm |
|-------------|---------|------------|
| **Index** | Đơn giản | ❌ Không stable khi list thay đổi<br>❌ Có thể trỏ nhầm step khác<br>❌ Không work với object mới |
| **ID** | ✅ Unique và stable<br>✅ Không bị ảnh hưởng bởi thứ tự<br>✅ Work với object mới | Cần null check |
| **Name + Tooth** | Có thể work | ❌ Không unique nếu có 2 step giống nhau<br>❌ Phức tạp |

## Cách Test

### Test 1: Chỉnh Sửa Bước COMPLETED

1. Tạo plan với 3 bước:
   - Bước 1: Khám - COMPLETED
   - Bước 2: X-quang - IN_PROGRESS
   - Bước 3: Nhổ răng - PENDING

2. Nhấn "Chỉnh sửa" bước 1

3. Sửa dữ liệu và nhấn "Lưu"

4. **Kỳ vọng**:
   - ✅ Hiển thị "Đã lưu thay đổi"
   - ✅ currentStep vẫn là bước 1
   - ✅ Bước 3 KHÔNG tự động start
   - ✅ UI hiển thị đúng bước 1

### Test 2: Kiểm Tra Log

```bash
adb logcat | grep DoctorWorkflow
```

**Log mong đợi:**
```
D/DoctorWorkflow: onStepSave called for step: Khám và tư vấn (ID: 123)
D/DoctorWorkflow: loadTreatmentPlanForRoom: currentStepId = 123
D/DoctorWorkflow: ✓ Restored currentStep by ID: Khám và tư vấn (Status: IN_PROGRESS)
D/DoctorWorkflow: ✓ Restored currentStep to edited step: Khám và tư vấn
```

### Test 3: Chỉnh Sửa Nhiều Lần

1. Chỉnh sửa bước 1 → Lưu
2. Chỉnh sửa bước 1 lại → Lưu
3. Chỉnh sửa bước 1 lần nữa → Lưu

**Kỳ vọng**: Mỗi lần đều restore đúng bước 1

### Test 4: Chỉnh Sửa Bước Khác

1. Chỉnh sửa bước 1 → Lưu
2. Chỉnh sửa bước 3 → Lưu

**Kỳ vọng**: Mỗi lần restore đúng bước đang chỉnh sửa

## Files Đã Sửa

- `mobile_android/app/src/main/java/com/hcmute/mobile_android/ui/activities/staff/DoctorWorkflowActivity.java`
  - Method `loadTreatmentPlanForRoom()`: Restore currentStep bằng ID
  - Method `onStepSave()`: Lưu và restore step ID

## So Sánh Trước và Sau

| Tính Năng | Trước | Sau |
|-----------|-------|-----|
| Restore method | Index | ID |
| Stability | ❌ Không stable | ✅ Stable |
| Accuracy | ❌ Có thể sai | ✅ Chính xác |
| Logging | ❌ Không có | ✅ Chi tiết |
| Auto-start sai step | ✅ Có lỗi | ❌ Không còn |

## Lưu Ý

- ID là unique và stable, không thay đổi khi reload
- Luôn lưu ID trước khi clear list
- Thêm logging để dễ debug
- Kiểm tra null trước khi so sánh ID

## Kết Luận

Fix này đảm bảo:
- ✅ currentStep luôn được restore đúng sau khi reload
- ✅ Không tự động start bước PENDING khác
- ✅ Trải nghiệm người dùng tốt hơn
- ✅ Dễ debug với logging chi tiết
