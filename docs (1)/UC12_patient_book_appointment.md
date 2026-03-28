# UC12: Use Case Bệnh nhân đặt lịch khám (Patient Book Appointment)

## 1. Thông tin chung
- **Mã Use Case**: UC12
- **Tên Use Case**: Đặt lịch khám
- **Tác nhân**: Bệnh nhân
- **Tiền điều kiện**: 
  - Người dùng đã đăng nhập với vai trò Bệnh nhân.

## 2. Luồng sự kiện chính
1. Bệnh nhân chọn **Danh mục dịch vụ** (ví dụ: Nha khoa tổng quát, Chỉnh nha).
2. Bệnh nhân chọn **Dịch vụ** cụ thể trong danh mục đó.
3. Bệnh nhân có thể chọn **Bác sĩ** mong muốn (không bắt buộc).
4. Bệnh nhân chọn **Ngày và Giờ** khám từ lịch biểu hiển thị.
5. Bệnh nhân xác nhận đặt lịch. Hệ thống gửi yêu cầu (`POST /api/appointments`) với Body JSON (`AppointmentRequest`):
   - `service_id`: ID của dịch vụ đã chọn.
   - `doctor_id`: ID của bác sĩ (null nếu tự động sắp xếp).
   - `appointment_datetime`: Thời gian mong muốn (ví dụ: `2026-03-24 10:00:00`).
   - `booking_type`: "ONLINE".
6. Hệ thống thực hiện các bước backend:
   - **Xác thực**: Kiểm tra token và lấy thông tin bệnh nhân.
   - **Kiểm tra trùng lặp**: Đảm bảo bệnh nhân không có lịch khám nào khác đang ở trạng thái `SCHEDULED` hoặc `IN_PROGRESS`.
   - **Phân bổ bác sĩ**: Nếu `doctor_id` trống, hệ thống tự động gán bác sĩ còn trống dựa trên chuyên môn của dịch vụ.
   - **Kiểm tra tính khả dụng**: 
     - Thời gian khám phải trong khung giờ hoạt động (08:00 - 16:40).
     - Bác sĩ không bận trong khoảng thời gian +/- 30 phút so với giờ hẹn.
   - **Lưu dữ liệu**: Khởi tạo bản ghi `Appointment` với trạng thái `SCHEDULED`.
7. Hệ thống trả về thông báo thành công cùng chi tiết lịch hẹn (`UpcomingAppointment`).

## 3. Các trường hợp ngoại lệ
- **400 Bad Request**: 
  - Bệnh nhân đang có lịch khám chưa hoàn thành.
  - Bác sĩ đã có lịch hẹn trong khung giờ được chọn.
  - Định dạng ngày giờ không hợp lệ hoặc ngoài giờ làm việc.
  - Không tìm thấy bác sĩ phù hợp cho dịch vụ.
- **401 Unauthorized**: Token không hợp lệ hoặc hết hạn.
- **500 Internal Server Error**: Lỗi hệ thống hoặc cơ sở dữ liệu.

## 4. Hậu điều kiện
- Lịch khám được lưu vào hệ thống và hiển thị trong danh sách "Sắp tới" của bệnh nhân.
