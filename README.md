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
- ✅ **Giao diện Odontogram**: Tương tác trực tiếp trên sơ đồ răng (OdontogramView) để ghi chú tình trạng.
- ✅ **Kê đơn thuốc**: Tạo và quản lý đơn thuốc điện tử (PrescriptionActivity).
- ✅ **Truy cập bệnh án**: Tra cứu nhanh lịch sử và thông tin lâm sàng của bệnh nhân.
### **👨‍💼 Cho Quản trị viên | For Admin**
- ✅ **Quản trị Dashboard**: Theo dõi số liệu thống kê và hiệu suất hoạt động của phòng khám (AdminMainActivity).
- ✅ **Quản lý Dịch vụ**: Cập nhật danh mục kỹ thuật, đơn giá và hình ảnh minh họa (AdminServiceActivity).
- ✅ **Quản lý Đội ngũ**: Thêm mới và kiểm soát trạng thái hoạt động của bác sĩ (AdminDoctorActivity).
- ✅ **Quản lý Cơ sở**: Theo dõi tình trạng các phòng khám và trang thiết bị (AdminRoomActivity).
- ✅ **Quản lý Danh mục**: Tổ chức dịch vụ theo categories với dialog thêm/sửa/xóa.h họa.
- ✅ **Quản lý Đội ngũ**: Thêm mới và kiểm soát trạng thái hoạt động của bác sĩ.
- ✅ **Quản lý Cơ sở**: Theo dõi tình trạng các phòng khám và trang thiết bị.

---

## 🛠️ **Tech Stack**

### **📱 Android App**
- **Language**: Java 11 / Android SDK (API 24+, Target 36)
- **UI Framework**: Material Design 3 + ConstraintLayout
- **Networking**: Retrofit 2.9.0 + OkHttp 4.9.3 (Logging Interceptor)
- **QR System**: ZXing Core 3.5.2 + ZXing Android Embedded 4.3.0
- **Image Loading**: Glide
- **Camera**: CameraX 1.3.1 (Core, Camera2, Lifecycle, View)
- **Security**: AndroidX Security Crypto 1.1.0-alpha06
- **Real-time**: Firebase Realtime Database (BOM 32.7.0)
- **Architecture**: Activity-based Pattern với Fragment Support

### **🖥️ Backend API**
- **Framework**: Spring Boot 3.2.4 (Java 17)
- **Security**: Spring Security + JWT (JJWT 0.11.5)
- **Database**: PostgreSQL với Spring Data JPA/Hibernate
- **Build System**: Maven
- **Validation**: Spring Boot Starter Validation
- **Utilities**: Lombok (Annotation Processing)
- **Push Notification**: Firebase Admin SDK 9.2.0
- **Real-time**: Server-Sent Events (SSE) & Async Processing

---

## 📂 **Cấu trúc dự án | Project Structure**

```text
Nhom9_ProjectCuoiKy_PhongKhamApp/
├── 📱 mobile_android/                    # Android Mobile Application
│   ├── app/src/main/java/com/hcmute/mobile_android/
│   │   ├── adapters/                    # RecyclerView Adapters (Queue, Treatment, etc.)
│   │   ├── network/                     # Retrofit API Service & Models
│   │   ├── ui/                          # Activities & Fragments
│   │   │   ├── activities/              # Main Activities (Login, QR Scanner, etc.)
│   │   │   │   └── staff/               # Staff-specific Activities
│   │   │   └── fragments/               # UI Fragments (Dashboard, Settings)
│   │   ├── util/                        # Utilities (TokenManager, QRCodeHelper)
│   │   └── services/                    # Background Services
│   ├── app/src/main/res/                # Material UI Resources & Layouts
│   │   ├── layout/                      # XML Layouts
│   │   ├── drawable/                    # Icons & Graphics
│   │   └── values/                      # Strings, Colors, Themes
│   └── app/build.gradle.kts             # Android Build Configuration (Kotlin DSL)
│
├── 🖥️ clinic_backend/                    # Spring Boot REST API
│   ├── src/main/java/com/hcmute/clinic/
│   │   ├── config/                      # Security, CORS, DataSeed Configuration
│   │   ├── controller/                  # REST Controllers (18 endpoints)
│   │   ├── dto/                         # Data Transfer Objects
│   │   ├── entity/                      # JPA Entities (Patient, Doctor, etc.)
│   │   ├── enums/                       # Enumerations (Status, Roles)
│   │   ├── exception/                   # Custom Exception Handlers
│   │   ├── repository/                  # Spring Data JPA Repositories
│   │   ├── security/                    # JWT Authentication & Filters
│   │   ├── service/                     # Business Logic Services
│   │   └── util/                        # Utility Classes
│   ├── src/main/resources/
│   │   ├── application.yml              # Spring Boot Configuration
│   │   └── uploads/                     # File Upload Directory
│   └── pom.xml                          # Maven Build Configuration
│
├── 📚 prod/                              # Production Documentation
│   └── USERSTORY.md                     # User Stories & Requirements
├── 📄 README.md                          # Project Overview Documentation
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
- **Android Studio** (Hedgehog 2023.1.1+)
- **Maven 3.6+** (hoặc dùng wrapper `./mvnw`)
- **Android SDK API 24+** (Target API 36)

### **🖥️ Backend Setup**
1. **Database**: Khởi tạo PostgreSQL database tên `phongkham`.
   ```sql
   CREATE DATABASE phongkham;
   ```

2. **Configuration**: Cập nhật file `clinic_backend/src/main/resources/application.yml`:
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/phongkham
       username: postgres
       password: your_password  # Thay đổi password của bạn
   ```

3. **Run Server**:
   ```bash
   cd clinic_backend
   ./mvnw clean spring-boot:run
   ```
   *Server sẽ chạy tại `http://0.0.0.0:8081` và tự động khởi tạo dữ liệu mẫu (DataSeed) khi chạy lần đầu.*

### **📱 Android App Setup**
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

- **User Stories**: Xem file `prod/USERSTORY.md` để hiểu chi tiết các use case
- **Prescription Fix**: Xem `clinic_backend/docs/PRESCRIPTION_FIX_COMPLETE.md` cho lịch sử fix bugs
- **API Testing**: Sử dụng Postman hoặc curl để test endpoints
- **Database Schema**: Xem các Entity classes trong `clinic_backend/src/main/java/com/hcmute/clinic/entity/`

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

Backend cung cấp RESTful API với 18+ controllers, bao gồm:

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
| **PATCH** | `/api/queue/{id}/status` | Cập nhật trạng thái bệnh nhân |

### **💊 Treatment & Prescription**
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| **GET** | `/api/treatment-plans/my` | Xem phác đồ điều trị của tôi |
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

### **📊 Real-time Features**
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| **GET** | `/api/queue/sse` | Server-Sent Events cho hàng đợi |
| **GET** | `/api/notifications/sse` | SSE cho thông báo real-time |

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

Chúng tôi tự hào giới thiệu đội ngũ phát triển đằng sau dự án PhongKham App:

| Thành viên | Vai trò | GitHub |
| :--- | :--- | :--- |
| **Đào Nguyễn Nhật Anh** | Frontend & Backend | [@YuuuuBin2k5](https://github.com/YuuuuBin2k5) |
| **Nguyễn Đoàn Trường Vĩ** | Team leader & Backend & QA Tester | [@truongvi-ute](https://github.com/truongvi-ute) |
| **Trần Hoàng Phúc Quân** | Frontend & UI/UX Designer & QA Tester | [@PhucQuan](https://github.com/PhucQuan) |
| **Nguyễn Hồng Phúc** | UI/UX Designer & QA Tester | [@NHP39](https://github.com/NHP39) |

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

### **Version 2.0 (Planned)**
- [ ] Tích hợp thanh toán online (VNPay, MoMo)
- [ ] Báo cáo thống kê và dashboard analytics
- [ ] Export PDF cho đơn thuốc và hóa đơn
- [ ] Video call tư vấn từ xa (Telemedicine)
- [ ] Multi-language support (English, Vietnamese)
- [ ] Dark mode cho mobile app

### **Version 1.5 (In Progress)**
- [x] QR Code check-in system
- [x] Real-time queue management với SSE
- [x] Odontogram interactive view
- [x] Treatment plan templates
- [ ] Push notification với Firebase Cloud Messaging
- [ ] Offline mode với local database

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

- **Team Lead**: Đào Nguyễn Nhật Anh - [@YuuuuBin2k5](https://github.com/YuuuuBin2k5)
- **Project Repository**: [Nhom9_ProjectCuoiKy_PhongKhamApp](https://github.com/YuuuuBin2k5/Nhom9_ProjectCuoiKy_PhongKhamApp)
- **Issues**: [GitHub Issues](https://github.com/YuuuuBin2k5/Nhom9_ProjectCuoiKy_PhongKhamApp/issues)

---

<div align="center">

**⭐ Nếu dự án hữu ích, hãy cho chúng tôi một star! ⭐**

Made with ❤️ by **Nhóm 9** - Digital Dental Solution

**🦷 Transforming Dental Care with Technology 🦷**

</div>
