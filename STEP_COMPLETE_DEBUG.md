# DEBUG: STEP 1 KHÔNG CẬP NHẬT SAU KHI HOÀN THÀNH

## 🐛 VẤN ĐỀ

Khi nhấn "Hoàn thành" cho step 1, hệ thống không cập nhật status của step 1 thành COMPLETED, mà chỉ hiển thị step 2 (X-quang) luôn.

## 🔍 PHÂN TÍCH LOG

### Queries thấy trong log:
```sql
-- Load treatment plan
SELECT tp1_0.*, s1_0.* FROM treatment_plans tp1_0 
LEFT JOIN treatment_plan_steps s1_0 ...

-- Load patient queue
SELECT ciq1_0.* FROM check_in_queue ciq1_0 
WHERE p1_0.id=? AND ciq1_0.check_in_time>=? ...

-- Load patient profile
SELECT pp1_0.* FROM patient_profiles pp1_0 ...
```

### Queries KHÔNG thấy:
```sql
-- ❌ MISSING: Update step 1 status
UPDATE treatment_plan_steps SET status='COMPLETED', completed_at=? WHERE id=?

-- ❌ MISSING: Update step 2 status  
UPDATE treatment_plan_steps SET status='IN_PROGRESS' WHERE id=?

-- ❌ MISSING: Update queue room
UPDATE check_in_queue SET clinic_room_id=?, status='WAITING', priority_level=? WHERE id=?
```

## 🎯 NGUYÊN NHÂN CÓ THỂ

### 1. API completeTreatmentStep không được gọi
- Mobile có gọi `apiService.completeTreatmentStep(step.getId(), body)` không?
- Response có thành công không?
- Có lỗi network không?

### 2. Backend có exception
- Transaction bị rollback do lỗi
- Exception trong `completeStepAndAdvance()`
- Validation failed (ví dụ: doctor không có quyền)

### 3. Step ID null hoặc không đúng
- Step được thêm thủ công có ID chưa?
- Auto-save có chạy thành công không?
- Reload plan có cập nhật IDs không?

## 🔧 CÁCH DEBUG

### Bước 1: Kiểm tra mobile có gọi API không

Thêm log trong `onStepComplete()`:

```java
@Override
public void onStepComplete(TreatmentPlan.Step step) {
    Log.d("DoctorWorkflow", "onStepComplete called for step: " + step.getId() + " - " + step.getServiceName());
    
    if (step.getId() == null) {
        Log.e("DoctorWorkflow", "Step ID is NULL!");
        Toast.makeText(this, "Lỗi: Bước chưa được lưu vào hệ thống. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
        return;
    }
    
    // ... rest of code
    
    apiService.completeTreatmentStep(step.getId(), body).enqueue(new Callback<MessageResponse>() {
        @Override
        public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
            Log.d("DoctorWorkflow", "Complete API response: " + response.code());
            if (response.isSuccessful() && response.body() != null) {
                Log.d("DoctorWorkflow", "Next room: " + response.body().getNextRoomName());
                // ... rest of code
            } else {
                Log.e("DoctorWorkflow", "Complete API failed: " + response.errorBody());
            }
        }
        
        @Override
        public void onFailure(Call<MessageResponse> call, Throwable t) {
            Log.e("DoctorWorkflow", "Complete API error: " + t.getMessage());
        }
    });
}
```

### Bước 2: Kiểm tra backend có exception không

Thêm log trong `TreatmentPlanService.completeStepAndAdvance()`:

```java
@Transactional
public String completeStepAndAdvance(...) {
    log.info("completeStepAndAdvance called for stepId: {}", stepId);
    
    TreatmentPlanStep currentStep = stepRepository.findById(stepId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bước không tồn tại"));
    
    log.info("Current step found: {} - {}", currentStep.getId(), currentStep.getService().getName());
    log.info("Current step status: {}", currentStep.getStatus());
    
    // ... validation checks
    
    // Hoàn thành bước hiện tại
    currentStep.setStatus(StepStatus.COMPLETED);
    currentStep.setCompletedAt(java.time.LocalDateTime.now());
    log.info("Setting step {} to COMPLETED", currentStep.getId());
    
    stepRepository.save(currentStep);
    log.info("Step {} saved as COMPLETED", currentStep.getId());
    
    // Tìm bước tiếp theo
    TreatmentPlanStep nextStep = plan.getSteps().stream()
            .filter(s -> s.getStatus() == StepStatus.PENDING)
            .min(Comparator.comparingInt(s -> s.getSequenceOrder() != null ? s.getSequenceOrder() : 0))
            .orElse(null);
    
    if (nextStep != null) {
        log.info("Next step found: {} - {}", nextStep.getId(), nextStep.getService().getName());
        nextStep.setStatus(StepStatus.IN_PROGRESS);
        stepRepository.save(nextStep);
        log.info("Next step {} set to IN_PROGRESS", nextStep.getId());
        
        ClinicRoom nextRoom = nextStep.getClinicRoom();
        if (nextRoom != null) {
            log.info("Next room: {}", nextRoom.getName());
            // ... chuyển phòng logic
            return nextRoom.getName();
        }
    }
    
    return null;
}
```

### Bước 3: Kiểm tra transaction có commit không

Thêm log sau khi transaction complete:

```java
@PostMapping("/steps/{stepId}/complete")
public ResponseEntity<?> completeStep(@PathVariable Long stepId, @RequestBody Map<String, Object> body) {
    try {
        log.info("Complete step API called for stepId: {}", stepId);
        
        String nextRoomName = treatmentPlanService.completeStepAndAdvance(...);
        
        log.info("Complete step successful, next room: {}", nextRoomName);
        
        return ResponseEntity.ok(new MessageResponse("Hoàn tất bước", nextRoomName));
    } catch (Exception e) {
        log.error("Complete step failed: ", e);
        throw e;
    }
}
```

## 🧪 TEST CASE

### Scenario: Complete step 1 với step 2 là X-quang

1. **Setup:**
   - Bệnh nhân có treatment plan với 2 steps:
     - Step 1: Khám và tư vấn (Phòng 01) - IN_PROGRESS
     - Step 2: Chụp X-quang (Phòng X-quang) - PENDING

2. **Action:**
   - Bác sĩ Phòng 01 nhấn "Hoàn thành" cho step 1

3. **Expected backend behavior:**
   ```sql
   -- 1. Update step 1
   UPDATE treatment_plan_steps 
   SET status='COMPLETED', completed_at=NOW() 
   WHERE id=<step1_id>;
   
   -- 2. Update step 2
   UPDATE treatment_plan_steps 
   SET status='IN_PROGRESS' 
   WHERE id=<step2_id>;
   
   -- 3. Update queue
   UPDATE check_in_queue 
   SET clinic_room_id=<xray_room_id>, status='WAITING', priority_level=priority_level+5 
   WHERE id=<queue_id>;
   
   -- 4. Insert notification
   INSERT INTO notifications ...
   ```

4. **Expected mobile behavior:**
   - API response: `{ "message": "...", "nextRoomName": "Phòng X-quang" }`
   - Dialog hiển thị: "Bệnh nhân cần được chuyển sang Phòng X-quang..."
   - Activity finish (đóng màn hình)

5. **Expected database state:**
   ```sql
   -- Step 1
   SELECT * FROM treatment_plan_steps WHERE id=<step1_id>;
   -- status='COMPLETED', completed_at IS NOT NULL
   
   -- Step 2
   SELECT * FROM treatment_plan_steps WHERE id=<step2_id>;
   -- status='IN_PROGRESS'
   
   -- Queue
   SELECT * FROM check_in_queue WHERE id=<queue_id>;
   -- clinic_room_id=<xray_room_id>, status='WAITING', priority_level=5
   ```

## 💡 GIẢI PHÁP TẠM THỜI

Nếu không tìm ra nguyên nhân, có thể:

1. **Restart backend** - Clear cache, reload data
2. **Check database** - Xem step có ID không, status là gì
3. **Test với Postman** - Gọi API trực tiếp để loại trừ vấn đề mobile
4. **Enable SQL logging** - Xem tất cả queries được execute

```yaml
# application.yml
logging:
  level:
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

## ❓ CÂU HỎI CẦN TRẢ LỜI

1. Mobile có hiển thị dialog "Chuyển phòng" không?
2. Có thông báo lỗi nào không?
3. Backend log có exception không?
4. Database có UPDATE queries không?
5. Step 1 có ID không? (check bằng cách xem log hoặc database)

---

**Next step:** Thêm logging và test lại để xác định chính xác nguyên nhân.

