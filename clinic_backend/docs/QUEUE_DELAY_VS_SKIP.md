# Hướng dẫn: Phân biệt "Delay" và "Skip" trong Quản lý Hàng đợi

## 📋 Tổng quan

Hệ thống có **2 chức năng "lùi"** khác nhau, phục vụ 2 use case khác nhau:

| Chức năng | Use Case | Status | Endpoint |
|-----------|----------|--------|----------|
| **Delay** | Bệnh nhân đang chờ muốn nhường lượt | WAITING, RETURNED_PRIORITY | `POST /api/queue/{id}/delay` |
| **Skip** | Bác sĩ gọi nhưng bệnh nhân không có mặt | IN_PROGRESS | `POST /api/queue/{id}/skip` hoặc `POST /api/reception/queue/{id}/skip` |

---

## 1️⃣ DELAY - Lùi bệnh nhân đang chờ

### Use Case
Bệnh nhân đang ngồi chờ ở sảnh (status = `WAITING`) muốn nhường lượt cho người sau (ví dụ: đi vệ sinh, chờ người thân, v.v.)

### Logic
```
1. Kiểm tra status phải là WAITING hoặc RETURNED_PRIORITY
2. Tìm vị trí của bệnh nhân trong danh sách chờ (đã sort theo priority DESC, queueNumber ASC)
3. Hoán đổi queueNumber với người tiếp theo
4. Broadcast cập nhật
```

### Ví dụ
```
Trước:
- Số 5: Nguyễn Văn A (WAITING, priority=0)
- Số 6: Trần Thị B (WAITING, priority=0)
- Số 7: Lê Văn C (WAITING, priority=0)

Sau khi delay A:
- Số 6: Trần Thị B (WAITING, priority=0)  ← lên trước
- Số 5: Nguyễn Văn A (WAITING, priority=0)  ← lùi xuống
- Số 7: Lê Văn C (WAITING, priority=0)
```

### API Request
```bash
POST /api/queue/123/delay
Authorization: Bearer <token>
```

### Response
```json
{
  "message": "Đã đẩy lùi bệnh nhân xuống 1 vị trí"
}
```

### Lưu ý
- Chỉ hoán đổi `queueNumber`, KHÔNG thay đổi `priorityLevel`
- Không tự động gọi người tiếp theo vào phòng
- Nếu bệnh nhân đã ở cuối hàng → báo lỗi

---

## 2️⃣ SKIP - Lùi bệnh nhân đang khám

### Use Case (theo UC_24_ProcessQueue.md)
Bác sĩ đã gọi bệnh nhân A vào phòng (status = `IN_PROGRESS`), nhưng:
- Bệnh nhân không có mặt
- Bệnh nhân cần thời gian (chờ gây tê, chờ kết quả X-quang, v.v.)
- Bác sĩ muốn khám người tiếp theo thay vì chờ

### Logic
```
1. Kiểm tra status phải là IN_PROGRESS
2. Chuyển bệnh nhân hiện tại → WAITING với +5 priority
3. Clear startedAt (vì không còn đang khám)
4. Tìm người tiếp theo trong hàng chờ (theo priority DESC, queueNumber ASC)
5. TỰ ĐỘNG gọi người tiếp theo → IN_PROGRESS
6. Gửi notification cho người tiếp theo
7. Broadcast cập nhật real-time
```

### Ví dụ
```
Trước:
- Số 5: Nguyễn Văn A (IN_PROGRESS, priority=0) ← đang khám
- Số 6: Trần Thị B (WAITING, priority=0)
- Số 7: Lê Văn C (WAITING, priority=0)

Sau khi skip A:
- Số 6: Trần Thị B (IN_PROGRESS, priority=0) ← tự động được gọi vào
- Số 5: Nguyễn Văn A (WAITING, priority=5) ← quay lại chờ với priority cao
- Số 7: Lê Văn C (WAITING, priority=0)

Khi A sẵn sàng, A sẽ được ưu tiên vì priority=5 > 0
```

### API Request
```bash
# Endpoint 1 (cho Reception/Doctor/Admin)
POST /api/reception/queue/123/skip
Authorization: Bearer <token>

# Endpoint 2 (cho Doctor/Admin)
POST /api/queue/123/skip
Authorization: Bearer <token>
```

### Response
```json
{
  "success": true,
  "message": "Đã lùi bệnh nhân và gọi người tiếp theo"
}
```

### Lưu ý
- Tự động gọi người tiếp theo (khác với Delay)
- Bệnh nhân bị skip được +5 priority → ưu tiên khi sẵn sàng
- Gửi notification cho người tiếp theo
- Broadcast real-time cho tất cả client

---

## 🎯 So sánh chi tiết

| Tiêu chí | DELAY | SKIP |
|----------|-------|------|
| **Status yêu cầu** | WAITING, RETURNED_PRIORITY | IN_PROGRESS |
| **Ai thực hiện** | Lễ tân / Bệnh nhân tự nguyện | Bác sĩ |
| **Thay đổi priority** | Không | Có (+5) |
| **Gọi người tiếp theo** | Không | Có (tự động) |
| **Gửi notification** | Không | Có |
| **Use case chính** | Nhường lượt tạm thời | Bệnh nhân không có mặt |
| **Tần suất sử dụng** | Ít | Nhiều |

---

## 📱 UI Implementation

### QueueManagementActivity (Android)

```java
// Nút "Lùi 1 người" → gọi skipPatient (cho IN_PROGRESS)
@Override
public void onSkipPatient(QueueItem item) {
    if ("IN_PROGRESS".equals(item.getStatus())) {
        // Show confirmation dialog
        new AlertDialog.Builder(this)
            .setTitle("Lùi 1 người")
            .setMessage("Bệnh nhân sẽ quay lại hàng đợi với độ ưu tiên cao.\n\n" +
                       "Người tiếp theo sẽ được gọi vào phòng.\n\nXác nhận?")
            .setPositiveButton("Xác nhận", (dialog, which) -> {
                apiService.skipPatient(item.getId()).enqueue(...);
            })
            .show();
    } else if ("WAITING".equals(item.getStatus())) {
        // Optional: implement delay for waiting patients
        apiService.delayWaitingPatient(item.getId()).enqueue(...);
    }
}
```

---

## 🔧 Troubleshooting

### Lỗi: "Chỉ có thể lùi bệnh nhân đang khám (IN_PROGRESS)"
- **Nguyên nhân**: Đang gọi `skipPatient()` cho bệnh nhân có status khác IN_PROGRESS
- **Giải pháp**: Kiểm tra status trước khi gọi API, hoặc dùng `delayPatient()` cho WAITING

### Lỗi: "Bệnh nhân đã ở cuối hàng đợi"
- **Nguyên nhân**: Đang gọi `delayPatient()` cho bệnh nhân cuối cùng
- **Giải pháp**: Disable nút "Delay" cho bệnh nhân cuối cùng trong UI

### Không có người tiếp theo sau khi skip
- **Hành vi**: Bệnh nhân hiện tại được chuyển về WAITING, nhưng không có ai được gọi vào
- **Nguyên nhân**: Không còn ai trong hàng chờ
- **Giải pháp**: Đây là hành vi đúng, log sẽ ghi "No next patient available"

---

## 📚 Tham khảo

- **Use Case**: `docs/UC_24_ProcessQueue.md`
- **Architecture**: `prod/KIEN_TRUC_VA_LOGIC.md`
- **Advanced Queue**: `prod/PLAN_SMART_DENTAL_BO_SUNG.md`
- **Service**: `CheckInQueueService.java`
- **Controller**: `QueueController.java`, `ReceptionController.java`

---

*Tài liệu này giải thích sự khác biệt giữa 2 chức năng "lùi" trong hệ thống quản lý hàng đợi.*
