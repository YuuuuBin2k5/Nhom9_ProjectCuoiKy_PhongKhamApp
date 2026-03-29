# Task 4: Root Cause Found - Step 3 Auto-Starting Bug

## Problem Summary
When editing step 1 (COMPLETED) while step 2 is IN_PROGRESS and step 3 is PENDING:
- After saving, step 3 incorrectly becomes IN_PROGRESS
- Step 3 should remain PENDING

## Root Cause Identified

### From Log Analysis (18:06:31-32)

1. **Mobile sends correct status** ✅
   ```
   → Step 3 (Nhổ răng khôn): OTHER - status=PENDING (preserving)
   ```

2. **Request body is correct** ✅
   ```json
   {"id":3,"sequenceOrder":2,"serviceId":7,"status":"PENDING","toothNumber":"null"}
   ```

3. **Backend returns wrong status** ❌
   ```json
   {"id":3,...,"status":"IN_PROGRESS",...}
   ```

### The Bug: Auto-Complete Logic

When you click "Chỉnh sửa" on a COMPLETED step, the mobile:

1. Calls `PATCH /api/treatment-plans/steps/1/cancel` to reopen step 1
   - Sets step 1 to IN_PROGRESS ✓

2. **Immediately calls `PATCH /api/treatment-plans/steps/1/complete`** ❌
   - This triggers `completeStepAndAdvance()` in backend
   - Which automatically starts the next PENDING step (step 3)!

### Backend Code (TreatmentPlanService.java:455-458)

```java
// Tìm bước tiếp theo (chỉ PENDING, không IN_PROGRESS)
TreatmentPlanStep nextStep = plan.getSteps().stream()
        .filter(s -> s.getStatus() == StepStatus.PENDING)
        .min(Comparator.comparingInt(s -> s.getSequenceOrder() != null ? s.getSequenceOrder() : 0))
        .orElse(null);

// ...

// Kích hoạt bước tiếp theo
nextStep.setStatus(StepStatus.IN_PROGRESS);  // ← This is the problem!
stepRepository.save(nextStep);
```

## Why This Happens

Looking at the mobile code flow when editing a COMPLETED step:

### DoctorWorkflowActivity.onStepEdit() (lines 964-1020)

```java
// CRITICAL FIX: If editing a COMPLETED step, call cancelStep API to revert to IN_PROGRESS
if ("COMPLETED".equals(step.getStatus())) {
    apiService.cancelTreatmentStep(step.getId()).enqueue(new Callback<...>() {
        @Override
        public void onResponse(...) {
            if (response.isSuccessful()) {
                step.setStatus("IN_PROGRESS");
                stepAdapter.notifyDataSetChanged();
                Toast.makeText(..., "Đang chỉnh sửa lại: " + step.getServiceName(), ...).show();
                
                // Continue with edit flow
                continueStepEdit(step);  // ← This is fine
            }
        }
    });
    return;
}
```

### The Problem: Auto-Complete After Edit

After editing, when the user clicks the dialog button (likely "OK" or "Lưu"), the mobile calls:

```java
// From log at 18:06:32.117
--> PATCH http://10.20.1.170:8081/api/treatment-plans/steps/1/complete
{"imageUrls":[],"doctorConclusion":""}
```

This `/complete` endpoint triggers `completeStepAndAdvance()` which:
1. Completes step 1 ✓
2. Finds next PENDING step (step 3)
3. Auto-starts step 3 by setting it to IN_PROGRESS ❌

## The Fix

### Option 1: Don't Auto-Complete When Editing (RECOMMENDED)

When editing a COMPLETED step, after making changes:
- Save the data (PUT /treatment-plans/{id})
- Call `/complete` again to mark it COMPLETED
- **BUT**: Backend should NOT auto-advance if the step was already COMPLETED before

### Option 2: Skip Auto-Advance for Already-Completed Steps

Modify backend `completeStepAndAdvance()` to check if this is a re-completion:
- If step was already COMPLETED before, don't auto-advance
- Only auto-advance when completing a step for the FIRST time

### Option 3: Mobile Should Not Call `/complete` When Editing

When editing a COMPLETED step:
- Call `/cancel` to reopen it
- Make changes
- Save with PUT (which preserves COMPLETED status)
- **Don't call `/complete` again**

## Recommended Solution: Option 3

Modify mobile code to NOT call `/complete` when editing an already-COMPLETED step.

### Changes Needed in DoctorWorkflowActivity.java

1. Track whether we're editing a previously-COMPLETED step
2. When saving, if it was previously COMPLETED:
   - Just save with PUT (status will be COMPLETED)
   - Don't call `/complete` endpoint

### Implementation

```java
private boolean editingPreviouslyCompletedStep = false;

@Override
public void onStepEdit(TreatmentPlan.Step step) {
    // Track if this was previously COMPLETED
    editingPreviouslyCompletedStep = "COMPLETED".equals(step.getStatus());
    
    // ... rest of existing code
}

// When user clicks "Hoàn thành bước" button
private void completeCurrentStep() {
    if (editingPreviouslyCompletedStep) {
        // This step was already completed before
        // Just save the data, don't call /complete endpoint
        saveTreatmentPlanInternal(false, () -> {
            // Mark as completed in memory
            currentStep.setStatus("COMPLETED");
            stepAdapter.notifyDataSetChanged();
            Toast.makeText(this, "Đã lưu thay đổi", Toast.LENGTH_SHORT).show();
            
            // Clear flag
            editingPreviouslyCompletedStep = false;
        });
    } else {
        // Normal completion flow - call /complete endpoint
        // ... existing code
    }
}
```

## Testing Plan

After implementing the fix:

1. Complete step 1
2. Start step 2 (leave it IN_PROGRESS)
3. Edit step 1
4. Save changes
5. **Verify**: Step 3 remains PENDING ✓

## Files to Modify

- `mobile_android/app/src/main/java/com/hcmute/mobile_android/ui/activities/staff/DoctorWorkflowActivity.java`
  - Add `editingPreviouslyCompletedStep` flag
  - Modify `onStepEdit()` to set flag
  - Modify `completeCurrentStep()` to check flag

## Status
✅ Root cause identified
✅ Solution designed
⏳ Implementation needed
⏳ Testing needed

## Summary

The bug is NOT in the status preservation logic. The mobile correctly sends PENDING for step 3.

The bug is that when editing a COMPLETED step, the mobile calls `/complete` again, which triggers backend's auto-advance logic that starts the next PENDING step.

**Solution**: Don't call `/complete` when editing an already-COMPLETED step. Just save the data with PUT.
