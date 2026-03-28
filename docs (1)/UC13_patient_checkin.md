# UC13: Use Case Bệnh nhân check-in (Patient Check-in)

## 1. Thông tin chung
- **Mã Use Case**: UC13
- **Tên Use Case**: Check-in xác nhận hiện diện
- **Tác nhân**: Bệnh nhân
- **Tiền điều kiện**: 
  - Bệnh nhân đã có lịch hẹn trong ngày hôm nay.
  - Người dùng đã đăng nhập vào ứng dụng di động.

## 2. Luồng sự kiện chính
1. Bệnh nhân đến phòng khám và mở tính năng **Check-in** trên ứng dụng.
2. Hệ thống cung cấp hai phương thức xác nhận:
   - **Cách 1: Quét mã QR**: Bệnh nhân dùng camera điện thoại quét mã QR được đặt tại quầy lễ tân hoặc cửa phòng khám.
   - **Cách 2: Nhập mã số**: Nếu camera hỏng hoặc không quét được, bệnh nhân nhập trực tiếp **Mã lịch hẹn** (Appointment ID) vào ứng dụng.
3. Ứng dụng gửi yêu cầu (`POST /api/checkin/self-scan`) với Body JSON (`CheckInScanRequest`):
   - `qrData`: Nội dung mã QR (ví dụ: `CLINIC_CHECKIN_RECEPTION` hoặc `CHECKIN:123`) hoặc Mã số nhập tay (ví dụ: `123`).
4. Hệ thống thực hiện các bước backend:
   - **Nhận dạng**: Phân tích `qrData` để xác định Appointment ID.
   - **Xác thực quyền sở hữu**: Kiểm tra xem lịch hẹn đó có thuộc về bệnh nhân đang đăng nhập hay không.
   - **Kiểm tra thời gian**: Đảm bảo lịch hẹn diễn ra trong ngày hôm nay.
   - **Khởi tạo hàng đợi**: 
     - Tìm phòng khám tương ứng với bác sĩ trong lịch hẹn.
     - Cấp số thứ tự (Queue Number) mới nhất cho phòng đó.
     - Tạo bản ghi trong `CheckInQueue` với trạng thái `WAITING`.
   - **Gửi thông báo**: Lưu thông báo "Check-in thành công" vào hồ sơ bệnh nhân.
5. Hệ thống trả về kết quả thành công:
   - Số thứ tự (Queue Number).
   - Vị trí hiện tại trong hàng đợi.
   - Tên phòng và vị trí phòng khám.
   - Thời gian chờ dự kiến.

## 3. Các trường hợp ngoại lệ
- **400 Bad Request**: 
  - Mã QR không đúng định dạng.
  - Bệnh nhân không có lịch hẹn nào trong ngày hôm nay.
  - Lịch hẹn không phải của ngày hôm nay.
- **403 Forbidden**: 
  - Mã lịch hẹn không thuộc về bệnh nhân này.
  - Tài khoản bệnh nhân bị khóa/vô hiệu hóa.
- **404 Not Found**: Không tìm thấy lịch hẹn tương ứng với mã đã nhập.
- **500 Internal Server Error**: Lỗi hệ thống khi khởi tạo hàng đợi.

## 4. Hậu điều kiện
- Bệnh nhân được đưa vào danh sách chờ của phòng khám và có thể theo dõi số thứ tự trên ứng dụng.
