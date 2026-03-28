# UC09: Use Case Quản trị viên cập nhật trạng thái Phòng khám (Update Clinic Room Status)

## 1. Thông tin chung
- **Mã Use Case**: UC09
- **Tên Use Case**: Cập nhật trạng thái hoạt động của Phòng khám
- **Tác nhân**: Quản trị viên (Admin)
- **Tiền điều kiện**: 
  - Admin đã đăng nhập và có quyền **ROLE_ADMIN**.

## 2. Luồng sự kiện chính
1. Admin chọn chức năng **Quản lý trạng thái phòng khám**.
2. Hệ thống thực hiện **Xem danh sách Phòng** hiện có.
3. Admin chọn một phòng cụ thể và thực hiện **Thay đổi trạng thái (Active/Inactive)**.
4. Hệ thống nhận yêu cầu (`PATCH /api/admin/rooms/{id}/status?active=...`).
5. Hệ thống thực hiện **Cập nhật vào CSDL**:
   - Kiểm tra sự tồn tại của phòng khám.
   - Cập nhật trạng thái `active`.
   - Lưu thông tin.
6. Hệ thống trả về thông báo: "Cập nhật thành công".

## 3. Các trường hợp ngoại lệ
- **403 Forbidden**: Người dùng không có quyền quản trị.
- **404 Not Found**: Không tìm thấy phòng khám theo ID.

## 4. Hậu điều kiện
- Trạng thái hoạt động của phòng khám được cập nhật.
