# Hướng Dẫn Test Fix Lỗi Step 3 Tự Động Chuyển IN_PROGRESS

## Vấn Đề Cần Test
Khi chỉnh sửa và lưu bước 1 (đã HOÀN THÀNH) trong khi bước 2 đang ĐANG THỰC HIỆN và bước 3 đang CHỜ XỬ LÝ:
- Sau khi lưu, bước 3 không nên tự động chuyển sang ĐANG THỰC HIỆN
- Bước 3 phải giữ nguyên trạng thái CHỒ XỬ LÝ

## Cài Đặt APK Mới

### Bước 1: Gỡ APK Cũ
```bash
adb uninstall com.hcmute.mobile_android
```

### Bước 2: Cài APK Mới
```bash
adb install app-debug-fix-step3-status.apk
```

### Bước 3: Xác Nhận Cài Đặt
- Mở app trên điện thoại
- Đăng nhập với tài khoản bác sĩ

## Kịch Bản Test

### Chuẩn Bị
1. Đăng nhập với tài khoản bác sĩ
2. Chọn bệnh nhân từ hàng đợi
3. Tạo phác đồ điều trị với 3 bước:
   - Bước 1: Khám và tư vấn (General Dental)
   - Bước 2: Chụp X-quang (X-ray)
   - Bước 3: Nhổ răng khôn (Surgery)

### Các Bước Test

#### Bước 1: Hoàn Thành Bước 1
1. Nhấp "Bắt đầu" trên bước 1 (Khám và tư vấn)
2. Nhập thông tin khám
3. Nhấp "Hoàn thành bước"
4. Xác nhận bước 1 có trạng thái: ✅ HOÀN THÀNH

#### Bước 2: Bắt Đầu Bước 2
1. Nhấp "Bắt đầu" trên bước 2 (Chụp X-quang)
2. Xác nhận bước 2 có trạng thái: 🔄 ĐANG THỰC HIỆN
3. **QUAN TRỌNG**: Xác nhận bước 3 vẫn là: ⏳ CHỜ XỬ LÝ

#### Bước 3: Chỉnh Sửa Bước 1 (Test Chính)
1. Nhấp nút "Chỉnh sửa" trên bước 1
2. Thay đổi một số thông tin (ví dụ: thêm ghi chú)
3. Nhấp "Lưu"
4. **KIỂM TRA QUAN TRỌNG**:
   - Bước 1: Nên quay về ✅ HOÀN THÀNH (sau khi hoàn thành lại)
   - Bước 2: Vẫn là 🔄 ĐANG THỰC HIỆN (không thay đổi)
   - Bước 3: Vẫn là ⏳ CHỜ XỬ LÝ (KHÔNG được chuyển sang ĐANG THỰC HIỆN) ✓

## Thu Thập Log

### Bước 1: Kết Nối ADB
```bash
adb devices
```

### Bước 2: Xóa Log Cũ
```bash
adb logcat -c
```

### Bước 3: Bắt Đầu Thu Thập Log
```bash
adb logcat -s DoctorWorkflow:D > test_step3_fix.log
```

### Bước 4: Thực Hiện Test
- Làm theo kịch bản test ở trên
- Đặc biệt chú ý bước 3 (chỉnh sửa bước 1)

### Bước 5: Dừng Thu Thập Log
- Nhấn Ctrl+C để dừng
- File log sẽ được lưu tại `test_step3_fix.log`

## Phân Tích Log

### Log Cần Tìm

#### 1. Trước Khi Lưu
```
=== saveTreatmentPlanInternal: Preparing request ===
editingStep: Khám và tư vấn (ID: 1)
currentStep: Chụp X-quang (ID: 2)
→ Step 1 (Khám và tư vấn): EDITING - status=IN_PROGRESS
→ Step 2 (Chụp X-quang): CURRENT - status=IN_PROGRESS
→ Step 3 (Nhổ răng khôn): OTHER - status=PENDING (preserving)
=== Sending 3 steps to backend ===
```

**Kiểm tra**: Step 3 phải có status=PENDING với ghi chú "(preserving)"

#### 2. Sau Khi Lưu
```
=== Save response received ===
Response code: 200
Success: true
```

**Kiểm tra**: Response code phải là 200

#### 3. Sau Khi Reload
```
=== loadTreatmentPlanForRoom ===
Received 3 steps from backend:
  - Step ID=1, Service=Khám và tư vấn, Status=IN_PROGRESS
  - Step ID=2, Service=Chụp X-quang, Status=IN_PROGRESS
  - Step ID=3, Service=Nhổ răng khôn, Status=PENDING
```

**Kiểm tra**: Step 3 phải có Status=PENDING (KHÔNG phải IN_PROGRESS)

## Kết Quả Mong Đợi

### ✅ Test Thành Công Nếu:
1. Mobile gửi step 3 với status=PENDING (có ghi chú "preserving")
2. Backend trả về step 3 với Status=PENDING
3. UI hiển thị step 3 với trạng thái ⏳ CHỜ XỬ LÝ
4. Workflow vẫn ở bước 2 (Chụp X-quang)

### ❌ Test Thất Bại Nếu:
1. Mobile gửi step 3 với status=IN_PROGRESS
2. Backend trả về step 3 với Status=IN_PROGRESS
3. UI hiển thị step 3 với trạng thái 🔄 ĐANG THỰC HIỆN
4. Workflow nhảy sang bước 3

## Xử Lý Nếu Test Thất Bại

### Trường Hợp 1: Mobile Gửi Sai Status
Nếu log cho thấy:
```
→ Step 3 (Nhổ răng khôn): OTHER - status=IN_PROGRESS (preserving)
```

**Nguyên nhân**: Status trong bộ nhớ đã bị thay đổi trước khi lưu
**Giải pháp**: Cần tìm và fix nơi thay đổi status của step 3

### Trường Hợp 2: Backend Trả Về Sai Status
Nếu log cho thấy:
```
→ Step 3 (Nhổ răng khôn): OTHER - status=PENDING (preserving)
```
Nhưng sau reload:
```
- Step ID=3, Service=Nhổ răng khôn, Status=IN_PROGRESS
```

**Nguyên nhân**: Backend đang thay đổi status
**Giải pháp**: Cần kiểm tra và fix logic backend

## Gửi Kết Quả

Vui lòng gửi:
1. File log: `test_step3_fix.log`
2. Screenshot UI sau khi lưu (hiển thị trạng thái 3 bước)
3. Mô tả kết quả: Thành công hay thất bại?

## Thông Tin Kỹ Thuật

### File Đã Sửa
- `DoctorWorkflowActivity.java` - Thêm logging chi tiết

### Thay Đổi Chính
- Logging rõ ràng hơn cho từng bước
- Ghi chú "(preserving)" cho các bước không được chỉnh sửa
- Logging response từ backend

### APK Version
- File: `app-debug-fix-step3-status.apk`
- Build date: 2026-03-29
- Purpose: Diagnose step 3 status bug
