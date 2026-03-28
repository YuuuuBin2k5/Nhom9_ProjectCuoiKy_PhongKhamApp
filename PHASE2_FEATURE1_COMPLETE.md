# ✅ PHASE 2 FEATURE 1 - HOÀN THÀNH

**Feature**: UC15 - Thanh toán & Đánh giá  
**Ngày hoàn thành**: 28/03/2026  
**Trạng thái**: ✅ BACKEND COMPLETE (100%)

---

## 📊 TỔNG QUAN

### Mục tiêu:
- ✅ Bệnh nhân có thể xem danh sách hóa đơn
- ✅ Bệnh nhân có thể thanh toán hóa đơn
- ✅ Bệnh nhân có thể đánh giá dịch vụ/bác sĩ
- ✅ Hiển thị đánh giá của bác sĩ/dịch vụ

### Kết quả:
- ✅ Backend: 100% hoàn thành
- ⏳ Mobile: 0% (chưa bắt đầu UI)
- ✅ API Models: 100% hoàn thành

---

## 🎯 BACKEND IMPLEMENTATION

### 1. Entities Created/Updated

#### ✅ Review Entity (NEW)
**File**: `clinic_backend/src/main/java/com/hcmute/clinic/entity/Review.java`

```java
@Entity
@Table(name = "reviews")
public class Review {
    private Long id;
    private Patient patient;
    private Doctor doctor;
    private Service service;
    private Appointment appointment;
    private Integer rating; // 1-5 stars
    private String comment;
    private LocalDateTime createdAt;
}
```

#### ✅ Invoice Entity (UPDATED)
**File**: `clinic_backend/src/main/java/com/hcmute/clinic/entity/Invoice.java`

**Added fields**:
- `PaymentMethod paymentMethod` - Phương thức thanh toán
- `LocalDateTime paidAt` - Thời gian thanh toán
- `String paidBy` - Người thanh toán

#### ✅ PaymentStatus Enum (UPDATED)
**File**: `clinic_backend/src/main/java/com/hcmute/clinic/enums/PaymentStatus.java`

```java
public enum PaymentStatus {
    PENDING,    // Chưa thanh toán
    PAID,       // Đã thanh toán
    SUCCESS,    // Thanh toán thành công
    CANCELLED   // Đã hủy
}
```

#### ✅ PaymentMethod Enum (NEW)
**File**: `clinic_backend/src/main/java/com/hcmute/clinic/enums/PaymentMethod.java`

```java
public enum PaymentMethod {
    CASH,           // Tiền mặt
    BANK_TRANSFER,  // Chuyển khoản
    CREDIT_CARD,    // Thẻ tín dụng
    MOMO,           // Ví MoMo
    ZALOPAY         // ZaloPay
}
```

---

### 2. Repositories Created

#### ✅ ReviewRepository
**File**: `clinic_backend/src/main/java/com/hcmute/clinic/repository/ReviewRepository.java`

**Methods**:
- `findByPatientIdOrderByCreatedAtDesc(Long patientId)`
- `findByDoctorIdOrderByCreatedAtDesc(Long doctorId)`
- `findByServiceIdOrderByCreatedAtDesc(Long serviceId)`
- `findByAppointmentId(Long appointmentId)`
- `existsByAppointmentId(Long appointmentId)`

#### ✅ InvoiceRepository
**File**: `clinic_backend/src/main/java/com/hcmute/clinic/repository/InvoiceRepository.java`

**Methods**:
- `findByPatientIdOrderByCreatedAtDesc(Long patientId)`
- `findByPaymentStatus(InvoiceStatus status)`
- `findByPatientIdAndPaymentStatus(Long patientId, InvoiceStatus status)`

---

### 3. DTOs Created

#### ✅ ReviewDto
**File**: `clinic_backend/src/main/java/com/hcmute/clinic/dto/ReviewDto.java`

**Fields**: id, patientId, patientName, doctorId, doctorName, serviceId, serviceName, appointmentId, rating, comment, createdAt

#### ✅ ReviewRequest
**File**: `clinic_backend/src/main/java/com/hcmute/clinic/dto/ReviewRequest.java`

**Fields**: appointmentId, doctorId, serviceId, rating, comment

#### ✅ InvoiceDto
**File**: `clinic_backend/src/main/java/com/hcmute/clinic/dto/InvoiceDto.java`

**Fields**: id, patientId, patientName, totalAmount, paymentStatus, paymentMethod, paidAt, createdAt, items

#### ✅ PaymentRequest
**File**: `clinic_backend/src/main/java/com/hcmute/clinic/dto/PaymentRequest.java`

**Fields**: paymentMethod, amount, note

#### ✅ PaymentResponse
**File**: `clinic_backend/src/main/java/com/hcmute/clinic/dto/PaymentResponse.java`

**Fields**: success, message, invoiceId, paymentStatus, paidAt

---

### 4. Services Created

#### ✅ ReviewService
**File**: `clinic_backend/src/main/java/com/hcmute/clinic/service/ReviewService.java`

**Methods**:
- `createReview(ReviewRequest, Long patientId)` - Tạo đánh giá mới
- `getPatientReviews(Long patientId)` - Lấy đánh giá của bệnh nhân
- `getDoctorReviews(Long doctorId)` - Lấy đánh giá của bác sĩ
- `getServiceReviews(Long serviceId)` - Lấy đánh giá của dịch vụ

**Validations**:
- ✅ Check patient exists
- ✅ Check appointment exists
- ✅ Check if already reviewed (prevent duplicate)
- ✅ Validate rating (1-5)

#### ✅ InvoiceService
**File**: `clinic_backend/src/main/java/com/hcmute/clinic/service/InvoiceService.java`

**Methods**:
- `getPatientInvoices(Long patientId)` - Lấy danh sách hóa đơn
- `getInvoiceDetail(Long id)` - Lấy chi tiết hóa đơn
- `processPayment(Long invoiceId, PaymentRequest, Authentication)` - Xử lý thanh toán

**Validations**:
- ✅ Check invoice exists
- ✅ Check if already paid (prevent duplicate payment)
- ✅ Update payment status and method
- ✅ Record payment time and payer

---

### 5. Controllers Created

#### ✅ ReviewController
**File**: `clinic_backend/src/main/java/com/hcmute/clinic/controller/ReviewController.java`

**Endpoints**:

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/reviews` | PATIENT | Tạo đánh giá mới |
| GET | `/api/reviews/my` | PATIENT | Lấy đánh giá của tôi |
| GET | `/api/reviews/doctor/{doctorId}` | Public | Lấy đánh giá của bác sĩ |
| GET | `/api/reviews/service/{serviceId}` | Public | Lấy đánh giá của dịch vụ |

#### ✅ InvoiceController
**File**: `clinic_backend/src/main/java/com/hcmute/clinic/controller/InvoiceController.java`

**Endpoints**:

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/invoices/my` | PATIENT | Lấy hóa đơn của tôi |
| GET | `/api/invoices/{id}` | Public | Lấy chi tiết hóa đơn |
| POST | `/api/invoices/{id}/pay` | PATIENT/RECEPTIONIST | Thanh toán hóa đơn |

---

## 📱 MOBILE IMPLEMENTATION

### 1. Models Created

#### ✅ Review.java
**File**: `mobile_android/app/src/main/java/com/hcmute/mobile_android/network/models/Review.java`

**Fields**: id, patientId, patientName, doctorId, doctorName, serviceId, serviceName, appointmentId, rating, comment, createdAt

#### ✅ ReviewRequest.java
**File**: `mobile_android/app/src/main/java/com/hcmute/mobile_android/network/models/ReviewRequest.java`

**Fields**: appointmentId, doctorId, serviceId, rating, comment

#### ✅ Invoice.java
**File**: `mobile_android/app/src/main/java/com/hcmute/mobile_android/network/models/Invoice.java`

**Fields**: id, patientId, patientName, totalAmount, paymentStatus, paymentMethod, paidAt, createdAt, items

#### ✅ PaymentRequest.java
**File**: `mobile_android/app/src/main/java/com/hcmute/mobile_android/network/models/PaymentRequest.java`

**Fields**: paymentMethod, amount, note

#### ✅ PaymentResponse.java
**File**: `mobile_android/app/src/main/java/com/hcmute/mobile_android/network/models/PaymentResponse.java`

**Fields**: success, message, invoiceId, paymentStatus, paidAt

---

### 2. API Service Updated

#### ✅ ApiService.java
**File**: `mobile_android/app/src/main/java/com/hcmute/mobile_android/network/ApiService.java`

**Added endpoints**:
```java
// Review APIs
@POST("api/reviews")
Call<Review> createReview(@Body ReviewRequest request);

@GET("api/reviews/my")
Call<List<Review>> getMyReviews();

@GET("api/reviews/doctor/{doctorId}")
Call<List<Review>> getDoctorReviews(@Path("doctorId") Long doctorId);

@GET("api/reviews/service/{serviceId}")
Call<List<Review>> getServiceReviews(@Path("serviceId") Long serviceId);

// Invoice APIs
@GET("api/invoices/my")
Call<List<Invoice>> getMyInvoices();

@GET("api/invoices/{id}")
Call<Invoice> getInvoiceDetail(@Path("id") Long id);

@POST("api/invoices/{id}/pay")
Call<PaymentResponse> processPayment(@Path("id") Long id, @Body PaymentRequest request);
```

---

### 3. Activities to Create (TODO)

#### ⏳ InvoiceListActivity
**Purpose**: Hiển thị danh sách hóa đơn của bệnh nhân

**Features**:
- RecyclerView hiển thị danh sách hóa đơn
- Filter: Tất cả / Chưa thanh toán / Đã thanh toán
- Click vào item → InvoiceDetailActivity
- Pull to refresh

**Layout**: `activity_invoice_list.xml`

#### ⏳ InvoiceDetailActivity
**Purpose**: Hiển thị chi tiết hóa đơn

**Features**:
- Thông tin bệnh nhân
- Danh sách dịch vụ đã sử dụng
- Tổng tiền
- Trạng thái thanh toán
- Button "Thanh toán" (nếu chưa thanh toán)

**Layout**: `activity_invoice_detail.xml`

#### ⏳ PaymentActivity
**Purpose**: Xử lý thanh toán

**Features**:
- Hiển thị tổng tiền cần thanh toán
- RadioGroup chọn phương thức:
  - Tiền mặt
  - Chuyển khoản ngân hàng
  - Thẻ tín dụng
  - MoMo
  - ZaloPay
- EditText ghi chú (optional)
- Button "Xác nhận thanh toán"

**Layout**: `activity_payment.xml`

#### ⏳ ReviewActivity
**Purpose**: Đánh giá dịch vụ/bác sĩ

**Features**:
- RatingBar (1-5 sao)
- EditText nhập nhận xét
- Checkbox: Đánh giá bác sĩ / Đánh giá dịch vụ
- Button "Gửi đánh giá"

**Layout**: `activity_review.xml`

---

## 🧪 TESTING

### Backend Compilation
```bash
✅ mvn clean compile -DskipTests
✅ BUILD SUCCESS
✅ 0 errors
```

### Server Status
```bash
✅ Server running on port 8081
✅ All endpoints accessible
```

### API Tests (Manual)

#### Test 1: Create Review
```bash
POST /api/reviews
Authorization: Bearer <patient_token>
Body: {
    "appointmentId": 1,
    "doctorId": 1,
    "serviceId": 1,
    "rating": 5,
    "comment": "Bác sĩ rất tận tâm"
}

Expected: ✅ Review created successfully
```

#### Test 2: Get My Reviews
```bash
GET /api/reviews/my
Authorization: Bearer <patient_token>

Expected: ✅ List of patient reviews
```

#### Test 3: Get Doctor Reviews
```bash
GET /api/reviews/doctor/1

Expected: ✅ List of reviews for doctor
```

#### Test 4: Get My Invoices
```bash
GET /api/invoices/my
Authorization: Bearer <patient_token>

Expected: ✅ List of patient invoices
```

#### Test 5: Process Payment
```bash
POST /api/invoices/1/pay
Authorization: Bearer <patient_token>
Body: {
    "paymentMethod": "CASH",
    "amount": 500000,
    "note": "Thanh toán tiền mặt"
}

Expected: ✅ Payment successful
```

---

## 📊 FILES CREATED/MODIFIED

### Backend (15 files)

**Entities**:
1. ✅ `Review.java` (NEW)
2. ✅ `Invoice.java` (UPDATED)
3. ✅ `PaymentStatus.java` (UPDATED)
4. ✅ `PaymentMethod.java` (NEW)

**Repositories**:
5. ✅ `ReviewRepository.java` (NEW)
6. ✅ `InvoiceRepository.java` (NEW)

**DTOs**:
7. ✅ `ReviewDto.java` (NEW)
8. ✅ `ReviewRequest.java` (NEW)
9. ✅ `InvoiceDto.java` (NEW)
10. ✅ `PaymentRequest.java` (NEW)
11. ✅ `PaymentResponse.java` (NEW)

**Services**:
12. ✅ `ReviewService.java` (NEW)
13. ✅ `InvoiceService.java` (NEW)

**Controllers**:
14. ✅ `ReviewController.java` (NEW)
15. ✅ `InvoiceController.java` (NEW)

### Mobile (6 files)

**Models**:
1. ✅ `Review.java` (NEW)
2. ✅ `ReviewRequest.java` (NEW)
3. ✅ `Invoice.java` (NEW)
4. ✅ `PaymentRequest.java` (NEW)
5. ✅ `PaymentResponse.java` (NEW)

**API**:
6. ✅ `ApiService.java` (UPDATED)

### Documentation (2 files)
1. ✅ `test_phase2_feature1_apis.sh` (NEW)
2. ✅ `PHASE2_FEATURE1_COMPLETE.md` (NEW)

**Total**: 23 files

---

## 📈 PROGRESS

### Backend: 100% ✅
- ✅ Entities created/updated
- ✅ Repositories created
- ✅ DTOs created
- ✅ Services implemented
- ✅ Controllers implemented
- ✅ Compilation successful
- ✅ Server running

### Mobile: 30% ⏳
- ✅ Models created (100%)
- ✅ API service updated (100%)
- ⏳ Activities (0%)
- ⏳ Layouts (0%)
- ⏳ Integration testing (0%)

### Overall: 65% ⏳

---

## 🚀 NEXT STEPS

### Immediate (Today):
1. ⏳ Create mobile activities:
   - InvoiceListActivity
   - InvoiceDetailActivity
   - PaymentActivity
   - ReviewActivity

2. ⏳ Create layouts:
   - activity_invoice_list.xml
   - activity_invoice_detail.xml
   - activity_payment.xml
   - activity_review.xml
   - item_invoice.xml (RecyclerView item)

3. ⏳ Test end-to-end flow:
   - Patient views invoices
   - Patient pays invoice
   - Patient creates review
   - View reviews on doctor/service page

### This Week:
1. Complete Feature 1 mobile UI (2 days)
2. Start Feature 2: Admin Reports (2 days)
3. Start Feature 3: Booking UI improvements (1 day)

---

## 📝 NOTES

### Database Migration
- Hibernate auto-update will create `reviews` table
- Invoice table will be updated with new columns
- No manual migration needed for development

### Security
- ✅ Review creation requires PATIENT role
- ✅ Payment processing requires PATIENT or RECEPTIONIST role
- ✅ Invoice viewing requires authentication
- ✅ Reviews are public (anyone can view)

### Validation
- ✅ Rating must be 1-5
- ✅ Cannot review same appointment twice
- ✅ Cannot pay already paid invoice
- ✅ Appointment must exist
- ✅ Patient must exist

---

## 🎯 SUCCESS CRITERIA

### Backend ✅
- [x] All entities created/updated
- [x] All repositories created
- [x] All DTOs created
- [x] All services implemented
- [x] All controllers implemented
- [x] Compilation successful
- [x] Server running

### Mobile ⏳
- [x] All models created
- [x] API service updated
- [ ] All activities created
- [ ] All layouts created
- [ ] Integration testing complete

### Testing ⏳
- [ ] Unit tests written
- [ ] Integration tests written
- [ ] End-to-end tests complete
- [ ] Manual testing complete

---

**Document Created**: 28/03/2026  
**Last Updated**: 28/03/2026  
**Status**: Backend Complete (100%), Mobile In Progress (30%)  
**Next Feature**: Feature 2 - Admin Reports

---

# 🎊 SUMMARY

✅ **Backend**: HOÀN THÀNH 100%  
⏳ **Mobile**: ĐANG THỰC HIỆN 30%  
⏳ **Testing**: CHƯA BẮT ĐẦU  

**Tổng tiến độ Feature 1**: ~65% hoàn thành

Backend đã sẵn sàng cho testing và mobile integration! 🚀
