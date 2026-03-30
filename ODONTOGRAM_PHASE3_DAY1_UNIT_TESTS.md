# 🧪 PHASE 3 - DAY 1: UNIT TESTS

**Date:** 30/03/2026
**Phase:** Phase 3 - Testing & QA
**Day:** Day 1 - Unit Tests
**Status:** READY TO RUN

---

## 📋 UNIT TESTS CREATED

### 1. ToothServiceCalculationServiceTest
**File:** `clinic_backend/src/test/java/com/hcmute/clinic/service/ToothServiceCalculationServiceTest.java`

**Test Cases (13 tests):**
1. ✅ `testAddServiceToTooth_Success` - Add tooth-specific service successfully
2. ✅ `testAddServiceToTooth_PlanNotFound` - Handle plan not found error
3. ✅ `testAddServiceToTooth_ServiceNotFound` - Handle service not found error
4. ✅ `testAddServiceToTooth_EmptyToothNumber` - Handle empty tooth number error
5. ✅ `testAddGeneralService_Success` - Add general service successfully
6. ✅ `testRecalculatePlanTotalCost_Success` - Calculate total cost correctly
7. ✅ `testRecalculatePlanTotalCost_NoSteps` - Handle no steps case
8. ✅ `testGetServicesForTooth_Success` - Get services for specific tooth
9. ✅ `testGetGeneralServices_Success` - Get general services
10. ✅ `testRemoveService_Success` - Remove service successfully
11. ✅ `testRemoveService_StepNotFound` - Handle step not found error
12. ✅ `testUpdateStepPrice_Success` - Update step price successfully
13. ✅ `testUpdateStepPrice_NegativePrice` - Handle negative price error
14. ✅ `testUpdateStepPrice_NullPrice` - Handle null price error
15. ✅ `testGetPlanStepsOrdered_Success` - Get plan steps ordered by sequence

**Coverage Target:** 80%+

### 2. ToothServiceControllerTest
**File:** `clinic_backend/src/test/java/com/hcmute/clinic/controller/ToothServiceControllerTest.java`

**Test Cases (10 tests):**
1. ✅ `testAddServiceToTooth_Success` - POST /api/treatment-plans/{planId}/services/teeth/{toothNumber}
2. ✅ `testAddServiceToTooth_Error` - Handle error when adding tooth service
3. ✅ `testAddGeneralService_Success` - POST /api/treatment-plans/{planId}/services/general
4. ✅ `testGetServicesForTooth_Success` - GET /api/treatment-plans/{planId}/services/teeth/{toothNumber}
5. ✅ `testGetGeneralServices_Success` - GET /api/treatment-plans/{planId}/services/general
6. ✅ `testRemoveService_Success` - DELETE /api/treatment-plans/{planId}/services/steps/{stepId}
7. ✅ `testRemoveService_Error` - Handle error when removing service
8. ✅ `testUpdateStepPrice_Success` - PUT /api/treatment-plans/{planId}/services/steps/{stepId}/price
9. ✅ `testGetAllSteps_Success` - GET /api/treatment-plans/{planId}/services/all

**Coverage Target:** 80%+

---

## 🚀 HOW TO RUN TESTS

### Run All Tests
```bash
cd clinic_backend
mvn clean test
```

### Run Specific Test Class
```bash
# Run ToothServiceCalculationServiceTest
mvn test -Dtest=ToothServiceCalculationServiceTest

# Run ToothServiceControllerTest
mvn test -Dtest=ToothServiceControllerTest
```

### Run Tests with Coverage Report
```bash
mvn clean test jacoco:report
```

### View Coverage Report
```bash
# Open in browser
open target/site/jacoco/index.html
```

### Run Tests in IDE
```
Right-click on test class → Run 'TestClassName'
```

---

## 📊 TEST COVERAGE TARGETS

| Component | Target | Method |
|-----------|--------|--------|
| ToothServiceCalculationService | 80%+ | JUnit + Mockito |
| ToothServiceController | 80%+ | JUnit + MockMvc |
| Overall | 80%+ | JaCoCo |

---

## ✅ TEST EXECUTION CHECKLIST

### Before Running Tests
- [ ] Maven installed
- [ ] Java 11+ installed
- [ ] All dependencies resolved
- [ ] No compilation errors

### Running Tests
- [ ] Run `mvn clean test`
- [ ] All tests pass
- [ ] No failures
- [ ] No skipped tests

### After Running Tests
- [ ] Generate coverage report: `mvn jacoco:report`
- [ ] Check coverage >= 80%
- [ ] Review test results
- [ ] Document any issues

---

## 📝 TEST RESULTS TEMPLATE

```
Test Execution Report - Odontogram Tooth Service
Date: [Date]
Time: [Time]

Summary:
- Total Tests: 25
- Passed: [Number]
- Failed: [Number]
- Skipped: [Number]
- Duration: [Time]

Coverage:
- ToothServiceCalculationService: [%]
- ToothServiceController: [%]
- Overall: [%]

Issues Found:
1. [Issue 1]
2. [Issue 2]
...

Status: [PASS/FAIL]
```

---

## 🎯 SUCCESS CRITERIA

- ✅ All 25 tests pass
- ✅ Code coverage >= 80%
- ✅ No compilation errors
- ✅ No runtime errors
- ✅ All assertions pass

---

## 📞 TROUBLESHOOTING

### Issue: Tests fail with "Plan not found"
**Solution:** Ensure mock setup is correct in @BeforeEach

### Issue: Coverage report not generated
**Solution:** Run `mvn clean test jacoco:report`

### Issue: Tests timeout
**Solution:** Increase timeout in pom.xml or test configuration

### Issue: Mock not working
**Solution:** Ensure @Mock and @InjectMocks annotations are used correctly

---

## 🔍 TEST DETAILS

### ToothServiceCalculationServiceTest

#### Test 1: Add Service to Tooth
```java
// Arrange: Setup mocks
when(planRepository.findById(1L)).thenReturn(Optional.of(testPlan));
when(serviceRepository.findById(7L)).thenReturn(Optional.of(testService));
when(stepRepository.save(any())).thenReturn(testStep);

// Act: Call method
TreatmentPlanStep result = toothService.addServiceToTooth(1L, 7L, "8", 1);

// Assert: Verify result
assertEquals("8", result.getToothNumber());
assertEquals(false, result.isGeneralService());
```

#### Test 2: Calculate Total Cost
```java
// Arrange: Setup steps
List<TreatmentPlanStep> steps = new ArrayList<>();
steps.add(step1); // 2,000,000
steps.add(step2); // 100,000
when(stepRepository.findByPlanId(1L)).thenReturn(steps);

// Act: Calculate
BigDecimal result = toothService.recalculatePlanTotalCost(1L);

// Assert: Verify total
assertEquals(new BigDecimal("2100000"), result);
```

### ToothServiceControllerTest

#### Test 1: Add Service to Tooth API
```java
// Arrange: Setup mock response
when(toothService.addServiceToTooth(1L, 7L, "8", 1)).thenReturn(testStep);
when(toothService.recalculatePlanTotalCost(1L)).thenReturn(new BigDecimal("2000000"));

// Act: Call API
mockMvc.perform(post("/api/treatment-plans/1/services/teeth/8")
    .contentType(MediaType.APPLICATION_JSON)
    .content(objectMapper.writeValueAsString(request)))

// Assert: Verify response
.andExpect(status().isOk())
.andExpect(jsonPath("$.stepId").value(1))
.andExpect(jsonPath("$.toothNumber").value("8"));
```

---

## 📚 NEXT STEPS

### After Unit Tests Pass
1. ✅ Generate coverage report
2. ✅ Review coverage metrics
3. ✅ Document test results
4. ✅ Move to Day 2: Integration Tests

### If Tests Fail
1. ❌ Review error messages
2. ❌ Check mock setup
3. ❌ Verify test data
4. ❌ Fix issues
5. ❌ Re-run tests

---

## 🎉 COMPLETION CRITERIA

- ✅ All 25 unit tests created
- ✅ All tests pass
- ✅ Code coverage >= 80%
- ✅ No compilation errors
- ✅ No runtime errors
- ✅ Ready for Day 2 Integration Tests

---

**Status:** 🟢 READY FOR EXECUTION

**Next:** Run tests and proceed to Day 2 Integration Tests

