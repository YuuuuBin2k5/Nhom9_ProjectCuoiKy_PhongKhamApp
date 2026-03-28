# UC01: Use Case Đăng nhập (Login)

## 1. Thông tin chung
- **Mã Use Case**: UC01
- **Tên Use Case**: Đăng nhập vào hệ thống
- **Tác nhân**: 
  - Bệnh nhân (Patient)
  - Bác sĩ (Doctor)
  - Quản trị viên (Admin)
- **Tiền điều kiện**: Tài khoản người dùng đã tồn tại trong hệ thống và ở trạng thái **Đang hoạt động (Active)**.

## 2. Luồng sự kiện chính (Đăng nhập bằng Email/Mật khẩu)
1. Người dùng truy cập vào trang đăng nhập hoặc ứng dụng di động.
2. Người dùng nhập **Email** và **Mật khẩu**.
3. Hệ thống kiểm tra tính hợp lệ của dữ liệu đầu vào.
4. Hệ thống tìm kiếm tài khoản trong cơ sở dữ liệu theo thứ tự: Admin -> Doctor -> Patient.
5. Hệ thống kiểm tra trạng thái hoạt động của tài khoản (Trạng thái **Active** phải bằng true).
6. Hệ thống thực hiện băm mật khẩu và so sánh với mật khẩu đã lưu (BCrypt).
7. Hệ thống khởi tạo **JWT Access Token** và **Refresh Token**.
8. Hệ thống trả về thông tin xác thực bao gồm: Token, Vai trò (Role), ID người dùng và Email.
9. Người dùng đăng nhập thành công và được chuyển hướng vào giao diện chính tương ứng với vai trò.

## 3. Các trường hợp ngoại lệ
- **Thông tin không đầy đủ**: Hệ thống thông báo yêu cầu nhập đầy đủ Email/Mật khẩu.
- **Tài khoản không tồn tại**: Hệ thống thông báo lỗi xác thực (401 Unauthorized).
- **Mật khẩu sai**: Hệ thống thông báo lỗi đăng nhập (401 Unauthorized).
- **Tài khoản bị khóa (Inactive)**: Hệ thống thông báo tài khoản không khả dụng.

## 4. Hậu điều kiện
- Người dùng được cấp quyền truy cập vào các tài nguyên của hệ thống dựa trên vai trò được xác định trong JWT Token.
