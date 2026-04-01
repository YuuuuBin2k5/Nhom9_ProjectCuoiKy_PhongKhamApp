# Fix Lỗi Không Kê Đơn Được - Prescription Validation Error

## Vấn Đề

Khi bác sĩ cố gắng kê đơn thuốc, hệ thống trả về lỗi 403:
```json
{
  "message": "Step/dịch vụ không thuộc lịch hẹn này. Vui lòng chọn đúng step hoặc bỏ trống để kê đơn chung.",
  "success": false
}
```

### Ví Dụ Cụ Thể
- Mobile app gửi request với `appointmentId: 30` và `treatmentPlanStepId: 4 hoặc 5`
- Backend validation fail vì step 4 và 5 thuộc appointment 1 (từ test data), không phải appointment 30

## Nguyên Nhân

**Root Cause**: Treatment Plan được load không thuộc về Appointment hiện tại.

### Chi Tiết Kỹ Thuật

1. **DoctorWorkflowActivity** lưu `treatmentPlanId` vào patient object từ lần khám trước
2. **PrescriptionActivity** nhận 2 tham số:
   - `appointmentId`: ID của lịch hẹn hiện tại (ví dụ: 30)
   - `treatmentPlanId`: ID của phác đồ điều trị từ patient object (có thể từ appointment cũ)

3. **Validation Logic** trong `PrescriptionService.java`:
   ```java
   if (step.getPlan().getAppointment().getId() != appointment.getId()) {
       throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
           "Step/dịch vụ không thuộc lịch hẹn này...");
   }
   ```

4. **Vấn đề**: Treatment Plan có thể được tạo từ appointment khác, nhưng app vẫn cho phép chọn và kê đơn → Validation fail.

## Giải Pháp

### Approach: Load Treatment Plan by AppointmentId Instead of TreatmentPlanId

Thay vì truyền `treatmentPlanId` (có thể từ appointment cũ), ta load treatment plan trực tiếp từ `appointmentId` hiện tại.

### 1. Backend: Thêm Endpoint Mới

**TreatmentPlanController.java** - New endpoint:
```java
@GetMapping("/by-appointment/{appointmentId}")
public ResponseEntity<TreatmentPlanDTO> getTreatmentPlanByAppointment(
        @PathVariable Long appointmentId) {
    TreatmentPlan plan = treatmentPlanService.findByAppointmentId(appointmentId);
    if (plan == null) {
        return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(toDTO(plan));
}
```

**TreatmentPlanService.java** - New method:
```java
public TreatmentPlan findByAppointmentId(Long appointmentId) {
    List<TreatmentPlan> plans = treatmentPlanRepository
        .findByAppointmentIdOrderByCreatedAtDesc(appointmentId);
    return plans.isEmpty() ? null : plans.get(0);
}
```

**TreatmentPlanRepository.java** - New query:
```java
List<TreatmentPlan> findByAppointmentIdOrderByCreatedAtDesc(Long appointmentId);
```

### 2. Android: Thêm API Method

**ApiService.java**:
```java
@GET("api/treatment-plans/by-appointment/{appointmentId}")
Call<TreatmentPlan> getTreatmentPlanByAppointment(
    @Path("appointmentId") Long appointmentId);
```

### 3. Android: Cập Nhật PrescriptionActivity

**PrescriptionActivity.java** - Method `loadTreatmentPlanSteps()`:
```java
private void loadTreatmentPlanSteps() {
    // CRITICAL FIX: Load treatment plan by appointmentId instead of treatmentPlanId
    // This ensures we always get the correct treatment plan for the current appointment
    apiService.getTreatmentPlanByAppointment(appointmentId).enqueue(new Callback<TreatmentPlan>() {
        @Override
        public void onResponse(Call<TreatmentPlan> call, Response<TreatmentPlan> response) {
            if (!response.isSuccessful() || response.body() == null) {
                Toast.makeText(PrescriptionActivity.this, 
                    "Không tải được phác đồ điều trị", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            TreatmentPlan plan = response.body();
            
            // Validate that treatment plan belongs to current appointment
            if (plan.getAppointmentId() != null && 
                !plan.getAppointmentId().equals(appointmentId)) {
                Toast.makeText(PrescriptionActivity.this, 
                    "Phác đồ không thuộc lịch hẹn này. Vui lòng kiểm tra lại.", 
                    Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            
            // ... rest of the code
        }
    });
}
```

**PrescriptionActivity.java** - Method `onCreate()`:
```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_prescription);

    appointmentId = getIntent().getLongExtra(EXTRA_APPOINTMENT_ID, -1);
    treatmentPlanId = getIntent().getLongExtra(EXTRA_TREATMENT_PLAN_ID, -1);
    if (appointmentId == -1) {
        Toast.makeText(this, "Không tìm thấy lịch hẹn", Toast.LENGTH_SHORT).show();
        finish();
        return;
    }
    // treatmentPlanId is now optional - we load by appointmentId instead

    apiService = RetrofitClient.getApiService(this);
    initViews();
    loadTreatmentPlanSteps();
}
```

### 4. Enhanced Backend Validation

**PrescriptionService.java** - Enhanced validation logic:
```java
// Check if step belongs to appointment (direct link)
if (step.getPlan().getAppointment() != null && 
    step.getPlan().getAppointment().getId().equals(appointment.getId())) {
    return; // Valid
}

// Check via medical record -> appointment path
if (step.getPlan().getMedicalRecord() != null && 
    step.getPlan().getMedicalRecord().getAppointment() != null &&
    step.getPlan().getMedicalRecord().getAppointment().getId().equals(appointment.getId())) {
    return; // Valid
}

// If neither path validates, throw error
throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
    "Step/dịch vụ không thuộc lịch hẹn này...");
```

## Các File Đã Sửa

### Backend
1. `clinic_backend/src/main/java/com/hcmute/clinic/controller/TreatmentPlanController.java`
   - Thêm endpoint `GET /api/treatment-plans/by-appointment/{appointmentId}`

2. `clinic_backend/src/main/java/com/hcmute/clinic/service/TreatmentPlanService.java`
   - Thêm method `findByAppointmentId()`

3. `clinic_backend/src/main/java/com/hcmute/clinic/repository/TreatmentPlanRepository.java`
   - Thêm query `findByAppointmentIdOrderByCreatedAtDesc()`

4. `clinic_backend/src/main/java/com/hcmute/clinic/service/PrescriptionService.java`
   - Enhanced validation logic để check cả 2 paths (direct và via medicalRecord)

### Android
1. `mobile_android/app/src/main/java/com/hcmute/mobile_android/network/ApiService.java`
   - Thêm method `getTreatmentPlanByAppointment()`

2. `mobile_android/app/src/main/java/com/hcmute/mobile_android/ui/activities/staff/PrescriptionActivity.java`
   - Sửa `loadTreatmentPlanSteps()` để dùng `getTreatmentPlanByAppointment()` thay vì `getTreatmentPlanForRoom()`
   - Sửa `onCreate()` để không require `treatmentPlanId` nữa

## Testing

### Test Case 1: Kê đơn với đúng Treatment Plan
1. Mở appointment ID 30
2. Load treatment plan thuộc appointment 30 (tự động load bằng appointmentId)
3. Chọn step và kê đơn
4. **Expected**: Thành công

### Test Case 2: Appointment không có Treatment Plan
1. Mở appointment mới chưa có treatment plan
2. **Expected**: App hiển thị "Không tải được phác đồ điều trị" và đóng màn hình

### Test Case 3: Multiple Treatment Plans cho cùng Appointment
1. Nếu có nhiều treatment plans cho cùng appointment
2. **Expected**: Load plan mới nhất (ORDER BY createdAt DESC)

### Test Case 4: Backward Compatibility
1. DoctorWorkflowActivity vẫn truyền treatmentPlanId (optional)
2. PrescriptionActivity ignore treatmentPlanId và dùng appointmentId
3. **Expected**: Hoạt động bình thường

## Lưu Ý Quan Trọng

1. **Guaranteed Correctness**: Load by appointmentId đảm bảo luôn lấy đúng treatment plan
2. **No Stale Data**: Không còn vấn đề cached treatmentPlanId từ appointment cũ
3. **Backward Compatible**: treatmentPlanId parameter vẫn tồn tại nhưng không bắt buộc
4. **Early Validation**: Kiểm tra ngay khi load treatment plan, không đợi đến khi submit
5. **Enhanced Backend**: Backend validation check cả 2 paths để tăng độ tin cậy

## Tác Động

- **Tích cực**: 
  - Ngăn chặn việc kê đơn sai appointment
  - Đảm bảo tính toàn vẹn dữ liệu
  - Giảm confusion cho bác sĩ
  - Code rõ ràng hơn (load by appointmentId thay vì treatmentPlanId)
  
- **Tiêu cực**: Không có

## Ngày Fix

2026-04-01

## Người Fix

Kiro AI Assistant
