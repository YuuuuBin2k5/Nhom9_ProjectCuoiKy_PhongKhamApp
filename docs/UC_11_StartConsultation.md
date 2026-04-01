# UC_11_StartConsultation: Bắt đầu khám bệnh

## 1. Thông tin chung
- **Mã Use Case**: UC_UC_14
- **Tên Use Case**: Bắt đầu khám bệnh
- **Tác nhân**: Doctor
- **Tiền điều kiện**: Bệnh nhân tiếp theo nằm vị trí `index 0` của Hàng Đợi (Queue).

## 2. Luồng sự kiện chính
1. Bác sĩ ngồi trong phòng, bấm "Tiếp nhận" hoặc "Gọi bệnh nhân tiếp theo".
2. Hệ thống loa hoặc chuông sẽ báo audio mời Patient di chuyển vào cửa phòng khám tương ứng.
3. Bác sĩ nhấn nút `Bắt đầu khám (Start Consultation)`.
4. Trạng thái khám chuyển từ "WAITING" -> "IN-PROGRESS".
5. Hệ thống nạp ID bệnh nhân và tự động khởi tạo Mẫu Bệnh Án Trống cho Record nội khoa hiện tại.

## 3. Các trường hợp ngoại lệ
- **Bênh nhân không có mặt**: Quá 5 phút, Doctor chọn skip (No-show), hệ thống vứt bệnh nhân xuống vị trí khác (Update Queue Status) -> đẩy lùi.

## 4. Hậu điều kiện
- Chốt giờ bắt đầu tư vấn (Consultation start time metric) và chuyển Doctor vào luồng khám trực tiếp với hồ sơ đang ròng.
