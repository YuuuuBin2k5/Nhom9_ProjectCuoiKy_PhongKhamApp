# Queue Estimation System - HOÀN THÀNH ✅

## Tóm Tắt

Hệ thống ước tính thời gian chờ hàng đợi đã được **hoàn thiện 100%** và sẵn sàng test.

## ✅ Đã Làm Xong

### 1. Backend Integration (100%)

#### a. CheckInQueueService.java
- ✅ Thêm `ServiceDurationTracker` và `QueueEstimationService` dependencies
- ✅ Gọi `durationTracker.markStarted()` khi bắt đầu khám (IN_PROGRESS)
- ✅ Update `QueueItemDto` record với 9 estimate fields
- ✅ Update `mapToQueueItemDto()` để calculate estimate cho mỗi queue item
- ✅ Update `getMyStatusToday()` để trả estimate cho patient

#### b. CheckInMyStatusResponse.java (Backend DTO)
- ✅ Thêm 9 estimate fields:
  - estimateDisplayType
  - estimatedMinutes, minMinutes, maxMinutes
  - estimateMessage, estimateConfidence
  - showApproximateLabel
  - estimateTitle, estimateSubtitle

### 2. Mobile Integration (100%)

#### a. CheckInMyStatusResponse.java (Mobile Model)
- ✅ Thêm 9 estimate fields với getters

#### b. PatientQueueActivity.java
- ✅ Update `updateQueueStatus()` để hiển thị estimate theo 3 loại:
  - **IN_PROGRESS**: "Đang khám"
  - **SOFT_COUNTDOWN**: "Bạn kế tiếp - ~5 phút"
  - **RANGE**: "Vị trí: #5 - 30-45 phút (Độ tin cậy: MEDIUM)"
- ✅ Fallback gracefully nếu estimate không có
- ✅ Auto-refresh mỗi 30 giây (đã có sẵn)

### 3. Compilation
- ✅ Backend compile thành công
- ✅ Mobile compile thành công

---

## 🎯 Cách Hoạt Động

### Flow Hoàn Chỉnh

```
1. Patient mở app → gọi GET /api/patient/me/checkin-status
                    ↓
2. Backend: CheckInQueueService.getMyStatusToday()
                    ↓
3. Gọi estimationService.calculateEstimate(queue)
                    ↓
4. QueueEstimationService tính toán:
   - Vị trí 0: IN_PROGRESS
   - Vị trí 1: SOFT_COUNTDOWN (~X phút)
   - Vị trí 2+: RANGE (X-Y phút)
                    ↓
5. Trả về CheckInMyStatusResponse với estimate fields
                    ↓
6. Mobile nhận response và hiển thị theo displayType
```

### Display Logic

**Position 0 (Đang khám):**
```
┌─────────────────────────────┐
│  Số: 5                      │
│  🟢 Đang khám               │
│  Bác sĩ đang khám bệnh      │
│  cho bạn                    │
└─────────────────────────────┘
```

**Position 1 (Kế tiếp):**
```
┌─────────────────────────────┐
│  Số: 6                      │
│  🟠 Bạn kế tiếp             │
│  ~5 phút                    │
│  Vui lòng ở gần phòng khám  │
└─────────────────────────────┘
```

**Position 2+ (Đang chờ):**
```
┌─────────────────────────────┐
│  Số: 7                      │
│  🔵 Vị trí: #5              │
│  30-45 phút                 │
│  Thời gian ước tính         │
│  (Độ tin cậy: MEDIUM)       │
└─────────────────────────────┘
```

---

## 📝 Những Gì Đã Thay Đổi

### Backend Files Modified (3 files)

1. **CheckInQueueService.java**
   - Thêm 2 dependencies
   - Thêm 1 dòng trong `callToRoom()`
   - Update `QueueItemDto` record
   - Update `mapToQueueItemDto()` method
   - Update `getMyStatusToday()` method

2. **CheckInMyStatusResponse.java** (DTO)
   - Thêm 9 fields

3. **CheckInQueue.java** (Entity)
   - Đã có `started_at`, `completed_at` từ trước

### Mobile Files Modified (2 files)

1. **CheckInMyStatusResponse.java** (Model)
   - Thêm 9 fields với getters

2. **PatientQueueActivity.java**
   - Update `updateQueueStatus()` method
   - Xóa `loadQueueEstimate()` (không cần nữa)

---

## 🧪 Cách Test

### Test 1: Kiểm Tra Backend API

```bash
# 1. Start backend
cd clinic_backend
./mvnw.cmd spring-boot:run

# 2. Login as patient và lấy token

# 3. Check-in tại quầy

# 4. Gọi API
curl -H "Authorization: Bearer {token}" \
  http://localhost:8080/api/patient/me/checkin-status

# Kết quả mong đợi:
{
  "checkedIn": true,
  "queueNumber": 5,
  "queuePosition": 3,
  "estimateDisplayType": "RANGE",
  "estimateTitle": "Vị trí: #3",
  "estimateSubtitle": "20-30 phút",
  "estimateMessage": "Thời gian ước tính",
  "estimateConfidence": "MEDIUM",
  ...
}
```

### Test 2: Kiểm Tra Mobile App

```bash
# 1. Build và install APK
cd mobile_android
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk

# 2. Mở app, login as patient

# 3. Check-in tại quầy (scan QR)

# 4. Vào màn hình "Trạng thái hàng đợi"

# Kết quả mong đợi:
# - Thấy số thứ tự
# - Thấy vị trí (nếu đang chờ)
# - Thấy thời gian ước tính (range hoặc soft countdown)
# - Thấy độ tin cậy (nếu có)
# - Màu card thay đổi theo trạng thái
```

### Test 3: Kiểm Tra Auto-Refresh

```
1. Mở app, xem trạng thái
2. Đợi 30 giây
3. Màn hình tự động refresh
4. Thời gian ước tính cập nhật
```

### Test 4: Kiểm Tra Edge Cases

**Case 1: Không có dữ liệu lịch sử**
- Hệ thống dùng default values (15 phút)
- Confidence = LOW
- Vẫn hiển thị estimate

**Case 2: Patient ở vị trí 1**
- Hiển thị "Bạn kế tiếp"
- Hiển thị "~X phút" (soft countdown)
- Không hiển thị số chính xác

**Case 3: Patient đang khám**
- Hiển thị "Đang khám"
- Card màu xanh
- Không hiển thị thời gian chờ

---

## 🔧 Troubleshooting

### Vấn đề: Không thấy estimate

**Nguyên nhân:**
- Backend chưa chạy migration V3
- estimationService bị lỗi

**Giải pháp:**
```bash
# Check backend logs
tail -f clinic_backend/logs/spring.log

# Tìm error liên quan đến "estimate" hoặc "QueueEstimationService"
```

### Vấn đề: Estimate luôn là default (15 phút)

**Nguyên nhân:**
- Chưa có dữ liệu lịch sử
- Cần chạy hệ thống 1-2 tuần để thu thập data

**Giải pháp:**
- Đợi thu thập dữ liệu
- Hoặc seed data thủ công vào `service_duration_history`

### Vấn đề: Mobile không hiển thị estimate

**Nguyên nhân:**
- Backend chưa trả estimate fields
- Mobile model không parse đúng

**Giải pháp:**
```bash
# Check API response
curl -H "Authorization: Bearer {token}" \
  http://localhost:8080/api/patient/me/checkin-status | jq .

# Kiểm tra có fields: estimateDisplayType, estimateTitle, etc.
```

---

## 📊 Metrics Cần Monitor

### Week 1: Data Collection
- Số lượng service_duration_history records
- Sample size per service
- Variance calculation accuracy

### Week 2-4: User Feedback
- User satisfaction với estimate
- Số lần refresh
- Accuracy của estimate (so với thực tế)

### SQL Queries Hữu Ích

```sql
-- Check data collection
SELECT 
    s.name,
    COUNT(*) as samples,
    AVG(actual_duration_minutes) as avg_actual,
    STDDEV(actual_duration_minutes) as std_dev
FROM service_duration_history h
JOIN services s ON h.service_id = s.id
WHERE h.completed_at >= NOW() - INTERVAL '7 days'
GROUP BY s.id, s.name;

-- Check estimate accuracy
SELECT 
    q.id,
    q.queue_number,
    s.name as service,
    TIMESTAMPDIFF(MINUTE, q.started_at, q.completed_at) as actual_duration,
    s.duration_minutes as scheduled_duration
FROM check_in_queue q
JOIN appointments a ON q.appointment_id = a.id
JOIN services s ON a.service_id = s.id
WHERE q.started_at IS NOT NULL 
  AND q.completed_at IS NOT NULL
  AND DATE(q.check_in_time) = CURDATE()
ORDER BY q.completed_at DESC;
```

---

## ✅ Checklist Trước Khi Deploy Production

- [ ] Backend compile OK ✅
- [ ] Mobile compile OK ✅
- [ ] Migration V3 đã chạy
- [ ] Test API trả estimate đúng
- [ ] Test mobile hiển thị đúng
- [ ] Test auto-refresh hoạt động
- [ ] Test edge cases (no data, position 1, in progress)
- [ ] Monitor logs không có error
- [ ] Backup database trước khi deploy

---

## 🎉 Kết Luận

**Hệ thống đã hoàn thiện 100%!**

- ✅ Backend integration xong
- ✅ Mobile UI xong
- ✅ Compile thành công
- ✅ Sẵn sàng test

**Bước tiếp theo:**
1. Chạy backend
2. Build và install mobile app
3. Test end-to-end
4. Thu thập feedback
5. Deploy production

**Thời gian ước tính để có dữ liệu tốt:** 1-2 tuần

---

**Tạo bởi:** Kiro AI Assistant
**Ngày:** 31/03/2026
**Status:** ✅ COMPLETE & READY FOR TESTING
