# 🐛 Fix: Phác đồ tự động hoàn tất (Auto-Complete Bug)

## 📋 Vấn đề

**Hiện tượng:**
- Khi hoàn thành bước cuối cùng trong phác đồ điều trị
- Phác đồ TỰ ĐỘNG chuyển sang trạng thái COMPLETED
- Bác sĩ chưa kịp nhấn nút "Hoàn thành khám"
- Sau đó không thể chỉnh sửa gì nữa (bị khóa)

**Ảnh hưởng:**
- Bác sĩ mất quyền kiểm soát
- Không thể thêm dịch vụ hoặc chỉnh sửa sau khi hoàn thành bước cuối
- UX không tốt - bác sĩ muốn tự quyết định khi nào hoàn thành khám

## 🔍 Root Cause Analysis

### Logic cũ (SAI):

**File:** `TreatmentPlanService.java` - Method `completeStepAndAdvance()`

```java
// Khi hoàn thành 1 bước
currentStep.setStatus(StepStatus.COMPLETED);
stepRepository.save(currentStep);

// Tìm bước tiếp theo
TreatmentPlanStep nextStep = plan.getSteps().stream()
    .filter(s -> s.getStatus() == StepStatus.PENDING)
    .min(Comparator.comparingInt(...))
    .orElse(null);

if (nextStep == null) {  // ❌ Không còn bước PENDING
    boolean hasInProgress = plan.getSteps().stream()
        .anyMatch(s -> s.getStatus() == StepStatus.IN_PROGRESS);
    
    if (!hasInProgress) {  // ❌ Không còn IN_PROGRESS
        // ❌❌❌ TỰ ĐỘNG SET COMPLETED!
        plan.setStatus(TreatmentPlanStatus.COMPLETED);
        planRepository.save(plan);
        
        // Gửi notification
        // ...
    }
    return null;
}
```

### Vấn đề:

1. **Trigger tự động:** Khi hoàn thành bước cuối cùng
2. **Điều kiện:** Không còn bước PENDING và không còn IN_PROGRESS
3. **Hành động:** Tự động set `plan.status = COMPLETED`
4. **Hậu quả:** Phác đồ bị khóa, không thể chỉnh sửa

### Workflow sai:

```
Bác sĩ hoàn thành bước cuối
    ↓
Backend kiểm tra: Còn bước PENDING? → Không
    ↓
Backend kiểm tra: Còn bước IN_PROGRESS? → Không
    ↓
❌ Backend TỰ ĐỘNG: plan.status = COMPLETED
    ↓
Phác đồ bị khóa
    ↓
Bác sĩ không thể chỉnh sửa gì nữa
```

## ✅ Giải pháp

### Logic mới (ĐÚNG):

```java
if (nextStep == null) {
    // Không còn PENDING steps
    // NOTE: KHÔNG tự động complete plan!
    // Bác sĩ phải nhấn nút "Hoàn thành khám" để complete plan và tạo hóa đơn
    
    // Chỉ return null, không làm gì cả
    return null; // Không còn bước nào để chuyển
}
```

### Nguyên tắc mới:

**Phác đồ chỉ chuyển sang COMPLETED khi:**
1. ✅ Bác sĩ nhấn nút "Hoàn thành khám"
2. ✅ Gọi API `POST /api/treatment-plans/{planId}/complete-and-generate-invoice`
3. ✅ Backend tạo hóa đơn và set status = COMPLETED

**KHÔNG tự động complete khi:**
- ❌ Hoàn thành bước cuối cùng
- ❌ Không còn bước PENDING
- ❌ Không còn bước IN_PROGRESS

### Workflow đúng:

```
Bác sĩ hoàn thành bước cuối
    ↓
Backend: Không còn bước PENDING → return null
    ↓
Phác đồ vẫn ở trạng thái IN_PROGRESS
    ↓
Bác sĩ có thể:
  - Thêm dịch vụ mới
  - Chỉnh sửa kết luận
  - Xem lại phác đồ
    ↓
Bác sĩ nhấn "Hoàn thành khám"
    ↓
✅ Backend: plan.status = COMPLETED + Tạo hóa đơn
    ↓
Phác đồ bị khóa (đúng thời điểm)
```

## 📝 Thay đổi Code

### File: `TreatmentPlanService.java`

**Trước:**
```java
if (nextStep == null) {
    boolean hasInProgress = plan.getSteps().stream()
        .anyMatch(s -> s.getStatus() == StepStatus.IN_PROGRESS);
    
    if (!hasInProgress) {
        plan.setStatus(TreatmentPlanStatus.COMPLETED);  // ❌ TỰ ĐỘNG
        planRepository.save(plan);
        // Send notification...
    }
    return null;
}
```

**Sau:**
```java
if (nextStep == null) {
    // KHÔNG tự động complete plan!
    // Bác sĩ phải nhấn nút "Hoàn thành khám"
    return null;  // ✅ Chỉ return, không làm gì
}
```

## 🎯 Lợi ích

1. **Kiểm soát tốt hơn:**
   - Bác sĩ quyết định khi nào hoàn thành khám
   - Không bị "bất ngờ" khi phác đồ tự khóa

2. **Linh hoạt hơn:**
   - Có thể thêm dịch vụ sau khi hoàn thành tất cả bước
   - Có thể chỉnh sửa kết luận trước khi hoàn tất

3. **UX tốt hơn:**
   - Workflow rõ ràng: Hoàn thành bước → Xem lại → Hoàn thành khám
   - Bác sĩ có thời gian kiểm tra lại trước khi finalize

4. **Tách biệt rõ ràng:**
   - Hoàn thành bước ≠ Hoàn thành khám
   - Hoàn thành khám = Tạo hóa đơn + Khóa phác đồ

## 🧪 Testing

### Test Case 1: Hoàn thành bước cuối

**Steps:**
1. Tạo phác đồ với 2 bước
2. Hoàn thành bước 1
3. Hoàn thành bước 2 (bước cuối)

**Expected:**
- ✅ Bước 2 chuyển sang COMPLETED
- ✅ Phác đồ vẫn ở trạng thái IN_PROGRESS (KHÔNG tự động COMPLETED)
- ✅ Có thể thêm dịch vụ mới
- ✅ Có thể chỉnh sửa kết luận

### Test Case 2: Hoàn thành khám

**Steps:**
1. Hoàn thành tất cả các bước
2. Nhấn nút "Hoàn thành khám"

**Expected:**
- ✅ Phác đồ chuyển sang COMPLETED
- ✅ Hóa đơn được tạo
- ✅ Không thể chỉnh sửa nữa (đúng)
- ✅ Bác sĩ quay về trang chủ

### Test Case 3: Thêm dịch vụ sau khi hoàn thành tất cả bước

**Steps:**
1. Hoàn thành tất cả các bước
2. Nhấn "Thêm dịch vụ"
3. Thêm dịch vụ mới

**Expected:**
- ✅ Có thể thêm dịch vụ (vì phác đồ chưa COMPLETED)
- ✅ Dịch vụ mới ở trạng thái PENDING
- ✅ Có thể hoàn thành dịch vụ mới
- ✅ Sau đó mới nhấn "Hoàn thành khám"

## 📊 Impact

**Trước fix:**
- Phác đồ tự động khóa → Bác sĩ không thể chỉnh sửa
- UX kém → Bác sĩ bối rối
- Phải tạo phác đồ mới nếu muốn thêm dịch vụ

**Sau fix:**
- Bác sĩ kiểm soát hoàn toàn
- UX tốt → Workflow rõ ràng
- Linh hoạt thêm/sửa trước khi hoàn tất

## 🔗 Related Changes

1. **Mobile:** Đổi nút "Thanh toán" → "Hoàn thành khám"
2. **Mobile:** Xóa nút Complete/Cancel dư thừa
3. **Mobile:** Thêm nút "Hóa đơn" cho Patient
4. **Backend:** Fix logic auto-complete (file này)

---

**Status:** ✅ FIXED
**Date:** 2026-03-29
**Priority:** HIGH - Critical UX issue
**Impact:** All doctors using the system
