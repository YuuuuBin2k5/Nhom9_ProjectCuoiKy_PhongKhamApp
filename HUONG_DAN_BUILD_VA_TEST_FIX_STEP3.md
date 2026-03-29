# 🚀 HƯỚNG DẪN BUILD VÀ TEST FIX STEP 3

## ✅ Đã Fix Xong

Đã implement 2 lớp bảo vệ chuyên nghiệp:

### Layer 1: Backend
- File: `clinic_backend/src/main/java/com/hcmute/clinic/service/TreatmentPlanService.java`
- Logic: Detect re-completing và skip auto-advance

### Layer 2: Mobile  
- File: `mobile_android/app/src/main/java/com/hcmute/mobile_android/ui/activities/staff/DoctorWorkflowActivity.java`
- Logic: Reset IN_PROGRESS steps không hợp lệ khi load từ backend

## 📦 Bước 1: Build Backend

### Option A: Sử dụng IntelliJ IDEA (Khuyến nghị)
1. Mở IntelliJ IDEA
2. Mở project `clinic_backend`
3. Click menu: **Build → Build Project** (hoặc Ctrl+F9)
4. Đợi build xong (xem progress ở thanh dưới)
5. Restart backend server

### Option B: Sử dụng Command Line
```bash
cd clinic_backend
gradlew.bat build -x test
```

### Option C: Restart Backend Nhanh (Nếu đang chạy)
1. Trong IntelliJ, tìm tab **Run** ở dưới
2. Click nút **Stop** (hình vuông đỏ)
3. Click nút **Run** (hình tam giác xanh) để restart

## 📱 Bước 2: Build Mobile APK

### Option A: Sử dụng Android Studio (Khuyến nghị)
1. Mở Android Studio
2. Mở project `mobile_android`
3. Click menu: **Build → Build Bundle(s) / APK(s) → Build APK(s)**
4. Đợi build xong (xem progress ở thanh dưới)
5. Khi xong, click **locate** để mở thư mục chứa APK
6. File APK: `mobile_android/app/build/outputs/apk/debug/app-debug.apk`

### Option B: Sử dụng Command Line
```bash
cd mobile_android
gradlew.bat assembleDebug
```

APK sẽ được tạo tại: `mobile_android/app/build/outputs/apk/debug/app-debug.apk`

## 📲 Bước 3: Cài Đặt APK Mới

### Option A: Sử dụng ADB
```bash
adb install -r mobile_android/app/build/outputs/apk/debug/app-debug.apk
```

### Option B: Copy APK vào điện thoại
1. Copy file `app-debug.apk` vào điện thoại (qua USB hoặc email)
2. Mở file manager trên điện thoại
3. Tìm và click vào file `app-debug.apk`
4. Cho phép cài đặt từ nguồn không xác định (nếu được hỏi)
5. Click **Cài đặt**

### Option C: Sử dụng Android Studio
1. Kết nối điện thoại qua USB
2. Bật USB Debugging trên điện thoại
3. Trong Android Studio, click **Run → Run 'app'** (hoặc Shift+F10)
4. Chọn điện thoại của bạn trong danh sách
5. App sẽ tự động cài đặt và chạy

## 🧪 Bước 4: Test Fix

### Chuẩn Bị
1. Đảm bảo backend đã restart với code mới
2. Đảm bảo mobile app đã cài đặt version mới
3. Login vào app với tài khoản bác sĩ

### Test Case: Edit Step 1 (đã COMPLETED)

**Setup:**
- Cần có treatment plan với ít nhất 3 steps
- Tất cả 3 steps đều đã COMPLETED

**Các bước test:**

1. **Mở treatment plan**
   - Vào màn hình Doctor Workflow
   - Load treatment plan có 3 steps đã COMPLETED

2. **Edit step 1**
   - Click nút "Sửa" (icon bút chì) trên step 1
   - Thay đổi dữ liệu (ví dụ: thêm ảnh, sửa kết luận)
   - Click "Hoàn thành bước"

3. **Kiểm tra kết quả**
   - Xem status của step 3 trong danh sách
   - Step 3 phải vẫn là **COMPLETED** (KHÔNG phải IN_PROGRESS)

4. **Kiểm tra log (Optional)**
   - Kết nối ADB: `adb logcat | grep DoctorWorkflow`
   - Tìm dòng: `✓ Restored currentStep by ID`
   - Không có dòng: `⚠️ RESET: Step ... có IN_PROGRESS`

### Kết Quả Mong Đợi

✅ **ĐÚNG:**
```
Step 1: COMPLETED (đã update)
Step 2: COMPLETED (không đổi)
Step 3: COMPLETED (không đổi) ← QUAN TRỌNG
```

❌ **SAI (bug cũ):**
```
Step 1: COMPLETED (đã update)
Step 2: COMPLETED (không đổi)
Step 3: IN_PROGRESS ← BUG
```

## 📊 Kiểm Tra Log Backend

Nếu muốn xem log backend:

```bash
# Trong IntelliJ IDEA, xem tab Run/Console
# Tìm dòng:
Re-completing step 123 - không auto-advance
```

## 🐛 Nếu Vẫn Có Bug

### Checklist:
- [ ] Backend đã restart với code mới?
- [ ] Mobile app đã cài đặt version mới?
- [ ] Đã clear app data? (Settings → Apps → PhongKham → Clear Data)
- [ ] Test case đúng? (Edit step đã COMPLETED, không phải step PENDING)

### Debug:
1. Xem log mobile: `adb logcat | grep DoctorWorkflow`
2. Xem log backend trong IntelliJ Console
3. Kiểm tra database: Xem status của steps trong bảng `treatment_plan_step`

## 📝 Tóm Tắt

**Files đã sửa:**
1. `clinic_backend/src/main/java/com/hcmute/clinic/service/TreatmentPlanService.java`
2. `mobile_android/app/src/main/java/com/hcmute/mobile_android/ui/activities/staff/DoctorWorkflowActivity.java`

**Cách build nhanh nhất:**
1. Backend: Restart trong IntelliJ (Stop → Run)
2. Mobile: Build APK trong Android Studio → Install qua ADB

**Test nhanh nhất:**
1. Edit step 1 đã COMPLETED
2. Click "Hoàn thành bước"
3. Kiểm tra step 3 vẫn COMPLETED

## ✅ Hoàn Thành

Fix đã sẵn sàng để test. Nếu có vấn đề gì, hãy gửi log để tôi hỗ trợ thêm!
