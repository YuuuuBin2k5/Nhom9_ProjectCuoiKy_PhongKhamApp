# UC_19_ManageRooms: Quản lý phòng khám

## 1. Thông tin chung
- **Mã Use Case**: UC_UC_26
- **Tên Use Case**: Quản lý phòng khám
- **Tác nhân**: Administrator (Admin)
- **Tiền điều kiện**: Admin đã đăng nhập hệ thống với quyền hợp lệ.

## 2. Luồng sự kiện chính
1. Admin điều hướng tới giao diện Quản lý Phòng Khám (Manage Rooms).
2. Hệ thống tải lên danh sách toàn bộ các phòng khám.
3. Admin chọn tính năng "Thêm mới" hoặc "Thay đổi trạng thái".
4. **Tạo mới**: Admin nhập cấu hình (mã phòng, tên phòng, tầng/vị trí).
5. **Enable/Disable**: Admin nhấn toggle chuyển trạng thái (Active/Inactive) để báo hiệu phòng đang khả dụng hoặc dùng để bảo trì.
6. Hệ thống kiểm tra dữ liệu, định cấu hình tránh trùng lặp mã phòng.
7. Hệ thống lưu thông tin và trạng thái cập nhật xuống Database.
8. Giao diện tải lại với trạng thái mới.

## 3. Các trường hợp ngoại lệ
- **Mã phòng trùng lặp**: Hệ thống từ chối tạo mới.

## 4. Hậu điều kiện
- Phòng mới được khởi tạo và sẵn sàng hoặc được thay đổi trạng thái đồng bộ đối với hệ thống hàng đợi và phân luồng điều trị.
