# UC_21_ManageInvoices: Quản lý hóa đơn

## 1. Thông tin chung
- **Mã Use Case**: UC_UC_28
- **Tên Use Case**: Quản lý hóa đơn
- **Tác nhân**: Administrator (Admin)
- **Tiền điều kiện**: Admin đã đăng nhập.

## 2. Luồng sự kiện chính
1. Admin truy cập hiển thị Quản lý Hóa đơn (Invoices).
2. Hệ thống gọi API và tải ra toàn bộ hóa đơn từ cơ sở dữ liệu (phân trang).
3. Admin có thể tra cứu hóa đơn qua các thông tin như: Mã bệnh nhân, Tên bệnh nhân hoặc Lọc trạng thái (Pending / Paid).
4. Admin nhấn đúp vào một hóa đơn bất kỳ để xem giao diện chi tiết: Tất cả dịch vụ, thuốc đã kê đơn, giá niêm yết, và chi tiết tổng tiền.
5. (Nếu nghiệp vụ cho phép) Admin có thể cập nhật trạng thái hóa đơn gặp trục trặc bằng tay thành Paid/Cancelled.
6. Hệ thống tiến hành lưu cập nhật trạng thái hóa đơn.

## 3. Các trường hợp ngoại lệ
- **Lỗi truy xuất hóa đơn**: Hóa đơn không tồn tại (xóa logic hoặc định danh sai). Hệ thống hiển báo thông tin lỗi (Not Found).

## 4. Hậu điều kiện
- Tất cả chi tiết của hóa đơn được trả ra đúng chuẩn với dữ liệu khám bệnh hiện tại để đối soát tài chính của phòng khám.
