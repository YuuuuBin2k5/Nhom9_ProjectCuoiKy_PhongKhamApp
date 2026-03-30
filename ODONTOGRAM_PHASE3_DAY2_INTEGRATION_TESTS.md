# 🧪 PHASE 3 - DAY 2: INTEGRATION TESTS

**Date:** 30/03/2026
**Phase:** Phase 3 - Testing & QA
**Day:** Day 2 - Integration Tests
**Status:** READY TO EXECUTE

---

## 📋 INTEGRATION TEST PLAN

### Test Environment Setup
1. Start backend server
2. Create test database
3. Load test data
4. Initialize API client

### Test Data Preparation
```sql
-- Create test patient
INSERT INTO patients (id, full_name, phone, email) 
VALUES (1, 'Test Patient', '0123456789', 'test@example.com');

-- Create test treatment plan
INSERT INTO treatment_plans (id, patient_id, status, total_cost) 
VALUES (1, 1, 'DRAFT', 0);

-- Create test services
INSERT INTO services (id, name, price, category) 
VALUES 
  (1, 'Khám và tư vấn', 100000, 'GENERAL'),
  (2, 'Chụp X-quang', 200000, 'GENERAL'),
  (7, 'Nhổ răng khôn', 2000000, 'SURGERY');
```

---

## 🧪 INTEGRATION TEST CASES

### Test 1: Add Tooth-Specific Service
**Endpoint:** `POST /api/treatment-plans/1/services/teeth/8`

**Request:**
```json
{
  "serviceId": 7,
  "sequenceOrder": 1
}
```

**Expected Response:**
```json
{
  "stepId": 1,
  "toothNumber": "8",
  "serviceName": "Nhổ răng khôn",
  "price": 2000000,
  "totalPlanCost": 2000000
}
```

**Verification:**
- [ ] Response status: 200 OK
- [ ] stepId is not null
- [ ] toothNumber = "8"
- [ ] price = 2,000,000
- [ ] totalPlanCost = 2,000,000
- [ ] Database: TreatmentPlanStep created with isGeneralService = false

**Test Script:**
```bash
curl -X POST http://localhost:8080/api/treatment-plans/1/services/teeth/8 \
  -H "Content-Type: application/json" \
  -d '{"serviceId": 7, "sequenceOrder": 1}'
```

---

### Test 2: Add General Service
**Endpoint:** `POST /api/treatment-plans/1/services/general`

**Request:**
```json
{
  "serviceId": 1,
  "sequenceOrder": 2
}
```

**Expected Response:**
```json
{
  "stepId": 2,
  "serviceName": "Khám và tư vấn",
  "price": 100000,
  "totalPlanCost": 2100000
}
```

**Verification:**
- [ ] Response status: 200 OK
- [ ] stepId is not null
- [ ] serviceName = "Khám và tư vấn"
- [ ] price = 100,000
- [ ] totalPlanCost = 2,100,000 (sum of all services)
- [ ] Database: TreatmentPlanStep created with isGeneralService = true, toothNumber = null

**Test Script:**
```bash
curl -X POST http://localhost:8080/api/treatment-plans/1/services/general \
  -H "Content-Type: application/json" \
  -d '{"serviceId": 1, "sequenceOrder": 2}'
```

---

### Test 3: Get Services for Tooth
**Endpoint:** `GET /api/treatment-plans/1/services/teeth/8`

**Expected Response:**
```json
[
  {
    "id": 1,
    "serviceId": 7,
    "serviceName": "Nhổ răng khôn",
    "toothNumber": "8",
    "actualPrice": 2000000,
    "status": "PENDING"
  }
]
```

**Verification:**
- [ ] Response status: 200 OK
- [ ] Array contains 1 item
- [ ] toothNumber = "8"
- [ ] serviceName = "Nhổ răng khôn"

**Test Script:**
```bash
curl -X GET http://localhost:8080/api/treatment-plans/1/services/teeth/8 \
  -H "Content-Type: application/json"
```

---

### Test 4: Get General Services
**Endpoint:** `GET /api/treatment-plans/1/services/general`

**Expected Response:**
```json
[
  {
    "id": 2,
    "serviceId": 1,
    "serviceName": "Khám và tư vấn",
    "toothNumber": null,
    "actualPrice": 100000,
    "status": "PENDING"
  }
]
```

**Verification:**
- [ ] Response status: 200 OK
- [ ] Array contains 1 item
- [ ] toothNumber = null
- [ ] serviceName = "Khám và tư vấn"

**Test Script:**
```bash
curl -X GET http://localhost:8080/api/treatment-plans/1/services/general \
  -H "Content-Type: application/json"
```

---

### Test 5: Get All Steps
**Endpoint:** `GET /api/treatment-plans/1/services/all`

**Expected Response:**
```json
{
  "steps": [
    {
      "id": 1,
      "serviceId": 7,
      "serviceName": "Nhổ răng khôn",
      "toothNumber": "8",
      "actualPrice": 2000000,
      "status": "PENDING"
    },
    {
      "id": 2,
      "serviceId": 1,
      "serviceName": "Khám và tư vấn",
      "toothNumber": null,
      "actualPrice": 100000,
      "status": "PENDING"
    }
  ],
  "totalCost": 2100000
}
```

**Verification:**
- [ ] Response status: 200 OK
- [ ] steps array contains 2 items
- [ ] totalCost = 2,100,000
- [ ] Both tooth-specific and general services included

**Test Script:**
```bash
curl -X GET http://localhost:8080/api/treatment-plans/1/services/all \
  -H "Content-Type: application/json"
```

---

### Test 6: Update Step Price
**Endpoint:** `PUT /api/treatment-plans/1/services/steps/1/price`

**Request:**
```json
{
  "newPrice": 1500000
}
```

**Expected Response:**
```json
{
  "message": "Price updated successfully",
  "newPrice": 1500000,
  "totalPlanCost": 1600000
}
```

**Verification:**
- [ ] Response status: 200 OK
- [ ] newPrice = 1,500,000
- [ ] totalPlanCost = 1,600,000 (recalculated)
- [ ] Database: Step price updated

**Test Script:**
```bash
curl -X PUT http://localhost:8080/api/treatment-plans/1/services/steps/1/price \
  -H "Content-Type: application/json" \
  -d '{"newPrice": 1500000}'
```

---

### Test 7: Remove Service
**Endpoint:** `DELETE /api/treatment-plans/1/services/steps/1`

**Expected Response:**
```json
{
  "message": "Service removed successfully",
  "totalPlanCost": 100000
}
```

**Verification:**
- [ ] Response status: 200 OK
- [ ] message = "Service removed successfully"
- [ ] totalPlanCost = 100,000 (only general service remains)
- [ ] Database: Step deleted

**Test Script:**
```bash
curl -X DELETE http://localhost:8080/api/treatment-plans/1/services/steps/1 \
  -H "Content-Type: application/json"
```

---

## 🚀 HOW TO RUN INTEGRATION TESTS

### Using Postman
1. Import Postman collection
2. Set base URL: `http://localhost:8080`
3. Run tests in sequence
4. Verify responses

### Using curl
```bash
# Run all tests
bash test_tooth_service_apis.sh
```

### Using REST Client (VS Code)
1. Create `test.http` file
2. Add test requests
3. Run requests

---

## 📊 TEST EXECUTION CHECKLIST

### Pre-Test Setup
- [ ] Backend server running
- [ ] Database initialized
- [ ] Test data loaded
- [ ] API accessible

### Test Execution
- [ ] Test 1: Add tooth service - PASS
- [ ] Test 2: Add general service - PASS
- [ ] Test 3: Get tooth services - PASS
- [ ] Test 4: Get general services - PASS
- [ ] Test 5: Get all steps - PASS
- [ ] Test 6: Update price - PASS
- [ ] Test 7: Remove service - PASS

### Post-Test Verification
- [ ] All tests passed
- [ ] No errors
- [ ] Database consistent
- [ ] Total cost calculated correctly

---

## 📝 TEST RESULTS TEMPLATE

```
Integration Test Report - Odontogram Tooth Service
Date: [Date]
Time: [Time]

Test Results:
- Test 1 (Add Tooth Service): [PASS/FAIL]
- Test 2 (Add General Service): [PASS/FAIL]
- Test 3 (Get Tooth Services): [PASS/FAIL]
- Test 4 (Get General Services): [PASS/FAIL]
- Test 5 (Get All Steps): [PASS/FAIL]
- Test 6 (Update Price): [PASS/FAIL]
- Test 7 (Remove Service): [PASS/FAIL]

Summary:
- Total Tests: 7
- Passed: [Number]
- Failed: [Number]
- Duration: [Time]

Issues Found:
1. [Issue 1]
2. [Issue 2]
...

Status: [PASS/FAIL]
```

---

## 🎯 SUCCESS CRITERIA

- ✅ All 7 API endpoints work correctly
- ✅ All tests pass
- ✅ No errors
- ✅ Database consistent
- ✅ Total cost calculated correctly
- ✅ Ready for Day 3 UAT

---

## 📞 TROUBLESHOOTING

### Issue: Connection refused
**Solution:** Ensure backend server is running on port 8080

### Issue: 404 Not Found
**Solution:** Verify endpoint URL is correct

### Issue: 400 Bad Request
**Solution:** Check request body format and parameters

### Issue: 500 Internal Server Error
**Solution:** Check backend logs for error details

---

## 🔍 DETAILED TEST SCENARIOS

### Scenario 1: Complete Workflow
```
1. Add tooth service for tooth #8 (Nhổ khôn) - 2M
2. Add tooth service for tooth #48 (Nhổ khôn) - 2M
3. Add general service (Khám) - 100k
4. Add general service (X-quang) - 200k
5. Verify total cost = 4,300,000
6. Update price of first service to 1.5M
7. Verify total cost = 3,800,000
8. Remove first service
9. Verify total cost = 1,800,000
```

### Scenario 2: Error Handling
```
1. Try to add service with invalid planId
   - Expected: 400 Bad Request
2. Try to add service with invalid serviceId
   - Expected: 400 Bad Request
3. Try to remove non-existent step
   - Expected: 400 Bad Request
4. Try to update price with negative value
   - Expected: 400 Bad Request
```

---

## 📚 NEXT STEPS

### After Integration Tests Pass
1. ✅ Document test results
2. ✅ Review any issues
3. ✅ Move to Day 3: UAT

### If Tests Fail
1. ❌ Review error messages
2. ❌ Check backend logs
3. ❌ Verify test data
4. ❌ Fix issues
5. ❌ Re-run tests

---

**Status:** 🟢 READY FOR EXECUTION

**Next:** Run integration tests and proceed to Day 3 UAT

