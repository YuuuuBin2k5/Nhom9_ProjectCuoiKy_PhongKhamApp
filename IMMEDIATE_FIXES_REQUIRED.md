# CÁC FIX CẦN LÀM NGAY LẬP TỨC

## PHÂN TÍCH TỪ DOCUMENTATION
Đã đọc kỹ tất cả 21 use cases trong `docs (1)` và so sánh với implementation hiện tại.

---

## 🔥 CRITICAL FIXES (Làm ngay hôm nay)

### FIX 1: Password Validation - UC02
**File**: `clinic_backend/src/main/java/com/hcmute/clinic/controller/AuthController.java`
**Dòng**: Method `register()`
**Vấn đề**: Chỉ check length, không check có chữ + số
**Documentation**: "Mật khẩu (tối thiểu 6 ký tự, bao gồm chữ và số)"

```java
// ❌ HIỆN TẠI
if (req.getPassword() == null || req.getPassword().length() < 6) {
    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 6 characters");
}

// ✅ CẦN SỬA
if (req.getPassword() == null || req.getPassword().length() < 6) {
    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 6 characters");
}
if (!req.getPassword().matches("^(?=.*[A-Za-z])(?=.*\\d).+$")) {
    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
        "Mật khẩu phải bao gồm cả chữ và số");
}
```

---

### FIX 2: Phone Number Validation - UC02, UC11
**File**: `clinic_backend/src/main/java/com/hcmute/clinic/controller/PatientController.java`
**Method**: `updateMe()`
**Vấn đề**: Không validate phone format

```java
// Thêm validation
if (req.getPhone() != null && !req.getPhone().matches("^(0|\\+84)[0-9]{9,10}$")) {
    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
        "Số điện thoại không hợp lệ. Định dạng: 0xxxxxxxxx hoặc +84xxxxxxxxx");
}
```

---

### FIX 3: Date of Birth Validation - UC11
**File**: `clinic_backend/src/main/java/com/hcmute/clinic/controller/PatientController.java`
**Method**: `updateMe()`
**Vấn đề**: Có thể nhập ngày sinh trong tương lai

```java
// Thêm validation
if (req.getDob() != null) {
    LocalDate dob = LocalDate.parse(req.getDob());
    if (dob.isAfter(LocalDate.now())) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
            "Ngày sinh không thể là ngày trong tương lai");
    }
    if (dob.isBefore(LocalDate.now().minusYears(120))) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
            "Ngày sinh không hợp lệ");
    }
}
```

---

### FIX 4: Appointment Past Date Validation - UC12
**File**: `clinic_backend/src/main/java/com/hcmute/clinic/controller/AppointmentController.java`
**Method**: `bookAppointment()`
**Vấn đề**: Có thể đặt lịch trong quá khứ

```java
// Thêm validation
if (req.getAppointmentDatetime().isBefore(LocalDateTime.now())) {
    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
        "Không thể đặt lịch trong quá khứ");
}
```

---

## 🟠 HIGH PRIORITY FIXES (Làm trong 1-2 ngày)

### FIX 5: QR Scanner cho Doctor - UC19
**File**: `mobile_android/app/src/main/java/com/hcmute/mobile_android/ui/activities/staff/DoctorWorkflowActivity.java`
**Vấn đề**: Thiếu QR scanner button
**Documentation**: "Bác sĩ mở tính năng Quét mã QR trên ứng dụng di động"

**Cần thêm**:
1. Button "Quét mã QR" trong layout
2. Launch QRScannerActivity
3. Handle scan result → load patient info

```xml
<!-- activity_doctor_workflow.xml -->
<Button
    android:id="@+id/btnScanPatientQR"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Quét mã QR bệnh nhân"
    android:drawableLeft="@drawable/ic_qr_scan"
    android:layout_margin="16dp" />
```

```java
// DoctorWorkflowActivity.java
binding.btnScanPatientQR.setOnClickListener(v -> {
    Intent intent = new Intent(this, QRScannerActivity.class);
    intent.putExtra("scan_type", "PATIENT_QR");
    startActivityForResult(intent, REQUEST_SCAN_PATIENT_QR);
});

@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_SCAN_PATIENT_QR && resultCode == RESULT_OK) {
        String qrData = data.getStringExtra("qr_data");
        // Parse patient ID from QR
        // Load patient info
        loadPatientFromQR(qrData);
    }
}
```

---

### FIX 6: Patient History Button - UC19
**File**: `mobile_android/app/src/main/java/com/hcmute/mobile_android/ui/activities/staff/DoctorWorkflowActivity.java`
**Vấn đề**: Không có button xem lịch sử khám
**Documentation**: "Bác sĩ chọn Xem lịch sử hồ sơ"

**Cần thêm**:
1. Button "Lịch sử khám"
2. Bottom sheet hiển thị medical history
3. Highlight allergies và underlying conditions

```xml
<Button
    android:id="@+id/btnPatientHistory"
    android:text="Lịch sử khám"
    android:drawableLeft="@drawable/ic_history" />
```

```java
binding.btnPatientHistory.setOnClickListener(v -> {
    showPatientHistoryBottomSheet();
});

private void showPatientHistoryBottomSheet() {
    // Create bottom sheet with:
    // - Previous appointments
    // - Previous diagnoses
    // - Previous prescriptions
    // - Allergies (highlighted in red)
    // - Underlying conditions
}
```

---

### FIX 7: Priority Indicator trong Queue - UC18
**File**: `mobile_android/app/src/main/java/com/hcmute/mobile_android/adapters/QueueAdapter.java`
**Vấn đề**: Không có visual indicator cho priority patients
**Documentation**: "Trạng thái hàng đợi được cập nhật thời gian thực"

**Cần update `item_queue.xml`**:
```xml
<ImageView
    android:id="@+id/ivPriorityBadge"
    android:layout_width="24dp"
    android:layout_height="24dp"
    android:src="@drawable/ic_star"
    android:tint="@color/gold"
    android:visibility="gone" />

<TextView
    android:id="@+id/tvWaitTime"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="~15 phút"
    android:textColor="@color/orange"
    android:textSize="12sp" />
```

**Update QueueAdapter.java**:
```java
@Override
public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    QueueItem item = items.get(position);
    
    // Show priority badge for priority > 5
    if (item.getPriority() != null && item.getPriority() > 5) {
        holder.binding.ivPriorityBadge.setVisibility(View.VISIBLE);
    } else {
        holder.binding.ivPriorityBadge.setVisibility(View.GONE);
    }
    
    // Color coding based on status
    int backgroundColor;
    switch (item.getStatus()) {
        case "WAITING":
            backgroundColor = Color.parseColor("#E8F5E9"); // Light green
            break;
        case "IN_PROGRESS":
            backgroundColor = Color.parseColor("#E3F2FD"); // Light blue
            break;
        case "RETURNED_PRIORITY":
            backgroundColor = Color.parseColor("#FFF3E0"); // Light orange
            break;
        case "PAUSED_FOR_TEST":
            backgroundColor = Color.parseColor("#F3E5F5"); // Light purple
            break;
        default:
            backgroundColor = Color.WHITE;
    }
    holder.itemView.setBackgroundColor(backgroundColor);
    
    // Calculate and show estimated wait time
    // TODO: Get from backend API
}
```

---

### FIX 8: Room Transfer Notification - UC14
**File**: `clinic_backend/src/main/java/com/hcmute/clinic/service/TreatmentPlanService.java`
**Method**: `completeStepAndAdvance()`
**Vấn đề**: Notification không đủ chi tiết
**Documentation**: "Bệnh nhân theo dõi trạng thái và di chuyển đến phòng khám được chỉ định"

**Cần improve notification**:
```java
// Current notification
com.hcmute.clinic.entity.Notification notif = com.hcmute.clinic.entity.Notification.builder()
        .patient(plan.getPatient())
        .title("Chuyển phòng khám")
        .message("Vui lòng di chuyển đến " + nextRoom.getName() + " để tiếp tục điều trị. Số TT: " + activeQueue.getQueueNumber())
        .type("ROOM_TRANSFER")
        .build();

// Should include:
// - Room location/description
// - Estimated wait time
// - Next service name
String message = String.format(
    "Vui lòng di chuyển đến %s (%s) để tiếp tục điều trị.\n" +
    "Dịch vụ tiếp theo: %s\n" +
    "Số thứ tự: %d\n" +
    "Thời gian chờ dự kiến: ~%d phút",
    nextRoom.getName(),
    nextRoom.getDescription() != null ? nextRoom.getDescription() : "Tầng 1",
    nextStep.getService().getName(),
    activeQueue.getQueueNumber(),
    estimatedWaitTime
);
```

---

### FIX 9: Payment Confirmation Workflow - UC15
**File**: Cần tạo mới
**Vấn đề**: Không có workflow cho staff confirm cash payment
**Documentation**: "Xác nhận sau khi trả tiền mặt tại quầy"

**Cần tạo**:
1. `StaffPaymentConfirmationActivity.java`
2. API endpoint: `POST /api/invoices/{id}/confirm-payment`
3. Update invoice status to PAID

```java
// InvoiceController.java
@PostMapping("/{id}/confirm-payment")
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
public ResponseEntity<?> confirmPayment(@PathVariable Long id) {
    Invoice invoice = invoiceService.findById(id);
    if (invoice.getStatus() == InvoiceStatus.PAID) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invoice already paid");
    }
    
    invoice.setStatus(InvoiceStatus.PAID);
    invoice.setPaidAt(LocalDateTime.now());
    invoiceRepository.save(invoice);
    
    // Send notification to patient
    Notification notif = Notification.builder()
            .patient(invoice.getPatient())
            .title("Thanh toán thành công")
            .message("Hóa đơn #" + invoice.getId() + " đã được thanh toán. Cảm ơn bạn!")
            .type("PAYMENT_SUCCESS")
            .build();
    notificationRepository.save(notif);
    
    return ResponseEntity.ok(Map.of("message", "Payment confirmed successfully"));
}
```

---

## 🟡 MEDIUM PRIORITY (Làm trong tuần này)

### FIX 10: Service Search - UC16
**File**: `mobile_android/app/src/main/java/com/hcmute/mobile_android/ui/fragments/PatientDashboardFragment.java`
**Vấn đề**: Thiếu search bar
**Documentation**: "Bệnh nhân có thể thực hiện Tìm kiếm dịch vụ bằng cách nhập từ khóa"

### FIX 11: Doctor Selection - UC12
**File**: Cần tạo `DoctorSelectionActivity.java`
**Vấn đề**: Không có UI để chọn bác sĩ
**Documentation**: "Bệnh nhân có thể chọn Bác sĩ mong muốn (không bắt buộc)"

### FIX 12: Revenue Chart - UC10
**File**: `mobile_android/app/src/main/java/com/hcmute/mobile_android/ui/fragments/AdminDashboardFragment.java`
**Vấn đề**: Không có chart hiển thị doanh thu
**Documentation**: "Hệ thống hiển thị kết quả bao gồm: Tổng doanh thu, Tổng số lượt khám"

---

## TỔNG KẾT

### Đã fix: 5 bugs
✅ originalRoomId in processSelfScan
✅ originalRoomId in DataSeed
✅ originalRoomId in transferToXRay
✅ completeXRay room return logic
✅ delayPatient swap logic

### Cần fix ngay: 4 validations
❌ Password validation (chữ + số)
❌ Phone number validation
❌ Date of birth validation
❌ Appointment past date validation

### Cần implement: 5 features
❌ QR Scanner cho Doctor
❌ Patient History button
❌ Priority indicator trong Queue
❌ Room transfer notification improvement
❌ Payment confirmation workflow

### Tổng cộng: 14 fixes cần làm
- 4 Critical (validation)
- 5 High (UI features)
- 5 Medium (nice to have)

Ưu tiên làm 4 validations trước vì ảnh hưởng đến data integrity!
