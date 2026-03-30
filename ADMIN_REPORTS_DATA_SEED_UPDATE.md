# Cập nhật Data Seed cho Báo cáo Admin

## Thay đổi đã thực hiện

Đã cập nhật file `DataSeed.java` để tự động tạo dữ liệu appointments COMPLETED và reviews khi khởi động backend.

### Dữ liệu được thêm

#### 1. Appointments COMPLETED (18 appointments)
- **Tháng hiện tại**: 8 appointments
  - Khám và tư vấn: 2 appointments
  - Trám răng: 2 appointments  
  - Lấy cao răng: 2 appointments
  - Nhổ răng khôn: 1 appointment
  - Tẩy trắng răng: 1 appointment
  - Niềng răng: 1 appointment

- **Tháng trước**: 6 appointments
  - Khám và tư vấn: 1 appointment
  - Trám răng: 2 appointments
  - Lấy cao răng: 1 appointment
  - Nhổ răng khôn: 1 appointment
  - Tẩy trắng răng: 1 appointment

- **2 tháng trước**: 4 appointments
  - Khám và tư vấn: 1 appointment
  - Trám răng: 1 appointment
  - Lấy cao răng: 1 appointment
  - Niềng răng: 1 appointment

#### 2. Reviews (10 reviews)
- Rating từ 4-5 sao
- Comments chi tiết về dịch vụ
- Liên kết với patients, doctors, và services

### Phân bố doanh thu dự kiến

Dựa trên giá dịch vụ:
- Khám và tư vấn (100,000 VNĐ): 4 appointments = 400,000 VNĐ
- Trám răng (300,000 VNĐ): 5 appointments = 1,500,000 VNĐ
- Lấy cao răng (250,000 VNĐ): 4 appointments = 1,000,000 VNĐ
- Nhổ răng khôn (2,000,000 VNĐ): 2 appointments = 4,000,000 VNĐ
- Tẩy trắng răng (2,500,000 VNĐ): 2 appointments = 5,000,000 VNĐ
- Niềng răng (30,000,000 VNĐ): 2 appointments = 60,000,000 VNĐ

**Tổng doanh thu**: ~71,900,000 VNĐ

## Cách áp dụng

### Bước 1: Restart Backend
```bash
# Stop backend nếu đang chạy
# Ctrl+C hoặc kill process

# Start lại backend
cd clinic_backend
./mvnw spring-boot:run
```

Backend sẽ tự động:
1. Xóa toàn bộ dữ liệu cũ (TRUNCATE)
2. Tạo lại dữ liệu mới với appointments COMPLETED
3. Tạo reviews cho các appointments

### Bước 2: Verify dữ liệu
```bash
# Test Revenue Report
curl -X GET "http://192.168.1.2:8081/api/admin/reports/revenue?startDate=2026-01-01&endDate=2026-12-31" \
  -H "Authorization: Bearer YOUR_TOKEN"

# Kết quả mong đợi:
# - totalAppointments: 28 (10 SCHEDULED + 18 COMPLETED)
# - completedAppointments: 18
# - totalRevenue: ~71,900,000
```

### Bước 3: Test trên Mobile App
1. Build và cài APK mới (đã fix AdminDashboardActivity)
2. Login với tài khoản admin@gmail.com / 123456
3. Nhấp "Báo cáo và Phân tích"
4. Kiểm tra:
   - ✅ Hiển thị doanh thu > 0
   - ✅ Top Services có danh sách
   - ✅ Doctor Performance có revenue > 0
   - ✅ Biểu đồ hiển thị dữ liệu

## Files đã thay đổi

1. `clinic_backend/src/main/java/com/hcmute/clinic/config/DataSeed.java`
   - Thêm ReviewRepository dependency
   - Thêm method `seedCompletedAppointments()`
   - Thêm method `addCompletedAppointment()`
   - Thêm method `addReview()`
   - Gọi `seedCompletedAppointments()` trong `seed()`

## Lợi ích

✅ Không cần chạy script SQL riêng
✅ Dữ liệu test tự động tạo mỗi lần restart backend
✅ Dữ liệu nhất quán và có thể reproduce
✅ Dễ dàng thêm/sửa dữ liệu test trong tương lai
✅ Phân bố dữ liệu hợp lý qua 3 tháng

## Ghi chú

- DataSeed chạy mỗi khi backend khởi động
- Tất cả dữ liệu cũ sẽ bị xóa (TRUNCATE)
- Appointments COMPLETED được phân bố từ 5-72 ngày trước
- Reviews được tạo với rating 4-5 sao
- Dữ liệu đủ để test tất cả báo cáo: Revenue, Top Services, Doctor Performance
