# 🚀 PHASE 2 - BẮT ĐẦU

**Ngày bắt đầu**: 28/03/2026  
**Trạng thái Phase 1**: ✅ HOÀN THÀNH 100%  
**Mục tiêu Phase 2**: Bổ sung 7 tính năng thiếu

---

## 📊 TỔNG QUAN PHASE 2

### Features cần làm:
1. ⏳ UC15: Thanh toán & Đánh giá (3 ngày)
2. ⏳ UC10: Admin Báo cáo Doanh thu (2 ngày)
3. ⏳ UC12: Hoàn thiện UI Đặt lịch (3 ngày)
4. ⏳ Appointment Cancel/Reschedule (2 ngày)
5. ⏳ Receptionist Role (3 ngày)
6. ⏳ Lưu ảnh X-Quang vào DB (1 ngày)
7. ⏳ Notification History (1 ngày)

**Tổng thời gian**: 15 ngày (3 tuần)

---

## 🎯 FEATURE 1: UC15 - THANH TOÁN & ĐÁNH GIÁ

**Priority**: 🔴 CRITICAL  
**Thời gian**: 3 ngày  
**Trạng thái**: ⏳ ĐANG BẮT ĐẦU

### Mục tiêu:
- Bệnh nhân có thể xem hóa đơn
- Bệnh nhân có thể thanh toán
- Bệnh nhân có thể đánh giá dịch vụ/bác sĩ

### Backend Tasks:

#### 1.1. Entity Review (Mới)
```java
@Entity
@Table(name = "reviews")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;
    
    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;
    
    @ManyToOne
    @JoinColumn(name = "service_id")
    private Service service;
    
    @ManyToOne
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;
    
    @Column(nullable = false)
    private Integer rating; // 1-5 stars
    
    @Column(columnDefinition = "TEXT")
    private String comment;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
```

#### 1.2. Update Invoice Entity
```java
// Thêm vào Invoice.java
@Enumerated(EnumType.STRING)
@Column(name = "payment_status")
private PaymentStatus paymentStatus; // PENDING, PAID, CANCELLED

@Enumerated(EnumType.STRING)
@Column(name = "payment_method")
private PaymentMethod paymentMethod; // CASH, BANK_TRANSFER, CREDIT_CARD

@Column(name = "paid_at")
private LocalDateTime paidAt;
```

#### 1.3. InvoiceController (Mới)
```java
@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {
    
    private final InvoiceService invoiceService;
    
    @GetMapping("/my")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<InvoiceDto>> getMyInvoices(Authentication auth) {
        Long patientId = Long.parseLong(auth.getName());
        return ResponseEntity.ok(invoiceService.getPatientInvoices(patientId));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<InvoiceDto> getInvoiceDetail(@PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.getInvoiceDetail(id));
    }
    
    @PostMapping("/{id}/pay")
    @PreAuthorize("hasRole('PATIENT') or hasRole('RECEPTIONIST')")
    public ResponseEntity<PaymentResponse> processPayment(
        @PathVariable Long id,
        @RequestBody PaymentRequest request,
        Authentication auth
    ) {
        return ResponseEntity.ok(invoiceService.processPayment(id, request, auth));
    }
}
```

#### 1.4. ReviewController (Mới)
```java
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {
    
    private final ReviewService reviewService;
    
    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ReviewDto> createReview(
        @RequestBody ReviewRequest request,
        Authentication auth
    ) {
        Long patientId = Long.parseLong(auth.getName());
        return ResponseEntity.ok(reviewService.createReview(request, patientId));
    }
    
    @GetMapping("/service/{serviceId}")
    public ResponseEntity<List<ReviewDto>> getServiceReviews(@PathVariable Long serviceId) {
        return ResponseEntity.ok(reviewService.getServiceReviews(serviceId));
    }
    
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<ReviewDto>> getDoctorReviews(@PathVariable Long doctorId) {
        return ResponseEntity.ok(reviewService.getDoctorReviews(doctorId));
    }
    
    @GetMapping("/my")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<ReviewDto>> getMyReviews(Authentication auth) {
        Long patientId = Long.parseLong(auth.getName());
        return ResponseEntity.ok(reviewService.getPatientReviews(patientId));
    }
}
```

### Mobile Tasks:

#### 2.1. InvoiceListActivity
```kotlin
// Hiển thị danh sách hóa đơn
// Filter: Tất cả / Chưa thanh toán / Đã thanh toán
// Click vào item -> InvoiceDetailActivity
```

#### 2.2. InvoiceDetailActivity
```kotlin
// Hiển thị chi tiết hóa đơn
// Danh sách dịch vụ đã sử dụng
// Tổng tiền
// Trạng thái thanh toán
// Button "Thanh toán" nếu chưa thanh toán
```

#### 2.3. PaymentActivity
```kotlin
// Chọn phương thức thanh toán:
// - Tiền mặt
// - Chuyển khoản ngân hàng
// - Thẻ tín dụng (future)
// Xác nhận thanh toán
```

#### 2.4. ReviewActivity
```kotlin
// Rating bar (1-5 sao)
// EditText nhận xét
// Chọn đánh giá: Dịch vụ / Bác sĩ / Cả hai
// Button Submit
```

---

## 📝 IMPLEMENTATION PLAN

### Day 1: Backend - Entities & Repositories
- [ ] Create Review entity
- [ ] Create ReviewRepository
- [ ] Update Invoice entity (payment fields)
- [ ] Create InvoiceRepository queries
- [ ] Database migration

### Day 2: Backend - Services & Controllers
- [ ] Create ReviewService
- [ ] Create InvoiceService
- [ ] Create ReviewController
- [ ] Create InvoiceController
- [ ] Test APIs

### Day 3: Mobile - UI & Integration
- [ ] Create InvoiceListActivity
- [ ] Create InvoiceDetailActivity
- [ ] Create PaymentActivity
- [ ] Create ReviewActivity
- [ ] API integration
- [ ] End-to-end testing

---

## 🧪 TEST CASES

### Test 1: Get My Invoices
```bash
GET /api/invoices/my
Authorization: Bearer <patient_token>

Expected: List of invoices for patient
```

### Test 2: Process Payment
```bash
POST /api/invoices/1/pay
Authorization: Bearer <patient_token>
Body: {
    "paymentMethod": "CASH",
    "amount": 500000
}

Expected: Payment successful, invoice status = PAID
```

### Test 3: Create Review
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

Expected: Review created successfully
```

---

## 📊 PROGRESS TRACKING

### Backend
- [ ] Review entity
- [ ] Invoice updates
- [ ] ReviewRepository
- [ ] InvoiceRepository
- [ ] ReviewService
- [ ] InvoiceService
- [ ] ReviewController
- [ ] InvoiceController
- [ ] API testing

### Mobile
- [ ] InvoiceListActivity
- [ ] InvoiceDetailActivity
- [ ] PaymentActivity
- [ ] ReviewActivity
- [ ] API models
- [ ] API service methods
- [ ] Integration testing

### Documentation
- [ ] API documentation
- [ ] Test cases
- [ ] User guide

---

## 🚀 NEXT STEPS

1. **Bắt đầu với Backend**:
   - Tạo Review entity
   - Update Invoice entity
   - Tạo repositories

2. **Sau đó Mobile**:
   - Tạo UI screens
   - Integrate APIs

3. **Testing**:
   - Unit tests
   - Integration tests
   - End-to-end tests

---

**Created**: 28/03/2026  
**Status**: ⏳ READY TO START  
**Next**: Create Review entity
