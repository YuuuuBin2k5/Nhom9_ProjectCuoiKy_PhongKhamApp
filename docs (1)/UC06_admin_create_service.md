# UC06: Use Case Quản trị viên tạo Dịch vụ mới (Create Service)

## 1. Thông tin chung
- **Mã Use Case**: UC06
- **Tên Use Case**: Tạo dịch vụ nha khoa mới
- **Tác nhân**: Quản trị viên (Admin)
- **Tiền điều kiện**: 
  - Admin đã đăng nhập và có quyền **ROLE_ADMIN**.
  - Danh mục dịch vụ (`Category`) liên quan phải đã tồn tại.

## 2. Luồng sự kiện chính
1. Admin truy cập giao diện Quản lý Danh mục.
2. Admin **chọn một Danh mục (Category)** cụ thể để quản lý các dịch vụ bên trong.
3. Admin chọn chức năng "Thêm dịch vụ mới" (categoryId được gán tự động từ danh mục đang chọn).
4. Admin nhập các thông tin dịch vụ (`ServiceRequest`):
   - **Tên dịch vụ**.
   - **Mô tả**.
   - **Giá tiền** (Hệ thống sẽ chuyển thành BigDecimal).
   - **Thời lượng dự kiến** (Duration in minutes).
   - **Danh sách link ảnh** (Image URLs).
5. Hệ thống nhận yêu cầu (`POST /api/admin/services`).
6. Hệ thống thực hiện các bước:
   - Kiểm tra id danh mục. Nếu không tồn tại, trả về lỗi "Category not found".
   - Khởi tạo thực thể `Service` với trạng thái mặc định **active = true**.
   - Trích xuất tên file từ các URL ảnh (nếu là link đầy đủ sẽ lấy phần cuối sau `/uploads/`).
   - Lưu thông tin dịch vụ và ảnh vào cơ sở dữ liệu.
7. Hệ thống trả về thông báo: "Service created successfully" kèm theo ID và Tên dịch vụ.

## 3. Các trường hợp ngoại lệ
- **403 Forbidden**: Người dùng không có quyền quản trị.
- **400 Bad Request**: Danh mục không tồn tại hoặc dữ liệu không hợp lệ.

## 4. Hậu điều kiện
- Dịch vụ mới được tạo và hiển thị ở trạng thái Đang hoạt động.
- Bệnh nhân có thể xem và đặt lịch cho dịch vụ này.
