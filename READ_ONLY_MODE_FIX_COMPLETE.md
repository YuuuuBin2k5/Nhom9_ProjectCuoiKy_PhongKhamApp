# Fix Read-Only Mode When Editing - COMPLETE ✅

## Vấn đề (Problem)
Khi nhấp "Chỉnh sửa" trên các bước đã hoàn thành (COMPLETED), một số fragment bị read-only và không thể chỉnh sửa được.

## Phân tích (Analysis)

### Trạng thái ban đầu:
1. **FragmentSurgeryChecklist** ✅ - Đã có nút "Chỉnh sửa" và logic `toggleEditMode()` hoạt động đầy đủ
2. **FragmentOrthodontics** ✅ - Đã có nút "Chỉnh sửa" và logic `toggleEditMode()` hoạt động đầy đủ
3. **FragmentXray** ❌ - Có nút trong XML nhưng KHÔNG có logic xử lý
4. **FragmentGeneralDental** ❌ - KHÔNG có nút và KHÔNG có logic xử lý

### Nguyên nhân:
- FragmentXray và FragmentGeneralDental chỉ có method `setReadOnlyMode()` để disable fields
- Không có cơ chế toggle để enable lại khi user nhấn "Chỉnh sửa"

## Giải pháp (Solution)

### 1. FragmentXray.java
**Thêm các thành phần:**
- `btnEditMode` button reference
- `isReadOnly` và `isEditMode` state variables
- `toggleEditMode()` method - chuyển đổi giữa chế độ xem và chỉnh sửa
- `updateEditableState()` method - cập nhật trạng thái enable/disable của tất cả fields
- Refactor `setReadOnlyMode()` để sử dụng `updateEditableState()`

**Hành vi:**
- Khi `isReadOnly = true` → hiện nút "Chỉnh sửa", tất cả fields disabled
- Nhấn "Chỉnh sửa" → `isEditMode = true` → nút đổi thành "Lưu", fields enabled
- Nhấn "Lưu" → `isEditMode = false` → nút đổi lại "Chỉnh sửa", fields disabled, hiện toast "Đã lưu thay đổi"

**Fields được quản lý:**
- `etXrayFindings` - Kết quả đọc phim
- `etXrayDiagnosis` - Chẩn đoán
- `etXrayRecommendations` - Khuyến nghị
- `etOtherType` - Loại X-quang khác
- `rgImageType` - Radio group chọn loại hình ảnh
- `btnUploadXrayImage` - Nút tải ảnh

### 2. FragmentGeneralDental.java
**Thêm các thành phần:**
- `btnEditMode` button (thêm vào XML layout)
- `isReadOnly` và `isEditMode` state variables
- `toggleEditMode()` method
- `updateEditableState()` method
- Refactor `setReadOnlyMode()` để sử dụng `updateEditableState()`

**Hành vi:**
- Tương tự FragmentXray
- Chỉ quản lý 2 fields: `etReason` và `etDiagnosis`
- Tooth notes (ghi chú răng) KHÔNG thể edit trực tiếp, phải dùng dialog odontogram

**Fields được quản lý:**
- `etReason` - Lý do khám
- `etDiagnosis` - Chẩn đoán
- Note: `toothCustomNotesMap` không được edit trong chế độ này

### 3. Layout Changes
**fragment_general_dental.xml:**
- Thêm `btnEditMode` button ở đầu layout
- Style: `Widget.Material3.Button.TonalButton`
- Visibility: `gone` (mặc định ẩn)
- Icon: `ic_menu_edit`

## Cơ chế hoạt động (How It Works)

### State Machine:
```
NORMAL MODE (isReadOnly=false, isEditMode=false)
  ↓
  setReadOnlyMode(true) được gọi khi load COMPLETED step
  ↓
READ-ONLY MODE (isReadOnly=true, isEditMode=false)
  - Hiện nút "Chỉnh sửa"
  - Tất cả fields disabled
  ↓
  User nhấn "Chỉnh sửa"
  ↓
EDIT MODE (isReadOnly=true, isEditMode=true)
  - Nút đổi thành "Lưu"
  - Tất cả fields enabled
  ↓
  User nhấn "Lưu"
  ↓
READ-ONLY MODE (isReadOnly=true, isEditMode=false)
  - Nút đổi lại "Chỉnh sửa"
  - Tất cả fields disabled
  - Toast: "Đã lưu thay đổi"
```

### Logic điều kiện:
```java
boolean canEdit = !isReadOnly || isEditMode;
```
- Nếu `isReadOnly = false` → `canEdit = true` (chế độ bình thường)
- Nếu `isReadOnly = true` và `isEditMode = false` → `canEdit = false` (read-only)
- Nếu `isReadOnly = true` và `isEditMode = true` → `canEdit = true` (đang edit)

## Files Modified

### Java Files:
1. `mobile_android/app/src/main/java/com/hcmute/mobile_android/ui/fragments/FragmentXray.java`
   - Added: `btnEditMode`, `isReadOnly`, `isEditMode` fields
   - Added: `toggleEditMode()`, `updateEditableState()` methods
   - Modified: `setReadOnlyMode()` to use new state management
   - Modified: `onViewCreated()` to setup button click listener
   - Modified: `onSaveInstanceState()` to save edit mode state

2. `mobile_android/app/src/main/java/com/hcmute/mobile_android/ui/fragments/FragmentGeneralDental.java`
   - Added: `btnEditMode`, `isReadOnly`, `isEditMode` fields
   - Added: `toggleEditMode()`, `updateEditableState()` methods
   - Modified: `setReadOnlyMode()` to use new state management
   - Modified: `onViewCreated()` to setup button click listener
   - Added: `import android.widget.Button;`

### XML Files:
3. `mobile_android/app/src/main/res/layout/fragment_general_dental.xml`
   - Added: `btnEditMode` button at the top of layout

## Testing Guide

### Test Case 1: FragmentXray Edit Mode
1. Tạo treatment plan với bước X-quang
2. Hoàn thành bước X-quang (nhập dữ liệu và complete)
3. Quay lại xem bước đã hoàn thành
4. **Expected:** Nút "Chỉnh sửa" hiện ra, tất cả fields disabled
5. Nhấn "Chỉnh sửa"
6. **Expected:** Nút đổi thành "Lưu", tất cả fields enabled
7. Thay đổi dữ liệu (VD: sửa "Kết quả đọc phim")
8. Nhấn "Lưu"
9. **Expected:** Nút đổi lại "Chỉnh sửa", fields disabled, toast "Đã lưu thay đổi"

### Test Case 2: FragmentGeneralDental Edit Mode
1. Tạo treatment plan với bước Khám tổng quát
2. Hoàn thành bước (nhập lý do, chẩn đoán)
3. Quay lại xem bước đã hoàn thành
4. **Expected:** Nút "Chỉnh sửa" hiện ra, fields disabled
5. Nhấn "Chỉnh sửa"
6. **Expected:** Nút đổi thành "Lưu", fields enabled
7. Sửa "Lý do khám" hoặc "Chẩn đoán"
8. Nhấn "Lưu"
9. **Expected:** Nút đổi lại "Chỉnh sửa", fields disabled, toast "Đã lưu thay đổi"

### Test Case 3: FragmentSurgeryChecklist (Existing)
1. Verify existing edit mode still works
2. **Expected:** Behavior unchanged, edit mode works as before

### Test Case 4: FragmentOrthodontics (Existing)
1. Verify existing edit mode still works
2. **Expected:** Behavior unchanged, edit mode works as before

## Consistency Across All Fragments

Tất cả 4 fragments giờ đây có cùng pattern:
1. ✅ `setReadOnlyMode(boolean)` - set read-only state
2. ✅ `toggleEditMode()` - toggle between view and edit
3. ✅ `updateEditableState()` - update UI based on state
4. ✅ `btnEditMode` button - visible only in read-only mode
5. ✅ State management: `isReadOnly`, `isEditMode`

## Status: COMPLETE ✅

Tất cả fragments giờ đây đều có khả năng chỉnh sửa khi ở chế độ read-only.
User có thể nhấn "Chỉnh sửa" để enable fields, sửa dữ liệu, và nhấn "Lưu" để lưu thay đổi.

---
**Date:** 2026-03-31
**Task:** Fix Read-Only Mode When Editing
**Status:** COMPLETE ✅
