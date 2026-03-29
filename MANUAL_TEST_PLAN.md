# MANUAL TEST PLAN - NEW FIXES

**Date**: 2026-03-28
**Tester**: _____________
**Device**: _____________
**Android Version**: _____________

---

## Chuẩn Bị

### Backend
```bash
cd clinic_backend
mvn spring-boot:run
```
Backend phải chạy ở `http://localhost:8081`

### Mobile App
```bash
# Build APK mới
cd mobile_android
./gradlew assembleDebug

# Install
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Test Accounts
- **Patient**: `patient01@gmail.com` / `password123`
- **Doctor**: `doc01@gmail.com` / `password123`
- **Doctor X-ray**: `doc_xray@gmail.com` / `password123`

---

## TEST SUITE 1: CRITICAL VALIDATIONS

### ✅ FIX 1: Password Validation

**Objective**: Verify password must contain both letters and numbers

| Test Case | Steps | Expected Result | Status | Notes |
|-----------|-------|-----------------|--------|-------|
| 1.1 | Register with password "abcdefgh" (no numbers) | Error: "Mật khẩu phải bao gồm cả chữ và số" | ☐ Pass ☐ Fail | |
| 1.2 | Register with password "12345678" (no letters) | Error: "Mật khẩu phải bao gồm cả chữ và số" | ☐ Pass ☐ Fail | |
| 1.3 | Register with password "abc12" (< 6 chars) | Error: "Password must be at least 6 characters" | ☐ Pass ☐ Fail | |
| 1.4 | Register with password "password123" | Success | ☐ Pass ☐ Fail | |

---

### ✅ FIX 2: Phone Number Validation

**Objective**: Verify phone number format validation

| Test Case | Steps | Expected Result | Status | Notes |
|-----------|-------|-----------------|--------|-------|
| 2.1 | Login as patient → Profile → Update phone to "123" | Error: "Số điện thoại không hợp lệ" | ☐ Pass ☐ Fail | |
| 2.2 | Update phone to "abc123" | Error: "Số điện thoại không hợp lệ" | ☐ Pass ☐ Fail | |
| 2.3 | Update phone to "0987654321" | Success | ☐ Pass ☐ Fail | |
| 2.4 | Update phone to "+84987654321" | Success | ☐ Pass ☐ Fail | |

---

### ✅ FIX 3: Date of Birth Validation

**Objective**: Verify DOB cannot be in future or too old

| Test Case | Steps | Expected Result | Status | Notes |
|-----------|-------|-----------------|--------|-------|
| 3.1 | Login as patient → Profile → Set DOB to 2030-01-01 | Error: "Ngày sinh không thể là ngày trong tương lai" | ☐ Pass ☐ Fail | |
| 3.2 | Set DOB to 1800-01-01 (> 120 years) | Error: "Ngày sinh không hợp lệ" | ☐ Pass ☐ Fail | |
| 3.3 | Set DOB to 1990-01-01 | Success | ☐ Pass ☐ Fail | |

---

### ✅ FIX 4: Appointment Date Validation

**Objective**: Verify cannot book appointment in the past

| Test Case | Steps | Expected Result | Status | Notes |
|-----------|-------|-----------------|--------|-------|
| 4.1 | Login as patient → Book appointment → Select past date | Error: "Không thể đặt lịch trong quá khứ" | ☐ Pass ☐ Fail | |
| 4.2 | Select today's date (earlier time) | Error: "Không thể đặt lịch trong quá khứ" | ☐ Pass ☐ Fail | |
| 4.3 | Select future date | Success | ☐ Pass ☐ Fail | |

---

## TEST SUITE 2: HIGH PRIORITY FEATURES

### ✅ FIX 5: QR Scanner for Doctor

**Objective**: Verify doctor can scan patient QR and auto-load info

| Test Case | Steps | Expected Result | Status | Notes |
|-----------|-------|-----------------|--------|-------|
| 5.1 | Login as doctor → DoctorWorkflowActivity → Click QR scan button | Camera opens | ☐ Pass ☐ Fail | |
| 5.2 | Scan patient QR code | Patient ID auto-fills in input field | ☐ Pass ☐ Fail | |
| 5.3 | After scan | Patient info loads automatically | ☐ Pass ☐ Fail | |
| 5.4 | Scan invalid QR | Error message displayed | ☐ Pass ☐ Fail | |

---

### ✅ FIX 6: Patient History Button

**Objective**: Verify doctor can view patient medical history

| Test Case | Steps | Expected Result | Status | Notes |
|-----------|-------|-----------------|--------|-------|
| 6.1 | Login as doctor → Look up patient → Click "Xem Lịch sử Khám bệnh" | Bottom sheet opens | ☐ Pass ☐ Fail | |
| 6.2 | View history for patient with completed appointments | List shows: date, doctor name, diagnosis, services, payment status | ☐ Pass ☐ Fail | |
| 6.3 | View history for new patient (no history) | Shows "Không có lịch sử khám bệnh" | ☐ Pass ☐ Fail | |
| 6.4 | Check data accuracy | Data matches actual appointment history | ☐ Pass ☐ Fail | |

---

### ✅ FIX 7: Priority Indicator in Queue

**Objective**: Verify priority patients have visual indicators

| Test Case | Steps | Expected Result | Status | Notes |
|-----------|-------|-----------------|--------|-------|
| 7.1 | Login as staff → View queue with priority patient (priority > 5) | ⭐ star icon visible | ☐ Pass ☐ Fail | |
| 7.2 | Check normal patient (priority ≤ 5) | No star icon | ☐ Pass ☐ Fail | |
| 7.3 | Check wait time display | Shows "~X phút" | ☐ Pass ☐ Fail | |
| 7.4 | Check color coding - WAITING status | Light green background (#E8F5E9) | ☐ Pass ☐ Fail | |
| 7.5 | Check color coding - IN_PROGRESS status | Light blue background (#E3F2FD) | ☐ Pass ☐ Fail | |
| 7.6 | Check color coding - RETURNED_PRIORITY status | Light orange background (#FFF3E0) | ☐ Pass ☐ Fail | |
| 7.7 | Check color coding - PAUSED_FOR_TEST status | Light purple background (#F3E5F5) | ☐ Pass ☐ Fail | |

---

### ✅ FIX 8: Room Transfer Notification

**Objective**: Verify notification shows detailed room transfer info

| Test Case | Steps | Expected Result | Status | Notes |
|-----------|-------|-----------------|--------|-------|
| 8.1 | Complete treatment step requiring room transfer | Notification received | ☐ Pass ☐ Fail | |
| 8.2 | Check notification content | Contains: 📍 Room name & location | ☐ Pass ☐ Fail | |
| 8.3 | Check notification content | Contains: 🔹 Next service name | ☐ Pass ☐ Fail | |
| 8.4 | Check notification content | Contains: 🎫 Queue number | ☐ Pass ☐ Fail | |
| 8.5 | Check notification content | Contains: ⏱️ Estimated wait time | ☐ Pass ☐ Fail | |
| 8.6 | For priority patient | Contains priority message | ☐ Pass ☐ Fail | |

---

### ✅ FIX 9: Payment Confirmation Workflow

**Objective**: Verify payment workflow works correctly

| Test Case | Steps | Expected Result | Status | Notes |
|-----------|-------|-----------------|--------|-------|
| 9.1 | Login as patient → View invoice | Invoice details displayed | ☐ Pass ☐ Fail | |
| 9.2 | Click "Thanh toán" → Select payment method | 5 options: CASH, BANK_TRANSFER, CREDIT_CARD, MOMO, ZALOPAY | ☐ Pass ☐ Fail | |
| 9.3 | Select CASH → Confirm | Confirmation dialog appears | ☐ Pass ☐ Fail | |
| 9.4 | Confirm payment | Success message displayed | ☐ Pass ☐ Fail | |
| 9.5 | Check invoice status | Status changed to PAID | ☐ Pass ☐ Fail | |
| 9.6 | Try to pay again | Error: "Invoice already paid" | ☐ Pass ☐ Fail | |
| 9.7 | After payment | Option to submit review appears | ☐ Pass ☐ Fail | |

---

## TEST SUITE 3: INTEGRATION TESTS

### End-to-End Workflow Test

**Scenario**: Complete patient journey from registration to payment

| Step | Action | Expected Result | Status | Notes |
|------|--------|-----------------|--------|-------|
| 1 | Register new patient with valid data | Success | ☐ Pass ☐ Fail | |
| 2 | Login as patient | Dashboard loads | ☐ Pass ☐ Fail | |
| 3 | Book appointment for future date | Success | ☐ Pass ☐ Fail | |
| 4 | Check-in via QR code | Added to queue | ☐ Pass ☐ Fail | |
| 5 | Doctor scans patient QR | Patient info loads | ☐ Pass ☐ Fail | |
| 6 | Doctor views patient history | History displayed (empty for new patient) | ☐ Pass ☐ Fail | |
| 7 | Doctor creates treatment plan | Plan saved | ☐ Pass ☐ Fail | |
| 8 | Complete treatment step | Room transfer notification sent | ☐ Pass ☐ Fail | |
| 9 | Check queue display | Priority indicator visible if applicable | ☐ Pass ☐ Fail | |
| 10 | Complete all treatment steps | Invoice generated | ☐ Pass ☐ Fail | |
| 11 | Patient pays invoice | Payment successful | ☐ Pass ☐ Fail | |
| 12 | Patient submits review | Review saved | ☐ Pass ☐ Fail | |

---

## TEST SUITE 4: BACKEND API TESTS

### Automated Tests

Run the automated test script:

```bash
chmod +x test_new_fixes.sh
./test_new_fixes.sh
```

**Expected Output**: All tests should pass

| Test | Status | Notes |
|------|--------|-------|
| Password validation tests | ☐ Pass ☐ Fail | |
| Phone validation tests | ☐ Pass ☐ Fail | |
| DOB validation tests | ☐ Pass ☐ Fail | |
| Appointment date validation tests | ☐ Pass ☐ Fail | |
| Medical records API tests | ☐ Pass ☐ Fail | |

---

## REGRESSION TESTS

### Verify existing features still work

| Feature | Test | Status | Notes |
|---------|------|--------|-------|
| Login | Patient can login | ☐ Pass ☐ Fail | |
| Login | Doctor can login | ☐ Pass ☐ Fail | |
| Appointment | Can book appointment | ☐ Pass ☐ Fail | |
| Check-in | QR check-in works | ☐ Pass ☐ Fail | |
| Queue | Queue displays correctly | ☐ Pass ☐ Fail | |
| Treatment | Doctor can create treatment plan | ☐ Pass ☐ Fail | |
| Treatment | Can complete treatment steps | ☐ Pass ☐ Fail | |
| X-ray | X-ray workflow works | ☐ Pass ☐ Fail | |
| Invoice | Invoice generation works | ☐ Pass ☐ Fail | |
| Review | Can submit review | ☐ Pass ☐ Fail | |

---

## BUG REPORT TEMPLATE

If any test fails, document here:

### Bug #1
- **Test Case**: _____________
- **Steps to Reproduce**: 
  1. 
  2. 
  3. 
- **Expected Result**: _____________
- **Actual Result**: _____________
- **Screenshots**: _____________
- **Priority**: ☐ Critical ☐ High ☐ Medium ☐ Low

---

## SIGN-OFF

- **Tester Name**: _____________
- **Date**: _____________
- **Overall Status**: ☐ All Pass ☐ Some Failures
- **Ready for Production**: ☐ Yes ☐ No
- **Comments**: _____________

---

## NOTES

- Test on multiple devices if possible (different screen sizes, Android versions)
- Test with slow network connection
- Test with airplane mode (offline scenarios)
- Test with different user roles (patient, doctor, admin, staff)
- Document any unexpected behavior even if not a failure
