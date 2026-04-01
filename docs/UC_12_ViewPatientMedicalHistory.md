# UC_12_ViewPatientMedicalHistory: Xem bệnh án bệnh nhân

## 1. Thông tin chung
- **Mã Use Case**: UC_UC_15
- **Tên Use Case**: Xem bệnh án bệnh nhân
- **Tác nhân**: Doctor
- **Tiền điều kiện**: Bác sĩ đang xem hồ sơ in-progress của một bệnh nhân được chỉ định.

## 2. Luồng sự kiện chính
1. Tại giao diện nhập liệu y khoa (Consultation Room Tab), Bác sĩ bấm vào nút "Xem Hồ sơ KCB" (Medical History).
2. Hệ thống mở cửa sổ Modal liệt kê phân trang mọi lịch sử đã từng thăm khám từ hệ thống trước đó.
3. Bác sĩ kiểm tra Ghi chú y khoa (Medical notes): "Chống chỉ định thuốc tê", "Máu khó đông", "Dị ứng Penicillin".
4. Bác sĩ tick mở xem ảnh đính kèm (Media attachments x-rays) từ những bác sĩ cũ để tham chiếu cấu trúc nướu/răng.

## 3. Các trường hợp ngoại lệ
- **New Patient**: Kết quả query báo "0 matches", màn hình hiển thị empty state "Đây là bệnh nhân mới, chưa có tiền sử điều trị được ghi nhận".

## 4. Hậu điều kiện
- Tránh những xung đột rủi ro y tế cực lớn trước khi qua bước Phác đồ điều trị.
