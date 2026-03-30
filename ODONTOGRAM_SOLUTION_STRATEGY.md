# 🎯 Phương Án Chiến Lược - Biểu Đồ Răng & Tính Toán Chi Phí

## 📌 Tóm Tắt Vấn Đề

Hệ thống có **11 dịch vụ** với các đặc điểm khác nhau:
- **4 dịch vụ** áp dụng cho từng răng cụ thể
- **7 dịch vụ** áp dụng cho tổng quát (không áp dụng cho từng răng)

---

## 🔍 Phân Loại Dịch Vụ

### Nhóm A: Áp Dụng Cho Từng Răng (Hiển Thị Trên Biểu Đồ)

| # | Dịch Vụ | Giá | Loại | Ghi Chú |
|---|---------|-----|------|---------|
| 1 | Trám răng sâu | 300k | GENERAL | Áp dụng cho 1 răng |
| 2 | Nhổ răng thường | 300k | SURGERY | Áp dụng cho 1 răng |
| 3 | Nhổ răng khôn | 2M | SURGERY | Áp dụng cho 1 răng (phức tạp) |
| 4 | Bọc răng sứ | 5M | GENERAL | Áp dụng cho 1 răng |

### Nhóm B: Dịch Vụ Tổng Quát (Không Áp Dụng Cho Từng Răng)

| # | Dịch Vụ | Giá | Loại | Ghi Chú |
|---|---------|-----|------|---------|
| 1 | Khám và tư vấn | 100k | GENERAL | Tổng quát |
| 2 | Chụp X-quang | 200k | GENERAL | Tổng quát |
| 3 | Lấy cao & đánh bóng | 250k | PERIO | Tổng quát |
| 4 | Điều trị tủy | 1.5M | GENERAL | Có thể 1 răng nhưng thường riêng |
| 5 | Tẩy trắng | 2.5M | GENERAL | Toàn bộ hàm |
| 6 | Niềng răng | 30M | ORTHO | Toàn bộ hàm, dài hạn |

---

## 💡 Phương Án Được Khuyến Nghị

### Phương Án: Tách Biệt Dịch Vụ Theo Loại

#### UI/UX Design:

```
┌─────────────────────────────────────────────────────────┐
│ DoctorWorkflowActivity                                  │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  ┌─────────────────────────────────────────────────┐   │
│  │ Biểu Đồ Răng (OdontogramView)                   │   │
│  │                                                 │   │
│  │ [18][17][16][15][14][13][12][11]               │   │
│  │ [21][22][23][24][25][26][27][28]               │   │
│  │ [48][47][46][45][44][43][42][41]               │   │
│  │ [31][32][33][34][35][36][37][38]               │   │
│  │                                                 │   │
│  │ Nhấp vào răng để thêm dịch vụ cụ thể           │   │
│  └─────────────────────────────────────────────────┘   │
│                                                         │
│  ┌─────────────────────────────────────────────────┐   │
│  │ Các Bước Khác (Dịch Vụ Tổng Quát)              │   │
│  │                                                 │   │
│  │ [+] Khám và tư vấn (100k)                       │   │
│  │ [+] Chụp X-quang (200k)                         │   │
│  │ [+] Lấy cao & đánh bóng (250k)                  │   │
│  │ [+] Điều trị tủy (1.5M)                         │   │
│  │ [+] Tẩy trắng (2.5M)                            │   │
│  │ [+] Niềng răng (30M)                            │   │
│  │                                                 │   │
│  │ Nhấp [+] để thêm dịch vụ tổng quát              │   │
│  └─────────────────────────────────────────────────┘   │
│                                                         │
│  Tổng chi phí: 0 đ                                      │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

---

## 🔧 Triển Khai Chi Tiết

### Backend Changes:

#### 1. Mở Rộng TreatmentPlanStep

```java
// Thêm field để phân biệt loại dịch vụ
@Entity
public class TreatmentPlanStep {
    // ... existing fields ...
    
    @Column(name = "is_general_service")
    private boolean isGeneralService;  // true = tổng quát, false = cụ thể
    
    // Nếu isGeneralService = true, toothNumber = null
    // Nếu isGeneralService = false, toothNumber = "8", "16", etc.
}
```

#### 2. API Endpoints

```
POST /api/treatment-plans/{planId}/teeth/{toothNumber}/services
  → Thêm dịch vụ cụ thể cho một răng
  Request: { "serviceId": 4, "sequenceOrder": 1 }
  
POST /api/treatment-plans/{planId}/general-services
  → Thêm dịch vụ tổng quát
  Request: { "serviceId": 1, "sequenceOrder": 1 }
  
GET /api/treatment-plans/{planId}/services
  → Lấy tất cả dịch vụ (cả cụ thể và tổng quát)
  
DELETE /api/treatment-plans/{planId}/steps/{stepId}
  → Xóa dịch vụ
```

### Frontend Changes:

#### 1. Dialog Chọn Dịch Vụ Cụ Thể

```java
// Khi bác sĩ nhấp vào một răng
private void showToothServiceDialog(String toothNumber) {
    ToothServiceDialog dialog = new ToothServiceDialog();
    dialog.setPlanId(treatmentPlanId);
    dialog.setToothNumber(toothNumber);
    
    // Chỉ hiển thị 4 dịch vụ cụ thể
    List<ServiceItem> specificServices = Arrays.asList(
        new ServiceItem(4, "Trám răng sâu", 300000),
        new ServiceItem(6, "Nhổ răng thường", 300000),
        new ServiceItem(7, "Nhổ răng khôn", 2000000),
        new ServiceItem(9, "Bọc răng sứ", 5000000)
    );
    
    dialog.setServices(specificServices);
    dialog.show(getSupportFragmentManager(), "ToothServiceDialog");
}
```

#### 2. Danh Sách Dịch Vụ Tổng Quát

```java
// Hiển thị danh sách dịch vụ tổng quát
private void setupGeneralServicesList() {
    List<ServiceItem> generalServices = Arrays.asList(
        new ServiceItem(1, "Khám và tư vấn", 100000),
        new ServiceItem(2, "Chụp X-quang", 200000),
        new ServiceItem(3, "Lấy cao & đánh bóng", 250000),
        new ServiceItem(5, "Điều trị tủy", 1500000),
        new ServiceItem(8, "Tẩy trắng", 2500000),
        new ServiceItem(10, "Niềng răng", 30000000)
    );
    
    // Hiển thị trong RecyclerView hoặc ListView
    generalServiceAdapter.setServices(generalServices);
}
```

---

## 📊 Ví Dụ Tính Toán

### Scenario: Bệnh nhân cần nhổ răng khôn

```
Bước 1: Khám tổng quát
  → Thêm "Khám và tư vấn" (100k)
  → toothNumber = null, isGeneralService = true

Bước 2: Chụp X-quang
  → Thêm "Chụp X-quang" (200k)
  → toothNumber = null, isGeneralService = true

Bước 3: Nhổ răng khôn số 8
  → Thêm "Nhổ răng khôn" (2M)
  → toothNumber = "8", isGeneralService = false

Bước 4: Nhổ răng khôn số 48
  → Thêm "Nhổ răng khôn" (2M)
  → toothNumber = "48", isGeneralService = false

─────────────────────────────────────────
Tổng chi phí: 100k + 200k + 2M + 2M = 4,300,000 đ
```

---

## 🎨 Biểu Đồ Răng - Hiển Thị

### Trước Khi Chọn Dịch Vụ:
```
Hàm trên:
┌──┬──┬──┬──┬──┬──┬──┬──┐
│18│17│16│15│14│13│12│11│  (Tất cả trắng)
└──┴──┴──┴──┴──┴──┴──┴──┘

Hàm dưới:
┌──┬──┬──┬──┬──┬──┬──┬──┐
│48│47│46│45│44│43│42│41│
└──┴──┴──┴──┴──┴──┴──┴──┘
```

### Sau Khi Chọn Dịch Vụ:
```
Hàm trên:
┌──┬──┬──┬──┬──┬──┬──┬──┐
│18│17│16│15│14│13│12│11│
└──┴──┴──┴──┴──┴──┴──┴──┘

Hàm dưới:
┌──┬──┬──┬──┬──┬──┬──┬──┐
│48│47│46│45│44│43│42│41│
└──┴──┴──┴──┴──┴──┴──┴──┘
      ↓ Nhổ khôn (2M)

Các bước khác:
  ✓ Khám và tư vấn (100k)
  ✓ Chụp X-quang (200k)

Tổng: 2,300,000 đ
```

---

## ✅ Lợi Ích Của Phương Án Này

1. **Rõ ràng:** Phân biệt rõ dịch vụ cụ thể vs tổng quát
2. **Dễ sử dụng:** Bác sĩ không bị nhầm lẫn
3. **Linh hoạt:** Có thể thêm dịch vụ tổng quát bất kỳ lúc nào
4. **Chính xác:** Tính toán chi phí đúng
5. **Mở rộng:** Dễ thêm dịch vụ mới

---

## 🔄 Quy Trình Triển Khai

### Phase 1: Backend (1 tuần)
- [ ] Mở rộng TreatmentPlanStep entity
- [ ] Tạo API endpoints
- [ ] Viết unit tests

### Phase 2: Frontend (1 tuần)
- [ ] Tạo ToothServiceDialog
- [ ] Tạo GeneralServicesList
- [ ] Tích hợp vào DoctorWorkflowActivity
- [ ] Cập nhật OdontogramView

### Phase 3: Testing (3-5 ngày)
- [ ] Test thêm/xóa dịch vụ
- [ ] Test tính toán chi phí
- [ ] Test UI/UX
- [ ] UAT

---

## 📝 Ghi Chú Quan Trọng

1. **Dịch vụ Nhổ Răng Khôn:** Có thể áp dụng cho 1 hoặc nhiều răng
   - Nếu nhổ 2 răng khôn → Thêm 2 TreatmentPlanStep
   - Mỗi step có toothNumber khác nhau

2. **Dịch vụ Điều Trị Tủy:** Có thể áp dụng cho 1 răng
   - Nên thêm vào danh sách dịch vụ cụ thể
   - Hoặc để trong danh sách tổng quát

3. **Dịch vụ Tẩy Trắng & Niềng:** Không áp dụng cho từng răng
   - Luôn để trong danh sách tổng quát
   - toothNumber = null

---

## 🎯 Kết Luận

**Phương án được khuyến nghị:**
- Tách biệt dịch vụ cụ thể (4 dịch vụ) và tổng quát (6 dịch vụ)
- Hiển thị trên 2 phần UI khác nhau
- Dễ sử dụng, rõ ràng, chính xác
- Thời gian triển khai: 2-3 tuần
