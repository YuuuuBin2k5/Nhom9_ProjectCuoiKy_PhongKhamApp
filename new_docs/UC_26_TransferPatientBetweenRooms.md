# UC_26_TransferPatientBetweenRooms: Chuyển bệnh nhân giữa các phòng

## 1. Thông tin chung
- **Mã Use Case**: UC_UC_21
- **Tên Use Case**: Chuyển bệnh nhân giữa các phòng
- **Tác nhân**: Doctor
- **Tiền điều kiện**: Bác sĩ nhận thấy bệnh nhân cần thực hiện các dịch vụ cận lâm sàng độc lập (Chụp X-Quang, Lấy tủy buồng chuyên dụng...).

## 2. Luồng sự kiện chính
1. Bác sĩ chỉ định ấn nút "Chuyển phòng/Chuyển tuyến" (Transfer Room) cho bệnh nhân hiện tại trên thanh công cụ khám.
2. ComboBox đổ xuống hiển thị danh sách các Phòng Khám/Phòng Chức năng (Rooms) hợp lệ và đang có trạng thái Active.
3. Bác sĩ ấn định phòng đích cần hướng tới.
4. Hệ thống can thiệp và bốc vị trí ID của Bệnh nhân đang truy vấn ra rời khỏi Queue hiện tại và enqueue/Join ngay tập tức vào hàng đợi Queue của Phòng Đích.
5. Phía Patient Mobile App sẽ nhận được Notification điều hướng: "Vui lòng di chuyển sang Phòng chụp X-Quang (Room 03)...".
6. Màn hình bác sĩ/y tá ở phòng mới sẽ thấy thông tin bệnh nhân chèn ngang qua cơ chế (Chuyển ngang - Priority nội bộ cao hơn Walk-in).

## 3. Các trường hợp ngoại lệ
- **Phòng đích đang bảo trì**: Phòng bị set Inactive bởi admin. Hệ thống chặn transfer và báo lỗi đỏ "Room temporarily unavailable".

## 4. Hậu điều kiện
- Bệnh nhân được luân chuyển nội trú cực kỳ tốc độ mà không cần quay trở ra Front-desk lấy phiếu giấy mới.
