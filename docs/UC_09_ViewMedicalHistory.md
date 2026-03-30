# UC_09_ViewMedicalHistory: Xem lịch sử khám

## 1. Thông tin chung
- **Mã Use Case**: UC_09
- **Tên Use Case**: Xem lịch sử khám
- **Tác nhân**: Patient
- **Tiền điều kiện**: Bệnh nhân phải đăng nhập thành công vào portal/app của phòng khám.

## 2. Luồng sự kiện chính
1. Bệnh nhân truy xuất danh mục "Hồ sơ y tế" (Medical History).
2. Hệ thống tải toàn bộ danh sách các record thăm khám trong quá khứ sắp xếp theo trục thời gian tuyến tính.
3. Bệnh nhân bấm vào để khai phá dữ liệu của một lần khám cụ thể.
4. Hệ thống tải lên và kết xuất File chẩn đoán chi tiết (Diagnosis): Biểu hiện lâm sàng, chẩn đoán cuối của bác sĩ.
5. Hệ thống tải lên và gộp Đơn thuốc (Prescriptions) đi kèm với record đó: Nhóm thuốc, liều dùng uống, lưu ý y tế.
6. Bệnh nhân được phép lưu kết quả điện tử này dưới dạng PDF.

## 3. Các trường hợp ngoại lệ
- **Hồ sơ rỗng**: Lịch sử trống do bệnh nhân chưa từng sử dụng dịch vụ tại phòng khám.

## 4. Hậu điều kiện
- Bệnh nhân có đầy đủ hồ sơ mang theo mọi lúc qua nền tảng Cloud thuận tiện cho mọi vấn đề cá nhân.
