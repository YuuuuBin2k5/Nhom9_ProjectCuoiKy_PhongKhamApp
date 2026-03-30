# 🦷 Phân Tích Chi Tiết Các Dịch Vụ Thực Tế Trong Hệ Thống

## 📋 Danh Sách Dịch Vụ Hiện Tại (Từ DataSeed.java)

### Tổng Cộng: 11 Dịch Vụ

---

## 1️⃣ DANH MỤC: Khám & Chẩn đoán

### Service 1: Khám và tư vấn răng miệng
- **Giá:** 100,000 đ
- **Thời gian:** 20 phút
- **UI Template:** GENERAL
- **Mô tả:** Khám tổng quát và lập kế hoạch
- **Hình ảnh:** 4 ảnh
- **Phòng:** Phòng khám 01-05 (Bác sĩ tổng quát)
- **Áp dụng cho:** Tất cả bệnh nhân

### Service 2: Chụp X-quang răng
- **Giá:** 200,000 đ
- **Thời gian:** 15 phút
- **UI Template:** GENERAL
- **Mô tả:** Chụp phim kỹ thuật số panorama
- **Hình ảnh:** 4 ảnh
- **Phòng:** Phòng X-quang (Bác sĩ chẩn đoán)
- **Áp dụng cho:** Bệnh nhân cần chẩn đoán hình ảnh

---

## 2️⃣ DANH MỤC: Nha khoa Tổng quát

### Service 3: Lấy cao răng & đánh bóng
- **Giá:** 250,000 đ
- **Thời gian:** 30 phút
- **UI Template:** PERIO
- **Mô tả:** Vệ sinh răng miệng chuyên sâu
- **Hình ảnh:** 4 ảnh
- **Phòng:** Phòng khám 01-05
- **Áp dụng cho:** Bệnh nhân cần vệ sinh

### Service 4: Trám răng sâu
- **Giá:** 300,000 đ
- **Thời gian:** 30 phút
- **UI Template:** GENERAL
- **Mô tả:** Phục hồi răng sâu bằng composite
- **Hình ảnh:** 4 ảnh
- **Phòng:** Phòng khám 01-05
- **Áp dụng cho:** Bệnh nhân có sâu răng

### Service 5: Điều trị tủy răng
- **Giá:** 1,500,000 đ
- **Thời gian:** 60 phút
- **UI Template:** GENERAL
- **Mô tả:** Nội nha lấy tủy và hàn ống tủy
- **Hình ảnh:** 4 ảnh
- **Phòng:** Phòng khám 01-05
- **Áp dụng cho:** Bệnh nhân cần điều trị tủy

---

## 3️⃣ DANH MỤC: Tiểu phẫu

### Service 6: Nhổ răng thường
- **Giá:** 300,000 đ
- **Thời gian:** 20 phút
- **UI Template:** SURGERY
- **Mô tả:** Nhổ răng lung lay hoặc hư tổn
- **Hình ảnh:** 4 ảnh
- **Phòng:** Phòng tiểu phẫu (Bác sĩ phẫu thuật)
- **Áp dụng cho:** Bệnh nhân cần nhổ răng thường

### Service 7: Nhổ răng khôn ⭐
- **Giá:** 2,000,000 đ
- **Thời gian:** 45 phút
- **UI Template:** SURGERY
- **Mô tả:** Phẫu thuật nhổ răng khôn mọc lệch
- **Hình ảnh:** 4 ảnh
- **Phòng:** Phòng tiểu phẫu (Bác sĩ phẫu thuật)
- **Áp dụng cho:** Bệnh nhân có răng khôn mọc lệch/ngầm
- **Lưu ý:** Dịch vụ phức tạp, cần nhiều bước

---

## 4️⃣ DANH MỤC: Thẩm mỹ

### Service 8: Tẩy trắng răng
- **Giá:** 2,500,000 đ
- **Thời gian:** 60 phút
- **UI Template:** GENERAL
- **Mô tả:** Làm trắng răng Laser
- **Hình ảnh:** 4 ảnh
- **Phòng:** Phòng chuyên khoa 02 (Bác sĩ thẩm mỹ)
- **Áp dụng cho:** Bệnh nhân muốn làm trắng răng

### Service 9: Bọc răng sứ thẩm mỹ
- **Giá:** 5,000,000 đ
- **Thời gian:** 90 phút
- **UI Template:** GENERAL
- **Mô tả:** Phục hình răng bằng sứ cao cấp
- **Hình ảnh:** 4 ảnh
- **Phòng:** Phòng chuyên khoa 02 (Bác sĩ thẩm mỹ)
- **Áp dụng cho:** Bệnh nhân cần phục hình thẩm mỹ

---

## 5️⃣ DANH MỤC: Chỉnh nha

### Service 10: Niềng răng ⭐
- **Giá:** 30,000,000 đ
- **Thời gian:** 60 phút
- **UI Template:** ORTHO
- **Mô tả:** Chỉnh nha mắc cài toàn hàm
- **Hình ảnh:** 4 ảnh
- **Phòng:** Phòng chuyên khoa 01 (Bác sĩ chỉnh nha)
- **Áp dụng cho:** Bệnh nhân cần chỉnh nha
- **Lưu ý:** Dịch vụ đắt nhất, thời gian dài

---

## 📊 Phân Tích Theo Loại

### Theo UI Template Type:
```
GENERAL:  5 dịch vụ (Khám, X-quang, Trám, Điều trị tủy, Tẩy trắng, Bọc sứ)
SURGERY:  2 dịch vụ (Nhổ thường, Nhổ khôn)
PERIO:    1 dịch vụ (Lấy cao)
ORTHO:    1 dịch vụ (Niềng)
```

### Theo Giá:
```
100k:     1 dịch vụ (Khám)
200k:     1 dịch vụ (X-quang)
250k:     1 dịch vụ (Lấy cao)
300k:     2 dịch vụ (Trám, Nhổ thường)
1.5M:     1 dịch vụ (Điều trị tủy)
2M:       1 dịch vụ (Nhổ khôn)
2.5M:     1 dịch vụ (Tẩy trắng)
5M:       1 dịch vụ (Bọc sứ)
30M:      1 dịch vụ (Niềng)
```

### Theo Thời Gian:
```
15 phút:  1 dịch vụ (X-quang)
20 phút:  2 dịch vụ (Khám, Nhổ thường)
30 phút:  2 dịch vụ (Lấy cao, Trám)
45 phút:  1 dịch vụ (Nhổ khôn)
60 phút:  3 dịch vụ (Điều trị tủy, Tẩy trắng, Niềng)
90 phút:  1 dịch vụ (Bọc sứ)
```

---

## 🎯 Các Dịch Vụ Đặc Biệt Cần Chú Ý

### 1. Nhổ Răng Khôn (Wisdom Tooth Extraction)
- **Giá cao:** 2,000,000 đ
- **Thời gian dài:** 45 phút
- **Phức tạp:** Cần phẫu thuật
- **Phòng:** Phòng tiểu phẫu
- **Bác sĩ:** Chuyên khoa phẫu thuật
- **Quy trình:** Khám → X-quang → Nhổ
- **Lưu ý:** Có thể áp dụng cho 1 hoặc nhiều răng khôn

### 2. Niềng Răng (Orthodontics)
- **Giá rất cao:** 30,000,000 đ
- **Thời gian:** 60 phút (mỗi lần)
- **Phức tạp:** Quá trình dài (tháng/năm)
- **Phòng:** Phòng chuyên khoa 01
- **Bác sĩ:** Chuyên khoa chỉnh nha
- **Quy trình:** Khám → X-quang → Vệ sinh → Niềng
- **Lưu ý:** Cần theo dõi định kỳ

### 3. Tẩy Trắng Răng (Teeth Whitening)
- **Giá cao:** 2,500,000 đ
- **Thời gian:** 60 phút
- **Phức tạp:** Thẩm mỹ
- **Phòng:** Phòng chuyên khoa 02
- **Bác sĩ:** Chuyên khoa thẩm mỹ
- **Quy trình:** Khám → Vệ sinh → Tẩy trắng
- **Lưu ý:** Cần vệ sinh trước

### 4. Bọc Răng Sứ (Crown)
- **Giá cao:** 5,000,000 đ
- **Thời gian:** 90 phút
- **Phức tạp:** Phục hình thẩm mỹ
- **Phòng:** Phòng chuyên khoa 02
- **Bác sĩ:** Chuyên khoa thẩm mỹ
- **Lưu ý:** Có thể áp dụng cho 1 hoặc nhiều răng

---

## 💡 Phương Án Cho Biểu Đồ Răng

### Dịch Vụ Áp Dụng Cho Từng Răng:
✅ Trám răng sâu (300k)
✅ Nhổ răng thường (300k)
✅ Nhổ răng khôn (2M)
✅ Bọc răng sứ (5M)

### Dịch Vụ Không Áp Dụng Cho Từng Răng:
❌ Khám và tư vấn (100k) - Tổng quát
❌ Chụp X-quang (200k) - Tổng quát
❌ Lấy cao & đánh bóng (250k) - Tổng quát
❌ Điều trị tủy (1.5M) - Có thể áp dụng cho 1 răng nhưng thường là bước riêng
❌ Tẩy trắng (2.5M) - Toàn bộ hàm
❌ Niềng răng (30M) - Toàn bộ hàm

---

## 🔧 Phương Án Triển Khai

### Cách 1: Chỉ Hiển Thị Dịch Vụ Áp Dụng Cho Từng Răng
```
Khi bác sĩ nhấp vào một răng, chỉ hiển thị:
  - Trám răng sâu (300k)
  - Nhổ răng thường (300k)
  - Nhổ răng khôn (2M)
  - Bọc răng sứ (5M)
```

### Cách 2: Hiển Thị Tất Cả Dịch Vụ + Cho Phép Chọn
```
Khi bác sĩ nhấp vào một răng, hiển thị tất cả dịch vụ:
  - Nếu chọn dịch vụ tổng quát → Lưu với toothNumber = null
  - Nếu chọn dịch vụ cụ thể → Lưu với toothNumber = "8"
```

### Cách 3: Tách Biệt Dịch Vụ Tổng Quát và Cụ Thể
```
Biểu đồ răng: Chỉ cho phép chọn dịch vụ cụ thể
Danh sách riêng: Hiển thị dịch vụ tổng quát (Khám, X-quang, etc.)
```

---

## ✅ Khuyến Nghị

**Sử dụng Cách 1 (Chỉ Hiển Thị Dịch Vụ Áp Dụng Cho Từng Răng)**

Lý do:
- Rõ ràng, dễ hiểu
- Tránh nhầm lẫn
- Phù hợp với UX
- Dễ triển khai

**Dịch vụ tổng quát sẽ được thêm riêng trong danh sách "Các bước khác"**
