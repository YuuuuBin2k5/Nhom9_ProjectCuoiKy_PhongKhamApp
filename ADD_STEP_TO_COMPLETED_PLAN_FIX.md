# ✅ FIX: CHO PHÉP THÊM STEP VÀO PLAN ĐÃ HOÀN THÀNH

## 🎯 VẤN ĐỀ ĐÃ KHẮC PHỤC

**Vấn đề:** Khi hoàn thành step 1 (Khám tổng quát), treatment plan tự động chuyển sang COMPLETED. Sau đó khi thêm step 2 (X-quang), backend trả về lỗi 400:

```
{"message":"Hồ sơ đã hoàn tất và bị khóa, không thể cập nhật"}
```

**Nguyên nhân:** 
- Logic cũ: Plan COMPLETED bị khóa hoàn toàn, không cho phép thêm step mới
- Trong thực tế: Bác sĩ có thể phát hiện cần thêm dịch vụ sau khi hoàn thành step đầu tiên

## 🔧 GIẢI PHÁP ĐÃ TRIỂN KHAI

### Thay đổi trong `TreatmentPlanService.updateSteps()`:

#### 1. Cho phép thêm step mới vào plan đã COMPLETED:

```java
// Allow updates if plan is COMPLETED but has IN_PROGRESS steps (parallel workflow)
// OR if adding new steps (doctor can add more services after completing initial steps)
boolean hasInProgressSteps = plan.getSteps() != null && plan.getSteps().stream()
        .anyMatch(s -> s.getStatus() == StepStatus.IN_PROGRESS);

boolean isAddingNewSteps = request != null && request.getSteps() != null && 
        request.getSteps().stream().anyMatch(s -> s.getId() == null);

if (plan.getStatus() == TreatmentPlanStatus.COMPLETED && !hasInProgressSteps && !isAddingNewSteps) {
    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Hồ sơ đã hoàn tất và bị khóa, không thể chỉnh sửa");
}
```

#### 2. Tự động reopen plan khi thêm step mới:

```java
// If plan was COMPLETED but we added new PENDING steps, reopen it to IN_PROGRESS
if (plan.getStatus() == TreatmentPlanStatus.COMPLETED && isAddingNewSteps) {
    boolean hasNewPendingSteps = existingSteps.stream()
            .anyMatch(s -> s.getStatus() == StepStatus.PENDING);
    if (hasNewPendingSteps) {
        plan.setStatus(TreatmentPlanStatus.IN_PROGRESS);
    }
}
```

### Cơ chế hoạt động:

1. **Bước 1:** Bác sĩ hoàn thành step 1 (Khám tổng quát)
   - Step 1: COMPLETED
   - Plan: COMPLETED (vì không còn step PENDING nào)

2. **Bước 2:** Bác sĩ thêm step 2 (X-quang)
   - Request có `id: null` → `isAddingNewSteps = true`
   - Backend cho phép update vì `isAddingNewSteps = true`
   - Step 2 được tạo với status: PENDING
   - Plan tự động chuyển về: IN_PROGRESS

3. **Bước 3:** Bác sĩ hoàn thành step 2
   - Step 2: COMPLETED
   - Plan: COMPLETED (vì tất cả steps đã COMPLETED)

## 📋 HƯỚNG DẪN TEST

### Chuẩn bị:
```bash
# 1. Stop backend hiện tại (Ctrl+C)

# 2. Start backend mới
cd clinic_backend
java -jar target/clinic-0.0.1-SNAPSHOT.jar

# 3. Kiểm tra backend đã chạy
curl http://localhost:8081/actuator/health
```

### Kịch bản test:

#### Test Case 1: Thêm step vào plan đã COMPLETED
1. Đăng nhập app Android với tài khoản bác sĩ
2. Vào Queue, chọn bệnh nhân
3. Tạo treatment plan với 1 step: "Khám tổng quát"
4. Hoàn thành step "Khám tổng quát"
   - **Kỳ vọng:** Plan chuyển sang COMPLETED
5. Nhấn "Thêm dịch vụ", chọn "X-quang"
   - **Kỳ vọng:** 
     - ✅ Step X-quang được thêm thành công
     - ✅ Plan tự động chuyển về IN_PROGRESS
     - ✅ KHÔNG có lỗi "Hồ sơ đã hoàn tất và bị khóa"

#### Test Case 2: Thêm nhiều step vào plan đã COMPLETED
1. Từ Test Case 1, hoàn thành step "X-quang"
   - **Kỳ vọng:** Plan chuyển sang COMPLETED
2. Thêm step "Nhổ răng"
   - **Kỳ vọng:** Step được thêm, plan chuyển về IN_PROGRESS
3. Thêm step "Niềng răng"
   - **Kỳ vọng:** Step được thêm, plan vẫn IN_PROGRESS
4. Hoàn thành cả 2 step
   - **Kỳ vọng:** Plan chuyển sang COMPLETED

#### Test Case 3: Không cho phép chỉnh sửa step đã COMPLETED
1. Tạo plan với 1 step, hoàn thành step đó
2. Cố gắng chỉnh sửa step đã COMPLETED (không thêm step mới)
   - **Kỳ vọng:** Backend vẫn từ chối (vì không phải thêm step mới)

## 🔍 BACKEND LOG KIỂM TRA

Khi thêm step vào plan đã COMPLETED, backend log sẽ hiện:

```
Hibernate: select ... from treatment_plans tp1_0 where tp1_0.id=?
Hibernate: update treatment_plans set status='IN_PROGRESS' where id=?
Hibernate: insert into treatment_plan_steps (...) values (...)
```

Nếu thấy `status='IN_PROGRESS'` → Fix hoạt động đúng!

## 📊 KẾT QUẢ BUILD

```
BUILD SUCCESS
Total time: 14.024 s
```

## 📁 FILES MODIFIED

- `clinic_backend/src/main/java/com/hcmute/clinic/service/TreatmentPlanService.java`
  - Method: `updateSteps()`
  - Changes:
    1. Added `isAddingNewSteps` check
    2. Allow updates when adding new steps to COMPLETED plan
    3. Auto-reopen plan to IN_PROGRESS when adding new PENDING steps

## 🎉 TÍNH NĂNG HOÀN CHỈNH

Giờ workflow linh hoạt hơn:

✅ Bác sĩ có thể hoàn thành step đầu tiên
✅ Plan tự động chuyển sang COMPLETED nếu không còn step nào
✅ Bác sĩ có thể thêm step mới vào plan đã COMPLETED
✅ Plan tự động reopen về IN_PROGRESS khi có step mới
✅ Workflow tiếp tục bình thường

## ⚠️ LƯU Ý

- Plan chỉ bị khóa hoàn toàn khi:
  - Status = COMPLETED
  - Không có step IN_PROGRESS nào
  - Không thêm step mới (chỉ chỉnh sửa step cũ)
  
- Plan sẽ tự động reopen khi:
  - Thêm step mới (id = null)
  - Step mới có status = PENDING

---

**Ngày hoàn thành:** 2026-03-29
**Build status:** ✅ SUCCESS
**Backend restart:** ⚠️ REQUIRED
