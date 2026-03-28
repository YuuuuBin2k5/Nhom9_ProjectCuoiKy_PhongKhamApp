# UC03: Use Case Quản trị viên tạo tài khoản Bác sĩ (Admin Create Doctor)

## 1. Thông tin chung
- **Mã Use Case**: UC03
- **Tên Use Case**: Tạo tài khoản Bác sĩ mới
- **Tác nhân**: Quản trị viên (Admin)
- **Tiền điều kiện**: 
  - Admin đã đăng nhập hệ thống.
  - Phải có quyền **ROLE_ADMIN** để truy cập endpoint `/api/admin/doctors`.

## 2. Luồng sự kiện chính
1. Admin truy cập trang Quản lý Bác sĩ và chọn **Thêm Bác sĩ**.
2. Admin nhập các thông tin (`CreateDoctorRequest`):
   - **Email** (bắt buộc, không được để trống).
   - **Mật khẩu** (bắt buộc, tối thiểu 6 ký tự).
   - **Họ & Tên**.
   - **Chuyên khoa**: Nhận diện từ trường `specialty` hoặc `specialization` trong request.
   - **Số giấy phép (License Number)**.
   - **Phòng khám (Clinic Room ID)**: Không bắt buộc.
   - **Số năm kinh nghiệm & Tiểu sử (Bio)**: Có trên giao diện nhưng chưa được lưu vào database ở phiên bản hiện tại.
3. Hệ thống chuẩn hóa dữ liệu: Trim và chuyển Email về chữ thường (lowercase).
4. Hệ thống kiểm tra:
   - Nếu Email trống: Báo lỗi "Email is required".
   - Nếu Mật khẩu < 6 ký tự: Báo lỗi "Password must be at least 6 characters".
   - Nếu Email đã tồn tại cho một bác sĩ khác: Báo lỗi "Email already registered for a doctor".
5. Hệ thống tìm kiếm Phòng khám (nếu có `clinicRoomId`). Nếu không tìm thấy, để trống (null).
6. Hệ thống thực hiện mã hóa mật khẩu (BCrypt).
7. Hệ thống tạo thực thể `Doctor`, gán trạng thái **isActive = true**.
8. Hệ thống lưu bác sĩ mới vào DB và trả về thông báo thành công kèm theo ID và Email của bác sĩ.

## 3. Các trường hợp ngoại lệ
- **403 Forbidden**: Người dùng không có quyền Admin.
- **400 Bad Request**: Sai định dạng dữ liệu hoặc Email đã tồn tại.
- **Lỗi tìm kiếm**: Nếu `clinicRoomId` không hợp lệ, phòng khám sẽ trả về `null` nhưng không làm dừng quy trình tạo bác sĩ.

## 4. Hậu điều kiện
- Tài khoản bác sĩ mới được kích hoạt ngay lập tức (`isActive = true`).
- Bác sĩ có thể đăng nhập ngay vào giao diện chuyên môn dùng Email/Password được cấp.
