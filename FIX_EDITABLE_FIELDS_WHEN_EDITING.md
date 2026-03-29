# FIX: Cho phép chỉnh sửa các trường khi bấm "Chỉnh sửa"

## VẤN ĐỀ

Khi bấm nút "Chỉnh sửa" trên bước đã hoàn thành, các trường "Lý do khám" và "Chẩn đoán sơ bộ" bị khóa (read-only), không cho phép chỉnh sửa.

## NGUYÊN NHÂN

Trong method `switchToTabForStep()`, code đang kiểm tra `step.isCompleted()` để quyết định có set read-only mode hay không.

**Vấn đề**: Khi user bấm "Chỉnh sửa":
1. `onStepEdit()` gọi API `cancelTreatmentStep` để đổi status từ COMPLETED → IN_PROGRESS
2. Sau đó gọi `continueStepEdit()` → `switchToTabForStep()`
3. Nhưng `switchToTabForStep()` vẫn dùng `step.isCompleted()` để kiểm tra
4. `isCompleted()` trả về `true` vì nó check status ban đầu
5. → Set read-only mode = true → Không cho chỉnh sửa ❌

## GIẢI PHÁP

Thay vì dùng `step.isCompleted()`, kiểm tra TRỰC TIẾP status hiện tại của step:

```java
// TRƯỚC (SAI):
boolean isStepCompleted = step.isCompleted();

// SAU (ĐÚNG):
boolean shouldBeReadOnly = "COMPLETED".equals(step.getStatus());
```

Khi user bấm "Chỉnh sửa":
1. Status đã được đổi sang "IN_PROGRESS"
2. `shouldBeReadOnly` = false
3. → Không set read-only mode
4. → Cho phép chỉnh sửa ✅

## THAY ĐỔI CODE

### File: `DoctorWorkflowActivity.java`

#### 1. Method `switchToTabForStep()`

**TRƯỚC**:
```java
String existingConclusion = step.getDoctorConclusion();
boolean isStepCompleted = step.isCompleted();
final Fragment finalFragment = targetFragment;

findViewById(R.id.fragmentContainerForm).post(() -> {
    if (finalFragment instanceof FragmentGeneralDental) {
        if (existingConclusion != null && !existingConclusion.trim().isEmpty()) {
            ((FragmentGeneralDental) finalFragment).setData(existingConclusion);
        }
        // Set read-only mode if step is completed
        if (isStepCompleted) {
            ((FragmentGeneralDental) finalFragment).setReadOnlyMode(true);
        }
    }
    // ... tương tự cho các fragment khác
});
```

**SAU**:
```java
String existingConclusion = step.getDoctorConclusion();
// CRITICAL FIX: Check CURRENT status, not isCompleted()
boolean shouldBeReadOnly = "COMPLETED".equals(step.getStatus());
final Fragment finalFragment = targetFragment;

findViewById(R.id.fragmentContainerForm).post(() -> {
    if (finalFragment instanceof FragmentGeneralDental) {
        if (existingConclusion != null && !existingConclusion.trim().isEmpty()) {
            ((FragmentGeneralDental) finalFragment).setData(existingConclusion);
        }
        // Set read-only mode ONLY if step is still COMPLETED (not being edited)
        if (shouldBeReadOnly) {
            ((FragmentGeneralDental) finalFragment).setReadOnlyMode(true);
        } else {
            // Ensure editable mode when editing
            ((FragmentGeneralDental) finalFragment).setReadOnlyMode(false);
        }
    }
    // ... tương tự cho các fragment khác
});
```

**Thay đổi cho TẤT CẢ fragment types**:
- `FragmentGeneralDental`
- `FragmentSurgeryChecklist`
- `FragmentOrthodontics`
- `FragmentXray`

#### 2. Method `continueStepEdit()` - Phần không cần switch tab

**TRƯỚC**:
```java
// Fragment already matches, just load existing data if step is completed
if (step.isCompleted() && step.getDoctorConclusion() != null && !step.getDoctorConclusion().trim().isEmpty()) {
    if (currentFragment instanceof FragmentGeneralDental) {
        ((FragmentGeneralDental) currentFragment).setData(step.getDoctorConclusion());
    }
    // ... không có setReadOnlyMode(false)
}
```

**SAU**:
```java
// Fragment already matches, just load existing data if step has data
if (step.getDoctorConclusion() != null && !step.getDoctorConclusion().trim().isEmpty()) {
    if (currentFragment instanceof FragmentGeneralDental) {
        ((FragmentGeneralDental) currentFragment).setData(step.getDoctorConclusion());
        // Ensure editable mode when editing (status is already IN_PROGRESS)
        ((FragmentGeneralDental) currentFragment).setReadOnlyMode(false);
    }
    // ... tương tự cho các fragment khác
}
```

**Thay đổi**:
1. Bỏ điều kiện `step.isCompleted()` - load data cho cả step đang edit
2. Thêm `setReadOnlyMode(false)` để đảm bảo editable

## LUỒNG HOẠT ĐỘNG SAU KHI FIX

### Khi user bấm "Chỉnh sửa" trên bước COMPLETED:

1. **`onStepEdit()`** được gọi
   - Phát hiện status = "COMPLETED"
   - Gọi API `cancelTreatmentStep(stepId)`

2. **Backend xử lý**
   - Đổi step status: COMPLETED → IN_PROGRESS
   - Trả về success

3. **Callback success**
   - Update local: `step.setStatus("IN_PROGRESS")`
   - Gọi `continueStepEdit(step)`

4. **`continueStepEdit()`**
   - Kiểm tra có cần switch tab không
   - Nếu CẦN: Gọi `switchToTabForStep(step)`
   - Nếu KHÔNG CẦN: Load data trực tiếp + `setReadOnlyMode(false)`

5. **`switchToTabForStep()`** (nếu được gọi)
   - Tạo fragment mới
   - Kiểm tra: `shouldBeReadOnly = "COMPLETED".equals(step.getStatus())`
   - Vì status = "IN_PROGRESS" → `shouldBeReadOnly = false`
   - Load data + `setReadOnlyMode(false)`

6. **Kết quả**
   - ✅ Tất cả các trường có thể chỉnh sửa
   - ✅ Sơ đồ răng có thể chọn/bỏ chọn răng
   - ✅ Có thể nhập/sửa "Lý do khám"
   - ✅ Có thể nhập/sửa "Chẩn đoán sơ bộ"

## TESTING

### Test Case: Chỉnh sửa bước đã hoàn thành

1. **Hoàn thành một bước**
   - Tạo dịch vụ "Khám và tư vấn răng miệng"
   - Nhập đầy đủ: chọn răng, lý do, chẩn đoán
   - Bấm "Hoàn thành"
   - ✅ Bước chuyển sang COMPLETED

2. **Bấm "Chỉnh sửa"**
   - Bấm nút "Chỉnh sửa" trên bước vừa hoàn thành
   - ✅ Toast: "Đang chỉnh sửa lại: Khám và tư vấn răng miệng"
   - ✅ Sơ đồ răng hiển thị đầy đủ răng đã chọn
   - ✅ Trường "Lý do khám" hiển thị dữ liệu và CHO PHÉP CHỈNH SỬA
   - ✅ Trường "Chẩn đoán sơ bộ" hiển thị dữ liệu và CHO PHÉP CHỈNH SỬA
   - ✅ Có thể chọn/bỏ chọn răng trên sơ đồ

3. **Chỉnh sửa dữ liệu**
   - Sửa "Lý do khám"
   - Sửa "Chẩn đoán sơ bộ"
   - Thêm/bớt răng
   - ✅ Tất cả thao tác hoạt động bình thường

4. **Hoàn thành lại**
   - Bấm "Hoàn thành"
   - ✅ Không có lỗi
   - ✅ Dữ liệu mới được lưu

## FILES MODIFIED

- `mobile_android/app/src/main/java/com/hcmute/mobile_android/ui/activities/staff/DoctorWorkflowActivity.java`
  - Method `switchToTabForStep()`: Đổi từ `isStepCompleted` sang `shouldBeReadOnly`
  - Method `continueStepEdit()`: Thêm `setReadOnlyMode(false)` khi load data

## BUILD STATUS

✅ **Build Successful**
- APK: `mobile_android/app/build/outputs/apk/debug/app-debug.apk`
- No compilation errors

## SUMMARY

**Vấn đề**: Không thể chỉnh sửa các trường khi bấm "Chỉnh sửa" bước đã hoàn thành

**Nguyên nhân**: Dùng `step.isCompleted()` thay vì kiểm tra status hiện tại

**Giải pháp**: Kiểm tra `step.getStatus()` trực tiếp để quyết định read-only mode

**Kết quả**: Tất cả các trường có thể chỉnh sửa khi user bấm "Chỉnh sửa" ✅
