# UC05: Use Case Quản trị viên tạo danh mục dịch vụ (Create Service Category)

## 1. Thông tin chung
- **Mã Use Case**: UC05
- **Tên Use Case**: Tạo danh mục dịch vụ mới
- **Tác nhân**: Quản trị viên (Admin)
- **Tiền điều kiện**: 
  - Admin đã đăng nhập vào hệ thống.
  - User có quyền **ROLE_ADMIN**.

## 2. Luồng sự kiện chính
1. Admin truy cập vào giao diện Quản lý Danh mục.
2. Admin chọn chức năng "Tạo danh mục mới".
3. Admin nhập các thông tin: **Tên danh mục** và **Mô tả**.
4. Hệ thống nhận yêu cầu (`POST /api/admin/services/categories`).
5. Hệ thống thực hiện kiểm tra quyền truy cập.
6. Hệ thống khởi tạo đối tượng danh mục mới và lưu vào cơ sở dữ liệu.
7. Hệ thống trả về thông báo: "Category created successfully" kèm theo ID và Tên danh mục vừa tạo.

## 3. Các trường hợp ngoại lệ
- **403 Forbidden**: Người dùng không có quyền quản trị.
- **Lỗi dữ liệu**: Tên danh mục để trống hoặc không hợp lệ (tùy thuộc vào ràng buộc DB).

## 4. Hậu điều kiện
- Một danh mục dịch vụ mới được tạo thành công.
- Admin có thể bắt đầu thêm các dịch vụ cụ thể vào danh mục này (UC06).
