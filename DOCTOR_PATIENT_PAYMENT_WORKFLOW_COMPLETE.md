# Hoàn thiện Workflow Thanh Toán - Tách biệt vai trò Bác sĩ & Bệnh nhân

## 📋 Tổng quan

Đã sửa lại logic thanh toán để phân tách rõ ràng vai trò:
- **Bác sĩ**: Chỉ hoàn thành khám bệnh
- **Bệnh nhân**: Xem hóa đơn và thanh toán

## ✅ Thay đổi đã thực hiện

### 1. Doctor Workflow - Sửa nút "Thanh toán" → "Hoàn thành khám"

#### File: `activity_doctor_workflow.xml`
```xml
<!-- Đã đổi text và icon -->
<com.google.android.material.button.MaterialButton
    android:id="@+id/btnPayment"
    android:text="Hoàn thành\nkhám"
    app:icon="@drawable/ic_check_circle"
    app:backgroundTint="#2E7D32" />
```

#### File: `DoctorWorkflowActivity.java`

**Logic mới:**
```java
// Khi nhấn "Hoàn thành khám"
btnPayment.setOnClickListener(v -> {
    // Validate tất cả bước đã hoàn thành
    // Hiển thị dialog xác nhận
    // Gọi completeExaminationAndReturn()
});

private void completeExaminationAndReturn() {
    // Tạo hóa đơn qua API
    apiService.completeAndGenerateInvoice(currentTreatmentPlanId)
    
    // Thành công → finish() (quay về trang chủ)
    // KHÔNG navigate đến PaymentActivity
}
```

**Điểm khác biệt:**
- ❌ Không mở form thanh toán
- ❌ Không yêu cầu bác sĩ chọn phương thức thanh toán
- ✅ Chỉ hoàn tất khám và tạo hóa đơn
- ✅ Quay về trang chủ ngay lập tức

### 2. Patient Dashboard - Thêm nút "Hóa đơn"

#### File: `fragment_patient_dashboard.xml`

Thêm 2 card Quick Actions:

```xml
<LinearLayout orientation="horizontal">
    <!-- Card 1: Hóa đơn -->
    <MaterialCardView android:id="@+id/cardMyInvoices">
        <ImageView src="@drawable/ic_payment" />
        <TextView text="Hóa đơn" />
        <TextView text="Xem & thanh toán" />
    </MaterialCardView>
    
    <!-- Card 2: Lịch hẹn -->
    <MaterialCardView android:id="@+id/cardMyAppointments">
        <ImageView src="@drawable/ic_calendar" />
        <TextView text="Lịch hẹn" />
        <TextView text="Quản lý lịch" />
    </MaterialCardView>
</LinearLayout>
```

#### File: `PatientDashboardFragment.java`

```java
private void initViews(View view) {
    // ... existing code ...
    
    // Setup Quick Actions
    View cardMyInvoices = view.findViewById(R.id.cardMyInvoices);
    if (cardMyInvoices != null) {
        cardMyInvoices.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), InvoiceListActivity.class);
            startActivity(intent);
        });
    }
}
```

## 🔄 Workflow hoàn chỉnh

### Quy trình khám bệnh & thanh toán:

```
1. Bệnh nhân check-in
   ↓
2. Bác sĩ khám bệnh
   ↓
3. Bác sĩ tạo phác đồ điều trị
   ↓
4. Bác sĩ hoàn thành các bước điều trị
   ↓
5. Bác sĩ nhấn "Hoàn thành khám"
   ├─ Hệ thống tạo hóa đơn tự động
   ├─ Bác sĩ quay về trang chủ
   └─ Hóa đơn ở trạng thái UNPAID
   ↓
6. Bệnh nhân vào app
   ↓
7. Bệnh nhân nhấn "Hóa đơn" trên dashboard
   ↓
8. Xem danh sách hóa đơn (InvoiceListActivity)
   ├─ Filter: Tất cả / Chưa thanh toán / Đã thanh toán
   └─ Nhấn vào hóa đơn chưa thanh toán
   ↓
9. Xem chi tiết hóa đơn (InvoiceDetailActivity)
   ├─ Hiển thị các dịch vụ
   ├─ Tổng tiền
   └─ Nút "Thanh toán" (chỉ hiện nếu UNPAID)
   ↓
10. Nhấn "Thanh toán" → PaymentActivity
    ├─ Chọn phương thức: Tiền mặt / Chuyển khoản / Thẻ / MoMo / ZaloPay
    ├─ Nhập ghi chú (optional)
    └─ Xác nhận thanh toán
    ↓
11. Hệ thống xử lý thanh toán
    ├─ Cập nhật trạng thái hóa đơn → PAID
    └─ Hiển thị "Thanh toán thành công"
```

## 📱 UI Components đã có sẵn

### Cho Patient:
1. ✅ `InvoiceListActivity` - Danh sách hóa đơn
2. ✅ `InvoiceDetailActivity` - Chi tiết hóa đơn + nút thanh toán
3. ✅ `PaymentActivity` - Form chọn phương thức thanh toán
4. ✅ `PatientDashboardFragment` - Đã thêm nút "Hóa đơn"

### Cho Doctor:
1. ✅ `DoctorWorkflowActivity` - Đã sửa nút "Hoàn thành khám"

## 🔌 API Endpoints

```java
// Bác sĩ hoàn thành khám
POST /api/treatment-plans/{planId}/complete-and-generate-invoice
→ Tạo hóa đơn với status UNPAID

// Bệnh nhân xem danh sách hóa đơn
GET /api/invoices/my
→ Trả về List<Invoice>

// Bệnh nhân xem chi tiết hóa đơn
GET /api/invoices/{id}
→ Trả về Invoice với items

// Bệnh nhân thanh toán
POST /api/invoices/{id}/pay
Body: { paymentMethod, amount, note }
→ Cập nhật status → PAID
```

## 🎯 Separation of Concerns

### Bác sĩ (Doctor):
- ✅ Khám bệnh
- ✅ Tạo phác đồ điều trị
- ✅ Hoàn thành các bước điều trị
- ✅ Kết thúc khám (tạo hóa đơn)
- ❌ KHÔNG xử lý thanh toán

### Bệnh nhân (Patient):
- ✅ Xem danh sách hóa đơn
- ✅ Xem chi tiết hóa đơn
- ✅ Chọn phương thức thanh toán
- ✅ Thanh toán hóa đơn

### Lễ tân (Receptionist) - Tương lai:
- 🔄 Có thể hỗ trợ bệnh nhân thanh toán tại quầy
- 🔄 Xem danh sách hóa đơn chưa thanh toán
- 🔄 In hóa đơn

## 📝 Testing Checklist

### Test Doctor Workflow:
- [ ] Bác sĩ hoàn thành tất cả bước điều trị
- [ ] Nhấn "Hoàn thành khám"
- [ ] Xác nhận dialog
- [ ] Kiểm tra hóa đơn được tạo (status = UNPAID)
- [ ] Kiểm tra bác sĩ quay về trang chủ (không mở form thanh toán)

### Test Patient Workflow:
- [ ] Login bằng tài khoản patient
- [ ] Nhấn card "Hóa đơn" trên dashboard
- [ ] Xem danh sách hóa đơn
- [ ] Filter: Chưa thanh toán
- [ ] Nhấn vào hóa đơn UNPAID
- [ ] Xem chi tiết hóa đơn
- [ ] Nhấn "Thanh toán"
- [ ] Chọn phương thức thanh toán
- [ ] Xác nhận thanh toán
- [ ] Kiểm tra status → PAID
- [ ] Kiểm tra nút "Thanh toán" biến mất

## 🎨 UI/UX Improvements

### Doctor:
- Nút màu xanh lá (#2E7D32) - màu "hoàn thành"
- Icon check circle - biểu tượng hoàn tất
- Message rõ ràng: "Hoàn thành khám bệnh thành công!"

### Patient:
- Card "Hóa đơn" nổi bật trên dashboard
- Icon payment màu xanh dương
- Text phụ: "Xem & thanh toán"
- Dễ dàng truy cập từ trang chủ

## 🚀 Deployment Notes

1. Build APK mới với các thay đổi
2. Test workflow đầy đủ trước khi deploy
3. Hướng dẫn bác sĩ về thay đổi UI (nút mới)
4. Hướng dẫn bệnh nhân cách thanh toán qua app

## 📚 Related Files

### Modified:
- `mobile_android/app/src/main/res/layout/activity_doctor_workflow.xml`
- `mobile_android/app/src/main/java/com/hcmute/mobile_android/ui/activities/staff/DoctorWorkflowActivity.java`
- `mobile_android/app/src/main/res/layout/fragment_patient_dashboard.xml`
- `mobile_android/app/src/main/java/com/hcmute/mobile_android/ui/fragments/PatientDashboardFragment.java`

### Existing (No changes needed):
- `mobile_android/app/src/main/java/com/hcmute/mobile_android/ui/activities/InvoiceListActivity.java`
- `mobile_android/app/src/main/java/com/hcmute/mobile_android/ui/activities/InvoiceDetailActivity.java`
- `mobile_android/app/src/main/java/com/hcmute/mobile_android/ui/activities/PaymentActivity.java`

---

**Status**: ✅ COMPLETE
**Date**: 2026-03-29
**Impact**: High - Cải thiện UX và phân tách rõ ràng vai trò
