# UC_20_ViewRevenueReports: Xem báo cáo doanh thu

## 1. Thông tin chung
- **Mã Use Case**: UC_UC_27
- **Tên Use Case**: Xem báo cáo doanh thu
- **Tác nhân**: Administrator (Admin)
- **Tiền điều kiện**: Admin đã xác thực trên hệ thống.

## 2. Luồng sự kiện chính
1. Admin truy cập mục Báo cáo Doanh thu (Revenue Reports) trên Dashboard.
2. Admin chọn cấu hình thời gian lọc: theo ngày (Day), tháng (Month), hoặc kiểm tra biểu đồ năm (Year).
3. Hệ thống tổng hợp các dữ liệu hóa đơn (Invoices) đã thanh toán (Paid) nằm trong khoảng thời gian được truy vấn.
4. Hệ thống phân tích, tính toán tổng doanh thu, và thống kê các biểu đồ tài chính phụ (số lượng bệnh nhân, loại dịch vụ chuộng, v.v).
5. Giao diện tự động vẽ lại biểu đồ dạng bảng hoặc đồ thị trực quan cho Admin.

## 3. Các trường hợp ngoại lệ
- **Không có dữ liệu**: Khoảng thời gian yêu cầu không có hóa đơn nào, hệ thống thông báo "No data available".

## 4. Hậu điều kiện
- Báo cáo tài chính, biểu đồ phân tích doanh thu được truy xuất và cung cấp đầy đủ thông tin hỗ trợ chiến lược.
