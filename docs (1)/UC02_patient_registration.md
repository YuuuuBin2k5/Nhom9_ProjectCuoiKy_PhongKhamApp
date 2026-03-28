# UC02: Use Case Đăng ký Bệnh nhân (Patient Registration)

## 1. Thông tin chung
- **Mã Use Case**: UC02
- **Tên Use Case**: Đăng ký tài khoản Bệnh nhân
- **Tác nhân**: Bệnh nhân mới (Chưa có tài khoản)
- **Tiền điều kiện**: Email đăng ký chưa tồn tại trong hệ thống.

## 2. Luồng sự kiện chính
1. Bệnh nhân chọn chức năng Đăng ký trên ứng dụng hoặc website.
2. Bệnh nhân nhập các thông tin: **Họ, Tên, Email, Mật khẩu, Số điện thoại**.
3. Hệ thống kiểm tra tính đầy đủ của các trường bắt buộc.
4. Hệ thống kiểm tra định dạng Email và độ phức tạp của Mật khẩu (tối thiểu 6 ký tự, bao gồm chữ và số).
5. Hệ thống kiểm tra xem Email đã được đăng ký trước đó chưa.
6. Hệ thống thực hiện mã hóa mật khẩu.
7. Hệ thống tạo mới bản ghi Bệnh nhân với trạng thái **Active = true**.
8. Hệ thống tự động khởi tạo **Mã QR định danh** cho bệnh nhân dựa trên ID vừa tạo.
9. Hệ thống khởi tạo cặp **JWT Access Token & Refresh Token**.
10. Hệ thống trả về thông báo thành công kèm theo Token và thông tin cơ bản.
11. Bệnh nhân được tự động đăng nhập vào giao diện chính.

## 3. Các trường hợp ngoại lệ
- **Thông tin thiếu/sai định dạng**: Hệ thống yêu cầu kiểm tra lại các trường Họ tên, Email hoặc định dạng Mật khẩu.
- **Email đã tồn tại**: Hệ thống thông báo Email đã được sử dụng và yêu cầu dùng Email khác hoặc Đăng nhập.
- **Lỗi hệ thống/Cơ sở dữ liệu**: Hệ thống thông báo lỗi và yêu cầu thử lại sau.

## 4. Hậu điều kiện
- Một tài khoản Bệnh nhân mới được tạo thành công và ở trạng thái sẵn sàng sử dụng.
- Bệnh nhân sở hữu một Mã QR định danh duy nhất để sử dụng tại phòng khám.
- Bệnh nhân có phiên làm việc (Session) hợp lệ để truy cập các dịch vụ khác.
