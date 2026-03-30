# 🦷 Tóm Tắt Cho Leader - Hệ Thống Biểu Đồ Răng & Tính Toán Chi Phí

## 📊 Tình Hình Hiện Tại

### ✅ Những Gì Đã Có
1. **Service Entity** - Lưu tất cả dịch vụ với giá
2. **TreatmentPlanStep** - Liên kết dịch vụ với răng cụ thể
3. **InvoiceItem** - Chi tiết hóa đơn (đã hỗ trợ toothNumber)
4. **OdontogramView** - Biểu đồ 32 răng (có thể click)
5. **FDI Tooth Numbering** - Hệ thống đánh số răng chuẩn

### ❌ Những Gì Cần Thêm
1. **Dialog chọn dịch vụ** - Khi bác sĩ nhấp vào răng
2. **API endpoints** - Thêm/xóa/cập nhật dịch vụ cho răng
3. **Logic tính toán** - Tự động tính tổng chi phí
4. **UI hiển thị** - Hiển thị dịch vụ trên biểu đồ

---

## 🎯 Phương Pháp Giải Pháp (Tóm Tắt)

### Luồng Xử Lý Chính
```
Bác sĩ nhấp vào răng 8
    ↓
Dialog hiển thị danh sách dịch vụ
    ↓
Bác sĩ chọn "Trám răng" (500k)
    ↓
API: POST /api/treatment-plans/1/teeth/8/services
    ↓
Backend: Tạo TreatmentPlanStep + Tính tổng chi phí
    ↓
Frontend: Cập nhật UI (màu răng + giá + tổng)
```

### Cấu Trúc Dữ Liệu
```
Service (Dịch vụ)
  ├── ID: 5
  ├── Name: "Trám răng"
  ├── Price: 500,000 đ ← Quan trọng!
  └── Category: "Điều trị cơ bản"
        ↓
TreatmentPlanStep (Bước điều trị)
  ├── Service: Service(5)
  ├── ToothNumber: "8" (FDI)
  ├── ActualPrice: 500,000 đ
  └── Status: PENDING
        ↓
InvoiceItem (Chi tiết hóa đơn)
  ├── ServiceName: "Trám răng"
  ├── ToothNumber: "8"
  ├── UnitPrice: 500,000 đ
  └── TotalPrice: 500,000 đ
```

---

## 💰 Ví Dụ Tính Toán Chi Phí

### Scenario: Bệnh nhân cần 3 dịch vụ

| Bước | Răng | Dịch Vụ | Giá | Tổng Tích Lũy |
|------|------|---------|-----|---------------|
| 1 | - | Chụp X-quang | 200k | 200k |
| 2 | 8 | Trám răng | 500k | 700k |
| 3 | 16 | Nhổ răng | 300k | 1,000k |

**Công Thức:**
```
Total = SUM(TreatmentPlanStep.actualPrice)
      = 200k + 500k + 300k
      = 1,000,000 đ
```

---

## 🏗️ Kiến Trúc Triển Khai

### Backend (Spring Boot)
```
ToothServiceCalculationService
├── addServiceToTooth(planId, serviceId, toothNumber)
├── removeServiceFromTooth(stepId)
├── recalculatePlanTotalCost(planId)
└── updateStepPrice(stepId, newPrice)

ToothServiceController
├── POST /api/treatment-plans/{planId}/teeth/{toothNumber}/services
├── GET /api/treatment-plans/{planId}/teeth/{toothNumber}/services
├── DELETE /api/treatment-plans/{planId}/teeth/steps/{stepId}
└── PUT /api/treatment-plans/{planId}/teeth/steps/{stepId}/price
```

### Frontend (Android)
```
ToothServiceDialog
├── Hiển thị danh sách dịch vụ
├── Bác sĩ chọn dịch vụ
└── Gọi API thêm dịch vụ

DoctorWorkflowActivity
├── OdontogramView (biểu đồ răng)
├── Listener: onToothSelected()
├── Hiển thị tổng chi phí
└── Load dữ liệu từ API

OdontogramView (Cập nhật)
├── Hiển thị tên dịch vụ dưới mỗi răng
├── Đổi màu khi có dịch vụ
└── Click để mở dialog
```

---

## 📋 Các Loại Dịch Vụ Hiện Tại

### 1. GENERAL (Khám & Điều trị cơ bản)
- Trám răng: 500k
- Tẩy trắng: 200k
- Vệ sinh: 150k
- Khám bệnh: 100k

### 2. SURGERY (Phẫu thuật)
- Nhổ răng thường: 300k
- Nhổ răng khôn: 800k
- Phẫu thuật nướu: 500k

### 3. XRAY (Chụp X-quang)
- X-quang cắn cánh: 100k
- X-quang toàn cảnh: 200k
- CT 3D: 500k

### 4. ORTHODONTICS (Niềng răng)
- Niềng kim loại: 5M
- Niềng sứ: 8M
- Niềng trong suốt: 10M

---

## 🔢 Hệ Thống Đánh Số Răng (FDI)

```
Hàm trên:
┌──┬──┬──┬──┬──┬──┬──┬──┐
│18│17│16│15│14│13│12│11│ (Phải)
└──┴──┴──┴──┴──┴──┴──┴──┘

┌──┬──┬──┬──┬──┬──┬──┬──┐
│21│22│23│24│25│26│27│28│ (Trái)
└──┴──┴──┴──┴──┴──┴──┴──┘

Hàm dưới:
┌──┬──┬──┬──┬──┬──┬──┬──┐
│48│47│46│45│44│43│42│41│ (Phải)
└──┴──┴──┴──┴──┴──┴──┴──┘

┌──┬──┬──┬──┬──┬──┬──┬──┐
│31│32│33│34│35│36│37│38│ (Trái)
└──┴──┴──┴──┴──┴──┴──┴──┘
```

---

## 📱 Giao Diện Người Dùng

### Trước Khi Chọn Dịch Vụ
```
┌─────────────────────────────────┐
│ Biểu Đồ Răng                    │
│                                 │
│ [18][17][16][15][14][13][12][11]│
│ [21][22][23][24][25][26][27][28]│
│ [48][47][46][45][44][43][42][41]│
│ [31][32][33][34][35][36][37][38]│
│                                 │
│ Tổng chi phí: 0 đ               │
└─────────────────────────────────┘
```

### Sau Khi Chọn Dịch Vụ
```
┌─────────────────────────────────┐
│ Biểu Đồ Răng                    │
│                                 │
│ [18][17][16][15][14][13][12][11]│
│ [21][22][23][24][25][26][27][28]│
│ [48][47][46][45][44][43][42][41]│
│ [31][32][33][34][35][36][37][38]│
│      ↓ Nhổ (300k)               │
│                                 │
│ Tổng chi phí: 300,000 đ         │
└─────────────────────────────────┘
```

---

## ✅ Checklist Triển Khai

### Phase 1: Backend (1-2 tuần)
- [ ] Tạo ToothServiceCalculationService
- [ ] Tạo ToothServiceController
- [ ] Tạo Request/Response DTOs
- [ ] Viết unit tests
- [ ] Deploy & test API

### Phase 2: Frontend (1-2 tuần)
- [ ] Tạo ToothServiceDialog
- [ ] Tạo ServiceSelectionAdapter
- [ ] Cập nhật OdontogramView
- [ ] Tích hợp vào DoctorWorkflowActivity
- [ ] Cập nhật ApiService

### Phase 3: Testing & QA (1 tuần)
- [ ] Test thêm/xóa dịch vụ
- [ ] Test tính toán chi phí
- [ ] Test UI/UX
- [ ] Test edge cases
- [ ] UAT với bác sĩ

---

## 🎯 Lợi Ích Của Giải Pháp

### Cho Bác Sĩ
✅ Dễ dàng chọn dịch vụ cho từng răng
✅ Thấy ngay tổng chi phí
✅ Có thể chỉnh sửa giá nếu cần
✅ Tự động tạo hóa đơn

### Cho Bệnh Nhân
✅ Rõ ràng về chi phí điều trị
✅ Biết được từng dịch vụ cho răng nào
✅ Dễ dàng thanh toán

### Cho Quản Lý
✅ Theo dõi doanh thu theo dịch vụ
✅ Báo cáo chi tiết theo từng bệnh nhân
✅ Quản lý hóa đơn tự động

---

## 🔗 Liên Kết Tài Liệu

1. **ODONTOGRAM_TOOTH_SERVICE_DESIGN.md** - Thiết kế chi tiết
2. **ODONTOGRAM_IMPLEMENTATION_MODELS.md** - Các model & DTO
3. **CURRENT_SERVICES_ANALYSIS.md** - Phân tích dịch vụ hiện tại

---

## 💡 Lưu Ý Quan Trọng

### Về Dữ Liệu
- Tất cả dịch vụ đã có giá trong Service.price
- TreatmentPlanStep đã hỗ trợ toothNumber
- InvoiceItem đã hỗ trợ toothNumber

### Về Tính Toán
- Giá = Service.price (có thể override bằng actualPrice)
- Tổng = SUM(TreatmentPlanStep.actualPrice)
- Hóa đơn = Tạo từ TreatmentPlan

### Về UI
- OdontogramView đã có sẵn
- Cần thêm Dialog để chọn dịch vụ
- Cần hiển thị tên dịch vụ trên biểu đồ

---

## 📞 Hỗ Trợ

Nếu có câu hỏi, vui lòng tham khảo:
- Tài liệu thiết kế chi tiết
- Ví dụ code trong ODONTOGRAM_IMPLEMENTATION_MODELS.md
- Phân tích dịch vụ trong CURRENT_SERVICES_ANALYSIS.md

---

## 🚀 Bước Tiếp Theo

1. **Review** tài liệu thiết kế
2. **Phê duyệt** kiến trúc
3. **Phân công** công việc
4. **Bắt đầu** triển khai Phase 1 (Backend)
5. **Test** & **Deploy**
