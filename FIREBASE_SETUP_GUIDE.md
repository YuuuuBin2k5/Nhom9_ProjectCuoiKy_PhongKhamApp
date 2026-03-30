# Hướng dẫn cấu hình Firebase cho Real-time Queue Updates

## Vấn đề hiện tại
App đang crash với lỗi:
```
java.lang.IllegalStateException: Default FirebaseApp is not initialized
```

## Giải pháp tạm thời (Đã áp dụng)
✅ Đã sửa `FirebaseQueueManager` để không crash khi Firebase chưa được cấu hình
- App vẫn hoạt động bình thường
- Real-time queue updates sẽ bị vô hiệu hóa
- Cần refresh thủ công để cập nhật danh sách hàng đợi

## Cách cấu hình Firebase đầy đủ (Tùy chọn)

### Bước 1: Tạo Firebase Project
1. Truy cập https://console.firebase.google.com/
2. Tạo project mới hoặc chọn project có sẵn
3. Thêm Android app với package name: `com.hcmute.mobile_android`

### Bước 2: Download google-services.json
1. Trong Firebase Console, vào Project Settings
2. Chọn Android app vừa tạo
3. Download file `google-services.json`
4. Copy file vào: `mobile_android/app/google-services.json`

### Bước 3: Thêm Google Services plugin
Thêm vào `mobile_android/app/build.gradle.kts`:

```kotlin
plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services") // Thêm dòng này
}
```

Thêm vào `mobile_android/build.gradle.kts`:

```kotlin
buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.4.0")
    }
}
```

### Bước 4: Enable Realtime Database
1. Trong Firebase Console, vào Realtime Database
2. Click "Create Database"
3. Chọn location (asia-southeast1 cho VN)
4. Chọn "Start in test mode" (hoặc cấu hình rules phù hợp)

### Bước 5: Cấu hình Database Rules
```json
{
  "rules": {
    "clinic": {
      "rooms": {
        "$roomId": {
          "queue": {
            ".read": true,
            ".write": true
          }
        }
      }
    }
  }
}
```

### Bước 6: Rebuild app
```bash
cd mobile_android
./gradlew clean assembleDebug
```

## Cấu trúc dữ liệu Firebase

```
clinic/
  rooms/
    1/  (roomId)
      queue/
        - patientId: 123
          patientName: "Nguyễn Văn A"
          queueNumber: 1
          status: "WAITING"
          checkInTime: "2026-03-30T10:00:00"
        - patientId: 124
          patientName: "Trần Thị B"
          queueNumber: 2
          status: "WAITING"
          checkInTime: "2026-03-30T10:05:00"
```

## Lợi ích khi cấu hình Firebase
- ✅ Real-time updates: Danh sách hàng đợi tự động cập nhật
- ✅ Multi-device sync: Nhiều thiết bị thấy cùng dữ liệu
- ✅ Offline support: Firebase cache dữ liệu khi mất mạng

## Lưu ý
- Firebase là tùy chọn, không bắt buộc
- App vẫn hoạt động tốt với REST API
- Chỉ cần Firebase nếu muốn real-time updates
- File `google-services.json` không được commit vào git (đã thêm vào .gitignore)

## Kiểm tra Firebase đã hoạt động
Sau khi cấu hình, kiểm tra log:
```
adb logcat | grep Firebase
```

Nếu thấy:
```
FirebaseApp initialization successful
```
→ Firebase đã hoạt động ✅

Nếu thấy:
```
Firebase not configured, real-time updates disabled
```
→ App vẫn hoạt động nhưng không có real-time ⚠️
