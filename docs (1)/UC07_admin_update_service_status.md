# UC07: Use Case Quản trị viên cập nhật trạng thái Dịch vụ (Update Service Status)

## 1. Thông tin chung
- **Mã Use Case**: UC07
- **Tên Use Case**: Cập nhật trạng thái hoạt động của Dịch vụ
- **Tác nhân**: Quản trị viên (Admin)
- Tiền điều kiện: 
  - Admin đã đăng nhập và có quyền **ROLE_ADMIN**.
## 2. Luồng sự kiện chính
1. Admin chọn chức năng **Quản lý trạng thái dịch vụ**.
2. Hệ thống yêu cầu Admin **Chọn Danh mục (Category)** để hiển thị danh sách.
3. Hệ thống thực hiện **Xem danh sách Dịch vụ** tương ứng với danh mục được chọn.
4. Admin thực hiện **Thay đổi trạng thái (Active/Inactive)** cho một dịch vụ cụ thể.
5. Hệ thống nhận yêu cầu (`PATCH /api/admin/services/{id}/status?active=...`).
6. Hệ thống thực hiện **Cập nhật vào CSDL**:
   - Kiểm tra sự tồn tại của dịch vụ. Nếu không thấy, báo lỗi "Service not found".
   - Lưu trạng thái `active` mới.
7. Hệ thống trả về thông báo: "Status updated successfully".

## 3. Các trường hợp ngoại lệ
- **403 Forbidden**: Người dùng không có quyền quản trị.
- **400 Bad Request**: Không tìm thấy dịch vụ theo ID.

## 4. Hậu điều kiện
- Trạng thái hoạt động của dịch vụ được cập nhật.
- Dịch vụ ngừng hoạt động sẽ không hiển thị trên giao diện đặt lịch của Bệnh nhân.
