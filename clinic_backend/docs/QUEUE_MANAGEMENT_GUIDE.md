# Hướng dẫn Quản lý Hàng đợi - Quick Reference

## 🎯 Khi nào dùng chức năng nào?

### Tình huống 1: Bác sĩ gọi bệnh nhân vào nhưng không có mặt
```
✅ Dùng: SKIP (Lùi 1 người)
📍 Endpoint: POST /api/queue/{id}/skip
📱 UI: Nút "Lùi 1 người" trong màn hình bác sĩ
```

**Kết quả:**
- Bệnh nhân hiện tại → quay lại chờ với priority cao
- Người tiếp theo → tự động được gọi vào phòng
- Gửi thông báo cho người tiếp theo

---

### Tình huống 2: Bệnh nhân đang chờ muốn nhường lượt
```
✅ Dùng: DELAY (Lùi xuống 1 vị trí)
📍 Endpoint: POST /api/queue/{id}/delay
📱 UI: Nút "Nhường lượt" ở quầy lễ tân
```

**Kết quả:**
- Hoán đổi số thứ tự với người sau
- Không tự động gọi ai vào phòng

---

## 📊 Bảng so sánh nhanh

| | SKIP | DELAY |
|---|---|---|
| **Dùng khi** | Bệnh nhân đang khám | Bệnh nhân đang chờ |
| **Status** | IN_PROGRESS | WAITING |
| **Ai dùng** | Bác sĩ | Lễ tân |
| **Tự động gọi người tiếp theo** | ✅ Có | ❌ Không |
| **Thay đổi priority** | ✅ +5 | ❌ Không |

---

## 🔧 API Examples

### Skip (Lùi bệnh nhân đang khám)
```bash
curl -X POST http://localhost:8080/api/queue/123/skip \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Delay (Lùi bệnh nhân đang chờ)
```bash
curl -X POST http://localhost:8080/api/queue/123/delay \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## 📚 Tài liệu chi tiết

Xem: [QUEUE_DELAY_VS_SKIP.md](./QUEUE_DELAY_VS_SKIP.md)
