# Phân tích luồng Check-in hiện tại và đề xuất thay đổi

## 1. LUỒNG HIỆN TẠI (Patient tạo QR → Staff quét)

### Cách hoạt động:
1. **Patient mở app** → Vào tab "QR Check-in" (QrCheckInFragment)
2. **App gọi API** `GET /api/checkin/qr-token` → Backend tạo JWT token có thời hạn (180s)
3. **App hiển thị QR code** chứa JWT token
4. **Patient đưa QR cho lễ tân** → Staff dùng QRScannerActivity quét
5. **Staff app gọi API** `POST /api/checkin/scan` với JWT token
6. **Backend xử lý**:
   - Parse JWT token → lấy patientId
   - Kiểm tra appointment hôm nay
   - Tạo CheckInQueue record
   - Trả về số thứ tự

### Files liên quan:
**Android (Patient):**
- `QrCheckInFragment.java` - Fragment hiển thị QR trong MainActivity
- `QRCheckInActivity.java` - Activity riêng để hiển thị QR (ít dùng)
- `fragment_qr_checkin.xml` - Layout hiển thị QR code

**Android (Staff):**
- `QRScannerActivity.java` - Activity quét QR của patient
- `activity_qr_scanner.xml` - Layout màn hình quét

**Backend:**
- `CheckInController.java`:
  - `GET /api/checkin/qr-token` - Tạo JWT token cho patient
  - `POST /api/checkin/scan` - Xử lý quét QR
- `CheckInQueueService.java` - Logic xử lý check-in
- `JwtService.java` - Tạo và parse JWT token

### Ưu điểm:
- Patient chủ động, không cần staff tạo QR
- Có thể check-in từ xa (nếu có QR)
- Token có thời hạn, bảo mật tốt

### Nhược điểm:
- Patient phải mở app, đăng nhập, tạo QR
- Phụ thuộc vào điện thoại patient (pin, mạng)
- Token hết hạn nhanh (3 phút)

---

## 2. LUỒNG ĐỀ XUẤT (Lễ tân tạo QR → Patient quét)

### Cách hoạt động mới:
1. **Lễ tân tạo QR code** cho appointment/patient
2. **Lễ tân in/hiển thị QR** trên màn hình/giấy
3. **Patient đến phòng khám** → Quét QR bằng app
4. **App gọi API check-in** với QR data
5. **Backend xử lý** → Tạo queue, trả số thứ tự

### Thay đổi cần thiết:

#### A. BACKEND (Minimal changes)

**1. Tạo API mới cho lễ tân tạo QR:**
```java
// ReceptionController.java
@PostMapping("/reception/generate-qr")
public ResponseEntity<?> generateCheckInQR(@RequestBody GenerateQRRequest request) {
    // request.appointmentId hoặc request.patientId
    // Tạo QR data: "checkin:appointmentId:timestamp" hoặc JWT
    // Trả về QR data + expiry
}
```

**2. Sửa CheckInController để patient có thể tự check-in:**
```java
// CheckInController.java
@PostMapping("/checkin/self-scan")
public ResponseEntity<?> selfCheckIn(Authentication auth, @RequestBody CheckInScanRequest request) {
    // Verify patient đang đăng nhập
    // Parse QR data từ lễ tân
    // Gọi checkInQueueService.processScan()
    // Trả về số thứ tự
}
```

#### B. ANDROID (Patient App)

**1. Thêm màn hình quét QR cho patient:**
- Tạo `PatientQRScannerActivity.java` (copy từ staff QRScannerActivity)
- Thay đổi:
  - Gọi API `/api/checkin/self-scan` thay vì `/api/checkin/scan`
  - UI khác: "Quét mã QR từ lễ tân"
  - Hiển thị kết quả: số thứ tự, phòng khám

**2. Sửa QrCheckInFragment:**
- Thay button "Tạo QR" → "Quét QR từ lễ tân"
- onClick → Mở PatientQRScannerActivity
- Vẫn giữ phần hiển thị trạng thái queue

**3. Thêm API method:**
```java
// ApiService.java
@POST("api/checkin/self-scan")
Call<CheckInResult> selfCheckIn(@Body CheckInScanRequest request);
```

#### C. ANDROID (Staff/Reception App)

**1. Tạo màn hình tạo QR cho lễ tân:**
- `ReceptionQRGeneratorActivity.java`
- Input: Tìm patient (theo phone/email/ID)
- Hiển thị danh sách appointments hôm nay
- Tạo QR cho appointment được chọn
- Có thể in hoặc hiển thị QR lớn

**2. Thêm vào AdminMainActivity:**
- Button "Tạo QR Check-in"
- Mở ReceptionQRGeneratorActivity

---

## 3. SO SÁNH HAI LUỒNG

| Tiêu chí | Luồng hiện tại | Luồng đề xuất |
|----------|----------------|---------------|
| **Ai tạo QR** | Patient | Lễ tân |
| **Ai quét QR** | Staff | Patient |
| **Phụ thuộc** | Patient phải có app + mạng | Patient chỉ cần camera |
| **Tốc độ** | Chậm (patient mở app) | Nhanh (QR sẵn) |
| **Bảo mật** | JWT có thời hạn | QR có thể dùng nhiều lần |
| **Trải nghiệm** | Patient chủ động | Lễ tân kiểm soát |
| **Offline** | Không (cần mạng tạo QR) | Có (QR in sẵn) |

---

## 4. KHUYẾN NGHỊ

### Option 1: Thay thế hoàn toàn
- Xóa luồng cũ (patient tạo QR)
- Chỉ giữ luồng mới (patient quét QR)
- **Ưu điểm**: Đơn giản, dễ maintain
- **Nhược điểm**: Mất tính năng check-in từ xa

### Option 2: Hỗ trợ cả hai luồng ⭐ (RECOMMENDED)
- Giữ luồng cũ cho patient tự check-in
- Thêm luồng mới cho lễ tân tạo QR
- Patient chọn: "Tạo QR của tôi" hoặc "Quét QR từ lễ tân"
- **Ưu điểm**: Linh hoạt, phù hợp nhiều tình huống
- **Nhược điểm**: Phức tạp hơn

### Option 3: Hybrid
- Lễ tân tạo QR tĩnh cho mỗi patient (in ra thẻ)
- Patient dùng QR tĩnh này mỗi lần đến
- Backend check appointment hôm nay khi quét
- **Ưu điểm**: Patient không cần mở app
- **Nhược điểm**: QR tĩnh kém bảo mật

---

## 5. IMPLEMENTATION PLAN (Option 2)

### Phase 1: Backend API
1. Tạo `GenerateQRRequest.java` DTO
2. Thêm endpoint `/api/reception/generate-qr` trong ReceptionController
3. Thêm endpoint `/api/checkin/self-scan` trong CheckInController
4. Test API với Postman

### Phase 2: Android Patient App
1. Tạo `PatientQRScannerActivity.java`
2. Tạo layout `activity_patient_qr_scanner.xml`
3. Sửa `QrCheckInFragment.java` thêm button "Quét QR"
4. Thêm API method trong `ApiService.java`
5. Test flow

### Phase 3: Android Staff/Reception App
1. Tạo `ReceptionQRGeneratorActivity.java`
2. Tạo layout `activity_reception_qr_generator.xml`
3. Thêm button trong `AdminMainActivity.java`
4. Test flow

### Phase 4: Testing & Refinement
1. Test cả hai luồng
2. Xử lý edge cases
3. Cải thiện UX

---

## 6. CÂU HỎI CẦN TRẢ LỜI

1. **Bạn muốn option nào?** (1, 2, hay 3)
2. **QR từ lễ tân có thời hạn không?** (Nên có để bảo mật)
3. **Có in QR ra giấy không?** (Cần thêm print function)
4. **Patient không có app thì sao?** (Cần web check-in?)
5. **Có cần lưu lịch sử QR đã tạo không?**

---

## 7. ESTIMATE

- **Backend**: 4-6 giờ
- **Android Patient**: 6-8 giờ
- **Android Staff**: 4-6 giờ
- **Testing**: 4 giờ
- **Total**: 18-24 giờ (2-3 ngày)
Tôi 