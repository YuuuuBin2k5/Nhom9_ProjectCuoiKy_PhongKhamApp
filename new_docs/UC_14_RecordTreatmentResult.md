# UC_14_RecordTreatmentResult: Ghi nhận kết quả khám

## 1. Thông tin chung
- **Mã Use Case**: UC_UC_17
- **Tên Use Case**: Ghi nhận kết quả khám
- **Tác nhân**: Doctor
- **Tiền điều kiện**: Trạng thái phiên khám đang ở mode "IN-PROGRESS".

## 2. Luồng sự kiện chính
1. Sau khi trực tiếp thao tác y tế trên ghế nha khoa hoàn toàn xong xuôi.
2. Bác sĩ thao tác trên màn hình phần mềm, điền TextBox nội dung kết luận (Chẩn đoán lâm sàng).
3. Bác sĩ upload hình ảnh y khoa trực quan (Ảnh Film X-Quang, Ảnh cận cảnh Before/After miệng).
4. Nếu lượt khám này là một phần của Phác đồ lớn, hệ thống sẽ kích hoạt ngầm `<<include>> UC_15_MovePatientToNextStep`.
5. Bác sĩ chốt danh sách thuốc thang, ấn nút Submit.
6. Hệ thống đẩy gói dữ liệu ghi log vào "Medical Records" và trigger phát sinh mã hóa đơn để bệnh nhân thanh toán.

## 3. Các trường hợp ngoại lệ
- **Thiếu Data Ràng Buộc**: Nếu hệ thống set rule bắt buộc Update ảnh mà bác sĩ quên, pop-up sẽ báo Error Validation.

## 4. Hậu điều kiện
- Quá trình tư vấn/chữa trị cho phiên này chấm dứt, giải phóng Doctor để tiếp tục với người khác ngóng chờ ngoài Queue.
