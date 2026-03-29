# 📱 ANDROID MANIFEST UPDATE GUIDE

## 🎯 CẦN THÊM VÀO AndroidManifest.xml

### Location
File: `mobile_android/app/src/main/AndroidManifest.xml`

### Activities cần thêm (4 activities)

Thêm vào trong thẻ `<application>`:

```xml
<!-- Phase 2: Invoice & Payment Activities -->
<activity
    android:name=".ui.activities.InvoiceListActivity"
    android:label="Hóa đơn của tôi"
    android:theme="@style/Theme.MobileAndroid"
    android:exported="false"/>

<activity
    android:name=".ui.activities.InvoiceDetailActivity"
    android:label="Chi tiết hóa đơn"
    android:theme="@style/Theme.MobileAndroid"
    android:exported="false"/>

<activity
    android:name=".ui.activities.PaymentActivity"
    android:label="Thanh toán"
    android:theme="@style/Theme.MobileAndroid"
    android:exported="false"/>

<activity
    android:name=".ui.activities.ReviewActivity"
    android:label="Đánh giá"
    android:theme="@style/Theme.MobileAndroid"
    android:exported="false"/>
```

---

## 📋 COMPLETE EXAMPLE

Đây là ví dụ đầy đủ phần `<application>` trong AndroidManifest.xml:

```xml
<application
    android:allowBackup="true"
    android:icon="@mipmap/ic_launcher"
    android:label="@string/app_name"
    android:roundIcon="@mipmap/ic_launcher_round"
    android:supportsRtl="true"
    android:theme="@style/Theme.MobileAndroid"
    android:usesCleartextTraffic="true">
    
    <!-- Existing Activities -->
    <activity
        android:name=".MainActivity"
        android:exported="true">
        <intent-filter>
            <action android:name="android.intent.action.MAIN" />
            <category android:name="android.intent.category.LAUNCHER" />
        </intent-filter>
    </activity>
    
    <!-- ... other existing activities ... -->
    
    <!-- Phase 2: NEW Invoice & Payment Activities -->
    <activity
        android:name=".ui.activities.InvoiceListActivity"
        android:label="Hóa đơn của tôi"
        android:theme="@style/Theme.MobileAndroid"
        android:exported="false"/>

    <activity
        android:name=".ui.activities.InvoiceDetailActivity"
        android:label="Chi tiết hóa đơn"
        android:theme="@style/Theme.MobileAndroid"
        android:exported="false"/>

    <activity
        android:name=".ui.activities.PaymentActivity"
        android:label="Thanh toán"
        android:theme="@style/Theme.MobileAndroid"
        android:exported="false"/>

    <activity
        android:name=".ui.activities.ReviewActivity"
        android:label="Đánh giá"
        android:theme="@style/Theme.MobileAndroid"
        android:exported="false"/>
    
</application>
```

---

## ✅ VERIFICATION CHECKLIST

Sau khi update AndroidManifest.xml:

- [ ] Kiểm tra không có lỗi syntax XML
- [ ] Kiểm tra tất cả activity names đúng package
- [ ] Kiểm tra `android:exported="false"` cho các internal activities
- [ ] Build project để verify
- [ ] Test mở từng activity

---

## 🔧 BUILD COMMANDS

### Clean & Build
```bash
cd mobile_android
./gradlew clean
./gradlew build
```

### Build Debug APK
```bash
./gradlew assembleDebug
```

### Build Release APK
```bash
./gradlew assembleRelease
```

---

## 🚀 TESTING

### Test từng Activity:

1. **InvoiceListActivity**
   - Mở từ Patient Dashboard
   - Test filter chips (All, Paid, Unpaid)
   - Test click vào invoice item

2. **InvoiceDetailActivity**
   - Test hiển thị invoice details
   - Test payment button (chỉ hiện khi unpaid)
   - Test navigation

3. **PaymentActivity**
   - Test chọn payment method
   - Test nhập note
   - Test confirm payment
   - Test success dialog

4. **ReviewActivity**
   - Test rating bar
   - Test comment input
   - Test submit review
   - Test validation

---

## 📝 NOTES

- Tất cả activities đều có `android:exported="false"` vì chúng là internal activities
- Theme sử dụng `@style/Theme.MobileAndroid` (hoặc theme hiện tại của app)
- Label có thể customize theo nhu cầu
- Activities không cần intent-filter vì được mở bằng explicit intent

---

**Ready to update AndroidManifest.xml!** ✅
