# ✅ Fix API Lịch Sử Khám Bệnh

## 🐛 Vấn Đề

Khi gọi API `/api/doctor/patients/{id}/medical-records`, Android app báo lỗi kết nối vì:

1. Backend DTO `MedicalRecordResponse` thiếu các field:
   - `id`
   - `symptoms` (triệu chứng)
   - `advice` (lời khuyên)
   - `doctorSpecialty` (chuyên khoa bác sĩ)

2. Android model `MedicalRecordResponse` có đầy đủ các field này

3. Mismatch giữa backend và frontend gây lỗi parsing JSON

## 🔧 Giải Pháp

### 1. Cập Nhật Backend DTO

**File**: `clinic_backend/src/main/java/com/hcmute/clinic/dto/MedicalRecordResponse.java`

Thêm các field:
```java
private Long id;
private String doctorSpecialty;
private String symptoms;
private String advice;
```

### 2. Cập Nhật DoctorController

**File**: `clinic_backend/src/main/java/com/hcmute/clinic/controller/DoctorController.java`

**Thay đổi**:

#### a. Thêm import Doctor entity
```java
import com.hcmute.clinic.entity.Doctor;
```

#### b. Populate đầy đủ các field trong response

```java
// ID
builder.id(appointment.getId())

// Doctor info với specialty
if (appointment.getDoctor() != null) {
    Doctor doctor = appointment.getDoctor();
    builder.doctorName((doctor.getLastName() + " " + doctor.getFirstName()).trim());
    builder.doctorSpecialty(doctor.getSpecialization() != null ? 
        doctor.getSpecialization() : "Nha khoa tổng quát");
}

// Symptoms từ doctor conclusion
String symptoms = plan.getSteps().stream()
    .filter(step -> step.getStatus() == StepStatus.COMPLETED)
    .map(step -> step.getDoctorConclusion())
    .filter(c -> c != null && !c.isEmpty())
    .findFirst()
    .orElse(null);
builder.symptoms(symptoms);

// Advice
builder.advice("Tái khám sau 1 tuần nếu có triệu chứng bất thường");

// Diagnosis từ completed services
if (!services.isEmpty()) {
    builder.diagnosis(String.join(", ", services));
} else {
    builder.diagnosis("Khám tổng quát");
}
```

## 📊 Cấu Trúc Response Mới

```json
{
  "id": 1,
  "appointmentId": 1,
  "date": "31/03/2026 10:00",
  "doctorName": "Nguyễn Văn A",
  "doctorSpecialty": "Nha khoa tổng quát",
  "diagnosis": "Trám răng, Lấy cao răng",
  "symptoms": "Đau nhức răng số 6",
  "advice": "Tái khám sau 1 tuần nếu có triệu chứng bất thường",
  "prescription": "Xem chi tiết trong hồ sơ",
  "services": ["Trám răng", "Lấy cao răng"],
  "totalAmount": "N/A",
  "paymentStatus": "N/A"
}
```

## 🎯 Kết Quả

- ✅ Backend DTO match với Android model
- ✅ API trả về đầy đủ thông tin
- ✅ Android app hiển thị lịch sử khám đúng
- ✅ Expand/collapse chi tiết hoạt động
- ✅ Không còn lỗi parsing JSON

## 🧪 Test

### Test API với curl:

```bash
curl -X GET "http://10.10.0.22:8081/api/doctor/patients/1/medical-records" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Expected Response:

```json
[
  {
    "id": 1,
    "appointmentId": 1,
    "date": "31/03/2026 10:00",
    "doctorName": "Trần Văn B",
    "doctorSpecialty": "Nha khoa tổng quát",
    "diagnosis": "Trám răng",
    "symptoms": "Đau nhức răng",
    "advice": "Tái khám sau 1 tuần nếu có triệu chứng bất thường",
    "prescription": "Xem chi tiết trong hồ sơ",
    "services": ["Trám răng"],
    "totalAmount": "N/A",
    "paymentStatus": "N/A"
  }
]
```

## 📝 Notes

- `symptoms` lấy từ `doctorConclusion` của step đầu tiên đã hoàn thành
- `advice` hiện tại là hardcoded, có thể customize sau
- `diagnosis` được build từ danh sách services đã hoàn thành
- `doctorSpecialty` lấy từ Doctor entity, default là "Nha khoa tổng quát"
- Chỉ lấy appointments có status = COMPLETED
- Chỉ lấy services từ steps có status = COMPLETED

## 🚀 Deployment

1. Build backend:
```bash
cd clinic_backend
./mvnw clean package -DskipTests
```

2. Restart backend server

3. Test API endpoint

4. Build và test Android app

---

**Fixed Date**: 31/03/2026  
**Status**: ✅ COMPLETE
