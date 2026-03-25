# Đề xuất Kiến trúc Hệ thống - PhongKham App

## 📊 PHÂN TÍCH HIỆN TRẠNG

### Cấu trúc hiện tại:
```
PhongKham/
├── clinic_backend/        # Spring Boot API (Java 17)
├── mobile_android/        # Android App (Java)
├── docs/                  # Documentation
└── prod/                  # Planning docs
```

### Vai trò hiện tại:
- **Backend**: REST API với JWT auth, PostgreSQL
- **Mobile Android**: App cho Patient + Staff (Admin, Doctor)
- **Web**: KHÔNG CÓ (chỉ có static HTML files trong backend)

---

## 🎯 ĐỀ XUẤT MỚI THEO YÊU CẦU

### Phân bổ Platform theo Vai trò:

| Vai trò | Platform | Lý do |
|---------|----------|-------|
| **Patient** | 📱 Mobile App ONLY | Di động, check-in QR, thông báo push |
| **Doctor** | 📱 Mobile App + 🌐 Web | App: khám bệnh di động; Web: xem hồ sơ chi tiết, báo cáo |
| **Admin** | 📱 Mobile App + 🌐 Web | App: quản lý nhanh; Web: dashboard, báo cáo, cấu hình |
| **Lễ Tân** | 🌐 Web ONLY | Máy tính cố định, tạo QR, quản lý lịch hẹn |

---

## 🏗️ KIẾN TRÚC ĐỀ XUẤT

```
┌─────────────────────────────────────────────────────────────┐
│                    CLINIC BACKEND API                        │
│              (Spring Boot + PostgreSQL)                      │
│         REST API + JWT Auth + Role-based Access              │
└─────────────────────────────────────────────────────────────┘
                            ▲
                            │
        ┌───────────────────┼───────────────────┐
        │                   │                   │
        ▼                   ▼                   ▼
┌──────────────┐   ┌──────────────┐   ┌──────────────┐
│   PATIENT    │   │   DOCTOR     │   │    ADMIN     │
│  Mobile App  │   │ Mobile + Web │   │ Mobile + Web │
│   (Java)     │   │              │   │              │
└──────────────┘   └──────────────┘   └──────────────┘
                            │
                            ▼
                   ┌──────────────┐
                   │   LỄ TÂN     │
                   │   Web Only   │
                   │  (React/Vue) │
                   └──────────────┘
```

---

## 📁 CẤU TRÚC THƯ MỤC ĐỀ XUẤT

```
PhongKham/
├── 🖥️ clinic_backend/              # Backend API (SHARED)
│   ├── src/main/java/
│   │   └── com.hcmute.clinic/
│   │       ├── controller/
│   │       │   ├── patient/      # Patient APIs
│   │       │   ├── doctor/       # Doctor APIs
│   │       │   ├── admin/        # Admin APIs
│   │       │   └── reception/    # Reception APIs ⭐ NEW
│   │       ├── service/
│   │       ├── repository/
│   │       └── entity/
│   └── pom.xml
│
├── 📱 mobile_android/              # Mobile App (Patient + Doctor + Admin)
│   ├── app/src/main/java/
│   │   └── com.hcmute.mobile_android/
│   │       ├── ui/
│   │       │   ├── patient/      # Patient screens
│   │       │   ├── doctor/       # Doctor screens
│   │       │   └── admin/        # Admin screens
│   │       ├── network/
│   │       └── util/
│   └── build.gradle.kts
│
├── 🌐 clinic_web/                  # Web App ⭐ NEW
│   ├── public/
│   ├── src/
│   │   ├── components/
│   │   │   ├── common/           # Shared components
│   │   │   ├── reception/        # Reception components
│   │   │   ├── doctor/           # Doctor web components
│   │   │   └── admin/            # Admin web components
│   │   ├── pages/
│   │   │   ├── reception/        # Reception pages
│   │   │   ├── doctor/           # Doctor pages
│   │   │   └── admin/            # Admin pages
│   │   ├── services/             # API services
│   │   ├── store/                # State management
│   │   └── utils/
│   ├── package.json
│   └── vite.config.js / webpack.config.js
│
├── 📚 docs/                        # Documentation
├── 🎯 prod/                        # Planning docs
└── 📄 README.md
```

---

## 🔧 TECH STACK ĐỀ XUẤT

### Backend (Không đổi)
```yaml
Framework: Spring Boot 3.2+
Language: Java 17
Database: PostgreSQL
Security: Spring Security + JWT
Build: Maven
```

### Mobile Android (Không đổi)
```yaml
Language: Java
UI: Material Design 3
Architecture: MVVM
Networking: Retrofit + OkHttp
QR: ZXing
Build: Gradle (Kotlin DSL)
```

### Web Frontend (MỚI) ⭐
```yaml
Framework: React 18+ hoặc Vue 3+
Language: TypeScript
UI Library: 
  - Material-UI (React) hoặc
  - Vuetify (Vue) hoặc
  - Ant Design
State Management:
  - Redux Toolkit (React) hoặc
  - Pinia (Vue)
HTTP Client: Axios
Build: Vite hoặc Webpack
Auth: JWT với localStorage/sessionStorage
```

**Khuyến nghị**: **React + TypeScript + Material-UI + Vite**
- Ecosystem lớn, nhiều thư viện
- TypeScript giúp type-safe
- Material-UI có sẵn components đẹp
- Vite build nhanh

---

## 📋 PHÂN TÍCH CHI TIẾT THEO VAI TRÒ

### 1. PATIENT - Mobile App Only 📱

**Tại sao chỉ Mobile?**
- ✅ Di động, check-in tại phòng khám
- ✅ Nhận thông báo push
- ✅ Quét QR code
- ✅ Xem lịch hẹn, phác đồ điều trị
- ❌ Không cần màn hình lớn
- ❌ Không cần nhập liệu nhiều

**Features (Mobile):**
- Login/Register với OTP
- QR Check-in (quét QR từ lễ tân)
- Dashboard: lịch hẹn, trạng thái queue
- Treatment plans & progress
- Notifications
- Profile management

---

### 2. DOCTOR - Mobile App + Web 📱🌐

**Tại sao cả hai?**

#### Mobile App 📱 (Ưu tiên)
**Use cases:**
- ✅ Khám bệnh di động (tablet tại phòng khám)
- ✅ Tra cứu bệnh nhân nhanh (QR scan)
- ✅ Cập nhật treatment plan
- ✅ Ghi chú nhanh trong quá trình khám
- ✅ Xem queue real-time

**Features:**
- QR Scanner để tra cứu patient
- Doctor Workflow với Odontogram
- Treatment Plan management
- Queue management
- Quick notes

#### Web App 🌐 (Bổ sung)
**Use cases:**
- ✅ Xem hồ sơ bệnh nhân chi tiết (màn hình lớn)
- ✅ Viết báo cáo y tế dài
- ✅ Xem ảnh X-quang, CT scan (màn hình lớn)
- ✅ Thống kê bệnh nhân của mình
- ✅ Lập kế hoạch điều trị phức tạp

**Features:**
- Patient records với search/filter
- Detailed medical history
- Image viewer (X-ray, photos)
- Treatment plan editor (advanced)
- Personal statistics dashboard
- Schedule management

---

### 3. ADMIN - Mobile App + Web 📱🌐

**Tại sao cả hai?**

#### Mobile App 📱 (Quản lý nhanh)
**Use cases:**
- ✅ Kiểm tra tình hình phòng khám
- ✅ Approve/reject requests
- ✅ Xem thông báo khẩn
- ✅ Quản lý doctor/room nhanh

**Features:**
- Dashboard overview
- Doctor management (CRUD)
- Room management (CRUD)
- Service management (CRUD)
- Quick approvals
- Notifications

#### Web App 🌐 (Quản lý chi tiết)
**Use cases:**
- ✅ Dashboard với charts, analytics
- ✅ Báo cáo doanh thu, thống kê
- ✅ Cấu hình hệ thống
- ✅ Quản lý users hàng loạt
- ✅ Export data, reports

**Features:**
- Advanced analytics dashboard
- Revenue reports với charts
- User management (bulk operations)
- System configuration
- Audit logs
- Data export (Excel, PDF)
- Email templates management

---

### 4. LỄ TÂN (RECEPTION) - Web Only 🌐

**Tại sao chỉ Web?**
- ✅ Làm việc tại quầy (máy tính cố định)
- ✅ Nhập liệu nhiều (thông tin bệnh nhân)
- ✅ Màn hình lớn để xem lịch
- ✅ In QR code, hóa đơn
- ✅ Quản lý appointments phức tạp
- ❌ Không cần di động

**Features (Web):**
- **Check-in Management:**
  - Tìm patient (phone/email/ID)
  - Xem appointments hôm nay
  - Tạo QR check-in cho patient
  - In QR code
  - Hiển thị mã số lớn trên màn hình

- **Appointment Management:**
  - Calendar view (day/week/month)
  - Create/Edit/Cancel appointments
  - Assign doctor & room
  - Send SMS reminders

- **Patient Registration:**
  - Register new patients
  - Update patient info
  - Upload documents

- **Queue Monitoring:**
  - Real-time queue display
  - Call patient to room
  - Transfer to X-ray

- **Payment & Invoicing:**
  - Create invoices
  - Process payments
  - Print receipts

---

## 🔄 API ENDPOINTS THEO VAI TRÒ

### Patient APIs (Mobile Only)
```
GET  /api/patient/me
GET  /api/patient/appointments
GET  /api/patient/treatment-plans
POST /api/patient/checkin/self-scan  ⭐ NEW
GET  /api/patient/checkin-status
GET  /api/patient/notifications
```

### Doctor APIs (Mobile + Web)
```
# Mobile & Web shared
GET  /api/doctor/patients
GET  /api/doctor/patient/{id}
GET  /api/doctor/queue
POST /api/doctor/treatment-plan
PUT  /api/doctor/treatment-plan/{id}

# Web only (advanced features)
GET  /api/doctor/statistics
GET  /api/doctor/reports
GET  /api/doctor/schedule
```

### Admin APIs (Mobile + Web)
```
# Mobile & Web shared
GET  /api/admin/doctors
POST /api/admin/doctors
GET  /api/admin/rooms
GET  /api/admin/services

# Web only (advanced features)
GET  /api/admin/analytics
GET  /api/admin/revenue-reports
GET  /api/admin/audit-logs
POST /api/admin/bulk-operations
GET  /api/admin/export/{type}
```

### Reception APIs (Web Only) ⭐ NEW
```
# Check-in
POST /api/reception/generate-checkin-qr
GET  /api/reception/appointments/today
GET  /api/reception/patients/search

# Appointment
POST /api/reception/appointments
PUT  /api/reception/appointments/{id}
DELETE /api/reception/appointments/{id}
GET  /api/reception/calendar

# Patient
POST /api/reception/patients
PUT  /api/reception/patients/{id}
GET  /api/reception/patients/{id}

# Queue
GET  /api/reception/queue
POST /api/reception/queue/call
POST /api/reception/queue/transfer

# Payment
POST /api/reception/invoices
POST /api/reception/payments
GET  /api/reception/invoices/{id}/print
```

---

## 🚀 IMPLEMENTATION ROADMAP

### Phase 1: Backend API cho Reception (1-2 tuần)
- [ ] Tạo ReceptionController với đầy đủ endpoints
- [ ] Implement ReceptionService
- [ ] Update SecurityConfig cho RECEPTION role
- [ ] Seed reception user
- [ ] Test APIs với Postman

### Phase 2: Web Frontend Setup (1 tuần)
- [ ] Init React/Vue project với TypeScript
- [ ] Setup routing (React Router / Vue Router)
- [ ] Setup state management (Redux / Pinia)
- [ ] Setup API client (Axios)
- [ ] Implement authentication flow
- [ ] Create layout components

### Phase 3: Reception Web App (2-3 tuần)
- [ ] Check-in page với QR generator
- [ ] Appointment calendar
- [ ] Patient search & registration
- [ ] Queue monitoring dashboard
- [ ] Payment & invoicing

### Phase 4: Doctor Web App (2 tuần)
- [ ] Patient records viewer
- [ ] Medical history detail
- [ ] Image viewer (X-ray)
- [ ] Advanced treatment plan editor
- [ ] Statistics dashboard

### Phase 5: Admin Web App (2 tuần)
- [ ] Analytics dashboard với charts
- [ ] Revenue reports
- [ ] User management
- [ ] System configuration
- [ ] Audit logs

### Phase 6: Mobile App Updates (1 tuần)
- [ ] Update patient app (quét QR từ lễ tân)
- [ ] Test integration với web

### Phase 7: Testing & Deployment (1 tuần)
- [ ] Integration testing
- [ ] E2E testing
- [ ] Performance optimization
- [ ] Deployment setup

**Total: 10-12 tuần**

---

## 💡 KHUYẾN NGHỊ KỸ THUẬT

### 1. Shared Components
Tạo thư viện components dùng chung giữa các role:
```
clinic_web/src/components/common/
├── Layout/
├── Table/
├── Form/
├── Chart/
├── Modal/
└── QRCode/
```

### 2. API Client
Tạo API client với interceptors cho JWT:
```typescript
// src/services/api.ts
import axios from 'axios';

const api = axios.create({
  baseURL: process.env.REACT_APP_API_URL,
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default api;
```

### 3. Role-based Routing
```typescript
// src/routes/index.tsx
const routes = [
  {
    path: '/reception',
    element: <ReceptionLayout />,
    children: [
      { path: 'checkin', element: <CheckInPage /> },
      { path: 'appointments', element: <AppointmentsPage /> },
      // ...
    ],
  },
  {
    path: '/doctor',
    element: <DoctorLayout />,
    children: [
      { path: 'patients', element: <PatientsPage /> },
      { path: 'statistics', element: <StatisticsPage /> },
      // ...
    ],
  },
  // ...
];
```

### 4. State Management
```typescript
// Redux Toolkit example
// src/store/slices/authSlice.ts
import { createSlice } from '@reduxjs/toolkit';

const authSlice = createSlice({
  name: 'auth',
  initialState: {
    user: null,
    token: null,
    role: null,
  },
  reducers: {
    setCredentials: (state, action) => {
      state.user = action.payload.user;
      state.token = action.payload.token;
      state.role = action.payload.role;
    },
    logout: (state) => {
      state.user = null;
      state.token = null;
      state.role = null;
    },
  },
});
```

---

## 📊 SO SÁNH TRƯỚC VÀ SAU

| Aspect | Trước | Sau |
|--------|-------|-----|
| **Patient** | Mobile App | Mobile App (không đổi) |
| **Doctor** | Mobile App | Mobile App + Web |
| **Admin** | Mobile App | Mobile App + Web |
| **Lễ Tân** | ❌ Không có | ✅ Web App |
| **Web Frontend** | ❌ Không có | ✅ React/Vue |
| **Tổng số app** | 1 (Mobile) | 2 (Mobile + Web) |

---

## ✅ LỢI ÍCH

1. **Phân tách rõ ràng**: Mỗi vai trò có platform phù hợp
2. **Tối ưu UX**: Mobile cho di động, Web cho nhập liệu nhiều
3. **Dễ maintain**: Code tách biệt theo vai trò
4. **Scalable**: Dễ thêm features cho từng role
5. **Professional**: Lễ tân có công cụ chuyên nghiệp

---

## 🎯 KẾT LUẬN

Đề xuất này giúp:
- ✅ Patient: Trải nghiệm mobile tốt nhất
- ✅ Doctor: Linh hoạt mobile + web
- ✅ Admin: Quản lý toàn diện
- ✅ Lễ Tân: Công cụ chuyên nghiệp tại quầy

**Next Step**: Bắt đầu với Phase 1 - Backend API cho Reception!
