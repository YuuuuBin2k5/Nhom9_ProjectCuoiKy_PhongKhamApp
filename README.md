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
- ✅ **Quét mã QR**: Tiếp đón bệnh nhân nhanh chóng bằng scanner tích hợp.
- ✅ **Quản lý hàng đợi**: Điều phối bệnh nhân, chuyển phòng (X-quang, Tiểu phẫu) linh hoạt.
- ✅ **Lập phác đồ điều trị**: Sử dụng các template chuyên nghiệp để thiết kế quy trình điều trị.
- ✅ **Giao diện Odontogram**: Tương tác trực tiếp trên sơ đồ răng để ghi chú tình trạng.
- ✅ **Truy cập bệnh án**: Tra cứu nhanh lịch sử và thông tin lâm sàng của bệnh nhân.

### **👨‍💼 Cho Quản trị viên | For Admin**
- ✅ **Quản trị Dashboard**: Theo dõi số liệu thống kê và hiệu suất hoạt động của phòng khám.
- ✅ **Quản lý Dịch vụ**: Cập nhật danh mục kỹ thuật, đơn giá và hình ảnh minh họa.
- ✅ **Quản lý Đội ngũ**: Thêm mới và kiểm soát trạng thái hoạt động của bác sĩ.
- ✅ **Quản lý Cơ sở**: Theo dõi tình trạng các phòng khám và trang thiết bị.

---

## 🛠️ **Tech Stack**

### **📱 Android App**
- **Language**: Java / Android SDK
- **UI Framework**: Material Design 3
- **Networking**: Retrofit 2 + OkHttp
- **QR System**: ZXing Library
- **Image Loading**: Glide
- **Architecture**: Integrated Activity-based Pattern

### **🖥️ Backend API**
- **Framework**: Spring Boot 3.2+
- **Security**: Spring Security + JWT (Stateless)
- **Database**: PostgreSQL (với JPA/Hibernate)
- **Build System**: Gradle
- **Real-time**: Server-Sent Events (SSE) & Async Processing

---

## 📂 **Cấu trúc dự án | Project Structure**

```text
Nhom9_ProjectCuoiKy_PhongKhamApp/
├── 📱 mobile_android/          # Android Mobile Application
│   ├── app/src/main/java/      # Source code (Activities, Adapters, Models)
│   ├── app/src/main/res/       # Material UI resources & Layouts
│   └── build.gradle            # Android build configuration
├── 🖥️ clinic_backend/          # Spring Boot REST API
│   ├── src/main/java/          # Source code (Controller, Service, Entity)
│   ├── src/main/resources/     # Configuration (application.yml, DataSeed)
│   └── build.gradle            # Backend build configuration
├── 📚 docs/                    # Tài liệu hướng dẫn & Thiết kế
└── 📄 README.md                # Tài liệu tổng quan dự án
```

---

## 🚀 **Cài đặt và Chạy | Setup & Run**

### **🖥️ Backend Setup**
1. **Database**: Khởi tạo PostgreSQL database tên `phongkham`.
2. **Configuration**: Cập nhật file `src/main/resources/application.yml` (copy từ bản `.example`).
3. **Run Server**:
   ```bash
   cd clinic_backend
   ./mvnw spring-boot:run
   ```
   *Server sẽ tự động khởi tạo dữ liệu mẫu (Data Seed) khi chạy lần đầu.*

### **📱 Android App Setup**
1. **API Endpoint**: Cập nhật IP trong `local.properties` (ví dụ: `backend.host=192.168.1.10`). Hệ thống sẽ tự động cập nhật vào `BuildConfig`.
2. **Build**: Mở thư mục `mobile_android` bằng Android Studio.
3. **Run**: Chạy trên emulator hoặc thiết bị thật cùng lớp mạng với server.

---

## 🔑 **Tài khoản dùng thử | Demo Credentials**

| Role | Email | Password |
| :--- | :--- | :--- |
| **Admin** | `admin@gmail.com` | `123456` |
| **Doctor** | `doc01@gmail.com` | `123456` |
| **Patient** | `patient01@gmail.com` | `123456` |

---

## 🔧 **API Documentation (Summary)**

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| **POST** | `/api/auth/login` | Đăng nhập hệ thống |
| **GET** | `/api/patients/me` | Lấy thông tin cá nhân bệnh nhân |
| **POST** | `/api/checkin/scan` | Quét mã QR tiếp đón |
| **GET** | `/api/treatment-plans/my` | Xem phác đồ điều trị của tôi |
| **PATCH** | `/api/admin/doctors/{id}/status` | Cập nhật hồ sơ bác sĩ (Admin) |

---

## 🔒 **Bảo mật | Security**
- **Bảo mật Token**: Sử dụng JWT cho mọi API request sau khi đăng nhập.
- **Xác thực 2 lớp**: OTP qua điện thoại cho các thao tác đăng ký quan trọng.
- **Phân quyền (RBAC)**: Kiểm soát truy cập chặt chẽ dựa trên vai trò Admin/Doctor/Patient.

---

## 🤝 **Thành viên phát triển | Team Members - Nhóm 9**

Chúng tôi tự hào giới thiệu đội ngũ phát triển đằng sau dự án PhongKham App:

| Thành viên | Vai trò | GitHub |
| :--- | :--- | :--- |
| **Đào Nguyễn Nhật Anh** | Team Lead & Backend | [@YuuuuBin2k5](https://github.com/YuuuuBin2k5) |
| **Nguyễn Đoàn Trường Vĩ** | Backend & QA Tester | [@truongvi-ute](https://github.com/truongvi-ute) |
| **Trần Hoàng Phúc Quân** | UI/UX Designer & QA Tester | [@PhucQuan](https://github.com/PhucQuan) |
| **Nguyễn Hồng Phúc** | UI/UX Designer & QA Tester | [@NHP39](https://github.com/NHP39) |

---

## 🙏 **Lời cảm ơn | Acknowledgments**
- **🏫 Trường Đại học Công Nghệ Kỹ Thuật TP.HCM (HUTECH)**
- **📚 Cộng đồng Spring Boot & Android Developers Việt Nam**
- **🔧 ZXing Project** - Thư viện hỗ trợ QR Code tuyệt vời.
- **🎨 Material Design** - Tiêu chuẩn cho giao diện chuyên nghiệp.

---

<div align="center">

**⭐ Nếu dự án hữu ích, hãy cho chúng tôi một star! ⭐**

Made with ❤️ by **Nhóm 9** - Digital Dental Solution

</div>
