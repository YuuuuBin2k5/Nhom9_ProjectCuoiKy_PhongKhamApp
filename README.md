# 🦷 Nhom9 PhongKham App - Clinic Management System

**Hệ thống quản lý phòng khám nha khoa hiện đại với ứng dụng Android và backend Spring Boot**
**Modern Dental Clinic Management System with Android Application and Spring Boot Backend**

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)](https://github.com/YuuuuBin2k5/Nhom9_ProjectCuoiKy_PhongKhamApp)
[![Android](https://img.shields.io/badge/Android-API%2024+-green)](https://developer.android.com)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2+-blue)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

---

## 📱 **Tổng quan dự án | Project Overview**

Dự án **PhongKham App** là một giải pháp chuyển đổi số toàn diện cho phòng khám nha khoa, được thiết kế bởi **Nhóm 9**. Hệ thống giúp tối ưu hóa quy trình từ khâu tiếp đón đến điều trị chuyên sâu.

The **PhongKham App** project is a comprehensive digital transformation solution for dental clinics, designed by **Team 9**. The system optimizes workflows from reception to advanced clinical treatment.

- **📱 Mobile Android App**: Ứng dụng dành cho Bệnh nhân & Đội ngũ y tế (Patient & Staff application).
- **🖥️ Spring Boot Backend**: API Server mạnh mẽ với bảo mật JWT (Robust API server with JWT security).
- **🔄 Real-time Features**: Quản lý hàng đợi và thông báo tức thời (Live Queue management & instant notifications).
- **📊 Admin Dashboard**: Quản lý toàn diện bác sĩ, dịch vụ và cơ sở vật chất (Full management of doctors, services, and facilities).

---

## 🚀 **Tính năng chính | Key Features**

### **👥 Cho Bệnh nhân | For Patients**
- ✅ **Đăng ký & Xác thực OTP**: Bảo mật tài khoản qua mã OTP gửi về điện thoại.
- ✅ **Check-in thông minh**: Tự động check-in qua QR code tại phòng khám.
- ✅ **Theo dõi hàng đợi**: Xem vị trí và thời gian chờ dự kiến trong hàng đợi real-time.
- ✅ **Hồ sơ bệnh án điện tử**: Xem lịch sử khám, đơn thuốc và phác đồ điều trị mọi lúc.
- ✅ **Quản lý lịch hẹn**: Đặt và theo dõi lịch hẹn với bác sĩ chuyên khoa.

### **👨‍⚕️ Cho Nhân viên Y tế | For Staff & Doctors**
- ✅ **Quét mã QR**: Tiếp đón bệnh nhân nhanh chóng bằng ZXing scanner tích hợp CameraX.
- ✅ **Quản lý hàng đợi**: Điều phối bệnh nhân, chuyển phòng (X-quang, Tiểu phẫu) với real-time SSE.
- ✅ **Lập phác đồ điều trị**: Sử dụng các template chuyên nghiệp để thiết kế quy trình điều trị.
- ✅ **Giao diện Odontogram**: Tương tác trực tiếp trên sơ đồ răng để ghi chú tình trạng.
- ✅ **Kê đơn thuốc**: Tạo và quản lý đơn thuốc điện tử với validation chặt chẽ.
- ✅ **Truy cập bệnh án**: Tra cứu nhanh lịch sử và thông tin lâm sàng của bệnh nhân.
- ✅ **Chat với bệnh nhân**: Tư vấn và trả lời câu hỏi qua hệ thống chat real-time.
### **👨‍💼 Cho Quản trị viên | For Admin**
- ✅ **Quản trị Dashboard**: Theo dõi số liệu thống kê và hiệu suất hoạt động của phòng khám.
- ✅ **Quản lý Dịch vụ**: Cập nhật danh mục kỹ thuật, đơn giá và hình ảnh minh họa.
- ✅ **Quản lý Đội ngũ**: Thêm mới và kiểm soát trạng thái hoạt động của bác sĩ, lễ tân.
- ✅ **Quản lý Cơ sở**: Theo dõi tình trạng các phòng khám và trang thiết bị.
- ✅ **Quản lý Danh mục**: Tổ chức dịch vụ theo categories với dialog thêm/sửa/xóa.
- ✅ **Báo cáo Doanh thu**: Thống kê doanh thu theo thời gian và dịch vụ.
- ✅ **Quản lý Template**: Tạo và quản lý template phác đồ điều trị.

---

## 🛠️ **Tech Stack**

### **📱 Android App**
- **Language**: Java 11 / Android SDK (Min API 26, Target API 36)
- **UI Framework**: Material Design 3 + ConstraintLayout
- **Networking**: Retrofit 2.9.0 + OkHttp 4.12.0 (Logging Interceptor)
- **QR System**: ZXing Core 3.5.2 + ZXing Android Embedded 4.3.0
- **Image Loading**: Glide + PhotoView 2.3.0 (Pinch-to-zoom)
- **Camera**: CameraX 1.3.1 (Core, Camera2, Lifecycle, View)
- **Security**: AndroidX Security Crypto 1.1.0-alpha06
- **Real-time**: Firebase Realtime Database (BOM 32.7.0)
- **Charts & Analytics**: MPAndroidChart 3.1.0
- **Animations**: Lottie 6.3.0
- **Export**: Apache POI 5.2.3 (Excel) + iText7 7.2.5 (PDF)
- **Architecture**: Activity-based Pattern với Fragment Support

### **🖥️ Backend API**
- **Framework**: Spring Boot 3.2.4 (Java 17)
- **Security**: Spring Security + JWT (JJWT 0.11.5)
- **Database**: PostgreSQL với Spring Data JPA/Hibernate
- **Build System**: Maven (với Maven Wrapper)
- **Validation**: Spring Boot Starter Validation
- **Utilities**: Lombok (Annotation Processing)
- **Push Notification**: Firebase Admin SDK 9.2.0
- **Real-time**: Server-Sent Events (SSE) + WebSocket Support
- **AOP**: AspectJ cho Audit Logging
- **File Upload**: Multipart Support (Max 10MB)

---

## 📂 **Cấu trúc dự án | Project Structure**

```text
Nhom9_ProjectCuoiKy_PhongKhamApp/
├── 📱 mobile_android/                    # Android Mobile Application
│   ├── app/
│   │   ├── src/main/java/com/hcmute/mobile_android/
│   │   │   ├── adapters/                # RecyclerView Adapters (Queue, Treatment, etc.)
│   │   │   ├── network/                 # Retrofit API Service & Models
│   │   │   ├── ui/                      # Activities & Fragments
│   │   │   │   ├── activities/          # Main Activities (Login, QR Scanner, etc.)
│   │   │   │   │   └── staff/           # Staff-specific Activities
│   │   │   │   └── fragments/           # UI Fragments (Dashboard, Settings)
│   │   │   ├── util/                    # Utilities (TokenManager, QRCodeHelper)
│   │   │   └── services/                # Background Services
│   │   ├── src/main/res/                # Material UI Resources & Layouts
│   │   │   ├── layout/                  # XML Layouts
│   │   │   ├── drawable/                # Icons & Graphics
│   │   │   └── values/                  # Strings, Colors, Themes
│   │   └── build.gradle.kts             # Android Build Configuration (Kotlin DSL)
│   ├── gradle/                          # Gradle Wrapper
│   ├── build.gradle.kts                 # Root Build Configuration
│   ├── settings.gradle.kts              # Project Settings
│   └── local.properties                 # Local Configuration (Backend URL)
│
├── 🖥️ clinic_backend/                    # Spring Boot REST API
│   ├── src/main/java/com/hcmute/clinic/
│   │   ├── annotation/                  # Custom Annotations (@Auditable)
│   │   ├── aspect/                      # AOP Aspects (AuditAspect)
│   │   ├── config/                      # Security, CORS, WebSocket, DataSeed
│   │   ├── controller/                  # REST Controllers (30+ endpoints)
│   │   ├── dto/                         # Data Transfer Objects
│   │   ├── entity/                      # JPA Entities (Patient, Doctor, etc.)
│   │   ├── enums/                       # Enumerations (Status, Roles)
│   │   ├── exception/                   # Custom Exception Handlers
│   │   ├── repository/                  # Spring Data JPA Repositories
│   │   ├── security/                    # JWT Authentication & Filters
│   │   ├── service/                     # Business Logic Services
│   │   └── util/                        # Utility Classes
│   ├── src/main/resources/
│   │   ├── db/                          # Database Migration Scripts
│   │   ├── static/                      # Static Resources
│   │   ├── application.yml              # Main Configuration
│   │   └── application-local.yml.example # Local Config Template
│   ├── docs/                            # Technical Documentation
│   │   ├── AVATAR_IMPLEMENTATION_SUMMARY.md
│   │   ├── PRESCRIPTION_FIX_COMPLETE.md
│   │   ├── PRESCRIPTION_FIX_TEST_GUIDE.md
│   │   └── QUEUE_MANAGEMENT_GUIDE.md
│   ├── uploads/                         # File Upload Directory
│   ├── pom.xml                          # Maven Build Configuration
│   └── mvnw.cmd                         # Maven Wrapper (Windows)
│
├── 📚 prod/                              # Production Documentation
│   └── USERSTORY.md                     # User Stories & Requirements
├── 📄 README.md                          # Project Overview Documentation
├── 📄 .gitignore                         # Git Ignore Rules
└── 📄 build.gradle.kts                   # Root Gradle Configuration
---

## 🎯 **Điểm nổi bật kỹ thuật | Technical Highlights**

### **🔄 Real-time Communication**
- **Server-Sent Events (SSE)**: Cập nhật hàng đợi và thông báo real-time không cần polling
- **Firebase Realtime Database**: Đồng bộ dữ liệu tức thời giữa các thiết bị
- **Async Processing**: Xử lý bất đồng bộ cho các tác vụ nặng

### **📱 Mobile Architecture**
- **Retrofit + OkHttp**: HTTP client mạnh mẽ với logging interceptor
- **Glide**: Lazy loading và caching hình ảnh hiệu quả
- **CameraX**: Modern camera API cho QR scanning
- **Custom Views**: OdontogramView - Interactive dental chart
- **RecyclerView Adapters**: Tối ưu hiển thị danh sách lớn

### **🖥️ Backend Architecture**
- **Layered Architecture**: Controller → Service → Repository pattern
- **Spring Data JPA**: ORM với Hibernate, tự động tạo queries
- **Lombok**: Giảm boilerplate code với annotations
- **Exception Handling**: Centralized error handling với @ControllerAdvice
- **Data Seeding**: Tự động khởi tạo dữ liệu mẫu khi chạy lần đầu

### **🔐 Security Implementation**
- **JWT Filter Chain**: Custom filter cho authentication
- **Password Encoder**: BCrypt với strength 10
- **CORS Configuration**: Flexible cross-origin setup
- **Role-based Authorization**: Method-level security với @PreAuthorize

---

## 🚀 **Cài đặt và Chạy | Setup & Run**
---

## 🚀 **Cài đặt và Chạy | Setup & Run**

### **📋 Yêu cầu hệ thống | Prerequisites**
- **Java 17** (cho Backend)
- **PostgreSQL 12+** (Database)
- **Android Studio** (Hedgehog 2023.1.1+ hoặc mới hơn)
- **Maven 3.6+** (hoặc dùng wrapper `./mvnw`)
- **Android SDK Min API 26+** (Target API 36)
- **Git** (Version Control)

### **🖥️ Backend Setup**

#### **Bước 1: Cài đặt Database**
```sql
-- Tạo database PostgreSQL
CREATE DATABASE phongkham;

-- Kiểm tra kết nối
\c phongkham
```

#### **Bước 2: Cấu hình Backend**
Tạo file `clinic_backend/src/main/resources/application-local.yml` (hoặc chỉnh sửa `application.yml`):

```yaml
server:
  port: 8081
  address: 0.0.0.0  # Cho phép truy cập từ mạng LAN

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/phongkham
    username: postgres
    password: your_password  # Thay đổi password của bạn
  jpa:
    hibernate:
      ddl-auto: update  # Tự động tạo/cập nhật schema
    show-sql: false

app:
  jwt:
    secret: "toothly-dev-jwt-secret-min-32-characters-long!!"
    expiration-ms: 86400000  # 24 giờ
  upload:
    dir: uploads  # Thư mục lưu file upload
```

#### **Bước 3: Chạy Backend Server**
```bash
cd clinic_backend

# Sử dụng Maven Wrapper (khuyến nghị)
./mvnw clean spring-boot:run

# Hoặc nếu đã cài Maven
mvn clean spring-boot:run
```

**Kiểm tra server đã chạy:**
- Mở browser: `http://localhost:8081/api/auth/login`
- Nếu thấy response JSON → Server đã sẵn sàng ✅
- Server sẽ tự động khởi tạo dữ liệu mẫu (DataSeed) khi chạy lần đầu

### **📱 Android App Setup**

#### **Cách 1: Tự động phát hiện IP (Khuyến nghị)**

Android app sẽ tự động phát hiện IP máy tính chạy backend. Chỉ cần:

1. **Đảm bảo cùng mạng WiFi**: Máy tính và điện thoại/emulator phải cùng mạng LAN (192.168.x.x, 10.x.x.x).
2. **Build & Run**: Mở thư mục `mobile_android` bằng Android Studio và chạy app.
3. **Kiểm tra Log**: Xem log build để xác nhận API URL:
   ```
   [Toothly] API_BASE_URL → http://192.168.1.6:8081/
   ```

#### **Cách 2: Cấu hình thủ công (Nếu cần)**

Tạo file `mobile_android/local.properties`:

```properties
# Cho Emulator (Android Studio)
backend.host=10.0.2.2

# Hoặc chỉ định IP cụ thể cho thiết bị thật
backend.host=192.168.1.6

# Thay đổi port nếu cần (mặc định 8081)
backend.port=8081
```

**Cách tìm IP máy tính:**
```bash
# Windows
ipconfig | findstr IPv4

# macOS/Linux
ifconfig | grep "inet "
```

#### **Bước 3: Build & Install**
```bash
cd mobile_android

# Build APK
./gradlew assembleDebug

# Install vào thiết bị đã kết nối
./gradlew installDebug
```

#### **Lưu ý quan trọng**
- ✅ Đảm bảo máy tính và điện thoại/emulator cùng mạng WiFi
- ✅ Tắt Firewall hoặc cho phép port 8081
- ✅ Server backend phải chạy trước khi mở app
- ✅ Với Emulator: Dùng IP `10.0.2.2` thay vì `localhost`
---

## 🔧 **Troubleshooting | Xử lý sự cố**

### **Backend không khởi động được**
```bash
# Kiểm tra PostgreSQL đã chạy chưa
sudo systemctl status postgresql  # Linux
brew services list                # macOS

# Kiểm tra port 8081 có bị chiếm không
netstat -ano | findstr :8081      # Windows
lsof -i :8081                     # macOS/Linux
```

### **Android App không kết nối được Backend**
1. Kiểm tra log build để xem API URL: `[Toothly] API_BASE_URL → ...`
2. Ping IP từ điện thoại: Mở browser truy cập `http://<IP>:8081/api/auth/login`
3. Tắt Firewall hoặc cho phép port 8081
4. Đảm bảo cùng mạng WiFi (không dùng mobile data)

### **QR Scanner không hoạt động**
- Cấp quyền Camera trong Settings → Apps → PhongKham App → Permissions
- Kiểm tra CameraX dependencies trong `build.gradle.kts`
- Test với QRTestActivity để debug

### **Database Migration Issues**
```yaml
# Trong application.yml, thay đổi ddl-auto nếu cần
spring:
  jpa:
    hibernate:
      ddl-auto: create  # Tạo lại schema (XÓA dữ liệu cũ!)
      # ddl-auto: update  # Cập nhật schema (Giữ dữ liệu)
```

---

## 📚 **Tài liệu bổ sung | Additional Documentation**

### **📖 Technical Documentation**
- **User Stories**: Xem file `prod/USERSTORY.md` để hiểu chi tiết các use case
- **Queue Management**: Xem `clinic_backend/docs/QUEUE_MANAGEMENT_GUIDE.md` cho hướng dẫn quản lý hàng đợi
- **Prescription Fix**: Xem `clinic_backend/docs/PRESCRIPTION_FIX_COMPLETE.md` cho lịch sử fix bugs
- **Avatar Implementation**: Xem `clinic_backend/docs/AVATAR_IMPLEMENTATION_SUMMARY.md` cho chi tiết avatar system
- **Database Schema**: Xem các Entity classes trong `clinic_backend/src/main/java/com/hcmute/clinic/entity/`

### **🧪 API Testing**
```bash
# Test Login API
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@gmail.com","password":"123456"}'

# Test với JWT Token
curl -X GET http://localhost:8081/api/patients/me \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### **📊 Database Schema Overview**
```sql
-- Core Entities
User (id, email, password, role, ...)
Patient (id, user_id, firstName, lastName, phone, ...)
Doctor (id, user_id, firstName, lastName, specialization, ...)
Appointment (id, patient_id, doctor_id, appointmentDate, status, ...)
Queue (id, appointment_id, queueNumber, status, priority, ...)
TreatmentPlan (id, patient_id, appointment_id, status, ...)
Prescription (id, appointment_id, doctor_id, medications, ...)
Service (id, name, price, category, ...)
ClinicRoom (id, name, type, status, ...)
```

---

## 🔑 **Tài khoản dùng thử | Demo Credentials**nh (192.168.x.x, 10.x.x.x) để kết nối.

1. **Build & Run**: Mở thư mục `mobile_android` bằng Android Studio và chạy app.
2. **Kiểm tra Log**: Xem log build để xác nhận API URL:
   ```
   [Toothly] API_BASE_URL → http://192.168.1.10:8081/
   ```

#### **Cách 2: Cấu hình thủ công (Nếu cần)**
Tạo file `local.properties` trong thư mục root project:

```properties
# Cho Emulator (Android Studio)
backend.host=EMULATOR

# Hoặc chỉ định IP cụ thể cho thiết bị thật
backend.host=192.168.1.10

# Hoặc chỉ định URL đầy đủ
backend.base.url=http://192.168.1.10:8081/

# Thay đổi port nếu cần (mặc định 8081)
backend.port=8081
```

#### **Lưu ý quan trọng**
- Đảm bảo máy tính và điện thoại/emulator cùng mạng WiFi
- Tắt Firewall hoặc cho phép port 8081
- Server backend phải chạy trước khi mở app

---

## 🔑 **Tài khoản dùng thử | Demo Credentials**

| Role | Email | Password |
| :--- | :--- | :--- |
| **Admin** | `admin@gmail.com` | `123456` |
| **Doctor** | `doc01@gmail.com` | `123456` |
| **Patient** | `patient01@gmail.com` | `123456` |

---

## 🔧 **API Documentation (Summary)**

Backend cung cấp RESTful API với 30+ controllers, bao gồm:

### **🔐 Authentication & Authorization**
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| **POST** | `/api/auth/login` | Đăng nhập hệ thống (JWT Token) |
| **POST** | `/api/auth/register` | Đăng ký tài khoản bệnh nhân |
| **POST** | `/api/auth/refresh` | Làm mới Access Token |
| **POST** | `/api/auth/otp/request` | Yêu cầu mã OTP |
| **POST** | `/api/auth/otp/verify` | Xác thực mã OTP |

### **👤 Patient Management**
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| **GET** | `/api/patients/me` | Lấy thông tin cá nhân bệnh nhân |
| **PUT** | `/api/patients/me` | Cập nhật thông tin cá nhân |
| **GET** | `/api/patients/me/appointments` | Xem lịch hẹn của tôi |
| **GET** | `/api/patients/me/medical-records` | Xem hồ sơ bệnh án |

### **🏥 Check-in & Queue Management**
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| **POST** | `/api/checkin/generate-qr` | Tạo mã QR check-in |
| **POST** | `/api/checkin/scan` | Quét mã QR tiếp đón (Staff) |
| **GET** | `/api/checkin/my-status` | Kiểm tra trạng thái hàng đợi |
| **GET** | `/api/queue/current` | Xem hàng đợi hiện tại (Staff) |
| **POST** | `/api/queue/{id}/skip` | Lùi 1 người (Doctor) |
| **POST** | `/api/queue/{id}/delay` | Nhường lượt (Receptionist) |
| **PATCH** | `/api/queue/{id}/status` | Cập nhật trạng thái bệnh nhân |

### **💊 Treatment & Prescription**
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| **GET** | `/api/treatment-plans/my` | Xem phác đồ điều trị của tôi |
| **GET** | `/api/treatment-plans/by-appointment/{id}` | Lấy phác đồ theo lịch hẹn |
| **POST** | `/api/treatment-plans` | Tạo phác đồ điều trị (Doctor) |
| **GET** | `/api/treatment-plans/templates` | Lấy danh sách template |
| **POST** | `/api/prescriptions` | Kê đơn thuốc (Doctor) |
| **GET** | `/api/prescriptions/{id}` | Xem chi tiết đơn thuốc |

### **👨‍💼 Admin Management**
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| **GET** | `/api/admin/doctors` | Danh sách bác sĩ |
| **POST** | `/api/admin/doctors` | Thêm bác sĩ mới |
| **PATCH** | `/api/admin/doctors/{id}/status` | Cập nhật trạng thái bác sĩ |
| **GET** | `/api/admin/services` | Quản lý dịch vụ |
| **POST** | `/api/admin/services` | Thêm dịch vụ mới |
| **GET** | `/api/admin/rooms` | Quản lý phòng khám |
| **GET** | `/api/admin/revenue` | Báo cáo doanh thu |
| **GET** | `/api/admin/reports` | Báo cáo thống kê |

### **📊 Real-time Features**
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| **GET** | `/api/queue/sse` | Server-Sent Events cho hàng đợi |
| **GET** | `/api/notifications/sse` | SSE cho thông báo real-time |

### **📁 File Upload**
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| **POST** | `/api/upload/avatar` | Upload avatar (Max 10MB) |
| **POST** | `/api/upload/document` | Upload tài liệu y tế |
| **GET** | `/uploads/{filename}` | Truy cập file đã upload |

*Tất cả API (trừ `/api/auth/*`) yêu cầu JWT Token trong header: `Authorization: Bearer <token>`*

---

## 🔒 **Bảo mật | Security**

### **Backend Security**
- **JWT Authentication**: Stateless token-based authentication với expiration 24h
- **Password Encryption**: BCrypt hashing cho mật khẩu người dùng
- **CORS Configuration**: Cấu hình CORS cho phép cross-origin requests từ mobile app
- **Role-Based Access Control (RBAC)**: Phân quyền chặt chẽ theo vai trò (ADMIN, DOCTOR, PATIENT)
- **Spring Security**: Bảo vệ endpoints với SecurityFilterChain

### **Mobile Security**
- **AndroidX Security Crypto**: Mã hóa SharedPreferences để lưu trữ token an toàn
- **HTTPS Ready**: Hỗ trợ kết nối HTTPS cho production
- **Token Management**: Tự động refresh token khi hết hạn
- **OTP Verification**: Xác thực 2 lớp qua mã OTP (TTL 5 phút, max 5 attempts)

### **Data Protection**
- **Input Validation**: Spring Boot Starter Validation cho tất cả request
- **SQL Injection Prevention**: JPA/Hibernate parameterized queries
- **XSS Protection**: Content Security Policy headers

---

## 🤝 **Thành viên phát triển | Team Members - Nhóm 9**

| Thành viên | Vai trò | GitHub | Đóng góp chính |
| :--- | :--- | :--- | :--- |
| **Đào Nguyễn Nhật Anh** | Frontend & Backend Developer | [@YuuuuBin2k5](https://github.com/YuuuuBin2k5) | Android UI, Spring Boot API |
| **Nguyễn Đoàn Trường Vĩ** | Team Leader & Backend & QA | [@truongvi-ute](https://github.com/truongvi-ute) | Architecture, Database, Testing |
| **Trần Hoàng Phúc Quân** | Frontend & UI/UX & QA | [@PhucQuan](https://github.com/PhucQuan) | Mobile UI/UX, Testing |
| **Nguyễn Hồng Phúc** | UI/UX Designer & QA Tester | [@NHP39](https://github.com/NHP39) | Design System, QA |

---

## 🙏 **Lời cảm ơn | Acknowledgments**
- **🏫 Trường Đại học Công Nghệ Kỹ Thuật TP.HCM (HUTECH)** - Môi trường học tập và hỗ trợ dự án
- **📚 Cộng đồng Spring Boot & Android Developers Việt Nam** - Nguồn tài liệu và giải đáp
- **🔧 ZXing Project** - Thư viện QR Code mã nguồn mở xuất sắc
- **🎨 Material Design** - Hệ thống thiết kế UI/UX chuyên nghiệp
- **🔥 Firebase** - Real-time database và push notification infrastructure
- **☕ JetBrains** - IntelliJ IDEA & Android Studio IDE

---

## 🗺️ **Roadmap | Kế hoạch phát triển**

### **Version 2.0 (Planned - Q3 2026)**
- [ ] 💳 Tích hợp thanh toán online (VNPay, MoMo, ZaloPay)
- [ ] 📊 Báo cáo thống kê và dashboard analytics nâng cao
- [ ] 📄 Export PDF cho đơn thuốc, hóa đơn và bệnh án
- [ ] 🎥 Video call tư vấn từ xa (Telemedicine với WebRTC)
- [ ] 🌐 Multi-language support (English, Vietnamese)
- [ ] 🌙 Dark mode cho mobile app
- [ ] 🔔 Push notification với Firebase Cloud Messaging

### **Version 1.5 (In Progress - Q2 2026)**
- [x] ✅ QR Code check-in system
- [x] ✅ Real-time queue management với SSE
- [x] ✅ Odontogram interactive view
- [x] ✅ Treatment plan templates
- [x] ✅ Prescription validation system
- [ ] 📴 Offline mode với local database (SQLite)
- [ ] 📈 Advanced analytics với MPAndroidChart
- [ ] 🎨 Lottie animations cho UX improvement

### **Version 1.0 (Released - Q1 2026)**
- [x] ✅ JWT Authentication & Authorization
- [x] ✅ Patient & Doctor management
- [x] ✅ Appointment booking system
- [x] ✅ Medical record management
- [x] ✅ Basic queue management
- [x] ✅ Service & Room management
- [x] ✅ Admin dashboard

---

## 📝 **License**

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 🤝 **Contributing | Đóng góp**

Chúng tôi hoan nghênh mọi đóng góp! Nếu bạn muốn contribute:

1. Fork repository này
2. Tạo branch mới (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Mở Pull Request

---

## 📧 **Liên hệ | Contact**

Nếu có câu hỏi hoặc góp ý, vui lòng liên hệ:

- **Team Leader**: Nguyễn Đoàn Trường Vĩ - [@truongvi-ute](https://github.com/truongvi-ute)
- **Technical Lead**: Đào Nguyễn Nhật Anh - [@YuuuuBin2k5](https://github.com/YuuuuBin2k5)
- **Project Repository**: [Nhom9_ProjectCuoiKy_PhongKhamApp](https://github.com/YuuuuBin2k5/Nhom9_ProjectCuoiKy_PhongKhamApp)
- **Issues & Bug Reports**: [GitHub Issues](https://github.com/YuuuuBin2k5/Nhom9_ProjectCuoiKy_PhongKhamApp/issues)
- **Documentation**: [Wiki](https://github.com/YuuuuBin2k5/Nhom9_ProjectCuoiKy_PhongKhamApp/wiki)

---

<div align="center">

**⭐ Nếu dự án hữu ích, hãy cho chúng tôi một star! ⭐**

Made with ❤️ by **Nhóm 9** - Digital Dental Solution

**🦷 Transforming Dental Care with Technology 🦷**

</div>
