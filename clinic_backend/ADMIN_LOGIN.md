# Admin Login Instructions

## Tài khoản Admin mặc định

Hệ thống đã được cấu hình với tài khoản admin mặc định:

**Email:** `admin@gmail.com`  
**Password:** `123456`

## Cách đăng nhập Admin trên Mobile App

1. **Mở ứng dụng mobile**
2. **Chọn "Đăng nhập"**
3. **Nhập thông tin:**
   - Email: `admin@gmail.com`
   - Mật khẩu: `123456`
4. **Nhấn "Đăng nhập"**

Ứng dụng sẽ tự động nhận diện role ADMIN và chuyển đến giao diện quản trị viên.

## Chức năng Admin có sẵn

### 1. Dashboard Admin
- Tổng quan hệ thống
- Thống kê dịch vụ
- Menu chức năng quản lý

### 2. Quản lý Bác sĩ
- Xem danh sách bác sĩ
- Thêm bác sĩ mới
- Thông tin chi tiết bác sĩ

### 3. Quản lý Dịch vụ
- Xem danh sách dịch vụ
- Thông tin giá cả và thời lượng

### 4. Quản lý Phòng khám
- Xem danh sách phòng khám
- Trạng thái phòng
- Số người chờ

## Tài khoản khác có sẵn

### Bác sĩ
**Email:** `doctor@gmail.com`  
**Password:** `123456`  
**Role:** DOCTOR

### Bệnh nhân test
**Email:** `patient@gmail.com`  
**Password:** `123456`  
**Role:** PATIENT

## Lưu ý bảo mật

- Đổi mật khẩu admin mặc định trong môi trường production
- Tài khoản admin có quyền cao nhất trong hệ thống
- Tất cả API admin đều yêu cầu authentication với role ADMIN

## Cấu hình Seed Data

Tài khoản admin được tạo tự động bởi `AdminSeedRunner.java`:
- Chạy khi khởi động ứng dụng
- Chỉ tạo nếu chưa có admin nào trong database
- Sử dụng BCrypt để mã hóa mật khẩu