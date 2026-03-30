# 🧪 ODONTOGRAM TESTING GUIDE

**Date:** 30/03/2026
**Phase:** Phase 3 - Testing & QA
**Status:** READY TO START

---

## 📋 TESTING CHECKLIST

### 1. Backend API Testing

#### Test 1.1: Add Tooth-Specific Service
**Endpoint:** `POST /api/treatment-plans/{planId}/services/teeth/{toothNumber}`

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
  "stepId": 123,
  "toothNumber": "8",
  "serviceName": "Nhổ răng khôn",
  "price": 2000000,
  "totalPlanCost": 2000000
}
```

**Test Steps:**
1. Create treatment plan
2. Call API with valid planId and toothNumber
3. Verify response contains stepId
4. Verify toothNumber is correct
5. Verify price is correct
6. Verify totalPlanCost is calculated

---

#### Test 1.2: Add General Service
**Endpoint:** `POST /api/treatment-plans/{planId}/services/general`

**Request:**
```json
{
  "serviceId": 1,
  "sequenceOrder": 1
}
```

**Expected Response:**
```json
{
  "stepId": 124,
  "serviceName": "Khám và tư vấn",
  "price": 100000,
  "totalPlanCost": 100000
}
```

**Test Steps:**
1. Create treatment plan
2. Call API with valid planId
3. Verify response contains stepId
4. Verify serviceName is correct
5. Verify price is correct
6. Verify totalPlanCost is calculated

---

#### Test 1.3: Get Services for Tooth
**Endpoint:** `GET /api/treatment-plans/{planId}/services/teeth/{toothNumber}`

**Expected Response:**
```json
[
  {
    "id": 123,
    "serviceId": 7,
    "serviceName": "Nhổ răng khôn",
    "toothNumber": "8",
    "actualPrice": 2000000,
    "status": "PENDING"
  }
]
```

**Test Steps:**
1. Add service to tooth
2. Call API with planId and toothNumber
3. Verify response contains service
4. Verify toothNumber matches

---

#### Test 1.4: Get General Services
**Endpoint:** `GET /api/treatment-plans/{planId}/services/general`

**Expected Response:**
```json
[
  {
    "id": 124,
    "serviceId": 1,
    "serviceName": "Khám và tư vấn",
    "toothNumber": null,
    "actualPrice": 100000,
    "status": "PENDING"
  }
]
```

**Test Steps:**
1. Add general service
2. Call API with planId
3. Verify response contains service
4. Verify toothNumber is null

---

#### Test 1.5: Get All Steps
**Endpoint:** `GET /api/treatment-plans/{planId}/services/all`

**Expected Response:**
```json
{
  "steps": [
    {
      "id": 123,
      "serviceId": 7,
      "serviceName": "Nhổ răng khôn",
      "toothNumber": "8",
      "actualPrice": 2000000,
      "status": "PENDING"
    },
    {
      "id": 124,
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

**Test Steps:**
1. Add multiple services
2. Call API with planId
3. Verify response contains all steps
4. Verify totalCost is sum of all prices

---

#### Test 1.6: Remove Service
**Endpoint:** `DELETE /api/treatment-plans/{planId}/services/steps/{stepId}`

**Expected Response:**
```json
{
  "message": "Service removed successfully",
  "totalPlanCost": 100000
}
```

**Test Steps:**
1. Add service
2. Call API with planId and stepId
3. Verify response contains success message
4. Verify totalPlanCost is updated

---

#### Test 1.7: Update Step Price
**Endpoint:** `PUT /api/treatment-plans/{planId}/services/steps/{stepId}/price`

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

**Test Steps:**
1. Add service
2. Call API with planId, stepId, and newPrice
3. Verify response contains success message
4. Verify newPrice is updated
5. Verify totalPlanCost is recalculated

---

### 2. Frontend UI Testing

#### Test 2.1: OdontogramView Display
**Steps:**
1. Open DoctorWorkflowActivity
2. Lookup patient
3. Verify OdontogramView displays
4. Verify all 32 teeth are visible
5. Verify teeth are numbered correctly (FDI notation)

**Expected Result:** ✅ OdontogramView displays with all teeth

---

#### Test 2.2: Tooth Click
**Steps:**
1. Click on tooth #8 in OdontogramView
2. Verify ToothServiceDialog appears
3. Verify dialog shows 4 tooth-specific services
4. Verify services are: Trám, Nhổ thường, Nhổ khôn, Bọc sứ
5. Verify prices are displayed correctly

**Expected Result:** ✅ ToothServiceDialog shows with correct services

---

#### Test 2.3: Add Tooth-Specific Service
**Steps:**
1. Click on tooth #8
2. Select "Nhổ răng khôn" (2,000,000 đ)
3. Verify API call is made
4. Verify service is added to treatment steps
5. Verify total cost is updated to 2,000,000 đ
6. Verify success toast is shown

**Expected Result:** ✅ Service added and total cost updated

---

#### Test 2.4: GeneralServicesList Display
**Steps:**
1. Scroll down to see GeneralServicesList
2. Verify list shows 6 general services
3. Verify services are: Khám, X-quang, Lấy cao, Điều trị tủy, Tẩy trắng, Niềng
4. Verify prices are displayed correctly
5. Verify "Thêm" button is visible for each service

**Expected Result:** ✅ GeneralServicesList displays with all services

---

#### Test 2.5: Add General Service
**Steps:**
1. Click "Thêm" button on "Khám và tư vấn" (100,000 đ)
2. Verify API call is made
3. Verify service is added to treatment steps
4. Verify total cost is updated to 2,100,000 đ
5. Verify success toast is shown

**Expected Result:** ✅ Service added and total cost updated

---

#### Test 2.6: Total Cost Calculation
**Steps:**
1. Add multiple services (tooth-specific and general)
2. Verify total cost is sum of all prices
3. Verify price is formatted in Vietnamese locale (1.000.000 đ)
4. Remove a service
5. Verify total cost is recalculated

**Expected Result:** ✅ Total cost calculated correctly

---

#### Test 2.7: Error Handling
**Steps:**
1. Try to add service without creating treatment plan
2. Verify error message is shown
3. Try to add service with invalid planId
4. Verify error message is shown
5. Try to add service with network error
6. Verify error message is shown

**Expected Result:** ✅ Error messages shown correctly

---

### 3. Integration Testing

#### Test 3.1: Complete Workflow
**Steps:**
1. Open DoctorWorkflowActivity
2. Lookup patient
3. Create treatment plan
4. Add tooth-specific service (click tooth #8, select "Nhổ khôn")
5. Add another tooth-specific service (click tooth #48, select "Nhổ khôn")
6. Add general service (click "Khám và tư vấn")
7. Add another general service (click "Chụp X-quang")
8. Verify total cost is: 2M + 2M + 100k + 200k = 4,300,000 đ
9. Verify all services are in treatment steps list

**Expected Result:** ✅ Complete workflow works correctly

---

#### Test 3.2: Service Removal
**Steps:**
1. Add multiple services
2. Verify total cost is calculated
3. Remove one service
4. Verify service is removed from list
5. Verify total cost is recalculated

**Expected Result:** ✅ Service removal works correctly

---

#### Test 3.3: Price Update
**Steps:**
1. Add service
2. Update price via API
3. Verify price is updated in UI
4. Verify total cost is recalculated

**Expected Result:** ✅ Price update works correctly

---

### 4. UAT (User Acceptance Testing)

#### Test 4.1: Doctor Workflow
**Steps:**
1. Doctor opens app
2. Doctor looks up patient
3. Doctor creates treatment plan
4. Doctor clicks on tooth to add service
5. Doctor selects service from dialog
6. Doctor adds general services
7. Doctor verifies total cost
8. Doctor completes treatment

**Expected Result:** ✅ Doctor can complete workflow without issues

---

#### Test 4.2: Data Persistence
**Steps:**
1. Add services to treatment plan
2. Close app
3. Reopen app
4. Lookup same patient
5. Verify services are still there
6. Verify total cost is correct

**Expected Result:** ✅ Data persists correctly

---

#### Test 4.3: Performance
**Steps:**
1. Add 10+ services
2. Verify UI is responsive
3. Verify no lag when scrolling
4. Verify API calls complete quickly

**Expected Result:** ✅ Performance is acceptable

---

## 📊 TEST RESULTS TEMPLATE

```
Test Case: [Test Name]
Date: [Date]
Tester: [Name]
Status: [PASS/FAIL]
Notes: [Any issues or observations]

Expected Result: [What should happen]
Actual Result: [What actually happened]
```

---

## 🎯 ACCEPTANCE CRITERIA

- ✅ All 7 API endpoints work correctly
- ✅ OdontogramView displays correctly
- ✅ ToothServiceDialog shows correct services
- ✅ GeneralServicesList shows correct services
- ✅ Services can be added via UI
- ✅ Total cost is calculated correctly
- ✅ Services can be removed
- ✅ Error handling works
- ✅ No compilation errors
- ✅ No runtime errors
- ✅ Performance is acceptable
- ✅ Doctor can complete workflow

---

## 📝 NOTES

1. **Test Environment:** Use test database with sample data
2. **Test Data:** Create sample treatment plans and patients
3. **API Testing:** Use Postman or curl for API testing
4. **UI Testing:** Use Android emulator or physical device
5. **Performance:** Monitor network requests and UI responsiveness

---

**Status:** READY FOR TESTING
**Next:** Start Phase 3 Testing

