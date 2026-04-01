# UC_03_ManageAccount: Quản lý tài khoản

## 1. Thông tin chung
- **Mã Use Case**: UC_03
- **Tên Use Case**: Quản lý tài khoản
- **Tác nhân**: Patient
- **Tiền điều kiện**: Bệnh nhân đã đăng nhập vào hệ thống (ngoại trừ chức năng Đăng ký).

## 2. Luồng sự kiện chính
- **Đăng ký (Register)**:
  1. Bệnh nhân nhập thông tin nhân trắc học: Họ tên, SĐT, Giới tính, Ngày sinh, Mật khẩu.
  2. Hệ thống rà soát SĐT tự động (chống trùng lặp), lưu tài khoản mang Role `PATIENT`.
- **Cập nhật hồ sơ (Update Profile)**:
  1. Bệnh nhân truy cập mục "Hồ sơ cá nhân".
  2. Bệnh nhân chỉnh sửa thêm Tùy chọn (Email, Hình đại diện, Địa chỉ, Tiền sử bệnh nền).
  3. Hệ thống kiểm tra tính vẹn toàn và update dữ liệu lên Server.
  4. Thông báo quá trình cập nhật diễn ra hoàn tất.

## 3. Các trường hợp ngoại lệ
- **Lỗi tải ảnh**: Ảnh upload vượt kích thước file upload config hoặc nhầm định dạng.
- **SĐT đã được đăng ký**: Hệ thống báo lỗi "Phone number already fully registered".

## 4. Hậu điều kiện
- Thông tin bệnh nhân được lưu ổn định và được mang đi đồng bộ với tất cả các hồ sơ y tế liên đới.
