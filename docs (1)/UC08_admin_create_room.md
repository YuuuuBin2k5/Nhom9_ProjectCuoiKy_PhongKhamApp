# UC08: Use Case Quản trị viên tạo Phòng khám/Bộ phận mới (Create Clinic Room)

## 1. Thông tin chung
- **Mã Use Case**: UC08
- **Tên Use Case**: Tạo phòng khám/bộ phận mới
- **Tác nhân**: Quản trị viên (Admin)
- **Tiền điều kiện**: 
  - Admin đã đăng nhập và có quyền **ROLE_ADMIN**.

## 2. Luồng sự kiện chính
1. Admin truy cập giao diện Quản lý Phòng khám.
2. Admin chọn chức năng **Tạo phòng khám mới**.
3. Admin nhập các thông tin:
   - **Tên phòng**.
   - **Mô tả**.
4. Hệ thống nhận yêu cầu (`POST /api/admin/rooms`).
5. Hệ thống thực hiện các bước:
   - Kiểm tra tên phòng bế tắc (không được để trống).
   - Khởi tạo thực thể `ClinicRoom` với trạng thái mặc định **active = true**.
   - Lưu thông tin vào cơ sở dữ liệu.
6. Hệ thống trả về thông báo: "Phòng đã được tạo thành công".

## 3. Các trường hợp ngoại lệ
- **403 Forbidden**: Người dùng không có quyền quản trị.
- **400 Bad Request**: Tên phòng để trống.

## 4. Hậu điều kiện
- Phòng khám mới được tạo và sẵn sàng để gán cho Bác sĩ.
