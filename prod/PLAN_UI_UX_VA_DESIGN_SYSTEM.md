# Kế hoạch: UI/UX, Design System & Trải nghiệm đa vai trò

Tài liệu **bổ sung sản phẩm** cho [`PLAN_CHECKIN_VA_PHAC_DO.md`](./PLAN_CHECKIN_VA_PHAC_DO.md), [`PLAN_SMART_DENTAL_BO_SUNG.md`](./PLAN_SMART_DENTAL_BO_SUNG.md) và [`PLAN_AUTH_UI_VA_VAI_TRO.md`](./PLAN_AUTH_UI_VA_VAI_TRO.md). Mục tiêu: thống nhất **ngôn ngữ thiết kế**, **component**, **luồng UX** trước khi mở rộng từng màn hình — tránh scope creep nhưng cho dev/design **một north star** rõ ràng.

---

## 0. Đối chiếu codebase hiện tại (Android)

| Hiện trạng | Ghi chú |
|------------|---------|
| `mobile_android/.../values/colors.xml` | Bảng **Toothly** (teal, pearl, gold) — đang dùng cho Welcome/Login/Main |
| Màn QR / Phác đồ | Layout cơ bản, chưa có skeleton, chưa có dynamic QR / queue lớn / wayfinding |
| Web scanner | `scanner.html` — chưa đạt spec Kiosk (idle animation, âm thanh, privacy lỗi) |

**Hướng xử lý:** Coi bảng màu trong mục 1 dưới đây là **mục tiêu sản phẩm**; có thể **migrate dần** từ Toothly sang Primary Trust Blue + Secondary Calm Teal, hoặc **giữ gold/teal** cho nhánh “premium Toothly” nếu brand chốt — cần quyết định PO một dòng trong tài liệu này khi chốt brand.

---

## 1. Nền tảng thiết kế (Design System & Visual Language)

### 1.1. Bảng màu (Color Palette)

| Vai trò | Màu | Hex | Dùng cho |
|---------|-----|-----|----------|
| **Primary** | Trust Blue | `#1A56DB` | Nền tảng y tế: header, CTA chính, liên kết, nhận diện tin cậy |
| **Secondary** | Calm Teal | `#0E9F6E` | Thành công, đã check-in, trạng thái “ổn / hoàn tất”, ưu tiên sau X-Quang |
| **Alert / Danger** | Soft Coral | `#F05252` | Nợ phí, cảnh báo nghiêm trọng, lỗi blocking |
| **Warning** | Amber | `#FF5A1F` | Đến trễ, cảnh báo vừa phải, cần chú ý |
| **Background** | White | `#FFFFFF` | Card, vùng đọc nội dung chính |
| **Background** | Gray nhạt | `#F9FAFB` | Nền app / desktop, giảm mỏi mắt (đặc biệt màn bác sĩ dày dữ liệu) |

**Typography — màu chữ (tránh đen tuyền):**

| Vai trò | Gợi ý | Hex (ví dụ) |
|---------|--------|----------------|
| Heading / body đậm | Dark Teal / Navy xanh | `#1A3C40` (hoặc tông navy đậm tương đương) |
| Body phụ | Gray slate | `#64748B` trở lên (đồng bộ với design token) |

**Form & glass (auth / overlay):**

- Nền form: trắng **mờ** (opacity ~30–50%) trên background có hình ảnh → hiệu ứng **kính mờ** (frosted glass).
- Ô nhập: trắng đục hơn form (~70–80% opacity), **drop shadow** mềm, nhạt.
- Viền: **1px** `rgba(255, 255, 255, 0.5)` quanh form và input → viền kính bắt sáng.

### 1.2. Hình khối & đường viền (Shapes & Borders)

- **Border radius:** **16px – 24px** cho container form, input, nút chính — đồng điệu “mềm” với visual brand (răng / sông / nền).
- **Nút đăng nhập / đăng ký:** gradient **Vàng kim → Champagne** hoặc **Teal**; trạng thái hover/active: **glow** nhẹ (web/tablet).

### 1.3. Typography (Font)

| Lớp | Gợi ý |
|-----|--------|
| UI chính | Sans-serif hiện đại, bo nhẹ: **Poppins**, **Montserrat**, **Nunito** |
| Tiêu đề kiểu “Log in / Sign up” (nhấn cao cấp) | Serif: **Playfair Display** (dùng có chừng mực, đúng hierarchy) |

### 1.4. Micro-interactions

| Tương tác | Mô tả |
|-----------|--------|
| **Skeleton loading** | Thay spinner toàn màn bằng **skeleton** khi tải phác đồ / lịch / queue (mobile + web) |
| **Haptic & sound** | Rung nhẹ + âm “ting” thanh lịch khi **QR scan thành công**; có thể tái dùng khi **BN ưu tiên sau X-Quang** (push + UI Teal) |

---

## 2. App bệnh nhân (Mobile — “Người bạn đồng hành”)

Trải nghiệm: **an tâm**, **rõ ràng**, giảm cảm giác chờ.

| # | Màn / vùng | Component UI chính | Chi tiết UX |
|---|------------|-------------------|-------------|
| 1 | **Home / Dashboard** | Card lịch hẹn sắp tới (lớn, nổi bật); progress niềng/điều trị; **FAB** “Mở thẻ QR Check-in” | **Cá nhân hóa:** ví dụ *“Chào buổi sáng, anh A. Hôm nay anh có lịch nhổ răng lúc 10:00.”* |
| 2 | **QR Check-in (Dynamic)** | QR ~**1/2 màn hình**; làm mới token ~**mỗi 3 phút** (đồng bộ [`PLAN_SMART_DENTAL_BO_SUNG.md`](./PLAN_SMART_DENTAL_BO_SUNG.md) — QR JWT) | Trạng thái mạng: **xanh** Online / **vàng** offline cache; **Auto-brightness** tối đa khi màn QR; **pulse** quanh QR báo “đang active” |
| 3 | **Live Queue & Wayfinding** | Số thứ tự **cực lớn**; card trạng thái (Chờ / Vào phòng / Đi chụp X-Quang); **mini-map 2D** (Lễ tân → Phòng 1) | **Push real-time:** khi từ X-Quang về — UI **Teal**: *“Bạn được ưu tiên, vui lòng vào thẳng Phòng 1”* |
| 4 | **Phác đồ & Viện phí** | Timeline dọc: đã làm ✓ (xanh), đang làm (nhấp nháy nhẹ), sắp làm (xám) | Badge viện phí: **Đã thanh toán** (xanh) / **Cần thanh toán** (đỏ + số tiền). Khi BS thêm bước (vd. X-Quang): **rung** + CTA **thanh toán** (Apple Pay / Momo — phase sau) |

**Phụ thuộc kỹ thuật:** API queue status, push/polling, QR động — lần lượt Phase B4/D2 và Smart Dental.

---

## 3. Bác sĩ (Tablet / Desktop — “Trung tâm chỉ huy”)

Ưu tiên: **màn rộng**, **mật độ thông tin cao** nhưng không rối; **1 click**; ít gõ phím.

| # | Màn / vùng | Component UI chính | Chi tiết UX |
|---|------------|-------------------|-------------|
| 1 | **Hàng đợi đa luồng** | Sidebar cố định: danh sách BN; cards phân tầng: **Đang khám** (lớn nhất), **Ưu tiên (X-Quang về)** (màu/icon X), **Sắp tới** | **1-click switch:** chọn “BN A ưu tiên” → hỏi *“Lưu tạm BN B?”* → đổi context ngay |
| 2 | **Header bệnh nhân (Sticky)** | Tên, tuổi, sinh hiệu (HA, nhịp tim), **cảnh báo dị ứng** (đỏ nhấp nháy nếu có) | **Luôn ghim** trên cùng khi scroll — tránh nhầm hồ sơ |
| 3 | **Odontogram tương tác** | 32 răng **FDI**; chế độ **trẻ em (răng sữa)**; 5 mặt răng | Hover tooltip lịch sử; click mặt → popup: Sâu / Trám / Mẻ… |
| 4 | **Form lâm sàng động** | Theo dịch vụ: Nhổ (checkbox sinh hiệu, **e-sign**), Chỉnh nha (before/after grid), Kê đơn (preset 1 click) | **Morphing UI** — ẩn field không liên quan; form sạch |

---

## 4. Lễ tân (Desktop Dashboard — “Tháp điều khiển”)

Tập trung **lưu lượng** và **xử lý ngoại lệ**.

### 4.0. Vị trí triển khai (chốt sản phẩm)

- **Toàn bộ UI lễ tân** (floor plan, exception inbox, thu ngân, nhật ký quét lỗi…) triển khai **trong cùng ứng dụng web Admin** — một **portal nội bộ**, không tách domain/product riêng.
- Điều hướng: menu **“Lễ tân”** / **“Tiếp nhận”** trong shell Admin; có thể gán quyền **`ROLE_RECEPTIONIST`** (chỉ thấy module lễ tân + thu ngân) hoặc dùng tài khoản **Admin** đầy đủ — tùy chính sách phòng khám.
- **Thiết bị vật lý** tại quầy (PC, màn hình phụ, máy quét): chỉ là **client** mở URL portal đó (hoặc trang kiosk full-screen) — vẫn thuộc **nhánh Admin**, không coi là “app thứ tư” độc lập.

| # | Màn / vùng | Component UI chính | Chi tiết UX |
|---|------------|-------------------|-------------|
| 1 | **Global Floor Plan** | Bird-eye các phòng: có BN **sáng**, trống **xám**; phòng X-Quang hiển thị **số người chờ** | Giúp thấy **bottleneck** để điều hướng |
| 2 | **Exception Inbox** | List có ngăn: Lỗi quét QR, Chưa đóng tiền, VIP… | Quick actions: **[In QR giấy] [Bỏ qua] [Nhắc nợ]**; khi kiosk **đỏ** → popup lý do trên máy lễ tân (không show chi tiết nhạy cảm trên kiosk) |
| 3 | **Thu ngân & thanh toán** | Split: trái chỉ định BS, phải **QR thanh toán động** | Đồng bộ **màn hình hướng khách** (Momo/VNPay) |

---

## 5. Máy quét / Kiosk (“Smart Scanner”)

Điểm chạm vật lý đầu tiên — **rõ ràng, to, có âm thanh**; bảo vệ privacy.

| Trạng thái | UI / Âm thanh |
|------------|----------------|
| **Idle** | Animation loop hướng dẫn đưa điện thoại vào (GIF/video ngắn) |
| **Thành công** | Màn **xanh lá** ~1s, tick lớn; loa: *“Check-in thành công. Số thứ tự của bạn là N”* |
| **Thất bại** | Màn **đỏ**, icon dừng; loa: *“Vui lòng liên hệ quầy Lễ tân”* — **không** hiển thị lý do chi tiết trên kiosk |

**Đồng bộ:** [`PLAN_CHECKIN_VA_PHAC_DO.md`](./PLAN_CHECKIN_VA_PHAC_DO.md) mục thiết bị quét; cần nâng `scanner.html` hoặc app kiosk theo bảng trên.

---

## 6. Kiến trúc UI (tránh scope creep)

### 6.1. Component-Driven Development

- Thư viện nội bộ (tên gợi ý): **DentalUIKit** / **ToothlyKit** — chứa component tái sử dụng:
  - `ToothIcon`, `QueueCard`, `AlertBadge`, `TimelineStep`, `PatientHeaderSticky`, v.v.
- **Android:** `composables` hoặc custom views + theme Material 3 token map về bảng màu §1.
- **Web:** một codebase portal **Staff** gồm **Bác sĩ** + **Admin** + **Lễ tân** (module lễ tân nằm trong Admin); design token (CSS variables) thống nhất.

### 6.2. Form Schema Builder (Dynamic Clinical Forms)

- Form bác sĩ **không hardcode toàn bộ** — sinh từ **JSON schema** (backend hoặc static bundle versioned).
- Lợi ích: thêm dịch vụ mới (vd. tẩy trắng) **không cần release app** (hoặc chỉ cần update schema version).
- **Bước kỹ thuật tiếp theo (chốt với team):**
  1. Draft **JSON Schema** cho 1–2 flow (vd. “Nhổ răng” + “Khám tổng quát”).
  2. Renderer web trước; mobile BS sau (hoặc ngược lại — theo roadmap).

### 6.3. Bước triển khai đề xuất (không ngợp)

| Ưu tiên | Việc |
|---------|------|
| P0 | Token màu + typography + radius trong `colors.xml` / theme + document map sang Trust Blue / Teal |
| P1 | Patient: Home card lịch + FAB QR; nâng màn QR (size, pulse, brightness); skeleton cho Phác đồ |
| P2 | Queue lớn + trạng thái (sau API B4/D2) |
| P3 | Kiosk scanner theo §5 |
| P4 | Web BS: queue đa cột + sticky header |
| P5 | Odontogram + JSON form (song song Smart Dental) |

---

## 7. Liên kết tài liệu

| Tài liệu | Nội dung liên quan UI |
|----------|------------------------|
| [`PLAN_CHECKIN_VA_PHAC_DO.md`](./PLAN_CHECKIN_VA_PHAC_DO.md) | Wireframe patient, scanner |
| [`PLAN_SMART_DENTAL_BO_SUNG.md`](./PLAN_SMART_DENTAL_BO_SUNG.md) | QR động, queue state machine, real-time |
| [`PLAN_AUTH_UI_VA_VAI_TRO.md`](./PLAN_AUTH_UI_VA_VAI_TRO.md) | Auth Toothly, flow OTP |
| [`KIEN_TRUC_VA_LOGIC.md`](./KIEN_TRUC_VA_LOGIC.md) | Kiến trúc tổng — bổ sung mục UI kit khi chốt |

---

*Tài liệu này ghi nhận yêu cầu UI/UX từ stakeholder; cập nhật khi chốt brand (Toothly vs Trust Blue), khi có mockup Figma, hoặc khi rút gọn scope theo sprint.*
