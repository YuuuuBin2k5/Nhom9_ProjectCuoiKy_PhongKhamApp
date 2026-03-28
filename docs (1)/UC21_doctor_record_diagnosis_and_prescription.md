# UC21: Use Case Bác sĩ Ghi nhận kết quả & Chẩn đoán (Diagnosis & Results)

## 1. Thông tin chung
- **Mã Use Case**: UC21
- **Tên Use Case**: Ghi nhận kết quả & Chẩn đoán
- **Tác nhân**: Bác sĩ (Doctor)
- **Tiền điều kiện**: 
  - Bác sĩ đang trong phiên khám với bệnh nhân (sau khi thực hiện UC18 và UC19).

## 2. Luồng sự kiện chính
1. Bác sĩ thực hiện tính năng **Ghi nhận kết quả & chẩn đoán** (UC21).
2. Hệ thống yêu cầu và thực hiện **Nhập triệu chứng & chẩn đoán** (Included).
3. Bác sĩ có thể thực hiện các hành động tùy chọn sau (mở rộng):
   - **Tải lên hình ảnh kết quả**: Đính kèm ảnh chụp thực tế hoặc phim X-quang vào hồ sơ.
   - **Kê đơn thuốc**: Lập đơn thuốc dựa trên chẩn đoán.
4. Hệ thống thực hiện **Hoàn thành bước điều trị** (Included) sau khi bác sĩ xác nhận kết quả:
   - Cập nhật trạng thái Medical Record.
   - Ghi nhận kết luận vào bước hiện tại của phác đồ.

## 3. Các trường hợp ngoại lệ
- **Missing Diagnosis**: Chưa nhập chẩn đoán, hệ thống yêu cầu bổ sung trước khi lưu.
- **Empty Prescription**: Đơn thuốc trống (chấp nhận được nếu chỉ cần ghi nhận chẩn đoán và lời tư vấn).

## 4. Hậu điều kiện
- Hồ sơ bệnh án của bệnh nhân được cập nhật đầy đủ thông tin chuyên môn.
- Đơn thuốc sẵn sàng để bệnh nhân tra cứu trên ứng dụng hoặc in ra tại quầy thuốc.
