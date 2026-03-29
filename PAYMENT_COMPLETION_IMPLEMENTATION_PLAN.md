# KẾ HOẠCH TRIỂN KHAI CHỨC NĂNG THANH TOÁN & HOÀN TẤT HỒ SƠ

## PHÂN TÍCH HIỆN TRẠNG

### ✅ Đã có (Existing)
1. **Invoice Entity** - Cấu trúc database đầy đủ
2. **InvoiceService** - Logic xử lý thanh toán
3. **InvoiceController** - API endpoints cơ bản
4. **PaymentActivity** - UI thanh toán (nhưng chỉ xử lý invoice có sẵn)
5. **TreatmentPlanService.completeStepAndAdvance()** - Hoàn thành từng bước
6. **TreatmentPlan.status = COMPLETED** - Đánh dấu hoàn tất phác đồ

### ❌ Thiếu (Missing)

#### Backend Missing:
1. **API tạo Invoice từ TreatmentPlan** - CRITICAL
   - Endpoint: `POST /api/treatment-plans/{planId}/complete-and-generate-invoice`
   - Logic: Tổng hợp tất cả steps → Tính tổng tiền → Tạo Invoice
   - Validation: Kiểm tra tất cả steps đã COMPLETED
   
2. **Logic hoàn tất toàn bộ Treatment Plan** - CRITICAL
   - Đánh dấu plan.status = COMPLETED
   - Tạo Invoice với items từ các steps
   - Gửi notification cho bệnh nhân
   
3. **InvoiceItem Entity/Table** - MISSING
   - Lưu chi tiết từng dịch vụ trong invoice
   - Cần có: serviceId, serviceName, quantity, unitPrice, totalPrice

#### Mobile Missing:
1. **Logic nút "Thanh toán" trong DoctorWorkflowActivity** - CRITICAL
   - Hiện tại chỉ navigate đến PaymentActivity
   - Cần: Gọi API complete plan → Nhận invoiceId → Navigate với invoiceId
   
2. **Validation trước khi thanh toán** - IMPORTANT
   - Kiểm tra tất cả steps đã hoàn thành
   - Hiển thị cảnh báo nếu còn steps chưa xong
   
3. **UI hiển thị trạng thái** - NICE TO HAVE
   - Loading state khi đang tạo invoice
   - Success/Error feedback

---

## KẾ HOẠCH TRIỂN KHAI CHI TIẾT

### PHASE 1: Backend - Tạo Invoice từ Treatment Plan (CRITICAL)

#### Step 1.1: Tạo InvoiceItem Entity
**File**: `clinic_backend/src/main/java/com/hcmute/clinic/entity/InvoiceItem.java`

```java
@Entity
@Table(name = "invoice_items")
public class InvoiceItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;
    
    @ManyToOne
    @JoinColumn(name = "service_id")
    private Service service;
    
    @ManyToOne
    @JoinColumn(name = "treatment_plan_step_id")
    private TreatmentPlanStep treatmentPlanStep;
    
    private String serviceName;
    private String toothNumber;
    private Integer quantity = 1;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private String description;
}
```

**Lý do**: Cần lưu chi tiết từng dịch vụ trong invoice để:
- Hiển thị breakdown cho bệnh nhân
- Audit trail
- Báo cáo doanh thu chi tiết

#### Step 1.2: Update Invoice Entity
**File**: `clinic_backend/src/main/java/com/hcmute/clinic/entity/Invoice.java`

```java
@OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL)
private List<InvoiceItem> items;
```

#### Step 1.3: Tạo InvoiceItemRepository
**File**: `clinic_backend/src/main/java/com/hcmute/clinic/repository/InvoiceItemRepository.java`

#### Step 1.4: Thêm method trong InvoiceService
**File**: `clinic_backend/src/main/java/com/hcmute/clinic/service/InvoiceService.java`

```java
@Transactional
public Invoice createInvoiceFromTreatmentPlan(Long treatmentPlanId) {
    // 1. Load treatment plan with steps
    TreatmentPlan plan = treatmentPlanRepository.findByIdWithSteps(treatmentPlanId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Treatment plan not found"));
    
    // 2. Validate: All steps must be COMPLETED or SKIPPED
    boolean hasIncompleteSteps = plan.getSteps().stream()
        .anyMatch(s -> s.getStatus() != StepStatus.COMPLETED && s.getStatus() != StepStatus.SKIPPED);
    
    if (hasIncompleteSteps) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
            "Không thể tạo hóa đơn. Vui lòng hoàn thành tất cả các bước điều trị.");
    }
    
    // 3. Check if invoice already exists
    Optional<Invoice> existingInvoice = invoiceRepository.findByTreatmentPlanId(treatmentPlanId);
    if (existingInvoice.isPresent()) {
        return existingInvoice.get(); // Return existing invoice
    }
    
    // 4. Calculate total amount from completed steps
    BigDecimal totalAmount = BigDecimal.ZERO;
    List<InvoiceItem> items = new ArrayList<>();
    
    for (TreatmentPlanStep step : plan.getSteps()) {
        if (step.getStatus() == StepStatus.COMPLETED) {
            BigDecimal price = step.getService().getPrice();
            totalAmount = totalAmount.add(price);
            
            InvoiceItem item = InvoiceItem.builder()
                .service(step.getService())
                .treatmentPlanStep(step)
                .serviceName(step.getService().getName())
                .toothNumber(step.getToothNumber())
                .quantity(1)
                .unitPrice(price)
                .totalPrice(price)
                .description(step.getDoctorConclusion())
                .build();
            items.add(item);
        }
    }
    
    // 5. Create invoice
    Invoice invoice = Invoice.builder()
        .patient(plan.getPatient())
        .treatmentPlan(plan)
        .medicalRecord(plan.getMedicalRecord())
        .totalAmount(totalAmount)
        .remainingAmount(totalAmount)
        .paymentStatus(InvoiceStatus.UNPAID)
        .build();
    
    invoice = invoiceRepository.save(invoice);
    
    // 6. Save invoice items
    for (InvoiceItem item : items) {
        item.setInvoice(invoice);
    }
    invoice.setItems(items);
    invoiceRepository.save(invoice);
    
    // 7. Mark treatment plan as COMPLETED
    plan.setStatus(TreatmentPlanStatus.COMPLETED);
    treatmentPlanRepository.save(plan);
    
    // 8. Send notification to patient
    Notification notif = Notification.builder()
        .patient(plan.getPatient())
        .title("Hóa đơn thanh toán")
        .message("Phác đồ điều trị của bạn đã hoàn tất. Vui lòng thanh toán hóa đơn " + 
                 String.format("%,.0f VNĐ", totalAmount))
        .type("INVOICE_CREATED")
        .build();
    notificationRepository.save(notif);
    
    if (plan.getPatient().getFcmToken() != null) {
        fcmService.sendNotification(
            plan.getPatient().getFcmToken(), 
            notif.getTitle(), 
            notif.getMessage()
        );
    }
    
    return invoice;
}
```

**Lý do từng bước**:
- Validate để đảm bảo không tạo invoice khi còn bước chưa xong
- Check existing để tránh duplicate invoice
- Tính tổng từ steps thực tế (không phải estimate)
- Tạo invoice items để có breakdown chi tiết
- Gửi notification để bệnh nhân biết cần thanh toán

#### Step 1.5: Thêm API endpoint
**File**: `clinic_backend/src/main/java/com/hcmute/clinic/controller/TreatmentPlanController.java`

```java
@PostMapping("/{planId}/complete-and-generate-invoice")
@PreAuthorize("hasRole('DOCTOR') or hasRole('RECEPTIONIST')")
public ResponseEntity<InvoiceDto> completeAndGenerateInvoice(
    @PathVariable Long planId,
    Authentication auth
) {
    Invoice invoice = invoiceService.createInvoiceFromTreatmentPlan(planId);
    return ResponseEntity.ok(toInvoiceDto(invoice));
}
```

---

### PHASE 2: Mobile - Update Payment Flow

#### Step 2.1: Thêm API method trong ApiService
**File**: `mobile_android/app/src/main/java/com/hcmute/mobile_android/network/ApiService.java`

```java
@POST("api/treatment-plans/{planId}/complete-and-generate-invoice")
Call<Invoice> completeAndGenerateInvoice(@Path("planId") Long planId);
```

#### Step 2.2: Update nút Thanh toán trong DoctorWorkflowActivity
**File**: `mobile_android/app/src/main/java/com/hcmute/mobile_android/ui/activities/staff/DoctorWorkflowActivity.java`

```java
btnPayment.setOnClickListener(v -> {
    if (currentPatient == null || currentTreatmentPlanId == null) {
        Toast.makeText(this, "Vui lòng chọn bệnh nhân và tạo phác đồ điều trị", Toast.LENGTH_SHORT).show();
        return;
    }
    
    // Validate: Check if all steps are completed
    boolean hasIncompleteSteps = treatmentSteps.stream()
        .anyMatch(s -> !"COMPLETED".equals(s.getStatus()) && !"SKIPPED".equals(s.getStatus()));
    
    if (hasIncompleteSteps) {
        new AlertDialog.Builder(this)
            .setTitle("Chưa thể thanh toán")
            .setMessage("Vui lòng hoàn thành tất cả các bước điều trị trước khi thanh toán.")
            .setPositiveButton("OK", null)
            .show();
        return;
    }
    
    // Show confirmation dialog
    new AlertDialog.Builder(this)
        .setTitle("Xác nhận hoàn tất")
        .setMessage("Bạn có chắc muốn hoàn tất phác đồ điều trị và tạo hóa đơn thanh toán?\n\n" +
                   "Tổng tiền: " + tvTotalEstimate.getText())
        .setPositiveButton("Xác nhận", (dialog, which) -> completeAndGenerateInvoice())
        .setNegativeButton("Hủy", null)
        .show();
});

private void completeAndGenerateInvoice() {
    // Show loading
    ProgressDialog progressDialog = new ProgressDialog(this);
    progressDialog.setMessage("Đang tạo hóa đơn...");
    progressDialog.setCancelable(false);
    progressDialog.show();
    
    apiService.completeAndGenerateInvoice(currentTreatmentPlanId).enqueue(new Callback<Invoice>() {
        @Override
        public void onResponse(Call<Invoice> call, Response<Invoice> response) {
            progressDialog.dismiss();
            
            if (response.isSuccessful() && response.body() != null) {
                Invoice invoice = response.body();
                
                // Show success message
                Toast.makeText(DoctorWorkflowActivity.this, 
                    "Đã tạo hóa đơn thành công!", Toast.LENGTH_SHORT).show();
                
                // Navigate to payment activity
                Intent intent = new Intent(DoctorWorkflowActivity.this, PaymentActivity.class);
                intent.putExtra("invoiceId", invoice.getId());
                intent.putExtra("amount", invoice.getTotalAmount());
                intent.putExtra("PATIENT_NAME", currentPatient.getFullName());
                startActivity(intent);
                
                // Close this activity
                finish();
            } else {
                try {
                    String errorBody = response.errorBody().string();
                    Toast.makeText(DoctorWorkflowActivity.this, 
                        "Lỗi: " + errorBody, Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(DoctorWorkflowActivity.this, 
                        "Lỗi tạo hóa đơn: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
        }
        
        @Override
        public void onFailure(Call<Invoice> call, Throwable t) {
            progressDialog.dismiss();
            Toast.makeText(DoctorWorkflowActivity.this, 
                "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
        }
    });
}
```

**Lý do**:
- Validate trước để tránh lỗi từ backend
- Confirmation dialog để tránh nhầm lẫn
- Loading state để UX tốt hơn
- Navigate đến PaymentActivity với invoiceId
- Finish activity sau khi tạo invoice thành công

#### Step 2.3: Update Invoice model
**File**: `mobile_android/app/src/main/java/com/hcmute/mobile_android/network/models/Invoice.java`

Đảm bảo có đầy đủ fields:
```java
private Long id;
private Long patientId;
private String patientName;
private Long treatmentPlanId;
private Double totalAmount;
private String paymentStatus;
private List<InvoiceItem> items; // Add this
```

#### Step 2.4: Tạo InvoiceItem model
**File**: `mobile_android/app/src/main/java/com/hcmute/mobile_android/network/models/InvoiceItem.java`

```java
public class InvoiceItem {
    private Long id;
    private String serviceName;
    private String toothNumber;
    private Integer quantity;
    private Double unitPrice;
    private Double totalPrice;
    private String description;
    // getters/setters
}
```

---

### PHASE 3: Enhanced Features (Optional but Recommended)

#### Feature 3.1: Hiển thị Invoice Detail trước khi thanh toán
- Update PaymentActivity để hiển thị breakdown
- RecyclerView với InvoiceItemAdapter
- Tương tự như PriceBreakdownAdapter đã tạo

#### Feature 3.2: Patient Notification
- Đã có trong backend (Step 1.4)
- Bệnh nhân nhận notification qua FCM
- Hiển thị trong app (PatientDashboardFragment)

#### Feature 3.3: Invoice History
- Đã có API: `GET /api/invoices/my`
- Đã có InvoiceListActivity
- Chỉ cần test

---

## CHECKLIST TRIỂN KHAI

### Backend Tasks
- [ ] 1.1: Tạo InvoiceItem Entity
- [ ] 1.2: Update Invoice Entity (add items relationship)
- [ ] 1.3: Tạo InvoiceItemRepository
- [ ] 1.4: Implement InvoiceService.createInvoiceFromTreatmentPlan()
- [ ] 1.5: Thêm API endpoint trong TreatmentPlanController
- [ ] 1.6: Test API với Postman/curl
- [ ] 1.7: Thêm InvoiceRepository.findByTreatmentPlanId()

### Mobile Tasks
- [ ] 2.1: Thêm API method trong ApiService
- [ ] 2.2: Update btnPayment logic trong DoctorWorkflowActivity
- [ ] 2.3: Implement completeAndGenerateInvoice() method
- [ ] 2.4: Update Invoice model (add items field)
- [ ] 2.5: Tạo InvoiceItem model
- [ ] 2.6: Test end-to-end flow

### Testing Tasks
- [ ] Test case 1: Hoàn tất phác đồ có 1 bước
- [ ] Test case 2: Hoàn tất phác đồ có nhiều bước
- [ ] Test case 3: Thử thanh toán khi còn bước chưa xong (should fail)
- [ ] Test case 4: Thử tạo invoice 2 lần cho cùng plan (should return existing)
- [ ] Test case 5: Kiểm tra notification đến bệnh nhân
- [ ] Test case 6: Kiểm tra invoice detail hiển thị đúng
- [ ] Test case 7: Thanh toán invoice thành công

---

## RISK ANALYSIS

### High Risk
1. **Duplicate Invoice Creation**
   - Mitigation: Check existing invoice trước khi tạo mới
   - Implemented in Step 1.4

2. **Incomplete Steps**
   - Mitigation: Validate tất cả steps đã COMPLETED
   - Implemented in Step 1.4 và 2.2

3. **Race Condition**
   - Mitigation: Use @Transactional
   - Implemented in Step 1.4

### Medium Risk
1. **Notification Failure**
   - Mitigation: Log error nhưng không block invoice creation
   - Use try-catch trong FCM send

2. **Price Mismatch**
   - Mitigation: Tính từ actual service price, không dùng estimate
   - Implemented in Step 1.4

---

## TIMELINE ESTIMATE

- **Backend Implementation**: 3-4 hours
  - Entity & Repository: 1 hour
  - Service logic: 1.5 hours
  - Controller & Testing: 1.5 hours

- **Mobile Implementation**: 2-3 hours
  - API integration: 1 hour
  - UI logic: 1 hour
  - Testing: 1 hour

- **Total**: 5-7 hours

---

## SUCCESS CRITERIA

✅ Bác sĩ có thể nhấn nút "Thanh toán" sau khi hoàn tất tất cả bước
✅ Hệ thống tự động tạo Invoice với chi tiết đầy đủ
✅ Treatment Plan được đánh dấu COMPLETED
✅ Bệnh nhân nhận notification về hóa đơn cần thanh toán
✅ Không tạo duplicate invoice
✅ Validate đúng các điều kiện trước khi tạo invoice
✅ UI/UX mượt mà với loading states và error handling

---

## NEXT STEPS

1. Review kế hoạch này với team
2. Tạo branch mới: `feature/payment-completion`
3. Implement theo thứ tự: Backend → Mobile → Testing
4. Code review sau mỗi phase
5. Deploy và monitor

---

**Prepared by**: AI Assistant (Leader Role)
**Date**: 2026-03-29
**Status**: READY FOR IMPLEMENTATION
