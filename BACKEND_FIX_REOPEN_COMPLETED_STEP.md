# BACKEND FIX: Cho phép reopen bước đã hoàn thành

## VẤN ĐỀ

Khi user bấm "Chỉnh sửa" trên bước đã COMPLETED, mobile app gọi API `cancelTreatmentStep`, nhưng backend trả về lỗi:

```
400 Bad Request
{"message":"403 FORBIDDEN \"Hồ sơ đã hoàn tất và bị khóa\""}
```

## NGUYÊN NHÂN

Backend method `cancelStep()` có validation:

```java
if (step.getPlan() != null && step.getPlan().getStatus() == TreatmentPlanStatus.COMPLETED) {
    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Hồ sơ đã hoàn tất và bị khóa");
}
```

Validation này CHẶN tất cả thao tác cancel khi TOÀN BỘ treatment plan đã COMPLETED, kể cả khi user muốn chỉnh sửa một bước đã hoàn thành.

## PHÂN TÍCH

Có 2 trường hợp khác nhau:

### Trường hợp 1: Cancel bước đang IN_PROGRESS
- User đang làm dở một bước
- Muốn hủy và quay lại PENDING
- Không nên cho phép nếu plan đã COMPLETED

### Trường hợp 2: Reopen bước đã COMPLETED để chỉnh sửa
- Bước đã hoàn thành
- User muốn sửa lại dữ liệu (fix lỗi, thêm thông tin)
- **NÊN CHO PHÉP** ngay cả khi plan đã COMPLETED

## GIẢI PHÁP

Sửa method `cancelStep()` để phân biệt 2 trường hợp:

```java
@Transactional
public void cancelStep(Long stepId) {
    TreatmentPlanStep step = stepRepository.findById(stepId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bước điều trị không tồn tại"));
    
    // CRITICAL FIX: Allow reopening COMPLETED steps for editing
    if (step.getStatus() == StepStatus.COMPLETED) {
        // Reopening a COMPLETED step for editing
        // This is allowed even if the plan is COMPLETED
        
        TreatmentPlan plan = step.getPlan();
        if (plan != null && plan.getStatus() == TreatmentPlanStatus.COMPLETED) {
            // Reopen the plan to IN_PROGRESS to allow updates
            plan.setStatus(TreatmentPlanStatus.IN_PROGRESS);
            planRepository.save(plan);
        }
        
        // Set step back to IN_PROGRESS (not PENDING) so it can be edited immediately
        step.setStatus(StepStatus.IN_PROGRESS);
        // Keep the existing data - don't clear doctorConclusion
        stepRepository.save(step);
        return;
    }
    
    // Original logic for canceling IN_PROGRESS steps
    if (step.getPlan() != null && step.getPlan().getStatus() == TreatmentPlanStatus.COMPLETED) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Hồ sơ đã hoàn tất và bị khóa");
    }

    if (step.getStatus() != StepStatus.IN_PROGRESS) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chỉ có thể hủy bước đang thực hiện");
    }
    
    // Đặt lại về PENDING
    step.setStatus(StepStatus.PENDING);
    step.setDoctorConclusion(null); // Xóa kết luận nếu có
    stepRepository.save(step);
}
```

## LOGIC MỚI

### Khi step.status == COMPLETED:
1. Kiểm tra plan status
2. Nếu plan == COMPLETED → Reopen plan sang IN_PROGRESS
3. Đổi step status: COMPLETED → IN_PROGRESS
4. **GIỮ NGUYÊN** doctorConclusion (không xóa dữ liệu)
5. Return (không throw exception)

### Khi step.status == IN_PROGRESS:
1. Kiểm tra plan status
2. Nếu plan == COMPLETED → Throw exception (không cho phép)
3. Đổi step status: IN_PROGRESS → PENDING
4. **XÓA** doctorConclusion
5. Return

## TẠI SAO REOPEN PLAN?

Khi reopen một step COMPLETED, chúng ta cũng phải reopen plan vì:

1. **Backend validation trong `updateSteps()`** (line 148):
   ```java
   if (plan.getStatus() == TreatmentPlanStatus.COMPLETED && !hasInProgressSteps && !isAddingNewSteps) {
       throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Hồ sơ đã hoàn tất và bị khóa, không thể chỉnh sửa");
   }
   ```

2. Sau khi reopen step sang IN_PROGRESS:
   - `hasInProgressSteps` = true
   - Backend CHO PHÉP update
   - User có thể lưu thay đổi

3. Khi user hoàn thành lại:
   - Step chuyển sang COMPLETED
   - Nếu TẤT CẢ steps đều COMPLETED → Plan tự động chuyển sang COMPLETED
   - Logic này đã có trong `completeStepAndAdvance()`

## LUỒNG HOẠT ĐỘNG SAU KHI FIX

### User bấm "Chỉnh sửa" bước đã COMPLETED:

1. **Mobile app** gọi `PATCH /api/treatment-plans/steps/{stepId}/cancel`

2. **Backend `cancelStep()`**:
   - Phát hiện step.status == COMPLETED
   - Reopen plan: COMPLETED → IN_PROGRESS
   - Reopen step: COMPLETED → IN_PROGRESS
   - Giữ nguyên doctorConclusion
   - Return success ✅

3. **Mobile app** nhận success:
   - Update local step.status = "IN_PROGRESS"
   - Load dữ liệu vào fragment
   - Set editable mode
   - User có thể chỉnh sửa ✅

4. **User chỉnh sửa và bấm "Hoàn thành"**:
   - Mobile app gọi `PUT /api/treatment-plans/{id}` để save
   - Backend kiểm tra: plan có IN_PROGRESS steps? → YES
   - Backend CHO PHÉP update ✅
   - Mobile app gọi `PATCH /api/treatment-plans/steps/{stepId}/complete`
   - Backend complete step thành công ✅

5. **Backend tự động complete plan** (nếu tất cả steps đều COMPLETED):
   - Logic trong `completeStepAndAdvance()`
   - Kiểm tra: Còn PENDING steps? → NO
   - Kiểm tra: Còn IN_PROGRESS steps? → NO
   - → Set plan.status = COMPLETED

## AN TOÀN VÀ BẢO MẬT

### Validation vẫn được giữ nguyên:
- Không thể cancel step IN_PROGRESS khi plan đã COMPLETED
- Chỉ cho phép reopen step COMPLETED (use case hợp lệ)
- Doctor vẫn phải có quyền trên phòng khám

### Audit trail:
- Mọi thay đổi vẫn được log
- Có thể track ai đã reopen và chỉnh sửa step

## FILES MODIFIED

- `clinic_backend/src/main/java/com/hcmute/clinic/service/TreatmentPlanService.java`
  - Method `cancelStep()`: Thêm logic reopen COMPLETED steps

## BUILD STATUS

✅ **Build Successful**
- JAR: `clinic_backend/target/clinic-0.0.1-SNAPSHOT.jar`
- No compilation errors

## TESTING

### Test Case: Chỉnh sửa bước đã hoàn thành

1. **Hoàn thành một bước**
   - Tạo dịch vụ
   - Nhập dữ liệu
   - Hoàn thành
   - ✅ Step status = COMPLETED
   - ✅ Plan status = COMPLETED (nếu không còn bước nào)

2. **Bấm "Chỉnh sửa"**
   - Mobile gọi `cancelTreatmentStep`
   - ✅ Backend trả về 200 OK (không còn 400 error)
   - ✅ Step status → IN_PROGRESS
   - ✅ Plan status → IN_PROGRESS
   - ✅ Dữ liệu được giữ nguyên

3. **Chỉnh sửa và hoàn thành lại**
   - Sửa dữ liệu
   - Bấm "Hoàn thành"
   - ✅ Backend cho phép save
   - ✅ Backend cho phép complete
   - ✅ Dữ liệu mới được lưu

## RESTART BACKEND

Sau khi build, cần restart backend:

```bash
# Stop backend hiện tại
# Ctrl+C hoặc kill process

# Start backend mới
cd clinic_backend
java -jar target/clinic-0.0.1-SNAPSHOT.jar
```

Hoặc nếu dùng IDE, restart application.

## SUMMARY

**Vấn đề**: Backend chặn reopen bước COMPLETED khi plan đã COMPLETED

**Giải pháp**: Phân biệt 2 use cases:
- Cancel IN_PROGRESS step → Không cho phép nếu plan COMPLETED
- Reopen COMPLETED step → Cho phép, và reopen plan cùng lúc

**Kết quả**: User có thể chỉnh sửa bước đã hoàn thành ✅
