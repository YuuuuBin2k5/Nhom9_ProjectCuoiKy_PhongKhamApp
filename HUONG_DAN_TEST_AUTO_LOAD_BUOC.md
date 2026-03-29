# Hướng Dẫn Test Tính Năng Tự Động Load Bước Đang Thực Hiện

## 🎯 Tính Năng Cần Test

Khi bác sĩ nhấp vào bệnh nhân từ màn hình Home/Queue, hệ thống sẽ:
1. Tự động mở màn hình khám
2. Tự động load thông tin bệnh nhân
3. Tự động load phác đồ điều trị
4. **TỰ ĐỘNG TÌM VÀ LOAD DỮ LIỆU CỦA BƯỚC ĐANG THỰC HIỆN** ⭐

## 📋 Chuẩn Bị Test

### Dữ Liệu Cần Có

#### Bệnh Nhân 1: Có Bước X-Quang Đang Làm
- Tên: Nguyễn Văn A
- Phác đồ điều trị: Đã tạo
- Bước 1: "Khám tổng quát" - COMPLETED
- Bước 2: "Chụp X-Quang" - **IN_PROGRESS** ⭐
  - Đã có ghi chú: "Bệnh nhân cần chụp răng số 6"
  - Đã có 2 ảnh X-quang
- Bước 3: "Nhổ răng" - PENDING

#### Bệnh Nhân 2: Có Bước Khám Chung Đang Làm
- Tên: Trần Thị B
- Phác đồ điều trị: Đã tạo
- Bước 1: "Khám tổng quát" - **IN_PROGRESS** ⭐
  - Đã có ghi chú: "Răng số 7 bị sâu"
- Bước 2: "Hàn răng" - PENDING

#### Bệnh Nhân 3: Chưa Bắt Đầu Bước Nào
- Tên: Lê Văn C
- Phác đồ điều trị: Đã tạo
- Bước 1: "Khám tổng quát" - PENDING
- Bước 2: "Cạo vôi" - PENDING

#### Bệnh Nhân 4: Đã Hoàn Thành Tất Cả
- Tên: Phạm Thị D
- Phác đồ điều trị: Đã tạo
- Bước 1: "Khám tổng quát" - COMPLETED
- Bước 2: "Hàn răng" - COMPLETED

## 🧪 Test Cases Chi Tiết

### Test Case 1: Bước X-Quang Đang IN_PROGRESS ⭐ QUAN TRỌNG

**Mục đích:** Kiểm tra auto-load bước X-Quang với dữ liệu đầy đủ

**Các bước:**
1. Mở app với tài khoản bác sĩ
2. Vào màn hình "Quản Lý Hàng Đợi"
3. Tìm bệnh nhân "Nguyễn Văn A"
4. **Nhấp vào thẻ bệnh nhân**
5. Quan sát màn hình

**Kết quả mong đợi:**

✅ **Màn hình khám mở ra**
- Hiển thị tên bệnh nhân: "Khám Bệnh nhân Nguyễn Văn A"
- Card nhập QR bị ẩn
- Khu vực khám hiển thị

✅ **Phác đồ điều trị load**
- Hiển thị 3 bước trong danh sách
- Bước 1: "Khám tổng quát" - màu xanh (COMPLETED)
- Bước 2: "Chụp X-Quang" - màu vàng (IN_PROGRESS)
- Bước 3: "Nhổ răng" - màu xám (PENDING)

✅ **Tab X-Quang được chọn tự động**
- Toggle button "X-Quang" được highlight
- Các tab khác không được chọn

✅ **FragmentXray hiển thị**
- Form X-Quang hiển thị đầy đủ
- Không phải là form khám chung

✅ **Dữ liệu đã lưu hiển thị**
- EditText "Chẩn đoán/Kết quả" có text: "Bệnh nhân cần chụp răng số 6"
- RecyclerView hiển thị 2 ảnh X-quang
- Text "Đã tải 2 ảnh" hiển thị

✅ **Nút điều khiển hiển thị**
- Nút "Hoàn thành bước" hiển thị (màu xanh)
- Nút "Hủy bước" hiển thị (màu đỏ)
- Nút "Lưu hồ sơ" hiển thị

✅ **Toast message**
- Hiển thị: "Đã tự động load bước đang thực hiện: Chụp X-Quang"

**Thao tác thêm để kiểm tra:**
- Nhấp vào ảnh → Mở full screen viewer
- Thêm ghi chú mới → Nhấp "Lưu" → Kiểm tra lưu thành công
- Nhấp "Hoàn thành bước" → Kiểm tra chuyển sang bước tiếp theo

---

### Test Case 2: Bước Khám Chung Đang IN_PROGRESS

**Mục đích:** Kiểm tra auto-load bước khám tổng quát

**Các bước:**
1. Vào "Quản Lý Hàng Đợi"
2. Tìm bệnh nhân "Trần Thị B"
3. **Nhấp vào thẻ bệnh nhân**
4. Quan sát

**Kết quả mong đợi:**

✅ **Tab "Khám chung" được chọn**
- Toggle button "Khám chung" được highlight

✅ **FragmentGeneralDental hiển thị**
- Form khám tổng quát hiển thị
- Có Odontogram (sơ đồ răng)

✅ **Dữ liệu đã lưu hiển thị**
- EditText có text: "Răng số 7 bị sâu"

✅ **Nút điều khiển hiển thị**
- Nút "Hoàn thành bước" hiển thị
- Nút "Hủy bước" hiển thị

✅ **Toast message**
- "Đã tự động load bước đang thực hiện: Khám tổng quát"

---

### Test Case 3: Không Có Bước IN_PROGRESS (Fallback PENDING)

**Mục đích:** Kiểm tra fallback sang bước PENDING đầu tiên

**Các bước:**
1. Vào "Quản Lý Hàng Đợi"
2. Tìm bệnh nhân "Lê Văn C"
3. **Nhấp vào thẻ bệnh nhân**
4. Quan sát

**Kết quả mong đợi:**

✅ **Tab "Khám chung" được chọn**
- Vì bước PENDING đầu tiên là "Khám tổng quát"

✅ **FragmentGeneralDental hiển thị**
- Form trống (chưa có dữ liệu)

✅ **Nút điều khiển hiển thị**
- Nút "Hoàn thành bước" hiển thị
- Nút "Hủy bước" hiển thị

✅ **Toast message**
- "Đã tự động load bước tiếp theo: Khám tổng quát"

**Lưu ý:**
- Đây là bước PENDING, chưa có dữ liệu
- Bác sĩ có thể bắt đầu nhập liệu ngay

---

### Test Case 4: Tất Cả Bước Đã COMPLETED

**Mục đích:** Kiểm tra không auto-load khi tất cả đã xong

**Các bước:**
1. Vào "Quản Lý Hàng Đợi"
2. Tìm bệnh nhân "Phạm Thị D"
3. **Nhấp vào thẻ bệnh nhân**
4. Quan sát

**Kết quả mong đợi:**

✅ **Không auto-load bước nào**
- Không có tab nào được chọn tự động
- Không có fragment nào hiển thị

✅ **Hiển thị danh sách bước**
- Tất cả các bước đều màu xanh (COMPLETED)
- Có icon check ✓ trên mỗi bước

✅ **Không có toast auto-load**
- Chỉ có toast: "Đã tải phác đồ điều trị (2 bước)"

✅ **Bác sĩ có thể:**
- Nhấp "Chỉnh sửa" trên bất kỳ bước nào để xem lại
- Nhấp "Thêm dịch vụ" để thêm bước mới

---

### Test Case 5: Bước COMPLETED - Xem Lại Dữ Liệu

**Mục đích:** Kiểm tra xem lại bước đã hoàn thành

**Các bước:**
1. Vào "Quản Lý Hàng Đợi"
2. Tìm bệnh nhân "Nguyễn Văn A"
3. Nhấp vào thẻ bệnh nhân
4. Hệ thống auto-load bước IN_PROGRESS (X-Quang)
5. **Nhấp "Chỉnh sửa" trên bước "Khám tổng quát" (COMPLETED)**
6. Quan sát

**Kết quả mong đợi:**

✅ **Tab "Khám chung" được chọn**
- Chuyển từ tab X-Quang sang tab Khám chung

✅ **Dữ liệu hiển thị**
- Ghi chú của bước đã hoàn thành hiển thị

✅ **Chế độ read-only**
- Các trường nhập liệu bị disable (màu xám)
- Không thể chỉnh sửa

✅ **Nút "Chỉnh sửa" hiển thị**
- Ở góc trên bên phải
- Nhấp vào để bật edit mode

✅ **Không có nút "Hoàn thành"**
- Vì bước đã COMPLETED rồi

---

### Test Case 6: Nhiều Bước IN_PROGRESS (Edge Case)

**Mục đích:** Kiểm tra khi có nhiều bước IN_PROGRESS

**Chuẩn bị:**
- Tạo bệnh nhân có 2 bước IN_PROGRESS (không nên xảy ra nhưng test edge case)

**Các bước:**
1. Nhấp vào bệnh nhân
2. Quan sát

**Kết quả mong đợi:**

✅ **Load bước IN_PROGRESS đầu tiên**
- Chỉ load bước đầu tiên trong danh sách
- Không load cả 2 bước cùng lúc

---

### Test Case 7: Bước Không Có Dữ liệu (Empty Data)

**Mục đích:** Kiểm tra load bước IN_PROGRESS nhưng chưa có dữ liệu

**Chuẩn bị:**
- Bệnh nhân có bước "X-Quang" IN_PROGRESS
- Bước này chưa có ghi chú, chưa có ảnh

**Các bước:**
1. Nhấp vào bệnh nhân
2. Quan sát

**Kết quả mong đợi:**

✅ **Tab X-Quang được chọn**
✅ **FragmentXray hiển thị**
✅ **Form trống**
- EditText trống
- RecyclerView trống (0 ảnh)
✅ **Nút "Hoàn thành" hiển thị**
✅ **Có thể nhập liệu ngay**

---

## 🔍 Các Điểm Cần Kiểm Tra Kỹ

### 1. Timing (Thời Gian)
- ⏱️ Delay 300ms có đủ không?
- ⏱️ UI có render kịp không?
- ⏱️ Fragment có load kịp không?

### 2. Data Integrity (Toàn Vẹn Dữ Liệu)
- 📝 Ghi chú hiển thị đúng không?
- 🖼️ Ảnh hiển thị đủ không?
- 🔢 Số lượng ảnh đúng không?
- ✅ Trạng thái bước đúng không?

### 3. UI State (Trạng Thái UI)
- 🎨 Tab đúng được chọn không?
- 🎨 Fragment đúng hiển thị không?
- 🎨 Nút điều khiển đúng không?
- 🎨 Read-only mode đúng không?

### 4. Navigation (Điều Hướng)
- 🔄 Chuyển tab mượt mà không?
- 🔄 Load fragment không bị lag?
- 🔄 Không bị crash?

### 5. Error Handling (Xử Lý Lỗi)
- ❌ Nếu không có bước nào?
- ❌ Nếu API lỗi?
- ❌ Nếu dữ liệu null?

---

## 📊 Checklist Tổng Hợp

### Chức Năng Cơ Bản
- [ ] Auto-load bước IN_PROGRESS
- [ ] Fallback sang bước PENDING
- [ ] Không load khi tất cả COMPLETED
- [ ] Chuyển tab đúng
- [ ] Load fragment đúng
- [ ] Hiển thị dữ liệu đúng

### Các Loại Bước
- [ ] Khám tổng quát (General Dental)
- [ ] X-Quang (Xray)
- [ ] Phẫu thuật (Surgery)
- [ ] Chỉnh nha (Orthodontics)

### Trạng Thái Bước
- [ ] PENDING → Auto-load (fallback)
- [ ] IN_PROGRESS → Auto-load (primary)
- [ ] COMPLETED → Không auto-load

### Dữ Liệu
- [ ] Có ghi chú
- [ ] Không có ghi chú
- [ ] Có ảnh
- [ ] Không có ảnh
- [ ] Có cả ghi chú và ảnh

### UI/UX
- [ ] Toast message hiển thị
- [ ] Nút "Hoàn thành" hiển thị đúng
- [ ] Nút "Hủy" hiển thị đúng
- [ ] Read-only mode hoạt động
- [ ] Edit mode hoạt động

### Performance
- [ ] Không bị lag
- [ ] Không bị crash
- [ ] Load nhanh (<1s)
- [ ] Mượt mà

---

## 🐛 Các Lỗi Có Thể Gặp

### Lỗi 1: Không Auto-Load
**Triệu chứng:** Nhấp vào bệnh nhân nhưng không tự động load bước

**Nguyên nhân có thể:**
- Không có bước IN_PROGRESS hoặc PENDING
- Delay không đủ
- Fragment chưa render kịp

**Cách kiểm tra:**
- Xem log: "Đã tự động load bước..."
- Kiểm tra status của các bước

### Lỗi 2: Load Sai Tab
**Triệu chứng:** Load bước X-Quang nhưng hiển thị tab Khám chung

**Nguyên nhân có thể:**
- `uiTemplateType` không đúng
- Logic chuyển tab sai

**Cách kiểm tra:**
- Xem `step.getUiTemplateType()`
- Kiểm tra logic trong `onStepEdit()`

### Lỗi 3: Dữ Liệu Không Hiển Thị
**Triệu chứng:** Tab đúng nhưng dữ liệu trống

**Nguyên nhân có thể:**
- `setData()` không được gọi
- Fragment chưa render kịp
- Dữ liệu null

**Cách kiểm tra:**
- Xem log trong `onStepEdit()`
- Kiểm tra `step.getDoctorConclusion()`
- Kiểm tra `step.getImages()`

### Lỗi 4: Ảnh Không Hiển Thị
**Triệu chứng:** Ghi chú hiển thị nhưng ảnh không có

**Nguyên nhân có thể:**
- RecyclerView adapter chưa sẵn sàng
- Double post pattern không hoạt động
- URL ảnh sai

**Cách kiểm tra:**
- Xem log trong FragmentXray
- Kiểm tra `setImageUrls()` được gọi
- Kiểm tra URL ảnh

### Lỗi 5: Crash Khi Load
**Triệu chứng:** App crash ngay sau khi nhấp bệnh nhân

**Nguyên nhân có thể:**
- NullPointerException
- Fragment chưa attach
- View chưa tạo

**Cách kiểm tra:**
- Xem logcat
- Kiểm tra null check
- Kiểm tra timing

---

## 📝 Báo Cáo Lỗi

Nếu gặp lỗi, vui lòng cung cấp:

1. **Mô tả lỗi:**
   - Điều gì xảy ra?
   - Khi nào xảy ra?

2. **Các bước tái hiện:**
   - Làm thế nào để gặp lỗi?
   - Bệnh nhân nào?
   - Bước nào?

3. **Kết quả mong đợi:**
   - Bạn mong đợi điều gì?

4. **Kết quả thực tế:**
   - Điều gì thực sự xảy ra?

5. **Screenshot/Video:**
   - Nếu có

6. **Logcat:**
   - Nếu có crash

---

## ✅ Kết Luận

Sau khi test tất cả các test cases trên, nếu:
- ✅ Tất cả test cases PASS
- ✅ Không có lỗi crash
- ✅ UI/UX mượt mà
- ✅ Dữ liệu hiển thị đúng

→ **Tính năng sẵn sàng đưa vào production!** 🎉

---

**Ngày tạo:** 29/03/2026
**Trạng thái:** Sẵn sàng test
**Ước tính thời gian test:** 30-45 phút
