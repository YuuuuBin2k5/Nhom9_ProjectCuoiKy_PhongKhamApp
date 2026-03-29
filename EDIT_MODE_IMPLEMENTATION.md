# Edit Mode Implementation - Step Item Save/Delete Buttons

## Status: ✅ COMPLETE

## Overview
Implemented edit mode functionality where the "Chỉnh sửa" button in COMPLETED step items changes to "Lưu" when clicked, and a "Xóa" button appears next to it.

## Changes Made

### 1. TreatmentStepAdapter.java
- Added `editModeMap` to track which steps are in edit mode (Map<Long, Boolean>)
- Added `onStepSave()` to the interface
- Modified `bind()` method to:
  - Accept editModeMap and adapter reference as parameters
  - Check if step is in edit mode
  - Toggle button text between "Chỉnh sửa" and "Lưu"
  - Show/hide "Xóa" button based on edit mode
  - Update editModeMap when buttons are clicked
  - Call `notifyItemChanged()` to refresh the item UI

### 2. DoctorWorkflowActivity.java
- Implemented `onStepSave()` method:
  - Calls `saveTreatmentPlanInternal(false, callback)`
  - Shows success toast
  - Reloads treatment plan to refresh UI
- Implemented `showDeleteStepDialog()`:
  - Shows confirmation dialog
  - Removes step from list
  - Saves changes
  - Shows success toast

## Button Behavior

### COMPLETED Steps
- Default: Shows "Chỉnh sửa" button
- When clicked: 
  - Button text changes to "Lưu"
  - "Xóa" button appears
  - Opens the form for editing
  - Sets editModeMap[stepId] = true
- When "Lưu" clicked:
  - Saves all changes
  - Button text changes back to "Chỉnh sửa"
  - "Xóa" button disappears
  - Sets editModeMap[stepId] = false
  - Reloads data from server
- When "Xóa" clicked:
  - Shows confirmation dialog
  - Removes step from treatment plan
  - Saves changes

### Other Step Statuses
- IN_PROGRESS: Shows "Khám bệnh" and "Hoàn thành" buttons
- PENDING: Shows "Bắt đầu" and "Xóa" buttons
- CANCELLED: Shows "Xem" button only

## Technical Details

### Edit Mode Tracking
```java
private java.util.Map<Long, Boolean> editModeMap = new java.util.HashMap<>();
```

### Button Configuration Logic
```java
case "COMPLETED":
    btnEdit.setVisibility(View.VISIBLE);
    btnEdit.setText(isInEditMode ? "Lưu" : "Chỉnh sửa");
    btnComplete.setVisibility(View.GONE);
    btnRemoveStep.setVisibility(isInEditMode ? View.VISIBLE : View.GONE);
    break;
```

### Save Flow
1. User clicks "Chỉnh sửa" on COMPLETED step
2. Form opens with existing data
3. Button changes to "Lưu", "Xóa" appears
4. User makes changes
5. User clicks "Lưu"
6. `onStepSave()` called → saves → reloads → UI updates
7. Buttons return to default state

## Files Modified
- `mobile_android/app/src/main/java/com/hcmute/mobile_android/adapters/TreatmentStepAdapter.java`
- `mobile_android/app/src/main/java/com/hcmute/mobile_android/ui/activities/staff/DoctorWorkflowActivity.java`

## Testing Checklist
- [ ] Click "Chỉnh sửa" on COMPLETED step → button changes to "Lưu", "Xóa" appears
- [ ] Make changes in form
- [ ] Click "Lưu" → changes saved, buttons return to default
- [ ] Click "Xóa" → confirmation dialog appears
- [ ] Confirm delete → step removed from list
- [ ] Verify data persists after reload
- [ ] Verify odontogram data is preserved when editing

## Notes
- Edit mode is tracked per step using step ID
- Only COMPLETED steps can enter edit mode
- Delete button only shows when in edit mode
- All changes are saved to server immediately
- UI refreshes after save to show latest data
