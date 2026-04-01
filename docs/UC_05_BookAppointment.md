# UC_05_BookAppointment: Đặt lịch khám

## 1. Thông tin chung
- **Mã Use Case**: UC_05
- **Tên Use Case**: Đặt lịch khám
- **Tác nhân**: Patient
- **Tiền điều kiện**: Bệnh nhân bắt buộc phải đăng nhập để định danh hồ sơ y tế.

## 2. Luồng sự kiện chính
1. Bệnh nhân nhấn nút `Đặt lịch` từ danh sách Dịch vụ (Service) đã chọn.
2. Hệ thống hiển thị modal pop-up điền form lịch hẹn.
3. Bệnh nhân tiến hành chọn: Bác sĩ (tùy chọn), Ngày hẹn, Khung giờ mong muốn có sẵn.
4. Bệnh nhân nhập TextBox ghi chú thêm tình trạng (nếu có).
5. Hệ thống check logic khung giờ và lịch trực của Bác sĩ trong Database để phòng khối tránh trùng lịch.
6. Hệ thống thực thi lưu Reservation xuống DB với trạng thái Booking là `PENDING`.
7. Hệ thống tự động push notification gửi thông báo xác nhận lịch hẹn về SĐT hoặc App của patient.

## 3. Các trường hợp ngoại lệ
- **Trùng lặp khung giờ**: Bác sĩ (hoặc phòng nha) đã full chỗ, hệ thống báo lỗi đỏ "Time slot fully booked".
- **Khung ngoài giờ trực**: Khung giờ chọn vượt quá lịch nghỉ của phòng khám.

## 4. Hậu điều kiện
- Chốt xong một lịch hẹn mới được tạo thành công chờ bệnh nhân đến phòng vật lý để đến bước Check-in.
