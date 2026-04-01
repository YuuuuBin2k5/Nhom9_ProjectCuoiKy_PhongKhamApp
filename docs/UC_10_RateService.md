# UC_10_RateService: Đánh giá dịch vụ

## 1. Thông tin chung
- **Mã Use Case**: UC_10
- **Tên Use Case**: Đánh giá dịch vụ
- **Tác nhân**: Patient
- **Tiền điều kiện**: Bệnh nhân đã hoàn thành xong dịch vụ hoặc được popup extend từ hóa đơn thanh toán (UC_08).

## 2. Luồng sự kiện chính
1. Bệnh nhân chủ động chui vào "Lịch sử dịch vụ/Ratings" hoặc được gợi ý nhảy thẳng từ UC_08 pop-up.
2. Bệnh nhân ấn chọn vào dịch vụ nha khoa muốn feedback thực tế.
3. Box feedback mở, bệnh nhân chấm điểm hệ sao đồ thị (1-5 point scale).
4. Bệnh nhân gõ comments feedback chi tiết về chuyên môn cũng như trải nghiệm CSKH.
5. Bệnh nhân trigger gửi mạng API đánh giá (Submit).
6. Hệ thống insert vào cơ sở dữ liệu table Ratings ánh xạ ID.
7. Hệ thống tự động kích hoạt tính toán lại rating trung bình cho dịch vụ đó để render Public.
8. Trả Notification Cảm ơn ghi nhận về client.

## 3. Các trường hợp ngoại lệ
- **Re-rate block**: Nghiệp vụ khóa không cho người dùng đánh giá lặp lại 2 lần vào 1 mã lịch sử khám.

## 4. Hậu điều kiện
- Feedback sẽ hiển thị trực quan đẩy mạnh tính Social Proof public cho người dùng khác tham khảo.
