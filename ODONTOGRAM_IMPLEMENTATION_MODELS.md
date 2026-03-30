# 🦷 Các Model & DTO Cần Tạo

## 1. Request DTOs

### AddToothServiceRequest.java
```java
package com.hcmute.clinic.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddToothServiceRequest {
    private Long serviceId;           // ID của dịch vụ
    private Integer sequenceOrder;    // Thứ tự bước (1, 2, 3...)
}
```

### UpdatePriceRequest.java
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
    private BigDecimal newPrice;  // Giá mới
}
```

---

## 2. Response DTOs

### ToothServiceResponse.java
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
    private Long stepId;              // ID của treatment step
    private String toothNumber;       // Số răng (FDI: "8", "16", etc.)
    private String serviceName;       // Tên dịch vụ
    private BigDecimal price;         // Giá dịch vụ
    private BigDecimal totalPlanCost; // Tổng chi phí của plan
}
```

### ToothStatusResponse.java
```java
package com.hcmute.clinic.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ToothStatusResponse {
    private String toothNumber;
    private List<ToothServiceDetail> services;
    private BigDecimal totalCostForTooth;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ToothServiceDetail {
        private Long stepId;
        private String serviceName;
        private BigDecimal price;
        private String status;  // PENDING, IN_PROGRESS, COMPLETED
    }
}
```

### TreatmentPlanSummaryResponse.java
```java
package com.hcmute.clinic.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TreatmentPlanSummaryResponse {
    private Long planId;
    private String patientName;
    private List<ToothServiceSummary> toothServices;
    private BigDecimal totalAmount;
    private Integer totalSteps;
    private Integer completedSteps;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ToothServiceSummary {
        private String toothNumber;
        private String serviceName;
        private BigDecimal price;
        private String status;
    }
}
```

---

## 3. Android Models

### ToothServiceItem.java (Mobile)
```java
package com.hcmute.mobile_android.network.models;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;

public class ToothServiceItem {
    @SerializedName("stepId")
    private Long stepId;
    
    @SerializedName("toothNumber")
    private String toothNumber;
    
    @SerializedName("serviceName")
    private String serviceName;
    
    @SerializedName("price")
    private BigDecimal price;
    
    @SerializedName("status")
    private String status;  // PENDING, IN_PROGRESS, COMPLETED
    
    // Getters
    public Long getStepId() { return stepId; }
    public String getToothNumber() { return toothNumber; }
    public String getServiceName() { return serviceName; }
    public BigDecimal getPrice() { return price; }
    public String getStatus() { return status; }
}
```

### ToothServiceResponse.java (Mobile)
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
    
    // Getters
    public Long getStepId() { return stepId; }
    public String getToothNumber() { return toothNumber; }
    public String getServiceName() { return serviceName; }
    public BigDecimal getPrice() { return price; }
    public BigDecimal getTotalPlanCost() { return totalPlanCost; }
}
```

---

## 4. API Service Methods (Android)

### Thêm vào ApiService.java

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
Call<List<ToothServiceItem>> getServicesForTooth(
    @Path("planId") Long planId,
    @Path("toothNumber") String toothNumber
);

// Xóa dịch vụ khỏi một răng
@DELETE("/api/treatment-plans/{planId}/teeth/steps/{stepId}")
Call<Void> removeServiceFromTooth(
    @Path("planId") Long planId,
    @Path("stepId") Long stepId
);

// Cập nhật giá cho một bước
@PUT("/api/treatment-plans/{planId}/teeth/steps/{stepId}/price")
Call<Void> updateStepPrice(
    @Path("planId") Long planId,
    @Path("stepId") Long stepId,
    @Body UpdatePriceRequest request
);

// Lấy tóm tắt treatment plan
@GET("/api/treatment-plans/{planId}/summary")
Call<TreatmentPlanSummaryResponse> getTreatmentPlanSummary(
    @Path("planId") Long planId
);
```

---

## 5. Request DTOs (Android)

### AddToothServiceRequest.java (Mobile)
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

### UpdatePriceRequest.java (Mobile)
```java
package com.hcmute.mobile_android.network.models;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;

public class UpdatePriceRequest {
    @SerializedName("newPrice")
    private BigDecimal newPrice;
    
    public UpdatePriceRequest(BigDecimal newPrice) {
        this.newPrice = newPrice;
    }
    
    public BigDecimal getNewPrice() { return newPrice; }
}
```

---

## 6. Adapter cho Dialog (Android)

### ServiceSelectionAdapter.java
```java
package com.hcmute.mobile_android.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.hcmute.mobile_android.network.models.ServiceItem;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ServiceSelectionAdapter extends ArrayAdapter<ServiceItem> {
    
    private Context context;
    private List<ServiceItem> services;
    private NumberFormat currencyFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
    
    public ServiceSelectionAdapter(Context context, List<ServiceItem> services) {
        super(context, 0, services);
        this.context = context;
        this.services = services;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        }
        
        ServiceItem service = services.get(position);
        
        TextView text1 = convertView.findViewById(android.R.id.text1);
        TextView text2 = convertView.findViewById(android.R.id.text2);
        
        text1.setText(service.getName());
        text2.setText(String.format("%s đ", currencyFormat.format(service.getPrice())));
        
        return convertView;
    }
}
```

---

## 7. Layout XML cho Dialog (Android)

### dialog_tooth_service.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:padding="16dp">
    
    <TextView
        android:id="@+id/tvToothNumber"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Chọn dịch vụ cho răng"
        android:textSize="18sp"
        android:textStyle="bold"
        android:layout_marginBottom="16dp" />
    
    <ListView
        android:id="@+id/lvServices"
        android:layout_width="match_parent"
        android:layout_height="300dp"
        android:layout_marginBottom="16dp" />
    
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:gravity="end">
        
        <Button
            android:id="@+id/btnCancel"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Hủy"
            android:layout_marginEnd="8dp" />
        
        <Button
            android:id="@+id/btnConfirm"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Chọn" />
    </LinearLayout>
</LinearLayout>
```

---

## 8. Cập Nhật OdontogramView

### Thêm vào OdontogramView.java

```java
// Thêm method để hiển thị dịch vụ cho từng răng
private Map<Integer, String> toothServices = new HashMap<>();

public void setToothService(int toothNumber, String serviceName) {
    toothServices.put(toothNumber, serviceName);
    invalidate();
}

public String getToothService(int toothNumber) {
    return toothServices.get(toothNumber);
}

// Cập nhật onDraw để hiển thị tên dịch vụ
@Override
protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    
    // ... existing code ...
    
    // Draw all teeth
    for (Map.Entry<Integer, RectF> entry : toothBounds.entrySet()) {
        int toothNumber = entry.getKey();
        RectF bounds = entry.getValue();
        
        // ... existing tooth drawing code ...
        
        // Draw service name if available
        String serviceName = toothServices.get(toothNumber);
        if (serviceName != null && !serviceName.isEmpty()) {
            textPaint.setTextSize(12f);
            textPaint.setColor(Color.GRAY);
            float serviceY = bounds.bottom + 15f;
            canvas.drawText(serviceName, bounds.centerX(), serviceY, textPaint);
            textPaint.setTextSize(24f); // Reset
        }
    }
}
```

---

## 9. Tóm Tắt Các File Cần Tạo

### Backend (Spring Boot)
```
clinic_backend/src/main/java/com/hcmute/clinic/
├── dto/
│   ├── AddToothServiceRequest.java
│   ├── UpdatePriceRequest.java
│   ├── ToothServiceResponse.java
│   ├── ToothStatusResponse.java
│   └── TreatmentPlanSummaryResponse.java
├── service/
│   └── ToothServiceCalculationService.java
└── controller/
    └── ToothServiceController.java
```

### Frontend (Android)
```
mobile_android/app/src/main/java/com/hcmute/mobile_android/
├── network/models/
│   ├── ToothServiceItem.java
│   ├── ToothServiceResponse.java
│   ├── AddToothServiceRequest.java
│   └── UpdatePriceRequest.java
├── adapters/
│   └── ServiceSelectionAdapter.java
├── ui/dialogs/
│   └── ToothServiceDialog.java
└── res/layout/
    └── dialog_tooth_service.xml
```

---

## 10. Cập Nhật ApiService.java

Thêm các method mới vào `ApiService.java` (đã liệt kê ở phần 4)

---

## 11. Thứ Tự Triển Khai

1. **Backend DTOs** (Request/Response)
2. **Backend Service** (ToothServiceCalculationService)
3. **Backend Controller** (ToothServiceController)
4. **Android Models** (ToothServiceItem, ToothServiceResponse)
5. **Android API Methods** (ApiService)
6. **Android Dialog** (ToothServiceDialog)
7. **Android Adapter** (ServiceSelectionAdapter)
8. **Android Layout** (dialog_tooth_service.xml)
9. **Cập nhật OdontogramView**
10. **Cập nhật DoctorWorkflowActivity**
11. **Testing & QA**
