# UC_08_MakePayment: Thanh toán

## 1. Thông tin chung
- **Mã Use Case**: UC_08
- **Tên Use Case**: Thanh toán
- **Tác nhân**: Patient
- **Tiền điều kiện**: Bệnh nhân đã hoàn tất dịch vụ khám chữa bệnh và được lập một hóa đơn chốt ở trạng thái `PENDING`.

## 2. Luồng sự kiện chính
1. Bệnh nhân điều hướng đến tab "Thanh toán/Hóa đơn" trên ứng dụng.
2. Hệ thống tải lên danh sách các hóa đơn chưa hoàn thành thanh toán định hướng cho hồ sơ đăng nhập.
3. Bệnh nhân chọn một hóa đơn cần thanh toán, giao diện sẽ pop-up chi tiết: Tên Dịch vụ, Giá thành phần, Phí kê đơn thuốc, Tổng thanh toán.
4. Bệnh nhân chọn "Thanh toán" và chọn phương thức khả dụng (VNPay, Momo, Banking, hoặc Quầy lễ tân).
5. Hệ thống thực thi gọi API cổng thanh toán của bên thứ ba (Gateway).
6. Khi Gateway trả về kết quả callback giao dịch thành công.
7. Hệ thống lập tức cập nhật trạng thái hóa đơn thành `PAID`.
8. Hệ thống trigger form gợi ý bệnh nhân thực hiện đánh giá dịch vụ (`<<extend>> UC_10_RateService`).

## 3. Các trường hợp ngoại lệ
- **Đóng giao dịch / Số dư không đủ**: Giao dịch bị hủy từ hệ thống thứ ba, Invoice giữ nguyên trạng thái cũ `PENDING`.

## 4. Hậu điều kiện
- Doanh thu được ghi nhận cập nhật, hóa đơn hoàn tất chu trình.
