# ADMIN PHASE 1 - CRITICAL FIXES IMPLEMENTATION

## 🎯 Objective
Fix các vấn đề nghiêm trọng nhất để admin module có thể hoạt động đúng

## 📋 Tasks Overview

### ✅ Task 1: Fix API Mismatch (AdminReportController)
**Status:** Ready to implement  
**Priority:** CRITICAL  
**Estimated Time:** 30 minutes

**Changes:**
1. Update AdminReportController to accept date range
2. Update AdminReportService to handle date range
3. Keep backward compatibility with year/month

### ✅ Task 2: Fix AdminMainActivity Navigation
**Status:** Ready to implement  
**Priority:** CRITICAL  
**Estimated Time:** 20 minutes

**Changes:**
1. Remove "Doctor Workflow" card
2. Add proper admin cards
3. Update layout

### ✅ Task 3: Add CRUD to AdminRoomActivity
**Status:** Ready to implement  
**Priority:** HIGH  
**Estimated Time:** 2 hours

**Changes:**
1. Add FAB button
2. Create add/edit room dialog
3. Implement delete with confirmation
4. Update adapter with click listeners

### ✅ Task 4: Add Edit/Delete to AdminServiceActivity
**Status:** Ready to implement  
**Priority:** HIGH  
**Estimated Time:** 2 hours

**Changes:**
1. Add click listener to adapter
2. Create edit dialog
3. Implement delete with confirmation

### ✅ Task 5: Add Edit to AdminDoctorActivity
**Status:** Ready to implement  
**Priority:** HIGH  
**Estimated Time:** 1.5 hours

**Changes:**
1. Add click listener to adapter
2. Create edit dialog
3. Add room assignment

---

## 🚀 Implementation Details

### Task 1: Fix API Mismatch

#### Backend Changes

**File:** `clinic_backend/src/main/java/com/hcmute/clinic/controller/AdminReportController.java`

```java
@GetMapping("/revenue")
public ResponseEntity<RevenueReportDto> getRevenueReport(
    @RequestParam(required = false) Integer year,
    @RequestParam(required = false) Integer month,
    @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
    @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate
) {
    // Support both old (year/month) and new (date range) API
    if (startDate != null && endDate != null) {
        return ResponseEntity.ok(adminReportService.getRevenueReportByDateRange(startDate, endDate));
    } else if (year != null && month != null) {
        return ResponseEntity.ok(adminReportService.getRevenueReport(year, month));
    } else {
        // Default to current month
        LocalDate now = LocalDate.now();
        return ResponseEntity.ok(adminReportService.getRevenueReport(now.getYear(), now.getMonthValue()));
    }
}
```

**File:** `clinic_backend/src/main/java/com/hcmute/clinic/service/AdminReportService.java`

Add new method:
```java
public RevenueReportDto getRevenueReportByDateRange(LocalDate start, LocalDate end) {
    LocalDateTime startDateTime = start.atStartOfDay();
    LocalDateTime endDateTime = end.atTime(23, 59, 59);
    
    List<Appointment> allAppointments = appointmentRepository
        .findByAppointmentDatetimeBetween(startDateTime, endDateTime);
    
    // ... rest of logic similar to getRevenueReport
}
```

#### Frontend Changes

**File:** `mobile_android/app/src/main/java/com/hcmute/mobile_android/network/ApiService.java`

Update method signature:
```java
@GET("api/admin/reports/revenue")
Call<RevenueReport> getRevenueReport(
    @Query("startDate") String startDate,
    @Query("endDate") String endDate
);
```

---

### Task 2: Fix AdminMainActivity Navigation

#### Layout Changes

**File:** `mobile_android/app/src/main/res/layout/activity_admin_main.xml`

Remove:
```xml
<com.google.android.material.card.MaterialCardView
    android:id="@+id/cardDoctorWorkflow"
    ... />
```

Add:
```xml
<com.google.android.material.card.MaterialCardView
    android:id="@+id/cardAppointments"
    android:layout_width="0dp"
    android:layout_height="120dp"
    android:layout_margin="8dp"
    app:cardCornerRadius="12dp"
    app:cardElevation="4dp">
    
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical"
        android:gravity="center"
        android:padding="16dp">
        
        <ImageView
            android:layout_width="48dp"
            android:layout_height="48dp"
            android:src="@drawable/ic_calendar"
            android:tint="@color/primary" />
        
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Appointments"
            android:textSize="14sp"
            android:textStyle="bold"
            android:layout_marginTop="8dp" />
    </LinearLayout>
</com.google.android.material.card.MaterialCardView>

<com.google.android.material.card.MaterialCardView
    android:id="@+id/cardSettings"
    ... />

<com.google.android.material.card.MaterialCardView
    android:id="@+id/cardAuditLogs"
    ... />
```

#### Java Changes

**File:** `mobile_android/app/src/main/java/com/hcmute/mobile_android/ui/activities/AdminMainActivity.java`

```java
private MaterialCardView cardServices, cardRooms, cardDoctors, cardQueue, 
                         cardAppointments, cardSettings, cardAuditLogs, cardLogout;

private void initViews() {
    cardServices = findViewById(R.id.cardServices);
    cardRooms = findViewById(R.id.cardRooms);
    cardDoctors = findViewById(R.id.cardDoctors);
    cardQueue = findViewById(R.id.cardQueue);
    cardAppointments = findViewById(R.id.cardAppointments);
    cardSettings = findViewById(R.id.cardSettings);
    cardAuditLogs = findViewById(R.id.cardAuditLogs);
    cardLogout = findViewById(R.id.cardLogout);
}

private void setupClickListeners() {
    cardServices.setOnClickListener(v -> 
        startActivity(new Intent(this, AdminServiceActivity.class)));
    
    cardRooms.setOnClickListener(v -> 
        startActivity(new Intent(this, AdminRoomActivity.class)));
    
    cardDoctors.setOnClickListener(v -> 
        startActivity(new Intent(this, AdminDoctorActivity.class)));
    
    cardQueue.setOnClickListener(v -> 
        startActivity(new Intent(this, QueueManagementActivity.class)));
    
    cardAppointments.setOnClickListener(v -> {
        // TODO: Create AdminAppointmentActivity
        Toast.makeText(this, "Coming soon", Toast.LENGTH_SHORT).show();
    });
    
    cardSettings.setOnClickListener(v -> {
        // TODO: Create ClinicSettingsActivity
        Toast.makeText(this, "Coming soon", Toast.LENGTH_SHORT).show();
    });
    
    cardAuditLogs.setOnClickListener(v -> {
        // TODO: Create AuditLogActivity
        Toast.makeText(this, "Coming soon", Toast.LENGTH_SHORT).show();
    });
    
    cardLogout.setOnClickListener(v -> logout());
}
```

---

### Task 3: Add CRUD to AdminRoomActivity

#### Create Dialog Layout

**File:** `mobile_android/app/src/main/res/layout/dialog_add_room.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:padding="24dp">

    <TextView
        android:id="@+id/tvDialogTitle"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Thêm Phòng Khám"
        android:textSize="20sp"
        android:textStyle="bold"
        android:layout_marginBottom="16dp" />

    <com.google.android.material.textfield.TextInputLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="Tên phòng"
        style="@style/Widget.Material3.TextInputLayout.OutlinedBox"
        android:layout_marginBottom="12dp">

        <com.google.android.material.textfield.TextInputEditText
            android:id="@+id/etRoomName"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:inputType="text"
            android:maxLines="1" />
    </com.google.android.material.textfield.TextInputLayout>

    <com.google.android.material.textfield.TextInputLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="Mô tả"
        style="@style/Widget.Material3.TextInputLayout.OutlinedBox"
        android:layout_marginBottom="16dp">

        <com.google.android.material.textfield.TextInputEditText
            android:id="@+id/etRoomDescription"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:inputType="textMultiLine"
            android:minLines="3"
            android:maxLines="5"
            android:gravity="top" />
    </com.google.android.material.textfield.TextInputLayout>

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:gravity="end">

        <com.google.android.material.button.MaterialButton
            android:id="@+id/btnCancel"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Hủy"
            style="@style/Widget.Material3.Button.TextButton"
            android:layout_marginEnd="8dp" />

        <com.google.android.material.button.MaterialButton
            android:id="@+id/btnSave"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Lưu" />
    </LinearLayout>
</LinearLayout>
```

#### Update AdminRoomActivity

Add methods:
```java
private void showAddRoomDialog() {
    showRoomDialog(null);
}

private void showEditRoomDialog(RoomItem room) {
    showRoomDialog(room);
}

private void showRoomDialog(RoomItem existingRoom) {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_room, null);
    
    TextView tvTitle = view.findViewById(R.id.tvDialogTitle);
    EditText etName = view.findViewById(R.id.etRoomName);
    EditText etDesc = view.findViewById(R.id.etRoomDescription);
    
    if (existingRoom != null) {
        tvTitle.setText("Sửa Phòng Khám");
        etName.setText(existingRoom.getName());
        etDesc.setText(existingRoom.getDescription());
    }
    
    builder.setView(view);
    AlertDialog dialog = builder.create();
    
    view.findViewById(R.id.btnSave).setOnClickListener(v -> {
        String name = etName.getText().toString().trim();
        String desc = etDesc.getText().toString().trim();
        
        if (name.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên phòng", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (existingRoom != null) {
            updateRoom(existingRoom.getId(), name, desc, dialog);
        } else {
            createRoom(name, desc, dialog);
        }
    });
    
    view.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
    dialog.show();
}

private void createRoom(String name, String description, AlertDialog dialog) {
    RoomRequest request = new RoomRequest(name, description);
    apiService.createRoom(request).enqueue(new Callback<RoomItem>() {
        @Override
        public void onResponse(Call<RoomItem> call, Response<RoomItem> response) {
            if (response.isSuccessful()) {
                Toast.makeText(AdminRoomActivity.this, "Thêm phòng thành công", Toast.LENGTH_SHORT).show();
                loadRooms();
                dialog.dismiss();
            } else {
                Toast.makeText(AdminRoomActivity.this, "Lỗi khi thêm phòng", Toast.LENGTH_SHORT).show();
            }
        }
        
        @Override
        public void onFailure(Call<RoomItem> call, Throwable t) {
            Toast.makeText(AdminRoomActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
        }
    });
}

private void updateRoom(Long id, String name, String description, AlertDialog dialog) {
    RoomRequest request = new RoomRequest(name, description);
    apiService.updateRoom(id, request).enqueue(new Callback<RoomItem>() {
        @Override
        public void onResponse(Call<RoomItem> call, Response<RoomItem> response) {
            if (response.isSuccessful()) {
                Toast.makeText(AdminRoomActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                loadRooms();
                dialog.dismiss();
            } else {
                Toast.makeText(AdminRoomActivity.this, "Lỗi khi cập nhật", Toast.LENGTH_SHORT).show();
            }
        }
        
        @Override
        public void onFailure(Call<RoomItem> call, Throwable t) {
            Toast.makeText(AdminRoomActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
        }
    });
}

private void deleteRoom(RoomItem room) {
    new AlertDialog.Builder(this)
        .setTitle("Xác nhận xóa")
        .setMessage("Bạn có chắc muốn xóa phòng " + room.getName() + "?")
        .setPositiveButton("Xóa", (dialog, which) -> {
            apiService.deleteRoom(room.getId()).enqueue(new Callback<MessageResponse>() {
                @Override
                public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(AdminRoomActivity.this, "Đã xóa phòng", Toast.LENGTH_SHORT).show();
                        loadRooms();
                    } else {
                        Toast.makeText(AdminRoomActivity.this, "Lỗi khi xóa", Toast.LENGTH_SHORT).show();
                    }
                }
                
                @Override
                public void onFailure(Call<MessageResponse> call, Throwable t) {
                    Toast.makeText(AdminRoomActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            });
        })
        .setNegativeButton("Hủy", null)
        .show();
}
```

---

## 📝 Implementation Checklist

### Backend
- [ ] Update AdminReportController with date range support
- [ ] Add getRevenueReportByDateRange to AdminReportService
- [ ] Update getDoctorPerformance to support date range
- [ ] Update getTopServices to support date range
- [ ] Test all APIs with Postman

### Frontend - AdminMainActivity
- [ ] Remove cardDoctorWorkflow from layout
- [ ] Add cardAppointments to layout
- [ ] Add cardSettings to layout
- [ ] Add cardAuditLogs to layout
- [ ] Update Java code
- [ ] Test navigation

### Frontend - AdminRoomActivity
- [ ] Create dialog_add_room.xml
- [ ] Add FAB button to layout
- [ ] Implement showRoomDialog method
- [ ] Implement createRoom method
- [ ] Implement updateRoom method
- [ ] Implement deleteRoom method
- [ ] Update adapter with click listeners
- [ ] Test CRUD operations

### Frontend - AdminServiceActivity
- [ ] Add edit functionality
- [ ] Add delete functionality
- [ ] Add confirmation dialogs
- [ ] Update adapter
- [ ] Test CRUD operations

### Frontend - AdminDoctorActivity
- [ ] Add edit functionality
- [ ] Add room assignment
- [ ] Update adapter
- [ ] Test CRUD operations

### Frontend - AdminDashboardFragment
- [ ] Update API call to use date range
- [ ] Test with new API
- [ ] Add error handling

### API Service
- [ ] Add createRoom endpoint
- [ ] Add updateRoom endpoint
- [ ] Add deleteRoom endpoint
- [ ] Add updateService endpoint
- [ ] Add deleteService endpoint
- [ ] Add updateDoctor endpoint
- [ ] Update getRevenueReport signature

---

## 🧪 Testing Plan

### Unit Tests
- [ ] Test AdminReportService.getRevenueReportByDateRange
- [ ] Test date range validation
- [ ] Test backward compatibility

### Integration Tests
- [ ] Test AdminReportController endpoints
- [ ] Test AdminRoomController CRUD
- [ ] Test AdminDoctorController update

### UI Tests
- [ ] Test AdminMainActivity navigation
- [ ] Test AdminRoomActivity CRUD flow
- [ ] Test AdminServiceActivity edit/delete
- [ ] Test AdminDoctorActivity edit
- [ ] Test AdminDashboardFragment with new API

### Manual Tests
- [ ] Test all admin flows end-to-end
- [ ] Test error scenarios
- [ ] Test with slow network
- [ ] Test with no data
- [ ] Test with large datasets

---

## 📊 Progress Tracking

| Task | Status | Time Spent | Notes |
|------|--------|------------|-------|
| Fix API Mismatch | 🔄 In Progress | - | - |
| Fix Navigation | ⏳ Pending | - | - |
| Add Room CRUD | ⏳ Pending | - | - |
| Add Service Edit/Delete | ⏳ Pending | - | - |
| Add Doctor Edit | ⏳ Pending | - | - |

---

**Next:** Start implementing fixes in order of priority
