# 🚀 PHASE 3 - QUICK START GUIDE

**Date:** 30/03/2026
**Phase:** Phase 3 - Testing & QA
**Duration:** 3 days
**Status:** READY TO START

---

## 📋 WHAT TO DO NEXT

### Day 1: Unit Tests (Backend)

#### 1. Test ToothServiceCalculationService
```bash
# Create test file:
clinic_backend/src/test/java/com/hcmute/clinic/service/ToothServiceCalculationServiceTest.java

# Test methods:
- testAddServiceToTooth()
- testAddGeneralService()
- testRecalculatePlanTotalCost()
- testGetServicesForTooth()
- testGetGeneralServices()
- testRemoveService()
- testUpdateStepPrice()
- testGetPlanStepsOrdered()
```

#### 2. Test ToothServiceController
```bash
# Create test file:
clinic_backend/src/test/java/com/hcmute/clinic/controller/ToothServiceControllerTest.java

# Test methods:
- testAddServiceToTooth()
- testAddGeneralService()
- testGetServicesForTooth()
- testGetGeneralServices()
- testRemoveService()
- testUpdateStepPrice()
- testGetAllSteps()
```

#### 3. Test Coverage Target
- Target: 80%+
- Use JaCoCo for coverage reporting
- Run: `mvn clean test jacoco:report`

---

### Day 2: Integration Tests (API)

#### 1. Test API Endpoints with Postman
```
1. POST /api/treatment-plans/{planId}/services/teeth/{toothNumber}
   - Add tooth-specific service
   - Verify response
   - Verify database

2. POST /api/treatment-plans/{planId}/services/general
   - Add general service
   - Verify response
   - Verify database

3. GET /api/treatment-plans/{planId}/services/teeth/{toothNumber}
   - Get tooth services
   - Verify response

4. GET /api/treatment-plans/{planId}/services/general
   - Get general services
   - Verify response

5. DELETE /api/treatment-plans/{planId}/services/steps/{stepId}
   - Remove service
   - Verify response
   - Verify database

6. PUT /api/treatment-plans/{planId}/services/steps/{stepId}/price
   - Update price
   - Verify response
   - Verify database

7. GET /api/treatment-plans/{planId}/services/all
   - Get all steps
   - Verify response
```

#### 2. Test Frontend UI
```
1. Build APK
   - Run: ./gradlew assembleDebug

2. Install on device/emulator
   - adb install app-debug.apk

3. Test OdontogramView
   - Open DoctorWorkflowActivity
   - Verify teeth display
   - Click on tooth
   - Verify dialog appears

4. Test ToothServiceDialog
   - Select service
   - Verify API call
   - Verify service added

5. Test GeneralServicesList
   - Click "Thêm" button
   - Verify API call
   - Verify service added

6. Test Total Cost
   - Add multiple services
   - Verify total cost calculated
   - Verify price formatted correctly
```

---

### Day 3: UAT & Final Testing

#### 1. UAT with Doctors
```
1. Prepare test data
   - Create sample patients
   - Create sample treatment plans

2. Test with doctors
   - Doctor looks up patient
   - Doctor creates treatment plan
   - Doctor adds tooth-specific services
   - Doctor adds general services
   - Doctor verifies total cost
   - Doctor completes treatment

3. Collect feedback
   - UI/UX feedback
   - Performance feedback
   - Feature feedback

4. Fix issues
   - Fix any bugs found
   - Optimize performance
   - Improve UI/UX
```

#### 2. Final Verification
```
1. Verify all endpoints work
2. Verify all UI components work
3. Verify error handling works
4. Verify performance is acceptable
5. Verify data persistence works
6. Verify no compilation errors
7. Verify no runtime errors
```

---

## 🧪 TESTING COMMANDS

### Backend Tests
```bash
# Run all tests
mvn clean test

# Run specific test class
mvn test -Dtest=ToothServiceCalculationServiceTest

# Run with coverage
mvn clean test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

### Frontend Tests
```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest
```

### API Tests
```bash
# Test with curl
curl -X POST http://localhost:8080/api/treatment-plans/1/services/teeth/8 \
  -H "Content-Type: application/json" \
  -d '{"serviceId": 7, "sequenceOrder": 1}'

# Test with Postman
# Import: ODONTOGRAM_POSTMAN_COLLECTION.json
```

---

## 📊 TEST COVERAGE TARGETS

| Component | Target | Method |
|-----------|--------|--------|
| ToothServiceCalculationService | 80%+ | JUnit + Mockito |
| ToothServiceController | 80%+ | JUnit + MockMvc |
| ToothServiceDialog | 70%+ | Espresso |
| GeneralServiceAdapter | 70%+ | Espresso |
| DoctorWorkflowActivity | 60%+ | Espresso |

---

## 🎯 ACCEPTANCE CRITERIA

### Backend
- [ ] All 7 API endpoints work correctly
- [ ] All unit tests pass
- [ ] All integration tests pass
- [ ] Code coverage >= 80%
- [ ] No compilation errors
- [ ] No runtime errors

### Frontend
- [ ] OdontogramView displays correctly
- [ ] ToothServiceDialog works correctly
- [ ] GeneralServicesList works correctly
- [ ] Total cost calculated correctly
- [ ] Error handling works
- [ ] No compilation errors
- [ ] No runtime errors

### Integration
- [ ] Complete workflow works
- [ ] Data persists correctly
- [ ] Performance is acceptable
- [ ] Doctor can complete workflow

### UAT
- [ ] Doctor approves workflow
- [ ] No critical bugs
- [ ] Performance acceptable
- [ ] Ready for deployment

---

## 📝 TEST REPORT TEMPLATE

```
Test Report - Odontogram Tooth Service Implementation
Date: [Date]
Tester: [Name]
Phase: Phase 3 - Testing & QA

Summary:
- Total Tests: [Number]
- Passed: [Number]
- Failed: [Number]
- Skipped: [Number]
- Coverage: [Percentage]

Issues Found:
1. [Issue 1]
2. [Issue 2]
...

Recommendations:
1. [Recommendation 1]
2. [Recommendation 2]
...

Status: [PASS/FAIL]
```

---

## 🚀 DEPLOYMENT CHECKLIST

### Pre-Deployment
- [ ] All tests pass
- [ ] Code review complete
- [ ] Documentation complete
- [ ] Database backup created

### Deployment
- [ ] Run migration script
- [ ] Deploy backend
- [ ] Deploy frontend APK
- [ ] Smoke testing
- [ ] Monitor logs

### Post-Deployment
- [ ] Monitor performance
- [ ] Collect user feedback
- [ ] Fix any issues
- [ ] Document lessons learned

---

## 📞 IMPORTANT NOTES

1. **Test Data:** Use sample patients and treatment plans
2. **Test Environment:** Use test database
3. **API Testing:** Use Postman or curl
4. **UI Testing:** Use Android emulator or physical device
5. **Performance:** Monitor network requests and UI responsiveness

---

## 🎯 SUCCESS CRITERIA

- ✅ All tests pass
- ✅ Code coverage >= 80%
- ✅ No critical bugs
- ✅ Doctor approves workflow
- ✅ Ready for deployment

---

**Status:** 🟢 READY FOR PHASE 3 TESTING

**Next:** Start Day 1 - Unit Tests

