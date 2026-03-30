# 🔧 TASK 2.5 - INTEGRATION GUIDE

**Task:** Integrate ToothServiceDialog and GeneralServicesList into DoctorWorkflowActivity
**Estimated Time:** 1 day
**Status:** READY TO IMPLEMENT

---

## 📋 INTEGRATION CHECKLIST

### Step 1: Add OdontogramView Listener
**File:** `DoctorWorkflowActivity.java`
**Location:** In `initViews()` method

```java
// Find OdontogramView (if not already in layout)
OdontogramView odontogramView = findViewById(R.id.odontogramView);

// Set listener for tooth selection
odontogramView.setOnToothSelectedListener(toothNumber -> {
    if (currentTreatmentPlanId == null) {
        Toast.makeText(this, "Vui lòng tạo phác đồ điều trị trước", Toast.LENGTH_SHORT).show();
        return;
    }
    
    // Get next sequence order
    int nextSequence = treatmentSteps.size() + 1;
    
    // Show ToothServiceDialog
    ToothServiceDialog dialog = ToothServiceDialog.newInstance(
        currentTreatmentPlanId,
        String.valueOf(toothNumber),
        nextSequence
    );
    
    dialog.setOnServiceSelectedListener(new ToothServiceDialog.OnServiceSelectedListener() {
        @Override
        public void onServiceSelected(ToothServiceResponse response) {
            // Add to treatment steps
            TreatmentPlan.Step step = new TreatmentPlan.Step();
            step.setId(response.getStepId());
            step.setServiceName(response.getServiceName());
            step.setToothNumber(response.getToothNumber());
            step.setActualPrice(response.getPrice());
            step.setStatus("PENDING");
            
            treatmentSteps.add(step);
            stepAdapter.notifyDataSetChanged();
            
            // Update total cost
            updateTotalEstimate();
            
            Toast.makeText(DoctorWorkflowActivity.this,
                "Đã thêm: " + response.getServiceName(),
                Toast.LENGTH_SHORT).show();
        }
        
        @Override
        public void onError(String message) {
            Toast.makeText(DoctorWorkflowActivity.this,
                "Lỗi: " + message,
                Toast.LENGTH_SHORT).show();
        }
    });
    
    dialog.show(getSupportFragmentManager(), "ToothServiceDialog");
});
```

---

### Step 2: Add GeneralServicesList to Layout
**File:** `activity_doctor_workflow.xml`
**Location:** After OdontogramView section

```xml
<!-- General Services Section -->
<LinearLayout
    android:id="@+id/layoutGeneralServices"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:padding="16dp"
    android:layout_marginTop="16dp">

    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Dịch Vụ Tổng Quát"
        android:textSize="16sp"
        android:textStyle="bold"
        android:textColor="@android:color/black"
        android:layout_marginBottom="8dp" />

    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/rvGeneralServices"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:nestedScrollingEnabled="false" />

</LinearLayout>
```

---

### Step 3: Setup GeneralServicesList in Activity
**File:** `DoctorWorkflowActivity.java`
**Location:** In `initViews()` method

```java
// Setup general services list
RecyclerView rvGeneralServices = findViewById(R.id.rvGeneralServices);
rvGeneralServices.setLayoutManager(new LinearLayoutManager(this));

List<ServiceItem> generalServices = getGeneralServices();
GeneralServiceAdapter generalServiceAdapter = new GeneralServiceAdapter(
    generalServices,
    service -> onGeneralServiceSelected(service)
);
rvGeneralServices.setAdapter(generalServiceAdapter);
```

---

### Step 4: Add Helper Methods
**File:** `DoctorWorkflowActivity.java`
**Location:** Add new methods to the class

```java
/**
 * Get the 6 general services
 */
private List<ServiceItem> getGeneralServices() {
    List<ServiceItem> services = new ArrayList<>();
    
    // Service IDs from backend DataSeed.java
    services.add(new ServiceItem(1L, "Khám và tư vấn", 100000.0));
    services.add(new ServiceItem(2L, "Chụp X-quang", 200000.0));
    services.add(new ServiceItem(3L, "Lấy cao & đánh bóng", 250000.0));
    services.add(new ServiceItem(5L, "Điều trị tủy", 1500000.0));
    services.add(new ServiceItem(8L, "Tẩy trắng", 2500000.0));
    services.add(new ServiceItem(10L, "Niềng răng", 30000000.0));
    
    return services;
}

/**
 * Handle general service selection
 */
private void onGeneralServiceSelected(ServiceItem service) {
    if (currentTreatmentPlanId == null) {
        Toast.makeText(this, "Vui lòng tạo phác đồ điều trị trước", Toast.LENGTH_SHORT).show();
        return;
    }
    
    // Show loading
    Toast.makeText(this, "Đang thêm dịch vụ...", Toast.LENGTH_SHORT).show();
    
    // Get next sequence order
    int nextSequence = treatmentSteps.size() + 1;
    
    // Create request
    AddGeneralServiceRequest request = new AddGeneralServiceRequest(
        service.getId(),
        nextSequence
    );
    
    // Call API
    apiService.addGeneralService(currentTreatmentPlanId, request)
        .enqueue(new Callback<GeneralServiceResponse>() {
            @Override
            public void onResponse(Call<GeneralServiceResponse> call, Response<GeneralServiceResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    GeneralServiceResponse result = response.body();
                    
                    // Add to treatment steps
                    TreatmentPlan.Step step = new TreatmentPlan.Step();
                    step.setId(result.getStepId());
                    step.setServiceName(result.getServiceName());
                    step.setActualPrice(result.getPrice());
                    step.setStatus("PENDING");
                    
                    treatmentSteps.add(step);
                    stepAdapter.notifyDataSetChanged();
                    
                    // Update total cost
                    updateTotalEstimate();
                    
                    Toast.makeText(DoctorWorkflowActivity.this,
                        "Đã thêm: " + result.getServiceName(),
                        Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(DoctorWorkflowActivity.this,
                        "Lỗi thêm dịch vụ: " + response.code(),
                        Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<GeneralServiceResponse> call, Throwable t) {
                Toast.makeText(DoctorWorkflowActivity.this,
                    "Lỗi kết nối: " + t.getMessage(),
                    Toast.LENGTH_SHORT).show();
            }
        });
}

/**
 * Update total cost display
 */
private void updateTotalEstimate() {
    BigDecimal total = BigDecimal.ZERO;
    
    for (TreatmentPlan.Step step : treatmentSteps) {
        if (step.getActualPrice() != null) {
            total = total.add(step.getActualPrice());
        }
    }
    
    // Format price
    NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
    String totalText = nf.format(total.longValue()) + " đ";
    tvTotalEstimate.setText(totalText);
}
```

---

### Step 5: Update Total Cost on Service Removal
**File:** `DoctorWorkflowActivity.java`
**Location:** In `onStepRemove()` or similar method

```java
// When removing a service
private void removeService(TreatmentPlan.Step step) {
    if (step.getId() == null) {
        treatmentSteps.remove(step);
        stepAdapter.notifyDataSetChanged();
        updateTotalEstimate();
        return;
    }
    
    // Call API to remove
    apiService.removeService(currentTreatmentPlanId, step.getId())
        .enqueue(new Callback<java.util.Map<String, Object>>() {
            @Override
            public void onResponse(Call<java.util.Map<String, Object>> call, 
                                 Response<java.util.Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    treatmentSteps.remove(step);
                    stepAdapter.notifyDataSetChanged();
                    updateTotalEstimate();
                    Toast.makeText(DoctorWorkflowActivity.this,
                        "Đã xóa dịch vụ",
                        Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<java.util.Map<String, Object>> call, Throwable t) {
                Toast.makeText(DoctorWorkflowActivity.this,
                    "Lỗi xóa dịch vụ: " + t.getMessage(),
                    Toast.LENGTH_SHORT).show();
            }
        });
}
```

---

### Step 6: Add Imports
**File:** `DoctorWorkflowActivity.java`
**Location:** At the top of the file

```java
import com.hcmute.mobile_android.ui.dialogs.ToothServiceDialog;
import com.hcmute.mobile_android.adapters.GeneralServiceAdapter;
import com.hcmute.mobile_android.network.models.AddGeneralServiceRequest;
import com.hcmute.mobile_android.network.models.GeneralServiceResponse;
import com.hcmute.mobile_android.network.models.ToothServiceResponse;
import java.text.NumberFormat;
import java.util.Locale;
```

---

## 🧪 TESTING CHECKLIST

### Unit Tests
- [ ] Test adding tooth-specific service
- [ ] Test adding general service
- [ ] Test removing service
- [ ] Test total cost calculation
- [ ] Test error handling

### Integration Tests
- [ ] Test API call for tooth service
- [ ] Test API call for general service
- [ ] Test API call for service removal
- [ ] Test UI update after API response
- [ ] Test error messages

### UI Tests
- [ ] Test OdontogramView click
- [ ] Test ToothServiceDialog display
- [ ] Test GeneralServicesList display
- [ ] Test total cost update
- [ ] Test service removal

---

## 📝 NOTES

1. **Service IDs:** Must match backend DataSeed.java
2. **Sequence Order:** Auto-increment based on existing steps
3. **Total Cost:** Always update after each operation
4. **Error Handling:** Show toast messages for errors
5. **Loading State:** Show progress dialog during API calls
6. **Validation:** Check planId is not null before API calls

---

## ✅ COMPLETION CRITERIA

- [ ] OdontogramView listener added
- [ ] ToothServiceDialog shows on tooth click
- [ ] GeneralServicesList displays in layout
- [ ] Services can be added via API
- [ ] Total cost updates correctly
- [ ] Services can be removed
- [ ] Error handling works
- [ ] All tests pass
- [ ] No compilation errors

---

**Status:** READY FOR IMPLEMENTATION
**Estimated Time:** 1 day
**Next:** Start implementation

