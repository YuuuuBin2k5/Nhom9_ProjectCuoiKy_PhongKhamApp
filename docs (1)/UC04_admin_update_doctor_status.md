# UC04: Use Case Quản trị viên cập nhật trạng thái Bác sĩ (Update Doctor Status)

## 1. Thông tin chung
- **Mã Use Case**: UC04
- **Tên Use Case**: Cập nhật trạng thái hoạt động của Bác sĩ
- **Tác nhân**: Quản trị viên (Admin)
- **Tiền điều kiện**: 
  - Admin đã đăng nhập và có quyền **ROLE_ADMIN**.
  - Bác sĩ cần cập nhật phải tồn tại trong hệ thống.

## 2. Luồng sự kiện chính
1. Admin truy cập giao diện danh sách Bác sĩ (`GET /api/admin/doctors`).
2. Admin chọn một bác sĩ cụ thể từ danh sách.
3. Admin thực hiện thay đổi trạng thái (**Kích hoạt** hoặc **Khóa**) cho bác sĩ đó.
4. Hệ thống nhận yêu cầu (`PATCH /api/admin/doctors/{id}/status?active=...`).
5. Hệ thống kiểm tra sự tồn tại của bác sĩ theo ID cung cấp.
6. Nếu tìm thấy, hệ thống cập nhật giá trị `isActive` tương ứng vào cơ sở dữ liệu.
7. Hệ thống trả về thông báo: "Doctor status updated successfully".

## 3. Các trường hợp ngoại lệ
- **403 Forbidden**: Người dùng không có quyền quản trị.
- **Bác sĩ không tồn tại (400 Bad Request)**: Hệ thống trả về lỗi "Doctor not found with id: {id}" nếu ID không hợp lệ hoặc bác sĩ đã bị xóa.

## 4. Hậu điều kiện
- Trạng thái `isActive` của bác sĩ được cập nhật.
- Nếu bị **Khóa**, bác sĩ sẽ không thể đăng nhập vào hệ thống ở lần truy cập tiếp theo.
