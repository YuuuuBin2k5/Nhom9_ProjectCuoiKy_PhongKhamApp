# PHASE 2: BỔ SUNG CÁC TÍNH NĂNG THIẾU

## 🔴 CRITICAL FEATURES (Phải có ngay)

### 1. UC15: Thanh toán & Đánh giá

**Backend Tasks:**
```java
// 1.1. InvoiceController
@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {
    
    @GetMapping("/my")
    @PreAuthorize("hasRole('PATIENT')")
    public List<InvoiceDto> getMyInvoices(Authentication auth);
    
    @GetMapping("/{id}")
    public InvoiceDto getInvoiceDetail(@PathVariable Long id);
    
    @PostMapping("/{id}/pay")
    @PreAuthorize("hasRole('PATIENT') or hasRole('RECEPTIONIST')")
    public PaymentResponse processPayment(@PathVariable Long id, 
                                         @RequestBody PaymentRequest request);
}

// 1.2. ReviewController
@RestController
@RequestMapping("/api/reviews")
public class ReviewController {
    
    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    public ReviewDto createReview(@RequestBody ReviewRequest request);
    
    @GetMapping("/service/{serviceId}")
    public List<ReviewDto> getServiceReviews(@PathVariable Long serviceId);
    
    @GetMapping("/doctor/{doctorId}")
    public List<ReviewDto> getDoctorReviews(@PathVariable Long doctorId);
}

// 1.3. Entity Review
@Entity
@Table(name = "reviews")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "patient_id")
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
    private LocalDateTime createdAt;
}
```

**Mobile Tasks:**
```kotlin
// PaymentActivity.kt
class PaymentActivity : AppCompatActivity() {
    // Hiển thị chi tiết hóa đơn
    // Chọn phương thức thanh toán (Tiền mặt/Chuyển khoản)
    // Xác nhận thanh toán
}

// ReviewActivity.kt
class ReviewActivity : AppCompatActivity() {
    // Rating bar (1-5 sao)
    // EditText nhập nhận xét
    // Submit review
}
```

**Ước tính:** 3 ngày

---

### 2. UC10: Admin Báo cáo Doanh thu

**Backend Tasks:**
```java
@RestController
@RequestMapping("/api/admin/reports")
@PreAuthorize("hasRole('ADMIN')")
public class AdminReportController {
    
    @GetMapping("/revenue")
    public RevenueReportDto getRevenue(
        @RequestParam int year,
        @RequestParam int month
    ) {
        LocalDateTime startDate = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime endDate = startDate.plusMonths(1);
        
        List<Appointment> completed = appointmentRepository
            .findByStatusAndDatetimeBetween(
                AppointmentStatus.COMPLETED, 
                startDate, 
                endDate
            );
        
        BigDecimal totalRevenue = completed.stream()
            .map(a -> a.getService().getPrice())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return RevenueReportDto.builder()
            .year(year)
            .month(month)
            .totalRevenue(totalRevenue)
            .totalAppointments(completed.size())
            .build();
    }
    
    @GetMapping("/top-services")
    public List<ServiceStatsDto> getTopServices(
        @RequestParam int year,
        @RequestParam int month,
        @RequestParam(defaultValue = "10") int limit
    );
    
    @GetMapping("/doctor-performance")
    public List<DoctorStatsDto> getDoctorPerformance(
        @RequestParam int year,
        @RequestParam int month
    );
}
```

**Mobile Tasks:**
```kotlin
// AdminDashboardFragment.kt
class AdminDashboardFragment : Fragment() {
    // Card: Doanh thu tháng này
    // Card: Số lượt khám
    // Chart: Doanh thu theo tháng (MPAndroidChart)
    // RecyclerView: Top dịch vụ
}
```

**Ước tính:** 2 ngày

---

### 3. UC12: Hoàn thiện UI Đặt lịch

**Backend Tasks:**
```java
@GetMapping("/api/appointments/available-slots")
public List<TimeSlotDto> getAvailableSlots(
    @RequestParam Long doctorId,
    @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate date
) {
    List<TimeSlotDto> slots = new ArrayList<>();
    LocalTime start = LocalTime.of(8, 0);
    LocalTime end = LocalTime.of(16, 40);
    
    while (start.isBefore(end)) {
        LocalDateTime slotTime = LocalDateTime.of(date, start);
        
        // Check if doctor is busy
        boolean isBusy = appointmentRepository
            .existsByDoctorIdAndDatetimeBetween(
                doctorId,
                slotTime.minusMinutes(15),
                slotTime.plusMinutes(45)
            );
        
        slots.add(TimeSlotDto.builder()
            .time(start.toString())
            .available(!isBusy)
            .build());
        
        start = start.plusMinutes(30);
    }
    
    return slots;
}
```

**Mobile Tasks:**
```kotlin
// BookAppointmentActivity.kt - Cải thiện
class BookAppointmentActivity : AppCompatActivity() {
    // Step 1: Chọn danh mục dịch vụ (Spinner)
    // Step 2: Chọn dịch vụ cụ thể (RecyclerView)
    // Step 3: Chọn bác sĩ (Spinner với ảnh + tên)
    // Step 4: Chọn ngày (CalendarView)
    // Step 5: Chọn giờ (RecyclerView slots)
    // Step 6: Xác nhận thông tin
}
```

**Ước tính:** 3 ngày

---

## 🟡 HIGH PRIORITY FEATURES

### 4. Appointment Cancel & Reschedule

**Backend Tasks:**
```java
@PatchMapping("/api/appointments/{id}/cancel")
@PreAuthorize("hasRole('PATIENT') or hasRole('ADMIN')")
public void cancelAppointment(@PathVariable Long id, Authentication auth) {
    Appointment appointment = appointmentRepository.findById(id)
        .orElseThrow();
    
    // Check ownership
    if (auth.getAuthorities().stream()
        .noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
        Long patientId = Long.parseLong(auth.getName());
        if (!appointment.getPatient().getId().equals(patientId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }
    
    // Check if can cancel (at least 2 hours before)
    if (appointment.getAppointmentDatetime()
        .isBefore(LocalDateTime.now().plusHours(2))) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
            "Không thể hủy lịch trong vòng 2 giờ trước giờ hẹn");
    }
    
    appointment.setStatus(AppointmentStatus.CANCELLED);
    appointmentRepository.save(appointment);
    
    // Send notification
    sendNotification(appointment.getPatient(), 
        "Lịch hẹn đã hủy", 
        "Lịch hẹn ngày " + appointment.getAppointmentDatetime() + " đã được hủy");
}

@PutMapping("/api/appointments/{id}/reschedule")
@PreAuthorize("hasRole('PATIENT')")
public void rescheduleAppointment(
    @PathVariable Long id,
    @RequestBody RescheduleRequest request
) {
    // Similar logic to booking
    // Check new slot availability
    // Update datetime
}
```

**Ước tính:** 2 ngày

---

### 5. Receptionist Role & Payment Processing

**Backend Tasks:**
```java
// 5.1. Entity Receptionist
@Entity
@Table(name = "receptionists")
public class Receptionist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private Boolean isActive;
}

// 5.2. ReceptionController
@RestController
@RequestMapping("/api/reception")
@PreAuthorize("hasRole('RECEPTIONIST')")
public class ReceptionController {
    
    @PostMapping("/checkin/scan")
    public CheckInResponse scanPatient(@RequestBody ScanRequest request);
    
    @PostMapping("/payment/process")
    public PaymentResponse processPayment(@RequestBody PaymentRequest request);
    
    @GetMapping("/queue/today")
    public List<QueueItemDto> getTodayQueue();
}

// 5.3. SecurityConfig - Add role
.requestMatchers("/api/reception/**").hasRole("RECEPTIONIST")
```

**Mobile Tasks:**
```kotlin
// ReceptionMainActivity.kt
class ReceptionMainActivity : AppCompatActivity() {
    // Tab 1: Quét QR check-in
    // Tab 2: Danh sách hàng đợi hôm nay
    // Tab 3: Thanh toán
}
```

**Ước tính:** 3 ngày

---

### 6. Lưu ảnh X-Quang vào DB

**Backend Tasks:**
```java
// TreatmentPlanService.completeStepAndAdvance()
// Đã có logic upload, chỉ cần lưu vào DB

// Thêm entity StepImage (đã có)
@Entity
@Table(name = "step_images")
public class StepImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "step_id")
    private TreatmentPlanStep step;
    
    private String imageUrl;
    
    @CreationTimestamp
    private LocalDateTime uploadedAt;
}

// API để lấy ảnh
@GetMapping("/api/treatment-plans/steps/{stepId}/images")
public List<String> getStepImages(@PathVariable Long stepId);
```

**Mobile Tasks:**
```kotlin
// DoctorWorkflowActivity - Cải thiện
// Hiển thị danh sách ảnh đã upload
// Cho phép xem ảnh full screen
// Cho phép xóa ảnh trước khi submit
```

**Ước tính:** 1 ngày

---

### 7. Notification History

**Backend Tasks:**
```java
@GetMapping("/api/notifications/my")
@PreAuthorize("hasRole('PATIENT') or hasRole('DOCTOR')")
public List<NotificationDto> getMyNotifications(Authentication auth) {
    Long userId = Long.parseLong(auth.getName());
    String role = auth.getAuthorities().iterator().next().getAuthority();
    
    if (role.equals("ROLE_PATIENT")) {
        return notificationRepository.findByPatientIdOrderByCreatedAtDesc(userId)
            .stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }
    // Similar for doctor
}

@PatchMapping("/api/notifications/{id}/read")
public void markAsRead(@PathVariable Long id);

@PatchMapping("/api/notifications/read-all")
public void markAllAsRead(Authentication auth);
```

**Mobile Tasks:**
```kotlin
// NotificationsFragment - Cải thiện
class NotificationsFragment : Fragment() {
    // RecyclerView với badge "unread"
    // Swipe to delete
    // Mark all as read button
}
```

**Ước tính:** 1 ngày

---

## 📋 CHECKLIST PHASE 2

### Critical (Tuần 1-2)
- [ ] UC15: Thanh toán & Đánh giá (3 ngày)
- [ ] UC10: Admin Báo cáo (2 ngày)
- [ ] UC12: UI Đặt lịch (3 ngày)

### High Priority (Tuần 3)
- [ ] Appointment Cancel/Reschedule (2 ngày)
- [ ] Receptionist Role (3 ngày)
- [ ] Lưu ảnh X-Quang (1 ngày)
- [ ] Notification History (1 ngày)

**Tổng ước tính:** 15 ngày (3 tuần)
