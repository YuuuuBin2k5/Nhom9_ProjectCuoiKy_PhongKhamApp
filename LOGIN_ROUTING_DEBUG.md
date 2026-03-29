# DEBUG: LOGIN ROUTING VÀO PATIENT THAY VÌ DOCTOR

**Vấn đề:** Đăng nhập doctor nhưng app vào màn hình patient

---

## 🔍 NGUYÊN NHÂN CÓ THỂ

### 1. Token cũ từ lần login trước
```
- Bạn đã login patient trước đó
- Token và role được lưu trong SharedPreferences
- Khi login doctor, nếu có lỗi thì vẫn dùng token cũ
```

### 2. Backend trả về role sai
```
- API /api/auth/login trả về role="PATIENT" thay vì "DOCTOR"
- Kiểm tra response từ backend
```

### 3. TokenManager không lưu role đúng
```
- saveUserRole() không hoạt động
- getUserRole() trả về giá trị cũ
```

---

## ✅ GIẢI PHÁP

### Solution 1: Clear App Data (NHANH NHẤT)
```
1. Settings → Apps → Clinic App
2. Storage → Clear Data
3. Mở app lại
4. Login với doc01@gmail.com / password123
```

### Solution 2: Logout và Login lại
```
1. Trong app, nhấn Profile/Settings
2. Nhấn Logout
3. Login lại với doc01@gmail.com
```

### Solution 3: Uninstall và Install lại
```bash
cd mobile_android
./gradlew uninstallDebug
./gradlew installDebug
```

---

## 🧪 KIỂM TRA

### Check 1: Verify Backend Response
```
Khi login doctor, check Logcat:
- Filter: "okhttp"
- Tìm response của /api/auth/login
- Verify: "role":"DOCTOR"
```

### Check 2: Verify Token Saved
```
Sau khi login, check Logcat:
- Filter: "TokenManager"
- Verify: saveUserRole("DOCTOR")
```

### Check 3: Verify MainActivity Routing
```
Khi MainActivity onCreate, check Logcat:
- Filter: "MainActivity"
- Verify: role = DOCTOR, isDoctor = true
```

---

## 📝 CODE HIỆN TẠI

### LoginActivity.java (Line 76-79)
```java
// Save user role
if (body.getRole() != null) {
    tm.saveUserRole(body.getRole());
}
```
✅ Code đúng - Đang lưu role

### MainActivity.java (Line 31-33)
```java
TokenManager tm = new TokenManager(this);
String role = tm.getUserRole();
isDoctor = "DOCTOR".equalsIgnoreCase(role);
```
✅ Code đúng - Đang check role

### MainActivity.java (Line 40-42)
```java
if (id == R.id.nav_home) {
    f = isDoctor ? new HomeFragment() : new PatientDashboardFragment();
}
```
✅ Code đúng - Routing theo role

---

## 🐛 DEBUG STEPS

### Step 1: Add Log to MainActivity
Thêm log để debug:

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    TokenManager tm = new TokenManager(this);
    String role = tm.getUserRole();
    isDoctor = "DOCTOR".equalsIgnoreCase(role);
    
    // DEBUG LOG
    android.util.Log.d("MainActivity", "onCreate: role=" + role + ", isDoctor=" + isDoctor);
    
    // ... rest of code
}
```

### Step 2: Add Log to LoginActivity
```java
if (body.getRole() != null) {
    tm.saveUserRole(body.getRole());
    android.util.Log.d("LoginActivity", "Saved role: " + body.getRole());
}
```

### Step 3: Check Backend
```bash
# Test login API
curl -X POST http://192.168.1.124:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"doc01@gmail.com","password":"password123"}'

# Expected response:
{
  "token": "...",
  "role": "DOCTOR",  # ← Phải là DOCTOR
  "userId": 1,
  "email": "doc01@gmail.com"
}
```

---

## 🎯 QUICK FIX

### Option A: Clear Data (Recommended)
```
1. Long press app icon
2. App info
3. Storage
4. Clear data
5. Open app
6. Login: doc01@gmail.com / password123
```

### Option B: Add Logout Button
Nếu chưa có logout, tạm thời dùng:
```
1. Settings → Apps → Clinic App → Force Stop
2. Clear Data
3. Open app
```

### Option C: Reinstall
```bash
cd mobile_android
./gradlew uninstallDebug
./gradlew installDebug
```

---

## ✅ EXPECTED BEHAVIOR

### Khi login doctor:
```
1. Input: doc01@gmail.com / password123
2. API response: role="DOCTOR"
3. TokenManager saves: role="DOCTOR"
4. MainActivity reads: role="DOCTOR", isDoctor=true
5. Bottom nav shows: HomeFragment (Doctor dashboard)
6. Bottom nav hides: QR Check-in, Treatment Plan tabs
```

### Khi login patient:
```
1. Input: patient01@gmail.com / password123
2. API response: role="PATIENT"
3. TokenManager saves: role="PATIENT"
4. MainActivity reads: role="PATIENT", isDoctor=false
5. Bottom nav shows: PatientDashboardFragment
6. Bottom nav shows: QR Check-in, Treatment Plan tabs
```

---

## 📱 ACCOUNTS FOR TESTING

### Doctor Accounts:
```
doc01@gmail.com / password123 (Phòng 01)
doc_xray@gmail.com / password123 (Phòng X-quang)
doc_surgery@gmail.com / password123 (Phòng tiểu phẫu)
```

### Patient Account:
```
patient01@gmail.com / password123
```

### Admin Account:
```
admin@clinic.com / admin123
```

---

## 🔧 PERMANENT FIX (If Needed)

Nếu vấn đề vẫn tiếp diễn, thêm validation:

```java
// In MainActivity.onCreate()
TokenManager tm = new TokenManager(this);
String role = tm.getUserRole();

// Validate role
if (role == null || role.isEmpty()) {
    // No role saved - redirect to login
    Intent intent = new Intent(this, LoginActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    startActivity(intent);
    finish();
    return;
}

isDoctor = "DOCTOR".equalsIgnoreCase(role);
android.util.Log.d("MainActivity", "Role: " + role + ", isDoctor: " + isDoctor);
```

---

**Status:** Cần clear app data hoặc logout/login lại  
**Root Cause:** Token cũ từ lần login patient trước đó  
**Solution:** Clear data → Login lại với doctor account
