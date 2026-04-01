# UC_17_ManageServices: Quản lý dịch vụ

## 1. Thông tin chung
- **Mã Use Case**: UC_UC_24
- **Tên Use Case**: Quản lý dịch vụ
- **Tác nhân**: Administrator (Admin)
- **Tiền điều kiện**: 
  - Admin đã đăng nhập.
  - Đã có ít nhất một danh mục (Category) tồn tại trong hệ thống.

## 2. Luồng sự kiện chính
1. Admin chọn một danh mục dịch vụ cụ thể.
2. Hệ thống tải danh sách các dịch vụ thuộc danh mục đó.
3. Admin thực hiện thêm mới dịch vụ hoặc bật/tắt (enable/disable) dịch vụ.
4. **Tạo mới**: Admin nhập cấu hình (Tên, Mô tả, Giá tiền, Thời lượng, Hình ảnh).
5. **Enable/Disable**: Admin nhấn toggle chuyển trạng thái (Active/Inactive).
6. Hệ thống validate payload và lưu thông tin dịch vụ vào DB.
7. Hệ thống thông báo thành công.

## 3. Các trường hợp ngoại lệ
- **Thiếu thông tin bắt buộc**: Cảnh báo không cho phép khởi tạo dịch vụ.

## 4. Hậu điều kiện
- Dịch vụ được khởi tạo hoặc thay đổi trạng thái thành công. Các dịch vụ "Active" tự động hiển thị cho bệnh nhân đặt lịch.
