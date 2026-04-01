# Hướng Dẫn Test Fix Lỗi Kê Đơn

## Chuẩn Bị

### 1. Build và Deploy

**Backend:**
```bash
cd clinic_backend
./mvnw clean install
./mvnw spring-boot:run
```

**Android:**
```bash
cd mobile_android
./gradlew clean assembleDebug
# Hoặc build từ Android Studio
```

### 2. Dữ Liệu Test

Cần có:
- Ít nhất 2 appointments với IDs khác nhau (ví dụ: 25, 30)
- Mỗi appointment có treatment plan riêng
- Treatment plans có steps với services

## Test Cases

### Test Case 1: Kê Đơn Thành Công (Happy Path)

**Mục đích**: Xác nhận kê đơn hoạt động bình thường khi treatment plan đúng appointment

**Các bước:**
1. Login với tài khoản bác sĩ (ID: 6)
2. Vào màn hình Queue Management
3. Chọn bệnh nhân có appointment ID 30
4. Click "Kê đơn" để mở PrescriptionActivity
5. Kiểm tra:
   - Màn hình load thành công
   - Spinner hiển thị danh sách services từ treatment plan
   - Có thể chọn service
6. Bật switch "Kê đơn cho dịch vụ này"
7. Nhập thông tin:
   - Chẩn đoán: "Thuốc đau"
   - Triệu chứng: "răng khôn"
   - Lời khuyên: "đều đặn"
   - Số tiền: 0
   - Thuốc: "thuốc c1", liều lượng "1", tần suất "sáng", thời gian "7", đơn vị "vỉ"
8. Click "Lưu Đơn Thuốc"

**Expected Result:**
- Toast hiển thị: "Lưu đơn thuốc thành công"
- Màn hình đóng và quay về màn hình trước
- Không có lỗi 403

**Log kiểm tra:**
```
POST http://192.168.1.6:8081/api/prescriptions
Response: 200 OK
```

---

### Test Case 2: Phát Hiện Treatment Plan Sai Appointment

**Mục đích**: Xác nhận validation ngăn chặn kê đơn với treatment plan không đúng

**Setup:**
1. Tạo appointment ID 30 với treatment plan A
2. Tạo appointment ID 25 với treatment plan B
3. Modify code tạm thời để pass treatment plan B vào PrescriptionActivity khi mở từ appointment 30

**Các bước:**
1. Login với tài khoản bác sĩ
2. Vào màn hình Queue Management
3. Chọn bệnh nhân có appointment ID 30
4. Click "Kê đơn" (nhưng pass treatmentPlanId của appointment 25)

**Expected Result:**
- Toast hiển thị: "Phác đồ không thuộc lịch hẹn này. Vui lòng kiểm tra lại."
- Màn hình PrescriptionActivity đóng ngay lập tức
- Không gọi API prescription

**Log kiểm tra:**
```
Loaded TreatmentPlan: appointmentId=25
Current appointmentId=30
Validation failed: appointmentId mismatch
Activity finished
```

---

### Test Case 3: Treatment Plan Không Có AppointmentId (Legacy Data)

**Mục đích**: Xác nhận backward compatibility với dữ liệu cũ

**Setup:**
1. Trong database, set `appointment_id = NULL` cho một treatment plan
2. Hoặc mock API response với `appointmentId: null`

**Các bước:**
1. Login với tài khoản bác sĩ
2. Mở PrescriptionActivity với treatment plan có appointmentId = null

**Expected Result:**
- Validation bỏ qua (null check)
- Màn hình load bình thường
- Có thể kê đơn (nhưng có thể fail ở backend validation)

**Lưu ý**: Đây là trường hợp edge case, nên cảnh báo user cập nhật dữ liệu

---

### Test Case 4: Kiểm Tra API Response

**Mục đích**: Xác nhận backend trả về appointmentId trong TreatmentPlanDTO

**Các bước:**
1. Gọi API: `GET /api/treatment-plans/{id}/for-room`
2. Kiểm tra response JSON

**Expected Response:**
```json
{
  "id": 1,
  "patientId": 2,
  "appointmentId": 30,  // <-- MUST HAVE THIS FIELD
  "status": "IN_PROGRESS",
  "isDraft": false,
  "steps": [...]
}
```

**Kiểm tra:**
- Field `appointmentId` có mặt
- Giá trị đúng với appointment của treatment plan
- Không null (trừ khi dữ liệu cũ)

---

### Test Case 5: Multiple Steps - Kê Đơn Từng Service

**Mục đích**: Xác nhận kê đơn cho nhiều services trong cùng treatment plan

**Các bước:**
1. Mở PrescriptionActivity với treatment plan có 3 steps
2. Chọn step 1, kê đơn, lưu thành công
3. Chọn step 2, kê đơn, lưu thành công
4. Chọn step 3, kê đơn, lưu thành công

**Expected Result:**
- Mỗi step có thể kê đơn riêng
- Dữ liệu không bị ghi đè
- Tất cả đều validate đúng appointmentId

---

## Regression Testing

### Các chức năng cần test lại:

1. **Load Treatment Plan**
   - Từ DoctorWorkflowActivity
   - Từ PatientDetailActivity
   - Từ Queue Management

2. **Create Treatment Plan**
   - Từ template
   - Từ appointment
   - Kiểm tra appointmentId được set đúng

3. **Update Treatment Plan**
   - Cập nhật steps
   - Complete steps
   - Kiểm tra appointmentId không bị thay đổi

## Debugging Tips

### 1. Check Logcat

**Android:**
```
adb logcat | grep -i "prescription\|treatment"
```

Tìm:
- "Loaded TreatmentPlan: appointmentId=..."
- "Validation failed: appointmentId mismatch"
- API request/response logs

### 2. Check Backend Logs

```bash
tail -f clinic_backend/logs/spring.log | grep -i "prescription\|treatment"
```

Tìm:
- "Step {} does not belong to appointment {}"
- SQL queries với treatment_plan_step
- Validation errors

### 3. Database Queries

```sql
-- Kiểm tra treatment plan có appointmentId
SELECT id, patient_id, appointment_id, status 
FROM treatment_plan 
WHERE id = 1;

-- Kiểm tra steps thuộc treatment plan nào
SELECT tps.id, tps.treatment_plan_id, tp.appointment_id, s.name
FROM treatment_plan_step tps
JOIN treatment_plan tp ON tps.treatment_plan_id = tp.id
JOIN service s ON tps.service_id = s.id
WHERE tps.id = 4;

-- Kiểm tra prescription details
SELECT pd.id, pd.treatment_plan_step_id, pd.medicine_name
FROM prescription_detail pd
WHERE pd.prescription_id = 1;
```

## Known Issues & Workarounds

### Issue 1: Treatment Plan Không Có AppointmentId

**Triệu chứng**: Dữ liệu cũ không có appointmentId

**Workaround**: Chạy migration script
```sql
UPDATE treatment_plan tp
SET appointment_id = (
    SELECT mr.appointment_id 
    FROM medical_record mr 
    WHERE mr.id = tp.medical_record_id
)
WHERE appointment_id IS NULL;
```

### Issue 2: Multiple Treatment Plans Cho Cùng Appointment

**Triệu chứng**: Một appointment có nhiều treatment plans

**Workaround**: Chọn treatment plan mới nhất
```java
TreatmentPlan plan = treatmentPlanRepository
    .findFirstByAppointmentIdOrderByCreatedAtDesc(appointmentId)
    .orElseThrow();
```

## Success Criteria

✅ Test Case 1 pass: Kê đơn thành công với đúng treatment plan
✅ Test Case 2 pass: Validation chặn treatment plan sai appointment
✅ Test Case 3 pass: Backward compatibility với dữ liệu cũ
✅ Test Case 4 pass: API trả về appointmentId
✅ Test Case 5 pass: Kê đơn nhiều steps
✅ Không có regression issues
✅ Không có lỗi 403 khi kê đơn đúng

## Rollback Plan

Nếu có vấn đề:

1. **Revert Android changes:**
   ```bash
   git revert <commit-hash>
   ```

2. **Revert Backend changes:**
   ```bash
   git revert <commit-hash>
   ./mvnw clean install
   ```

3. **Remove appointmentId field** (nếu cần):
   - Xóa field trong TreatmentPlanDTO
   - Xóa field trong TreatmentPlan model
   - Remove validation trong PrescriptionActivity

## Contact

Nếu có vấn đề, liên hệ:
- Developer: Kiro AI Assistant
- Date: 2026-04-01
