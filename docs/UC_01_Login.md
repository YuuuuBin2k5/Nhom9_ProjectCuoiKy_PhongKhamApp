# UC_01_Login: Đăng nhập

## 1. Thông tin chung
- **Mã Use Case**: UC_01
- **Tên Use Case**: Đăng nhập
- **Tác nhân**: Patient
- **Tiền điều kiện**: Bệnh nhân đã đăng ký tài khoản thành công.

## 2. Luồng sự kiện chính
1. Bệnh nhân mở ứng dụng/trang web và chọn "Đăng nhập".
2. Bệnh nhân nhập số điện thoại hoặc email và mật khẩu.
3. Bệnh nhân nhấn nút "Đăng nhập".
4. Hệ thống kiểm tra thông tin định danh.
5. Hệ thống xác thực thành công và cấp quyền truy cập.
6. Hệ thống chuyển hướng bệnh nhân vào Trang chủ.

## 3. Các trường hợp ngoại lệ
- **Sai thông tin**: Mật khẩu hoặc tài khoản không đúng, hệ thống báo lỗi xác thực "Invalid credentials".
- **Tài khoản bị khóa**: Hệ thống báo lỗi "Account disabled".

## 4. Hậu điều kiện
- Bệnh nhân có thể sử dụng các chức năng yêu cầu quyền đăng nhập.
