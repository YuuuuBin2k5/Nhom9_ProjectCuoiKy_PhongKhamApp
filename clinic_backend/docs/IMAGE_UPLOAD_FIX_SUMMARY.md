# Image Upload Loss Fix - Summary

## Problem Description

Images uploaded during treatment were sometimes lost when the doctor clicked "Hoàn thành" (Complete) button to finish the treatment plan and generate an invoice.

## Root Cause Analysis

The issue occurred due to a race condition in the completion workflow:

1. User uploads an image → image URL stored in `currentStepImageUrls` (in memory)
2. Image NOT immediately saved to backend (waiting for manual save or auto-save)
3. User clicks "Hoàn thành" button → calls `completeAndGenerateInvoice()`
4. Backend API marks treatment plan as `COMPLETED` status
5. Activity calls `finish()` which triggers `onPause()` lifecycle method
6. `onPause()` calls `saveTreatmentPlanInternal(true)` to auto-save pending changes
7. Backend rejects the save with **400 Bad Request**: "Hồ sơ đã hoàn tất và bị khóa, không thể cập nhật"
8. **Image URLs are lost!**

### Why Treatment Plans Are Locked

From `TreatmentPlanController.java` line 212:
```java
if (plan.getStatus() == TreatmentPlanStatus.COMPLETED && !hasInProgressSteps) {
    return ResponseEntity.badRequest().body(Map.of("message", "Hồ sơ đã hoàn tất và bị khóa, không thể cập nhật"));
}
```

Once a treatment plan is marked as `COMPLETED`, it becomes locked to prevent further modifications. This is a security feature to maintain data integrity.

## Solution Implemented

### 1. Save Before Complete

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

### 2. Prevent Duplicate Save

Added `isCompletingTreatment` flag to prevent `onPause()` from triggering a duplicate save during the completion process:

```java
@Override
protected void onPause() {
    super.onPause();
    // Don't auto-save if we're completing (already saved before locking)
    if (currentTreatmentPlanId != null && currentPatient != null && !isCompletingTreatment) {
        saveTreatmentPlanInternal(true);
    }
}
```

### 3. Proper Error Handling

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
   - Enhanced error handling in `saveTreatmentPlanInternal()`

## Testing Recommendations

### Test Case 1: Normal Flow
1. Open treatment plan
2. Upload an image to a step
3. Click "Hoàn thành" immediately (without manual save)
4. **Expected**: Image should be saved and visible in completed treatment plan

### Test Case 2: Save Failure
1. Open treatment plan
2. Upload an image
3. Disconnect network
4. Click "Hoàn thành"
5. **Expected**: Error message shown, completion cancelled, user can retry

### Test Case 3: Multiple Images
1. Open treatment plan
2. Upload multiple images to different steps
3. Click "Hoàn thành" without saving
4. **Expected**: All images saved before completion

### Test Case 4: Already Saved
1. Open treatment plan
2. Upload image and manually save
3. Click "Hoàn thành"
4. **Expected**: No duplicate save, smooth completion

## Benefits

1. **Data Integrity**: Images are never lost during completion
2. **User Experience**: Seamless workflow, no need to remember to save before completing
3. **Error Recovery**: Clear error messages if save fails, user can retry
4. **Performance**: No duplicate saves, efficient network usage

## Related Documentation

- `QUEUE_FIX_SUMMARY.md` - Queue management fixes
- `QUEUE_DELAY_VS_SKIP.md` - Queue delay vs skip logic
- `QUEUE_MANAGEMENT_GUIDE.md` - Complete queue management guide
