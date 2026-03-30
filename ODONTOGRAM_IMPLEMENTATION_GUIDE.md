# 🦷 Hướng Dẫn Triển Khai Chi Tiết - Biểu Đồ Răng & Tính Toán Chi Phí

## 📋 Mục Lục
1. [Backend Implementation](#backend-implementation)
2. [Frontend Implementation](#frontend-implementation)
3. [Testing Guide](#testing-guide)
4. [Troubleshooting](#troubleshooting)

---

## Backend Implementation

### Step 1: Tạo DTOs

#### File: `clinic_backend/src/main/java/com/hcmute/clinic/dto/AddToothServiceRequest.java`
```java
package com.hcmute.clinic.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddToothServiceRequest {
    private Long serviceId;
    private Integer sequenceOrder;
}
```

#### File: `clinic_backend/src/main/java/com/hcmute/clinic/dto/UpdatePriceRequest.java`
```java
package com.hcmute.clinic.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePriceRequest {
    private BigDecimal newPrice;
}
```

#### File: `clinic_backend/src/main/java/com/hcmute/clinic/dto/ToothServiceResponse.java`
```java
package com.hcmute.clinic.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ToothServiceResponse {
    private Long stepId;
    private String toothNumber;
    private String serviceName;
    private BigDecimal price;
    private BigDecimal totalPlanCost;
}
```

### Step 2: Mở Rộng Repository

#### File: `clinic_backend/src/main/java/com/hcmute/clinic/repository/TreatmentPlanStepRepository.java`

Thêm các method mới:
```java
@Repository
public interface TreatmentPlanStepRepository extends JpaRepository<TreatmentPlanStep, Long> {
    // Existing methods
    List<TreatmentPlanStep> findByPlanId(Long planId);
    
    // New methods
    List<TreatmentPlanStep> findByPlanIdAndToothNumber(Long planId, String toothNumber);
    List<TreatmentPlanStep> findByPlanIdOrderBySequenceOrder(Long planId);
    Optional<TreatmentPlanStep> findByPlanIdAndToothNumberAndServiceId(
        Long planId, String toothNumber, Long serviceId
    );
}
```

### Step 3: Tạo Service Layer

#### File: `clinic_backend/src/main/java/com/hcmute/clinic/service/ToothServiceCalculationService.java`

```java
package com.hcmute.clinic.service;

import com.hcmute.clinic.entity.*;
import com.hcmute.clinic.enums.StepStatus;
import com.hcmute.clinic.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ToothServiceCalculationService {
    
    private final TreatmentPlanStepRepository stepRepository;
    private final TreatmentPlanRepository planRepository;
    private final ServiceRepository serviceRepository;
    
    /**
     * Thêm dịch vụ cho một răng
     */
    @Transactional
    public TreatmentPlanStep addServiceToTooth(
        Long planId,
        Long serviceId,
        String toothNumber,
        Integer sequenceOrder
    ) {
        log.info("Adding service {} to tooth {} in plan {}", serviceId, toothNumber, planId);
        
        TreatmentPlan plan = planRepository.findById(planId)
            .orElseThrow(() -> new RuntimeException("Treatment plan not found: " + planId));
        
        Service service = serviceRepository.findById(serviceId)
            .orElseThrow(() -> new RuntimeException("Service not found: " + serviceId));
        
        // Tạo step mới
        TreatmentPlanStep step = TreatmentPlanStep.builder()
            .plan(plan)
            .service(service)
            .toothNumber(toothNumber)
            .actualPrice(service.getPrice())
            .sequenceOrder(sequenceOrder)
            .status(StepStatus.PENDING)
            .build();
        
        TreatmentPlanStep savedStep = stepRepository.save(step);
        log.info("Service added successfully. Step ID: {}", savedStep.getId());
        
        return savedStep;
    }
    
    /**
     * Tính lại tổng chi phí của treatment plan
     */
    @Transactional
    public BigDecimal recalculatePlanTotalCost(Long planId) {
        log.info("Recalculating total cost for plan {}", planId);
        
        List<TreatmentPlanStep> steps = stepRepository.findByPlanId(planId);
        
        BigDecimal totalCost = steps.stream()
            .map(TreatmentPlanStep::getActualPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        log.info("Total cost for plan {}: {}", planId, totalCost);
        return totalCost;
    }
    
    /**
     * Lấy tất cả dịch vụ cho một răng
     */
    public List<TreatmentPlanStep> getServicesForTooth(Long planId, String toothNumber) {
        log.info("Getting services for tooth {} in plan {}", toothNumber, planId);
        return stepRepository.findByPlanIdAndToothNumber(planId, toothNumber);
    }
    
    /**
     * Xóa dịch vụ khỏi một răng
     */
    @Transactional
    public void removeServiceFromTooth(Long stepId) {
        log.info("Removing service from step {}", stepId);
        
        TreatmentPlanStep step = stepRepository.findById(stepId)
            .orElseThrow(() -> new RuntimeException("Step not found: " + stepId));
        
        Long planId = step.getPlan().getId();
        stepRepository.delete(step);
        
        log.info("Service removed. Recalculating plan cost...");
        recalculatePlanTotalCost(planId);
    }
    
    /**
     * Cập nhật giá cho một bước
     */
    @Transactional
    public void updateStepPrice(Long stepId, BigDecimal newPrice) {
        log.info("Updating price for step {} to {}", stepId, newPrice);
        
        TreatmentPlanStep step = stepRepository.findById(stepId)
            .orElseThrow(() -> new RuntimeException("Step not found: " + stepId));
        
        step.setActualPrice(newPrice);
        stepRepository.save(step);
        
        log.info("Price updated. Recalculating plan cost...");
        recalculatePlanTotalCost(step.getPlan().getId());
    }
    
    /**
     * Lấy tất cả steps của một plan theo thứ tự
     */
    public List<TreatmentPlanStep> getPlanStepsOrdered(Long planId) {
        return stepRepository.findByPlanIdOrderBySequenceOrder(planId);
    }
}
```

### Step 4: Tạo Controller

#### File: `clinic_backend/src/main/java/com/hcmute/clinic/controller/ToothServiceController.java`

```java
package com.hcmute.clinic.controller;

import com.hcmute.clinic.dto.AddToothServiceRequest;
import com.hcmute.clinic.dto.ToothServiceResponse;
import com.hcmute.clinic.dto.UpdatePriceRequest;
import com.hcmute.clinic.entity.TreatmentPlanStep;
import com.hcmute.clinic.service.ToothServiceCalculationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/treatment-plans/{planId}/teeth")
@RequiredArgsConstructor
@Slf4j
public class ToothServiceController {
    
    private final ToothServiceCalculationService toothService;
    
    /**
     * Thêm dịch vụ cho một răng
     * POST /api/treatment-plans/1/teeth/8/services
     */
    @PostMapping("/{toothNumber}/services")
    public ResponseEntity<?> addServiceToTooth(
        @PathVariable Long planId,
        @PathVariable String toothNumber,
        @RequestBody AddToothServiceRequest request
    ) {
        log.info("POST /api/treatment-plans/{}/teeth/{}/services", planId, toothNumber);
        
        try {
            TreatmentPlanStep step = toothService.addServiceToTooth(
                planId,
                request.getServiceId(),
                toothNumber,
                request.getSequenceOrder()
            );
            
            BigDecimal totalCost = toothService.recalculatePlanTotalCost(planId);
            
            ToothServiceResponse response = new ToothServiceResponse(
                step.getId(),
                toothNumber,
                step.getService().getName(),
                step.getActualPrice(),
                totalCost
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error adding service to tooth", e);
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    /**
     * Lấy tất cả dịch vụ cho một răng
     * GET /api/treatment-plans/1/teeth/8/services
     */
    @GetMapping("/{toothNumber}/services")
    public ResponseEntity<?> getServicesForTooth(
        @PathVariable Long planId,
        @PathVariable String toothNumber
    ) {
        log.info("GET /api/treatment-plans/{}/teeth/{}/services", planId, toothNumber);
        
        try {
            List<TreatmentPlanStep> steps = toothService.getServicesForTooth(planId, toothNumber);
            return ResponseEntity.ok(steps);
        } catch (Exception e) {
            log.error("Error getting services for tooth", e);
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    /**
     * Xóa dịch vụ khỏi một răng
     * DELETE /api/treatment-plans/1/teeth/steps/123
     */
    @DeleteMapping("/steps/{stepId}")
    public ResponseEntity<?> removeServiceFromTooth(
        @PathVariable Long planId,
        @PathVariable Long stepId
    ) {
        log.info("DELETE /api/treatment-plans/{}/teeth/steps/{}", planId, stepId);
        
        try {
            toothService.removeServiceFromTooth(stepId);
            BigDecimal totalCost = toothService.recalculatePlanTotalCost(planId);
            
            return ResponseEntity.ok(new ToothServiceResponse(
                null, null, null, null, totalCost
            ));
        } catch (Exception e) {
            log.error("Error removing service from tooth", e);
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    /**
     * Cập nhật giá cho một bước
     * PUT /api/treatment-plans/1/teeth/steps/123/price
     */
    @PutMapping("/steps/{stepId}/price")
    public ResponseEntity<?> updateStepPrice(
        @PathVariable Long planId,
        @PathVariable Long stepId,
        @RequestBody UpdatePriceRequest request
    ) {
        log.info("PUT /api/treatment-plans/{}/teeth/steps/{}/price", planId, stepId);
        
        try {
            toothService.updateStepPrice(stepId, request.getNewPrice());
            BigDecimal totalCost = toothService.recalculatePlanTotalCost(planId);
            
            return ResponseEntity.ok(new ToothServiceResponse(
                stepId, null, null, request.getNewPrice(), totalCost
            ));
        } catch (Exception e) {
            log.error("Error updating step price", e);
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
```

---

## Frontend Implementation

### Step 1: Tạo Android Models

#### File: `mobile_android/app/src/main/java/com/hcmute/mobile_android/network/models/ToothServiceResponse.java`

```java
package com.hcmute.mobile_android.network.models;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;

public class ToothServiceResponse {
    @SerializedName("stepId")
    private Long stepId;
    
    @SerializedName("toothNumber")
    private String toothNumber;
    
    @SerializedName("serviceName")
    private String serviceName;
    
    @SerializedName("price")
    private BigDecimal price;
    
    @SerializedName("totalPlanCost")
    private BigDecimal totalPlanCost;
    
    public Long getStepId() { return stepId; }
    public String getToothNumber() { return toothNumber; }
    public String getServiceName() { return serviceName; }
    public BigDecimal getPrice() { return price; }
    public BigDecimal getTotalPlanCost() { return totalPlanCost; }
}
```

#### File: `mobile_android/app/src/main/java/com/hcmute/mobile_android/network/models/AddToothServiceRequest.java`

```java
package com.hcmute.mobile_android.network.models;

import com.google.gson.annotations.SerializedName;

public class AddToothServiceRequest {
    @SerializedName("serviceId")
    private Long serviceId;
    
    @SerializedName("sequenceOrder")
    private Integer sequenceOrder;
    
    public AddToothServiceRequest(Long serviceId, Integer sequenceOrder) {
        this.serviceId = serviceId;
        this.sequenceOrder = sequenceOrder;
    }
    
    public Long getServiceId() { return serviceId; }
    public Integer getSequenceOrder() { return sequenceOrder; }
}
```

### Step 2: Cập Nhật ApiService

#### File: `mobile_android/app/src/main/java/com/hcmute/mobile_android/network/ApiService.java`

Thêm các method:
```java
// Thêm dịch vụ cho một răng
@POST("/api/treatment-plans/{planId}/teeth/{toothNumber}/services")
Call<ToothServiceResponse> addServiceToTooth(
    @Path("planId") Long planId,
    @Path("toothNumber") String toothNumber,
    @Body AddToothServiceRequest request
);

// Lấy tất cả dịch vụ cho một răng
@GET("/api/treatment-plans/{planId}/teeth/{toothNumber}/services")
Call<List<TreatmentPlanStep>> getServicesForTooth(
    @Path("planId") Long planId,
    @Path("toothNumber") String toothNumber
);

// Xóa dịch vụ khỏi một răng
@DELETE("/api/treatment-plans/{planId}/teeth/steps/{stepId}")
Call<Void> removeServiceFromTooth(
    @Path("planId") Long planId,
    @Path("stepId") Long stepId
);
```

### Step 3: Tạo Dialog

#### File: `mobile_android/app/src/main/java/com/hcmute/mobile_android/ui/dialogs/ToothServiceDialog.java`

```java
package com.hcmute.mobile_android.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import com.hcmute.mobile_android.adapters.ServiceSelectionAdapter;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.models.AddToothServiceRequest;
import com.hcmute.mobile_android.network.models.ServiceItem;
import com.hcmute.mobile_android.network.models.ToothServiceResponse;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ToothServiceDialog extends DialogFragment {
    
    private Long planId;
    private String toothNumber;
    private List<ServiceItem> services;
    private OnServiceSelectedListener listener;
    private int selectedIndex = -1;
    
    public interface OnServiceSelectedListener {
        void onServiceSelected(Long serviceId, String serviceName, double price);
        void onTotalCostUpdated(double totalCost);
    }
    
    public void setPlanId(Long planId) {
        this.planId = planId;
    }
    
    public void setToothNumber(String toothNumber) {
        this.toothNumber = toothNumber;
    }
    
    public void setOnServiceSelectedListener(OnServiceSelectedListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        
        fetchServices();
        
        builder.setTitle("Chọn dịch vụ cho răng " + toothNumber)
            .setSingleChoiceItems(
                new String[]{},
                -1,
                (dialog, which) -> selectedIndex = which
            )
            .setPositiveButton("Chọn", (dialog, which) -> {
                if (selectedIndex >= 0 && selectedIndex < services.size()) {
                    ServiceItem selected = services.get(selectedIndex);
                    addServiceToTooth(selected.getId());
                }
            })
            .setNegativeButton("Hủy", null);
        
        return builder.create();
    }
    
    private void fetchServices() {
        ApiService apiService = RetrofitClient.getApiService();
        apiService.getActiveServices().enqueue(new Callback<List<ServiceItem>>() {
            @Override
            public void onResponse(Call<List<ServiceItem>> call, Response<List<ServiceItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    services = response.body();
                    updateDialog();
                }
            }
            
            @Override
            public void onFailure(Call<List<ServiceItem>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi tải dịch vụ: " + t.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void updateDialog() {
        AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog != null && services != null) {
            String[] items = new String[services.size()];
            for (int i = 0; i < services.size(); i++) {
                ServiceItem service = services.get(i);
                items[i] = String.format("%s - %,.0f đ", 
                    service.getName(), service.getPrice());
            }
            dialog.getListView().setAdapter(
                new android.widget.ArrayAdapter<>(
                    getContext(),
                    android.R.layout.simple_list_item_single_choice,
                    items
                )
            );
        }
    }
    
    private void addServiceToTooth(Long serviceId) {
        ApiService apiService = RetrofitClient.getApiService();
        AddToothServiceRequest request = new AddToothServiceRequest(serviceId, 1);
        
        apiService.addServiceToTooth(planId, toothNumber, request)
            .enqueue(new Callback<ToothServiceResponse>() {
                @Override
                public void onResponse(Call<ToothServiceResponse> call, 
                    Response<ToothServiceResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        ToothServiceResponse result = response.body();
                        Toast.makeText(getContext(), "Đã thêm dịch vụ", 
                            Toast.LENGTH_SHORT).show();
                        
                        if (listener != null) {
                            listener.onServiceSelected(
                                result.getStepId(),
                                result.getServiceName(),
                                result.getPrice().doubleValue()
                            );
                            listener.onTotalCostUpdated(
                                result.getTotalPlanCost().doubleValue()
                            );
                        }
                        dismiss();
                    }
                }
                
                @Override
                public void onFailure(Call<ToothServiceResponse> call, Throwable t) {
                    Toast.makeText(getContext(), "Lỗi: " + t.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                }
            });
    }
}
```

### Step 4: Cập Nhật DoctorWorkflowActivity

Thêm vào `DoctorWorkflowActivity.java`:

```java
private void setupOdontogramListener() {
    odontogramView.setOnToothSelectedListener(toothNumber -> {
        showToothServiceDialog(String.valueOf(toothNumber));
    });
}

private void showToothServiceDialog(String toothNumber) {
    ToothServiceDialog dialog = new ToothServiceDialog();
    dialog.setPlanId(treatmentPlanId);
    dialog.setToothNumber(toothNumber);
    dialog.setOnServiceSelectedListener(new ToothServiceDialog.OnServiceSelectedListener() {
        @Override
        public void onServiceSelected(Long serviceId, String serviceName, double price) {
            odontogramView.setToothService(Integer.parseInt(toothNumber), serviceName);
        }
        
        @Override
        public void onTotalCostUpdated(double totalCost) {
            updateTotalCostDisplay(totalCost);
        }
    });
    dialog.show(getSupportFragmentManager(), "ToothServiceDialog");
}

private void updateTotalCostDisplay(double totalCost) {
    tvTotalCost.setText(String.format("Tổng chi phí: %,.0f đ", totalCost));
}
```

---

## Testing Guide

### Backend Testing

#### Test 1: Thêm dịch vụ cho một răng
```bash
curl -X POST http://localhost:8080/api/treatment-plans/1/teeth/8/services \
  -H "Content-Type: application/json" \
  -d '{
    "serviceId": 5,
    "sequenceOrder": 1
  }'
```

**Expected Response:**
```json
{
  "stepId": 123,
  "toothNumber": "8",
  "serviceName": "Trám răng",
  "price": 500000,
  "totalPlanCost": 500000
}
```

#### Test 2: Lấy dịch vụ cho một răng
```bash
curl -X GET http://localhost:8080/api/treatment-plans/1/teeth/8/services
```

#### Test 3: Xóa dịch vụ
```bash
curl -X DELETE http://localhost:8080/api/treatment-plans/1/teeth/steps/123
```

#### Test 4: Cập nhật giá
```bash
curl -X PUT http://localhost:8080/api/treatment-plans/1/teeth/steps/123/price \
  -H "Content-Type: application/json" \
  -d '{
    "newPrice": 600000
  }'
```

### Frontend Testing

#### Test 1: Nhấp vào răng
- Mở DoctorWorkflowActivity
- Nhấp vào một răng trên biểu đồ
- Kiểm tra dialog hiển thị

#### Test 2: Chọn dịch vụ
- Chọn dịch vụ từ dialog
- Kiểm tra API call thành công
- Kiểm tra UI cập nhật

#### Test 3: Tính toán chi phí
- Thêm 3 dịch vụ
- Kiểm tra tổng chi phí tính đúng

---

## Troubleshooting

### Backend Issues

**Issue 1: Service not found**
- Kiểm tra serviceId có tồn tại
- Kiểm tra service có active = true

**Issue 2: Treatment plan not found**
- Kiểm tra planId có tồn tại
- Kiểm tra plan có status = IN_PROGRESS

### Frontend Issues

**Issue 1: Dialog không hiển thị**
- Kiểm tra FragmentManager
- Kiểm tra dialog.show() được gọi

**Issue 2: API call thất bại**
- Kiểm tra URL endpoint
- Kiểm tra request body format
- Kiểm tra network connection

**Issue 3: UI không cập nhật**
- Kiểm tra listener được set
- Kiểm tra invalidate() được gọi
