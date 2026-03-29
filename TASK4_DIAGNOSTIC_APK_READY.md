# Task 4: Diagnostic APK Ready - Step 3 Status Bug

## Vấn Đề (Problem)
Khi chỉnh sửa và lưu bước 1 (COMPLETED) trong khi bước 2 đang IN_PROGRESS và bước 3 đang PENDING:
- Sau khi lưu, bước 3 tự động chuyển sang IN_PROGRESS (SAI)
- Bước 3 nên giữ nguyên PENDING (ĐÚNG)

When editing and saving step 1 (COMPLETED) while step 2 is IN_PROGRESS and step 3 is PENDING:
- After save, step 3 automatically becomes IN_PROGRESS (WRONG)
- Step 3 should remain PENDING (CORRECT)

## Giải Pháp (Solution)

### Bước 1: Thêm Logging Chi Tiết (Step 1: Add Detailed Logging)
Đã thêm logging để theo dõi:
- Status của mỗi bước TRƯỚC KHI gửi lên backend
- Response từ backend
- Status của mỗi bước SAU KHI reload từ backend

Added logging to track:
- Status of each step BEFORE sending to backend
- Backend response
- Status of each step AFTER reloading from backend

### Bước 2: Build APK Mới (Step 2: Build New APK)
✅ APK đã build xong: `app-debug-fix-step3-status.apk`

### Bước 3: Hướng Dẫn Test (Step 3: Testing Guide)
✅ Đã tạo hướng dẫn chi tiết: `HUONG_DAN_TEST_FIX_STEP3.md`

## Cài Đặt và Test (Installation and Testing)

### Cài Đặt APK (Install APK)
```bash
# Gỡ APK cũ (Uninstall old APK)
adb uninstall com.hcmute.mobile_android

# Cài APK mới (Install new APK)
adb install app-debug-fix-step3-status.apk
```

### Thu Thập Log (Collect Logs)
```bash
# Xóa log cũ (Clear old logs)
adb logcat -c

# Bắt đầu thu thập log (Start collecting logs)
adb logcat -s DoctorWorkflow:D > test_step3_fix.log
```

### Kịch Bản Test (Test Scenario)
1. Tạo phác đồ với 3 bước (Create plan with 3 steps):
   - Bước 1: Khám và tư vấn
   - Bước 2: Chụp X-quang
   - Bước 3: Nhổ răng khôn

2. Hoàn thành bước 1 (Complete step 1)

3. Bắt đầu bước 2 (Start step 2)

4. Chỉnh sửa bước 1 (Edit step 1)

5. Lưu (Save)

6. **KIỂM TRA (CHECK)**: Bước 3 vẫn là PENDING? (Step 3 still PENDING?)

## Log Cần Tìm (Expected Logs)

### Trước Khi Lưu (Before Save)
```
=== saveTreatmentPlanInternal: Preparing request ===
editingStep: Khám và tư vấn (ID: 1)
currentStep: Chụp X-quang (ID: 2)
→ Step 1 (Khám và tư vấn): EDITING - status=IN_PROGRESS
→ Step 2 (Chụp X-quang): CURRENT - status=IN_PROGRESS
→ Step 3 (Nhổ răng khôn): OTHER - status=PENDING (preserving)
```

**QUAN TRỌNG (IMPORTANT)**: Step 3 phải có `status=PENDING` với ghi chú `(preserving)`

### Sau Khi Reload (After Reload)
```
=== loadTreatmentPlanForRoom ===
Received 3 steps from backend:
  - Step ID=1, Service=Khám và tư vấn, Status=IN_PROGRESS
  - Step ID=2, Service=Chụp X-quang, Status=IN_PROGRESS
  - Step ID=3, Service=Nhổ răng khôn, Status=PENDING
```

**QUAN TRỌNG (IMPORTANT)**: Step 3 phải có `Status=PENDING` (KHÔNG phải IN_PROGRESS)

## Phân Tích (Analysis)

### Nếu Mobile Gửi Sai (If Mobile Sends Wrong Status)
Log sẽ hiển thị (Log will show):
```
→ Step 3 (Nhổ răng khôn): OTHER - status=IN_PROGRESS (preserving)
```
→ Cần fix mobile code

### Nếu Backend Trả Về Sai (If Backend Returns Wrong Status)
Log sẽ hiển thị (Log will show):
```
→ Step 3: status=PENDING (preserving)  ← Mobile gửi đúng
...
- Step ID=3, Status=IN_PROGRESS  ← Backend trả về sai
```
→ Cần fix backend code

## Files

### APK
- `app-debug-fix-step3-status.apk` - APK mới với logging chi tiết

### Documentation
- `FIX_STEP3_STATUS_BUG.md` - Phân tích kỹ thuật chi tiết
- `HUONG_DAN_TEST_FIX_STEP3.md` - Hướng dẫn test bằng tiếng Việt
- `TASK4_DIAGNOSTIC_APK_READY.md` - Tài liệu này

### Code Changes
- `mobile_android/app/src/main/java/com/hcmute/mobile_android/ui/activities/staff/DoctorWorkflowActivity.java`
  - Enhanced logging in `saveTreatmentPlanInternal` method (lines ~145-185)
  - Added response logging

## Next Steps

1. ✅ Cài APK mới (Install new APK)
2. ✅ Thu thập log (Collect logs)
3. ✅ Chạy test scenario (Run test scenario)
4. ⏳ Gửi log file và screenshot (Send log file and screenshots)
5. ⏳ Phân tích log để xác định root cause (Analyze logs to determine root cause)
6. ⏳ Implement fix dựa trên kết quả (Implement fix based on results)

## Status
✅ Diagnostic APK ready
✅ Testing guide ready
⏳ Waiting for user testing and log collection
