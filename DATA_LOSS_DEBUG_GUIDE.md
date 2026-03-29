# Hướng dẫn Debug vấn đề mất dữ liệu khi nhấn "Hoàn thành"

## Tóm tắt vấn đề
User báo: Khi nhập dữ liệu vào form (FragmentGeneralDental) và nhấn "Hoàn thành", dữ liệu bị mất và không được gửi lên server.

## Các fix đã thực hiện

### 1. Fix compile errors sau khi revert
- ✅ Thêm method `onStepSave()` vào `DoctorWorkflowActivity` (yêu cầu bởi interface `OnStepActionListener`)
- ✅ Xóa các lời gọi `triggerImageUpload()` trong `FragmentXray` và `FragmentOrthodontics` (method không tồn tại trong code cũ)

### 2. Thêm debug logging
Đã thêm log chi tiết vào 2 file:

#### FragmentGeneralDental.java
- Log khi fragment được tạo (onViewCreated) - track fragment instance
- Log khi lưu tooth note - track dữ liệu được lưu vào map
- Log khi gọi getFormDataNotes() - track dữ liệu được lấy ra

#### DoctorWorkflowActivity.java
- Log khi onStepComplete() được gọi
- Log fragment instance được tìm thấy
- Log dữ liệu được lấy từ EditText
- Log request body gửi lên server
- Log response từ server

## Cách test và thu thập log

### Bước 1: Cài đặt APK mới
```bash
# APK đã được build tại:
mobile_android/app/build/outputs/apk/debug/app-debug.apk

# Cài đặt:
adb install -r mobile_android/app/build/outputs/apk/debug/app-debug.apk
```

### Bước 2: Bật logcat để xem log
```bash
# Mở terminal và chạy:
adb logcat -s FragmentGeneralDental:D DoctorWorkflowActivity:D

# Hoặc xem tất cả log:
adb logcat | grep -E "(FragmentGeneralDental|DoctorWorkflowActivity)"
```

### Bước 3: Thực hiện test case
1. Mở app và đăng nhập với tài khoản bác sĩ
2. Quét QR hoặc nhập mã bệnh nhân
3. Chọn một dịch vụ và nhấn "Bắt đầu"
4. Nhập dữ liệu vào form:
   - Nhập "Lý do khám"
   - Nhập "Chẩn đoán"
   - Nhấn vào răng trên sơ đồ và thêm ghi chú
5. Nhấn nút "Hoàn thành"
6. Quan sát log trong terminal

### Bước 4: Phân tích log

#### Log mong đợi khi THÀNH CÔNG:
```
FragmentGeneralDental: === onViewCreated ===
FragmentGeneralDental: Fragment instance: 123456789

FragmentGeneralDental: === Saved tooth note ===
FragmentGeneralDental: Fragment instance: 123456789
FragmentGeneralDental: Tooth: 11
FragmentGeneralDental: Status: Sâu răng
FragmentGeneralDental: Custom note: 'Sâu nặng'
FragmentGeneralDental: toothCustomNotesMap size after save: 1
FragmentGeneralDental: toothCustomNotesMap content: {11=Sâu răng - Sâu nặng}

DoctorWorkflowActivity: === onStepComplete() called ===
DoctorWorkflowActivity: Fragment found: FragmentGeneralDental
DoctorWorkflowActivity: Fragment instance: 123456789
DoctorWorkflowActivity: Reason from EditText: 'Đau răng'
DoctorWorkflowActivity: Diagnosis from EditText: 'Sâu răng số 11'
DoctorWorkflowActivity: Final notes to send: 'Lý do: Đau răng
Chẩn đoán: Sâu răng số 11
Điều trị: 1 răng.'
```

#### Các dấu hiệu BUG:

**Bug 1: Fragment bị recreate**
```
FragmentGeneralDental: === onViewCreated ===
FragmentGeneralDental: Fragment instance: 123456789

FragmentGeneralDental: === Saved tooth note ===
FragmentGeneralDental: Fragment instance: 123456789
...

DoctorWorkflowActivity: Fragment instance: 987654321  <-- KHÁC instance!
```
→ Fragment bị recreate, dữ liệu trong map bị mất

**Bug 2: Fragment instance không đúng**
```
DoctorWorkflowActivity: Fragment found: null
```
→ Không tìm thấy fragment trong container

**Bug 3: Map bị reset**
```
FragmentGeneralDental: toothCustomNotesMap size after save: 1
...
FragmentGeneralDental: === getFormDataNotes() called ===
FragmentGeneralDental: toothCustomNotesMap size: 0  <-- BỊ RESET!
```
→ Map bị clear giữa chừng

**Bug 4: EditText bị clear**
```
DoctorWorkflowActivity: Reason from EditText: ''  <-- RỖNG!
DoctorWorkflowActivity: Diagnosis from EditText: ''  <-- RỖNG!
```
→ EditText bị clear hoặc không lưu được

## Các khả năng và cách fix

### Khả năng 1: Fragment bị recreate do configuration change
**Nguyên nhân**: Xoay màn hình, thay đổi ngôn ngữ, hoặc hệ thống kill process
**Fix**: Lưu state trong `onSaveInstanceState()` và restore trong `onViewStateRestored()`

### Khả năng 2: Fragment bị replace khi nhấn "Hoàn thành"
**Nguyên nhân**: Code có thể replace fragment trước khi lấy dữ liệu
**Fix**: Lấy dữ liệu TRƯỚC khi replace fragment

### Khả năng 3: Fragment instance không đúng
**Nguyên nhân**: `getSupportFragmentManager().findFragmentById()` trả về fragment cũ hoặc null
**Fix**: Lưu reference đến fragment khi tạo, không dùng findFragmentById

### Khả năng 4: Memory leak hoặc GC
**Nguyên nhân**: Fragment bị GC thu hồi, map bị clear
**Fix**: Lưu dữ liệu vào Activity thay vì Fragment

## Next steps

1. **Thu thập log từ user** - Chạy test case và gửi log đầy đủ
2. **Phân tích log** - Xác định chính xác bug nào đang xảy ra
3. **Implement fix** - Dựa trên phân tích để fix đúng root cause
4. **Test lại** - Verify fix hoạt động đúng

## Files đã sửa

1. `mobile_android/app/src/main/java/com/hcmute/mobile_android/ui/activities/staff/DoctorWorkflowActivity.java`
   - Thêm method `onStepSave()`
   - Thêm debug log trong `onStepComplete()`

2. `mobile_android/app/src/main/java/com/hcmute/mobile_android/ui/fragments/FragmentGeneralDental.java`
   - Thêm debug log trong `onViewCreated()`, `showToothNoteDialog()`, `getFormDataNotes()`

3. `mobile_android/app/src/main/java/com/hcmute/mobile_android/ui/fragments/FragmentXray.java`
   - Xóa lời gọi `triggerImageUpload()`

4. `mobile_android/app/src/main/java/com/hcmute/mobile_android/ui/fragments/FragmentOrthodontics.java`
   - Xóa lời gọi `triggerImageUpload()`

## Build info
- Build time: 2026-03-29
- Build successful: ✅
- APK location: `mobile_android/app/build/outputs/apk/debug/app-debug.apk`
