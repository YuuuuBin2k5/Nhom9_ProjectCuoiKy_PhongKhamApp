# Fix Lỗi Không Kê Đơn Được - Prescription Validation Error

## Vấn Đề

Khi bác sĩ cố gắng kê đơn thuốc, hệ thống trả về lỗi 403:
```json
{
  "message": "Step/dịch vụ không thuộc lịch hẹn này. Vui lòng chọn đúng step hoặc bỏ trống để kê đơn chung.",
  "success": false
}
```

## Nguyên Nhân

**Root Cause**: Treatment Plan được load không thuộc về Appointment hiện tại.

### Chi Tiết Kỹ Thuật

1. **PrescriptionActivity** nhận 2 tham số:
   - `appointmentId`: ID của lịch hẹn hiện tại (ví dụ: 30)
   - `treatmentPlanId`: ID của phác đồ điều trị (ví dụ: plan có ID khác)

2. **Validation Logic** trong `PrescriptionService.java` (line 135-145):
   ```java
   if (step.getPlan().getAppointment().getId() != appointment.getId()) {
       throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
           "Step/dịch vụ không thuộc lịch hẹn này...");
   }
   ```

3. **Vấn đề**: Treatment Plan có thể được tạo từ appointment khác, nhưng app vẫn cho phép chọn và kê đơn → Validation fail.

## Giải Pháp

### 1. Thêm `appointmentId` vào TreatmentPlan Model

**Android Model** (`TreatmentPlan.java`):
```java
public class TreatmentPlan {
    private Long id;
    private Long patientId;
    private Long appointmentId; // ADDED
    // ... other fields
    
    public Long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Long appointmentId) { 
        this.appointmentId = appointmentId; 
    }
}
```

**Backend DTO** (`TreatmentPlanDTO.java`):
```java
@Data
@Builder
public class TreatmentPlanDTO {
    private Long id;
    private Long patientId;
    private Long appointmentId; // ADDED
    // ... other fields
}
```

### 2. Cập Nhật Backend Controller

**TreatmentPlanController.java** - Method `toDTO()`:
```java
return TreatmentPlanDTO.builder()
    .id(plan.getId())
    .patientId(plan.getPatient().getId())
    .appointmentId(plan.getAppointment() != null ? 
        plan.getAppointment().getId() : null) // ADDED
    // ... other fields
    .build();
```

### 3. Thêm Validation trong Android App

**PrescriptionActivity.java** - Method `loadTreatmentPlanSteps()`:
```java
TreatmentPlan plan = response.body();

// CRITICAL FIX: Validate that treatment plan belongs to current appointment
if (plan.getAppointmentId() != null && 
    !plan.getAppointmentId().equals(appointmentId)) {
    Toast.makeText(PrescriptionActivity.this, 
        "Phác đồ không thuộc lịch hẹn này. Vui lòng kiểm tra lại.", 
        Toast.LENGTH_LONG).show();
    finish();
    return;
}
```

## Các File Đã Sửa

### Backend
1. `clinic_backend/src/main/java/com/hcmute/clinic/dto/TreatmentPlanDTO.java`
   - Thêm field `appointmentId`

2. `clinic_backend/src/main/java/com/hcmute/clinic/controller/TreatmentPlanController.java`
   - Cập nhật method `toDTO()` để set `appointmentId`

### Android
1. `mobile_android/app/src/main/java/com/hcmute/mobile_android/network/models/TreatmentPlan.java`
   - Thêm field `appointmentId` và getter/setter

2. `mobile_android/app/src/main/java/com/hcmute/mobile_android/ui/activities/staff/PrescriptionActivity.java`
   - Thêm validation check trong `loadTreatmentPlanSteps()`

## Testing

### Test Case 1: Kê đơn với đúng Treatment Plan
1. Mở appointment ID 30
2. Load treatment plan thuộc appointment 30
3. Chọn step và kê đơn
4. **Expected**: Thành công

### Test Case 2: Kê đơn với sai Treatment Plan
1. Mở appointment ID 30
2. Nếu treatment plan thuộc appointment khác (ví dụ: appointment 25)
3. **Expected**: App hiển thị lỗi và đóng màn hình ngay lập tức

### Test Case 3: Treatment Plan không có appointmentId
1. Dữ liệu cũ có thể không có appointmentId
2. **Expected**: Validation bỏ qua (null check)

## Lưu Ý Quan Trọng

1. **Backward Compatibility**: Validation có null check để tương thích với dữ liệu cũ
2. **Early Validation**: Kiểm tra ngay khi load treatment plan, không đợi đến khi submit
3. **User Experience**: Hiển thị thông báo rõ ràng và đóng màn hình để tránh nhầm lẫn

## Tác Động

- **Tích cực**: Ngăn chặn việc kê đơn sai appointment, đảm bảo tính toàn vẹn dữ liệu
- **Tiêu cực**: Không có (chỉ thêm validation logic)

## Ngày Fix

2026-04-01

## Người Fix

Kiro AI Assistant
