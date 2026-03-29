# FIX: CẬP NHẬT UI TRƯỚC KHI HIỂN THỊ DIALOG CHUYỂN PHÒNG

## 🐛 VẤN ĐỀ

Khi nhấn "Hoàn thành" và có chuyển phòng:
1. Backend complete step thành công ✅
2. Mobile hiển thị dialog "Chuyển phòng" ✅
3. Nhưng UI KHÔNG cập nhật step status → Step vẫn hiển thị IN_PROGRESS ❌
4. Khi đóng dialog và finish activity, user không thấy step đã COMPLETED

## 🔍 NGUYÊN NHÂN

Code cũ trong `onStepComplete()`:

```java
if (nextRoom != null) {
    // Hiển thị dialog ngay lập tức
    new AlertDialog.Builder(this)
        .setTitle("Chuyển phòng")
        .setMessage("...")
        .setPositiveButton("OK", (dialog, which) -> {
            finish(); // Đóng activity luôn
        })
        .show();
} else {
    // Chỉ reload khi KHÔNG chuyển phòng
    loadTreatmentPlanForRoom(currentTreatmentPlanId);
}
```

**Vấn đề:** 
- Khi có `nextRoom`, không reload data → UI không update
- Dialog hiển thị → User nhấn OK → Activity finish
- User không thấy step đã COMPLETED

## ✅ GIẢI PHÁP

Update UI NGAY SAU KHI API thành công, TRƯỚC KHI hiển thị dialog:

```java
@Override
public void onResponse(...) {
    if (response.isSuccessful() && response.body() != null) {
        String msg = response.body().getMessage();
        String nextRoom = response.body().getNextRoomName();
        
        // 1. Update local step status IMMEDIATELY
        step.setStatus("COMPLETED");
        stepAdapter.notifyDataSetChanged();
        btnCompleteStep.setVisibility(View.GONE);
        currentStep = null;
        
        // 2. Reload data from server
        if (nextRoom != null) {
            loadTreatmentPlanForRoom(currentTreatmentPlanId);
            
            // 3. THEN show dialog
            new AlertDialog.Builder(this)
                .setTitle("Chuyển phòng")
                .setMessage("Bệnh nhân cần được chuyển sang " + nextRoom + " để tiếp tục.\nHệ thống đã tự động đẩy hồ sơ.")
                .setPositiveButton("OK", (dialog, which) -> {
                    finish();
                })
                .show();
        } else {
            Toast.makeText(this, "Hoàn tất bước khám", Toast.LENGTH_SHORT).show();
            loadTreatmentPlanForRoom(currentTreatmentPlanId);
        }
    }
}
```

## 📊 WORKFLOW MỚI

### Trước (Bug):
```
User nhấn "Hoàn thành"
  ↓
API complete step → SUCCESS
  ↓
Show dialog "Chuyển phòng" (UI chưa update) ❌
  ↓
User nhấn OK → finish()
  ↓
Activity đóng (step vẫn hiển thị IN_PROGRESS)
```

### Sau (Fixed):
```
User nhấn "Hoàn thành"
  ↓
API complete step → SUCCESS
  ↓
Update local step.status = "COMPLETED" ✅
  ↓
stepAdapter.notifyDataSetChanged() ✅
  ↓
loadTreatmentPlanForRoom() ✅
  ↓
Show dialog "Chuyển phòng" (UI đã update) ✅
  ↓
User nhấn OK → finish()
  ↓
Activity đóng (step hiển thị COMPLETED)
```

## 🎯 LỢI ÍCH

1. ✅ User thấy step chuyển sang COMPLETED ngay lập tức
2. ✅ UI phản hồi nhanh, không phải đợi reload
3. ✅ Data được sync với server qua `loadTreatmentPlanForRoom()`
4. ✅ Consistent behavior cho cả 2 cases (có/không chuyển phòng)

## 🧪 TEST CASES

### Test 1: Complete step với chuyển phòng
```
GIVEN: Step 1 IN_PROGRESS, Step 2 PENDING (phòng khác)

WHEN: Bác sĩ nhấn "Hoàn thành" step 1

THEN:
  1. Step 1 UI chuyển sang COMPLETED ngay lập tức ✅
  2. Button "Hoàn thành" ẩn đi ✅
  3. Dialog "Chuyển phòng sang X-quang" hiển thị ✅
  4. Step 2 chuyển sang IN_PROGRESS (sau reload) ✅
  5. User nhấn OK → Activity finish ✅
```

### Test 2: Complete step không chuyển phòng
```
GIVEN: Step 1 IN_PROGRESS, Step 2 PENDING (cùng phòng)

WHEN: Bác sĩ nhấn "Hoàn thành" step 1

THEN:
  1. Step 1 UI chuyển sang COMPLETED ngay lập tức ✅
  2. Button "Hoàn thành" ẩn đi ✅
  3. Toast "Hoàn tất bước khám" hiển thị ✅
  4. Step 2 chuyển sang IN_PROGRESS (sau reload) ✅
  5. Activity vẫn mở, hiển thị step 2 ✅
```

### Test 3: Complete step cuối cùng
```
GIVEN: Step 2 IN_PROGRESS, không còn step nào

WHEN: Bác sĩ nhấn "Hoàn thành" step 2

THEN:
  1. Step 2 UI chuyển sang COMPLETED ngay lập tức ✅
  2. Button "Hoàn thành" ẩn đi ✅
  3. Toast "Hoàn tất bước khám" hiển thị ✅
  4. Plan status = COMPLETED (sau reload) ✅
  5. Activity vẫn mở, không còn step nào để làm ✅
```

## 📝 FILES MODIFIED

1. `mobile_android/app/src/main/java/com/hcmute/mobile_android/ui/activities/staff/DoctorWorkflowActivity.java`
   - Method: `onStepComplete()` - Line ~850
   - Added immediate UI update before showing dialog
   - Added reload for both cases (with/without room transfer)

## ✅ STATUS

- [x] Mobile compiled successfully
- [ ] Test scenario 1: Complete with room transfer
- [ ] Test scenario 2: Complete without room transfer
- [ ] Test scenario 3: Complete last step
- [ ] Verify UI updates immediately

## 🚀 NEXT STEPS

1. Install APK mới
2. Test workflow đầy đủ:
   - Complete step 1 → Verify UI shows COMPLETED
   - Dialog "Chuyển phòng" hiển thị
   - Step 2 tự động IN_PROGRESS
3. Test cùng phòng:
   - Complete step không chuyển phòng
   - Verify UI updates correctly

## 💡 TECHNICAL NOTES

**Tại sao update local trước khi reload?**
- Immediate feedback cho user (responsive UI)
- Không phải đợi network request
- Reload sau đó để sync với server (source of truth)

**Tại sao gọi reload cho cả 2 cases?**
- Consistency: Cùng behavior cho mọi trường hợp
- Đảm bảo data sync với server
- Update next step status (PENDING → IN_PROGRESS)

**Tại sao không dùng callback từ reload?**
- Dialog có thể hiển thị ngay (không block UI)
- Reload chạy async, không ảnh hưởng UX
- User thấy update ngay lập tức
