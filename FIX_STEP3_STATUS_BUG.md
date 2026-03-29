# Fix: Step 3 Incorrectly Becoming IN_PROGRESS When Editing Step 1

## Problem
When editing and saving step 1 (COMPLETED) while step 2 is IN_PROGRESS and step 3 is PENDING:
- After save, step 3 incorrectly becomes IN_PROGRESS
- Step 3 should remain PENDING

## Root Cause Analysis

### Backend Behavior
The backend `updateSteps` method (TreatmentPlanService.java:173-180) accepts and applies ANY status sent by mobile:
```java
if (item.getStatus() != null) {
    try {
        step.setStatus(StepStatus.valueOf(item.getStatus().toUpperCase()));
    } catch (IllegalArgumentException ignored) {}
}
```

This means if mobile sends wrong status, backend will apply it.

### Mobile Behavior
The mobile `saveTreatmentPlanInternal` method sends ALL steps with their current in-memory status. If the in-memory status is wrong, it will be saved to backend.

## Solution Implemented

### Enhanced Logging
Added comprehensive logging to track:
1. What `editingStep` and `currentStep` are before save
2. What status is being sent for each step
3. Whether each step is being edited, is current, or is "other"
4. Backend response code

### Code Changes in DoctorWorkflowActivity.java

**Location**: `saveTreatmentPlanInternal` method (lines ~145-185)

**Changes**:
1. Added detailed logging header showing editingStep and currentStep
2. For each step, log whether it's EDITING, CURRENT, or OTHER
3. Explicitly log the status being sent for each step with "(preserving)" note for other steps
4. Added logging for backend response

**Key Logic**:
```java
boolean isEditingThisStep = (editingStep != null && s.getId() != null && s.getId().equals(editingStep.getId()));
boolean isCurrentThisStep = (currentStep != null && s.getId() != null && s.getId().equals(currentStep.getId()));

if (isEditingThisStep) {
    statusToSend = s.getStatus();
    Log.d("DoctorWorkflow", "→ Step " + s.getId() + " (" + s.getServiceName() + "): EDITING - status=" + statusToSend);
} else if (isCurrentThisStep) {
    statusToSend = s.getStatus();
    Log.d("DoctorWorkflow", "→ Step " + s.getId() + " (" + s.getServiceName() + "): CURRENT - status=" + statusToSend);
} else {
    // CRITICAL: For other steps, preserve their status exactly as-is
    statusToSend = s.getStatus();
    Log.d("DoctorWorkflow", "→ Step " + s.getId() + " (" + s.getServiceName() + "): OTHER - status=" + statusToSend + " (preserving)");
}
```

## Testing Plan

### Test Scenario
1. Create treatment plan with 3 steps:
   - Step 1: Khám và tư vấn (General Dental)
   - Step 2: Chụp X-quang (X-ray)
   - Step 3: Nhổ răng khôn (Surgery)

2. Complete step 1 (status = COMPLETED)

3. Start step 2 (status = IN_PROGRESS)

4. Verify step 3 is PENDING

5. Click "Chỉnh sửa" on step 1

6. Make a change and save

7. Check logcat for:
   ```
   === saveTreatmentPlanInternal: Preparing request ===
   editingStep: Khám và tư vấn (ID: 1)
   currentStep: Chụp X-quang (ID: 2)
   → Step 1 (Khám và tư vấn): EDITING - status=IN_PROGRESS
   → Step 2 (Chụp X-quang): CURRENT - status=IN_PROGRESS
   → Step 3 (Nhổ răng khôn): OTHER - status=PENDING (preserving)
   ```

8. After reload, verify:
   - Step 1: COMPLETED (after re-completing)
   - Step 2: IN_PROGRESS (unchanged)
   - Step 3: PENDING (unchanged) ✓

### Expected Log Output

**Before Save**:
```
=== saveTreatmentPlanInternal: Preparing request ===
editingStep: Khám và tư vấn (ID: 1)
currentStep: Chụp X-quang (ID: 2)
→ Step 1 (Khám và tư vấn): EDITING - status=IN_PROGRESS
→ Step 2 (Chụp X-quang): CURRENT - status=IN_PROGRESS
→ Step 3 (Nhổ răng khôn): OTHER - status=PENDING (preserving)
=== Sending 3 steps to backend ===
```

**After Save**:
```
=== Save response received ===
Response code: 200
Success: true
```

**After Reload**:
```
=== loadTreatmentPlanForRoom ===
Received 3 steps from backend:
  - Step ID=1, Service=Khám và tư vấn, Status=IN_PROGRESS
  - Step ID=2, Service=Chụp X-quang, Status=IN_PROGRESS
  - Step ID=3, Service=Nhổ răng khôn, Status=PENDING
```

## Next Steps

1. Build new APK with enhanced logging
2. Install on device
3. Run test scenario
4. Collect logcat output
5. Analyze logs to determine:
   - Is mobile sending wrong status?
   - Is backend modifying status?
   - Where is the bug occurring?

## Files Modified
- `mobile_android/app/src/main/java/com/hcmute/mobile_android/ui/activities/staff/DoctorWorkflowActivity.java`
  - Enhanced logging in `saveTreatmentPlanInternal` method
  - Added response logging
  - Clarified status preservation logic

## Status
✅ Code changes complete
✅ APK built: `app-debug-fix-step3-status.apk`
✅ Testing guide created: `HUONG_DAN_TEST_FIX_STEP3.md`
⏳ Waiting for user testing and log collection

## Summary

This fix adds comprehensive logging to diagnose why step 3 is incorrectly becoming IN_PROGRESS when editing step 1. The enhanced logging will show:

1. **Before save**: What status is being sent for each step
2. **After save**: Backend response code
3. **After reload**: What status backend returns for each step

This will help us determine whether the bug is in:
- Mobile (sending wrong status)
- Backend (modifying status incorrectly)
- Or both

Once we have the logs, we can implement the appropriate fix.
