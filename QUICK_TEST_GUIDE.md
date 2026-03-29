# HƯỚNG DẪN TEST NHANH

**Date**: 2026-03-28

---

## Bước 1: Khởi động Backend

```bash
cd clinic_backend
mvn spring-boot:run
```

Đợi đến khi thấy: `Started ClinicApplication`

Backend sẽ chạy ở: `http://localhost:8081`

---

## Bước 2: Build và Install Mobile App

```bash
cd mobile_android
./gradlew assembleDebug
```

Sau khi build xong:

```bash
# Copy APK ra root
cp app/build/outputs/apk/debug/app-debug.apk ../app-debug-new.apk

# Install vào device/emulator
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## Bước 3: Test Backend APIs (Automated)

Khi backend đã chạy:

```bash
chmod +x test_new_fixes.sh
./test_new_fixes.sh
```

**Kết quả mong đợi**: Tất cả tests PASS

---

## Bước 4: Test Mobile App (Manual)

### Test 1: Password Validation ✅

1. Mở app → Register
2. Thử password: `abcdefgh` (không có số)
   - **Mong đợi**: Lỗi "Mật khẩu phải bao gồm cả chữ và số"
3. Thử password: `12345678` (không có chữ)
   - **Mong đợi**: Lỗi "Mật khẩu phải bao gồm cả chữ và số"
4. Thử password: `password123` (hợp lệ)
   - **Mong đợi**: Đăng ký thành công

---

### Test 2: Phone Validation ✅

1. Login: `patient01@gmail.com` / `password123`
2. Vào Profile → Edit phone
3. Thử: `123` (không hợp lệ)
   - **Mong đợi**: Lỗi "Số điện thoại không hợp lệ"
4. Thử: `0987654321` (hợp lệ)
   - **Mong đợi**: Cập nhật thành công

---

### Test 3: DOB Validation ✅

1. Login: `patient01@gmail.com` / `password123`
2. Vào Profile → Edit DOB
3. Chọn ngày trong tương lai (VD: 2030-01-01)
   - **Mong đợi**: Lỗi "Ngày sinh không thể là ngày trong tương lai"
4. Chọn ngày hợp lệ (VD: 1990-01-01)
   - **Mong đợi**: Cập nhật thành công

---

### Test 4: Appointment Date Validation ✅

1. Login: `patient01@gmail.com` / `password123`
2. Book Appointment → Chọn ngày trong quá khứ
   - **Mong đợi**: Lỗi "Không thể đặt lịch trong quá khứ"
3. Chọn ngày trong tương lai
   - **Mong đợi**: Đặt lịch thành công

---

### Test 5: QR Scanner for Doctor ✅

1. Login: `doc01@gmail.com` / `password123`
2. Vào DoctorWorkflowActivity
3. Click nút QR scanner
   - **Mong đợi**: Camera mở
4. Scan patient QR code
   - **Mong đợi**: Patient ID tự động điền vào input
   - **Mong đợi**: Patient info tự động load

---

### Test 6: Patient History ✅

1. Login: `doc01@gmail.com` / `password123`
2. Look up patient (VD: patient:1)
3. Click "Xem Lịch sử Khám bệnh"
   - **Mong đợi**: Bottom sheet hiển thị
   - **Mong đợi**: Danh sách lịch sử khám (nếu có)
   - **Mong đợi**: Hiển thị: ngày, bác sĩ, chẩn đoán, dịch vụ, thanh toán

---

### Test 7: Priority Indicator ✅

1. Login as staff
2. Xem queue management
3. Kiểm tra patient có priority > 5
   - **Mong đợi**: Có icon ⭐
   - **Mong đợi**: Hiển thị thời gian chờ (~X phút)
4. Kiểm tra màu nền theo status:
   - WAITING: Xanh lá nhạt
   - IN_PROGRESS: Xanh dương nhạt
   - RETURNED_PRIORITY: Cam nhạt
   - PAUSED_FOR_TEST: Tím nhạt

---

### Test 8: Room Transfer Notification ✅

1. Login as doctor
2. Complete một treatment step cần chuyển phòng
3. Kiểm tra notification
   - **Mong đợi**: Có 📍 tên phòng và vị trí
   - **Mong đợi**: Có 🔹 tên dịch vụ tiếp theo
   - **Mong đợi**: Có 🎫 số thứ tự
   - **Mong đợi**: Có ⏱️ thời gian chờ dự kiến

---

### Test 9: Payment Workflow ✅

1. Login: `patient01@gmail.com` / `password123`
2. Xem invoice
3. Click "Thanh toán"
4. Chọn payment method (CASH, BANK_TRANSFER, etc.)
5. Confirm payment
   - **Mong đợi**: Success message
   - **Mong đợi**: Invoice status = PAID
6. Thử pay lại
   - **Mong đợi**: Lỗi "Invoice already paid"

---

## Bước 5: Test Backend API Trực Tiếp

### Test Medical Records API

```bash
# 1. Login as doctor
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "doc01@gmail.com",
    "password": "password123"
  }'

# Copy token từ response

# 2. Get medical records
curl -X GET http://localhost:8081/api/doctor/patients/1/medical-records \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

**Mong đợi**: 
- Status 200
- JSON array với medical records
- Mỗi record có: appointmentId, date, doctorName, diagnosis, services, totalAmount, paymentStatus

---

## Checklist Tổng Hợp

### Backend
- [ ] Backend khởi động thành công
- [ ] Automated tests pass (test_new_fixes.sh)
- [ ] Medical records API hoạt động

### Mobile - Critical Validations
- [ ] Password validation (chữ + số)
- [ ] Phone validation (format VN)
- [ ] DOB validation (không future)
- [ ] Appointment date validation (không past)

### Mobile - High Priority Features
- [ ] QR Scanner auto-load patient
- [ ] Patient History bottom sheet
- [ ] Priority indicator (⭐ + màu)
- [ ] Room transfer notification (chi tiết)
- [ ] Payment workflow (đầy đủ)

### Regression
- [ ] Login vẫn hoạt động
- [ ] Appointment booking vẫn hoạt động
- [ ] Check-in vẫn hoạt động
- [ ] Treatment workflow vẫn hoạt động

---

## Nếu Có Lỗi

### Backend không start
```bash
# Check port 8081
netstat -ano | findstr :8081

# Kill process nếu cần
taskkill /PID <PID> /F

# Check database connection
# Xem application.properties
```

### Mobile build fail
```bash
# Clean build
./gradlew clean

# Rebuild
./gradlew assembleDebug
```

### API test fail
- Kiểm tra backend đã chạy chưa
- Kiểm tra port 8081
- Kiểm tra database có data test chưa

---

## Kết Quả Mong Đợi

✅ **Backend**: All tests pass
✅ **Mobile**: All manual tests pass
✅ **Regression**: No breaking changes

**Nếu tất cả pass** → Sẵn sàng deploy! 🎉

---

## Liên Hệ

Nếu có vấn đề, check:
1. `MANUAL_TEST_PLAN.md` - Chi tiết test cases
2. `FINAL_STATUS_ALL_FIXES.md` - Tổng quan fixes
3. `ALL_HIGH_PRIORITY_FIXES_COMPLETE.md` - Documentation

**Good luck testing!** 🚀
