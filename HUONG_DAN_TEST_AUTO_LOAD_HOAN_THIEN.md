# 🧪 HƯỚNG DẪN TEST TÍNH NĂNG AUTO-LOAD DỮ LIỆU BƯỚC ĐÃ HOÀN THÀNH

## 📱 CÀI ĐẶT APK MỚI

### Bước 1: Build APK mới
```bash
cd mobile_android
./gradlew clean assembleDebug
```

### Bước 2: Cài đặt APK
APK nằm ở: `mobile_android/app/build/outputs/apk/debug/app-debug.apk`

Cài đặt bằng ADB:
```bash
adb install -r mobile_android/app/build/outputs/apk/debug/app-debug.apk
```

## 🎯 CHUẨN BỊ DỮ LIỆU TEST

### Yêu cầu:
1. Backend đang chạy tại `http://10.20.1.170:8081/`
2. Có bệnh nhân với phác đồ điều trị
3. Phác đồ có ít nhất 2-3 bước đã HOÀN THÀNH (status = COMPLETED)
4. Mỗi bước đã hoàn thành có:
   - Nội dung trong trường `doctorConclusion`
   - Hình ảnh (đối với bước X-Quang)
   - `uiTemplateType` khác nhau (GENERAL, XRAY, SURGERY, ORTHO)

### Ví dụ dữ liệu:
- **Bước 1**: Khám và tư vấn răng miệng (GENERAL) - COMPLETED
  - doctorConclusion: "Sâu răng số 6, cần hàn trám"
  
- **Bước 2**: Chụp X-Quang (XRAY) - COMPLETED
  - doctorConclusion: "Phim X-Quang cho thấy sâu răng sâu đến tủy"
  - images: 2 ảnh X-Quang

- **Bước 3**: Phẫu thuật (SURGERY) - IN_PROGRESS
  - Chưa hoàn thành

## 🧪 KỊCH BẢN TEST

### TEST 1: Kiểm tra Pre-loading Cache

**Mục tiêu**: Xác nhận hệ thống tải trước dữ liệu của TẤT CẢ bước đã hoàn thành

**Các bước**:
1. Mở app, đăng nhập với tài khoản bác sĩ
2. Vào màn hình "Quản lý hàng đợi"
3. Nhấp vào bệnh nhân có phác đồ với bước đã hoàn thành
4. **Kiểm tra**: Có thông báo toast hiện lên:
   ```
   "Đã tải X bước đã hoàn thành. Dữ liệu sẽ tự động hiển thị khi chuyển tab."
   ```
5. **Xác nhận**: Số X phải khớp với số bước COMPLETED trong phác đồ

**Kết quả mong đợi**: ✅ Toast hiện đúng số bước

---

### TEST 2: Auto-load Tab "Khám chung"

**Mục tiêu**: Dữ liệu tự động hiển thị khi chuyển sang tab Khám chung

**Các bước**:
1. Tiếp tục từ TEST 1
2. Nhấp vào tab "Khám chung" (nút toggle đầu tiên)
3. **Kiểm tra**:
   - Trường "Tóm tắt lý do khám" có nội dung tự động hiển thị
   - Nội dung khớp với dữ liệu đã lưu trước đó
   - Trường bị vô hiệu hóa (màu xám, không thể nhập)
4. Thử nhấp vào trường và gõ chữ
5. **Xác nhận**: Không thể chỉnh sửa

**Kết quả mong đợi**: 
- ✅ Dữ liệu tự động hiển thị
- ✅ Trường ở chế độ READ-ONLY

---

### TEST 3: Auto-load Tab "X-Quang"

**Mục tiêu**: Dữ liệu + hình ảnh tự động hiển thị

**Các bước**:
1. Nhấp vào tab "X-Quang"
2. **Kiểm tra**:
   - Trường "Kết quả đọc phim" có nội dung tự động
   - Hình ảnh X-Quang hiển thị trong gallery (nếu có)
   - Số lượng ảnh hiển thị đúng
   - Trường bị vô hiệu hóa
3. Nhấp nút "Tải ảnh"
4. **Xác nhận**: Hiện toast "Dữ liệu đã hoàn thành, không thể chỉnh sửa"

**Kết quả mong đợi**: 
- ✅ Text + images tự động hiển thị
- ✅ Không thể upload thêm ảnh
- ✅ Trường ở chế độ READ-ONLY

---

### TEST 4: Auto-load Tab "Phẫu thuật"

**Mục tiêu**: Dữ liệu phức tạp (BP, HR, checkbox) tự động parse và hiển thị

**Các bước**:
1. Nhấp vào tab "Phẫu thuật"
2. **Kiểm tra**:
   - Trường "Huyết áp" có giá trị (VD: 120/80)
   - Trường "Nhịp tim" có giá trị (VD: 75)
   - Checkbox "Máu khó đông" được tick (nếu có trong dữ liệu)
   - Checkbox "Dị ứng thuốc tê" được tick (nếu có)
   - Trường "Ghi chú" có nội dung
   - Tất cả trường bị vô hiệu hóa
3. Thử tick/untick checkbox
4. **Xác nhận**: Không thể thay đổi

**Kết quả mong đợi**: 
- ✅ Tất cả dữ liệu parse đúng và hiển thị
- ✅ Tất cả trường ở chế độ READ-ONLY

---

### TEST 5: Auto-load Tab "Niềng răng"

**Mục tiêu**: Dữ liệu đơn giản tự động hiển thị

**Các bước**:
1. Nhấp vào tab "Niềng răng"
2. **Kiểm tra**:
   - Trường "Ghi chú" có nội dung
   - Trường bị vô hiệu hóa
3. Nhấp nút "Tải ảnh trước" hoặc "Tải ảnh sau"
4. **Xác nhận**: Hiện toast "Dữ liệu đã hoàn thành, không thể chỉnh sửa"

**Kết quả mong đợi**: 
- ✅ Dữ liệu tự động hiển thị
- ✅ Không thể upload ảnh
- ✅ Trường ở chế độ READ-ONLY

---

### TEST 6: Chuyển đổi giữa các tab

**Mục tiêu**: Dữ liệu vẫn giữ nguyên khi chuyển qua lại

**Các bước**:
1. Chuyển từ "Khám chung" → "X-Quang" → "Phẫu thuật" → "Niềng răng"
2. Chuyển ngược lại: "Niềng răng" → "Phẫu thuật" → "X-Quang" → "Khám chung"
3. **Kiểm tra**: Mỗi lần chuyển tab:
   - Dữ liệu tự động hiển thị lại
   - Không bị mất dữ liệu
   - Vẫn ở chế độ READ-ONLY

**Kết quả mong đợi**: 
- ✅ Dữ liệu persistent khi chuyển tab
- ✅ Không có lỗi hoặc crash

---

### TEST 7: Bệnh nhân KHÔNG có bước hoàn thành

**Mục tiêu**: Hệ thống không lỗi khi không có dữ liệu cached

**Các bước**:
1. Quay lại màn hình "Quản lý hàng đợi"
2. Nhấp vào bệnh nhân CHƯA có bước hoàn thành (hoặc phác đồ mới)
3. **Kiểm tra**:
   - KHÔNG có toast "Đã tải X bước..."
   - Chuyển sang các tab
   - Trường RỖNG (không có dữ liệu)
   - Trường có thể CHỈNH SỬA (không read-only)

**Kết quả mong đợi**: 
- ✅ Không có lỗi
- ✅ Trường rỗng và có thể chỉnh sửa bình thường

---

### TEST 8: Ra ngoài Home và quay lại

**Mục tiêu**: Dữ liệu vẫn auto-load sau khi thoát và mở lại

**Các bước**:
1. Từ màn hình DoctorWorkflowActivity, nhấn nút "Quay lại"
2. Quay về màn hình Home
3. Vào lại "Quản lý hàng đợi"
4. Nhấp lại vào cùng bệnh nhân
5. **Kiểm tra**:
   - Toast "Đã tải X bước..." hiện lại
   - Chuyển tab → Dữ liệu tự động hiển thị
   - Vẫn ở chế độ READ-ONLY

**Kết quả mong đợi**: 
- ✅ Tính năng hoạt động nhất quán
- ✅ Không bị mất cache

---

## 📊 BẢNG TỔNG KẾT KẾT QUẢ TEST

| # | Test Case | Kết quả | Ghi chú |
|---|-----------|---------|---------|
| 1 | Pre-loading Cache | ⬜ | Toast hiện đúng số bước |
| 2 | Auto-load Khám chung | ⬜ | Dữ liệu + READ-ONLY |
| 3 | Auto-load X-Quang | ⬜ | Text + Images + READ-ONLY |
| 4 | Auto-load Phẫu thuật | ⬜ | BP/HR/Checkbox + READ-ONLY |
| 5 | Auto-load Niềng răng | ⬜ | Notes + READ-ONLY |
| 6 | Chuyển đổi tab | ⬜ | Dữ liệu persistent |
| 7 | Không có bước hoàn thành | ⬜ | Không lỗi, trường editable |
| 8 | Ra Home và quay lại | ⬜ | Tính năng nhất quán |

**Chú thích**: 
- ⬜ = Chưa test
- ✅ = Pass
- ❌ = Fail

---

## 🐛 NẾU GẶP LỖI

### Lỗi 1: Toast không hiện
**Nguyên nhân**: Không có bước COMPLETED trong phác đồ
**Giải pháp**: Kiểm tra database, đảm bảo có bước với status = "COMPLETED"

### Lỗi 2: Dữ liệu không hiển thị
**Nguyên nhân**: Template type không khớp
**Giải pháp**: Kiểm tra `uiTemplateType` trong database (phải là GENERAL, XRAY, SURGERY, ORTHO)

### Lỗi 3: Hình ảnh không hiển thị
**Nguyên nhân**: URL ảnh không hợp lệ hoặc server không trả về
**Giải pháp**: Kiểm tra `images` array trong step, đảm bảo `imageUrl` không null

### Lỗi 4: Vẫn chỉnh sửa được
**Nguyên nhân**: Fragment không gọi `setReadOnlyMode(true)`
**Giải pháp**: Kiểm tra log, đảm bảo `autoPopulateFragmentFromCache()` được gọi

### Lỗi 5: App crash khi chuyển tab
**Nguyên nhân**: Fragment view chưa ready
**Giải pháp**: Đã xử lý bằng `postDelayed(100ms)`, nếu vẫn lỗi tăng delay lên 200ms

---

## 📞 HỖ TRỢ

Nếu gặp vấn đề, cung cấp thông tin sau:
1. Kết quả test case nào fail
2. Log từ Logcat (filter: "DoctorWorkflow")
3. Dữ liệu bệnh nhân (patientId, treatmentPlanId)
4. Screenshot màn hình lỗi

---

**Ngày tạo**: 2026-03-29
**Trạng thái**: ✅ SẴN SÀNG TEST
**Build**: SUCCESS
