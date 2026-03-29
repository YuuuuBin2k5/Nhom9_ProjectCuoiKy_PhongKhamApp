# TEST NHANH CHỨC NĂNG THANH TOÁN

## Bước 1: Kiểm tra Backend đang chạy

```bash
curl http://localhost:8080/actuator/health
```

Kết quả mong đợi: `{"status":"UP"}`

---

## Bước 2: Login để lấy token

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "doctor@clinic.com",
    "password": "Doctor@123"
  }'
```

Lưu token từ response.

---

## Bước 3: Test API tạo invoice

Thay `YOUR_TOKEN` và `PLAN_ID`:

```bash
curl -X POST http://localhost:8080/api/treatment-plans/1/complete-and-generate-invoice \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -v
```

### Kết quả mong đợi:

**Nếu thành công (HTTP 200):**
```json
{
  "id": 1,
  "patientId": 1,
  "patientName": "Nguyen Van A",
  "totalAmount": 1500000.0,
  "paymentStatus": "UNPAID",
  "items": [
    {
      "serviceName": "Nhổ răng khôn",
      "totalPrice": 500000.0
    }
  ]
}
```

**Nếu còn bước chưa hoàn thành (HTTP 400):**
```json
{
  "message": "Không thể tạo hóa đơn. Vui lòng hoàn thành tất cả các bước điều trị trước."
}
```

---

## Bước 4: Build và test Mobile

```bash
# Build APK
cd mobile_android
./gradlew assembleDebug

# Install
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Check logs
adb logcat | grep -i "DoctorWorkflow\|Invoice\|Payment"
```

---

## Bước 5: Test trên Mobile

1. Mở app, login as doctor
2. Quét QR hoặc chọn bệnh nhân
3. Hoàn thành tất cả bước điều trị
4. Nhấn nút "Thanh toán"
5. Xác nhận trong dialog
6. Kiểm tra chuyển đến màn hình thanh toán

---

## Kiểm tra Database

```sql
-- Xem invoice vừa tạo
SELECT * FROM invoices ORDER BY created_at DESC LIMIT 1;

-- Xem chi tiết items
SELECT * FROM invoice_items WHERE invoice_id = (
  SELECT id FROM invoices ORDER BY created_at DESC LIMIT 1
);

-- Xem treatment plan status
SELECT id, status FROM treatment_plans WHERE id = 1;
```

---

## Troubleshooting

### Lỗi: Table invoice_items doesn't exist
```sql
CREATE TABLE invoice_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    invoice_id BIGINT NOT NULL,
    service_id BIGINT,
    treatment_plan_step_id BIGINT,
    service_name VARCHAR(255) NOT NULL,
    tooth_number VARCHAR(50),
    quantity INT NOT NULL DEFAULT 1,
    unit_price DECIMAL(10,2) NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    description TEXT,
    FOREIGN KEY (invoice_id) REFERENCES invoices(id)
);
```

### Lỗi: 401 Unauthorized
- Token hết hạn → Login lại
- Token sai → Copy đúng token

### Lỗi: 400 Bad Request
- Kiểm tra tất cả steps đã COMPLETED chưa
- Đây là validation đúng!
