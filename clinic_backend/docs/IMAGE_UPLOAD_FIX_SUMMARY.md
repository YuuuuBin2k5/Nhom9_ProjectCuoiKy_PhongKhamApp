# Image Upload Loss Fix - Summary

## Problem Description

Images uploaded during treatment were sometimes lost when the doctor clicked "Hoàn thành" (Complete) button to finish the treatment plan and generate an invoice, OR when completing an X-ray step that triggers automatic room transfer.

## Root Cause Analysis

There were TWO scenarios where images could be lost:

### Scenario 1: Completing Treatment Plan (Original Issue)

1. User uploads an image → image URL stored in `currentStepImageUrls` (in memory)
2. Image NOT immediately saved to backend (waiting for manual save or auto-save)
3. User clicks "Hoàn thành" button → calls `completeAndGenerateInvoice()`
4. Backend API marks treatment plan as `COMPLETED` status
5. Activity calls `finish()` which triggers `onPause()` lifecycle method
6. `onPause()` calls `saveTreatmentPlanInternal(true)` to auto-save pending changes
7. Backend rejects the save with **400 Bad Request**: "Hồ sơ đã hoàn tất và bị khóa, không thể cập nhật"
8. **Image URLs are lost!**

### Scenario 2: X-ray Step Auto Room Transfer (User Discovered Issue) ⭐

1. User uploads X-ray images in FragmentXray
2. User clicks complete step button
3. `onStepComplete()` called → `saveTreatmentResult()` (SE_14) saves images to backend ✅
4. Then `performMoveToNextStep()` (SE_15) is called
5. Backend returns `nextRoom` (e.g., "Phòng khám 1")
6. Dialog shown: "Vui lòng hướng dẫn bệnh nhân di chuyển đến..."
7. User clicks OK → `finish()` called immediately
8. `finish()` triggers `onPause()` → `saveTreatmentPlanInternal(true)`
9. **Race condition**: onPause tries to save again while activity is closing
10. **Potential data loss or corruption**

### Why Treatment Plans Are Locked

From `TreatmentPlanController.java` line 212:
```java
if (plan.getStatus() == TreatmentPlanStatus.COMPLETED && !hasInProgressSteps) {
    return ResponseEntity.badRequest().body(Map.of("message", "Hồ sơ đã hoàn tất và bị khóa, không thể cập nhật"));
}
```

Once a treatment plan is marked as `COMPLETED`, it becomes locked to prevent further modifications. This is a security feature to maintain data integrity.

## Solution Implemented

### 1. Save Before Complete (Scenario 1 Fix)

Modified `completeAndGenerateInvoice()` in `DoctorWorkflowActivity.java` to:
- **Save treatment plan FIRST** (including any pending image URLs)
- **Only proceed with completion** after save succeeds
- Show progress: "Đang lưu hồ sơ..." → "Đang tạo hóa đơn..."

```java
private void completeAndGenerateInvoice() {
    isCompletingTreatment = true;
    
    // Step 1: Save treatment plan with pending changes
    saveTreatmentPlanInternal(true, () -> {
        // Step 2: After save succeeds, complete and generate invoice
        apiService.completeAndGenerateInvoice(currentTreatmentPlanId).enqueue(...);
    });
}
```

### 2. Prevent Duplicate Save on Room Transfer (Scenario 2 Fix) ⭐

Modified `performMoveToNextStep()` to set the `isCompletingTreatment` flag before calling `finish()`:

```java
if (nextRoom != null) {
    // CRITICAL FIX: Set flag to prevent onPause auto-save when finishing
    // The step result (including images) was already saved by saveTreatmentResult (SE_14)
    isCompletingTreatment = true;
    
    new AlertDialog.Builder(this)
        .setMessage("Vui lòng hướng dẫn bệnh nhân di chuyển đến " + nextRoom)
        .setPositiveButton("OK", (dialog, id) -> finish())
        .show();
}
```

### 3. Unified Flag System

Added `isCompletingTreatment` flag to prevent `onPause()` from triggering duplicate saves in BOTH scenarios:

```java
@Override
protected void onPause() {
    super.onPause();
    // Don't auto-save if we're completing (already saved before locking)
    // OR if we're finishing after room transfer (already saved by saveTreatmentResult)
    if (currentTreatmentPlanId != null && currentPatient != null && !isCompletingTreatment) {
        saveTreatmentPlanInternal(true);
    }
}
```

### 4. Proper Error Handling

Enhanced error handling in `saveTreatmentPlanInternal()`:
- If save fails during completion, show error message
- Reset `isCompletingTreatment` flag
- Dismiss progress dialog
- Don't proceed with completion (don't call `onDone` callback)

```java
if (onDone != null) {
    Toast.makeText(this, "Không thể lưu hồ sơ: " + response.code(), Toast.LENGTH_SHORT).show();
    if (isCompletingTreatment) {
        isCompletingTreatment = false;
        if (completionProgressDialog != null) {
            completionProgressDialog.dismiss();
        }
    }
}
// Don't call onDone on failure
```

## Files Modified

1. **DoctorWorkflowActivity.java**
   - Added `isCompletingTreatment` flag
   - Added `completionProgressDialog` field
   - Modified `onPause()` to check flag
   - Modified `completeAndGenerateInvoice()` to save first
   - Modified `performMoveToNextStep()` to set flag before finish() ⭐
   - Enhanced error handling in `saveTreatmentPlanInternal()`

## Testing Recommendations

### Test Case 1: Normal Completion Flow
1. Open treatment plan
2. Upload an image to a step
3. Click "Hoàn thành" immediately (without manual save)
4. **Expected**: Image should be saved and visible in completed treatment plan

### Test Case 2: X-ray Room Transfer Flow ⭐
1. Open treatment plan with X-ray step
2. Upload X-ray images
3. Click complete step button
4. **Expected**: Dialog shows "Vui lòng hướng dẫn bệnh nhân di chuyển..."
5. Click OK
6. **Expected**: Images saved, no duplicate save attempt, smooth room transfer

### Test Case 3: Save Failure
1. Open treatment plan
2. Upload an image
3. Disconnect network
4. Click "Hoàn thành"
5. **Expected**: Error message shown, completion cancelled, user can retry

### Test Case 4: Multiple Images
1. Open treatment plan
2. Upload multiple images to different steps
3. Click "Hoàn thành" without saving
4. **Expected**: All images saved before completion

### Test Case 5: Already Saved
1. Open treatment plan
2. Upload image and manually save
3. Click "Hoàn thành"
4. **Expected**: No duplicate save, smooth completion

## Benefits

1. **Data Integrity**: Images are never lost during completion or room transfer
2. **User Experience**: Seamless workflow, no need to remember to save before completing
3. **Error Recovery**: Clear error messages if save fails, user can retry
4. **Performance**: No duplicate saves, efficient network usage
5. **Race Condition Prevention**: Flag system prevents conflicting save operations

## Related Documentation

- `QUEUE_FIX_SUMMARY.md` - Queue management fixes
- `QUEUE_DELAY_VS_SKIP.md` - Queue delay vs skip logic
- `QUEUE_MANAGEMENT_GUIDE.md` - Complete queue management guide
- `IMAGE_UPLOAD_FIX_FLOW.md` - Visual flow diagrams
