# PHASE 1: SỬA CÁC LỖI LOGIC CRITICAL - DOCTOR WORKFLOW

## 🔴 VẤN ĐỀ PHÁT HIỆN TRONG DOCTOR WORKFLOW

### 1. **LỖI NGHIÊM TRỌNG: Thiếu liên kết giữa Appointment và TreatmentPlan**

**Vấn đề:**
- Khi bác sĩ quét QR bệnh nhân, hệ thống chỉ trả về `appointmentId`
- KHÔNG có cách nào để biết bệnh nhân đã có TreatmentPlan chưa
- Bác sĩ phải TỰ TẠO plan mới mỗi lần, dẫn đến duplicate plans

**Theo docs UC18-UC21:**
> "Bác sĩ kiểm tra trạng thái liệu trình (Treatment Plan) của bệnh nhân"
> "Nếu đã có phác đồ điều trị -> Xem thông tin Phác đồ -> Thực hiện bước điều trị hiện tại"
> "Nếu chưa có -> Tiến hành thăm khám ban đầu -> Tạo Phác đồ điều trị mới"

**Hiện trạng code:**
```java
// DoctorController.getPatientByQr() - THIẾU treatmentPlanId
return ResponseEntity.ok(Map.of(
    "appointmentId", finalAppointmentId,
    // ❌ THIẾU: "treatmentPlanId", planId
    // ❌ THIẾU: "hasTreatmentPlan", true/false
));
```

**Hậu quả:**
- Bác sĩ tạo nhiều plan cho cùng 1 appointment
- Không theo dõi được tiến trình điều trị
- UI không biết hiển thị form nào (tạo mới vs xem plan)

---

### 2. **LỖI LOGIC: MedicalRecord không liên kết với TreatmentPlan đúng cách**

**Vấn đề:**
- `TreatmentPlan` có field `medicalRecordId` (nullable)
- `MedicalRecord` KHÔNG có field `treatmentPlanId`
- Quan hệ 1-1 không rõ ràng

**Theo docs UC20-UC21:**
> "Bác sĩ thực hiện khám và chẩn đoán, sau đó tạo lộ trình điều trị"
> "Ghi nhận kết quả & Chẩn đoán -> Tạo/Cập nhật MedicalRecord"

**Luồng đúng phải là:**
1. Bác sĩ khám bệnh nhân (Appointment)
2. Tạo MedicalRecord (chẩn đoán sơ bộ)
3. Tạo TreatmentPlan dựa trên MedicalRecord
4. Thực hiện từng bước trong Plan
5. Mỗi bước hoàn thành -> Cập nhật MedicalRecordDetail

**Hiện trạng:**
- TreatmentPlan có thể tạo KHÔNG CẦN MedicalRecord
- Prescription tạo MedicalRecord nếu chưa có
- Không rõ MedicalRecord nào thuộc về TreatmentPlan nào

---

### 3. **LỖI LOGIC: Kê đơn thuốc không liên kết với TreatmentPlanStep**

**Vấn đề:**
- Prescription chỉ liên kết với MedicalRecord
- KHÔNG biết đơn thuốc thuộc về bước nào trong plan

**Theo docs UC21:**
> "Bác sĩ ghi nhận kết quả cho BƯỚC ĐIỀU TRỊ HIỆN TẠI"
> "Kê đơn thuốc (tùy chọn) cho bước đó"
> "Hoàn thành bước điều trị"

**Luồng đúng:**
- Mỗi TreatmentPlanStep có thể có 1 Prescription riêng
- Ví dụ: Bước "Nhổ răng" -> Đơn thuốc giảm đau
- Bước "Trám răng" -> Đơn thuốc kháng sinh

**Hiện trạng:**
- 1 MedicalRecord chỉ có 1 Prescription
- Không thể kê nhiều đơn cho nhiều bước

---

### 4. **LỖI LOGIC: completeStepAndAdvance() quá phức tạp và sai**

**Vấn đề:**
```java
// TreatmentPlanService.completeStepAndAdvance()
// Logic tự động sinh bước "Đọc kết quả" SAI
if (nextStep == null) {
    if (activeQueue.getOriginalRoomId() != null) {
        // Tự động tạo bước mới ???
        TreatmentPlanStep returnStep = ...
    }
}
```

**Sai ở đâu:**
- Tự động sinh bước KHÔNG NÊN ở service layer
- Bác sĩ phải TỰ QUYẾT ĐỊNH có cần bước "Đọc kết quả" không
- Logic này thuộc về business rule, không phải technical

**Theo docs AC21:**
> "Bác sĩ nhấn Hoàn thành bước điều trị"
> "Hệ thống kiểm tra: Còn step tiếp theo?"
> "Nếu có -> Kích hoạt bước tiếp theo + Chuyển phòng"
> "Nếu không -> Hoàn tất phác đồ"

KHÔNG CÓ logic tự động sinh bước!

---

### 5. **LỖI THIẾT KẾ: Appointment.planStepId không được sử dụng**

**Vấn đề:**
```java
@Entity
public class Appointment {
    @Column(name = "plan_step_id")
    private Long planStepId; // ❌ KHÔNG BAO GIỜ ĐƯỢC SET
}
```

**Mục đích ban đầu:**
- Liên kết Appointment với TreatmentPlanStep
- Biết appointment này để thực hiện bước nào

**Hiện trạng:**
- Field này luôn NULL
- Không có code nào set giá trị

---

## ✅ GIẢI PHÁP - PHASE 1

### Fix 1: Thêm TreatmentPlan info vào API getPatientByQr

```java
// DoctorController.java
@GetMapping("/patient")
public ResponseEntity<?> getPatientByQr(@RequestParam String qr) {
    // ... existing code ...
    
    // Tìm TreatmentPlan của appointment
    TreatmentPlan activePlan = null;
    if (finalAppointmentId != null) {
        activePlan = treatmentPlanRepository
            .findByAppointmentIdAndStatusNot(finalAppointmentId, TreatmentPlanStatus.COMPLETED)
            .orElse(null);
    }
    
    return ResponseEntity.ok(Map.of(
        "id", p.getId(),
        // ... existing fields ...
        "appointmentId", finalAppointmentId != null ? finalAppointmentId : -1,
        "treatmentPlanId", activePlan != null ? activePlan.getId() : -1,
        "hasTreatmentPlan", activePlan != null,
        "treatmentPlanStatus", activePlan != null ? activePlan.getStatus().name() : "NONE"
    ));
}
```

### Fix 2: Sửa quan hệ MedicalRecord ↔ TreatmentPlan

```java
// MedicalRecord.java
@Entity
public class MedicalRecord {
    // ... existing fields ...
    
    @OneToOne(mappedBy = "medicalRecord", cascade = CascadeType.ALL)
    private TreatmentPlan treatmentPlan; // Thêm quan hệ ngược
}

// TreatmentPlan.java
@Entity
public class TreatmentPlan {
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_record_id")
    private MedicalRecord medicalRecord; // Đã có
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id") // THÊM MỚI
    private Appointment appointment;
}
```

### Fix 3: Thêm Prescription vào TreatmentPlanStep

```java
// TreatmentPlanStep.java
@Entity
public class TreatmentPlanStep {
    // ... existing fields ...
    
    @OneToOne(mappedBy = "step", cascade = CascadeType.ALL)
    private Prescription prescription; // Mỗi bước có 1 đơn thuốc
}

// Prescription.java
@Entity
public class Prescription {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_record_id")
    private MedicalRecord medicalRecord; // Giữ lại cho backward compatibility
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "step_id") // THÊM MỚI
    private TreatmentPlanStep step;
}
```

### Fix 4: Đơn giản hóa completeStepAndAdvance()

```java
// TreatmentPlanService.java
@Transactional
public String completeStepAndAdvance(Long stepId, String doctorConclusion, 
                                     List<String> imageUrls, Long doctorRoomId) {
    TreatmentPlanStep currentStep = stepRepository.findById(stepId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    
    // 1. Kiểm tra quyền
    if (doctorRoomId != null && currentStep.getClinicRoom() != null) {
        if (!doctorRoomId.equals(currentStep.getClinicRoom().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                "Bạn không có quyền hoàn thành bước này");
        }
    }
    
    // 2. Hoàn thành bước hiện tại
    currentStep.setStatus(StepStatus.COMPLETED);
    currentStep.setDoctorConclusion(doctorConclusion);
    currentStep.setCompletedAt(LocalDateTime.now());
    
    // 3. Lưu ảnh
    if (imageUrls != null && !imageUrls.isEmpty()) {
        for (String url : imageUrls) {
            StepImage img = StepImage.builder()
                .step(currentStep)
                .imageUrl(url)
                .build();
            currentStep.getImages().add(img);
        }
    }
    stepRepository.save(currentStep);
    
    // 4. Tìm bước tiếp theo
    TreatmentPlan plan = currentStep.getPlan();
    TreatmentPlanStep nextStep = plan.getSteps().stream()
        .filter(s -> s.getStatus() == StepStatus.PENDING)
        .min(Comparator.comparingInt(s -> s.getSequenceOrder() != null ? s.getSequenceOrder() : 0))
        .orElse(null);
    
    if (nextStep == null) {
        // Hoàn tất toàn bộ phác đồ
        plan.setStatus(TreatmentPlanStatus.COMPLETED);
        planRepository.save(plan);
        
        // Gửi notification
        sendNotification(plan.getPatient(), "Phác đồ hoàn tất", 
            "Phác đồ điều trị của bạn đã hoàn tất.");
        
        return null; // Không còn bước nào
    }
    
    // 5. Kích hoạt bước tiếp theo
    nextStep.setStatus(StepStatus.IN_PROGRESS);
    stepRepository.save(nextStep);
    
    // 6. Chuyển phòng nếu cần
    ClinicRoom nextRoom = nextStep.getClinicRoom();
    if (nextRoom != null) {
        transferPatientToRoom(plan.getPatient(), nextRoom);
        return nextRoom.getName();
    }
    
    return null;
}
```

### Fix 5: Sử dụng Appointment.planStepId

```java
// Khi tạo TreatmentPlan từ Appointment
@Transactional
public TreatmentPlan createFromAppointment(Long appointmentId) {
    Appointment appointment = appointmentRepository.findById(appointmentId)
        .orElseThrow();
    
    // Tạo MedicalRecord trước
    MedicalRecord medicalRecord = MedicalRecord.builder()
        .appointment(appointment)
        .patient(appointment.getPatient())
        .doctor(appointment.getDoctor())
        .createdAt(LocalDateTime.now())
        .build();
    medicalRecord = medicalRecordRepository.save(medicalRecord);
    
    // Tạo TreatmentPlan
    TreatmentPlan plan = TreatmentPlan.builder()
        .appointment(appointment)
        .medicalRecord(medicalRecord)
        .patient(appointment.getPatient())
        .status(TreatmentPlanStatus.IN_PROGRESS)
        .isDraft(true)
        .build();
    plan = planRepository.save(plan);
    
    // Link ngược
    medicalRecord.setTreatmentPlan(plan);
    
    return plan;
}
```

---

## 📋 CHECKLIST PHASE 1

- [ ] Fix 1: Thêm treatmentPlanId vào API getPatientByQr
- [ ] Fix 2: Sửa quan hệ MedicalRecord ↔ TreatmentPlan
- [ ] Fix 3: Thêm Prescription vào TreatmentPlanStep
- [ ] Fix 4: Đơn giản hóa completeStepAndAdvance()
- [ ] Fix 5: Sử dụng Appointment.planStepId
- [ ] Test: Luồng khám bệnh từ đầu đến cuối
- [ ] Test: Tạo plan, thực hiện bước, kê đơn, hoàn thành
- [ ] Test: Chuyển phòng giữa các bước
- [ ] Migration: Thêm các column mới vào DB

---

**Ước tính thời gian:** 2-3 ngày
**Độ ưu tiên:** 🔴 CRITICAL - Phải làm trước tất cả
