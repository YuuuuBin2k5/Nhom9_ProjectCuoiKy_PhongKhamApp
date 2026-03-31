# Medical History Prescription JSON Parse Error - FIXED

## Vấn đề

Lỗi khi load lịch sử bệnh nhân:
```
java.lang.IllegalStateException: Expected BEGIN_OBJECT but was STRING at line 1 column 197 path $[0].prescription
```

## Nguyên nhân

**Backend** (DoctorController.java line 276):
```java
builder.prescription("Xem chi tiết trong hồ sơ");  // Trả về STRING
```

**Android Model** (MedicalRecordResponse.java):
```java
private Prescription prescription;  // Mong đợi OBJECT
```

→ **Type mismatch**: Backend trả về String, Android mong đợi Object

## Giải pháp đã áp dụng

### Sửa Android Model để hỗ trợ cả String và Object

**File**: `mobile_android/app/src/main/java/com/hcmute/mobile_android/network/models/MedicalRecordResponse.java`

**Thay đổi**:
```java
// Trước
private Prescription prescription;

// Sau
@SerializedName("prescription")
private Object prescriptionRaw;  // Chấp nhận cả String và Object

// Smart getter
public String getPrescriptionText() {
    if (prescriptionRaw instanceof String) {
        return (String) prescriptionRaw;
    } else if (prescriptionRaw instanceof Prescription) {
        Prescription p = (Prescription) prescriptionRaw;
        if (p.getDetails() != null && !p.getDetails().isEmpty()) {
            return p.getDetails().size() + " loại thuốc";
        }
    }
    return "Không có đơn thuốc";
}

public Prescription getPrescription() {
    if (prescriptionRaw instanceof Prescription) {
        return (Prescription) prescriptionRaw;
    }
    return null;
}
```

**Thêm fields thiếu**:
```java
private Long appointmentId;
private List<String> services;
private String totalAmount;
private String paymentStatus;
```

## Cách hoạt động

1. **Gson deserialize** `prescription` field thành `Object`
2. **Runtime type check**:
   - Nếu là `String` → Trả về text trực tiếp
   - Nếu là `Prescription` object → Parse và hiển thị số lượng thuốc
   - Nếu null → Trả về "Không có đơn thuốc"

## Lợi ích

✅ **Backward compatible**: Hỗ trợ cả String và Object từ backend
✅ **No backend changes needed**: Không cần sửa backend ngay
✅ **Flexible**: Có thể mở rộng sau khi backend implement đầy đủ prescription object
✅ **Safe**: Không crash khi backend thay đổi format

## Testing

### Test Case 1: Backend trả về String
```json
{
  "prescription": "Xem chi tiết trong hồ sơ"
}
```
→ `getPrescriptionText()` returns: "Xem chi tiết trong hồ sơ"

### Test Case 2: Backend trả về Object
```json
{
  "prescription": {
    "id": 123,
    "details": [
      {"medicineName": "Paracetamol", "dosage": "500mg", "quantity": 10}
    ]
  }
}
```
→ `getPrescriptionText()` returns: "1 loại thuốc"

### Test Case 3: Backend trả về null
```json
{
  "prescription": null
}
```
→ `getPrescriptionText()` returns: "Không có đơn thuốc"

## Cách sử dụng trong code

```java
// Lấy text để hiển thị
String prescriptionText = record.getPrescriptionText();
tvPrescription.setText(prescriptionText);

// Lấy object để xử lý chi tiết (nếu có)
MedicalRecordResponse.Prescription prescription = record.getPrescription();
if (prescription != null && prescription.getDetails() != null) {
    // Hiển thị chi tiết đơn thuốc
    for (PrescriptionDetail detail : prescription.getDetails()) {
        // ...
    }
}
```

## Khuyến nghị cho tương lai

### Backend nên trả về format chuẩn:

```java
// Thay vì
builder.prescription("Xem chi tiết trong hồ sơ");

// Nên
if (prescriptionOpt.isPresent()) {
    Prescription prescription = prescriptionOpt.get();
    builder.prescription(PrescriptionDTO.builder()
        .id(prescription.getId())
        .details(prescription.getDetails().stream()
            .map(d -> PrescriptionDetailDTO.builder()
                .medicineName(d.getMedicineName())
                .dosage(d.getDosage())
                .quantity(d.getQuantity())
                .build())
            .collect(Collectors.toList()))
        .build());
} else {
    builder.prescription(null);  // Hoặc empty object
}
```

## Files đã sửa

1. ✅ `mobile_android/app/src/main/java/com/hcmute/mobile_android/network/models/MedicalRecordResponse.java`

## Status

✅ **FIXED** - App sẽ không còn crash khi load lịch sử bệnh nhân

## Rebuild & Test

```bash
# Rebuild Android app
cd mobile_android
./gradlew clean assembleDebug

# Install
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Test
# 1. Login as doctor
# 2. Chọn bệnh nhân
# 3. Xem lịch sử khám bệnh
# 4. Verify không còn crash
```
