# UC_24_ProcessQueue: Xử lý hàng đợi

## 1. Thông tin chung
- **Mã Use Case**: UC_UC_19
- **Tên Use Case**: Xử lý hàng đợi
- **Tác nhân**: Doctor
- **Tiền điều kiện**: Bác sĩ online tại phòng trực ban, đã đăng nhập.

## 2. Luồng sự kiện chính
1. Bác sĩ mở widget/view Quản lý Hàng Đợi (Queue Dashboard).
2. Hệ thống tải thời gian thực (WebSockets) danh sách các bệnh nhân đang xếp theo chuẩn Priority ngầm.
3. Bác sĩ bấm duyệt chọn người tại Index 0.
4. Hệ thống trigger `<<include>> UC_11_StartConsultation` để mời người này vào.
5. Khi quy trình gặp/chẩn án kết thúc, bác sĩ điều chỉnh trạng thái ca khám, trigger `<<include>> UC_25_UpdateQueueStatus` để giải phóng vị trí.
6. Hệ thống reset vòng lặp với bệnh nhân Index 1.

## 3. Các trường hợp ngoại lệ
- **Hàng waitlist trống**: Hệ thống im lặng, màn hình standby chờ bệnh nhân Check-in mới từ quầy front-desk.

## 4. Hậu điều kiện
- Chu trình nhận bệnh nhân, khám và thả bệnh nhân trơn tru vòng khép kín. Giải tỏa ùn tắc tại sảnh chờ.
