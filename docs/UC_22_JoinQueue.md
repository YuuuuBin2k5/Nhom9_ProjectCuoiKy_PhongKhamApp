# UC_22_JoinQueue: Tham gia hàng đợi

## 1. Thông tin chung
- **Mã Use Case**: UC_22
- **Tên Use Case**: Tham gia hàng đợi
- **Tác nhân**: Patient
- **Tiền điều kiện**: Bệnh nhân đã hoàn tất thủ tục Check-in (tiền đề từ UC_06).

## 2. Luồng sự kiện chính
1. Hệ thống tự động bắt tín hiệu đẩy hồ sơ khám bệnh/mã đặt chỗ của bệnh nhân vào danh sách Queue của phòng khám hoặc bác sĩ chuyên khoa tương ứng.
2. Logic phân loại thứ tự ưu tiên (Priority Rules) được hệ thống tự động chạy ngầm:
   - Đặt lịch tới đúng giờ: Mức độ ưu tiên cao nhất.
   - Trễ giờ dưới 15 phút: Độ ưu tiên cấp trung bình.
   - Walk-in / Trễ vượt 30 phút: Ưu tiên thấp nhất.
3. Hệ thống trả về cấu trúc vị trí hiện tại của Object Patient.
4. Hệ thống trigger ngầm `<<include>> UC_23_ViewQueueStatus` tự động để báo trạng thái/thời gian thực.
5. Patient có thể quan sát thứ tự chờ thông qua app.

## 3. Các trường hợp ngoại lệ
- **Hàng đợi vượt tải**: Hệ thống tự động xếp vào queue dự phòng (overflow) hoặc yêu cầu nhân sự hỗ trợ tay.

## 4. Hậu điều kiện
- Bệnh nhân sẵn sàng chờ tới báo thức gọi tên và chuyển ngay vào luồng gặp bác sĩ (Start Consultation).
