# UC18: Use Case Bác sĩ Quản lý hàng đợi (Queue Management)

## 1. Thông tin chung
- **Mã Use Case**: UC18
- **Tên Use Case**: Quản lý hàng đợi
- **Tác nhân**: Bác sĩ (Doctor)
- **Tiền điều kiện**: 
  - Bác sĩ đã đăng nhập vào hệ thống mobile hoặc web.
  - Bác sĩ được chỉ định vào một phòng khám (Clinic Room).

## 2. Luồng sự kiện chính
1. Bác sĩ mở ứng dụng và truy cập màn hình **Quản lý hàng đợi**.
2. Hệ thống hiển thị danh sách bệnh nhân đang chờ tại phòng khám được chỉ định.
3. Bác sĩ thực hiện hành động **Gọi bệnh nhân** và chọn bệnh nhân để bắt đầu xử lý (UC18).
4. Hệ thống yêu cầu và thực hiện **Khai thác bệnh án** (bao gồm Quét mã QR/Nhập mã) để xác định bệnh nhân (UC19 - Included).
5. Bác sĩ tiến hành thăm khám và thực hiện **Ghi nhận kết quả & chẩn đoán** (UC21 - Included).
6. Sau khi ghi nhận kết quả, hệ thống thực hiện **Chuyển sang bước điều trị tiếp theo** (Included):
   - Giải phóng bệnh nhân khỏi hàng đợi hiện tại.
   - (Tùy chọn) Tự động đẩy hồ sơ sang phòng khám tiếp theo theo phác đồ.

## 3. Các trường hợp ngoại lệ
- **Room Not Assigned**: Bác sĩ chưa được gán vào phòng nào, không thể thấy hàng đợi.
- **Empty Queue**: Không có bệnh nhân nào đang chờ.

## 4. Hậu điều kiện
- Bệnh nhân được điều phối luân chuyển hợp lý giữa các phòng chức năng.
- Trạng thái hàng đợi được cập nhật thời gian thực cho cả bác sĩ và bệnh nhân.
