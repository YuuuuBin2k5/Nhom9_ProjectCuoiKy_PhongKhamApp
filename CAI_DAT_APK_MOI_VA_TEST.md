# Hướng Dẫn Cài Đặt APK Mới Và Test Tính Năng

## ⚠️ QUAN TRỌNG

APK mới đã được build với tính năng **tự động load dữ liệu các bước đã hoàn thành (READ-ONLY)**.

## 📱 Bước 1: Gỡ App Cũ

**PHẢI GỠ APP CŨ TRƯỚC** để tránh conflict:

```
1. Vào Settings → Apps
2. Tìm app "Toothly" hoặc tên app của bạn
3. Nhấp "Uninstall"
4. Xác nhận gỡ
```

## 📦 Bước 2: Cài APK Mới

APK mới nằm ở:
```
mobile_android/app/build/outputs/apk/debug/app-debug.apk
```

**Cách cài:**

### Option 1: Qua ADB (Nếu có)
```bash
adb install -r mobile_android/app/build/outputs/apk/debug/app-debug.apk
```

### Option 2: Copy File
```
1. Copy file app-debug.apk vào điện thoại
2. Mở file manager
3. Nhấp vào file app-debug.apk
4. Cho phép cài đặt từ nguồn không xác định (nếu hỏi)
5. Nhấp "Install"
```

## 🧪 Bước 3: Test Tính Năng

### Test Case: Bệnh Nhân Có Bước Đã Hoàn Thành

**Chuẩn bị:**
- Cần có bệnh nhân với ít nhất 1 bước COMPLETED
- Bước đó phải có dữ liệu (ghi chú hoặc ảnh)

**Các bước test:**

1. **Đăng nhập** với tài khoản bác sĩ

2. **Vào "Quản Lý Hàng Đợi"**

3. **Tìm bệnh nhân** có bước đã hoàn thành

4. **Nhấp vào bệnh nhân**

5. **Quan sát kết quả:**

   ✅ **Toast đầu tiên:**
   ```
   "Đã tải phác đồ điều trị (X bước)"
   ```

   ✅ **Toast thứ hai (MỚI):**
   ```
   "Đã tải 2 bước đã hoàn thành. Nhấp 'Chỉnh sửa' để xem chi tiết."
   ```
   (Số 2 là ví dụ, tùy vào số bước COMPLETED)

   ✅ **Tab tự động chuyển:**
   - Nếu bước đầu tiên là "Khám tổng quát" → Tab "Khám chung" được chọn
   - Nếu bước đầu tiên là "Chụp X-Quang" → Tab "X-Quang" được chọn

   ✅ **Toast thứ ba (MỚI):**
   ```
   "Đang xem: [Tên bước] (Chế độ chỉ xem)"
   ```

   ✅ **Dữ liệu hiển thị:**
   - Ghi chú hiển thị trong EditText
   - Ảnh hiển thị (nếu có)

   ✅ **Chế độ READ-ONLY:**
   - EditText màu xám, không nhập được
   - Nút upload ảnh bị ẩn
   - Nút "Hoàn thành" và "Hủy" bị ẩn

6. **Test xem bước khác:**
   - Nhấp "Chỉnh sửa" trên bước khác trong danh sách
   - Quan sát: Chuyển tab, load dữ liệu, vẫn READ-ONLY

## 🔍 Dấu Hiệu Thành Công

### ✅ Nếu Thấy:
- Toast "Đã tải X bước đã hoàn thành..."
- Toast "Đang xem: ... (Chế độ chỉ xem)"
- Tab tự động chuyển
- Dữ liệu hiển thị
- EditText màu xám (không nhập được)

→ **TÍNH NĂNG HOẠT ĐỘNG!** 🎉

### ❌ Nếu Không Thấy:
- Không có toast về "bước đã hoàn thành"
- Tab không tự động chuyển
- Không có dữ liệu hiển thị
- EditText vẫn màu đen (nhập được)

→ **Có vấn đề, cần debug**

## 🐛 Troubleshooting

### Vấn Đề 1: Vẫn Như Cũ

**Nguyên nhân:** App cũ chưa được gỡ hoặc cache

**Giải pháp:**
```
1. Gỡ app hoàn toàn
2. Xóa cache: Settings → Apps → Toothly → Clear Cache
3. Xóa data: Settings → Apps → Toothly → Clear Data
4. Cài lại APK mới
```

### Vấn Đề 2: Không Có Toast

**Nguyên nhân:** Không có bước COMPLETED

**Giải pháp:**
```
1. Kiểm tra bệnh nhân có bước COMPLETED không
2. Thử với bệnh nhân khác
3. Tạo bước mới và hoàn thành nó
```

### Vấn Đề 3: Crash Khi Nhấp Bệnh Nhân

**Nguyên nhân:** Lỗi code hoặc dữ liệu null

**Giải pháp:**
```
1. Xem logcat để biết lỗi cụ thể
2. Báo lỗi với stack trace
```

## 📊 Checklist Test

- [ ] Gỡ app cũ
- [ ] Cài APK mới
- [ ] Đăng nhập thành công
- [ ] Vào Quản Lý Hàng Đợi
- [ ] Nhấp vào bệnh nhân có bước COMPLETED
- [ ] Thấy toast "Đã tải X bước đã hoàn thành"
- [ ] Tab tự động chuyển
- [ ] Thấy toast "Đang xem: ... (Chế độ chỉ xem)"
- [ ] Dữ liệu hiển thị
- [ ] EditText màu xám (READ-ONLY)
- [ ] Không nhập được text
- [ ] Nút upload ảnh bị ẩn
- [ ] Nút "Hoàn thành" bị ẩn
- [ ] Nhấp "Chỉnh sửa" trên bước khác → Chuyển tab
- [ ] Dữ liệu bước khác hiển thị

## 📸 Screenshot Cần Chụp

Nếu có vấn đề, vui lòng chụp:

1. **Toast messages** (3 toast)
2. **Tab được chọn** (màu highlight)
3. **EditText với dữ liệu** (màu xám)
4. **Danh sách bước** (status COMPLETED)
5. **Logcat** (nếu crash)

## 🎯 Kết Quả Mong Đợi

### Kịch Bản Hoàn Chỉnh

```
Bệnh nhân: Nguyễn Văn A
Bước 1: "Khám tổng quát" - COMPLETED
  - Ghi chú: "Răng số 6 bị sâu"
Bước 2: "Chụp X-Quang" - COMPLETED
  - Ghi chú: "Đã chụp răng số 6"
  - Ảnh: 2 ảnh
Bước 3: "Nhổ răng" - PENDING
```

**Khi nhấp vào bệnh nhân:**

```
1. Toast: "Đã tải phác đồ điều trị (3 bước)"
   ↓
2. Toast: "Đã tải 2 bước đã hoàn thành. Nhấp 'Chỉnh sửa' để xem chi tiết."
   ↓
3. Tab "Khám chung" được chọn tự động
   ↓
4. FragmentGeneralDental hiển thị
   ↓
5. EditText hiển thị: "Răng số 6 bị sâu"
   ↓
6. EditText màu xám (không nhập được)
   ↓
7. Toast: "Đang xem: Khám tổng quát (Chế độ chỉ xem)"
   ↓
8. Nút "Hoàn thành" và "Hủy" bị ẩn
   ↓
9. ✅ THÀNH CÔNG!
```

**Khi nhấp "Chỉnh sửa" trên bước "Chụp X-Quang":**

```
1. Tab chuyển sang "X-Quang"
   ↓
2. FragmentXray hiển thị
   ↓
3. EditText hiển thị: "Đã chụp răng số 6"
   ↓
4. RecyclerView hiển thị 2 ảnh
   ↓
5. EditText màu xám (READ-ONLY)
   ↓
6. Nút upload ảnh bị ẩn
   ↓
7. ✅ THÀNH CÔNG!
```

## 📞 Báo Kết Quả

Sau khi test, vui lòng báo:

1. **Có hoạt động không?** (Có/Không)
2. **Toast nào hiển thị?** (Chụp ảnh)
3. **Tab có tự động chuyển không?** (Có/Không)
4. **Dữ liệu có hiển thị không?** (Có/Không)
5. **EditText có màu xám không?** (Có/Không)
6. **Có nhập được text không?** (Có/Không - phải là KHÔNG)
7. **Có vấn đề gì không?** (Mô tả)

---

**Ngày:** 29/03/2026
**APK:** app-debug.apk (Build mới)
**Tính năng:** Auto-load completed steps (READ-ONLY)
**Trạng thái:** Sẵn sàng test
