# Phân tích tính năng "Lùi 1 người" (Skip Patient)

## 📋 Yêu cầu nghiệp vụ

### Use Case
Khi bác sĩ đang khám bệnh nhân A (status = IN_PROGRESS), nhưng cần thêm thời gian (chờ thuốc tê, chờ kết quả X-quang, bệnh nhân cần chuẩn bị thêm...), bác sĩ có thể nhấn nút "Lùi 1 người" để:

1. Bệnh nhân A quay về trạng thái WAITING với độ ưu tiên cao
2. Bệnh nhân B (người tiếp theo trong hàng đợi) được gọi vào phòng (IN_PROGRESS)
3. Sau khi khám xong B, bác sĩ sẽ gọi A trở lại (vì A có priority cao)

### Workflow
```
TRƯỚC KHI LÙI:
- Bệnh nhân A: IN_PROGRESS (đang khám)
- Bệnh nhân B: WAITING (vị trí #1)
- Bệnh nhân C: WAITING (vị trí #2)

SAU KHI LÙI:
- Bệnh nhân A: WAITING với priority cao (sẽ được ưu tiên gọi sau)
- Bệnh nhân B: IN_PROGRESS (được gọi vào ngay)
- Bệnh nhân C: WAITING (vị trí #1)
```

## 🔍 Phân tích hiện trạng

### 1. Backend đã có method `delayPatient()`
File: `CheckInQueueService.java` (lines ~750-790)

**Chức năng hiện tại:**
- Chỉ hoán đổi `queueNumber` giữa 2 bệnh nhân
- KHÔNG thay đổi status
- KHÔNG tự động gọi người tiếp theo

**Vấn đề:**
- Method này chỉ dùng cho bệnh nhân đang WAITING/RETURNED_PRIORITY
- KHÔNG xử lý được trường hợp bệnh nhân đang IN_PROGRESS
- KHÔNG tự động call người tiếp theo vào phòng

### 2. Frontend chưa có UI
- `QueueManagementActivity.java`: Chưa có nút "Lùi 1 người"
- `QueueAdapter.java`: Chưa có button trong item layout
- `activity_queue_management.xml`: Chưa có UI element

### 3. API endpoint chưa có
- `ReceptionController.java`: Chưa có endpoint `/api/reception/queue/{queueId}/skip`

## 🎯 Giải pháp thiết kế

### Backend API Design

#### Endpoint
```
POST /api/reception/queue/{queueId}/skip
```

#### Logic Flow
```java
@Transactional
public void skipCurrentPatient(Long queueId) {
    // 1. Validate: Bệnh nhân phải đang IN_PROGRESS
    CheckInQueue current = findById(queueId);
    if (current.getStatus() != QueueStatus.IN_PROGRESS) {
        throw new Exception("Chỉ có thể lùi bệnh nhân đang khám");
    }
    
    // 2. Đánh dấu thời gian kết thúc (để tracking duration)
    durationTracker.markCompleted(current);
    
    // 3. Chuyển bệnh nhân hiện tại về WAITING với priority cao
    current.setStatus(QueueStatus.WAITING);
    current.setPriorityLevel(current.getPriorityLevel() + 5); // +5 cho "skipped"
    save(current);
    
    // 4. Tìm bệnh nhân tiếp theo trong hàng đợi
    CheckInQueue next = findNextWaitingPatient(current.getClinicRoom().getId());
    
    // 5. Gọi bệnh nhân tiếp theo vào phòng (nếu có)
    if (next != null) {
        next.setStatus(QueueStatus.IN_PROGRESS);
        durationTracker.markStarted(next);
        save(next);
        
        // Broadcast notification
        queueEventService.broadcastQueueCalled(
            next.getClinicRoom().getId(), 
            next.getQueueNumber(), 
            next.getClinicRoom().getName()
        );
    }
    
    // 6. Broadcast queue update
    queueEventService.broadcastQueueUpdated(current.getClinicRoom().getId());
}
```

#### Helper Method
```java
private CheckInQueue findNextWaitingPatient(Long roomId) {
    LocalDate today = LocalDate.now();
    List<CheckInQueue> waiting = checkInQueueRepository.findByRoomAndDateRange(
        roomId,
        today.atStartOfDay(),
        today.plusDays(1).atStartOfDay(),
        List.of(QueueStatus.WAITING, QueueStatus.RETURNED_PRIORITY)
    );
    
    // Sort by priority DESC, then queueNumber ASC
    waiting.sort((a, b) -> {
        int priorityCompare = Integer.compare(
            b.getPriorityLevel() != null ? b.getPriorityLevel() : 0,
            a.getPriorityLevel() != null ? a.getPriorityLevel() : 0
        );
        if (priorityCompare != 0) return priorityCompare;
        return Integer.compare(a.getQueueNumber(), b.getQueueNumber());
    });
    
    return waiting.isEmpty() ? null : waiting.get(0);
}
```

### Frontend UI Design

#### 1. Thêm button vào QueueAdapter
File: `item_queue.xml`

```xml
<com.google.android.material.button.MaterialButton
    android:id="@+id/btnSkip"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Lùi 1 người"
    app:icon="@drawable/ic_skip_next"
    style="@style/Widget.Material3.Button.OutlinedButton" />
```

#### 2. Hiển thị button khi status = IN_PROGRESS
File: `QueueAdapter.java`

```java
private void configureButtons(QueueItem item, OnQueueActionListener listener) {
    // ... existing code ...
    
    case "IN_PROGRESS":
        btnExamine.setVisibility(View.VISIBLE);
        btnXRay.setVisibility(View.VISIBLE);
        btnSkip.setVisibility(View.VISIBLE);  // NEW
        btnComplete.setVisibility(View.VISIBLE);
        
        btnExamine.setOnClickListener(v -> listener.onExaminePatient(item));
        btnXRay.setOnClickListener(v -> listener.onTransferToXRay(item));
        btnSkip.setOnClickListener(v -> listener.onSkipPatient(item));  // NEW
        btnComplete.setOnClickListener(v -> listener.onCompletePatient(item));
        break;
}
```

#### 3. Thêm interface method
```java
public interface OnQueueActionListener {
    void onCallPatient(QueueItem item);
    void onExaminePatient(QueueItem item);
    void onTransferToXRay(QueueItem item);
    void onSkipPatient(QueueItem item);  // NEW
    void onCompletePatient(QueueItem item);
}
```

#### 4. Implement trong QueueManagementActivity
```java
@Override
public void onSkipPatient(QueueItem item) {
    // Show confirmation dialog
    new AlertDialog.Builder(this)
        .setTitle("Lùi 1 người")
        .setMessage("Bệnh nhân " + item.getPatientName() + " sẽ quay lại hàng đợi với độ ưu tiên cao. Người tiếp theo sẽ được gọi vào. Xác nhận?")
        .setPositiveButton("Xác nhận", (dialog, which) -> {
            showLoading(true, "Đang xử lý...");
            apiService.skipPatient(item.getId()).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    showLoading(false);
                    if (response.isSuccessful()) {
                        showSuccess("Đã lùi " + item.getPatientName() + " và gọi người tiếp theo");
                        loadQueue();
                    } else {
                        showError("Lỗi: " + response.code());
                    }
                }
                
                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    showLoading(false);
                    showError("Lỗi kết nối: " + t.getMessage());
                }
            });
        })
        .setNegativeButton("Hủy", null)
        .show();
}
```

## 📊 Priority Level System

### Hiện tại
- Default: 0
- X-Ray return: +10
- Room transfer: +5 (nếu có)

### Sau khi thêm Skip
- Default: 0
- Skip (lùi 1 người): +5
- X-Ray return: +10
- Multiple skips: +5 mỗi lần (có thể tích lũy)

### Ví dụ
```
Bệnh nhân A: priority = 0
→ Skip lần 1: priority = 5
→ Skip lần 2: priority = 10
→ Đi X-Ray và quay lại: priority = 20 (10 + 10)
```

## 🔄 Sequence Diagram

```
User (Bác sĩ)          Mobile App          Backend                    Firebase
     |                      |                  |                          |
     |--[Nhấn "Lùi 1 người"]->|                  |                          |
     |                      |--POST /skip------>|                          |
     |                      |                  |--[Validate IN_PROGRESS]  |
     |                      |                  |--[Set A: WAITING +5]     |
     |                      |                  |--[Find next: B]          |
     |                      |                  |--[Set B: IN_PROGRESS]    |
     |                      |                  |--[Broadcast update]----->|
     |                      |<--Success---------|                          |
     |<--[Toast: Success]---|                  |                          |
     |                      |<--[Firebase event]-------------------------|
     |                      |--[Reload queue]-->|                          |
```

## ✅ Checklist Implementation

### Backend
- [ ] Thêm method `skipCurrentPatient()` vào `CheckInQueueService`
- [ ] Thêm helper method `findNextWaitingPatient()`
- [ ] Thêm endpoint `POST /api/reception/queue/{queueId}/skip` vào `ReceptionController`
- [ ] Test với Postman/curl
- [ ] Build backend: `mvn clean compile -DskipTests`

### Mobile
- [ ] Thêm button "Lùi 1 người" vào `item_queue.xml`
- [ ] Thêm icon `ic_skip_next.xml`
- [ ] Thêm method `onSkipPatient()` vào interface `OnQueueActionListener`
- [ ] Implement `onSkipPatient()` trong `QueueManagementActivity`
- [ ] Thêm API method `skipPatient()` vào `ApiService`
- [ ] Update `configureButtons()` trong `QueueAdapter`
- [ ] Test trên thiết bị thực

### Testing Scenarios
- [ ] Test skip khi có 1 người đang IN_PROGRESS, 2 người WAITING
- [ ] Test skip khi chỉ có 1 người IN_PROGRESS (không có người tiếp theo)
- [ ] Test skip nhiều lần liên tiếp (priority tích lũy)
- [ ] Test skip + X-Ray return (priority = 5 + 10 = 15)
- [ ] Test Firebase real-time update
- [ ] Test UI refresh sau khi skip

## 🎨 UI/UX Considerations

### Button Style
- Icon: `skip_next` (Material Icons)
- Color: Warning/Amber (để phân biệt với các action khác)
- Position: Giữa "X-Quang" và "Hoàn thành"

### Confirmation Dialog
- Title: "Lùi 1 người"
- Message: Hiển thị tên bệnh nhân và giải thích hành động
- Buttons: "Xác nhận" (primary) và "Hủy" (secondary)

### Toast Messages
- Success: "Đã lùi [Tên BN] và gọi [Tên BN tiếp theo]"
- No next patient: "Đã lùi [Tên BN]. Không có bệnh nhân tiếp theo."
- Error: "Lỗi: [Chi tiết lỗi]"

## 🚀 Deployment Notes

1. Backend phải deploy trước (để có API endpoint)
2. Mobile app cần rebuild và cài APK mới
3. Test trên môi trường staging trước khi production
4. Thông báo cho nhân viên về tính năng mới

## 📝 Documentation Updates

- [ ] Update API documentation
- [ ] Update user manual
- [ ] Create training video for staff
- [ ] Update CHANGELOG.md
