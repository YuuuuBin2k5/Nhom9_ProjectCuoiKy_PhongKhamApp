# Sửa lỗi Firebase Crash - QueueManagementActivity

## Vấn đề
App crash khi mở `QueueManagementActivity` với lỗi:
```
java.lang.IllegalStateException: Default FirebaseApp is not initialized in this process com.hcmute.mobile_android. 
Make sure to call FirebaseApp.initializeApp(Context) first.
```

## Nguyên nhân
1. App sử dụng Firebase Realtime Database cho real-time queue updates
2. Chưa có file `google-services.json` để cấu hình Firebase
3. `FirebaseQueueManager` gọi `FirebaseDatabase.getInstance()` mà Firebase chưa được khởi tạo

## Giải pháp đã áp dụng

### 1. Sửa FirebaseQueueManager.java
Thêm try-catch để xử lý trường hợp Firebase chưa được cấu hình:

```java
public FirebaseQueueManager(Long roomId, QueueUpdateListener listener) {
    this.updateListener = listener;
    try {
        queueRef = FirebaseDatabase.getInstance()
            .getReference("clinic")
            .child("rooms")
            .child(String.valueOf(roomId))
            .child("queue");
    } catch (Exception e) {
        Log.w(TAG, "Firebase not configured, real-time updates disabled: " + e.getMessage());
        queueRef = null;
    }
}

public void startListening() {
    if (queueRef == null) {
        Log.w(TAG, "Firebase not configured, cannot start listening");
        return;
    }
    // ... rest of code
}
```

### 2. Kết quả
✅ App không còn crash
✅ QueueManagementActivity mở được bình thường
✅ Danh sách phòng khám load từ REST API
⚠️ Real-time updates bị vô hiệu hóa (cần refresh thủ công)

## Hành vi hiện tại

### Khi Firebase chưa cấu hình (hiện tại)
- App hoạt động bình thường
- Danh sách hàng đợi load từ REST API
- Cần pull-to-refresh để cập nhật
- Log: "Firebase not configured, real-time updates disabled"

### Khi Firebase đã cấu hình (tương lai)
- App hoạt động bình thường
- Danh sách hàng đợi tự động cập nhật real-time
- Không cần refresh thủ công
- Log: "FirebaseApp initialization successful"

## Các lỗi khác trong log

### 1. Lottie Animation Error (Không ảnh hưởng)
```
Unable to fetch https://lottie.host/8cd7ceb1-d3ba-4796-9800-4742f1f3a2d5/mF0T0bS1mQ.json
Failed with 403 Access Denied
```
- Lỗi này xảy ra ở lần chạy trước
- Không ảnh hưởng đến lần chạy hiện tại
- Nếu có animation loading, nên dùng file local thay vì URL

## Kiểm tra app đã hoạt động

### Test case 1: Mở AdminMainActivity
✅ Mở được
✅ Hiển thị dashboard
✅ Các nút điều hướng hoạt động

### Test case 2: Mở AdminDoctorActivity
✅ Mở được
✅ Load danh sách bác sĩ
✅ Hiển thị thông tin phòng khám

### Test case 3: Mở AdminRoomActivity
✅ Mở được
✅ Load danh sách phòng khám
✅ Hiển thị số bệnh nhân chờ
✅ Dialog thêm/sửa phòng hoạt động

### Test case 4: Mở QueueManagementActivity
✅ Mở được (đã fix)
✅ Load danh sách phòng khám
✅ Không còn crash

## Các file đã sửa
1. `mobile_android/app/src/main/java/com/hcmute/mobile_android/services/FirebaseQueueManager.java`
   - Thêm try-catch trong constructor
   - Thêm null check trong startListening()

## Các file tạo mới
1. `FIREBASE_SETUP_GUIDE.md` - Hướng dẫn cấu hình Firebase đầy đủ

## Khuyến nghị

### Ngắn hạn (Hiện tại)
✅ App đã hoạt động ổn định
✅ Không cần cấu hình Firebase ngay
✅ Sử dụng REST API + pull-to-refresh

### Dài hạn (Tùy chọn)
- Cấu hình Firebase nếu cần real-time updates
- Hoặc loại bỏ Firebase dependency nếu không dùng
- Xem hướng dẫn trong `FIREBASE_SETUP_GUIDE.md`

## Tóm tắt
🎯 Lỗi crash đã được sửa
🎯 App hoạt động bình thường
🎯 Firebase là tùy chọn, không bắt buộc
🎯 Có thể cấu hình sau nếu cần real-time features
