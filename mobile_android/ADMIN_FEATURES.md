# Admin Features - Mobile App

## Tổng quan
Ứng dụng mobile đã được tích hợp các chức năng quản trị viên (Admin) dựa trên thiết kế từ project Nhom9_Project_PhongKham.

## Tài khoản Admin mặc định

**Email:** `admin@gmail.com`  
**Password:** `123456`

## Chức năng Admin

### 1. Đăng nhập Admin
- Khi đăng nhập với tài khoản có role "ADMIN", ứng dụng sẽ tự động điều hướng đến giao diện admin
- Giao diện admin khác hoàn toàn với giao diện bệnh nhân

### 2. Màn hình chính Admin (AdminMainActivity)
- Tổng quan hệ thống với biểu đồ thống kê
- Menu chức năng quản lý:
  - Quản lý dịch vụ
  - Quản lý phòng khám  
  - Quản lý bác sĩ
  - QR Check-in
  - Đăng xuất

### 3. Quản lý bác sĩ (AdminDoctorActivity)
- Xem danh sách tất cả bác sĩ
- Thêm bác sĩ mới với form đầy đủ:
  - Họ tên
  - Email (tên đăng nhập)
  - Mật khẩu
  - Chuyên khoa
  - Kinh nghiệm (năm)
  - Tiểu sử

### 4. Quản lý dịch vụ (AdminServiceActivity)
- Xem danh sách tất cả dịch vụ
- Hiển thị thông tin: tên, mô tả, giá, thời lượng

### 5. Quản lý phòng khám (AdminRoomActivity)
- Xem danh sách phòng khám
- Hiển thị trạng thái và số người chờ

## Cấu trúc Code

### Activities
- `AdminMainActivity`: Màn hình chính admin
- `AdminDoctorActivity`: Quản lý bác sĩ
- `AdminServiceActivity`: Quản lý dịch vụ  
- `AdminRoomActivity`: Quản lý phòng khám

### Adapters
- `AdminDoctorAdapter`: Adapter cho danh sách bác sĩ
- `AdminServiceAdapter`: Adapter cho danh sách dịch vụ
- `AdminRoomAdapter`: Adapter cho danh sách phòng khám

### Models
- `CreateDoctorRequest`: Request tạo bác sĩ mới
- `RoomItem`: Model phòng khám
- `MessageResponse`: Response chung

### API Endpoints
- `POST /api/admin/doctors`: Tạo bác sĩ mới
- `GET /api/admin/rooms`: Lấy danh sách phòng khám
- `GET /api/doctors`: Lấy danh sách bác sĩ
- `GET /api/services`: Lấy danh sách dịch vụ

## Layouts
- `activity_admin_main.xml`: Layout màn hình chính admin
- `activity_admin_doctor.xml`: Layout quản lý bác sĩ
- `activity_admin_service.xml`: Layout quản lý dịch vụ
- `activity_admin_room.xml`: Layout quản lý phòng khám
- `dialog_add_doctor.xml`: Dialog thêm bác sĩ
- `item_admin_doctor.xml`: Item layout bác sĩ
- `item_admin_service.xml`: Item layout dịch vụ
- `item_admin_room.xml`: Item layout phòng khám

## Cách sử dụng

1. **Đăng nhập**: Sử dụng tài khoản admin để đăng nhập
2. **Điều hướng**: Ứng dụng tự động chuyển đến giao diện admin
3. **Quản lý**: Sử dụng các card chức năng để truy cập từng module quản lý
4. **Thêm mới**: Sử dụng FAB (Floating Action Button) để thêm bác sĩ mới
5. **Đăng xuất**: Sử dụng card "Đăng xuất" để thoát khỏi phiên làm việc

## Lưu ý
- Tất cả API admin đều yêu cầu role "ADMIN"
- Giao diện được thiết kế responsive cho mobile
- Sử dụng Material Design components
- Tích hợp với backend Spring Boot hiện có