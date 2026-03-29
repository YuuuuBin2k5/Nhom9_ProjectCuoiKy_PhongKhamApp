# Tự Động Lưu Khi Nhấn Nút Quay Về

## Vấn Đề

Khi bác sĩ nhấn nút quay về (back button hoặc mũi tên ở góc trên) trong DoctorWorkflowActivity, dữ liệu chưa được tự động lưu, dẫn đến mất dữ liệu nếu bác sĩ quên nhấn "Lưu".

## Giải Pháp

Thêm logic tự động lưu trước khi thoát activity.

### 1. Sửa Listener của Nút Back

**Trước:**
```java
findViewById(R.id.btnBack).setOnClickListener(v -> finish());
```

**Sau:**
```java
findViewById(R.id.btnBack).setOnClickListener(v -> handleBackPress());
```

### 2. Thêm Method handleBackPress()

```java
private void handleBackPress() {
    android.util.Log.d("DoctorWorkflow", "handleBackPress called");
    
    // If no patient or no treatment plan, just exit
    if (currentPatient == null || currentTreatmentPlanId == null) {
        finish();
        return;
    }
    
    // If currently saving, wait
    if (isSaving) {
        Toast.makeText(this, "Đang lưu dữ liệu, vui lòng đợi...", Toast.LENGTH_SHORT).show();
        return;
    }
    
    // Check if there's any unsaved data
    boolean hasUnsavedData = false;
    
    // Check if current step has data
    Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainerForm);
    if (currentFragment != null && currentStep != null && "IN_PROGRESS".equals(currentStep.getStatus())) {
        hasUnsavedData = true;
    }
    
    // Check if there are any steps
    if (!treatmentSteps.isEmpty()) {
        hasUnsavedData = true;
    }
    
    if (hasUnsavedData) {
        // Auto-save silently before exiting
        saveTreatmentPlanInternal(true, () -> {
            runOnUiThread(() -> {
                Toast.makeText(this, "Đã tự động lưu", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    } else {
        finish();
    }
}
```

### 3. Override onBackPressed()

```java
@Override
public void onBackPressed() {
    handleBackPress();
}
```

## Logic Hoạt Động

### Khi Nhấn Nút Back:

1. **Kiểm tra điều kiện cơ bản:**
   - Nếu chưa có bệnh nhân hoặc chưa có treatment plan → Thoát ngay
   - Nếu đang lưu → Hiển thị thông báo "Đang lưu, vui lòng đợi"

2. **Kiểm tra dữ liệu chưa lưu:**
   - Có bước đang IN_PROGRESS với fragment đang mở → Có dữ liệu chưa lưu
   - Có bất kỳ bước nào trong treatmentSteps → Có dữ liệu chưa lưu

3. **Tự động lưu:**
   - Gọi `saveTreatmentPlanInternal(true, callback)`
   - `true` = silent mode (không hiển thị loading dialog)
   - `callback` = Sau khi lưu xong, hiển thị toast "Đã tự động lưu" và thoát

4. **Thoát:**
   - Gọi `finish()` để đóng activity

## Các Trường Hợp Xử Lý

### Trường Hợp 1: Chưa Có Bệnh Nhân
```
User: Nhấn back
→ Không có dữ liệu để lưu
→ Thoát ngay
```

### Trường Hợp 2: Đang Nhập Dữ liệu Bước IN_PROGRESS
```
User: Đang nhập dữ liệu X-quang
User: Nhấn back
→ Phát hiện bước IN_PROGRESS
→ Tự động lưu
→ Hiển thị "Đã tự động lưu"
→ Thoát
```

### Trường Hợp 3: Đã Hoàn Thành Tất Cả Bước
```
User: Tất cả bước đã COMPLETED
User: Nhấn back
→ Có treatmentSteps
→ Tự động lưu (đảm bảo sync với server)
→ Hiển thị "Đã tự động lưu"
→ Thoát
```

### Trường Hợp 4: Đang Lưu
```
User: Nhấn "Lưu"
User: Nhấn back ngay lập tức
→ Phát hiện isSaving = true
→ Hiển thị "Đang lưu dữ liệu, vui lòng đợi..."
→ Không thoát
```

## Lợi Ích

1. ✅ **Không mất dữ liệu**: Tự động lưu trước khi thoát
2. ✅ **Trải nghiệm tốt hơn**: Bác sĩ không cần nhớ nhấn "Lưu"
3. ✅ **An toàn**: Kiểm tra trạng thái trước khi lưu
4. ✅ **Feedback rõ ràng**: Hiển thị toast "Đã tự động lưu"
5. ✅ **Xử lý edge cases**: Không lưu khi đang lưu, không lưu khi không có dữ liệu

## Cách Test

### Test 1: Tự Động Lưu Bước IN_PROGRESS
1. Vào DoctorWorkflowActivity
2. Chọn bệnh nhân
3. Bắt đầu bước X-quang (IN_PROGRESS)
4. Nhập một số dữ liệu
5. Nhấn nút back (mũi tên)
6. **Kỳ vọng**: 
   - Hiển thị toast "Đã tự động lưu"
   - Thoát activity
   - Vào lại → Dữ liệu vẫn còn

### Test 2: Không Lưu Khi Chưa Có Dữ Liệu
1. Vào DoctorWorkflowActivity
2. Chưa chọn bệnh nhân
3. Nhấn nút back
4. **Kỳ vọng**: 
   - Thoát ngay
   - Không hiển thị toast

### Test 3: Chặn Khi Đang Lưu
1. Vào DoctorWorkflowActivity
2. Chọn bệnh nhân và nhập dữ liệu
3. Nhấn "Lưu"
4. Ngay lập tức nhấn back
5. **Kỳ vọng**: 
   - Hiển thị "Đang lưu dữ liệu, vui lòng đợi..."
   - Không thoát
   - Sau khi lưu xong, có thể nhấn back lại

### Test 4: Tự Động Lưu Nhiều Bước
1. Vào DoctorWorkflowActivity
2. Chọn bệnh nhân
3. Hoàn thành 2-3 bước
4. Nhấn nút back
5. **Kỳ vọng**: 
   - Tự động lưu tất cả bước
   - Hiển thị "Đã tự động lưu"
   - Thoát

### Test 5: Hardware Back Button
1. Vào DoctorWorkflowActivity
2. Chọn bệnh nhân và nhập dữ liệu
3. Nhấn nút back vật lý của điện thoại
4. **Kỳ vọng**: 
   - Hoạt động giống như nhấn nút back trên UI
   - Tự động lưu và thoát

## Xem Log

```bash
adb logcat | grep DoctorWorkflow
```

**Log mong đợi:**
```
D/DoctorWorkflow: handleBackPress called
D/DoctorWorkflow: Has IN_PROGRESS step with potential unsaved data
D/DoctorWorkflow: Auto-saving before exit
D/DoctorWorkflow: Auto-save completed, exiting
```

## Files Đã Sửa

- `mobile_android/app/src/main/java/com/hcmute/mobile_android/ui/activities/staff/DoctorWorkflowActivity.java`
  - Sửa listener của btnBack
  - Thêm method `handleBackPress()`
  - Override `onBackPressed()`

## So Sánh Với Trước

| Tính Năng | Trước | Sau |
|-----------|-------|-----|
| Nhấn back | Thoát ngay | Tự động lưu rồi thoát |
| Mất dữ liệu | ✅ Có thể mất | ❌ Không mất |
| Feedback | ❌ Không có | ✅ Toast "Đã tự động lưu" |
| Xử lý đang lưu | ❌ Không | ✅ Chặn và thông báo |
| Hardware back | ❌ Không xử lý | ✅ Xử lý giống UI back |

## Lưu Ý

- Tự động lưu chỉ xảy ra khi có dữ liệu
- Không lưu khi đang trong quá trình lưu
- Sử dụng silent mode để không hiển thị loading dialog
- Callback đảm bảo chỉ thoát sau khi lưu xong
- Toast "Đã tự động lưu" giúp user biết dữ liệu đã được lưu

## Kết Luận

Tính năng tự động lưu khi nhấn back giúp:
- Bảo vệ dữ liệu của bác sĩ
- Cải thiện trải nghiệm người dùng
- Giảm thiểu lỗi do quên lưu
- Xử lý tốt các edge cases
