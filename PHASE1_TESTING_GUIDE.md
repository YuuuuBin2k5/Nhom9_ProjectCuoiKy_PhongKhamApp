# PHASE 1: TESTING GUIDE

## 📋 PRE-REQUISITES

Trước khi test, đảm bảo:
- ✅ Đã thay thế method `completeStepAndAdvance()` trong TreatmentPlanService
- ✅ Backend đang chạy (port 8080)
- ✅ Database đã được migrate
- ✅ Có dữ liệu test (patients, doctors, appointments)

---

## 🚀 BƯỚC 1: RUN DATABASE MIGRATION

### Option 1: Sử dụng Flyway (Recommended)
```bash
cd clinic_backend

# Check migration status
./mvnw flyway:info

# Run migration
./mvnw flyway:migrate

# Verify
./mvnw flyway:info
```

### Option 2: Manual SQL
```bash
# Connect to database
mysql -u root -p clinic_db

# Run migration script
source src/main/resources/db/migration/V1__phase1_critical_fixes.sql

# Verify columns
DESCRIBE treatment_plans;
DESCRIBE prescriptions;
DESCRIBE treatment_plan_steps;
```

### Option 3: JPA Auto-Update (Development only)
```properties
# application.properties
spring.jpa.hibernate.ddl-auto=update
```
Restart application, JPA will auto-create columns.

---

## 🧪 BƯỚC 2: COMPILE & RUN BACKEND

```bash
cd clinic_backend

# Clean and compile
./mvnw clean compile

# Check for compilation errors
# Expected: BUILD SUCCESS

# Run application
./mvnw spring-boot:run

# Wait for log:
# "Started ClinicApplication in X seconds"
```

---

## 🔍 BƯỚC 3: VERIFY DATABASE SCHEMA

```bash
# Connect to database
mysql -u root -p clinic_db

# Check new columns
DESCRIBE treatment_plans;
# Expected: appointment_id column exists

DESCRIBE prescriptions;
# Expected: step_id column exists

DESCRIBE treatment_plan_steps;
# Expected: completed_at column exists

# Check indexes
SHOW INDEXES FROM treatment_plans WHERE Key_name = 'idx_treatment_plans_appointment_id';
# Expected: 1 row

# Check foreign keys
SELECT CONSTRAINT_NAME, TABLE_NAME, REFERENCED_TABLE_NAME
FROM information_schema.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'clinic_db'
  AND CONSTRAINT_NAME LIKE 'fk_treatment_plan%';
# Expected: fk_treatment_plan_appointment row
```

---

## 🎯 BƯỚC 4: MANUAL API TESTING

### 4.1. Get Doctor Token

```bash
# Login as doctor
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "doctor@example.com",
    "password": "password123"
  }'

# Copy the "token" from response
# Example: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 4.2. Test API getPatientByQr

```bash
# Replace {TOKEN} with actual token
curl -X GET "http://localhost:8080/api/doctor/patient?qr=1" \
  -H "Authorization: Bearer {TOKEN}" | jq '.'

# Check response has new fields:
# - treatmentPlanId
# - hasTreatmentPlan
# - treatmentPlanStatus
```

### 4.3. Test Create Plan from Appointment

```bash
# First, create an appointment or use existing one
# Get appointmentId from database or previous API call

curl -X POST "http://localhost:8080/api/treatment-plans/from-appointment" \
  -H "Authorization: Bearer {TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "appointmentId": 1,
    "templateId": 1
  }' | jq '.'

# Check response has:
# - id (plan ID)
# - appointmentId (linked)
# - steps array
```

### 4.4. Test Complete Step

```bash
# Get stepId from previous response or database
# Example: stepId = 1

curl -X PATCH "http://localhost:8080/api/treatment-plans/steps/1/complete" \
  -H "Authorization: Bearer {TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "doctorConclusion": "Test completion",
    "imageUrls": []
  }' | jq '.'

# Check response:
# - message: "Đã hoàn thành bước điều trị"
# - nextRoomName: null or room name (if has next step)
```

### 4.5. Verify No Auto-Generate Step

```bash
# After completing last step, check database
mysql -u root -p clinic_db

SELECT COUNT(*) as step_count 
FROM treatment_plan_steps 
WHERE plan_id = 1;

# Expected: Original number of steps (e.g., 3)
# NOT increased (e.g., NOT 4 or 5)

SELECT status FROM treatment_plans WHERE id = 1;
# Expected: COMPLETED
```

---

## 🤖 BƯỚC 5: AUTOMATED TESTING

### 5.1. Run Test Script

```bash
# Make script executable
chmod +x test_phase1_apis.sh

# Edit script to add your doctor token
nano test_phase1_apis.sh
# Replace: DOCTOR_TOKEN="your_doctor_jwt_token_here"

# Run tests
./test_phase1_apis.sh

# Review output
# Look for GREEN ✓ marks (success)
# Look for RED ✗ marks (failure)
```

### 5.2. Run Unit Tests (if available)

```bash
cd clinic_backend

# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=TreatmentPlanServiceTest

# Check results
# Expected: All tests PASSED
```

---

## 📊 BƯỚC 6: INTEGRATION TESTING

### Scenario: Full Doctor Workflow

**Step 1: Patient Check-in**
```bash
# Login as patient
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "patient@example.com",
    "password": "password123"
  }'

# Self check-in
curl -X POST "http://localhost:8080/api/checkin/self-scan" \
  -H "Authorization: Bearer {PATIENT_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "qrData": "CHECKIN:1"
  }' | jq '.'

# Note the appointmentId from response
```

**Step 2: Doctor Scans Patient QR**
```bash
curl -X GET "http://localhost:8080/api/doctor/patient?qr=CHECKIN:1" \
  -H "Authorization: Bearer {DOCTOR_TOKEN}" | jq '.'

# Verify:
# - hasTreatmentPlan: false (first time)
# - appointmentId: 1
```

**Step 3: Doctor Creates Treatment Plan**
```bash
curl -X POST "http://localhost:8080/api/treatment-plans/from-appointment" \
  -H "Authorization: Bearer {DOCTOR_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "appointmentId": 1,
    "templateId": 1
  }' | jq '.'

# Note the plan ID and step IDs
```

**Step 4: Doctor Scans Again**
```bash
curl -X GET "http://localhost:8080/api/doctor/patient?qr=CHECKIN:1" \
  -H "Authorization: Bearer {DOCTOR_TOKEN}" | jq '.'

# Verify:
# - hasTreatmentPlan: true (now has plan)
# - treatmentPlanId: > 0
# - treatmentPlanStatus: "IN_PROGRESS"
```

**Step 5: Complete Steps One by One**
```bash
# Complete step 1
curl -X PATCH "http://localhost:8080/api/treatment-plans/steps/{STEP1_ID}/complete" \
  -H "Authorization: Bearer {DOCTOR_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "doctorConclusion": "Step 1 done"
  }' | jq '.'

# Complete step 2
curl -X PATCH "http://localhost:8080/api/treatment-plans/steps/{STEP2_ID}/complete" \
  -H "Authorization: Bearer {DOCTOR_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "doctorConclusion": "Step 2 done"
  }' | jq '.'

# Complete step 3 (last)
curl -X PATCH "http://localhost:8080/api/treatment-plans/steps/{STEP3_ID}/complete" \
  -H "Authorization: Bearer {DOCTOR_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "doctorConclusion": "All done"
  }' | jq '.'

# Verify: No nextRoomName in response
```

**Step 6: Verify Plan Completed**
```bash
# Check plan status
mysql -u root -p clinic_db

SELECT id, status, 
  (SELECT COUNT(*) FROM treatment_plan_steps WHERE plan_id = treatment_plans.id) as total_steps,
  (SELECT COUNT(*) FROM treatment_plan_steps WHERE plan_id = treatment_plans.id AND status = 'COMPLETED') as completed_steps
FROM treatment_plans 
WHERE id = {PLAN_ID};

# Expected:
# - status: COMPLETED
# - total_steps = completed_steps
# - No extra steps generated
```

---

## ✅ SUCCESS CRITERIA

Phase 1 is successful if:

1. **Database Migration**
   - ✅ All new columns exist
   - ✅ All indexes created
   - ✅ All foreign keys work
   - ✅ No migration errors

2. **API getPatientByQr**
   - ✅ Returns treatmentPlanId
   - ✅ Returns hasTreatmentPlan
   - ✅ Returns treatmentPlanStatus
   - ✅ Values are correct

3. **Create Plan from Appointment**
   - ✅ Creates plan successfully
   - ✅ Links with appointment
   - ✅ Creates MedicalRecord if not exists
   - ✅ Prevents duplicate plans

4. **Complete Step**
   - ✅ Completes step successfully
   - ✅ Sets completedAt timestamp
   - ✅ Activates next step
   - ✅ Does NOT auto-generate steps
   - ✅ Completes plan when last step done
   - ✅ Checks doctor permissions

5. **No Regressions**
   - ✅ Old features still work
   - ✅ No compilation errors
   - ✅ No runtime errors
   - ✅ Performance acceptable

---

## 🐛 TROUBLESHOOTING

### Issue 1: Migration fails
```
Error: Column 'appointment_id' already exists
```
**Solution:** Column already added, skip migration or drop column first.

### Issue 2: Compilation error
```
Error: cannot find symbol treatmentPlanRepository
```
**Solution:** Check if dependency is added in TreatmentPlanService constructor.

### Issue 3: API returns 500
```
Error: NullPointerException in getPatientByQr
```
**Solution:** Check if treatmentPlanRepository is properly injected.

### Issue 4: Test fails - hasTreatmentPlan always false
```
Expected: true, Actual: false
```
**Solution:** Check if appointment has a linked treatment plan in database.

### Issue 5: Auto-generate step still happens
```
Expected: 3 steps, Actual: 4 steps
```
**Solution:** Method not replaced correctly, check TreatmentPlanService.java line ~290.

---

## 📞 SUPPORT

If tests fail:
1. Check logs: `clinic_backend/logs/application.log`
2. Check database: `SELECT * FROM treatment_plans;`
3. Review code changes
4. Compare with PHASE1_IMPLEMENTATION_SUMMARY.md

---

**Last Updated:** 2026-03-28
**Ready for Testing!** 🚀
