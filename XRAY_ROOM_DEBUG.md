# DEBUG: PHÒNG X-QUANG TRỐNG

## 🐛 VẤN ĐỀ

Phòng X-quang hiện đang trống, không có bệnh nhân nào trong queue.

## 🔍 PHÂN TÍCH

### Query Logic:
```java
// CheckInQueueRepository.findTodayByClinicRoomId()
SELECT q FROM CheckInQueue q
WHERE q.clinicRoom.id = :roomId  // Tìm theo phòng hiện tại
AND q.checkInTime >= :start
AND q.checkInTime < :end
AND q.status IN (WAITING, IN_PROGRESS, PAUSED_FOR_TEST, RETURNED_PRIORITY)
ORDER BY q.priorityLevel DESC, q.queueNumber ASC
```

### Workflow chuyển phòng:
```java
// TreatmentPlanService.completeStepAndAdvance()
if (nextRoom != null && !nextRoom.getId().equals(activeQueue.getClinicRoom().getId())) {
    // Lưu phòng gốc
    if (activeQueue.getOriginalRoomId() == null) {
        activeQueue.setOriginalRoomId(oldRoomId);
    }
    
    // Chuyển sang phòng mới
    activeQueue.setClinicRoom(nextRoom);  // ✅ Cập nhật clinic_room_id
    activeQueue.setStatus(QueueStatus.WAITING);
    activeQueue.setPriorityLevel(+5);
    queueRepo.save(activeQueue);
}
```

## 🎯 CÁC TRƯỜNG HỢP CÓ THỂ XẢY RA

### Trường hợp 1: Chưa có bệnh nhân nào được chuyển đến
- ✅ Logic đúng: Phòng X-quang trống vì chưa có ai hoàn thành bước khám
- **Giải pháp**: Cần test workflow từ đầu

### Trường hợp 2: Bệnh nhân đã được chuyển nhưng không hiển thị
- ❌ Bug: Queue đã được update nhưng query không tìm thấy
- **Nguyên nhân có thể**:
  - Status không đúng (không phải WAITING/IN_PROGRESS)
  - checkInTime không trong khoảng hôm nay
  - clinicRoom.id không khớp với phòng X-quang

### Trường hợp 3: Bước X-quang chưa được kích hoạt
- ❌ Bug: nextStep.setStatus(IN_PROGRESS) không được gọi
- **Nguyên nhân**: Logic trong completeStepAndAdvance() có vấn đề

## 🧪 CÁCH KIỂM TRA

### Bước 1: Kiểm tra có bệnh nhân nào trong hệ thống không
```sql
-- Xem tất cả queue hôm nay
SELECT * FROM check_in_queue 
WHERE check_in_time >= CURDATE() 
ORDER BY id DESC;
```

### Bước 2: Kiểm tra treatment plan steps
```sql
-- Xem các bước điều trị đang IN_PROGRESS
SELECT 
    tps.id,
    tps.status,
    s.name as service_name,
    cr.name as room_name,
    p.first_name, p.last_name
FROM treatment_plan_steps tps
JOIN services s ON s.id = tps.service_id
LEFT JOIN clinic_rooms cr ON cr.id = tps.clinic_room_id
JOIN treatment_plans tp ON tp.id = tps.plan_id
JOIN patients p ON p.id = tp.patient_id
WHERE tps.status = 'IN_PROGRESS'
ORDER BY tps.id DESC;
```

### Bước 3: Kiểm tra phòng X-quang ID
```sql
-- Lấy ID phòng X-quang
SELECT id, name FROM clinic_rooms WHERE name LIKE '%X-quang%';
```

### Bước 4: Test workflow hoàn chỉnh

**Scenario: Bệnh nhân làm phác đồ "Nhổ răng khôn"**

1. **Login bác sĩ Phòng 01** (`doc01@gmail.com` / `password123`)
2. **Scan QR bệnh nhân** (patient01, patient02, hoặc patient03)
3. **Tạo treatment plan** từ template "Phác đồ Nhổ răng khôn"
   - Bước 1: Khám và tư vấn (Phòng khám 01) - IN_PROGRESS
   - Bước 2: Chụp X-quang (Phòng X-quang) - PENDING
   - Bước 3: Nhổ răng khôn (Phòng tiểu phẫu) - PENDING
4. **Hoàn thành bước 1** (Khám và tư vấn)
   - Nhấn "Hoàn thành"
   - Expected: 
     - Bước 1 → COMPLETED
     - Bước 2 → IN_PROGRESS
     - Queue chuyển từ Phòng 01 → Phòng X-quang
     - Notification gửi cho bệnh nhân
5. **Login bác sĩ X-quang** (`doc_xray@gmail.com` / `password123`)
6. **Kiểm tra queue Phòng X-quang**
   - Expected: Thấy bệnh nhân trong danh sách WAITING
   - Priority level = 5 (đã tăng)

## 🔧 DEBUG STEPS

### Step 1: Kiểm tra DataSeed
```bash
# Restart backend để chạy lại DataSeed
# Kiểm tra log xem có tạo được:
# - 3 patients
# - 6 doctors (bao gồm doc_xray)
# - 4 treatment plan templates
# - 3 queue entries ban đầu
```

### Step 2: Kiểm tra database
```sql
-- Đếm số phòng
SELECT COUNT(*) FROM clinic_rooms;  -- Expected: 6

-- Đếm số bác sĩ
SELECT COUNT(*) FROM doctors;  -- Expected: 6

-- Đếm số bệnh nhân
SELECT COUNT(*) FROM patients;  -- Expected: 3

-- Đếm số queue hôm nay
SELECT COUNT(*) FROM check_in_queue 
WHERE check_in_time >= CURDATE();  -- Expected: >= 3
```

### Step 3: Test API trực tiếp
```bash
# Login bác sĩ X-quang
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"doc_xray@gmail.com","password":"password123"}'

# Lấy queue phòng X-quang (thay {roomId} bằng ID thực tế)
curl -X GET http://localhost:8080/api/queue/room/{roomId} \
  -H "Authorization: Bearer {token}"
```

## 💡 GIẢI PHÁP TẠM THỜI

Nếu phòng X-quang trống vì chưa có bệnh nhân được chuyển đến:

### Option 1: Tạo queue test trực tiếp
```sql
-- Lấy ID phòng X-quang
SELECT @xray_room_id := id FROM clinic_rooms WHERE name LIKE '%X-quang%';

-- Lấy ID bệnh nhân
SELECT @patient_id := id FROM patients LIMIT 1;

-- Lấy ID bác sĩ X-quang
SELECT @doctor_id := id FROM doctors WHERE email = 'doc_xray@gmail.com';

-- Lấy ID dịch vụ X-quang
SELECT @service_id := id FROM services WHERE name LIKE '%X-quang%';

-- Tạo appointment
INSERT INTO appointments (patient_id, doctor_id, service_id, appointment_datetime, status, booking_type)
VALUES (@patient_id, @doctor_id, @service_id, NOW(), 'SCHEDULED', 'WALK_IN');

SET @appointment_id = LAST_INSERT_ID();

-- Tạo queue entry
INSERT INTO check_in_queue (appointment_id, clinic_room_id, queue_number, check_in_time, status, priority_level)
VALUES (@appointment_id, @xray_room_id, 1, NOW(), 'WAITING', 5);
```

### Option 2: Test workflow đầy đủ
1. Login bác sĩ Phòng 01
2. Tạo treatment plan cho bệnh nhân
3. Hoàn thành bước khám
4. Kiểm tra log backend xem có chuyển phòng không
5. Login bác sĩ X-quang và kiểm tra queue

## 📊 EXPECTED BEHAVIOR

Sau khi bác sĩ Phòng 01 hoàn thành bước khám:

1. **Backend log:**
   ```
   Hibernate: UPDATE treatment_plan_steps SET status='COMPLETED' WHERE id=?
   Hibernate: UPDATE treatment_plan_steps SET status='IN_PROGRESS' WHERE id=?
   Hibernate: UPDATE check_in_queue SET clinic_room_id=?, status='WAITING', priority_level=? WHERE id=?
   Hibernate: INSERT INTO notifications ...
   ```

2. **Database state:**
   ```sql
   -- Step 1: COMPLETED
   -- Step 2: IN_PROGRESS
   -- Queue: clinic_room_id = X-quang room ID, status = WAITING
   ```

3. **Mobile UI:**
   - Bác sĩ Phòng 01: Dialog "Chuyển phòng" → "Phòng X-quang"
   - Bác sĩ X-quang: Queue có 1 bệnh nhân WAITING

## ✅ CHECKLIST

- [ ] Kiểm tra DataSeed đã chạy thành công
- [ ] Kiểm tra có 6 phòng trong database
- [ ] Kiểm tra có bác sĩ X-quang (doc_xray@gmail.com)
- [ ] Kiểm tra có dịch vụ X-quang
- [ ] Kiểm tra có template với bước X-quang
- [ ] Test workflow: Tạo plan → Hoàn thành bước 1 → Kiểm tra phòng X-quang
- [ ] Kiểm tra log backend khi complete step
- [ ] Kiểm tra database sau khi complete step
- [ ] Login bác sĩ X-quang và xem queue

---

**Kết luận**: Phòng X-quang trống có thể là do:
1. Chưa có bệnh nhân nào được chuyển đến (cần test workflow)
2. Logic chuyển phòng có bug (cần kiểm tra log)
3. Query không tìm thấy queue (cần kiểm tra database)

**Next step**: Test workflow đầy đủ từ đầu để xác định nguyên nhân chính xác.

