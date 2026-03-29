# X-QUANG COMPREHENSIVE TEST PLAN & EDGE CASES

## 🔍 PHÂN TÍCH CODE - PHÁT HIỆN VẤN ĐỀ

### ❌ CRITICAL ISSUES FOUND

#### 1. **NULL POINTER RISK - getFormDataNotes()**
```java
String findings = etXrayFindings.getText().toString().trim();
```
**Vấn đề**: Nếu `etXrayFindings` là null, app sẽ crash
**Edge case**: Fragment được gọi trước khi `onViewCreated()` hoàn thành

#### 2. **PARSING BUG - setData() với multi-line content**
```java
if (line.startsWith("Kết quả đọc phim:")) {
    String findings = line.substring("Kết quả đọc phim:".length()).trim();
```
**Vấn đề**: Chỉ đọc 1 dòng, nếu "Kết quả đọc phim" có nhiều dòng thì mất data
**Edge case**: 
```
Kết quả đọc phim: Phát hiện sâu răng ở răng 12
Tổn thương lan rộng đến tủy răng
Viêm quanh chóp răng
```
→ Chỉ lấy được "Phát hiện sâu răng ở răng 12"

#### 3. **RADIO BUTTON NOT CHECKED - getSelectedImageType()**
```java
if (checkedId == R.id.rbPanoramic) {
```
**Vấn đề**: Nếu không có radio button nào được check, `checkedId` = -1
**Edge case**: User xóa hết data, không chọn loại X-quang

#### 4. **MISSING NULL CHECK - setImageType()**
```java
if (type.contains("Panoramic") || type.contains("Toàn cảnh")) {
```
**Vấn đề**: Nếu `type` là null hoặc empty, sẽ crash
**Edge case**: Data từ server bị corrupt hoặc format sai

#### 5. **FRAGMENT LIFECYCLE ISSUE**
```java
if (etXrayFindings != null) {
    etXrayFindings.setText(findings);
}
```
**Vấn đề**: Kiểm tra null nhưng không handle trường hợp view chưa được tạo
**Edge case**: `setData()` được gọi ngay sau `commitNow()` nhưng view chưa inflate xong

#### 6. **IMAGE UPLOAD KHÔNG HOẠT ĐỘNG**
```java
android.widget.Toast.makeText(getContext(), "Sử dụng nút 'Tải ảnh' ở trên...", ...);
```
**Vấn đề**: Không trigger upload thực sự, chỉ show toast
**Edge case**: User nhấn nút upload trong fragment nhưng không có gì xảy ra

#### 7. **MISSING VALIDATION**
- Không validate độ dài text (có thể quá dài)
- Không validate format của findings/diagnosis
- Không check xem có ít nhất 1 field được điền không

---

## 🧪 TEST CASES - EDGE CASES

### A. FRAGMENT LIFECYCLE TESTS

#### TC-XR-001: Fragment được tạo và destroy nhiều lần
**Steps**:
1. Mở X-quang tab
2. Nhập data vào các field
3. Chuyển sang tab khác (Tổng quát)
4. Quay lại X-quang tab
5. Kiểm tra data có còn không

**Expected**: Data bị mất (fragment bị destroy)
**Risk**: HIGH - User mất công nhập liệu

#### TC-XR-002: setData() được gọi trước onViewCreated()
**Steps**:
1. Tạo step X-quang với doctorConclusion có sẵn
2. Click "Chỉnh sửa" ngay lập tức
3. Kiểm tra xem data có được load không

**Expected**: Có thể crash hoặc data không hiển thị
**Risk**: CRITICAL

#### TC-XR-003: Rotation/Configuration change
**Steps**:
1. Nhập data vào X-quang form
2. Xoay màn hình
3. Kiểm tra data

**Expected**: Data bị mất
**Risk**: MEDIUM

---

### B. DATA PARSING TESTS

#### TC-XR-004: Multi-line findings
**Input**:
```
[X-quang] Loại: Panoramic (Toàn cảnh)
Kết quả đọc phim: Dòng 1
Dòng 2
Dòng 3
Chẩn đoán: Test
```
**Expected**: Chỉ lấy "Dòng 1", mất "Dòng 2" và "Dòng 3"
**Risk**: HIGH - Mất thông tin quan trọng

#### TC-XR-005: Special characters trong text
**Input**:
```
Kết quả đọc phim: Răng #12: Sâu răng 50%
Chẩn đoán: Cần điều trị @ phòng X-quang
Khuyến nghị: Tái khám sau 2-3 tuần
```
**Expected**: Có thể bị lỗi parsing với ký tự đặc biệt
**Risk**: MEDIUM

#### TC-XR-006: Empty/null doctorConclusion
**Input**: `null` hoặc `""`
**Expected**: Không crash, hiển thị form trống
**Risk**: LOW

#### TC-XR-007: Malformed data
**Input**:
```
[X-quang] Loại:
Kết quả đọc phim:
Chẩn đoán:
```
**Expected**: Các field trống, không crash
**Risk**: MEDIUM

#### TC-XR-008: Missing prefix
**Input**:
```
Loại: Panoramic
Findings: Test
```
**Expected**: Không parse được, form trống
**Risk**: LOW

---

### C. RADIO BUTTON TESTS

#### TC-XR-009: Không chọn radio button
**Steps**:
1. Mở X-quang tab
2. Không chọn loại X-quang
3. Nhập findings
4. Click "Hoàn thành"

**Expected**: Lưu với default "Panoramic (Toàn cảnh)"
**Risk**: LOW - Có default value

#### TC-XR-010: Chọn "Khác" nhưng không có field nhập loại khác
**Steps**:
1. Chọn radio "Khác"
2. Nhập findings
3. Lưu

**Expected**: Lưu với text "Khác" - không rõ ràng
**Risk**: MEDIUM - Thiếu thông tin chi tiết

---

### D. TEXT INPUT TESTS

#### TC-XR-011: Text quá dài (>10000 characters)
**Steps**:
1. Paste đoạn text rất dài vào findings
2. Lưu

**Expected**: Có thể bị truncate hoặc lỗi database
**Risk**: MEDIUM

#### TC-XR-012: Emoji và Unicode characters
**Input**: `Kết quả: 😊 Răng tốt ✅ Không có vấn đề 🦷`
**Expected**: Có thể hiển thị sai hoặc lỗi encoding
**Risk**: LOW

#### TC-XR-013: Tất cả fields trống
**Steps**:
1. Mở X-quang tab
2. Không nhập gì
3. Click "Hoàn thành"

**Expected**: Lưu với chỉ có "[X-quang] Loại: Panoramic (Toàn cảnh)"
**Risk**: MEDIUM - Nên validate ít nhất 1 field

---

### E. IMAGE UPLOAD TESTS

#### TC-XR-014: Click nút "Tải lên hình ảnh X-quang" trong fragment
**Steps**:
1. Mở X-quang tab
2. Click nút upload trong fragment

**Expected**: Chỉ show toast, không upload được
**Risk**: HIGH - Feature không hoạt động

#### TC-XR-015: Upload ảnh từ nút global
**Steps**:
1. Mở X-quang tab
2. Click nút "Tải ảnh" ở trên (global button)
3. Chọn ảnh

**Expected**: Ảnh được upload nhưng không hiển thị trong fragment
**Risk**: HIGH - Không có integration

---

### F. INTEGRATION TESTS

#### TC-XR-016: Chuyển từ tab khác sang X-quang
**Steps**:
1. Nhập data ở tab "Tổng quát"
2. Chuyển sang tab "X-quang"
3. Kiểm tra data tab "Tổng quát"

**Expected**: Data tab "Tổng quát" bị mất
**Risk**: HIGH - Fragment bị replace

#### TC-XR-017: Lưu và reload
**Steps**:
1. Nhập data X-quang
2. Click "Lưu hồ sơ"
3. Thoát và vào lại
4. Click "Chỉnh sửa"

**Expected**: Data được restore đúng
**Risk**: CRITICAL - Test persistence

#### TC-XR-018: Complete step với X-quang data
**Steps**:
1. Nhập đầy đủ X-quang data
2. Click "Hoàn thành"
3. Kiểm tra database

**Expected**: Data được lưu đúng format
**Risk**: CRITICAL

---

### G. UI/UX TESTS

#### TC-XR-019: Toggle visibility khi là X-quang service
**Steps**:
1. Tạo step với serviceName chứa "X-quang"
2. Click "Bắt đầu"

**Expected**: Toggle buttons bị ẩn, chỉ hiện image upload
**Risk**: MEDIUM - Logic conflict

#### TC-XR-020: Scroll behavior
**Steps**:
1. Mở X-quang tab
2. Nhập nhiều text
3. Kiểm tra scroll

**Expected**: Scroll mượt, không bị cut off
**Risk**: LOW

---

## 🐛 BUGS CẦN FIX NGAY

### Priority 1 - CRITICAL

1. **Fix multi-line parsing trong setData()**
   - Cần parse toàn bộ content của mỗi section, không chỉ 1 dòng
   - Sử dụng state machine hoặc regex để parse đúng

2. **Add null checks cho tất cả EditText**
   - Wrap tất cả `.getText()` trong null check
   - Hoặc sử dụng safe call operator

3. **Fix fragment lifecycle issue**
   - Save/restore state với onSaveInstanceState
   - Hoặc sử dụng ViewModel để giữ data

### Priority 2 - HIGH

4. **Integrate image upload**
   - Kết nối nút upload trong fragment với activity
   - Hiển thị ảnh đã upload trong ivXrayPreview

5. **Add validation**
   - Validate ít nhất 1 field phải có data
   - Validate độ dài text
   - Show error message nếu invalid

### Priority 3 - MEDIUM

6. **Improve "Khác" option**
   - Thêm EditText để nhập loại X-quang khác
   - Show/hide based on radio selection

7. **Add confirmation dialog**
   - Khi user chuyển tab mà chưa lưu
   - Tránh mất data

---

## 📋 TEST EXECUTION CHECKLIST

### Pre-test Setup
- [ ] Backend đang chạy
- [ ] Database có data test
- [ ] APK mới nhất được cài
- [ ] Clear app data trước khi test

### Test Execution
- [ ] Run tất cả TC-XR-001 đến TC-XR-020
- [ ] Document kết quả cho mỗi test case
- [ ] Screenshot các bugs
- [ ] Log errors từ Logcat

### Post-test
- [ ] Tổng hợp bugs tìm được
- [ ] Prioritize bugs
- [ ] Tạo fix plan
- [ ] Re-test sau khi fix

---

## 🎯 EXPECTED RESULTS SUMMARY

| Category | Total Tests | Expected Pass | Expected Fail |
|----------|-------------|---------------|---------------|
| Lifecycle | 3 | 1 | 2 |
| Data Parsing | 5 | 2 | 3 |
| Radio Button | 2 | 2 | 0 |
| Text Input | 3 | 2 | 1 |
| Image Upload | 2 | 0 | 2 |
| Integration | 3 | 1 | 2 |
| UI/UX | 2 | 2 | 0 |
| **TOTAL** | **20** | **10** | **10** |

**Pass Rate Dự kiến: 50%** - Cần fix nhiều issues!

