# UC_02_ForgotPassword: Quên mật khẩu

## 1. Thông tin chung
- **Mã Use Case**: UC_02
- **Tên Use Case**: Quên mật khẩu
- **Tác nhân**: Patient
- **Tiền điều kiện**: Bệnh nhân không đăng nhập được do quên mật khẩu gốc.

## 2. Luồng sự kiện chính
1. Bệnh nhân nhấn "Quên mật khẩu" ở màn hình đăng nhập chính.
2. Bệnh nhân nhập số điện thoại hoặc email khôi phục đã đăng ký.
3. Hệ thống kiểm tra tài khoản tương ứng từ DB.
4. Hệ thống gửi mã OTP xác thực (qua SMS/Email) đến bệnh nhân.
5. Bệnh nhân điền mã OTP để xác nhận nhân thân.
6. Bệnh nhân thực hiện gõ mật khẩu mới.
7. Hệ thống tiến hành mã hóa và cập nhật mật khẩu xuống cơ sở dữ liệu.
8. Hệ thống thông báo thành công.

## 3. Các trường hợp ngoại lệ
- **Sai OTP**: Hệ thống báo lỗi OTP không khớp hoặc đã hết hạn.
- **Tài khoản không tồn tại**: Thông báo không tìm thấy thông tin trên hệ thống.

## 4. Hậu điều kiện
- Bệnh nhân được cấp quyền truy cập qua giao diện đăng nhập với mật khẩu mới lập.
