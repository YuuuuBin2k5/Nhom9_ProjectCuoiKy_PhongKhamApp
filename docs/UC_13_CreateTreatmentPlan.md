# UC_13_CreateTreatmentPlan: Tạo phác đồ điều trị

## 1. Thông tin chung
- **Mã Use Case**: UC_UC_16
- **Tên Use Case**: Tạo phác đồ điều trị
- **Tác nhân**: Doctor
- **Tiền điều kiện**: Bác sĩ chuyên khoa đã hoàn tất rà soát thông tin lâm sàng của bệnh nhân.

## 2. Luồng sự kiện chính
1. Bác sĩ click chọn nút "Lập phác đồ điều trị mới" (Create Plan) trong module Khám bệnh.
2. Form dữ liệu mở rộng, bác sĩ gõ định danh Tên Phác đồ (vd: Niềng răng mắc cài).
3. Bác sĩ chia nhỏ tiến trình thành các `Step` (các buổi hẹn khám định kỳ):
   - Đặt tên step (nhổ răng khôn, gắn khí cụ...).
   - Chỉ định các dịch vụ y tế áp dụng cho step đó.
   - Thêm đơn thuốc mặc định.
4. Bác sĩ Review tổng thể các bước đã thêm.
5. Nhấn "Lưu phác đồ" để Commit xuống hệ thống Database.
6. Hệ thống đồng bộ thông tin Phác đồ này hiển thị cho Patient ở UC_07 bên phía Mobile App.

## 3. Các trường hợp ngoại lệ
- **Blank Steps**: Bác sĩ quên Add item bước điều trị, hệ thống cảnh báo "Plan must contain at least 1 step".

## 4. Hậu điều kiện
- Một Timeline dạng Tree được sinh ra để track dài hạn, giúp phân luồng và tối ưu lịch khám tương lai.
