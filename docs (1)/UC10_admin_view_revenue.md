# UC10: Use Case Quản trị viên xem báo cáo doanh thu (View Revenue Report)

## 1. Thông tin chung
- **Mã Use Case**: UC10
- **Tên Use Case**: Xem báo cáo doanh thu theo thời gian
- **Tác nhân**: Quản trị viên (Admin)
- **Tiền điều kiện**: 
  - Admin đã đăng nhập và có quyền **ROLE_ADMIN**.

## 2. Luồng sự kiện chính
1. Admin truy cập giao diện Báo cáo & Thống kê.
2. Admin chọn chức năng **Xem báo cáo doanh thu**.
3. Admin thực hiện **Chọn thời gian (Tháng/Năm)** muốn xem.
4. Hệ thống nhận yêu cầu (`GET /api/admin/reports/revenue?year=...&month=...`).
5. Hệ thống thực hiện các bước:
   - **Truy vấn dữ liệu cuộc hẹn đã hoàn thành**: Lọc các `Appointment` có trạng thái `COMPLETED` trong khoảng thời gian đã chọn.
   - **Tính toán tổng doanh thu**: Cộng dồn giá tiền (`price`) của các dịch vụ trong các cuộc hẹn đó.
6. Hệ thống hiển thị kết quả bao gồm:
   - Tổng doanh thu (VNĐ).
   - Tổng số lượt khám đã hoàn thành.

## 3. Các trường hợp ngoại lệ
- **403 Forbidden**: Người dùng không có quyền quản trị.
- **400 Bad Request**: Định dạng năm/tháng không hợp lệ.

## 4. Hậu điều kiện
- Admin nắm bắt được tình hình tài chính của phòng khám theo giai đoạn.
