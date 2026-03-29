# Hướng Dẫn Kiểm Tra Tính Năng Auto-Load Bệnh Nhân

## 🎯 Tính Năng Đã Hoàn Thành

Khi bác sĩ nhấp vào bệnh nhân từ màn hình Home/Queue, hệ thống sẽ **TỰ ĐỘNG** mở hồ sơ bệnh nhân mà **KHÔNG CẦN** quét QR code.

## 📱 Cách Kiểm Tra

### Bước 1: Mở Màn Hình Quản Lý Hàng Đợi
1. Đăng nhập với tài khoản bác sĩ
2. Vào màn hình "Quản Lý Hàng Đợi" (Queue Management)
3. Bạn sẽ thấy danh sách bệnh nhân đang chờ

### Bước 2: Nhấp Vào Bệnh Nhân
Bạn có thể nhấp vào:
- ✅ **Thẻ bệnh nhân** (toàn bộ card)
- ✅ **Tên bệnh nhân** (text)
- ✅ **Nút "Khám"** (nếu hiển thị)

### Bước 3: Quan Sát Kết Quả
Sau khi nhấp, hệ thống sẽ:

#### ✅ Tự Động Mở DoctorWorkflowActivity
- Màn hình khám bệnh mở ra
- Không cần thao tác thêm

#### ✅ Tự Động Load Thông Tin Bệnh Nhân
- Tên bệnh nhân hiển thị ở header
- Dịch vụ đã đặt hiển thị (nếu có)
- Thông tin bệnh nhân đầy đủ

#### ✅ Tự Động Ẩn Phần Nhập QR
- Card nhập QR code bị ẩn
- Không cần quét QR
- Không cần nhập mã thủ công

#### ✅ Tự Động Hiển Thị Khu Vực Khám
- Khu vực khám bệnh hiện ra
- Form khám bệnh sẵn sàng
- Có thể bắt đầu khám ngay

#### ✅ Tự Động Load Phác Đồ Điều Trị (Nếu Có)
- Nếu bệnh nhân đã có phác đồ → Load phác đồ
- Hiển thị tất cả các bước điều trị
- Hiển thị trạng thái từng bước
- Có thể chỉnh sửa/hoàn thành bước

#### ✅ Hiển Thị Form Trống (Nếu Chưa Có Phác Đồ)
- Nếu bệnh nhân chưa có phác đồ → Hiển thị form mới
- Có thể tạo phác đồ mới
- Có thể chọn template
- Có thể thêm dịch vụ

## 🔍 Các Trường Hợp Kiểm Tra

### Test Case 1: Bệnh Nhân Đã Có Phác Đồ
**Mục đích:** Kiểm tra load phác đồ existing

**Các bước:**
1. Chọn bệnh nhân đã có phác đồ điều trị
2. Nhấp vào bệnh nhân
3. **Kỳ vọng:**
   - Màn hình khám mở ra
   - Phác đồ điều trị hiển thị
   - Các bước điều trị hiển thị
   - Trạng thái từng bước đúng
   - Có thể chỉnh sửa ngay

### Test Case 2: Bệnh Nhân Chưa Có Phác Đồ
**Mục đích:** Kiểm tra tạo phác đồ mới

**Các bước:**
1. Chọn bệnh nhân chưa có phác đồ
2. Nhấp vào bệnh nhân
3. **Kỳ vọng:**
   - Màn hình khám mở ra
   - Form trống hiển thị
   - Có thể chọn template
   - Có thể thêm dịch vụ
   - Có thể tạo phác đồ mới

### Test Case 3: Bệnh Nhân Đang Khám (IN_PROGRESS)
**Mục đích:** Kiểm tra tiếp tục khám

**Các bước:**
1. Chọn bệnh nhân đang trong quá trình khám
2. Nhấp vào bệnh nhân
3. **Kỳ vọng:**
   - Màn hình khám mở ra
   - Phác đồ hiện tại hiển thị
   - Bước đang khám được highlight
   - Có thể tiếp tục khám

### Test Case 4: Bệnh Nhân Ưu Tiên (PRIORITY)
**Mục đích:** Kiểm tra xử lý bệnh nhân ưu tiên

**Các bước:**
1. Chọn bệnh nhân ưu tiên (có badge ưu tiên)
2. Nhấp vào bệnh nhân
3. **Kỳ vọng:**
   - Màn hình khám mở ra
   - Load thông tin bình thường
   - Không có lỗi

### Test Case 5: Bệnh Nhân Từ Tab "Cận Lâm Sàng"
**Mục đích:** Kiểm tra load từ tab khác

**Các bước:**
1. Chuyển sang tab "Cận Lâm Sàng"
2. Chọn bệnh nhân đang chờ X-Quang
3. Nhấp vào bệnh nhân
4. **Kỳ vọng:**
   - Màn hình khám mở ra
   - Load thông tin đúng
   - Hiển thị bước X-Quang

## ❌ Các Lỗi Có Thể Gặp

### Lỗi 1: Không Tìm Thấy ID Bệnh Nhân
**Triệu chứng:** Toast message "Lỗi: Không tìm thấy ID bệnh nhân"

**Nguyên nhân:** QueueItem không có patientId

**Giải pháp:** Kiểm tra API trả về đầy đủ patientId

### Lỗi 2: Không Tìm Thấy Bệnh Nhân
**Triệu chứng:** Toast message "Không tìm thấy bệnh nhân"

**Nguyên nhân:** API lookup không tìm thấy bệnh nhân với ID đó

**Giải pháp:** Kiểm tra backend API /api/patients/lookup

### Lỗi 3: Màn Hình Mở Nhưng Không Load
**Triệu chứng:** DoctorWorkflowActivity mở nhưng vẫn hiển thị card nhập QR

**Nguyên nhân:** EXTRA_INITIAL_QR không được truyền đúng

**Giải pháp:** Kiểm tra Intent extras trong QueueManagementActivity

### Lỗi 4: Load Chậm
**Triệu chứng:** Màn hình mở nhưng phải đợi lâu mới load xong

**Nguyên nhân:** API chậm hoặc network lag

**Giải pháp:** Kiểm tra kết nối mạng và backend performance

## ✅ Checklist Kiểm Tra

Đánh dấu ✅ khi kiểm tra xong:

- [ ] Nhấp vào bệnh nhân từ tab "Đang chờ"
- [ ] Nhấp vào bệnh nhân từ tab "Cận lâm sàng"
- [ ] Nhấp vào bệnh nhân từ tab "Ưu tiên"
- [ ] Nhấp vào tên bệnh nhân (không phải card)
- [ ] Nhấp vào card bệnh nhân
- [ ] Kiểm tra bệnh nhân có phác đồ
- [ ] Kiểm tra bệnh nhân chưa có phác đồ
- [ ] Kiểm tra bệnh nhân đang khám
- [ ] Kiểm tra card QR bị ẩn
- [ ] Kiểm tra khu vực khám hiển thị
- [ ] Kiểm tra tên bệnh nhân ở header
- [ ] Kiểm tra phác đồ load đúng
- [ ] Kiểm tra các bước điều trị hiển thị
- [ ] Kiểm tra có thể chỉnh sửa ngay
- [ ] Kiểm tra không cần quét QR

## 📊 Kết Quả Mong Đợi

### Thành Công ✅
- Nhấp vào bệnh nhân → Màn hình khám mở ngay
- Không cần quét QR code
- Thông tin bệnh nhân hiển thị đầy đủ
- Phác đồ điều trị load tự động (nếu có)
- Có thể bắt đầu khám ngay lập tức
- Trải nghiệm mượt mà, không gián đoạn

### Thất Bại ❌
- Phải quét QR code thủ công
- Card nhập QR vẫn hiển thị
- Thông tin bệnh nhân không load
- Phác đồ không hiển thị
- Phải nhập mã thủ công

## 🎬 Video Demo (Nếu Cần)

Nếu cần hỗ trợ, có thể quay video màn hình để:
1. Hiển thị màn hình Queue Management
2. Nhấp vào bệnh nhân
3. Quan sát kết quả
4. Gửi video để được hỗ trợ

## 📞 Báo Lỗi

Nếu gặp vấn đề, vui lòng cung cấp:
1. **Mô tả lỗi:** Điều gì xảy ra?
2. **Các bước tái hiện:** Làm thế nào để gặp lỗi?
3. **Kết quả mong đợi:** Bạn mong đợi điều gì?
4. **Kết quả thực tế:** Điều gì thực sự xảy ra?
5. **Screenshot/Video:** Nếu có

## 🎉 Kết Luận

Tính năng **auto-load bệnh nhân** đã được triển khai đầy đủ và sẵn sàng sử dụng. Bác sĩ chỉ cần nhấp vào bệnh nhân từ màn hình Home/Queue là có thể bắt đầu khám ngay, không cần quét QR code.

---

**Ngày tạo:** 29/03/2026
**Trạng thái:** ✅ Đã triển khai
**Build:** Thành công
