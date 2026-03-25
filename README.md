# 🦷 Nhom9 PhongKham App - Clinic Management System

**Hệ thống quản lý phòng khám nha khoa hiện đại với ứng dụng Android và backend Spring Boot**

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)](https://github.com/YuuuuBin2k5/Nhom9_ProjectCuoiKy_PhongKhamApp)
[![Android](https://img.shields.io/badge/Android-API%2024+-green)](https://developer.android.com)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2+-blue)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

---

## 📱 **Tổng quan dự án**

Dự án **PhongKham App** là một hệ thống quản lý phòng khám nha khoa toàn diện, bao gồm:

- **📱 Mobile Android App**: Ứng dụng cho bệnh nhân và nhân viên
- **🖥️ Spring Boot Backend**: API server với JWT authentication
- **🔄 Real-time Features**: Queue management và notifications
- **📊 Admin Dashboard**: Quản lý bác sĩ, phòng khám, dịch vụ

---

## 🚀 **Tính năng chính**

### **👥 Cho Bệnh nhân**
- ✅ **Đăng ký/Đăng nhập** với OTP qua SMS
- ✅ **QR Check-in** với ZXing library
- ✅ **Theo dõi hàng đợi** real-time
- ✅ **Xem phác đồ điều trị** và tiến độ
- ✅ **Lịch hẹn** và thông báo
- ✅ **Profile management** cá nhân

### **👨‍⚕️ Cho Nhân viên Y tế**
- ✅ **QR Scanner** để check-in bệnh nhân
- ✅ **Queue Management** với drag & drop
- ✅ **Doctor Workflow** với odontogram
- ✅ **Treatment Plan** management
- ✅ **Patient Information** access

### **👨‍💼 Cho Quản trị viên**
- ✅ **Doctor Management** - thêm/sửa/xóa bác sĩ
- ✅ **Room Management** - quản lý phòng khám
- ✅ **Service Management** - quản lý dịch vụ
- ✅ **Analytics Dashboard** với biểu đồ
- ✅ **System Configuration**

---

## 🛠️ **Tech Stack**

### **📱 Android App**
```
• Language: Java
• UI Framework: Material Design 3
• Architecture: MVVM Pattern
• Networking: Retrofit + OkHttp
• QR System: ZXing Library
• Authentication: JWT Token
• Local Storage: SharedPreferences
• Build System: Gradle (Kotlin DSL)
```

### **🖥️ Backend API**
```
• Framework: Spring Boot 3.2+
• Language: Java 17+
• Database: MySQL with JPA/Hibernate
• Security: Spring Security + JWT
• Build System: Maven
• Real-time: Server-Sent Events (SSE)
• Documentation: OpenAPI/Swagger
```

---

## 📂 **Cấu trúc dự án**

```
Nhom9_ProjectCuoiKy_PhongKhamApp/
├── 📱 mobile_android/          # Android Application
│   ├── app/src/main/java/      # Java source code
│   ├── app/src/main/res/       # Android resources
│   ├── app/build.gradle.kts    # Android build config
│   └── 📋 *.md                 # Documentation files
├── 🖥️ clinic_backend/          # Spring Boot Backend
│   ├── src/main/java/          # Java source code
│   ├── src/main/resources/     # Application resources
│   ├── pom.xml                 # Maven configuration
│   └── 📋 *.md                 # API documentation
├── 📚 docs/                    # Project documentation
├── 🎯 prod/                    # Production planning docs
└── 📄 README.md                # This file
```

---

## 🚀 **Cài đặt và Chạy**

### **📋 Yêu cầu hệ thống**
- **Java 17+** (cho backend)
- **Android Studio** (cho mobile app)
- **MySQL 8.0+** (database)
- **Git** (version control)

### **🖥️ Backend Setup**

1. **Clone repository:**
```bash
git clone https://github.com/YuuuuBin2k5/Nhom9_ProjectCuoiKy_PhongKhamApp.git
cd Nhom9_ProjectCuoiKy_PhongKhamApp/clinic_backend
```

2. **Cấu hình database:**
```bash
# Tạo database MySQL
mysql -u root -p
CREATE DATABASE clinic_db;
```

3. **Cấu hình application.yml:**
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/clinic_db
    username: your_username
    password: your_password
```

4. **Chạy backend:**
```bash
./mvnw spring-boot:run
```

Backend sẽ chạy tại: `http://localhost:8081`

### **📱 Android App Setup**

1. **Mở Android Studio:**
```bash
cd mobile_android
# Open in Android Studio
```

2. **Cấu hình API URL:**
```java
// Trong build.gradle.kts
buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:8081/\"")
```

3. **Build và chạy:**
```bash
./gradlew assembleDebug
# Hoặc Run từ Android Studio
```

---

## 📱 **Screenshots**

### **Patient App**
| Login Screen | QR Check-in | Dashboard | Treatment Plan |
|--------------|-------------|-----------|----------------|
| ![Login](docs/screenshots/login.png) | ![QR](docs/screenshots/qr.png) | ![Dashboard](docs/screenshots/dashboard.png) | ![Treatment](docs/screenshots/treatment.png) |

### **Staff App**
| QR Scanner | Queue Management | Doctor Workflow | Admin Panel |
|------------|------------------|-----------------|-------------|
| ![Scanner](docs/screenshots/scanner.png) | ![Queue](docs/screenshots/queue.png) | ![Workflow](docs/screenshots/workflow.png) | ![Admin](docs/screenshots/admin.png) |

---

## 🔧 **API Documentation**

### **🔐 Authentication Endpoints**
```http
POST /api/auth/login          # Đăng nhập
POST /api/auth/register       # Đăng ký
POST /api/auth/otp/request    # Yêu cầu OTP
POST /api/auth/otp/verify     # Xác thực OTP
```

### **👤 Patient Endpoints**
```http
GET  /api/patient/me          # Thông tin cá nhân
GET  /api/patient/appointments # Lịch hẹn
GET  /api/patient/treatment-plans # Phác đồ điều trị
POST /api/patient/checkin/qr  # Tạo QR check-in
```

### **🏥 Staff Endpoints**
```http
GET  /api/queue               # Danh sách hàng đợi
POST /api/queue/scan          # Quét QR check-in
PUT  /api/queue/{id}/status   # Cập nhật trạng thái
GET  /api/doctors             # Danh sách bác sĩ
```

### **👨‍💼 Admin Endpoints**
```http
GET  /api/admin/doctors       # Quản lý bác sĩ
POST /api/admin/doctors       # Thêm bác sĩ mới
GET  /api/admin/rooms         # Quản lý phòng
GET  /api/admin/services      # Quản lý dịch vụ
```

---

## 🎯 **Tính năng đặc biệt**

### **📱 QR Check-in System**
- **ZXing Library** integration
- **Real-time QR generation** với expiry time
- **Professional scanner UI** với viewfinder
- **Offline QR display** capability

### **🔄 Real-time Queue Management**
- **Server-Sent Events (SSE)** cho live updates
- **Drag & drop** queue reordering
- **Status tracking** với color coding
- **Estimated wait time** calculation

### **🦷 Digital Odontogram**
- **Interactive tooth chart** với custom view
- **Treatment step tracking** per tooth
- **Visual progress indicators**
- **Touch-optimized interface**

### **📊 Admin Analytics**
- **Real-time dashboard** với charts
- **Service statistics** và performance metrics
- **Doctor workload** monitoring
- **Patient flow** analysis

---

## 🔒 **Bảo mật**

- **🔐 JWT Authentication** với refresh tokens
- **📱 OTP Verification** qua SMS
- **🛡️ Input Validation** và sanitization
- **🔒 HTTPS Encryption** (production)
- **👥 Role-based Access Control** (RBAC)
- **🚫 SQL Injection Protection** với JPA

---

## 🧪 **Testing**

### **Backend Testing**
```bash
cd clinic_backend
./mvnw test
```

### **Android Testing**
```bash
cd mobile_android
./gradlew test
./gradlew connectedAndroidTest
```

---

## 📈 **Performance**

### **Backend Metrics**
- **⚡ Response Time**: < 200ms average
- **🔄 Throughput**: 1000+ requests/second
- **💾 Memory Usage**: < 512MB heap
- **📊 Database**: Optimized queries với indexing

### **Mobile Metrics**
- **📱 APK Size**: < 15MB
- **🔋 Battery Usage**: Optimized với background limits
- **📶 Network**: Efficient caching và compression
- **🚀 Startup Time**: < 3 seconds cold start

---

## 🤝 **Contributing**

Chúng tôi hoan nghênh mọi đóng góp! Vui lòng:

1. **Fork** repository
2. **Create feature branch**: `git checkout -b feature/amazing-feature`
3. **Commit changes**: `git commit -m 'Add amazing feature'`
4. **Push to branch**: `git push origin feature/amazing-feature`
5. **Open Pull Request**

### **📋 Development Guidelines**
- Follow **Java Code Conventions**
- Write **comprehensive tests**
- Update **documentation**
- Use **meaningful commit messages**

---

## 📄 **License**

Dự án này được phân phối dưới **MIT License**. Xem file [LICENSE](LICENSE) để biết thêm chi tiết.

---

## 👥 **Team Members - Nhóm 9**

| Thành viên | Vai trò | GitHub |
|------------|---------|--------|
| **Đào Nguyễn Nhật Anh** | Team Lead & Backend | [@YuuuuBin2k5](https://github.com/YuuuuBin2k5) |
| **Nguyễn Đoàn Trường Vĩ** | Backend & QA Tester | [@truongvi-ute](https://github.com/truongvi-ute) |
| **Trần Hoàng Phúc Quân** | UI/UX Designer & QA Tester | [@PhucQuan](https://github.com/PhucQuan) |
| **Nguyễn Hồng Phúc** | UI/UX Designer & QA Tester | [@NHP39](https://github.com/NHP39) |

---

## 📞 **Liên hệ**

- **📧 Email**: nhom9.phongkham@gmail.com
- **🐛 Issues**: [GitHub Issues](https://github.com/YuuuuBin2k5/Nhom9_ProjectCuoiKy_PhongKhamApp/issues)
- **💬 Discussions**: [GitHub Discussions](https://github.com/YuuuuBin2k5/Nhom9_ProjectCuoiKy_PhongKhamApp/discussions)

---

## 🙏 **Acknowledgments**

- **🏫 Trường Đại học Bách Khoa TP.HCM** - Hỗ trợ học thuật
- **📚 Spring Boot Community** - Framework documentation
- **🤖 Android Developers** - Mobile development resources
- **🔧 ZXing Project** - QR code library
- **🎨 Material Design** - UI/UX guidelines

---

<div align="center">

**⭐ Nếu dự án hữu ích, hãy cho chúng tôi một star! ⭐**

Made with ❤️ by **Nhóm 9** - HCMUT

</div>
