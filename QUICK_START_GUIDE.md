# 🚀 QUICK START GUIDE

## 📋 Prerequisites

### Backend Requirements
- Java 17 or higher
- PostgreSQL 14+
- Maven 3.8+ or Gradle 7+
- 2GB RAM minimum

### Mobile Requirements
- Android Studio Arctic Fox or newer
- Android SDK 24+ (Android 7.0)
- JDK 17
- 4GB RAM minimum

---

## ⚡ QUICK START (5 Minutes)

### Step 1: Clone Repository
```bash
git clone https://github.com/YuuuuBin2k5/Nhom9_ProjectCuoiKy_PhongKhamApp.git
cd Nhom9_ProjectCuoiKy_PhongKhamApp
```

### Step 2: Setup Database
```bash
# Create database
psql -U postgres
CREATE DATABASE phongkham;
\q

# Database will auto-initialize on first run
```

### Step 3: Start Backend
```bash
cd clinic_backend

# Using Maven
./mvnw spring-boot:run

# Or using Gradle
./gradlew bootRun

# Server will start on http://localhost:8081
```

### Step 4: Configure Mobile
```bash
cd mobile_android

# Create local.properties if not exists
echo "backend.host=192.168.1.10" > local.properties
# Replace 192.168.1.10 with your computer's IP

# Open in Android Studio
# File > Open > Select mobile_android folder
```

### Step 5: Run Mobile App
```
1. Open Android Studio
2. Wait for Gradle sync
3. Click Run (▶️) button
4. Select emulator or device
5. App will install and launch
```

---

## 🧪 VERIFY INSTALLATION

### Test Backend
```bash
# Health check
curl http://localhost:8081/actuator/health

# Expected: {"status":"UP"}

# Test login
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"patient01@gmail.com","password":"123456"}'

# Expected: JWT token response
```

### Test Mobile
1. Launch app
2. Login with: `patient01@gmail.com` / `123456`
3. Should see patient dashboard
4. Navigate to "Hóa đơn" to see invoice list

---

## 🔑 DEFAULT ACCOUNTS

| Role | Email | Password |
|------|-------|----------|
| Admin | admin@gmail.com | 123456 |
| Doctor | doc01@gmail.com | 123456 |
| Patient | patient01@gmail.com | 123456 |

---

## 📱 TESTING NEW FEATURES

### Test Invoice & Payment Flow
1. Login as Patient
2. Navigate to "Hóa đơn của tôi"
3. Click on any invoice
4. Click "Thanh toán"
5. Select payment method
6. Confirm payment
7. Verify success message

### Test Review System
1. Login as Patient
2. Navigate to completed appointment
3. Click "Đánh giá"
4. Select rating (1-5 stars)
5. Enter comment
6. Submit review
7. Verify success

### Test Admin Dashboard
1. Login as Admin
2. Navigate to Dashboard
3. Select date range
4. Click "Tải" to load reports
5. Verify revenue statistics
6. Check top services
7. Review doctor performance

---

## 🐛 TROUBLESHOOTING

### Backend Issues

**Problem**: Port 8081 already in use
```bash
# Find process using port
lsof -i :8081

# Kill process
kill -9 <PID>

# Or change port in application.properties
server.port=8082
```

**Problem**: Database connection failed
```bash
# Check PostgreSQL is running
sudo systemctl status postgresql

# Start PostgreSQL
sudo systemctl start postgresql

# Verify credentials in application.properties
spring.datasource.username=postgres
spring.datasource.password=your_password
```

**Problem**: Build failed
```bash
# Clean and rebuild
./mvnw clean install

# Or with Gradle
./gradlew clean build
```

### Mobile Issues

**Problem**: Cannot connect to backend
```
1. Check backend.host in local.properties
2. Verify backend is running
3. Ensure device/emulator on same network
4. Try using computer's IP instead of localhost
```

**Problem**: Build failed
```
1. File > Invalidate Caches / Restart
2. Clean Project
3. Rebuild Project
4. Sync Project with Gradle Files
```

**Problem**: Activities not found
```
1. Check AndroidManifest.xml has all activities
2. Rebuild project
3. Uninstall app from device
4. Reinstall
```

---

## 📊 MONITORING

### Backend Logs
```bash
# View logs
tail -f logs/application.log

# Or if using systemd
journalctl -u clinic-backend -f
```

### Mobile Logs
```bash
# View Android logs
adb logcat | grep "PhongKham"

# Or use Android Studio Logcat window
```

---

## 🔧 CONFIGURATION

### Backend Configuration
File: `clinic_backend/src/main/resources/application.properties`

```properties
# Server
server.port=8081

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/phongkham
spring.datasource.username=postgres
spring.datasource.password=your_password

# JWT
jwt.secret=your-secret-key
jwt.expiration=86400000

# CORS
cors.allowed-origins=http://localhost:3000,http://192.168.1.10
```

### Mobile Configuration
File: `mobile_android/local.properties`

```properties
# Backend API
backend.host=192.168.1.10
backend.port=8081
backend.protocol=http
```

---

## 📚 API DOCUMENTATION

### Base URL
```
http://localhost:8081/api
```

### Authentication
All endpoints (except login/register) require JWT token:
```
Authorization: Bearer <your-jwt-token>
```

### Key Endpoints

**Invoice & Payment**
```
GET    /api/invoices/my
GET    /api/invoices/{id}
POST   /api/invoices/{id}/pay
```

**Review**
```
POST   /api/reviews
GET    /api/reviews/my
```

**Admin Reports**
```
GET    /api/admin/reports/revenue?startDate=2024-01-01&endDate=2024-12-31
GET    /api/admin/reports/top-services?limit=10
GET    /api/admin/reports/doctor-performance?startDate=2024-01-01&endDate=2024-12-31
```

---

## 🎯 NEXT STEPS

After successful setup:

1. ✅ Explore all features
2. ✅ Test payment flow
3. ✅ Test review system
4. ✅ Test admin dashboard
5. ✅ Read full documentation
6. ✅ Deploy to production

---

## 📞 SUPPORT

### Documentation
- `DEPLOYMENT_CHECKLIST.md` - Deployment guide
- `ANDROID_MANIFEST_UPDATE.md` - Mobile setup
- `FINAL_HANDOVER_DOCUMENT.md` - Complete reference

### Issues
- Check existing documentation
- Review troubleshooting section
- Contact team members

---

## ✅ SUCCESS CHECKLIST

- [ ] Backend running on port 8081
- [ ] Database connected
- [ ] Mobile app installed
- [ ] Can login successfully
- [ ] Invoice list displays
- [ ] Payment flow works
- [ ] Review submission works
- [ ] Admin dashboard loads

---

**🎉 You're ready to go! Happy coding! 🚀**
