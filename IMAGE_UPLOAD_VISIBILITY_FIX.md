# FIX: HIỂN THỊ SECTION UPLOAD ẢNH CHO MỌI STEP

**Ngày:** 28/03/2026  
**Status:** ✅ FIXED

---

## 🐛 VẤN ĐỀ

Khi bấm "Chỉnh sửa" trên step X-quang, section upload ảnh KHÔNG hiển thị.

**Nguyên nhân:**
```java
// Logic cũ - CHỈ hiển thị khi:
boolean isDiagnostic = step.getServiceName().contains("x-quang") || step.getServiceName().contains("chụp");
layout_result_images.setVisibility(isDiagnostic || step.isCompleted() ? View.VISIBLE : View.GONE);
```

**Vấn đề:**
- ❌ Chỉ hiển thị nếu service name chứa "x-quang" hoặc "chụp"
- ❌ Nếu service name là "Chụp X-Quang răng" thì OK, nhưng nếu là "X-Ray" hoặc tên khác thì KHÔNG hiển thị
- ❌ Không hiển thị cho các step khác (nhổ răng, phẫu thuật) mà cũng cần upload ảnh kết quả

---

## ✅ GIẢI PHÁP

### Thay đổi logic hiển thị

**File:** `mobile_android/app/src/main/java/com/hcmute/mobile_android/ui/activities/staff/DoctorWorkflowActivity.java`

**Method:** `onStepEdit()` - Line ~632

```java
// NEW LOGIC - Hiển thị cho MỌI step đang IN_PROGRESS hoặc COMPLETED
boolean showImageSection = "IN_PROGRESS".equals(step.getStatus()) || 
                          "COMPLETED".equals(step.getStatus());
layout_result_images.setVisibility(showImageSection ? View.VISIBLE : View.GONE);

// Check if this is a diagnostic/imaging service (for UI customization)
boolean isDiagnostic = step.getServiceName() != null && 
    (step.getServiceName().toLowerCase().contains("x-quang") || 
     step.getServiceName().toLowerCase().contains("chụp") ||
     step.getServiceName().toLowerCase().contains("x quang") ||
     step.getServiceName().toLowerCase().contains("ct scan"));

// Hide dental form for diagnostic services (they only need images)
if (toggleFormType != null) {
    toggleFormType.setVisibility(isDiagnostic ? View.GONE : View.VISIBLE);
}
View fragContainer = findViewById(R.id.fragmentContainerForm);
if (fragContainer != null) {
    fragContainer.setVisibility(isDiagnostic ? View.GONE : View.VISIBLE);
}
```

---

## 📊 LOGIC MỚI

### Hiển thị Section Upload Ảnh:
```
✅ Step status = IN_PROGRESS → SHOW
✅ Step status = COMPLETED → SHOW
❌ Step status = PENDING → HIDE
```

### Hiển thị Form Khám (Odontogram, etc.):
```
✅ Service thường (Khám, Nhổ răng, etc.) → SHOW form + SHOW upload
❌ Service chẩn đoán (X-quang, Chụp CT) → HIDE form + SHOW upload
```

---

## 🎯 USE CASES

### Case 1: Step X-Quang (Diagnostic)
```
User nhấn "Chỉnh sửa" step X-Quang
  ↓
Step status = IN_PROGRESS
  ↓
isDiagnostic = true (service name contains "x-quang")
  ↓
UI hiển thị:
  ✅ Section upload ảnh (VISIBLE)
  ❌ Toggle form type (GONE)
  ❌ Fragment container (GONE)
  ✅ Ghi chú bác sĩ (VISIBLE)
  ✅ Button "Hoàn thành" (VISIBLE)
```

### Case 2: Step Nhổ Răng (Treatment)
```
User nhấn "Chỉnh sửa" step Nhổ Răng
  ↓
Step status = IN_PROGRESS
  ↓
isDiagnostic = false
  ↓
UI hiển thị:
  ✅ Section upload ảnh (VISIBLE) - Có thể upload ảnh kết quả
  ✅ Toggle form type (VISIBLE)
  ✅ Fragment container (VISIBLE) - Odontogram, checklist
  ✅ Ghi chú bác sĩ (VISIBLE)
  ✅ Button "Hoàn thành" (VISIBLE)
```

### Case 3: Step Khám Tổng Quát (General)
```
User nhấn "Chỉnh sửa" step Khám
  ↓
Step status = IN_PROGRESS
  ↓
isDiagnostic = false
  ↓
UI hiển thị:
  ✅ Section upload ảnh (VISIBLE) - Có thể upload ảnh miệng
  ✅ Toggle form type (VISIBLE)
  ✅ Fragment container (VISIBLE) - Odontogram
  ✅ Ghi chú bác sĩ (VISIBLE)
  ✅ Button "Hoàn thành" (VISIBLE)
```

### Case 4: Step PENDING (Chưa bắt đầu)
```
Step status = PENDING
  ↓
UI hiển thị:
  ❌ Section upload ảnh (GONE)
  ✅ Toggle form type (VISIBLE)
  ✅ Fragment container (VISIBLE)
  ❌ Button "Hoàn thành" (GONE)
```

---

## 🔄 WORKFLOW

### Trước fix:
```
1. Bác sĩ X-quang nhấn "Chỉnh sửa" step X-Quang
2. ❌ Section upload ảnh KHÔNG hiển thị (nếu service name không match)
3. ❌ Không thể upload ảnh
4. ❌ Phải nhập ghi chú rồi hoàn thành mà không có ảnh
```

### Sau fix:
```
1. Bác sĩ X-quang nhấn "Chỉnh sửa" step X-Quang
2. ✅ Section upload ảnh HIỂN THỊ (vì step IN_PROGRESS)
3. ✅ Nhấn button "+" để upload ảnh
4. ✅ Upload ảnh X-Quang
5. ✅ Nhập ghi chú
6. ✅ Nhấn "Hoàn thành" với ảnh đính kèm
```

---

## 🧪 TEST SCENARIOS

### Test 1: Step X-Quang
```
1. Login bác sĩ X-quang (doc_xray@gmail.com)
2. Scan QR bệnh nhân có step X-Quang
3. Nhấn "Chỉnh sửa" trên step X-Quang
Expected:
   ✅ Section "Ảnh Kết Quả / X-Quang" hiển thị
   ✅ Button "+" upload hiển thị
   ✅ Form khám ẨN (vì là diagnostic)
   ✅ Ghi chú hiển thị
```

### Test 2: Step Nhổ Răng
```
1. Login bác sĩ Phòng tiểu phẫu
2. Scan QR bệnh nhân có step Nhổ Răng
3. Nhấn "Chỉnh sửa" trên step Nhổ Răng
Expected:
   ✅ Section "Ảnh Kết Quả / X-Quang" hiển thị
   ✅ Button "+" upload hiển thị
   ✅ Form khám HIỂN THỊ (toggle, odontogram)
   ✅ Ghi chú hiển thị
```

### Test 3: Step PENDING
```
1. Tạo plan với step mới (PENDING)
2. Nhấn "Chỉnh sửa" trên step PENDING
Expected:
   ❌ Section upload ảnh ẨN (chưa bắt đầu)
   ✅ Form khám hiển thị
   ❌ Button "Hoàn thành" ẨN
```

### Test 4: Upload ảnh cho step thường
```
1. Nhấn "Chỉnh sửa" step Khám Tổng Quát
2. Section upload ảnh hiển thị
3. Nhấn "+" và chọn ảnh miệng bệnh nhân
Expected:
   ✅ Upload thành công
   ✅ Ảnh hiển thị trong RecyclerView
   ✅ Counter: "1 ảnh"
```

---

## 📝 FILES MODIFIED

1. `mobile_android/app/src/main/java/com/hcmute/mobile_android/ui/activities/staff/DoctorWorkflowActivity.java`
   - Method: `onStepEdit()` - Line ~632
   - Changed visibility logic from service-name-based to status-based
   - Now shows image section for ALL IN_PROGRESS and COMPLETED steps

---

## ✅ COMPILATION STATUS

```bash
cd mobile_android
./gradlew assembleDebug
```

**Result:** ✅ BUILD SUCCESSFUL in 11s (35 tasks, 9 executed, 26 up-to-date)

---

## 💡 BENEFITS

### Before (Cũ):
- ❌ Chỉ hiển thị cho service có tên chứa "x-quang" hoặc "chụp"
- ❌ Không linh hoạt với tên service khác
- ❌ Không thể upload ảnh cho các step khác

### After (Mới):
- ✅ Hiển thị cho MỌI step đang IN_PROGRESS hoặc COMPLETED
- ✅ Linh hoạt với mọi loại service
- ✅ Bác sĩ có thể upload ảnh kết quả cho bất kỳ step nào
- ✅ Phù hợp với workflow thực tế (mọi điều trị đều có thể cần ảnh)

---

## 🎨 UI BEHAVIOR

### Diagnostic Services (X-Quang, CT Scan):
```
┌─────────────────────────────────────────┐
│ [Header: Bệnh nhân...]                  │
├─────────────────────────────────────────┤
│ ❌ Toggle Form Type (HIDDEN)            │
│ ❌ Odontogram/Form (HIDDEN)             │
├─────────────────────────────────────────┤
│ ✅ 📷 Ảnh Kết Quả / X-Quang   [2 ảnh]  │
│    [img1] [img2] [img3] ...      [ + ]  │
├─────────────────────────────────────────┤
│ ✅ Ghi chú bác sĩ                       │
│    [TextArea...]                        │
├─────────────────────────────────────────┤
│ ✅ [Hoàn thành]                         │
└─────────────────────────────────────────┘
```

### Treatment Services (Nhổ Răng, Khám):
```
┌─────────────────────────────────────────┐
│ [Header: Bệnh nhân...]                  │
├─────────────────────────────────────────┤
│ ✅ Toggle Form Type                     │
│    [Tổng quát] [Tiểu phẫu] [Niềng]    │
├─────────────────────────────────────────┤
│ ✅ Odontogram + Form                    │
│    [Sơ đồ răng...]                      │
├─────────────────────────────────────────┤
│ ✅ 📷 Ảnh Kết Quả / X-Quang   [1 ảnh]  │
│    [img1]                        [ + ]  │
├─────────────────────────────────────────┤
│ ✅ Ghi chú bác sĩ                       │
│    [TextArea...]                        │
├─────────────────────────────────────────┤
│ ✅ [Hoàn thành]                         │
└─────────────────────────────────────────┘
```

---

## 🚀 NEXT STEPS

1. **Install APK mới:**
   ```bash
   cd mobile_android
   ./gradlew installDebug
   ```

2. **Test workflow:**
   - Login bác sĩ X-quang
   - Nhấn "Chỉnh sửa" step X-Quang
   - Verify section upload ảnh hiển thị
   - Upload ảnh và hoàn thành

3. **Test với các step khác:**
   - Step Nhổ Răng → Verify có cả form + upload
   - Step Khám → Verify có cả odontogram + upload
   - Step PENDING → Verify không có upload

---

**Status:** ✅ FIXED  
**Build:** ✅ SUCCESS  
**Impact:** Section upload ảnh giờ hiển thị cho MỌI step đang làm việc
