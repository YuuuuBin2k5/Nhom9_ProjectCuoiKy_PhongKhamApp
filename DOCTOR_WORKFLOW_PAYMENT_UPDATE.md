# Cập nhật Doctor Workflow - Thanh toán và Chi tiết giá

## Tóm tắt thay đổi

Đã thực hiện các cập nhật sau cho màn hình Doctor Workflow:

### 1. Bỏ nút "Lưu hồ sơ" ✅
- **Lý do**: Hệ thống đã tự động lưu hồ sơ (auto-save)
- **Thay đổi**:
  - Xóa nút `btnSavePlan` khỏi layout
  - Xóa tất cả logic liên quan đến nút Lưu hồ sơ
  - Giữ lại logic auto-save trong `onPause()` và các điểm cần thiết
  - Cập nhật thông báo từ "Đã lưu hồ sơ bệnh án thành công!" thành "Đã tự động lưu hồ sơ bệnh án!"

### 2. Thêm nút "Thanh toán" ✅
- **Vị trí**: Thay thế nút "Lưu hồ sơ" trong thanh action buttons
- **Thiết kế**:
  - Màu xanh lá (#2E7D32)
  - Icon thanh toán (ic_payment.xml)
  - Text: "Thanh toán"
- **Chức năng**:
  - Chuyển đến `PaymentActivity` với thông tin:
    - `PATIENT_ID`: ID bệnh nhân
    - `TREATMENT_PLAN_ID`: ID phác đồ điều trị
    - `PATIENT_NAME`: Tên bệnh nhân
  - Kiểm tra điều kiện: Phải có bệnh nhân và phác đồ điều trị

### 3. Hiển thị chi tiết giá ✅
- **Vị trí**: Phần "Tổng tạm tính" được mở rộng
- **Thiết kế**:
  - Card với background trắng bo góc
  - Tiêu đề: "Chi tiết tạm tính:"
  - RecyclerView hiển thị danh sách dịch vụ và giá
  - Đường phân cách
  - Tổng cộng (in đậm, màu xanh)
- **Nội dung hiển thị**:
  - Tên dịch vụ (kèm số răng nếu có)
  - Giá từng dịch vụ
  - Chỉ hiển thị các bước không bị hủy hoặc bỏ qua

## Files đã thay đổi

### 1. Layout Files
- `mobile_android/app/src/main/res/layout/activity_doctor_workflow.xml`
  - Xóa nút `btnSavePlan`
  - Thêm nút `btnPayment`
  - Thêm RecyclerView `rvPriceBreakdown` cho chi tiết giá
  - Cập nhật cấu trúc hiển thị tổng tiền

### 2. New Files Created
- `mobile_android/app/src/main/res/drawable/ic_payment.xml`
  - Icon thanh toán (thẻ tín dụng)
  
- `mobile_android/app/src/main/res/layout/item_price_breakdown.xml`
  - Layout cho từng item trong danh sách chi tiết giá
  
- `mobile_android/app/src/main/java/com/hcmute/mobile_android/adapters/PriceBreakdownAdapter.java`
  - Adapter cho RecyclerView hiển thị chi tiết giá

### 3. Activity Files
- `mobile_android/app/src/main/java/com/hcmute/mobile_android/ui/activities/staff/DoctorWorkflowActivity.java`
  - Xóa biến `btnSavePlan`
  - Thêm biến `btnPayment` và `rvPriceBreakdown`
  - Thêm `priceBreakdownAdapter`
  - Xóa listener cho `btnSavePlan`
  - Thêm listener cho `btnPayment` (chuyển đến PaymentActivity)
  - Cập nhật `setupAdapters()` để khởi tạo `priceBreakdownAdapter`
  - Cập nhật `updateTotalEstimate()` để cập nhật cả chi tiết giá
  - Cập nhật `updateUIMode()` để bỏ logic liên quan đến btnSavePlan
  - Cập nhật `saveTreatmentPlanInternal()` để bỏ enable/disable btnSavePlan
  - Cập nhật `createBlankPlanAndSave()` để bỏ enable/disable btnSavePlan
  - Thêm import cho `PaymentActivity` và `PrescriptionActivity`

## Cách hoạt động

### Auto-save
- Hệ thống tự động lưu khi:
  - Rời khỏi màn hình (`onPause()`)
  - Thêm dịch vụ mới
  - Hoàn thành bước khám
  - Các thao tác quan trọng khác

### Chi tiết giá
1. Khi có thay đổi trong danh sách bước điều trị, `updateTotalEstimate()` được gọi
2. Hàm này:
   - Lọc các bước không bị hủy/bỏ qua
   - Tính tổng tiền
   - Cập nhật `priceBreakdownAdapter` với danh sách bước đã lọc
3. Adapter hiển thị:
   - Tên dịch vụ + số răng (nếu có)
   - Giá tiền định dạng VNĐ

### Thanh toán
1. Người dùng nhấn nút "Thanh toán"
2. Kiểm tra điều kiện (có bệnh nhân và phác đồ)
3. Chuyển đến `PaymentActivity` với thông tin cần thiết

## Testing

### Test Cases
1. ✅ Kiểm tra nút "Lưu hồ sơ" đã bị xóa
2. ✅ Kiểm tra nút "Thanh toán" hiển thị đúng
3. ✅ Kiểm tra chi tiết giá hiển thị đầy đủ các dịch vụ
4. ✅ Kiểm tra tổng tiền tính đúng
5. ✅ Kiểm tra auto-save vẫn hoạt động
6. ✅ Kiểm tra nút thanh toán chuyển đến PaymentActivity

### Cách test
```bash
# Build và cài đặt APK
cd mobile_android
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Test workflow
1. Đăng nhập với tài khoản bác sĩ
2. Quét QR bệnh nhân
3. Thêm dịch vụ vào phác đồ
4. Kiểm tra chi tiết giá hiển thị đúng
5. Kiểm tra tổng tiền
6. Nhấn nút "Thanh toán"
7. Kiểm tra chuyển đến màn hình thanh toán
```

## Notes
- Auto-save vẫn hoạt động bình thường, không cần nút "Lưu hồ sơ" nữa
- Chi tiết giá giúp bác sĩ và bệnh nhân thấy rõ các khoản phí
- Nút thanh toán giúp chuyển nhanh đến màn hình thanh toán
- Code đã được tối ưu, xóa bỏ các phần không cần thiết

## Status
✅ Hoàn thành - Tất cả thay đổi đã được implement và test
