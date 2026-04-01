# Queue Management Fix - Summary Report

## 🎯 Vấn đề đã fix

### Vấn đề ban đầu
Logic "lùi 1 người" không rõ ràng và không hoạt động đúng với use case trong tài liệu:
- `delayPatient()` có logic đúng nhưng thiếu validation và error handling
- `skipCurrentPatient()` thiếu notification và logging chi tiết
- Không có endpoint `/skip` trong QueueController
- Không có tài liệu giải thích sự khác biệt giữa 2 chức năng

### Root Cause
Nhầm lẫn giữa 2 use case khác nhau:
1. **Delay**: Bệnh nhân đang chờ muốn nhường lượt (ít dùng)
2. **Skip**: Bác sĩ gọi nhưng bệnh nhân không có mặt (use case chính)

---

## ✅ Các thay đổi đã thực hiện

### 1. Backend - CheckInQueueService.java

#### `delayPatient()` - Cải thiện
- ✅ Thêm validation chi tiết với error message rõ ràng
- ✅ Kiểm tra bệnh nhân có phải cuối hàng không
- ✅ Thêm logging chi tiết
- ✅ Thêm JavaDoc giải thích use case
- ✅ Throw exception với message hướng dẫn dùng Skip nếu status sai

**Trước:**
```java
if (q.getStatus() != QueueStatus.WAITING && q.getStatus() != QueueStatus.RETURNED_PRIORITY) {
    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chỉ có thể lùi lượt cho bệnh nhân đang chờ");
}
```

**Sau:**
```java
if (q.getStatus() != QueueStatus.WAITING && q.getStatus() != QueueStatus.RETURNED_PRIORITY) {
    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
        "Chỉ có thể lùi lượt cho bệnh nhân đang chờ. Nếu bệnh nhân đang khám, vui lòng dùng chức năng 'Lùi 1 người' (Skip).");
}
```

#### `skipCurrentPatient()` - Cải thiện
- ✅ Thêm logging chi tiết với tên bệnh nhân
- ✅ Thêm notification cho người tiếp theo
- ✅ Clear `startedAt` khi chuyển về WAITING
- ✅ Set `startedAt` khi gọi người tiếp theo
- ✅ Thêm JavaDoc chi tiết với reference đến UC_24
- ✅ Better error messages

**Thêm mới:**
```java
// Send notification to next patient
if (next.getAppointment() != null && next.getAppointment().getPatient() != null) {
    Patient nextPatient = next.getAppointment().getPatient();
    Notification notif = Notification.builder()
            .patient(nextPatient)
            .title("Đến lượt khám")
            .message("Vui lòng vào " + room.getName() + ". Số thứ tự: " + next.getQueueNumber())
            .type("QUEUE_CALLED")
            .build();
    notificationRepository.save(notif);
    fcmService.sendNotification(nextPatient.getFcmToken(), notif.getTitle(), notif.getMessage());
}
```

### 2. Backend - QueueController.java

#### Thêm endpoint `/skip`
- ✅ Thêm `POST /api/queue/{id}/skip` để Doctor/Admin có thể gọi
- ✅ Giữ nguyên endpoint cũ `/api/reception/queue/{id}/skip` cho backward compatibility
- ✅ Response format nhất quán với ReceptionController

**Code mới:**
```java
@PostMapping("/{id}/skip")
public ResponseEntity<?> skipCurrentPatient(@PathVariable Long id) {
    try {
        checkInQueueService.skipCurrentPatient(id);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Đã lùi bệnh nhân và gọi người tiếp theo"
        ));
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(Map.of(
            "success", false,
            "message", e.getMessage()
        ));
    }
}
```

### 3. Android - ApiService.java

#### Thêm comment và endpoint mới
- ✅ Thêm comment giải thích use case cho mỗi endpoint
- ✅ Thêm `skipPatientAlt()` cho endpoint mới `/api/queue/{id}/skip`
- ✅ Thêm `delayWaitingPatient()` cho chức năng delay

**Code mới:**
```java
// Skip current patient (IN_PROGRESS) - moves back to WAITING and auto-calls next patient
// Use case: Doctor called patient but they're not present or need time
@POST("api/reception/queue/{id}/skip")
Call<Void> skipPatient(@Path("id") Long queueId);

@POST("api/queue/{id}/skip")
Call<Void> skipPatientAlt(@Path("id") Long queueId);

// Delay waiting patient by one position (swap with next person)
@POST("api/queue/{id}/delay")
Call<Void> delayWaitingPatient(@Path("id") Long queueId);
```

### 4. Documentation

#### Tạo mới
- ✅ `QUEUE_DELAY_VS_SKIP.md` - Tài liệu chi tiết so sánh 2 chức năng
- ✅ `QUEUE_MANAGEMENT_GUIDE.md` - Quick reference guide
- ✅ `QUEUE_FIX_SUMMARY.md` - Báo cáo tổng kết (file này)

#### Cập nhật
- ✅ `KIEN_TRUC_VA_LOGIC.md` - Ghi nhận fix trong mục "Rủi ro / nợ kỹ thuật"

### 5. Testing

#### Tạo test skeleton
- ✅ `QueueDelaySkipTest.java` - Test cases cho cả 2 chức năng
- ✅ Document expected behavior cho từng test case

---

## 📊 Kết quả

### Trước khi fix
- ❌ Logic không rõ ràng
- ❌ Thiếu validation
- ❌ Không có notification
- ❌ Logging không đầy đủ
- ❌ Không có tài liệu
- ❌ Endpoint không nhất quán

### Sau khi fix
- ✅ Logic rõ ràng, tách biệt 2 use case
- ✅ Validation đầy đủ với error message hướng dẫn
- ✅ Notification cho người tiếp theo
- ✅ Logging chi tiết với tên bệnh nhân
- ✅ Tài liệu đầy đủ (3 file)
- ✅ Endpoint nhất quán, backward compatible
- ✅ Test cases documented
- ✅ No compilation errors

---

## 🎯 Use Case Coverage

### ✅ UC_24_ProcessQueue.md
> "Bác sĩ bấm duyệt chọn người tại Index 0"
> "Khi quy trình gặp/chẩn án kết thúc, bác sĩ điều chỉnh trạng thái ca khám"

**Covered by:** `skipCurrentPatient()`
- Bác sĩ gọi bệnh nhân (IN_PROGRESS)
- Bệnh nhân không có mặt → skip
- Tự động gọi người tiếp theo
- Broadcast real-time

### ✅ PLAN_SMART_DENTAL_BO_SUNG.md
> "Bài toán: Rẽ nhánh X-Quang & Đôn hàng đợi"
> "A quay lại có kết quả X-Quang → ưu tiên"

**Covered by:** Priority system
- X-ray return: +10 priority (RETURNED_PRIORITY)
- Skip: +5 priority
- Normal: 0 priority
- Sort: priority DESC, queueNumber ASC

---

## 🔍 Testing Checklist

### Manual Testing
- [ ] Test delay với bệnh nhân WAITING
- [ ] Test delay với bệnh nhân cuối hàng (should fail)
- [ ] Test delay với bệnh nhân IN_PROGRESS (should fail with helpful message)
- [ ] Test skip với bệnh nhân IN_PROGRESS
- [ ] Test skip với bệnh nhân WAITING (should fail)
- [ ] Test skip khi không có người tiếp theo
- [ ] Verify notification được gửi cho người tiếp theo
- [ ] Verify broadcast real-time hoạt động
- [ ] Test priority order: +10 > +5 > 0

### API Testing
```bash
# Test skip
curl -X POST http://localhost:8080/api/queue/123/skip \
  -H "Authorization: Bearer TOKEN"

# Test delay
curl -X POST http://localhost:8080/api/queue/123/delay \
  -H "Authorization: Bearer TOKEN"
```

---

## 📚 Files Changed

### Modified
1. `clinic_backend/src/main/java/com/hcmute/clinic/service/CheckInQueueService.java`
2. `clinic_backend/src/main/java/com/hcmute/clinic/controller/QueueController.java`
3. `mobile_android/app/src/main/java/com/hcmute/mobile_android/network/ApiService.java`
4. `prod/KIEN_TRUC_VA_LOGIC.md`

### Created
1. `clinic_backend/docs/QUEUE_DELAY_VS_SKIP.md`
2. `clinic_backend/docs/QUEUE_MANAGEMENT_GUIDE.md`
3. `clinic_backend/docs/QUEUE_FIX_SUMMARY.md`
4. `clinic_backend/src/test/java/com/hcmute/clinic/service/QueueDelaySkipTest.java`

---

## 🚀 Next Steps

1. **Manual Testing**: Test tất cả các trường hợp trong checklist
2. **Implement Unit Tests**: Hoàn thiện các test case trong `QueueDelaySkipTest.java`
3. **UI Update** (optional): Thêm nút "Nhường lượt" cho bệnh nhân đang chờ
4. **Monitor**: Theo dõi logs để đảm bảo logic hoạt động đúng trong production

---

## 📞 Support

Nếu có vấn đề, tham khảo:
- **Quick Guide**: `clinic_backend/docs/QUEUE_MANAGEMENT_GUIDE.md`
- **Detailed Comparison**: `clinic_backend/docs/QUEUE_DELAY_VS_SKIP.md`
- **Architecture**: `prod/KIEN_TRUC_VA_LOGIC.md`

---

*Fix completed: 2024*
*Verified: No compilation errors, logic matches documentation*
