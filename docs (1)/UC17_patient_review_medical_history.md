# UC17: Use Case Bệnh nhân Xem lại lịch sử khám (Medical History Review)

## 1. Thông tin chung
- **Mã Use Case**: UC17
- **Tên Use Case**: Xem lại lịch sử khám
- **Tác nhân**: Bệnh nhân
- **Tiền điều kiện**: 
  - Bệnh nhân đã đăng nhập vào hệ thống.
  - Bệnh nhân đã có ít nhất một lần khám hoặc một lộ trình điều trị trong quá khứ.

## 2. Luồng sự kiện chính
1. Bệnh nhân mở ứng dụng và truy cập mục **Hồ sơ/Lịch sử**.
2. Hệ thống hiển thị giao diện **Tra cứu lịch sử khám** tổng quát.
3. Bệnh nhân có thể thực hiện các hành động tùy chọn sau (mở rộng):
   - **Xem hồ sơ bệnh án**: Chọn một lần khám để xem chi tiết chẩn đoán và dịch vụ.
     - Trong khi xem hồ sơ, bệnh nhân có thể chọn **Xem đơn thuốc** (tùy chọn mở rộng).
   - **Xem tiến độ phác đồ**: Chuyển sang danh sách các liệu trình điều trị để xem trạng thái hoàn thiện.
   - **Xem lịch sử hóa đơn**: Xem lại danh sách các hóa đơn và trạng thái thanh toán.
4. Bệnh nhân nắm bắt được toàn bộ quá trình chăm sóc sức khỏe của mình tại phòng khám.

## 3. Các trường hợp ngoại lệ
- **401 Unauthorized**: Phiên đăng nhập hết hạn.
- **No Data**: Bệnh nhân là người mới và chưa có bất kỳ dữ liệu khám chữa bệnh nào.

## 4. Hậu điều kiện
- Bệnh nhân nắm rõ tình trạng sức khỏe cá nhân, lịch sử các lần điều trị và các loại thuốc đã sử dụng để thuận tiện cho việc tái khám hoặc theo dõi sức khỏe dài hạn.
