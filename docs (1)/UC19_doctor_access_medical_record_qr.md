# UC19: Use Case Bác sĩ Khai thác bệnh án qua mã QR (Medical Record Access via QR)

## 1. Thông tin chung
- **Mã Use Case**: UC19
- **Tên Use Case**: Khai thác bệnh án qua mã QR
- **Tác nhân**: Bác sĩ (Doctor)
- **Tiền điều kiện**: 
  - Bác sĩ đã đăng nhập vào hệ thống.
  - Bệnh nhân có mã QR (trên ứng dụng di động hoặc thẻ in).

## 2. Luồng sự kiện chính
1. Bác sĩ mở tính năng **Quét mã QR** trên ứng dụng di động.
2. Bác sĩ hướng camera về phía mã QR của bệnh nhân.
3. Hệ thống nhận diện mã QR (JWT hoặc ID) và tự động truy vấn thông tin:
   - Thông tin cá nhân bệnh nhân.
   - Trạng thái lịch hẹn hôm nay (Service, Status, Queue ID).
4. Hệ thống hiển thị tóm tắt thông tin bệnh nhân trên màn hình làm việc (Doctor Workflow).
5. Bác sĩ chọn **Xem lịch sử hồ sơ**:
   - Hệ thống hiển thị danh sách các lần khám trước đó dưới dạng Bottom Sheet hoặc màn hình riêng.
6. Bác sĩ xem các thông tin quan trọng như: Nhóm máu, Dị ứng, Bệnh lý nền và các đơn thuốc cũ để hỗ trợ quá trình chẩn đoán hiện tại.

## 3. Các trường hợp ngoại lệ
- **Invalid QR**: Mã QR không hợp lệ hoặc đã hết hạn (nếu là JWT).
- **Patient Not Found**: Không tìm thấy thông tin bệnh nhân tương ứng với mã QR.
- **Network Error**: Lỗi kết nối khiến không thể tải hồ sơ lịch sử.

## 4. Hậu điều kiện
- Bác sĩ có đầy đủ thông tin về tình trạng và lịch sử sức khỏe của bệnh nhân để đưa ra chẩn đoán chính xác và an toàn.
