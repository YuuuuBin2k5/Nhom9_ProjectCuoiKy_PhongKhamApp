# Triển Khai Màn Hình Chi Tiết Bệnh Nhân Trước Khám

## 📋 Phân Tích Nghiệp Vụ

### Yêu Cầu
1. **Lần đầu tiên bác sĩ nhấp vào bệnh nhân**: Hiển thị màn hình chi tiết giới thiệu bệnh nhân
2. **Các lần sau**: Vào trực tiếp form khám
3. **Nút "i" trong form khám**: Mở lại màn hình chi tiết
4. **Di chuyển lịch sử khám**: Từ form khám sang màn hình chi tiết

### Dữ Liệu Hiển Thị
- Thông tin cá nhân bệnh nhân (từ PatientInfo)
- Ghi chú/hồ sơ tự cập nhật của bệnh nhân
- Lịch sử khám bệnh (từ MedicalRecordResponse)
- Triệu chứng, chẩn đoán, lời khuyên từ các lần khám trước

### Luồng Nghiệp Vụ

```
[Bác sĩ tra cứu bệnh nhân]
         ↓
[Kiểm tra: Đã từng khám bệnh nhân này chưa?]
         ↓
    ┌────┴────┐
    │         │
[Lần đầu]  [Đã khám]
    │         │
    ↓         ↓
[Màn hình  [Vào trực tiếp
 chi tiết]  form khám]
    │
    ↓
[Nút "Bắt đầu khám"]
    ↓
[Form khám với nút "i"]
```

## 🎨 Thiết Kế UI

### Màn Hình Chi Tiết Bệnh Nhân
```
┌─────────────────────────────────────┐
│  ← Quay lại    Chi Tiết Bệnh Nhân   │
├─────────────────────────────────────┤
│                                     │
│  👤 Nguyễn Văn A                    │
│  📞 0901234567                      │
│  🎂 01/01/1990 (34 tuổi)            │
│  ⚥  Nam                             │
│  📍 123 Đường ABC, Q1, TP.HCM       │
│                                     │
├─────────────────────────────────────┤
│  📝 Ghi Chú Của Bệnh Nhân           │
│  ┌───────────────────────────────┐ │
│  │ - Tiền sử dị ứng thuốc X      │ │
│  │ - Đang điều trị bệnh Y        │ │
│  │ - Ghi chú khác...             │ │
│  └───────────────────────────────┘ │
├─────────────────────────────────────┤
│  📋 Lịch Sử Khám Bệnh               │
│  ┌───────────────────────────────┐ │
│  │ 15/03/2026 - BS. Trần Văn B   │ │
│  │ Chẩn đoán: Sâu răng số 6      │ │
│  │ Triệu chứng: Đau nhức...      │ │
│  └───────────────────────────────┘ │
│  ┌───────────────────────────────┐ │
│  │ 01/02/2026 - BS. Lê Thị C     │ │
│  │ Chẩn đoán: Viêm nướu          │ │
│  └───────────────────────────────┘ │
├─────────────────────────────────────┤
│                                     │
│     [Bắt Đầu Khám Bệnh]            │
│                                     │
└─────────────────────────────────────┘
```

### Nút "i" Trong Form Khám
```
┌─────────────────────────────────────┐
│  ← Quay lại  Khám Bệnh  [i]         │
│                          ↑           │
│                    Nút xem chi tiết  │
└─────────────────────────────────────┘
```

## 🔧 Thiết Kế Kỹ Thuật

### 1. Shared Preferences - Lưu Trạng Thái
```java
// Key: "doctor_{doctorId}_patient_{patientId}_first_visit"
// Value: true/false
```

### 2. Models Mới

#### PatientDetailInfo.java
```java
public class PatientDetailInfo {
    private PatientInfo basicInfo;
    private String patientNotes;  // Ghi chú tự cập nhật
    private List<MedicalRecordResponse> medicalHistory;
}
```

### 3. Activities Mới

#### PatientDetailActivity.java
- Hiển thị thông tin chi tiết bệnh nhân
- Load lịch sử khám từ API
- Nút "Bắt đầu khám" → DoctorWorkflowActivity

### 4. Cập Nhật DoctorWorkflowActivity

#### Thêm nút "i" trong header
```xml
<ImageButton
    android:id="@+id/btnPatientInfo"
    android:src="@drawable/ic_info"
    android:contentDescription="Xem chi tiết bệnh nhân" />
```

#### Logic kiểm tra lần đầu
```java
private boolean isFirstVisit(Long patientId) {
    SharedPreferences prefs = getSharedPreferences("doctor_visits", MODE_PRIVATE);
    String key = "doctor_" + currentDoctorId + "_patient_" + patientId + "_visited";
    return !prefs.getBoolean(key, false);
}

private void markPatientVisited(Long patientId) {
    SharedPreferences prefs = getSharedPreferences("doctor_visits", MODE_PRIVATE);
    String key = "doctor_" + currentDoctorId + "_patient_" + patientId + "_visited";
    prefs.edit().putBoolean(key, true).apply();
}
```

### 5. API Endpoints Cần Thiết

#### Backend - Thêm endpoint lấy ghi chú bệnh nhân
```java
@GetMapping("/api/patients/{id}/notes")
public ResponseEntity<String> getPatientNotes(@PathVariable Long id)
```

## 📝 Implementation Plan

### Phase 1: Backend API
1. ✅ API lấy lịch sử khám đã có: `/api/doctor/patients/{id}/medical-records`
2. 🔨 Thêm API lấy ghi chú bệnh nhân (nếu chưa có)

### Phase 2: Android Models & API
1. Tạo PatientDetailInfo model
2. Thêm API call trong ApiService

### Phase 3: UI Components
1. Tạo layout `activity_patient_detail.xml`
2. Tạo layout `item_medical_history.xml`
3. Thêm icon `ic_info.xml`

### Phase 4: Activity Implementation
1. Tạo PatientDetailActivity
2. Implement adapter cho lịch sử khám
3. Xử lý navigation

### Phase 5: Integration với DoctorWorkflowActivity
1. Thêm logic kiểm tra lần đầu
2. Thêm nút "i" trong header
3. Di chuyển phần lịch sử khám
4. Update navigation flow

### Phase 6: Testing
1. Test lần đầu khám → hiện detail
2. Test lần sau → vào trực tiếp form
3. Test nút "i" → mở detail
4. Test lịch sử khám hiển thị đúng

## 🎯 Acceptance Criteria

- [ ] Lần đầu bác sĩ tra cứu bệnh nhân → Hiện màn hình chi tiết
- [ ] Lần sau tra cứu cùng bệnh nhân → Vào trực tiếp form khám
- [ ] Nút "i" trong form khám → Mở màn hình chi tiết
- [ ] Lịch sử khám hiển thị trong màn hình chi tiết
- [ ] UI đẹp, rõ ràng, dễ đọc
- [ ] Không còn lịch sử khám trong form khám

## 📊 Data Flow

```
PatientInfo (API) → PatientDetailActivity
                         ↓
                    Display Info
                         ↓
                    Load Medical History (API)
                         ↓
                    Display History
                         ↓
                    [Bắt đầu khám] clicked
                         ↓
                    Mark as visited (SharedPreferences)
                         ↓
                    Open DoctorWorkflowActivity
```

## 🔐 Security Considerations

1. Chỉ bác sĩ được phép xem chi tiết bệnh nhân
2. Kiểm tra quyền truy cập trước khi load data
3. Không cache thông tin nhạy cảm
4. Clear data khi logout

## 📱 Responsive Design

- Scroll view cho nội dung dài
- Card view cho từng section
- Spacing hợp lý giữa các phần
- Font size dễ đọc (14-16sp cho nội dung)
- Color coding cho các loại thông tin

---

**Prepared by**: AI Assistant  
**Date**: 31/03/2026  
**Status**: Ready for Implementation
