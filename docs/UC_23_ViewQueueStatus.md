# UC_23_ViewQueueStatus: Xem trạng thái hàng đợi

## 1. Thông tin chung
- **Mã Use Case**: UC_23
- **Tên Use Case**: Xem trạng thái hàng đợi
- **Tác nhân**: Patient
- **Tiền điều kiện**: Bệnh nhân phải đang tồn tại trong một hàng Queue hợp lệ chưa bị đuổi.

## 2. Luồng sự kiện chính
1. Bệnh nhân mở App hoặc quan sát màn hình TV Dashboard tại sảnh phòng chờ.
2. Hệ thống fetch vị trí (Current Position) của bệnh nhân và đếm ngược vị trí người phía trước.
3. Hệ thống ước lượng dữ liệu (Estimated waiting time) phân bổ trên tổng thời lượng bình quân dịch vụ.
4. Client-side liên tục fetch realtime (Socket) để cập nhật màu sắc tín hiệu khi bị thay đổi ngót người.
5. Khi đến lượt vị trí top list, hệ thống gửi Notification Call rung lên smartphone nhắc khám bệnh.

## 3. Các trường hợp ngoại lệ
- **Mất socket server**: Giao diện báo lỗi mất data realtime, hiển thị màn hình nạp offline.

## 4. Hậu điều kiện
- Tiến trình chờ được tối ưu hóa sự mệt mỏi, bệnh nhân đến đúng số phòng theo phân cấp đúng hạn.
