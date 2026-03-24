# Refactored Check-in Flow - Implementation Complete

## Thay đổi: Từ "Patient tạo QR" → "Patient quét QR từ lễ tân"

### LUỒNG MỚI

1. **Lễ tân tạo QR/mã số** cho appointment của patient
2. **Patient đến phòng khám** → Mở app → Tab "Check-in"
3. **Patient nhấn "Quét mã QR"** hoặc "Nhập mã số"
4. **Quét QR hoặc nhập mã** từ lễ tân
5. **Backend xác thực** → Tạo queue → Trả số thứ tự
6. **Patient nhận thông báo** check-in thành công + số thứ tự

---

## FILES CHANGED

### BACKEND

#### 1. New DTOs
- `GenerateCheckInQRRequest.java` - Request để tạo QR (appointmentId/patientId/phone)
- `GenerateCheckInQRResponse.java` - Response chứa QR data + display code

#### 2. Controllers
- `CheckInController.java`:
  - **REMOVED**: `GET /api/checkin/qr-token` (patient tạo JWT token)
  - **ADDED**: `POST /api/checkin/self-scan` (patient tự check-in bằng QR)
  
- `ReceptionController.java`:
  - **ADDED**: `POST /api/reception/generate-checkin-qr` (lễ tân tạo QR)

#### 3. Services
- `CheckInQueueService.java`:
  - **ADDED**: `processSelfScan()` - Xử lý patient tự quét QR
  - **ADDED**: `generateCheckInQR()` - Tạo QR cho lễ tân

#### 4. Security
- `SecurityConfig.java`:
  - **ADDED**: `.requestMatchers("/api/checkin/self-scan").hasRole("PATIENT")`

---

### ANDROID PATIENT APP

#### 1. New Activity
- `PatientQRScannerActivity.java` - Màn hình quét QR cho patient
  - Quét QR bằng camera (ZXing)
  - Nhập mã số thủ công (dialog)
  - Gọi API `/api/checkin/self-scan`
  - Hiển thị kết quả check-in

#### 2. Modified Fragment
- `QrCheckInFragment.java`:
  - **REMOVED**: Logic tạo QR code (QrCodeGenerator, QR token API)
  - **REMOVED**: Hiển thị QR code của patient
  - **REMOVED**: Download QR button
  - **ADDED**: Button "Quét mã QR" → Mở PatientQRScannerActivity
  - **KEPT**: Hiển thị trạng thái queue sau check-in

#### 3. Layouts
- `activity_patient_qr_scanner.xml` - Layout màn hình quét QR
  - DecoratedBarcodeView (ZXing scanner)
  - Status card (icon, message, buttons)
  - Manual input button
  
- `fragment_qr_checkin.xml`:
  - **REMOVED**: ImageView QR code
  - **REMOVED**: Download button
  - **ADDED**: Large scan button với icon camera
  - **KEPT**: Queue status card

#### 4. New Icons
- `ic_edit.xml` - Icon nhập mã thủ công
- `ic_processing.xml` - Icon đang xử lý
- `ic_error_circle.xml` - Icon lỗi

#### 5. API Service
- `ApiService.java`:
  - **ADDED**: `selfCheckIn()` - POST /api/checkin/self-scan

#### 6. Manifest
- `AndroidManifest.xml`:
  - **ADDED**: PatientQRScannerActivity

---

### ANDROID STAFF APP (TODO - Chưa implement)

Cần thêm màn hình cho lễ tán tạo QR:

#### 1. New Activity (TODO)
- `ReceptionQRGeneratorActivity.java`
  - Tìm patient (phone/email/ID)
  - Hiển thị appointments hôm nay
  - Tạo QR cho appointment
  - Hiển thị QR + mã số lớn
  - Option in QR

#### 2. Add to AdminMainActivity (TODO)
- Button "Tạo QR Check-in"
- Mở ReceptionQRGeneratorActivity

---

## API ENDPOINTS

### Patient APIs
```
POST /api/checkin/self-scan
Authorization: Bearer <patient_token>
Body: { "qrData": "CHECKIN:123" or "123" }
Response: {
  "success": true,
  "queueNumber": 5,
  "roomName": "Phòng 1",
  "roomLocation": "Tầng 1",
  "message": "Check-in thành công",
  "alreadyCheckedIn": false
}
```

### Staff APIs
```
POST /api/reception/generate-checkin-qr
Authorization: Bearer <staff_token>
Body: {
  "appointmentId": 123,
  // OR "patientId": 456,
  // OR "patientPhone": "0901234567"
}
Response: {
  "qrData": "CHECKIN:123",
  "displayCode": "123",
  "patientName": "Nguyễn Văn A",
  "appointmentId": 123,
  "expiresAt": "2026-03-25"
}
```

---

## QR DATA FORMAT

**Format**: `CHECKIN:<appointmentId>`

**Examples**:
- QR: `CHECKIN:123` → appointmentId = 123
- Manual input: `123` → appointmentId = 123

**Validation**:
- Backend kiểm tra appointment thuộc về patient đang đăng nhập
- Appointment phải là hôm nay
- Patient phải active
- Chưa check-in trước đó

---

## TESTING CHECKLIST

### Backend
- [x] Build thành công
- [ ] Test API `/api/checkin/self-scan` với patient token
- [ ] Test API `/api/reception/generate-checkin-qr` với admin token
- [ ] Test validation: appointment không thuộc patient
- [ ] Test validation: appointment không phải hôm nay
- [ ] Test check-in duplicate

### Android Patient
- [ ] Build app thành công
- [ ] Mở tab "Check-in" → Hiển thị button quét
- [ ] Nhấn "Quét mã QR" → Mở camera
- [ ] Quét QR thành công → Hiển thị số thứ tự
- [ ] Nhấn "Nhập mã số" → Dialog input
- [ ] Nhập mã số → Check-in thành công
- [ ] Test lỗi: mã không hợp lệ
- [ ] Test lỗi: mã không thuộc về mình
- [ ] Sau check-in → Hiển thị queue status

### Android Staff (TODO)
- [ ] Tạo màn hình tạo QR
- [ ] Tìm patient theo phone
- [ ] Hiển thị appointments
- [ ] Tạo QR thành công
- [ ] Hiển thị QR + mã số lớn

---

## NEXT STEPS

1. **Restart backend** để apply changes
2. **Build Android app** và test trên thiết bị
3. **Implement Staff QR Generator** (ReceptionQRGeneratorActivity)
4. **Test end-to-end flow**:
   - Staff tạo QR
   - Patient quét QR
   - Check-in thành công
5. **Optional**: Thêm tính năng in QR cho lễ tân

---

## ROLLBACK (Nếu cần)

Nếu muốn quay lại luồng cũ:
1. Restore `CheckInController.java` - thêm lại `GET /api/checkin/qr-token`
2. Restore `QrCheckInFragment.java` - thêm lại logic tạo QR
3. Restore `fragment_qr_checkin.xml` - thêm lại ImageView QR

---

## NOTES

- Luồng cũ (patient tạo QR) đã bị XÓA hoàn toàn
- Patient KHÔNG THỂ tự tạo QR nữa
- Patient CHỈ CÓ THỂ quét QR hoặc nhập mã từ lễ tân
- Staff app cần implement màn hình tạo QR (chưa làm)
- QR format đơn giản: `CHECKIN:<appointmentId>`
- Mã số thủ công: chỉ cần nhập appointmentId (VD: 123)
