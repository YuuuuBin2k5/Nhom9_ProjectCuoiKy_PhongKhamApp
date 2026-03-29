# FIX 5 & 6: QR Scanner and Patient History - STATUS ⚠️

## Status: PARTIALLY IMPLEMENTED
**Date**: 2026-03-28
**Priority**: HIGH

## FIX 5: QR Scanner for Doctor (UC19)

### Current Status: ✅ UI COMPLETE, ⚠️ BACKEND MISSING

**What's Already Done:**
- ✅ QR Scanner button exists in `activity_doctor_workflow.xml` (line 119-127)
- ✅ Button click handler implemented in `DoctorWorkflowActivity.java` (line 299-303)
- ✅ Launches `PatientQRScannerActivity` with result callback
- ✅ QR scanner activity exists and works

**What's Missing:**
- ⚠️ Need to handle the QR scan result in `onActivityResult` or ActivityResultLauncher
- ⚠️ Need to parse patient ID from QR data
- ⚠️ Need to auto-load patient info after successful scan

**Code Location:**
```java
// File: DoctorWorkflowActivity.java (line 299-303)
btnScanQr.setOnClickListener(v -> {
    Intent intent = new Intent(this, PatientQRScannerActivity.class);
    intent.putExtra(PatientQRScannerActivity.EXTRA_RETURN_RESULT, true);
    qrScannerLauncher.launch(intent);
});
```

**What Needs to Be Added:**
```java
// Need to add ActivityResultLauncher callback
private final ActivityResultLauncher<Intent> qrScannerLauncher = 
    registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            String qrData = result.getData().getStringExtra("qr_data");
            // Parse patient ID from QR (format: "patient:123")
            if (qrData != null && qrData.startsWith("patient:")) {
                String patientId = qrData.substring(8);
                etQrInput.setText(qrData);
                lookupPatient();
            }
        }
    });
```

---

## FIX 6: Patient History Button (UC19)

### Current Status: ✅ UI COMPLETE, ❌ BACKEND API MISSING

**What's Already Done:**
- ✅ "View History" button exists in `activity_doctor_workflow.xml` (line 157-163)
- ✅ Button click handler implemented in `DoctorWorkflowActivity.java` (line 305-310)
- ✅ `BottomSheetMedicalHistory` class exists and implemented
- ✅ UI shows loading, empty state, and list of medical records

**What's Missing:**
- ❌ Backend API endpoint `/api/doctor/patients/{id}/medical-records` does NOT exist
- ❌ `MedicalRecordResponse` model may not match backend structure

**Code Location:**
```java
// File: DoctorWorkflowActivity.java (line 305-310)
btnViewHistory.setOnClickListener(v -> {
    if (currentPatient != null) {
        BottomSheetMedicalHistory bottomSheet = BottomSheetMedicalHistory.newInstance(currentPatient.getId());
        bottomSheet.show(getSupportFragmentManager(), "MedicalHistory");
    }
});
```

**Backend API Needed:**
```java
// File: DoctorController.java
@GetMapping("/patients/{id}/medical-records")
@PreAuthorize("hasRole('DOCTOR')")
public ResponseEntity<?> getPatientMedicalRecords(@PathVariable Long id) {
    // Return list of:
    // - Previous appointments with dates
    // - Diagnoses
    // - Prescriptions
    // - Allergies (highlighted)
    // - Underlying conditions
    // - Blood type
}
```

**Response Model Needed:**
```java
public class MedicalRecordResponse {
    private String date;
    private String doctorName;
    private String diagnosis;
    private String prescription;
    private List<String> allergies;
    private List<String> underlyingConditions;
    private String bloodType;
}
```

---

## Recommendation

### For FIX 5 (QR Scanner):
**Priority: MEDIUM** - UI is ready, just need to wire up the result handler. This is a 5-minute fix.

### For FIX 6 (Patient History):
**Priority: LOW** - Requires backend API development. The UI is ready but won't work until backend is implemented. This is a 30-minute fix (backend + testing).

### Suggested Action:
1. **Skip for now** - Both features have UI ready but need additional work
2. **Focus on FIX 9** (Payment Confirmation) which is more critical for the workflow
3. **Come back later** if time permits

---

## Related Use Cases
- UC19: Doctor Access Medical Record via QR

## Files Involved
- `mobile_android/app/src/main/java/com/hcmute/mobile_android/ui/activities/staff/DoctorWorkflowActivity.java`
- `mobile_android/app/src/main/java/com/hcmute/mobile_android/ui/fragments/BottomSheetMedicalHistory.java`
- `mobile_android/app/src/main/res/layout/activity_doctor_workflow.xml`
- `clinic_backend/src/main/java/com/hcmute/clinic/controller/DoctorController.java` (needs update)
