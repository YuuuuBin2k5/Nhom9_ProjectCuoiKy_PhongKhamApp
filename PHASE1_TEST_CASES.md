# PHASE 1: TEST CASES

## 🧪 TEST SCENARIOS

### TEST 1: API getPatientByQr - Trả về TreatmentPlan info

**Endpoint:** `GET /api/doctor/patient?qr={qrData}`

**Test Case 1.1: Bệnh nhân chưa có TreatmentPlan**
```bash
# Request
curl -X GET "http://localhost:8080/api/doctor/patient?qr=123" \
  -H "Authorization: Bearer {doctor_token}"

# Expected Response
{
  "id": 1,
  "firstName": "Nguyen",
  "lastName": "Van A",
  "email": "patient@example.com",
  "phone": "0123456789",
  "bookedService": "Khám tổng quát",
  "appointmentStatus": "SCHEDULED",
  "queueId": 5,
  "appointmentId": 10,
  "treatmentPlanId": -1,           // ✅ NEW
  "hasTreatmentPlan": false,       // ✅ NEW
  "treatmentPlanStatus": "NONE"    // ✅ NEW
}
```

**Test Case 1.2: Bệnh nhân đã có TreatmentPlan**
```bash
# Request
curl -X GET "http://localhost:8080/api/doctor/patient?qr=456" \
  -H "Authorization: Bearer {doctor_token}"

# Expected Response
{
  "id": 2,
  "firstName": "Tran",
  "lastName": "Thi B",
  "email": "patient2@example.com",
  "phone": "0987654321",
  "bookedService": "Nhổ răng khôn",
  "appointmentStatus": "IN_PROGRESS",
  "queueId": 6,
  "appointmentId": 11,
  "treatmentPlanId": 3,                    // ✅ NEW - có plan
  "hasTreatmentPlan": true,                // ✅ NEW
  "treatmentPlanStatus": "IN_PROGRESS"     // ✅ NEW
}
```

**Verification:**
- ✅ Response có 3 fields mới
- ✅ `hasTreatmentPlan` = false khi chưa có plan
- ✅ `hasTreatmentPlan` = true khi đã có plan
- ✅ `treatmentPlanId` = -1 khi chưa có, > 0 khi có
- ✅ `treatmentPlanStatus` = "NONE" | "IN_PROGRESS" | "COMPLETED"

---

### TEST 2: Tạo TreatmentPlan từ Appointment

**Endpoint:** `POST /api/treatment-plans/from-appointment`

**Test Case 2.1: Tạo plan mới thành công**
```bash
# Request
curl -X POST "http://localhost:8080/api/treatment-plans/from-appointment" \
  -H "Authorization: Bearer {doctor_token}" \
  -H "Content-Type: application/json" \
  -d '{
    "appointmentId": 10,
    "templateId": 1
  }'

# Expected Response
{
  "id": 5,
  "patientId": 1,
  "appointmentId": 10,           // ✅ NEW - linked
  "medicalRecordId": 8,
  "status": "IN_PROGRESS",
  "isDraft": true,
  "steps": [...]
}
```

**Test Case 2.2: Appointment đã có plan active**
```bash
# Request
curl -X POST "http://localhost:8080/api/treatment-plans/from-appointment" \
  -H "Authorization: Bearer {doctor_token}" \
  -H "Content-Type: application/json" \
  -d '{
    "appointmentId": 11,
    "templateId": 1
  }'

# Expected Response (400 Bad Request)
{
  "message": "Lịch hẹn này đã có phác đồ điều trị đang hoạt động"
}
```

**Verification:**
- ✅ Tạo plan thành công khi appointment chưa có plan
- ✅ Tự động tạo MedicalRecord nếu chưa có
- ✅ Link plan với appointment
- ✅ Reject nếu appointment đã có plan active

---

### TEST 3: Complete Step - Không tự động sinh bước

**Endpoint:** `PATCH /api/treatment-plans/steps/{stepId}/complete`

**Test Case 3.1: Hoàn thành bước cuối cùng**
```bash
# Setup: Plan có 2 bước, bước 1 đã COMPLETED, bước 2 đang IN_PROGRESS

# Request
curl -X PATCH "http://localhost:8080/api/treatment-plans/steps/20/complete" \
  -H "Authorization: Bearer {doctor_token}" \
  -H "Content-Type: application/json" \
  -d '{
    "doctorConclusion": "Hoàn thành điều trị",
    "imageUrls": []
  }'

# Expected Response
{
  "message": "Đã hoàn thành bước điều trị"
  // ✅ KHÔNG có nextRoomName
}

# Verify in DB
SELECT status FROM treatment_plans WHERE id = 5;
-- Expected: COMPLETED

SELECT status FROM treatment_plan_steps WHERE id = 20;
-- Expected: COMPLETED

SELECT completed_at FROM treatment_plan_steps WHERE id = 20;
-- Expected: NOT NULL (timestamp)
```

**Test Case 3.2: Hoàn thành bước, còn bước tiếp theo**
```bash
# Setup: Plan có 3 bước, bước 1 đang IN_PROGRESS

# Request
curl -X PATCH "http://localhost:8080/api/treatment-plans/steps/21/complete" \
  -H "Authorization: Bearer {doctor_token}" \
  -H "Content-Type: application/json" \
  -d '{
    "doctorConclusion": "Hoàn thành bước 1",
    "imageUrls": ["uploads/xray1.jpg"]
  }'

# Expected Response
{
  "message": "Đã hoàn thành bước điều trị",
  "nextRoomName": "Phòng X-Quang"  // ✅ Nếu bước 2 ở phòng khác
}

# Verify in DB
SELECT status FROM treatment_plan_steps WHERE id = 21;
-- Expected: COMPLETED

SELECT status FROM treatment_plan_steps WHERE id = 22;
-- Expected: IN_PROGRESS (bước tiếp theo được kích hoạt)

SELECT COUNT(*) FROM treatment_plan_steps WHERE plan_id = 6;
-- Expected: 3 (KHÔNG tự động sinh bước mới)
```

**Test Case 3.3: Bác sĩ không có quyền hoàn thành bước**
```bash
# Setup: Bác sĩ ở Phòng A, bước thuộc Phòng B

# Request
curl -X PATCH "http://localhost:8080/api/treatment-plans/steps/23/complete" \
  -H "Authorization: Bearer {doctor_token_room_A}" \
  -H "Content-Type: application/json" \
  -d '{
    "doctorConclusion": "Test"
  }'

# Expected Response (403 Forbidden)
{
  "message": "Bạn không có quyền hoàn thành bước này. Bước này thuộc về phòng khác."
}
```

**Verification:**
- ✅ Hoàn thành bước cuối → Plan status = COMPLETED
- ✅ Hoàn thành bước giữa → Kích hoạt bước tiếp theo
- ✅ KHÔNG tự động sinh bước "Đọc kết quả"
- ✅ Lưu completedAt timestamp
- ✅ Lưu ảnh vào step_images table
- ✅ Kiểm tra quyền bác sĩ

---

### TEST 4: Prescription link với Step

**Endpoint:** `POST /api/prescriptions`

**Test Case 4.1: Kê đơn thuốc cho bước cụ thể**
```bash
# Request
curl -X POST "http://localhost:8080/api/prescriptions" \
  -H "Authorization: Bearer {doctor_token}" \
  -H "Content-Type: application/json" \
  -d '{
    "appointmentId": 10,
    "stepId": 21,                    // ✅ NEW - link với step
    "diagnosis": "Sâu răng",
    "symptoms": "Đau răng",
    "advice": "Uống thuốc đúng giờ",
    "details": [
      {
        "medicineName": "Paracetamol",
        "dosage": "500mg",
        "frequency": "3 lần/ngày",
        "duration": "5 ngày",
        "unit": "viên"
      }
    ]
  }'

# Expected Response
{
  "id": 10,
  "medicalRecordId": 8,
  "stepId": 21,                      // ✅ NEW
  "doctorId": 2,
  "doctorName": "Nguyen Van C",
  "createdAt": "2026-03-28T10:30:00",
  "details": [...]
}

# Verify in DB
SELECT step_id FROM prescriptions WHERE id = 10;
-- Expected: 21
```

**Verification:**
- ✅ Prescription có field stepId
- ✅ Link với TreatmentPlanStep
- ✅ Backward compatible (vẫn có medicalRecordId)

---

### TEST 5: Database Migration

**Test Case 5.1: Check new columns exist**
```sql
-- Run after migration
DESCRIBE treatment_plans;
-- Expected: appointment_id column exists

DESCRIBE prescriptions;
-- Expected: step_id column exists

DESCRIBE treatment_plan_steps;
-- Expected: completed_at column exists
```

**Test Case 5.2: Check indexes**
```sql
SHOW INDEXES FROM treatment_plans WHERE Key_name = 'idx_treatment_plans_appointment_id';
-- Expected: 1 row

SHOW INDEXES FROM prescriptions WHERE Key_name = 'idx_prescriptions_step_id';
-- Expected: 1 row
```

**Test Case 5.3: Check foreign keys**
```sql
SELECT CONSTRAINT_NAME, TABLE_NAME, REFERENCED_TABLE_NAME
FROM information_schema.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'clinic_db'
  AND CONSTRAINT_NAME = 'fk_treatment_plan_appointment';
-- Expected: 1 row
```

---

## 🔍 INTEGRATION TEST SCENARIO

### Scenario: Luồng khám bệnh hoàn chỉnh

**Step 1: Bệnh nhân check-in**
```bash
POST /api/checkin/self-scan
{
  "qrData": "CHECKIN:10"
}
# Expected: Queue created, appointmentId = 10
```

**Step 2: Bác sĩ quét QR bệnh nhân**
```bash
GET /api/doctor/patient?qr=CHECKIN:10
# Expected: 
# - appointmentId = 10
# - hasTreatmentPlan = false
# - treatmentPlanId = -1
```

**Step 3: Bác sĩ tạo phác đồ**
```bash
POST /api/treatment-plans/from-appointment
{
  "appointmentId": 10,
  "templateId": 1
}
# Expected: Plan created with 3 steps
```

**Step 4: Bác sĩ quét QR lại**
```bash
GET /api/doctor/patient?qr=CHECKIN:10
# Expected:
# - hasTreatmentPlan = true
# - treatmentPlanId = 5
# - treatmentPlanStatus = "IN_PROGRESS"
```

**Step 5: Hoàn thành bước 1**
```bash
PATCH /api/treatment-plans/steps/21/complete
{
  "doctorConclusion": "Khám xong"
}
# Expected:
# - Step 21 status = COMPLETED
# - Step 22 status = IN_PROGRESS
# - nextRoomName = "Phòng X-Quang"
```

**Step 6: Kê đơn thuốc cho bước 1**
```bash
POST /api/prescriptions
{
  "appointmentId": 10,
  "stepId": 21,
  "details": [...]
}
# Expected: Prescription linked to step 21
```

**Step 7: Hoàn thành bước 2**
```bash
PATCH /api/treatment-plans/steps/22/complete
{
  "doctorConclusion": "X-Quang xong"
}
# Expected:
# - Step 22 status = COMPLETED
# - Step 23 status = IN_PROGRESS
```

**Step 8: Hoàn thành bước 3 (cuối cùng)**
```bash
PATCH /api/treatment-plans/steps/23/complete
{
  "doctorConclusion": "Hoàn tất điều trị"
}
# Expected:
# - Step 23 status = COMPLETED
# - Plan status = COMPLETED
# - NO new step created
```

**Step 9: Verify không tạo duplicate plan**
```bash
POST /api/treatment-plans/from-appointment
{
  "appointmentId": 10,
  "templateId": 1
}
# Expected: 400 Bad Request
# "Lịch hẹn này đã có phác đồ điều trị đang hoạt động"
```

---

## 📊 PERFORMANCE TEST

### Test Case: Query performance với indexes

```sql
-- Test 1: Find plan by appointment (should use index)
EXPLAIN SELECT * FROM treatment_plans WHERE appointment_id = 10;
-- Expected: type = ref, key = idx_treatment_plans_appointment_id

-- Test 2: Find prescription by step (should use index)
EXPLAIN SELECT * FROM prescriptions WHERE step_id = 21;
-- Expected: type = ref, key = idx_prescriptions_step_id

-- Test 3: Find active plans for patient (should use index)
EXPLAIN SELECT * FROM treatment_plans 
WHERE patient_id = 1 AND status = 'IN_PROGRESS';
-- Expected: type = ref, key = idx_treatment_plans_patient_status
```

---

## ✅ TEST CHECKLIST

### Unit Tests
- [ ] TreatmentPlanService.createFromAppointment()
- [ ] TreatmentPlanService.completeStepAndAdvance() - no auto-generate
- [ ] DoctorController.getPatientByQr() - returns treatmentPlanId
- [ ] TreatmentPlanRepository.findByAppointmentId()

### Integration Tests
- [ ] Full workflow: Check-in → Scan → Create Plan → Complete Steps
- [ ] Duplicate plan prevention
- [ ] Prescription link with step
- [ ] Room transfer logic

### Database Tests
- [ ] Migration runs successfully
- [ ] Foreign keys work
- [ ] Indexes improve query performance
- [ ] No data loss after migration

### API Tests
- [ ] All endpoints return correct response format
- [ ] Error handling works (400, 403, 404)
- [ ] Authorization works (doctor only)

---

## 🐛 REGRESSION TEST

### Test các tính năng cũ vẫn hoạt động

- [ ] Tạo plan từ template (không có appointmentId) vẫn OK
- [ ] Kê đơn thuốc (không có stepId) vẫn OK
- [ ] Complete step với ảnh vẫn lưu được
- [ ] Chuyển phòng vẫn hoạt động
- [ ] Notification vẫn được gửi

---

**Last Updated:** 2026-03-28
**Status:** Ready for Testing
