# ✅ Tính năng "Lùi 1 người" - Hoàn thành

## 📋 Tổng quan

Đã implement thành công tính năng "Lùi 1 người" (Skip Patient) cho phép bác sĩ tạm dừng khám bệnh nhân hiện tại và gọi người tiếp theo vào phòng.

## 🎯 Use Case

**Tình huống:** Bác sĩ đang khám bệnh nhân A (IN_PROGRESS), nhưng cần thêm thời gian:
- Chờ thuốc tê có hiệu lực
- Chờ kết quả X-quang
- Bệnh nhân cần chuẩn bị thêm
- Cần tư vấn với bác sĩ khác

**Giải pháp:** Nhấn nút "Lùi 1 người"
1. Bệnh nhân A → WAITING với priority +5
2. Bệnh nhân B (người tiếp theo) → IN_PROGRESS (tự động gọi vào)
3. Sau khi khám xong B, bác sĩ gọi A trở lại (A có priority cao hơn)

## 🔧 Implementation Details

### Backend Changes

#### 1. CheckInQueueService.java
**File:** `clinic_backend/src/main/java/com/hcmute/clinic/service/CheckInQueueService.java`

**Thêm method mới:**
```java
@Transactional
public void skipCurrentPatient(Long queueId) {
    // 1. Validate: Only IN_PROGRESS can be skipped
    // 2. Mark duration tracking as completed
    // 3. Move current patient to WAITING with +5 priority
    // 4. Find next patient in queue
    // 5. Call next patient to IN_PROGRESS
    // 6. Broadcast updates
}

private CheckInQueue findNextWaitingPatient(Long roomId) {
    // Sort by priority DESC, then queueNumber ASC
    // Return highest priority patient
}
```

**Logic chi tiết:**
- Validate: Chỉ bệnh nhân IN_PROGRESS mới có thể skip
- Priority system: +5 cho mỗi lần skip (có thể tích lũy)
- Auto-call next patient: Tự động gọi người tiếp theo vào phòng
- Firebase broadcast: Cập nhật real-time cho tất cả clients

#### 2. ReceptionController.java
**File:** `clinic_backend/src/main/java/com/hcmute/clinic/controller/ReceptionController.java`

**Thêm endpoint:**
```java
@PostMapping("/queue/{queueId}/skip")
public ResponseEntity<?> skipPatient(@PathVariable Long queueId) {
    checkInQueueService.skipCurrentPatient(queueId);
    return ResponseEntity.ok(Map.of(
        "success", true,
        "message", "Đã lùi bệnh nhân và gọi người tiếp theo"
    ));
}
```

**Build status:** ✅ SUCCESS
```bash
mvn clean compile -DskipTests
[INFO] BUILD SUCCESS
```

### Mobile Changes

#### 1. ApiService.java
**File:** `mobile_android/app/src/main/java/com/hcmute/mobile_android/network/ApiService.java`

**Thêm API method:**
```java
@POST("api/reception/queue/{id}/skip")
Call<Void> skipPatient(@Path("id") Long queueId);
```

#### 2. QueueAdapter.java
**File:** `mobile_android/app/src/main/java/com/hcmute/mobile_android/adapters/QueueAdapter.java`

**Thêm interface method:**
```java
public interface OnQueueActionListener {
    void onCallPatient(QueueItem item);
    void onExaminePatient(QueueItem item);
    void onTransferToXRay(QueueItem item);
    void onSkipPatient(QueueItem item);  // NEW
    void onCompletePatient(QueueItem item);
}
```

**Thêm button field:**
```java
private MaterialButton btnSkip;
```

**Update configureButtons():**
```java
case "IN_PROGRESS":
    btnExamine.setVisibility(View.VISIBLE);
    btnXRay.setVisibility(View.VISIBLE);
    btnSkip.setVisibility(View.VISIBLE);  // NEW
    btnComplete.setVisibility(View.VISIBLE);
    
    btnSkip.setOnClickListener(v -> listener.onSkipPatient(item));
    break;
```

#### 3. item_queue.xml
**File:** `mobile_android/app/src/main/res/layout/item_queue.xml`

**Thêm button:**
```xml
<com.google.android.material.button.MaterialButton
    android:id="@+id/btnSkip"
    style="@style/Widget.Material3.Button.OutlinedButton"
    android:layout_width="wrap_content"
    android:layout_height="36dp"
    android:text="Lùi 1 người"
    android:textSize="12sp"
    android:layout_marginEnd="8dp"
    app:strokeColor="@color/warning_amber"
    app:iconTint="@color/warning_amber"
    android:textColor="@color/warning_amber"
    app:icon="@drawable/ic_skip_next"
    app:iconSize="16dp"
    android:visibility="gone" />
```

**Button style:**
- OutlinedButton (không fill background)
- Warning amber color (để phân biệt với các action khác)
- Icon: skip_next (Material Icons)
- Position: Giữa "Chụp XQ" và "Hoàn thành"

#### 4. ic_skip_next.xml
**File:** `mobile_android/app/src/main/res/drawable/ic_skip_next.xml`

**Icon mới:** Material Design skip_next icon

#### 5. QueueManagementActivity.java
**File:** `mobile_android/app/src/main/java/com/hcmute/mobile_android/ui/activities/staff/QueueManagementActivity.java`

**Implement onSkipPatient():**
```java
@Override
public void onSkipPatient(QueueItem item) {
    // Show confirmation dialog
    new AlertDialog.Builder(this)
        .setTitle("Lùi 1 người")
        .setMessage("Bệnh nhân " + item.getPatientName() + " sẽ quay lại hàng đợi...")
        .setPositiveButton("Xác nhận", (dialog, which) -> {
            // Call API
            apiService.skipPatient(item.getId()).enqueue(...);
        })
        .setNegativeButton("Hủy", null)
        .show();
}
```

**Features:**
- Confirmation dialog trước khi skip
- Loading indicator khi đang xử lý
- Success/Error toast messages
- Auto-refresh queue sau khi skip thành công

## 📊 Priority System

### Hiện tại
```
Default: 0
Skip (lùi 1 người): +5
X-Ray return: +10
```

### Ví dụ tích lũy
```
Bệnh nhân A: priority = 0
→ Skip lần 1: priority = 5
→ Skip lần 2: priority = 10
→ Đi X-Ray và quay lại: priority = 20 (10 + 10)
```

### Sorting Logic
```java
// Sort by priority DESC, then queueNumber ASC
waiting.sort((a, b) -> {
    int priorityCompare = Integer.compare(priorityB, priorityA);
    if (priorityCompare != 0) return priorityCompare;
    return Integer.compare(a.getQueueNumber(), b.getQueueNumber());
});
```

## 🔄 Workflow Diagram

```
TRƯỚC KHI LÙI:
┌─────────────────────────────────────┐
│ Bệnh nhân A: IN_PROGRESS (đang khám)│
│ Bệnh nhân B: WAITING (vị trí #1)    │
│ Bệnh nhân C: WAITING (vị trí #2)    │
└─────────────────────────────────────┘

        [Nhấn "Lùi 1 người"]
                 ↓

SAU KHI LÙI:
┌─────────────────────────────────────┐
│ Bệnh nhân B: IN_PROGRESS (được gọi) │
│ Bệnh nhân A: WAITING (priority +5)  │
│ Bệnh nhân C: WAITING (vị trí #1)    │
└─────────────────────────────────────┘
```

## 🎨 UI/UX Design

### Button Appearance
- **Style:** OutlinedButton (viền, không fill)
- **Color:** Warning Amber (#FFA726)
- **Icon:** skip_next (Material Icons)
- **Text:** "Lùi 1 người"
- **Size:** 36dp height, wrap_content width

### Confirmation Dialog
```
┌─────────────────────────────────────┐
│ Lùi 1 người                         │
├─────────────────────────────────────┤
│ Bệnh nhân Nguyễn Văn A sẽ quay lại │
│ hàng đợi với độ ưu tiên cao.       │
│                                     │
│ Người tiếp theo sẽ được gọi vào    │
│ phòng.                              │
│                                     │
│ Xác nhận?                           │
├─────────────────────────────────────┤
│           [Hủy]    [Xác nhận]      │
└─────────────────────────────────────┘
```

### Toast Messages
- **Success:** "Đã lùi [Tên BN] và gọi người tiếp theo"
- **No next patient:** "Đã lùi [Tên BN]. Không có bệnh nhân tiếp theo."
- **Error:** "Lỗi: [Chi tiết lỗi]"

## ✅ Testing Checklist

### Backend Tests
- [x] Build successful: `mvn clean compile -DskipTests`
- [ ] Test skip với 1 người IN_PROGRESS, 2 người WAITING
- [ ] Test skip khi chỉ có 1 người IN_PROGRESS (không có người tiếp theo)
- [ ] Test skip nhiều lần liên tiếp (priority tích lũy)
- [ ] Test skip + X-Ray return (priority = 5 + 10 = 15)
- [ ] Test API endpoint với Postman: `POST /api/reception/queue/{id}/skip`

### Mobile Tests
- [ ] Build APK thành công
- [ ] Button "Lùi 1 người" hiển thị khi status = IN_PROGRESS
- [ ] Button KHÔNG hiển thị khi status = WAITING/COMPLETED/PAUSED_FOR_TEST
- [ ] Confirmation dialog hiển thị đúng
- [ ] API call thành công
- [ ] Toast message hiển thị đúng
- [ ] Queue refresh tự động sau khi skip
- [ ] Firebase real-time update hoạt động
- [ ] Test trên thiết bị thực

### Integration Tests
- [ ] Skip → Next patient auto-called → Firebase broadcast
- [ ] Skip → No next patient → Only current patient moved to WAITING
- [ ] Skip multiple times → Priority accumulates correctly
- [ ] Skip + X-Ray return → Priority = 15 (5 + 10)
- [ ] UI updates correctly after skip

## 📝 Files Changed

### Backend (2 files)
1. `clinic_backend/src/main/java/com/hcmute/clinic/service/CheckInQueueService.java`
   - Added `skipCurrentPatient()` method
   - Added `findNextWaitingPatient()` helper method

2. `clinic_backend/src/main/java/com/hcmute/clinic/controller/ReceptionController.java`
   - Added `POST /api/reception/queue/{id}/skip` endpoint

### Mobile (5 files)
1. `mobile_android/app/src/main/java/com/hcmute/mobile_android/network/ApiService.java`
   - Added `skipPatient()` API method

2. `mobile_android/app/src/main/java/com/hcmute/mobile_android/adapters/QueueAdapter.java`
   - Added `onSkipPatient()` to interface
   - Added `btnSkip` field
   - Updated `configureButtons()` method

3. `mobile_android/app/src/main/res/layout/item_queue.xml`
   - Added `btnSkip` button

4. `mobile_android/app/src/main/res/drawable/ic_skip_next.xml`
   - Created new icon

5. `mobile_android/app/src/main/java/com/hcmute/mobile_android/ui/activities/staff/QueueManagementActivity.java`
   - Implemented `onSkipPatient()` method

## 🚀 Deployment Steps

### 1. Backend Deployment
```bash
cd clinic_backend
mvn clean package -DskipTests
# Deploy JAR to server
# Restart backend service
```

### 2. Mobile Deployment
```bash
cd mobile_android
./gradlew assembleDebug
# Install APK: app/build/outputs/apk/debug/app-debug.apk
# Or build release APK for production
```

### 3. Testing
1. Start backend server
2. Install mobile APK
3. Login as receptionist/doctor
4. Go to Queue Management
5. Call a patient (status → IN_PROGRESS)
6. Click "Lùi 1 người" button
7. Verify confirmation dialog
8. Confirm skip
9. Verify patient moved to WAITING with priority +5
10. Verify next patient auto-called to IN_PROGRESS
11. Verify Firebase real-time update

## 📚 Documentation

### API Documentation
```
POST /api/reception/queue/{queueId}/skip

Description: Skip current patient and call next patient
Authorization: Required (RECEPTIONIST role)

Path Parameters:
- queueId: Long (required) - ID of queue item to skip

Response:
{
  "success": true,
  "message": "Đã lùi bệnh nhân và gọi người tiếp theo"
}

Error Responses:
- 404: Queue not found
- 400: Patient not IN_PROGRESS
- 500: Internal server error
```

### User Manual
**Hướng dẫn sử dụng tính năng "Lùi 1 người":**

1. Khi bệnh nhân đang khám (IN_PROGRESS), nút "Lùi 1 người" sẽ hiển thị
2. Nhấn nút "Lùi 1 người" khi cần tạm dừng khám bệnh nhân hiện tại
3. Xác nhận trong dialog
4. Bệnh nhân hiện tại sẽ quay lại hàng đợi với độ ưu tiên cao
5. Người tiếp theo sẽ tự động được gọi vào phòng
6. Sau khi khám xong người tiếp theo, gọi lại bệnh nhân đã lùi (họ sẽ ở đầu hàng đợi)

## 🎉 Summary

Đã hoàn thành implementation tính năng "Lùi 1 người" với đầy đủ:
- ✅ Backend API endpoint
- ✅ Mobile UI button
- ✅ Confirmation dialog
- ✅ Priority system (+5 per skip)
- ✅ Auto-call next patient
- ✅ Firebase real-time update
- ✅ Error handling
- ✅ Build successful

**Next steps:**
1. Test trên thiết bị thực
2. Deploy lên staging environment
3. User acceptance testing
4. Deploy lên production
5. Training cho nhân viên

## 📞 Support

Nếu có vấn đề khi testing hoặc deployment, liên hệ team để được hỗ trợ.
