# UC_18_ManageDoctors: Quản lý bác sĩ

## 1. Thông tin chung
- **Mã Use Case**: UC_UC_25
- **Tên Use Case**: Quản lý bác sĩ
- **Tác nhân**: Administrator (Admin)
- **Tiền điều kiện**: 
  - Admin đã xác thực vào hệ thống.

## 2. Luồng sự kiện chính
1. Admin điều hướng đến trang Quản lý Bác sĩ.
2. Hệ thống hiển thị danh sách toàn bộ các bác sĩ hành nghề.
3. Admin chọn tính năng Tạo mới hoặc Bật/Tắt (Enable/Disable) tài khoản bác sĩ.
4. **Tạo mới**: Cấp phát tài khoản bằng cách nhập Username, Mật khẩu, Tên, Chuyên khoa, Số điện thoại.
5. **Enable/Disable**: Khóa (đình chỉ) hoặc mở khóa tài khoản bác sĩ.
6. Hệ thống thực thi xử lý DB.
7. Cập nhật giao diện tự động.

## 3. Các trường hợp ngoại lệ
- **Tên đăng nhập đã tồn tại**: Hệ thống từ chối tạo mới tài khoản.

## 4. Hậu điều kiện
- Bác sĩ có thể đăng nhập ngay lập tức nếu tài khoản được đánh dấu ở trạng thái 'Enable'.
