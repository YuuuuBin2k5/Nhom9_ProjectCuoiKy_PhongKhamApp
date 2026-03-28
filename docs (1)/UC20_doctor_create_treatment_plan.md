# UC20: Use Case Bác sĩ Lập phác đồ điều trị (Treatment Plan Creation)

## 1. Thông tin chung
- **Mã Use Case**: UC20
- **Tên Use Case**: Lập phác đồ điều trị
- **Tác nhân**: Bác sĩ (Doctor)
- **Tiền điều kiện**: 
  - Bác sĩ đã xác định được tình trạng và nhu cầu điều trị lâu dài của bệnh nhân (thông qua UC19 hoặc UC21).

## 2. Luồng sự kiện chính
1. Bác sĩ truy cập tính năng **Lập phác đồ điều trị** từ hồ sơ bệnh nhân (UC20).
2. Bác sĩ có thể thực hiện các hành động tùy chọn sau (mở rộng):
   - **Xem mẫu phác đồ**: Duyệt qua danh sách các phác đồ chuẩn của phòng khám.
   - **Tạo từ mẫu**: Khởi tạo nhanh phác đồ dựa trên mẫu đã chọn.
   - **Tạo phác đồ mới**: Tự xây dựng phác đồ thủ công không theo mẫu.
   - **Tùy chỉnh bước điều trị**: Thêm, sửa, xóa hoặc thay đổi thứ tự các bước trong phác đồ hiện tại.
3. Bác sĩ có thể chọn **Kích hoạt phác đồ** (tùy chọn mở rộng) để bắt đầu lộ trình điều trị chính thức.
4. Hệ thống lưu trữ phác đồ và đồng bộ tới ứng dụng của bệnh nhân.

## 3. Các trường hợp ngoại lệ
- **No Templates**: Hệ thống chưa có mẫu phác đồ nào, bác sĩ phải tự thêm từng bước thủ công.
- **Incomplete Plan**: Phác đồ chưa có bước nào, không thể kích hoạt.

## 4. Hậu điều kiện
- Một lộ trình điều trị khoa học được thiết lập, giúp phòng khám quản lý quy trình chuyên môn và bệnh nhân nắm rõ kế hoạch điều trị.
