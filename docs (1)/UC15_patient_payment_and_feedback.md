# UC15: Use Case Bệnh nhân Thanh toán & Đánh giá (Payment & Feedback)

## 1. Thông tin chung
- **Mã Use Case**: UC15
- **Tên Use Case**: Thanh toán & Đánh giá
- **Tác nhân**: Bệnh nhân
- **Tiền điều kiện**: 
  - Bệnh nhân đã hoàn thành tất cả các bước trong lộ trình điều trị (UC14).
  - Hệ thống đã tổng hợp chi phí và tạo hóa đơn (Invoice).

## 2. Luồng sự kiện chính
1. Sau khi bước điều trị cuối cùng được xác nhận hoàn thiện, Bệnh nhân truy cập mục **Thanh toán** trên ứng dụng.
2. Bệnh nhân thực hiện **Xem chi tiết hóa đơn**:
   - Hệ thống hiển thị: Danh sách dịch vụ, đơn giá, tổng tiền, thuế và các khoản giảm giá (nếu có).
3. Bệnh nhân thực hiện **Xác nhận thanh toán**:
   - Bệnh nhân kiểm tra lại thông tin và nhấn xác nhận đã thanh toán (có thể là xác nhận sau khi trả tiền mặt tại quầy hoặc thực hiện qua cổng thanh toán).
4. Sau khi việc thanh toán được hệ thống ghi nhận thành công, Bệnh nhân có thể **Gửi đánh giá dịch vụ** (tùy chọn):
   - Chọn số sao đánh giá và nhập nội dung nhận xét.
5. Hệ thống ghi nhận đánh giá và hoàn tất quy trình điều trị của Bệnh nhân.

## 3. Các trường hợp ngoại lệ
- **401 Unauthorized**: Token không hợp lệ hoặc hết hạn.
- **Invoice Not Ready**: Hóa đơn chưa được tạo do lộ trình chưa hoàn tất.
- **Payment Failed**: Lỗi trong quá trình thanh toán trực tuyến.

## 4. Hậu điều kiện
- Hóa đơn được chuyển sang trạng thái đã thanh toán.
- Đánh giá của bệnh nhân được ghi nhận vào hệ thống để cải thiện chất lượng dịch vụ.
- Bệnh nhân hoàn tất quy trình khám chữa bệnh tại phòng khám.
