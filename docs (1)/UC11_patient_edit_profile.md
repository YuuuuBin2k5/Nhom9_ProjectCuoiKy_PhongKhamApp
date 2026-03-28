# UC11: Use Case Bệnh nhân chỉnh sửa hồ sơ cá nhân (Patient Edit Profile)

## 1. Thông tin chung
- **Mã Use Case**: UC11
- **Tên Use Case**: Chỉnh sửa hồ sơ cá nhân
- **Tác nhân**: Bệnh nhân
- **Tiền điều kiện**: 
  - Người dùng đã đăng nhập với vai trò Bệnh nhân.

## 2. Luồng sự kiện chính
1. Bệnh nhân gửi yêu cầu cập nhật (`PUT /api/patients/me`) với Body JSON (`UpdatePatientRequest`):
   - `firstName`, `lastName`, `phone`, `address`, `gender`, `dob`, `avatarUrl`, `allergies`, `underlyingConditions`, `bloodType`.
2. Hệ thống thực hiện các bước backend:
   - **Xác thực Identity**: Lấy `patientId` từ Security Context.
   - **Tìm kiếm dữ liệu**: Truy vấn `Patient` kèm theo `PatientProfile`.
   - **Đồng bộ thông tin**: 
     - Cập nhật các trường cơ bản (trim dữ liệu chuỗi).
     - Chuyển đổi `dob` từ String sang `LocalDate`.
     - **Quản lý Profile**: Nếu bệnh nhân chưa có `PatientProfile`, hệ thống tự động khởi tạo mới.
   - **Thực thi nghiệp vụ**: Gọi `patientRepository.saveAndFlush(p)` trong một Transaction.
3. Hệ thống trả về đối tượng `PatientMeResponse` chứa dữ liệu mới nhất.

## 3. Các trường hợp ngoại lệ
- **401 Unauthorized**: Token không hợp lệ hoặc hết hạn.
- **500 Internal Server Error**: Lỗi kết nối cơ sở dữ liệu.

## 4. Hậu điều kiện
- Hồ sơ của bệnh nhân được cập nhật thành công và hiển thị thông tin mới nhất.
