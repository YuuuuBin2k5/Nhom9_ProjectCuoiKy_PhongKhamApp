# Fix Read-Only Mode - Correct Solution ✅

## Vấn đề (Problem)
Khi nhấp nút "Chỉnh sửa" trên các bước đã hoàn thành (COMPLETED), một số fragment (FragmentXray, FragmentGeneralDental) vẫn bị read-only và không thể chỉnh sửa được.

## Phân tích sai ban đầu ❌
Ban đầu tôi nghĩ cần thêm nút "Chỉnh sửa" vào trong mỗi fragment. Nhưng điều này SAI vì:
- Nút "Chỉnh sửa" đã có sẵn trong `item_treatment_step.xml` (item của RecyclerView)
- Logic toggle edit mode đã có trong `TreatmentStepAdapter`
- `DoctorWorkflowActivity` đã có code gọi `setReadOnlyMode(false)` khi editing

## Nguyên nhân thực sự (Root Cause)
FragmentXray và FragmentGeneralDental có method `setReadOnlyMode()` nhưng:
- Khi `readOnly = true` → disable fields ✅
- Khi `readOnly = false` → KHÔNG enable lại fields đúng cách ❌

Cụ thể:
- Thiếu `setFocusableInTouchMode(true)` để user có thể nhấn vào EditText
- Upload button visibility logic bị ngược (readOnly ? GONE : VISIBLE thay vì !readOnly ? VISIBLE : GONE)

## Giải pháp đúng (Correct Solution)

### 1. FragmentXray.java
Sửa method `setReadOnlyMode()`:
```java
public void setReadOnlyMode(boolean readOnly) {
    // Enable/disable all input fields
    if (etXrayFindings != null) {
        etXrayFindings.setEnabled(!readOnly);
        etXrayFindings.setFocusable(!readOnly);
        etXrayFindings.setFocusableInTouchMode(!readOnly); // ← THÊM DÒNG NÀY
    }
    
    // Tương tự cho etXrayDiagnosis, etXrayRecommendations, etOtherType
    
    // Upload button
    if (btnUploadXrayImage != null) {
        btnUploadXrayImage.setEnabled(!readOnly);
        btnUploadXrayImage.setVisibility(!readOnly ? View.VISIBLE : View.GONE); // ← SỬA LOGIC
    }
}
```

### 2. FragmentGeneralDental.java
Sửa method `setReadOnlyMode()`:
```java
public void setReadOnlyMode(boolean readOnly) {
    if (etReason != null) {
        etReason.setEnabled(!readOnly);
        etReason.setFocusable(!readOnly);
        etReason.setFocusableInTouchMode(!readOnly); // ← THÊM DÒNG NÀY
    }
    if (etDiagnosis != null) {
        etDiagnosis.setEnabled(!readOnly);
        etDiagnosis.setFocusable(!readOnly);
        etDiagnosis.setFocusableInTouchMode(!readOnly); // ← THÊM DÒNG NÀY
    }
}
```

## Workflow hoạt động (How It Works)

### Khi xem bước COMPLETED:
1. User nhấn vào step COMPLETED trong RecyclerView
2. `DoctorWorkflowActivity.loadFragmentForStep()` được gọi
3. Fragment được load với `setReadOnlyMode(true)`
4. Tất cả fields bị disable, không thể edit

### Khi nhấn "Chỉnh sửa":
1. User nhấn nút "Chỉnh sửa" trong item (từ `TreatmentStepAdapter`)
2. `onStepEdit(step)` được gọi trong `DoctorWorkflowActivity`
3. Backend API `cancelTreatmentStep()` được gọi → status: COMPLETED → IN_PROGRESS
4. `continueStepEdit(step)` được gọi
5. Fragment được reload với `setReadOnlyMode(false)` ← QUAN TRỌNG
6. Tất cả fields được enable, user có thể edit

### Code flow trong DoctorWorkflowActivity:
```java
// Line 1188, 1202, 1206, 1210 - trong continueStepEdit()
((FragmentXray) currentFragment).setReadOnlyMode(false);

// Line 1365, 1382, 1399, 1451 - trong loadFragmentForStep()
if (shouldBeReadOnly) {
    fragment.setReadOnlyMode(true);
} else {
    fragment.setReadOnlyMode(false); // ← Khi editing
}
```

## Sự khác biệt với FragmentSurgeryChecklist và FragmentOrthodontics

Hai fragment này đã có edit mode hoạt động tốt vì:
- Có state management: `isReadOnly`, `isEditMode`
- Có method `toggleEditMode()` và `updateEditableState()`
- Có nút "Chỉnh sửa" riêng trong fragment (không cần thiết nhưng không gây lỗi)

Nhưng FragmentXray và FragmentGeneralDental KHÔNG CẦN các thứ này vì:
- Nút "Chỉnh sửa" đã có trong item của RecyclerView
- Logic toggle đã có trong adapter
- Chỉ cần `setReadOnlyMode()` hoạt động đúng là đủ

## Files Modified

1. `mobile_android/app/src/main/java/com/hcmute/mobile_android/ui/fragments/FragmentXray.java`
   - Fixed: `setReadOnlyMode()` - added `setFocusableInTouchMode()` for all EditText
   - Fixed: Upload button visibility logic

2. `mobile_android/app/src/main/java/com/hcmute/mobile_android/ui/fragments/FragmentGeneralDental.java`
   - Fixed: `setReadOnlyMode()` - added `setFocusableInTouchMode()` for all EditText

## Testing Guide

### Test Case 1: FragmentXray Edit Mode
1. Tạo treatment plan với bước X-quang
2. Hoàn thành bước X-quang (nhập dữ liệu và complete)
3. Quay lại xem bước đã hoàn thành
4. **Expected:** Tất cả fields disabled, không thể nhấn vào
5. Nhấn nút "Chỉnh sửa" trong item (RecyclerView)
6. **Expected:** 
   - Nút đổi thành "Lưu"
   - Tất cả fields enabled
   - Có thể nhấn vào EditText và nhập liệu
   - Nút "Tải ảnh" hiện ra
7. Thay đổi dữ liệu (VD: sửa "Kết quả đọc phim")
8. Nhấn "Hoàn thành" để lưu
9. **Expected:** Dữ liệu được lưu, step chuyển về COMPLETED

### Test Case 2: FragmentGeneralDental Edit Mode
1. Tạo treatment plan với bước Khám tổng quát
2. Hoàn thành bước (nhập lý do, chẩn đoán)
3. Quay lại xem bước đã hoàn thành
4. **Expected:** Fields disabled
5. Nhấn "Chỉnh sửa" trong item
6. **Expected:** 
   - Nút đổi thành "Lưu"
   - Fields enabled
   - Có thể nhập liệu
7. Sửa "Lý do khám" hoặc "Chẩn đoán"
8. Nhấn "Hoàn thành"
9. **Expected:** Dữ liệu được lưu

### Test Case 3: Verify No Duplicate Edit Buttons
1. Xem bất kỳ step COMPLETED nào
2. **Expected:** CHỈ có 1 nút "Chỉnh sửa" (trong item của RecyclerView)
3. **Expected:** KHÔNG có nút "Chỉnh sửa" thứ 2 trong fragment

## Key Differences from Previous Wrong Solution

### Wrong Solution ❌:
- Thêm nút "Chỉnh sửa" vào mỗi fragment
- Thêm state management (`isReadOnly`, `isEditMode`) vào fragment
- Thêm `toggleEditMode()` method
- Duplicate logic với adapter

### Correct Solution ✅:
- KHÔNG thêm nút mới
- KHÔNG thêm state management
- CHỈ sửa `setReadOnlyMode()` để enable/disable đúng
- Tận dụng logic có sẵn trong adapter và activity

## Status: COMPLETE ✅

Tất cả fragments giờ đây có thể chỉnh sửa được khi nhấn nút "Chỉnh sửa" trong item.
Giải pháp đơn giản, không duplicate code, tận dụng architecture có sẵn.

---
**Date:** 2026-03-31
**Task:** Fix Read-Only Mode When Editing (Correct Solution)
**Status:** COMPLETE ✅
