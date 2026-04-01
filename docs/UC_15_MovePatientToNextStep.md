# UC_15_MovePatientToNextStep: Chuyển sang bước tiếp theo

## 1. Thông tin chung
- **Mã Use Case**: UC_UC_18
- **Tên Use Case**: Chuyển sang bước tiếp theo
- **Tác nhân**: Doctor
- **Tiền điều kiện**: Hệ thống được trigger tự động/thủ công từ bước UC_UC_17 khi Record một Treatment có sở hữu Plan.

## 2. Luồng sự kiện chính
1. Khi bác sĩ tích Checkmark hoàn thành một "Step" hiện tại trong Object `Treatment Plan`.
2. Hệ thống query DB và tự động trỏ tham chiếu đến `index` của Step tiếp theo.
3. Update trạng thái Step cũ thành `COMPLETED`, Step mới chuẩn bị được nhổ neo thành `PENDING` (chờ bệnh nhân book lịch hẹn mới).
4. Thanh Progress tracking của bệnh nhân bên Mobile App lập tức trượt phần trăm.
5. Nếu Step vừa làm xong là Node lá (cuối cùng), hệ thống đánh dấu toàn chu trình là `FINISHED`.

## 3. Các trường hợp ngoại lệ
- **Hủy (Abort) nhánh**: Bệnh nhân bỏ ngang, bác sĩ thao tác chuyển step thành `CANCELLED` thay vì `COMPLETED`.

## 4. Hậu điều kiện
- Lưu trữ trơn tru luồng điều trị kỹ thuật số, tránh bác sĩ bị mất bối cảnh lịch sử.
