# 📋 Phân Tích Chi Tiết Các Dịch Vụ Hiện Tại

## 1. Cấu Trúc Dịch Vụ Trong Hệ Thống

### Service Entity
```java
@Entity
@Table(name = "services")
public class Service {
    private Long id;
    private ServiceCategory category;      // Danh mục dịch vụ
    private String name;                   // Tên dịch vụ
    private String description;            // Mô tả
    private BigDecimal price;              // ✅ GIÁ (Quan trọng!)
    private Integer durationMinutes;       // Thời gian (phút)
    private boolean active;                // Có hoạt động không
    private UiTemplateType uiTemplateType; // Loại UI (GENERAL, SURGERY, XRAY, ORTHODONTICS)
    private List<ServiceImage> images;     // Hình ảnh
}
```

### ServiceCategory Entity
```java
@Entity
@Table(name = "service_categories")
public class ServiceCategory {
    private Long id;
    private String name;        // Tên danh mục (ví dụ: "Khám bệnh", "Phẫu thuật")
    private String description; // Mô tả
}
```

---

## 2. Các Loại Dịch Vụ Theo UI Template

### A. GENERAL (Khám bệnh & Điều trị cơ bản)
**Ví dụ dịch vụ:**
- Khám bệnh ban đầu
- Tư vấn điều trị
- Vệ sinh răng
- Trám răng
- Tẩy trắng
- Lấy cao răng

**Đặc điểm:**
- Có thể áp dụng cho nhiều răng
- Giá cố định
- Không cần phòng đặc biệt

**Ví dụ giá:**
```
Trám răng:      500,000 đ
Tẩy trắng:      200,000 đ
Vệ sinh:        150,000 đ
Khám bệnh:      100,000 đ
```

### B. SURGERY (Phẫu thuật)
**Ví dụ dịch vụ:**
- Nhổ răng
- Nhổ răng khôn
- Phẫu thuật nướu
- Cắt lợi

**Đặc điểm:**
- Cần phòng phẫu thuật
- Giá cao hơn
- Cần bác sĩ chuyên khoa
- Thường áp dụng cho 1 răng

**Ví dụ giá:**
```
Nhổ răng thường:    300,000 đ
Nhổ răng khôn:      800,000 đ
Phẫu thuật nướu:    500,000 đ
```

### C. XRAY (Chụp X-quang)
**Ví dụ dịch vụ:**
- Chụp X-quang toàn cảnh
- Chụp X-quang cắn cánh
- Chụp CT 3D

**Đặc điểm:**
- Cần phòng X-quang
- Giá thấp
- Có thể áp dụng cho nhiều răng
- Thường là bước đầu tiên

**Ví dụ giá:**
```
X-quang cắn cánh:   100,000 đ
X-quang toàn cảnh:  200,000 đ
CT 3D:              500,000 đ
```

### D. ORTHODONTICS (Niềng răng)
**Ví dụ dịch vụ:**
- Niềng răng kim loại
- Niềng răng sứ
- Niềng răng trong suốt
- Tháo niềng

**Đặc điểm:**
- Giá rất cao
- Thời gian dài (tháng/năm)
- Áp dụng cho toàn bộ hàm
- Cần theo dõi định kỳ

**Ví dụ giá:**
```
Niềng kim loại:     5,000,000 đ
Niềng sứ:           8,000,000 đ
Niềng trong suốt:   10,000,000 đ
Tháo niềng:         1,000,000 đ
```

---

## 3. Mối Quan Hệ Giữa Service & Tooth

### Khi Bác Sĩ Chọn Dịch Vụ Cho Một Răng

```
┌─────────────────────────────────────────────────────────┐
│ Service (Dịch vụ)                                       │
├─────────────────────────────────────────────────────────┤
│ ID: 5                                                   │
│ Name: "Trám răng"                                       │
│ Price: 500,000 đ                                        │
│ Category: "Điều trị cơ bản"                              │
│ UiTemplateType: GENERAL                                 │
│ Duration: 30 phút                                       │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│ TreatmentPlanStep (Bước điều trị)                       │
├─────────────────────────────────────────────────────────┤
│ ID: 123                                                 │
│ Service: Service(id=5)                                  │
│ ToothNumber: "8" (FDI notation)                          │
│ ActualPrice: 500,000 đ (lấy từ Service.price)           │
│ SequenceOrder: 1                                        │
│ Status: PENDING                                         │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│ InvoiceItem (Chi tiết hóa đơn)                          │
├─────────────────────────────────────────────────────────┤
│ ID: 456                                                 │
│ ServiceName: "Trám răng"                                │
│ ToothNumber: "8"                                        │
│ UnitPrice: 500,000 đ                                    │
│ Quantity: 1                                             │
│ TotalPrice: 500,000 đ                                   │
└─────────────────────────────────────────────────────────┘
```

---

## 4. Quy Tắc Tính Toán Chi Phí

### Quy Tắc 1: Một Dịch Vụ Cho Một Răng

```
Trám răng cho răng 8:
  UnitPrice = Service.price = 500,000 đ
  Quantity = 1
  TotalPrice = 500,000 × 1 = 500,000 đ
```

### Quy Tắc 2: Một Dịch Vụ Cho Nhiều Răng

```
Trám răng cho 3 răng (8, 16, 36):
  
  Răng 8:  500,000 đ
  Răng 16: 500,000 đ
  Răng 36: 500,000 đ
  ─────────────────
  Tổng:    1,500,000 đ
  
  Cách lưu:
  - TreatmentPlanStep 1: toothNumber="8", service=Trám, price=500k
  - TreatmentPlanStep 2: toothNumber="16", service=Trám, price=500k
  - TreatmentPlanStep 3: toothNumber="36", service=Trám, price=500k
```

### Quy Tắc 3: Nhiều Dịch Vụ Cho Một Răng

```
Răng 8 cần:
  1. Chụp X-quang: 100,000 đ
  2. Trám răng:    500,000 đ
  ─────────────────────────
  Tổng:            600,000 đ
  
  Cách lưu:
  - TreatmentPlanStep 1: toothNumber="8", service=X-quang, price=100k
  - TreatmentPlanStep 2: toothNumber="8", service=Trám, price=500k
```

### Quy Tắc 4: Tính Tổng Chi Phí Treatment Plan

```
TreatmentPlan Total = SUM(TreatmentPlanStep.actualPrice)

Ví dụ:
  Step 1: Răng 8, Trám, 500k
  Step 2: Răng 16, Nhổ, 300k
  Step 3: Răng 36, Trám, 500k
  ─────────────────────────
  Total: 1,300,000 đ
```

---

## 5. Các Trường Hợp Đặc Biệt

### Trường Hợp 1: Dịch Vụ Có Giá Khác Nhau Tùy Theo Tình Trạng

**Vấn đề:** Nhổ răng thường vs nhổ răng khôn có giá khác nhau

**Giải Pháp:**
- Tạo 2 dịch vụ riêng biệt:
  - Service 1: "Nhổ răng thường" - 300k
  - Service 2: "Nhổ răng khôn" - 800k
- Bác sĩ chọn dịch vụ phù hợp

### Trường Hợp 2: Giảm Giá Cho Nhiều Răng

**Vấn đề:** Trám 5 răng có thể được giảm giá

**Giải Pháp:**
- Tạo dịch vụ riêng: "Trám 5 răng" - 2,000k (thay vì 2,500k)
- Hoặc: Bác sĩ có thể chỉnh sửa `actualPrice` sau khi thêm

### Trường Hợp 3: Dịch Vụ Bao Gồm Nhiều Bước

**Vấn đề:** Niềng răng là quá trình dài, cần tính chi phí từng giai đoạn

**Giải Pháp:**
- Tạo dịch vụ: "Niềng răng - Giai đoạn 1" - 2,000k
- Tạo dịch vụ: "Niềng răng - Giai đoạn 2" - 2,000k
- Tạo dịch vụ: "Niềng răng - Giai đoạn 3" - 1,000k

---

## 6. Ví Dụ Thực Tế Chi Tiết

### Scenario: Bệnh nhân Nguyễn Văn A

**Tình trạng:**
- Răng 8: Sâu, cần trám
- Răng 16: Sâu nặng, cần nhổ
- Răng 36: Sâu, cần trám
- Cần chụp X-quang trước

**Kế Hoạch Điều Trị:**

| Bước | Răng | Dịch Vụ | Giá | Tổng |
|------|------|---------|-----|------|
| 1 | - | Chụp X-quang toàn cảnh | 200k | 200k |
| 2 | 8 | Trám răng | 500k | 700k |
| 3 | 16 | Nhổ răng | 300k | 1,000k |
| 4 | 36 | Trám răng | 500k | 1,500k |

**Cấu Trúc Dữ Liệu:**

```
TreatmentPlan (ID: 1)
├── Patient: Nguyễn Văn A
├── Status: IN_PROGRESS
└── Steps:
    ├── Step 1:
    │   ├── Service: Chụp X-quang toàn cảnh
    │   ├── ToothNumber: null (không áp dụng cho răng cụ thể)
    │   ├── ActualPrice: 200,000
    │   └── SequenceOrder: 1
    │
    ├── Step 2:
    │   ├── Service: Trám răng
    │   ├── ToothNumber: "8"
    │   ├── ActualPrice: 500,000
    │   └── SequenceOrder: 2
    │
    ├── Step 3:
    │   ├── Service: Nhổ răng
    │   ├── ToothNumber: "16"
    │   ├── ActualPrice: 300,000
    │   └── SequenceOrder: 3
    │
    └── Step 4:
        ├── Service: Trám răng
        ├── ToothNumber: "36"
        ├── ActualPrice: 500,000
        └── SequenceOrder: 4

Total Cost: 1,500,000 đ
```

**Hóa Đơn:**

```
Invoice (ID: 1)
├── Patient: Nguyễn Văn A
├── TreatmentPlan: Plan 1
├── Items:
│   ├── Item 1: Chụp X-quang toàn cảnh - 200,000 đ
│   ├── Item 2: Trám răng (Răng 8) - 500,000 đ
│   ├── Item 3: Nhổ răng (Răng 16) - 300,000 đ
│   └── Item 4: Trám răng (Răng 36) - 500,000 đ
├── TotalAmount: 1,500,000 đ
└── PaymentStatus: UNPAID
```

---

## 7. Biểu Đồ Răng - Hiển Thị Dịch Vụ

### Trước Khi Chọn Dịch Vụ
```
Hàm trên:
┌──┬──┬──┬──┬──┬──┬──┬──┐
│18│17│16│15│14│13│12│11│  (Tất cả trắng - healthy)
└──┴──┴──┴──┴──┴──┴──┴──┘

┌──┬──┬──┬──┬──┬──┬──┬──┐
│21│22│23│24│25│26│27│28│
└──┴──┴──┴──┴──┴──┴──┴──┘

Hàm dưới:
┌──┬──┬──┬──┬──┬──┬──┬──┐
│48│47│46│45│44│43│42│41│
└──┴──┴──┴──┴──┴──┴──┴──┘

┌──┬──┬──┬──┬──┬──┬──┬──┐
│31│32│33│34│35│36│37│38│
└──┴──┴──┴──┴──┴──┴──┴──┘
```

### Sau Khi Chọn Dịch Vụ
```
Hàm trên:
┌──┬──┬──┬──┬──┬──┬──┬──┐
│18│17│16│15│14│13│12│11│
└──┴──┴──┴──┴──┴──┴──┴──┘
      ↓ Nhổ (300k)

┌──┬──┬──┬──┬──┬──┬──┬──┐
│21│22│23│24│25│26│27│28│
└──┴──┴──┴──┴──┴──┴──┴──┘

Hàm dưới:
┌──┬──┬──┬──┬──┬──┬──┬──┐
│48│47│46│45│44│43│42│41│
└──┴──┴──┴──┴──┴──┴──┴──┘

┌──┬──┬──┬──┬──┬──┬──┬──┐
│31│32│33│34│35│36│37│38│
└──┴──┴──┴──┴──┴──┴──┴──┘
            ↓ Trám (500k)

Tổng: 800,000 đ
```

---

## 8. Lưu Ý Quan Trọng

### ✅ Những Gì Đã Có Sẵn
- Service entity với price
- TreatmentPlanStep với toothNumber
- InvoiceItem với toothNumber
- OdontogramView để hiển thị 32 răng

### ⚠️ Những Gì Cần Thêm
- Dialog để chọn dịch vụ
- API endpoint để thêm dịch vụ cho răng
- Logic tính toán tổng chi phí
- Hiển thị dịch vụ trên biểu đồ răng

### 🔄 Quy Trình Tính Toán
1. Bác sĩ chọn dịch vụ cho răng
2. Lấy giá từ Service.price
3. Lưu vào TreatmentPlanStep.actualPrice
4. Tính tổng: SUM(TreatmentPlanStep.actualPrice)
5. Hiển thị trên UI

---

## 9. Kiểm Tra Dữ Liệu Hiện Tại

### SQL Query để Xem Dịch Vụ Hiện Tại
```sql
-- Xem tất cả dịch vụ
SELECT s.id, s.name, s.price, sc.name as category, s.ui_template_type
FROM services s
LEFT JOIN service_categories sc ON s.category_id = sc.id
WHERE s.is_active = true
ORDER BY sc.name, s.name;

-- Xem dịch vụ theo loại
SELECT * FROM services WHERE ui_template_type = 'GENERAL' AND is_active = true;
SELECT * FROM services WHERE ui_template_type = 'SURGERY' AND is_active = true;
SELECT * FROM services WHERE ui_template_type = 'XRAY' AND is_active = true;
SELECT * FROM services WHERE ui_template_type = 'ORTHODONTICS' AND is_active = true;

-- Xem treatment plan steps
SELECT tps.id, tps.tooth_number, s.name, tps.actual_price, tps.status
FROM treatment_plan_steps tps
LEFT JOIN services s ON tps.service_id = s.id
WHERE tps.plan_id = 1
ORDER BY tps.sequence_order;
```

---

## 10. Tóm Tắt

| Khía Cạnh | Chi Tiết |
|-----------|---------|
| **Dịch vụ** | Có sẵn trong Service entity |
| **Giá** | Lưu trong Service.price |
| **Răng** | Lưu trong TreatmentPlanStep.toothNumber |
| **Tính toán** | SUM(TreatmentPlanStep.actualPrice) |
| **Hóa đơn** | Tạo từ TreatmentPlan → InvoiceItem → Invoice |
| **UI** | OdontogramView + Dialog chọn dịch vụ |
