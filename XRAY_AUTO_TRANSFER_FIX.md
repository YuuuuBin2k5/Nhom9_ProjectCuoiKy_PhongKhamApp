# FIX: Tự động chuyển phòng X-quang

## VẤN ĐỀ

Theo user story, khi bác sĩ chỉ định bệnh nhân đi chụp X-quang, bệnh nhân phải được **TỰ ĐỘNG CHUYỂN** sang phòng X-quang và vào hàng đợi của phòng đó. Nhưng logic hiện tại THIẾU chức năng này.

## USER STORY YÊU CẦU

> **Tình huống thực tế**: Khám nha khoa rất hay phải đi chụp X-quang/Panorama. Khi tôi chỉ định bệnh nhân A đi chụp X-quang, trạng thái của A chuyển sang "Đang làm cận lâm sàng". Lúc này tôi gọi bệnh nhân B vào khám.
>
> **Edge Case**: Khi A chụp xong và cầm phim quay lại, A không phải xếp hàng lại từ đầu. Lễ tân (hoặc kỹ thuật viên X-quang) sẽ update trạng thái phim đã có, A lập tức được đẩy vào danh sách Priority.

## PHÂN TÍCH

### Backend đã có sẵn:
- ✅ API `POST /api/queue/{id}/transfer-xray` - Chuyển bệnh nhân sang phòng X-quang
- ✅ API `POST /api/queue/{id}/complete-xray` - Đánh dấu hoàn thành X-quang và trả về phòng khám
- ✅ Logic trong `CheckInQueueService.transferToXRay()` - Tự động tìm phòng X-quang

### Mobile app THIẾU:
- ❌ Khi bác sĩ **THÊM** dịch vụ X-quang → Không tự động chuyển phòng
- ❌ Khi bác sĩ **BẮT ĐẦU** bước X-quang → Không tự động chuyển phòng

## GIẢI PHÁP

### Fix 1: Tự động chuyển khi THÊM dịch vụ X-quang

**File**: `DoctorWorkflowActivity.java`

**Method**: `addServiceAsStep()`

```java
private void addServiceAsStep(ServiceItem svc, Integer toothNumber) {
    // ... existing code to add step ...
    
    // CRITICAL FIX: Auto-transfer to X-ray room when adding X-ray service
    boolean isXrayService = svc.getName() != null && 
            (svc.getName().toLowerCase().contains("x-quang") || 
             svc.getName().toLowerCase().contains("xquang") ||
             svc.getName().toLowerCase().contains("x quang") ||
             svc.getName().toLowerCase().contains("panorama"));
    
    // Auto-save immediately so the step gets an ID from backend
    saveTreatmentPlanInternal(true, () -> {
        // Reload to get the step IDs from backend
        loadTreatmentPlanForRoom(currentTreatmentPlanId);
        
        // If X-ray service, transfer patient to X-ray room
        if (isXrayService && currentPatient != null && currentPatient.getQueueId() != null && currentPatient.getQueueId() > 0) {
            transferPatientToXRay(currentPatient.getQueueId(), svc.getName());
        }
    });
}
```

### Fix 2: Tự động chuyển khi BẮT ĐẦU bước X-quang

**File**: `DoctorWorkflowActivity.java`

**Method**: `continueStepEdit()`

```java
private void continueStepEdit(TreatmentPlan.Step step) {
    // If the step is PENDING, call the "start" API to move it to IN_PROGRESS
    if ("PENDING".equals(step.getStatus())) {
        if (step.getId() != null) {
            // CRITICAL FIX: Check if this is X-ray service - transfer patient first
            boolean isXrayService = step.getServiceName() != null && 
                    (step.getServiceName().toLowerCase().contains("x-quang") || 
                     step.getServiceName().toLowerCase().contains("xquang") ||
                     step.getServiceName().toLowerCase().contains("x quang") ||
                     step.getServiceName().toLowerCase().contains("panorama"));
            
            if (isXrayService && currentPatient != null && currentPatient.getQueueId() != null && currentPatient.getQueueId() > 0) {
                // Transfer to X-ray room first, then start the step
                transferPatientToXRay(currentPatient.getQueueId(), step.getServiceName());
                return; // Don't continue - patient is transferred
            }
            
            // ... existing code to start step ...
        }
    }
    // ... rest of method ...
}
```

### Fix 3: Method mới để chuyển phòng

**File**: `DoctorWorkflowActivity.java`

**New Method**: `transferPatientToXRay()`

```java
/**
 * Transfer patient to X-ray room
 */
private void transferPatientToXRay(Long queueId, String serviceName) {
    // Call API to transfer patient to X-ray room
    apiService.transferToXRay(queueId, new java.util.HashMap<>()).enqueue(new Callback<Void>() {
        @Override
        public void onResponse(Call<Void> call, Response<Void> response) {
            if (response.isSuccessful()) {
                new AlertDialog.Builder(DoctorWorkflowActivity.this)
                    .setTitle("Chuyển phòng X-quang")
                    .setMessage("Bệnh nhân đã được chuyển sang phòng X-quang để thực hiện dịch vụ: " + serviceName + 
                              "\n\nHệ thống sẽ tự động đưa bệnh nhân trở lại sau khi hoàn thành.")
                    .setPositiveButton("OK", (dialog, which) -> {
                        // Close this activity - patient is now in X-ray room
                        finish();
                    })
                    .setCancelable(false)
                    .show();
            } else {
                Toast.makeText(DoctorWorkflowActivity.this, "Lỗi chuyển phòng X-quang", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onFailure(Call<Void> call, Throwable t) {
            Toast.makeText(DoctorWorkflowActivity.this, "Lỗi kết nối khi chuyển phòng", Toast.LENGTH_SHORT).show();
        }
    });
}
```

## LUỒNG HOẠT ĐỘNG SAU KHI FIX

### Kịch bản 1: Bác sĩ thêm dịch vụ X-quang

1. **Bác sĩ đang khám bệnh nhân A**
   - Bệnh nhân A đang ở phòng khám (queueId = 123)

2. **Bác sĩ click vào răng → Chọn "X-quang răng"**
   - `addServiceAsStep()` được gọi
   - Thêm step X-quang vào treatment plan
   - Lưu plan lên backend

3. **Sau khi lưu thành công**
   - Phát hiện service name chứa "x-quang"
   - Gọi `transferPatientToXRay(123, "X-quang răng")`

4. **API `transferToXRay` được gọi**
   - Backend tìm phòng X-quang
   - Chuyển bệnh nhân A sang phòng X-quang
   - Status: WAITING → PAUSED_FOR_TEST
   - Lưu originalRoomId để biết phòng ban đầu

5. **Dialog hiển thị**
   - "Bệnh nhân đã được chuyển sang phòng X-quang..."
   - Bác sĩ bấm OK
   - Activity đóng lại

6. **Bác sĩ gọi bệnh nhân B vào khám**
   - Bệnh nhân A đang ở phòng X-quang
   - Bác sĩ tiếp tục khám bệnh nhân B

### Kịch bản 2: Bác sĩ bắt đầu bước X-quang có sẵn

1. **Treatment plan đã có bước X-quang (status = PENDING)**
   - Bệnh nhân A đang ở phòng khám

2. **Bác sĩ click "Bắt đầu" trên bước X-quang**
   - `onStepEdit()` được gọi
   - `continueStepEdit()` được gọi

3. **Phát hiện step PENDING và là X-ray service**
   - Gọi `transferPatientToXRay()`
   - Return ngay (không tiếp tục start step)

4. **Bệnh nhân được chuyển sang phòng X-quang**
   - Dialog hiển thị
   - Activity đóng lại

### Kịch bản 3: Bệnh nhân hoàn thành X-quang

1. **Kỹ thuật viên X-quang hoàn thành chụp**
   - Gọi API `completeXRay(queueId)`

2. **Backend xử lý**
   - Status: PAUSED_FOR_TEST → RETURNED_PRIORITY
   - Priority level: +10
   - Chuyển về phòng khám ban đầu (originalRoomId)

3. **Bác sĩ thấy bệnh nhân A trong tab "Priority"**
   - Gọi bệnh nhân A vào
   - Đọc kết quả X-quang
   - Tiếp tục điều trị

## ĐIỀU KIỆN CHUYỂN PHÒNG

### Kiểm tra service name:
```java
boolean isXrayService = serviceName != null && 
        (serviceName.toLowerCase().contains("x-quang") || 
         serviceName.toLowerCase().contains("xquang") ||
         serviceName.toLowerCase().contains("x quang") ||
         serviceName.toLowerCase().contains("panorama"));
```

### Kiểm tra queueId:
```java
if (currentPatient != null && 
    currentPatient.getQueueId() != null && 
    currentPatient.getQueueId() > 0) {
    // Transfer
}
```

## FILES MODIFIED

- `mobile_android/app/src/main/java/com/hcmute/mobile_android/ui/activities/staff/DoctorWorkflowActivity.java`
  - Method `addServiceAsStep()`: Thêm logic chuyển phòng sau khi thêm dịch vụ X-quang
  - Method `continueStepEdit()`: Thêm logic chuyển phòng khi bắt đầu bước X-quang
  - New Method `transferPatientToXRay()`: Gọi API và hiển thị dialog

## BUILD STATUS

✅ **Build Successful**
- APK: `mobile_android/app/build/outputs/apk/debug/app-debug.apk`
- No compilation errors

## TESTING

### Test Case 1: Thêm dịch vụ X-quang

1. Bác sĩ đang khám bệnh nhân
2. Click vào răng → Chọn "X-quang răng"
3. ✅ **KIỂM TRA**:
   - Dialog hiển thị: "Bệnh nhân đã được chuyển sang phòng X-quang..."
   - Activity đóng lại
   - Bệnh nhân xuất hiện trong hàng đợi phòng X-quang
   - Status = PAUSED_FOR_TEST

### Test Case 2: Bắt đầu bước X-quang

1. Treatment plan có bước X-quang (PENDING)
2. Bác sĩ click "Bắt đầu" trên bước X-quang
3. ✅ **KIỂM TRA**:
   - Dialog hiển thị: "Bệnh nhân đã được chuyển sang phòng X-quang..."
   - Activity đóng lại
   - Bệnh nhân xuất hiện trong hàng đợi phòng X-quang

### Test Case 3: Hoàn thành X-quang

1. Kỹ thuật viên X-quang hoàn thành
2. Gọi API `completeXRay`
3. ✅ **KIỂM TRA**:
   - Bệnh nhân trở về phòng khám ban đầu
   - Status = RETURNED_PRIORITY
   - Xuất hiện trong tab "Priority" của bác sĩ

## SUMMARY

**Vấn đề**: Không tự động chuyển phòng khi thêm/bắt đầu dịch vụ X-quang

**Giải pháp**: 
- Thêm logic phát hiện dịch vụ X-quang
- Tự động gọi API `transferToXRay`
- Hiển thị dialog thông báo
- Đóng activity để bác sĩ tiếp tục khám bệnh nhân khác

**Kết quả**: 
- ✅ Tự động chuyển phòng khi thêm dịch vụ X-quang
- ✅ Tự động chuyển phòng khi bắt đầu bước X-quang
- ✅ Bệnh nhân trở về với priority cao sau khi hoàn thành
- ✅ Đúng theo user story yêu cầu
