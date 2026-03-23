# Thông tin đăng nhập hệ thống

## Tài khoản mặc định

Tất cả tài khoản đều sử dụng mật khẩu chung: **123456**

### 👨‍💼 Admin
- **Email:** `admin@gmail.com`
- **Password:** `123456`
- **Role:** ADMIN
- **Quyền:** Quản lý toàn bộ hệ thống

### 👨‍⚕️ Doctor (Bác sĩ)
- **Email:** `doctor@gmail.com`
- **Password:** `123456`
- **Role:** DOCTOR
- **Quyền:** Quản lý bệnh nhân, kê đơn thuốc, xem lịch khám

### 👤 Patient (Bệnh nhân)
- **Email:** `patient@gmail.com`
- **Password:** `123456`
- **Role:** PATIENT
- **Quyền:** Đặt lịch khám, xem thông tin cá nhân, thanh toán

## Cách sử dụng

### Trên Mobile App:
1. Mở ứng dụng
2. Chọn "Đăng nhập"
3. Nhập email và mật khẩu tương ứng
4. App sẽ tự động điều hướng theo role:
   - Admin → AdminMainActivity
   - Doctor → DoctorMainActivity (nếu có)
   - Patient → MainActivity

### Test API với curl:
```bash
# Test admin login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "admin@gmail.com", "password": "123456"}'

# Test doctor login  
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "doctor@gmail.com", "password": "123456"}'

# Test patient login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "patient@gmail.com", "password": "123456"}'
```

## Lưu ý bảo mật

⚠️ **Quan trọng:** Đây là tài khoản demo/development. Trong môi trường production:
- Thay đổi tất cả mật khẩu mặc định
- Sử dụng mật khẩu mạnh và phức tạp
- Kích hoạt xác thực 2 yếu tố (2FA) nếu có
- Thường xuyên thay đổi mật khẩu

## Seed Data

Các tài khoản này được tạo tự động khi khởi động ứng dụng:
- `AdminSeedRunner.java` - Tạo tài khoản admin
- `SeedDataLoader.java` - Tạo tài khoản doctor và patient

Mật khẩu được mã hóa bằng BCrypt trước khi lưu vào database.