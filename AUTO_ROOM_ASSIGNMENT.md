# TỰ ĐỘNG GÁN PHÒNG CHO DỊCH VỤ

## 🎯 VẤN ĐỀ

Khi bác sĩ thêm dịch vụ thủ công bằng nút "+Dịch vụ" (không phải từ template), step mới không có `clinic_room_id`. Khi hoàn thành bước trước đó, hệ thống không biết chuyển bệnh nhân sang phòng nào.

**Ví dụ:**
1. Bác sĩ Phòng 01 khám bệnh nhân
2. Bác sĩ thêm dịch vụ "Chụp X-quang răng" bằng nút "+Dịch vụ"
3. Bác sĩ hoàn thành bước khám
4. ❌ Hệ thống KHÔNG chuyển bệnh nhân sang Phòng X-quang vì step không có `clinic_room_id`

## ✅ GIẢI PHÁP

Thêm logic tự động gán phòng dựa trên tên dịch vụ khi tạo step mới.

### Backend Changes

**File:** `clinic_backend/src/main/java/com/hcmute/clinic/service/TreatmentPlanService.java`

#### 1. Sửa method `updateSteps()` (Line ~190)

```java
// Auto-assign clinic room based on service if not provided
ClinicRoom room = null;
if (item.getClinicRoomId() != null) {
    room = clinicRoomRepository.findById(item.getClinicRoomId()).orElse(null);
} else {
    // ✅ NEW: Try to find appropriate room based on service name
    room = findRoomForService(svc);
}
```

**Before:** Chỉ gán phòng nếu mobile gửi `clinicRoomId`
**After:** Tự động tìm phòng phù hợp dựa trên tên dịch vụ

#### 2. Thêm method `findRoomForService()` (Line ~445)

```java
/**
 * Auto-assign clinic room based on service name/type
 * This allows manually added services to be assigned to the correct room
 */
private ClinicRoom findRoomForService(com.hcmute.clinic.entity.Service service) {
    if (service == null || service.getName() == null) {
        return null;
    }
    
    String serviceName = service.getName().toLowerCase();
    
    // Map service names to room names
    if (serviceName.contains("x-quang") || serviceName.contains("xquang") || serviceName.contains("x quang")) {
        return clinicRoomRepository.findAll().stream()
                .filter(r -> r.getName() != null && r.getName().toLowerCase().contains("x-quang"))
                .findFirst()
                .orElse(null);
    }
    
    if (serviceName.contains("nhổ răng") || serviceName.contains("phẫu thuật") || serviceName.contains("tiểu phẫu")) {
        return clinicRoomRepository.findAll().stream()
                .filter(r -> r.getName() != null && (r.getName().toLowerCase().contains("phẫu") || r.getName().toLowerCase().contains("surgery")))
                .findFirst()
                .orElse(null);
    }
    
    if (serviceName.contains("niềng") || serviceName.contains("chỉnh nha") || serviceName.contains("ortho")) {
        return clinicRoomRepository.findAll().stream()
                .filter(r -> r.getName() != null && r.getName().toLowerCase().contains("chỉnh nha"))
                .findFirst()
                .orElse(null);
    }
    
    // Default: return null (will be handled by current doctor's room)
    return null;
}
```

### Mapping Rules

| Tên dịch vụ chứa | Phòng được gán |
|------------------|----------------|
| "x-quang", "xquang", "x quang" | Phòng X-quang |
| "nhổ răng", "phẫu thuật", "tiểu phẫu" | Phòng tiểu phẫu |
| "niềng", "chỉnh nha", "ortho" | Phòng chỉnh nha |
| Khác | null (giữ nguyên phòng hiện tại) |

## 🔄 WORKFLOW SAU KHI FIX

### Scenario: Thêm dịch vụ X-quang thủ công

1. **Bác sĩ Phòng 01 khám bệnh nhân**
   - Scan QR → Load patient info
   - Tạo treatment plan (hoặc không tạo từ template)

2. **Thêm dịch vụ X-quang**
   - Nhấn "+Dịch vụ"
   - Chọn "Chụp X-quang răng"
   - Step mới được thêm vào list local (chưa có ID, chưa có clinic_room_id)

3. **Auto-save**
   - `saveTreatmentPlanInternal(true, callback)` được gọi
   - Backend nhận request với step mới
   - ✅ Backend gọi `findRoomForService(svc)`
   - ✅ Tìm thấy "x-quang" trong tên dịch vụ
   - ✅ Tìm phòng có tên chứa "x-quang"
   - ✅ Gán `clinic_room_id` = Phòng X-quang
   - Step được save với `clinic_room_id` đã gán

4. **Reload plan**
   - Mobile reload plan từ backend
   - Step X-quang bây giờ có `clinic_room_id` = Phòng X-quang

5. **Hoàn thành bước khám**
   - Bác sĩ nhấn "Hoàn thành" cho bước khám
   - Backend gọi `completeStepAndAdvance()`
   - Tìm next step = Step X-quang
   - ✅ `nextStep.getClinicRoom()` = Phòng X-quang (không null)
   - ✅ Chuyển queue sang Phòng X-quang
   - ✅ Gửi notification cho bệnh nhân
   - ✅ Return "Phòng X-quang"

6. **Bệnh nhân đến Phòng X-quang**
   - Bác sĩ X-quang login
   - Thấy bệnh nhân trong queue với status WAITING

## 🧪 TESTING

### Test case 1: Thêm dịch vụ X-quang thủ công

```
1. Login bác sĩ Phòng 01 (doc01@gmail.com / password123)
2. Scan QR bệnh nhân
3. Nhấn "+Dịch vụ" → Chọn "Chụp X-quang răng"
4. Nhấn "Hoàn thành" cho bước hiện tại
Expected: 
   - Dialog "Chuyển phòng" → "Phòng X-quang"
   - Bệnh nhân được chuyển sang Phòng X-quang
5. Login bác sĩ X-quang (doc_xray@gmail.com / password123)
Expected:
   - Thấy bệnh nhân trong queue
```

### Test case 2: Thêm dịch vụ Nhổ răng khôn

```
1. Login bác sĩ Phòng 01
2. Scan QR bệnh nhân
3. Nhấn "+Dịch vụ" → Chọn "Nhổ răng khôn"
4. Nhấn "Hoàn thành" cho bước hiện tại
Expected:
   - Dialog "Chuyển phòng" → "Phòng tiểu phẫu"
   - Bệnh nhân được chuyển sang Phòng tiểu phẫu
```

### Test case 3: Thêm dịch vụ thường (không có phòng đặc biệt)

```
1. Login bác sĩ Phòng 01
2. Scan QR bệnh nhân
3. Nhấn "+Dịch vụ" → Chọn "Lấy cao răng"
4. Nhấn "Hoàn thành" cho bước hiện tại
Expected:
   - Không chuyển phòng (vì dịch vụ không match rule nào)
   - Bệnh nhân tiếp tục ở Phòng 01
```

## 📊 IMPACT

### Trước fix:
- ❌ Dịch vụ thêm thủ công không có phòng
- ❌ Không chuyển phòng khi hoàn thành bước trước
- ❌ Bệnh nhân bị "mắc kẹt" ở phòng hiện tại

### Sau fix:
- ✅ Tự động gán phòng dựa trên tên dịch vụ
- ✅ Chuyển phòng tự động khi hoàn thành bước trước
- ✅ Workflow mượt mà, không cần can thiệp thủ công

## 🎯 EXTENSIBILITY

Để thêm mapping cho dịch vụ mới, chỉ cần thêm điều kiện trong `findRoomForService()`:

```java
if (serviceName.contains("implant") || serviceName.contains("cấy ghép")) {
    return clinicRoomRepository.findAll().stream()
            .filter(r -> r.getName() != null && r.getName().toLowerCase().contains("implant"))
            .findFirst()
            .orElse(null);
}
```

## ✅ COMPILATION STATUS

- ✅ Backend: BUILD SUCCESS
- ✅ No errors, ready for testing

---

**Ngày implement:** 28/03/2026
**Status:** ✅ COMPLETE - Ready for testing
**Feature:** Auto room assignment for manually added services

