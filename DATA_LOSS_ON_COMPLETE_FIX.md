# Fix: Dữ liệu bị mất khi nhấn "Hoàn thành" - UPDATED

## 🔴 VẤN ĐỀ MỚI PHÁT HIỆN

Từ log thực tế, vấn đề KHÔNG phải là cache bị xóa hay fragment bị recreate sau khi complete.

**Vấn đề thực sự**: Khi user nhập dữ liệu và nhấn "Hoàn thành", `getFormDataNotes()` trả về chuỗi RỖNG!

```
15:37:49.136  okhttp.OkHttpClient  {"imageUrls":[],"doctorConclusion":""}
```

## 🔍 PHÂN TÍCH LOG

### Timeline từ log:

1. **15:37:39**: User nhấn "Bắt đầu" → step chuyển IN_PROGRESS ✅
2. **15:37:42**: User click vào răng → mở dialog tooth note ✅
3. **15:37:43**: User nhập dữ liệu → đóng dialog ✅
4. **15:37:44-48**: User click vào EditText khác → keyboard hiện ✅
5. **15:37:49**: User nhấn "Hoàn thành" → `doctorConclusion=""` ❌ **TRỐNG!**

### Các khả năng:

**Khả năng 1**: Fragment bị recreate trước khi nhấn "Hoàn thành"
- Từ log thấy có nhiều `AutofillManager` calls
- Fragment có thể bị destroy và recreate → `toothCustomNotesMap` bị reset

**Khả năng 2**: Fragment instance không đúng
- `completeStepInternal()` lấy fragment từ `getSupportFragmentManager().findFragmentById()`
- Có thể lấy được fragment instance khác (mới tạo) thay vì instance user đang nhập dữ liệu

**Khả năng 3**: Dữ liệu không được lưu vào map
- Logic lưu trong dialog có vấn đề
- `toothCustomNotesMap.put()` không được gọi hoặc bị override

## ✅ FIX ĐÃ THỰC HIỆN

### Fix 1: Thêm debug log vào FragmentGeneralDental

**File**: `FragmentGeneralDental.java`

**Thêm log khi lưu dữ liệu trong dialog**:
```java
view.findViewById(R.id.btnSave).setOnClickListener(v -> {
    // ... existing code ...
    
    android.util.Log.d("FragmentGeneralDental", "=== SAVE TOOTH NOTE ===");
    android.util.Log.d("FragmentGeneralDental", "Tooth: R" + toothNumber);
    android.util.Log.d("FragmentGeneralDental", "Status: " + status + " (" + statusText + ")");
    android.util.Log.d("FragmentGeneralDental", "Custom note: " + customNote);
    android.util.Log.d("FragmentGeneralDental", "Fragment instance: " + FragmentGeneralDental.this);
    
    // ... save to map ...
    
    android.util.Log.d("FragmentGeneralDental", "Map size after save: " + toothCustomNotesMap.size());
    android.util.Log.d("FragmentGeneralDental", "Map content: " + toothCustomNotesMap);
});
```

**Thêm log khi lấy dữ liệu**:
```java
public String getFormDataNotes() {
    android.util.Log.d("FragmentGeneralDental", "=== getFormDataNotes DEBUG ===");
    android.util.Log.d("FragmentGeneralDental", "Fragment instance: " + this);
    android.util.Log.d("FragmentGeneralDental", "toothCustomNotesMap size: " + toothCustomNotesMap.size());
    android.util.Log.d("FragmentGeneralDental", "toothCustomNotesMap: " + toothCustomNotesMap);
    android.util.Log.d("FragmentGeneralDental", "etReason: " + reason);
    android.util.Log.d("FragmentGeneralDental", "etDiagnosis: " + diagnosis);
    android.util.Log.d("FragmentGeneralDental", "Final result: " + result);
    
    // ... existing code ...
}
```

### Fix 2-4: Các fix trước đó (vẫn giữ nguyên)

- Fix 2: Không xóa cache trong `autoLoadInProgressStep()`
- Fix 3: Không tự động select tab sau khi reload plan  
- Fix 4: Lưu dữ liệu vào cache trước khi complete

## 📋 HƯỚNG DẪN TEST

### Bước 1: Cài đặt APK mới với debug log

```bash
cd mobile_android
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Bước 2: Test và thu thập log

1. Mở app → Login as Doctor
2. Chọn bệnh nhân → Tạo phác đồ với "Khám tổng quát"
3. Nhấn "Bắt đầu"
4. **Nhập dữ liệu**:
   - Lý do: "Test lý do"
   - Chẩn đoán: "Test chẩn đoán"
   - Click răng R12 → chọn "Sâu răng" → nhập note "Test note"
5. Nhấn "Hoàn thành"
6. **Thu thập log**:
   ```bash
   adb logcat -d | grep -E "FragmentGeneralDental|DoctorWorkflow|okhttp" > debug_log.txt
   ```

### Bước 3: Phân tích log

Tìm các dòng log sau:

**Khi lưu dữ liệu trong dialog**:
```
FragmentGeneralDental: === SAVE TOOTH NOTE ===
FragmentGeneralDental: Tooth: R12
FragmentGeneralDental: Status: caries (Sâu răng)
FragmentGeneralDental: Custom note: Test note
FragmentGeneralDental: Fragment instance: FragmentGeneralDental@abc123
FragmentGeneralDental: Map size after save: 1
FragmentGeneralDental: Map content: {12=Sâu răng - Test note}
```

**Khi nhấn "Hoàn thành"**:
```
FragmentGeneralDental: === getFormDataNotes DEBUG ===
FragmentGeneralDental: Fragment instance: FragmentGeneralDental@abc123  <-- Phải GIỐNG instance ở trên!
FragmentGeneralDental: toothCustomNotesMap size: 1  <-- Phải > 0!
FragmentGeneralDental: toothCustomNotesMap: {12=Sâu răng - Test note}
FragmentGeneralDental: etReason: Test lý do
FragmentGeneralDental: etDiagnosis: Test chẩn đoán
FragmentGeneralDental: Final result: Lý do: Test lý do\nChẩn đoán: Test chẩn đoán\nTình trạng răng:\n- R12: Sâu răng - Test note
```

### Bước 4: Xác định root cause

**Nếu fragment instance KHÁC NHAU**:
→ Fragment bị recreate → Cần fix logic tạo fragment

**Nếu toothCustomNotesMap.size() == 0**:
→ Map bị reset → Cần kiểm tra lifecycle của fragment

**Nếu etReason/etDiagnosis trống**:
→ EditText bị reset → Cần kiểm tra view binding

## 🎯 NEXT STEPS

1. ✅ Đã thêm debug log
2. ⏳ Chờ user test và cung cấp log
3. ⏳ Phân tích log để xác định root cause chính xác
4. ⏳ Implement fix dựa trên root cause

---

**Status**: 🔄 IN PROGRESS - Chờ log từ user
**Date**: 2026-03-29
**Files Modified**:
- `mobile_android/app/src/main/java/com/hcmute/mobile_android/ui/fragments/FragmentGeneralDental.java` (Added debug logs)
- `mobile_android/app/src/main/java/com/hcmute/mobile_android/ui/activities/staff/DoctorWorkflowActivity.java` (Previous fixes)
