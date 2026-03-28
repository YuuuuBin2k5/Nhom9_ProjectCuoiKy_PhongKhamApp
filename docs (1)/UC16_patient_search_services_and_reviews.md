# UC16: Use Case Bệnh nhân Tra cứu dịch vụ & Đánh giá (Search Services & Reviews)

## 1. Thông tin chung
- **Mã Use Case**: UC16
- **Tên Use Case**: Tra cứu danh mục, dịch vụ và đánh giá
- **Tác nhân**: Bệnh nhân
- **Tiền điều kiện**: 
  - Bệnh nhân đã đăng nhập vào ứng dụng (tùy chọn cho việc tra cứu, nhưng bắt buộc để xem đánh giá chi tiết hoặc đặt lịch).

## 2. Luồng sự kiện chính
1. Bệnh nhân mở ứng dụng và truy cập màn hình **Tra cứu dịch vụ**.
2. Hệ thống mặc định hiển thị **Danh sách dịch vụ theo danh mục** (ví dụ: Nha khoa tổng quát, Thẩm mỹ).
3. Bệnh nhân có thể thực hiện **Tìm kiếm dịch vụ** bằng cách nhập từ khóa vào ô tìm kiếm (tùy chọn).
4. Bệnh nhân có thể chọn một dịch vụ bất kỳ để **Xem chi tiết dịch vụ** (tùy chọn):
   - Hệ thống hiển thị thông tin chi tiết: mô tả, đơn giá, thời gian.
   - Hệ thống tự động hiển thị **Xem đánh giá**: điểm số sao trung bình và các nhận xét từ những người dùng khác đã trải nghiệm dịch vụ.
5. Bệnh nhân tham khảo thông tin để quyết định sử dụng dịch vụ hoặc đặt lịch (UC12).

## 3. Các trường hợp ngoại lệ
- **Disconnected**: Lỗi kết nối mạng, không thể tải danh sách dịch vụ.
- **Empty List**: Danh mục hiện tại chưa có dịch vụ nào hoặc dịch vụ chưa có đánh giá nào.

## 4. Hậu điều kiện
- Bệnh nhân có đầy đủ thông tin về dịch vụ và mức độ hài lòng của cộng đồng để đưa ra quyết định khám chữa bệnh.
