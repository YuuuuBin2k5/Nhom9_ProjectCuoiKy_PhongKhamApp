# UC_06_CheckIn: Check-in phòng khám

## 1. Thông tin chung
- **Mã Use Case**: UC_06
- **Tên Use Case**: Check-in phòng khám
- **Tác nhân**: Patient
- **Tiền điều kiện**: Bệnh nhân đã di chuyển tới phòng khám thực tế.

## 2. Luồng sự kiện chính
1. Bệnh nhân sử dụng máy quét mã tự động đặt tại phòng khám hoặc sử dụng qua giao diện App Lễ tân.
2. Bệnh nhân tiến hành quét mã QR (QR tự động cấp đi cùng với Booking Code lịch hẹn) hoặc nhập bằng tay Booking Code định danh.
3. Hệ thống trigger truy vấn và tìm kiếm đối chiếu code trong Database.
4. **Trường hợp có lịch hẹn trước**: Đánh dấu tình trạng lịch hẹn đó từ "PENDING" thành "CHECKED-IN".
5. **Trường hợp Walk-in (chưa đặt hẹn rỗng)**: Định tuyến qua tính năng cấp tốc (Walk-in appointment process flow).
6. Hệ thống chốt dữ liệu và tự động đẩy người dùng tham gia vào hàng chờ thông qua `<<include>> UC_22_JoinQueue`.

## 3. Các trường hợp ngoại lệ
- **Mã lỗi định dạng**: Cấu trúc chuỗi QR Code bị sai số hóa hoặc quá hạn, trả về màn báo "Invalid QR/Code".
- **Check-in sai giờ**: Việc quét mã nằm cách xa rất nhiều chuẩn giờ quy định cho prior rule, hệ thống đánh mark ưu tiên giảm thứ hạng.

## 4. Hậu điều kiện
- Bệnh nhân thành công thực hiện check-in định danh vật lý, sẵn sàng chuyển ngay vào vòng Join Queue.
