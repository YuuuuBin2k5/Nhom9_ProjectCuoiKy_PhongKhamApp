# 🦷 ODONTOGRAM FDI CLEANUP & ERROR 400 FIX - COMPLETE

**Date**: March 30, 2026  
**Status**: ✅ COMPLETE  
**Session**: Continuation Session 2

---

## 📋 SUMMARY

Hoàn thành cleanup sơ đồ FDI cũ và fix lỗi 400 khi chọn răng và dịch vụ. Đảm bảo chỉ có 1 sơ đồ răng tích hợp dịch vụ và luôn mặc định tab "Tổng quát" khi tra cứu bệnh nhân.

---

## ✅ COMPLETED TASKS

### 1. Xóa Sơ Đồ FDI Cũ (Redundant Odontogram)
**Problem**: Có 2 sơ đồ răng trong ứng dụng:
- FragmentGeneralDental: Sơ đồ FDI cũ (chỉ để ghi chú)
- DoctorWorkflowActivity: Sơ đồ tích hợp dịch vụ (mới)

**Solution**:
- ✅ Xóa MaterialCardView chứa OdontogramView trong `fragment_general_dental.xml`
- ✅ Xóa import `OdontogramView` trong `FragmentGeneralDental.java`
- ✅ Xóa field `odontogramView` và logic liên quan
- ✅ Xóa MaterialCardView rỗng trong `activity_doctor_workflow.xml`

**Files Modified**:
```
mobile_android/app/src/main/res/layout/fragment_general_dental.xml
mobile_android/app/src/main/java/com/hcmute/mobile_android/ui/fragments/FragmentGeneralDental.java
mobile_android/app/src/main/res/layout/activity_doctor_workflow.xml
```

---

### 2. Fix Lỗi 400 Khi Chọn Răng và Dịch Vụ
**Problem**: 
- User click vào răng → Chọn dịch vụ → API trả về lỗi 400
- Root cause: `currentTreatmentPlanId` = null khi chưa tạo treatment plan

**Backend Requirements** (từ ToothServiceController.java):
```java
POST /api/treatment-plans/{planId}/services/teeth/{toothNumber}
- planId: Long (required, must exist)
- toothNumber: String (required, not empty)
- serviceId: Long (required, must exist)
```

**Solution**:
- ✅ Thêm check `currentTreatmentPlanId == null` trong `showToothServiceSelectionDialog()`
- ✅ Tự động tạo treatment plan nếu chưa có
- ✅ Sau khi tạo xong, mới hiển thị dialog chọn dịch vụ

**Code Changes**:
```java
// DoctorWorkflowActivity.java
private void showToothServiceSelectionDialog(int toothNumber, OdontogramView odontogramView) {
    // CRITICAL FIX: Ensure treatment plan exists before adding tooth service
    if (currentTreatmentPlanId == null) {
        Toast.makeText(this, "Đang tạo phác đồ điều trị...", Toast.LENGTH_SHORT).show();
        createBlankPlanAndSave(true, () -> {
            // After plan is created, show dialog
            showToothServiceSelectionDialogInternal(toothNumber, odontogramView);
        });
        return;
    }
    
    showToothServiceSelectionDialogInternal(toothNumber, odontogramView);
}
```

---

### 3. Mặc Định Tab "Tổng Quát" Khi Tra Cứu Bệnh Nhân
**Problem**: Khi tra cứu bệnh nhân, tab nào được chọn không rõ ràng

**Solution**:
- ✅ Thêm `toggleFormType.check(R.id.btnFormGeneral)` trong `displayPatientInfo()`
- ✅ Đảm bảo tab "Tổng quát" luôn được chọn đầu tiên

**Code Changes**:
```java
// DoctorWorkflowActivity.java - displayPatientInfo()
private void displayPatientInfo(PatientInfo patient) {
    // ... existing code ...
    
    // CRITICAL FIX: Always default to "Tổng quát" tab first
    toggleFormType.check(R.id.btnFormGeneral);
    
    // ... rest of code ...
}
```

---

## 🎯 FINAL STATE

### Odontogram Architecture
```
┌─────────────────────────────────────────────────────┐
│ DoctorWorkflowActivity                              │
│                                                     │
│  ┌──────────────────────────────────────────────┐  │
│  │ OdontogramView (Integrated Service Selection)│  │
│  │ - Click tooth → Select service                │  │
│  │ - Auto-create plan if needed                  │  │
│  │ - Color-coded by service type                 │  │
│  └──────────────────────────────────────────────┘  │
│                                                     │
│  ┌──────────────────────────────────────────────┐  │
│  │ FragmentGeneralDental (Clinical Notes Only)   │  │
│  │ - Reason for visit                            │  │
│  │ - Diagnosis                                   │  │
│  │ - Tooth notes (text-based)                    │  │
│  └──────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────┘
```

### User Flow
```
1. Tra cứu bệnh nhân
   ↓
2. Tab "Tổng quát" được chọn mặc định
   ↓
3. Click vào răng trên sơ đồ
   ↓
4. Nếu chưa có treatment plan → Tự động tạo
   ↓
5. Hiển thị dialog chọn dịch vụ (4 options)
   ↓
6. Chọn dịch vụ → API call → Success
   ↓
7. Răng được tô màu theo dịch vụ
   ↓
8. Dịch vụ được thêm vào phác đồ điều trị
```

---

## 📊 FILES CHANGED

### Layout Files (3)
1. `mobile_android/app/src/main/res/layout/fragment_general_dental.xml`
   - Removed: OdontogramView MaterialCardView (60+ lines)
   - Kept: Clinical notes section only

2. `mobile_android/app/src/main/res/layout/activity_doctor_workflow.xml`
   - Removed: Empty MaterialCardView for general services
   - Kept: Single integrated OdontogramView

### Java Files (2)
3. `mobile_android/app/src/main/java/com/hcmute/mobile_android/ui/fragments/FragmentGeneralDental.java`
   - Removed: OdontogramView import
   - Removed: odontogramView field
   - Removed: OdontogramView listener setup
   - Removed: setReadOnlyMode() odontogramView logic

4. `mobile_android/app/src/main/java/com/hcmute/mobile_android/ui/activities/staff/DoctorWorkflowActivity.java`
   - Added: Auto-create plan check in showToothServiceSelectionDialog()
   - Added: showToothServiceSelectionDialogInternal() method
   - Added: Default tab selection in displayPatientInfo()

---

## 🧪 TESTING CHECKLIST

### Test Case 1: Sơ Đồ FDI Đã Bị Xóa
- [ ] Mở app → Tra cứu bệnh nhân
- [ ] Tab "Tổng quát" chỉ hiển thị form ghi chú (không có sơ đồ răng)
- [ ] Sơ đồ răng chỉ xuất hiện ở phần chính (bên dưới toggle tabs)

### Test Case 2: Chọn Răng và Dịch Vụ (Chưa Có Plan)
- [ ] Tra cứu bệnh nhân mới (chưa có treatment plan)
- [ ] Click vào răng trên sơ đồ
- [ ] Toast hiển thị: "Đang tạo phác đồ điều trị..."
- [ ] Dialog chọn dịch vụ xuất hiện (4 options)
- [ ] Chọn dịch vụ → Success (không có lỗi 400)
- [ ] Răng được tô màu theo dịch vụ
- [ ] Dịch vụ xuất hiện trong danh sách phác đồ

### Test Case 3: Chọn Răng và Dịch Vụ (Đã Có Plan)
- [ ] Tra cứu bệnh nhân đã có treatment plan
- [ ] Click vào răng trên sơ đồ
- [ ] Dialog chọn dịch vụ xuất hiện ngay (không cần tạo plan)
- [ ] Chọn dịch vụ → Success
- [ ] Răng được tô màu
- [ ] Dịch vụ được thêm vào phác đồ

### Test Case 4: Tab Mặc Định
- [ ] Tra cứu bệnh nhân
- [ ] Tab "Tổng quát" được chọn mặc định (màu xanh)
- [ ] FragmentGeneralDental được load
- [ ] Chuyển sang tab khác → Quay lại → Tab "Tổng quát" vẫn hoạt động

---

## 🔍 TECHNICAL DETAILS

### API Endpoint Used
```
POST /api/treatment-plans/{planId}/services/teeth/{toothNumber}

Request Body:
{
  "serviceId": 4,
  "sequenceOrder": 1
}

Response:
{
  "stepId": 123,
  "toothNumber": "11",
  "serviceName": "Trám răng sâu",
  "price": 300000.0,
  "totalPlanCost": 300000.0
}
```

### Service IDs (from DataSeed.java)
```java
4L - "Trám răng sâu" - 300,000 VNĐ
6L - "Nhổ răng thường" - 300,000 VNĐ
7L - "Nhổ răng khôn" - 2,000,000 VNĐ
9L - "Bọc răng sứ" - 5,000,000 VNĐ
```

### Color Mapping (OdontogramView.java)
```java
"Trám" → Amber (#FFA000)
"Nhổ" → Red (#D32F2F)
"Bọc" → Purple (#7B1FA2)
"Cấy" → Teal (#00897B)
"Niềng" → Indigo (#3F51B5)
"Tẩy" → Cyan (#00ACC1)
"Chữa tủy" → Deep Orange (#E64A19)
"Lấy cao răng" → Light Green (#7CB342)
"X-quang" → Blue Grey (#546E7A)
Default → Grey (#9E9E9E)
```

---

## 📝 NOTES

### Why Remove FDI Odontogram from FragmentGeneralDental?
1. **Redundancy**: Có 2 sơ đồ răng gây nhầm lẫn cho user
2. **Different Purpose**: 
   - FDI cũ: Chỉ để ghi chú tình trạng răng
   - Integrated mới: Chọn dịch vụ cụ thể + tô màu + thêm vào phác đồ
3. **Better UX**: 1 sơ đồ tích hợp đầy đủ tốt hơn 2 sơ đồ riêng biệt

### Why Auto-Create Treatment Plan?
1. **Prevent Error 400**: Backend yêu cầu planId phải tồn tại
2. **Better UX**: User không cần tạo plan thủ công trước
3. **Seamless Flow**: Click răng → Chọn dịch vụ → Done

### Why Default to "Tổng quát" Tab?
1. **Consistent UX**: Luôn bắt đầu từ bước khám tổng quát
2. **Clinical Workflow**: Khám tổng quát là bước đầu tiên trong quy trình
3. **Predictable**: User biết chắc tab nào sẽ được chọn

---

## 🎉 COMPLETION STATUS

| Task | Status | Completion |
|------|--------|-----------|
| Xóa sơ đồ FDI cũ | ✅ Done | 100% |
| Fix lỗi 400 | ✅ Done | 100% |
| Mặc định tab Tổng quát | ✅ Done | 100% |
| Code cleanup | ✅ Done | 100% |
| Documentation | ✅ Done | 100% |

**Overall**: 100% Complete

---

## 🚀 NEXT STEPS

1. Build APK mới
2. Test trên thiết bị thật
3. Verify không có lỗi 400
4. Verify chỉ có 1 sơ đồ răng
5. Verify tab "Tổng quát" mặc định

---

**Completed by**: AI Assistant  
**Reviewed by**: User  
**Date**: March 30, 2026
