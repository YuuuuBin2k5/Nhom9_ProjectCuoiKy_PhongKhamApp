# TIỂU PHẪU & NIỀNG RĂNG - COMPREHENSIVE TEST PLAN

## 🔍 PHÂN TÍCH CODE - PHÁT HIỆN VẤN ĐỀ

### ❌ FRAGMENT SURGERY CHECKLIST - CRITICAL ISSUES

#### 1. **NULL POINTER RISK - getFormDataNotes()**
```java
sb.append("[Sinh hiệu] BP: ").append(etBloodPressure.getText().toString().trim())
```
**Vấn đề**: Nếu `etBloodPressure` là null, app sẽ crash
**Edge case**: Fragment được gọi trước khi `onViewCreated()` hoàn thành

#### 2. **MISSING VALIDATION**
- Không validate format huyết áp (phải là "120/80")
- Không validate nhịp tim (phải là số, trong khoảng hợp lý 40-200)
- Có thể lưu form trống hoàn toàn
- Không check xung đột: Nếu check "Máu khó đông" + "Dị ứng thuốc tê" → cảnh báo nguy hiểm

#### 3. **PARSING BUG - Multi-line Notes**
```java
else if (line.startsWith("- Ghi chú:")) {
    String note = line.substring("- Ghi chú:".length()).trim();
```
**Vấn đề**: Chỉ lấy 1 dòng, nếu ghi chú có nhiều dòng thì mất data
**Edge case**:
```
- Ghi chú: Bệnh nhân lo lắng
Cần theo dõi sau phẫu thuật
Tái khám sau 1 tuần
```
→ Chỉ lấy được "Bệnh nhân lo lắng"

#### 4. **CHECKBOX STATE NOT PRESERVED**
- Nếu parse fail, checkbox không được set
- Không có fallback nếu text không match chính xác
- Case-sensitive: "- Máu khó đông" vs "- máu khó đông"

---

### ❌ FRAGMENT ORTHODONTICS - CRITICAL ISSUES

#### 1. **NULL POINTER RISK - getFormDataNotes()**
```java
return "[Niềng răng] " + etOrthoNotes.getText().toString().trim();
```
**Vấn đề**: Nếu `etOrthoNotes` là null, app sẽ crash

#### 2. **IMAGE UPLOAD KHÔNG HOẠT ĐỘNG**
```java
android.widget.Toast.makeText(getContext(), "Máy ảnh/Chọn ảnh...", ...);
```
**Vấn đề**: Chỉ show toast, không trigger upload thực sự
**Edge case**: User nhấn nút nhưng không có gì xảy ra

#### 3. **NO VALIDATION**
- Có thể lưu form trống
- Không validate độ dài notes
- Không check xem có ít nhất 1 field được điền không

#### 4. **IMAGE PREVIEW KHÔNG HOẠT ĐỘNG**
- `ivPreview` chỉ hiển thị placeholder
- Không load ảnh đã upload
- Không có integration với upload flow

#### 5. **MISSING FEATURES**
- Không có field để nhập loại dây cung
- Không có field để nhập lực kéo
- Không có progress tracking (bước thứ mấy trong quá trình niềng)
- Hint text rất chi tiết nhưng không có field tương ứng

---

## 🧪 TEST CASES - TIỂU PHẪU

### TC-SUR-001: Null Safety
**Steps**:
1. Call `getFormDataNotes()` trước khi view ready
2. Kiểm tra crash

**Expected**: Crash với NullPointerException
**Risk**: CRITICAL

---

### TC-SUR-002: Empty Form
**Steps**:
1. Mở tab Tiểu phẫu
2. Không nhập gì
3. Click "Hoàn thành"

**Expected**: Lưu với "[Sinh hiệu] BP: , HR: "
**Risk**: HIGH - Không có validation

---

### TC-SUR-003: Invalid Blood Pressure Format
**Steps**:
1. Nhập BP: "abc" hoặc "999" hoặc "120-80"
2. Lưu

**Expected**: Lưu thành công (không validate)
**Risk**: HIGH - Data không hợp lệ

---

### TC-SUR-004: Invalid Heart Rate
**Steps**:
1. Nhập HR: "abc" hoặc "999" hoặc "-50"
2. Lưu

**Expected**: Lưu thành công (không validate)
**Risk**: HIGH - Data không hợp lệ

---

### TC-SUR-005: Multi-line Notes
**Steps**:
```
Ghi chú:
Bệnh nhân lo lắng
Cần theo dõi sau phẫu thuật
Tái khám sau 1 tuần
```

**Expected**: Chỉ lấy "Bệnh nhân lo lắng", mất 2 dòng sau
**Risk**: HIGH - Mất thông tin quan trọng

---

### TC-SUR-006: Checkbox Parsing Case Sensitivity
**Steps**:
1. Manually insert: "- máu khó đông" (lowercase)
2. Load lại

**Expected**: Checkbox không được check (case-sensitive)
**Risk**: MEDIUM

---

### TC-SUR-007: Both Checkboxes Checked
**Steps**:
1. Check cả 2: "Máu khó đông" + "Dị ứng thuốc tê"
2. Lưu

**Expected**: Lưu thành công, không có warning
**Risk**: MEDIUM - Nên có cảnh báo nguy hiểm

---

### TC-SUR-008: Special Characters in Notes
**Steps**:
```
Ghi chú: Bệnh nhân #123
BP: 120/80 @ 10:00
Cần theo dõi 24/7
```

**Expected**: Có thể bị lỗi parsing
**Risk**: MEDIUM

---

### TC-SUR-009: Very Long Notes (>1000 chars)
**Steps**: Paste đoạn text rất dài vào notes

**Expected**: Lưu đầy đủ hoặc truncate
**Risk**: LOW

---

### TC-SUR-010: Save and Reload
**Steps**:
1. Nhập đầy đủ: BP, HR, check boxes, notes
2. Lưu và reload

**Expected**: Tất cả data được restore đúng
**Risk**: CRITICAL

---

## 🧪 TEST CASES - NIỀNG RĂNG

### TC-ORT-001: Null Safety
**Steps**:
1. Call `getFormDataNotes()` trước khi view ready
2. Kiểm tra crash

**Expected**: Crash với NullPointerException
**Risk**: CRITICAL

---

### TC-ORT-002: Empty Form
**Steps**:
1. Mở tab Niềng răng
2. Không nhập gì
3. Click "Hoàn thành"

**Expected**: Lưu với "[Niềng răng] "
**Risk**: HIGH - Không có validation

---

### TC-ORT-003: Image Upload Buttons
**Steps**:
1. Click "Ảnh Mặt thẳng"
2. Click "Ảnh Cận hàm"

**Expected**: Chỉ show toast, không upload
**Risk**: HIGH - Feature không hoạt động

---

### TC-ORT-004: Multi-line Notes
**Steps**:
```
Ghi chú:
Thay dây cung size 0.016
Lực kéo tăng lên
Nướu hơi sưng
Tái khám sau 2 tuần
```

**Expected**: Tất cả lines được lưu (không có bug parsing như Surgery)
**Risk**: LOW - Simple parsing

---

### TC-ORT-005: Very Long Notes
**Steps**: Paste >1000 characters

**Expected**: Lưu đầy đủ
**Risk**: LOW

---

### TC-ORT-006: Special Characters
**Steps**:
```
Dây cung: 0.016" NiTi
Lực: 150g @ mỗi bên
Chi phí: 5,000,000 VNĐ
```

**Expected**: Lưu đúng
**Risk**: LOW

---

### TC-ORT-007: Save and Reload
**Steps**:
1. Nhập notes
2. Lưu và reload

**Expected**: Notes được restore đúng
**Risk**: CRITICAL

---

### TC-ORT-008: Image Preview
**Steps**:
1. Upload ảnh từ global button
2. Kiểm tra ivPreview

**Expected**: Vẫn hiển thị placeholder (không có integration)
**Risk**: MEDIUM

---

### TC-ORT-009: Fallback Parsing
**Steps**:
1. Manually insert data không có prefix "[Niềng răng]"
2. Load lại

**Expected**: Fallback - hiển thị toàn bộ string
**Risk**: LOW - Có fallback

---

### TC-ORT-010: Empty Notes After Prefix
**Steps**:
1. Lưu với notes trống
2. Load lại: "[Niềng răng] "

**Expected**: etOrthoNotes trống
**Risk**: LOW

---

## 🐛 BUGS CẦN FIX

### TIỂU PHẪU - Priority 1 (CRITICAL)

1. **Add null checks**
   ```java
   if (etBloodPressure == null || etHeartRate == null || etSurgeryNotes == null) {
       return "";
   }
   ```

2. **Fix multi-line notes parsing**
   - Sử dụng state machine như X-quang
   - Append tất cả lines sau "- Ghi chú:"

3. **Add validation**
   - BP format: "xxx/yyy" (số/số)
   - HR: số trong khoảng 40-200
   - Ít nhất BP hoặc HR phải có giá trị

4. **Add warning cho risk combination**
   - Nếu cả 2 checkboxes được check → show warning dialog

### TIỂU PHẪU - Priority 2 (HIGH)

5. **Improve checkbox parsing**
   - Case-insensitive
   - Trim whitespace
   - Flexible matching

6. **Add character counter**
   - Hiển thị số ký tự đã nhập cho notes

### NIỀNG RĂNG - Priority 1 (CRITICAL)

1. **Add null checks**
   ```java
   if (etOrthoNotes == null) {
       return "[Niềng răng] ";
   }
   ```

2. **Add validation**
   - Notes không được trống
   - Hoặc ít nhất 1 ảnh phải được upload

3. **Integrate image upload**
   - Kết nối buttons với activity
   - Hiển thị ảnh trong ivPreview

### NIỀNG RĂNG - Priority 2 (HIGH)

4. **Add more fields**
   - Loại dây cung (Spinner)
   - Lực kéo (EditText với unit)
   - Progress step (EditText hoặc Spinner)

5. **Improve UI**
   - Show uploaded images
   - Add delete image button
   - Add image gallery view

---

## 📊 EXPECTED RESULTS SUMMARY

| Fragment | Total Tests | Expected Pass | Expected Fail |
|----------|-------------|---------------|---------------|
| Tiểu phẫu | 10 | 3 | 7 |
| Niềng răng | 10 | 5 | 5 |
| **TOTAL** | **20** | **8** | **12** |

**Pass Rate Dự kiến: 40%** - Nhiều bugs cần fix!

---

## 🎯 COMPARISON WITH X-QUANG

| Feature | X-quang | Tiểu phẫu | Niềng răng |
|---------|---------|-----------|------------|
| Null Safety | ✅ Fixed | ❌ Missing | ❌ Missing |
| Validation | ✅ Fixed | ❌ Missing | ❌ Missing |
| Multi-line | ✅ Fixed | ❌ Bug | ✅ OK |
| Image Upload | ✅ Fixed | N/A | ❌ Not working |
| Special Fields | ✅ "Khác" | ✅ Checkboxes | ❌ Missing |

**Kết luận**: Tiểu phẫu và Niềng răng cần nhiều fixes giống như X-quang đã làm!

