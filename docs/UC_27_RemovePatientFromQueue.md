# UC_27_RemovePatientFromQueue: Loại bệnh nhân khỏi hàng đợi

## 1. Thông tin chung
- **Mã Use Case**: UC_UC_29
- **Tên Use Case**: Loại bệnh nhân khỏi hàng đợi
- **Tác nhân**: Admin (hoặc Quản lý lễ tân có quyền điều phối)
- **Tiền điều kiện**: Một bệnh nhân dính sự cố không thể/không muốn tiếp tục đứng trong hàng chờ khám (Queue).

## 2. Luồng sự kiện chính
1. Admin truy xuất vào bảng Điều phối Hàng đợi Tổng (Master Queue Manager).
2. Tìm bệnh nhân thông qua tên hoặc số thứ tự.
3. Nhấn Force Remove (Hủy bỏ/Đuổi phiên khám) với lý do bất khả kháng (Ví dụ: khách hàng quậy phá, hoặc cấp cứu khẩn cấp sang viện khác).
4. Hệ thống đá bệnh nhân ra khỏi danh sách list chờ Array hiện hành tại room.
5. Server gọi Broadcast tính toán lại Real-time cho mọi người phía sau tiến lên 1 bậc.

## 3. Các trường hợp ngoại lệ
- **Không tìm thấy id**: Bệnh nhân vốn dĩ đã khám xong (COMPLETED) trước đó vài giây.

## 4. Hậu điều kiện
- Cấu trúc phòng khám vẫn vận hành ổn định mà không bị tắc nghẽn vô hình do "bệnh nhân bóng ma".
