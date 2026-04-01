# UC_07_TrackTreatmentPlan: Theo dõi liệu trình

## 1. Thông tin chung
- **Mã Use Case**: UC_07
- **Tên Use Case**: Theo dõi liệu trình
- **Tác nhân**: Patient
- **Tiền điều kiện**: Bác sĩ (Doctor) đã chẩn án lưu log và kích hoạt phác đồ cho hồ sơ.

## 2. Luồng sự kiện chính
1. Bệnh nhân truy cập mục "Phác đồ điều trị" (My Treatment Plans) trên Menu chính.
2. Hệ thống select ra tree danh sách các liệu trình chạy dài hạn (vd: Niềng răng/Bọc răng sứ).
3. Bệnh nhân nhấn mở chi tiết cụ thể để đối chiếu timeline:
   - Quá khứ: X-ray/Gắn khay.
   - Hôm nay: Xử lý thay dây thun bảo trì.
   - Tương lai: Tháo niềng.
4. Bệnh nhân tiến hành xem hình ảnh (Media/Attachments) liên kết đính kèm các chốt bảo trì từ Doctor.

## 3. Các trường hợp ngoại lệ
- **Blank data**: Hồ sơ chưa từng được tạo plan, hiển thị empty state.

## 4. Hậu điều kiện
- Bệnh nhân ý thức được tiến độ (progress bar) trong lộ trình chữa răng của bản thân.
