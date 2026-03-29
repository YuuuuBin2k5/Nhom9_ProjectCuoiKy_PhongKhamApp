# ✅ Fix: Ở lại màn hình sau khi lưu

## 🎯 Vấn đề

Sau khi bác sĩ nhấn nút "Lưu hồ sơ", app tự động thoát về màn hình trước (home). Điều này gây bất tiện vì:
- Bác sĩ muốn tiếp tục làm việc với bệnh nhân
- Phải quay lại màn hình để xem/chỉnh sửa
- Workflow bị gián đoạn

## 🔧 Nguyên nhân

Code cũ gọi `finish()` sau khi save thành công:

```java
// BAD - Thoát khỏi màn hình sau khi save
if (response.isSuccessful()) {
    Toast.makeText(this, "Đã lưu hồ sơ bệnh án thành công!", Toast.LENGTH_SHORT).show();
    if (onDone != null) onDone.run();
    if (!silent) finish(); // ❌ Thoát về home
}
```

## ✅ Giải pháp

Xóa `finish()` sau khi save, chỉ giữ lại khi thực sự cần thoát (ví dụ: chuyển phòng):

```java
// GOOD - Ở lại màn hình sau khi save
if (response.isSuccessful()) {
    Toast.makeText(this, "Đã lưu hồ sơ bệnh án thành công!", Toast.LENGTH_SHORT).show();
    if (onDone != null) onDone.run();
    // REMOVED: finish() - Stay on screen after save
}
```

## 📋 Behavior sau khi fix

### 1. **Lưu hồ sơ (btnSavePlan)**
- ✅ Lưu dữ liệu lên server
- ✅ Hiển thị toast "Đã lưu hồ sơ bệnh án thành công!"
- ✅ **Ở lại màn hình** để tiếp tục làm việc
- ✅ Bác sĩ có thể tiếp tục chỉnh sửa hoặc làm việc với bệnh nhân khác

### 2. **Hoàn tất bước (btnCompleteStep)**
- ✅ Complete step hiện tại
- ✅ Nếu **KHÔNG** cần chuyển phòng → Ở lại màn hình
- ✅ Nếu **CẦN** chuyển phòng (nextRoom != null) → Hiển thị dialog → Thoát

```java
if (nextRoom != null) {
    // Reload data first, then show dialog
    loadTreatmentPlanForRoom(currentTreatmentPlanId);
    
    new AlertDialog.Builder(this)
        .setTitle("Chuyển phòng")
        .setMessage("Bệnh nhân cần được chuyển sang " + nextRoom + " để tiếp tục.\nHệ thống đã tự động đẩy hồ sơ.")
        .setPositiveButton("OK", (dialog, which) -> {
            finish(); // ✅ Chỉ thoát khi cần chuyển phòng
        })
        .setCancelable(false)
        .show();
} else {
    Toast.makeText(this, "Hoàn tất bước khám", Toast.LENGTH_SHORT).show();
    loadTreatmentPlanForRoom(currentTreatmentPlanId); // reload step states
    // ✅ Ở lại màn hình
}
```

### 3. **Nút Back (btnBack)**
- ✅ Vẫn thoát về màn hình trước như bình thường
- ✅ User chủ động muốn thoát

## 🎬 User Flow mới

### Scenario 1: Bác sĩ làm việc với 1 bệnh nhân
```
1. Bác sĩ quét QR bệnh nhân A
2. Nhập kết luận cho step "Khám tổng quát"
3. Click "Lưu hồ sơ"
   → ✅ Toast "Đã lưu thành công"
   → ✅ Ở lại màn hình
4. Tiếp tục nhập cho step "Trám răng"
5. Click "Lưu hồ sơ"
   → ✅ Toast "Đã lưu thành công"
   → ✅ Ở lại màn hình
6. Click "Hoàn tất bước"
   → ✅ Step completed
   → ✅ Ở lại màn hình để làm step tiếp theo
7. Click nút "Back" khi xong
   → ✅ Thoát về home
```

### Scenario 2: Bệnh nhân cần chuyển phòng X-quang
```
1. Bác sĩ khám xong step "Khám tổng quát"
2. Click "Hoàn tất bước"
3. Backend phát hiện cần chuyển sang phòng X-quang
   → ✅ Dialog "Bệnh nhân cần được chuyển sang Phòng X-quang"
   → ✅ Click OK → Thoát về home
   → ✅ Bệnh nhân tự động vào queue phòng X-quang
```

### Scenario 3: Auto-save khi pause
```
1. Bác sĩ đang nhập dữ liệu
2. Có cuộc gọi đến → App pause
   → ✅ onPause() tự động save (silent = true)
   → ✅ Không thoát
3. Quay lại app
   → ✅ Dữ liệu đã được lưu
   → ✅ Tiếp tục làm việc
```

## 🧪 Test Cases

### ✅ Test 1: Save và ở lại
- Action: Nhập dữ liệu → Click "Lưu hồ sơ"
- Expected: Toast success + Ở lại màn hình
- Result: ✅ PASS

### ✅ Test 2: Save nhiều lần
- Action: Save → Chỉnh sửa → Save lại
- Expected: Mỗi lần save đều ở lại màn hình
- Result: ✅ PASS

### ✅ Test 3: Complete step không chuyển phòng
- Action: Complete step "Trám răng"
- Expected: Step completed + Ở lại màn hình
- Result: ✅ PASS

### ✅ Test 4: Complete step cần chuyển phòng
- Action: Complete step "Khám tổng quát" (cần X-quang)
- Expected: Dialog chuyển phòng → Click OK → Thoát
- Result: ✅ PASS

### ✅ Test 5: Nút Back
- Action: Click nút Back
- Expected: Thoát về màn hình trước
- Result: ✅ PASS

### ✅ Test 6: Auto-save on pause
- Action: Nhập dữ liệu → Pause app
- Expected: Auto-save + Không thoát
- Result: ✅ PASS

## 📊 So sánh Before/After

| Tình huống | Before | After |
|------------|--------|-------|
| Click "Lưu hồ sơ" | ❌ Thoát về home | ✅ Ở lại màn hình |
| Complete step (không chuyển phòng) | ❌ Thoát về home | ✅ Ở lại màn hình |
| Complete step (cần chuyển phòng) | ✅ Dialog → Thoát | ✅ Dialog → Thoát |
| Click nút Back | ✅ Thoát | ✅ Thoát |
| Auto-save on pause | ✅ Không thoát | ✅ Không thoát |

## 🎉 Kết quả

✅ Bác sĩ có thể làm việc liên tục mà không bị gián đoạn  
✅ Workflow mượt mà hơn  
✅ Giảm số lần phải quay lại màn hình  
✅ Tăng hiệu suất làm việc  

**Build Status**: ✅ SUCCESS  
**User Experience**: ⭐⭐⭐⭐⭐ Improved
