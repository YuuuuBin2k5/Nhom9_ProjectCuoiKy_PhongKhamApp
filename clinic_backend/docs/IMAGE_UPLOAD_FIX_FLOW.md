# Image Upload Fix - Flow Diagram

## BEFORE FIX (Problem Flow)

```
┌─────────────────────────────────────────────────────────────────┐
│ User uploads image                                              │
│ → Image URL stored in memory (currentStepImageUrls)            │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ User clicks "Hoàn thành" button                                 │
│ → completeAndGenerateInvoice() called                           │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ Backend API: POST /api/treatment-plans/{id}/complete-and-...   │
│ → Treatment plan status changed to COMPLETED                    │
│ → Plan is now LOCKED (no more updates allowed)                  │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ Activity calls finish()                                         │
│ → onPause() lifecycle method triggered                          │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ onPause() calls saveTreatmentPlanInternal(true)                 │
│ → Tries to save pending image URLs                              │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ Backend API: PUT /api/treatment-plans/{id}                      │
│ → 400 Bad Request ❌                                            │
│ → "Hồ sơ đã hoàn tất và bị khóa, không thể cập nhật"           │
└─────────────────────────────────────────────────────────────────┘
                              ↓
                    ❌ IMAGE LOST! ❌
```

## AFTER FIX (Solution Flow)

```
┌─────────────────────────────────────────────────────────────────┐
│ User uploads image                                              │
│ → Image URL stored in memory (currentStepImageUrls)            │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ User clicks "Hoàn thành" button                                 │
│ → completeAndGenerateInvoice() called                           │
│ → isCompletingTreatment = true (flag set)                       │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ STEP 1: Save treatment plan FIRST                               │
│ → saveTreatmentPlanInternal(true, onDone)                       │
│ → Progress: "Đang lưu hồ sơ..."                                │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ Backend API: PUT /api/treatment-plans/{id}                      │
│ → 200 OK ✅                                                     │
│ → Image URLs saved successfully                                 │
│ → Plan still ACTIVE (not locked yet)                            │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ STEP 2: Complete and generate invoice (onDone callback)         │
│ → Progress: "Đang tạo hóa đơn..."                              │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ Backend API: POST /api/treatment-plans/{id}/complete-and-...   │
│ → 200 OK ✅                                                     │
│ → Treatment plan status changed to COMPLETED                    │
│ → Invoice generated                                             │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ Activity calls finish()                                         │
│ → onPause() lifecycle method triggered                          │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ onPause() checks isCompletingTreatment flag                     │
│ → Flag is TRUE, so SKIP auto-save                               │
│ → No duplicate save attempt                                     │
└─────────────────────────────────────────────────────────────────┘
                              ↓
                    ✅ IMAGE SAVED! ✅
```

## Error Handling Flow

```
┌─────────────────────────────────────────────────────────────────┐
│ User clicks "Hoàn thành"                                        │
│ → isCompletingTreatment = true                                  │
│ → Progress dialog shown: "Đang lưu hồ sơ..."                   │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ STEP 1: Save treatment plan                                     │
│ → saveTreatmentPlanInternal(true, onDone)                       │
└─────────────────────────────────────────────────────────────────┘
                              ↓
                    ┌─────────┴─────────┐
                    │                   │
              ✅ Success            ❌ Failure
                    │                   │
                    ↓                   ↓
    ┌───────────────────────┐  ┌──────────────────────────┐
    │ Call onDone callback  │  │ Show error message       │
    │ → Proceed to Step 2   │  │ Reset flag               │
    │                       │  │ Dismiss dialog           │
    └───────────────────────┘  │ DON'T call onDone        │
                               │ User can retry           │
                               └──────────────────────────┘
```

## Key Changes

### 1. Flag to Prevent Duplicate Save
```java
private boolean isCompletingTreatment = false;

@Override
protected void onPause() {
    if (currentTreatmentPlanId != null && !isCompletingTreatment) {
        saveTreatmentPlanInternal(true);  // Only save if NOT completing
    }
}
```

### 2. Save Before Complete
```java
private void completeAndGenerateInvoice() {
    isCompletingTreatment = true;
    
    // Step 1: Save first
    saveTreatmentPlanInternal(true, () -> {
        // Step 2: Complete only after save succeeds
        apiService.completeAndGenerateInvoice(...);
    });
}
```

### 3. Error Recovery
```java
// In save error handler:
if (isCompletingTreatment) {
    isCompletingTreatment = false;  // Reset flag
    completionProgressDialog.dismiss();  // Cleanup UI
}
// Don't call onDone - prevents completion
```

## Benefits

| Aspect | Before Fix | After Fix |
|--------|-----------|-----------|
| **Image Safety** | ❌ Lost on completion | ✅ Always saved |
| **User Action** | Must remember to save | Automatic |
| **Error Handling** | Silent failure | Clear error message |
| **Data Integrity** | Inconsistent | Guaranteed |
| **Network Efficiency** | Duplicate saves | Single save |
