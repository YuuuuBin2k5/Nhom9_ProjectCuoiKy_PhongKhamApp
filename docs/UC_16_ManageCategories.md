# UC_16_ManageCategories: Quản lý danh mục

## 1. Thông tin chung
- **Mã Use Case**: UC_UC_23
- **Tên Use Case**: Quản lý danh mục
- **Tác nhân**: Administrator (Admin)
- **Tiền điều kiện**: 
  - Admin đã đăng nhập vào hệ thống với quyền hợp lệ.

## 2. Luồng sự kiện chính
1. Admin truy cập giao diện Quản lý Danh mục (Manage Categories).
2. Hệ thống hiển thị danh sách các danh mục hiện có.
3. Admin có thể thực hiện các thao tác:
   - **Tạo mới**: Nhập tên và mô tả danh mục mới.
   - **Cập nhật**: Chỉnh sửa thông tin danh mục hiện tại.
   - **Xóa/Vô hiệu hóa**: Xóa hoặc vô hiệu hóa danh mục không còn sử dụng.
4. Hệ thống kiểm tra tính hợp lệ của dữ liệu (ví dụ: không trùng tên).
5. Hệ thống lưu dữ liệu xuống Database.
6. Hệ thống trả về thông báo thành công và cập nhật giao diện.

## 3. Các trường hợp ngoại lệ
- **Tên danh mục trùng lặp**: Hệ thống báo lỗi.
- **Lỗi kết nối**: Thông báo lỗi mạng hoặc máy chủ.

## 4. Hậu điều kiện
- Trạng thái danh mục dịch vụ mới được lưu đồng bộ trên hệ thống.
