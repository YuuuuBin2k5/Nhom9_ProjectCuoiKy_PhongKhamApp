# ✅ FIX: TỰ ĐỘNG CHUYỂN TAB KHI NHẤN NÚT "KHÁM BỆNH"

## 🎯 VẤN ĐỀ ĐÃ KHẮC PHỤC

**Vấn đề:** Khi nhấn nút "Khám bệnh" trên một step, app không tự động chuyển đến tab tương ứng với loại dịch vụ.

**Ví dụ:**
- Step "X-quang" → Nhấn "Khám bệnh" → Không chuyển đến tab "X-Quang"
- Step "Phẫu thuật" → Nhấn "Khám bệnh" → Không chuyển đến tab "Phẫu thuật"

**Nguyên nhân:** 
- Logic chuyển tab chỉ có trong `onStepEdit()` (nút "Chỉnh sửa")
- `onStepComplete()` (nút "Khám bệnh") không có logic chuyển tab

## 🔧 GIẢI PHÁP ĐÃ TRIỂN KHAI

### 1. Tạo method helper `switchToTabForStep()`:

```java
/**
 * Switch to the appropriate tab based on step's UI template type
 * and load the fragment with existing data
 */
private void switchToTabForStep(TreatmentPlan.Step step) {
    // Determine which fragment to load based on template type
    Fragment targetFragment = null;
    if (toggleFormType != null && step.getUiTemplateType() != null) {
        String template = step.getUiTemplateType().toUpperCase();
        if (template.contains("SURGERY")) {
            toggleFormType.check(R.id.btnFormSurgery);
            targetFragment = new FragmentSurgeryChecklist();
        } else if (template.contains("ORTHO")) {
            toggleFormType.check(R.id.btnFormOrtho);
            targetFragment = new FragmentOrthodontics();
        } else if (template.contains("XRAY") || template.contains("X-RAY") || template.contains("X_RAY")) {
            toggleFormType.check(R.id.btnFormXray);
            targetFragment = new com.hcmute.mobile_android.ui.fragments.FragmentXray();
        } else {
            toggleFormType.check(R.id.btnFormGeneral);
            targetFragment = new FragmentGeneralDental();
        }
    } else {
        // Default to general dental
        toggleFormType.check(R.id.btnFormGeneral);
        targetFragment = new FragmentGeneralDental();
    }
    
    // Load fragment and populate with existing data...
}
```

### 2. Gọi `switchToTabForStep()` trong cả 2 methods:

#### `onStepEdit()` (nút "Chỉnh sửa"):
```java
@Override
public void onStepEdit(TreatmentPlan.Step step) {
    this.currentStep = step;
    // ... validation logic ...
    
    // Switch to appropriate tab and load fragment
    switchToTabForStep(step);
    
    // ... rest of logic ...
}
```

#### `onStepComplete()` (nút "Khám bệnh"):
```java
@Override
public void onStepComplete(TreatmentPlan.Step step) {
    // Set current step
    this.currentStep = step;
    
    // Switch to appropriate tab based on service type
    switchToTabForStep(step);
    
    // Validate form before completing...
}
```

### Mapping UI Template Type → Tab:

| UI Template Type | Tab được chọn | Fragment |
|-----------------|---------------|----------|
| `SURGERY` | Phẫu thuật | FragmentSurgeryChecklist |
| `ORTHO` | Niềng răng | FragmentOrthodontics |
| `XRAY`, `X-RAY`, `X_RAY` | X-Quang | FragmentXray |
| `GENERAL` hoặc khác | Khám chung | FragmentGeneralDental |

## 📋 HƯỚNG DẪN TEST

### Chuẩn bị:
```bash
# 1. Cài đặt APK mới
adb install -r mobile_android/app/build/outputs/apk/debug/app-debug.apk
```

### Kịch bản test:

#### Test Case 1: Khám bệnh với dịch vụ X-quang
1. Đăng nhập với tài khoản bác sĩ
2. Vào Queue, chọn bệnh nhân
3. Thêm dịch vụ "X-quang" vào treatment plan
4. Nhấn nút "Khám bệnh" trên step X-quang
5. **Kỳ vọng:**
   - ✅ Tab "X-Quang" tự động được chọn (màu xanh)
   - ✅ Fragment X-quang được load
   - ✅ Có thể nhập kết quả đọc phim, chẩn đoán, upload ảnh
   - ✅ Toast hiện: "Nhập kết luận cho: X-quang"

#### Test Case 2: Khám bệnh với dịch vụ Phẫu thuật
1. Thêm dịch vụ "Nhổ răng" (loại SURGERY)
2. Nhấn nút "Khám bệnh" trên step Nhổ răng
3. **Kỳ vọng:**
   - ✅ Tab "Phẫu thuật" tự động được chọn
   - ✅ Fragment Phẫu thuật được load
   - ✅ Có thể chọn checklist các bước phẫu thuật

#### Test Case 3: Khám bệnh với dịch vụ Niềng răng
1. Thêm dịch vụ "Niềng răng" (loại ORTHO)
2. Nhấn nút "Khám bệnh" trên step Niềng răng
3. **Kỳ vọng:**
   - ✅ Tab "Niềng răng" tự động được chọn
   - ✅ Fragment Niềng răng được load

#### Test Case 4: Khám bệnh với dịch vụ Khám tổng quát
1. Thêm dịch vụ "Khám tổng quát" (loại GENERAL)
2. Nhấn nút "Khám bệnh" trên step Khám tổng quát
3. **Kỳ vọng:**
   - ✅ Tab "Khám chung" tự động được chọn
   - ✅ Fragment Khám chung được load
   - ✅ Có thể nhập lý do, chẩn đoán, chọn răng

#### Test Case 5: Chỉnh sửa step đã hoàn thành
1. Hoàn thành step X-quang
2. Nhấn nút "Chỉnh sửa" trên step X-quang
3. **Kỳ vọng:**
   - ✅ Tab "X-Quang" tự động được chọn
   - ✅ Dữ liệu cũ được load vào fragment
   - ✅ Chế độ READ-ONLY, có nút "Chỉnh sửa"

## 🔍 LOGIC FLOW

### Khi nhấn "Khám bệnh":
```
1. User nhấn "Khám bệnh" trên step
   ↓
2. Adapter gọi listener.onStepComplete(step)
   ↓
3. DoctorWorkflowActivity.onStepComplete() được gọi
   ↓
4. Set currentStep = step
   ↓
5. Gọi switchToTabForStep(step)
   ↓
6. Đọc step.getUiTemplateType()
   ↓
7. Chọn tab tương ứng (toggleFormType.check())
   ↓
8. Tạo fragment tương ứng
   ↓
9. Load fragment vào fragmentContainerForm
   ↓
10. Populate dữ liệu cũ (nếu có) vào fragment
   ↓
11. Validate form
   ↓
12. Gọi API complete step
```

### Khi nhấn "Chỉnh sửa":
```
1. User nhấn "Chỉnh sửa" trên step
   ↓
2. Adapter gọi listener.onStepEdit(step)
   ↓
3. DoctorWorkflowActivity.onStepEdit() được gọi
   ↓
4. Set currentStep = step
   ↓
5. Gọi switchToTabForStep(step)
   ↓
6-10. (Giống như trên)
   ↓
11. Load images nếu có
   ↓
12. Hiển thị nút "Hoàn thành" và "Hủy"
```

## 📊 KẾT QUẢ BUILD

```
BUILD SUCCESSFUL in 10s
35 actionable tasks: 9 executed, 26 up-to-date
```

## 📁 FILES MODIFIED

- `mobile_android/app/src/main/java/com/hcmute/mobile_android/ui/activities/staff/DoctorWorkflowActivity.java`
  - Added: `switchToTabForStep(TreatmentPlan.Step step)` method
  - Modified: `onStepEdit()` - now calls `switchToTabForStep()`
  - Modified: `onStepComplete()` - now calls `switchToTabForStep()`

## 🎉 TÍNH NĂNG HOÀN CHỈNH

Giờ workflow mượt mà hơn:

✅ Nhấn "Khám bệnh" → Tự động chuyển đến tab đúng loại dịch vụ
✅ Nhấn "Chỉnh sửa" → Tự động chuyển đến tab đúng loại dịch vụ
✅ Fragment được load với dữ liệu cũ (nếu có)
✅ Bác sĩ không cần tự chuyển tab thủ công
✅ Giảm thiểu lỗi nhập sai tab

## 💡 LƯU Ý

- Nếu step không có `uiTemplateType`, mặc định chuyển đến tab "Khám chung"
- Logic matching template type không phân biệt hoa thường
- Hỗ trợ nhiều biến thể: `XRAY`, `X-RAY`, `X_RAY` đều chuyển đến tab X-Quang

---

**Ngày hoàn thành:** 2026-03-29
**Build status:** ✅ SUCCESS
**APK location:** `mobile_android/app/build/outputs/apk/debug/app-debug.apk`
