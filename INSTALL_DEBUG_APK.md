# 📱 CÀI ĐẶT APK DEBUG MỚI

## ✅ BUILD THÀNH CÔNG

APK đã được build thành công với debug logging.

## 📦 VỊ TRÍ APK

```
mobile_android/app/build/outputs/apk/debug/app-debug.apk
```

## 🔧 CÁCH CÀI ĐẶT

### Phương pháp 1: Dùng ADB (Khuyến nghị)

```bash
adb install -r mobile_android/app/build/outputs/apk/debug/app-debug.apk
```

Flag `-r` sẽ reinstall app mà không xóa dữ liệu.

### Phương pháp 2: Copy file APK sang điện thoại

1. Copy file `app-debug.apk` vào điện thoại (qua USB hoặc email)
2. Mở file APK trên điện thoại
3. Cho phép cài đặt từ nguồn không xác định (nếu được hỏi)
4. Nhấn "Cài đặt"

## 🔍 XEM LOG DEBUG

### Mở Logcat trước khi test:

```bash
adb logcat -s DoctorWorkflow:D
```

Hoặc lưu log ra file:

```bash
adb logcat -s DoctorWorkflow:D > debug_log.txt
```

## 🧪 TEST NGAY

1. Cài APK
2. Mở Logcat (lệnh trên)
3. Mở app, đăng nhập bác sĩ
4. Vào "Quản lý hàng đợi"
5. Nhấp vào bệnh nhân có bước COMPLETED
6. **Quan sát Logcat** - sẽ thấy:

```
D/DoctorWorkflow: === AUTO-LOAD DEBUG ===
D/DoctorWorkflow: Total steps: X
D/DoctorWorkflow: Step ID=..., Status=..., UiTemplateType=..., ServiceName=...
```

7. Nếu thấy "Completed count: 0" → Vấn đề ở database
8. Nếu thấy "Completed count: X" nhưng không có toast → Vấn đề ở UI
9. Chuyển tab → Xem log "=== AUTO-POPULATE DEBUG ==="

## 📊 PHÂN TÍCH LOG

Gửi cho tôi toàn bộ log từ:
- "=== AUTO-LOAD DEBUG ===" 
- Đến hết "=== AUTO-POPULATE DEBUG ==="

Tôi sẽ phân tích và tìm nguyên nhân chính xác.

## 🛠️ NẾU VẪN KHÔNG HOẠT ĐỘNG

Chạy SQL script để kiểm tra database:

```sql
-- Kiểm tra treatment plan của bệnh nhân
SELECT 
    tp.id AS plan_id,
    COUNT(tps.id) AS total_steps,
    SUM(CASE WHEN tps.status = 'COMPLETED' THEN 1 ELSE 0 END) AS completed_steps
FROM treatment_plans tp
LEFT JOIN treatment_plan_steps tps ON tps.plan_id = tp.id
WHERE tp.patient_id = ?  -- THAY ? BẰNG PATIENT_ID
GROUP BY tp.id;

-- Kiểm tra chi tiết các bước
SELECT 
    tps.id,
    s.name AS service_name,
    tps.status,
    s.ui_template_type,
    tps.doctor_conclusion
FROM treatment_plan_steps tps
JOIN services s ON s.id = tps.service_id
WHERE tps.plan_id = ?  -- THAY ? BẰNG PLAN_ID
ORDER BY tps.sequence_order;
```

Nếu thấy:
- `ui_template_type` = NULL → Chạy fix trong `check_completed_steps.sql`
- `status` không phải "COMPLETED" → Cập nhật status
- `doctor_conclusion` = NULL → Thêm dữ liệu test

---

**Ngày**: 2026-03-29
**Build**: ✅ SUCCESS
**APK**: Ready to install
**Next**: Test và gửi log
