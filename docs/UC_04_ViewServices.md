# UC_04_ViewServices: Xem dịch vụ

## 1. Thông tin chung
- **Mã Use Case**: UC_04
- **Tên Use Case**: Xem dịch vụ
- **Tác nhân**: Patient
- **Tiền điều kiện**: Không yêu cầu đăng nhập đối với chức năng xem danh mục công khai.

## 2. Luồng sự kiện chính
1. Bệnh nhân truy cập trang/màn hình Danh mục Dịch vụ trên hệ thống.
2. Hệ thống tải trực quan danh sách các nhóm Category.
3. Bệnh nhân click chọn một Category cụ thể.
4. Hệ thống tải lên danh sách các Dịch vụ (Services) được gán trạng thái `active=true` thuộc nhóm đó.
5. Bệnh nhân có thể bấm vào xem chi tiết: Tên, Mô tả, Giá tiền, Thời lượng, Đánh giá trung bình (Ratings), và chùm Hình ảnh.

## 3. Các trường hợp ngoại lệ
- **Lỗi truy vấn db/lỗi mạng**: Hệ thống không thể tải danh sách dịch vụ, hiển thị pop-up "Failed to load services".
- **Danh mục phòng trống**: Không có dịch vụ nào tồn tại, hệ thống hiển thị "No services available".

## 4. Hậu điều kiện
- Bệnh nhân nắm rõ cấu trúc và thông tin chi tiết dịch vụ, sẵn sàng chuyển tiếp qua chức năng `Book Appointment`.
