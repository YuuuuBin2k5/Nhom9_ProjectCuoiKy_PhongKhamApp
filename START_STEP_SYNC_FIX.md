# Fix: Đồng bộ dữ liệu sau khi Start Step

## Vấn đề 1: UI không đồng bộ sau khi Start Step

Sau khi nhấn nút "Bắt đầu" để start một step (PENDING → IN_PROGRESS), UI không đồng bộ được với server.

### Root Cause

Trong `DoctorWorkflowActivity.onStepEdit()`, sau khi gọi API `startTreatmentStep()` thành công, code chỉ:
1. Set status local: `step.setStatus("IN_PROGRESS")`
2. Notify adapter: `stepAdapter.notifyDataSetChanged()`

Không reload lại treatment plan từ server để đồng bộ dữ liệu mới nhất.

### Giải pháp

Reload treatment plan từ server sau khi start step thành công, update `currentStep` reference, và gọi `continueStepEditAfterSync()`.

---

## Vấn đề 2: Dữ liệu không được lưu khi nhấn "Hoàn thành"

User điền thông tin vào fragment, nhấn "Hoàn thành", nhưng dữ liệu không được lưu vào database.

### Root Cause

1. User nhấn "Bắt đầu" → start step thành công
2. Reload treatment plan → `autoLoadInProgressStep()` cache dữ liệu từ các bước COMPLETED
3. `continueStepEditAfterSync()` → `switchToTabForStep()` → load fragment
4. Fragment được load → `autoPopulateFragmentFromCache()` được gọi
5. **BUG**: Fragment bị set READ-ONLY mode vì có cached data từ bước COMPLETED khác (cùng template type)
6. User nhập dữ liệu nhưng fragment ở READ-ONLY → dữ liệu không được lưu

Logic auto-populate đang populate dữ liệu từ bước COMPLETED khác vào bước IN_PROGRESS hiện tại, và set READ-ONLY mode luôn.

### Giải pháp

Chỉ auto-populate và set READ-ONLY khi user đang XEM bước COMPLETED, KHÔNG set READ-ONLY khi đang EDIT bước IN_PROGRESS.

```java
private void autoPopulateFragmentFromCache(Fragment fragment, String templateKey) {
    // CRITICAL: Only auto-populate if current step is COMPLETED
    // Do NOT populate for IN_PROGRESS steps (user is actively editing)
    if (currentStep == null || !"COMPLETED".equals(currentStep.getStatus())) {
        android.util.Log.w("DoctorWorkflow", "⚠️ Current step is not COMPLETED, skipping auto-populate");
        return;
    }
    
    // ... rest of logic
}
```

---

## Files Changed

- `mobile_android/app/src/main/java/com/hcmute/mobile_android/ui/activities/staff/DoctorWorkflowActivity.java`
  - Refactor `onStepEdit()` để reload treatment plan sau khi start step
  - Thêm method mới `continueStepEditAfterSync()` để xử lý logic chung
  - Update `currentStep` reference sau khi reload
  - **FIX**: Chỉ auto-populate và set READ-ONLY khi `currentStep.status == COMPLETED`

## Benefits

1. **Đồng bộ hoàn toàn**: UI luôn phản ánh đúng trạng thái từ server
2. **Tránh stale data**: `currentStep` reference luôn được update
3. **Dữ liệu được lưu**: Fragment không bị lock READ-ONLY khi đang edit IN_PROGRESS step
4. **UX tốt hơn**: User có thể nhập dữ liệu và lưu thành công

## Status

✅ **COMPLETED** - Cả 2 vấn đề đã được fix thành công
