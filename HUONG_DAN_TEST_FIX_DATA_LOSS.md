# HƯỚNG DẪN TEST FIX LỖI MẤT DỮ LIỆU VÀ CHỈNH SỬA

## CÀI ĐẶT APK MỚI

```bash
# Copy APK từ máy tính sang điện thoại
adb push mobile_android/app/build/outputs/apk/debug/app-debug.apk /sdcard/

# Hoặc cài đặt trực tiếp
adb install -r mobile_android/app/build/outputs/apk/debug/app-debug.apk
```

## CÁC LỖI ĐÃ FIX

### Lỗi 1: Mất dữ liệu khi hoàn thành lần đầu ✅
- **Trước**: Nhập đầy đủ → Nhấn "Hoàn thành" → Dữ liệu NULL
- **Sau**: Dữ liệu được lưu đầy đủ

### Lỗi 2: Lỗi đồng bộ khi chỉnh sửa lại ✅
- **Trước**: Hoàn thành → Chỉnh sửa → Nhập lại → Hoàn thành → Lỗi "Hồ sơ đã hoàn tất và bị khóa"
- **Sau**: Có thể chỉnh sửa và lưu nhiều lần không bị lỗi

### Lỗi 3: Sơ đồ răng không hiển thị dữ liệu ✅
- **Trước**: Chỉnh sửa bước đã hoàn thành → Sơ đồ răng trống
- **Sau**: Sơ đồ răng hiển thị đầy đủ dữ liệu đã lưu

## KỊCH BẢN TEST CHI TIẾT

### Test Case 1: Hoàn thành lần đầu

1. **Đăng nhập** với tài khoản bác sĩ
2. **Tra cứu bệnh nhân** bằng QR code
3. **Tạo dịch vụ** "Khám và tư vấn răng miệng"
4. **Nhấn "Bắt đầu"** trên dịch vụ vừa tạo
5. **Nhập dữ liệu**:
   - Chọn răng trên sơ đồ (ví dụ: R11, R12, R21)
   - Chọn tình trạng cho mỗi răng (Sâu răng, Đã trám, v.v.)
   - Nhập ghi chú cho mỗi răng
   - Nhập "Lý do khám"
   - Nhập "Chẩn đoán"
6. **Nhấn "Hoàn thành"**
7. **KIỂM TRA**:
   - ✅ Không có lỗi
   - ✅ Toast hiển thị "Hoàn tất bước khám"
   - ✅ Trạng thái bước chuyển sang "COMPLETED"
   - ✅ Dữ liệu KHÔNG bị NULL

### Test Case 2: Chỉnh sửa bước đã hoàn thành

1. **Sau khi hoàn thành** Test Case 1
2. **Nhấn "Chỉnh sửa"** trên bước vừa hoàn thành
3. **KIỂM TRA**:
   - ✅ Toast hiển thị "Đang chỉnh sửa lại: Khám và tư vấn răng miệng"
   - ✅ Sơ đồ răng hiển thị đầy đủ các răng đã chọn
   - ✅ Màu sắc răng đúng với tình trạng đã lưu
   - ✅ Trường "Lý do khám" hiển thị dữ liệu đã lưu
   - ✅ Trường "Chẩn đoán" hiển thị dữ liệu đã lưu
4. **Chỉnh sửa dữ liệu**:
   - Thêm răng mới (ví dụ: R13)
   - Thay đổi tình trạng răng cũ
   - Sửa "Lý do khám"
   - Sửa "Chẩn đoán"
5. **Nhấn "Hoàn thành"**
6. **KIỂM TRA**:
   - ✅ KHÔNG có lỗi "Hồ sơ đã hoàn tất và bị khóa"
   - ✅ Toast hiển thị "Hoàn tất bước khám"
   - ✅ Dữ liệu mới được lưu thành công

### Test Case 3: Chỉnh sửa nhiều lần

1. **Hoàn thành** → **Chỉnh sửa** → **Hoàn thành** → **Chỉnh sửa** → **Hoàn thành**
2. **KIỂM TRA mỗi lần**:
   - ✅ Không có lỗi
   - ✅ Sơ đồ răng luôn hiển thị dữ liệu đã lưu
   - ✅ Các trường text luôn hiển thị dữ liệu đã lưu
   - ✅ Dữ liệu mới được lưu thành công

### Test Case 4: Test với các loại dịch vụ khác

Lặp lại Test Case 1-3 với:
- **X-quang răng** (có upload ảnh)
- **Nhổ răng / Tiểu phẫu** (có checklist)
- **Chỉnh nha** (có kế hoạch điều trị)

## ĐIỂM QUAN TRỌNG CẦN KIỂM TRA

### 1. Dữ liệu không bị mất
- Sau khi nhấn "Hoàn thành" lần đầu, dữ liệu phải được lưu
- Không có trường nào bị NULL

### 2. Chỉnh sửa không bị lỗi
- Khi nhấn "Chỉnh sửa" bước đã hoàn thành, không có lỗi backend
- Có thể lưu lại nhiều lần

### 3. Sơ đồ răng hiển thị đúng
- Khi chỉnh sửa, sơ đồ răng hiển thị đầy đủ răng đã chọn
- Màu sắc răng đúng với tình trạng (đỏ = sâu răng, xanh = đã trám, v.v.)

### 4. Không có lỗi "Hồ sơ đã hoàn tất và bị khóa"
- Lỗi này KHÔNG được xuất hiện khi chỉnh sửa và lưu lại

## LOG CẦN KIỂM TRA

Khi test, mở Logcat và lọc theo tag:
```
adb logcat | grep -E "FragmentGeneralDental|DoctorWorkflowActivity"
```

### Log khi chỉnh sửa bước đã hoàn thành:
```
FragmentGeneralDental: === setData() called ===
FragmentGeneralDental: toothCustomNotesMap size after setData: 3
FragmentGeneralDental: Loaded tooth R11: Sâu răng - cần điều trị
FragmentGeneralDental: Loaded tooth R12: Đã trám
FragmentGeneralDental: Loaded tooth R21: BN yêu cầu
```

### Log khi lưu dữ liệu:
```
FragmentGeneralDental: === getFormDataNotes() called ===
FragmentGeneralDental: toothCustomNotesMap size: 3
FragmentGeneralDental: Final result: 'Lý do: Đau răng...'
```

## KẾT QUẢ MONG ĐỢI

✅ **TẤT CẢ** các test case phải PASS
✅ **KHÔNG CÓ** lỗi "Hồ sơ đã hoàn tất và bị khóa"
✅ **KHÔNG CÓ** dữ liệu bị NULL
✅ Sơ đồ răng **LUÔN HIỂN THỊ** dữ liệu đã lưu

## NẾU CÓ LỖI

1. **Chụp màn hình** lỗi
2. **Copy log** từ Logcat
3. **Ghi lại** các bước đã làm
4. **Báo cáo** chi tiết để fix tiếp

## THAY ĐỔI KỸ THUẬT

### 1. DoctorWorkflowActivity.java
- `onStepEdit()`: Gọi API `cancelTreatmentStep` khi chỉnh sửa bước COMPLETED
- `continueStepEdit()`: Method mới để xử lý logic sau khi API trả về

### 2. FragmentGeneralDental.java
- `setData()`: Parse dữ liệu răng từ chuỗi "Tình trạng răng:"
- Cập nhật `toothCustomNotesMap` và sơ đồ răng

## BUILD INFO

- **Build time**: 2026-03-29
- **APK location**: `mobile_android/app/build/outputs/apk/debug/app-debug.apk`
- **Build status**: ✅ SUCCESS
