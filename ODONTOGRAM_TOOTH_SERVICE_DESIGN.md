# 🦷 Thiết Kế Hệ Thống Biểu Đồ Răng - Tính Toán Chi Phí Tự Động

## 📋 Phân Tích Hiện Trạng Hệ Thống

### 1. Cấu Trúc Dữ Liệu Hiện Tại

#### Backend Entities:
```
Service (Dịch vụ)
├── id: Long
├── name: String (ví dụ: "Trám răng", "Nhổ răng", "Tẩy trắng")
├── price: BigDecimal ✅ (Đã có giá)
├── category: ServiceCategory
├── uiTemplateType: UiTemplateType (GENERAL, SURGERY, XRAY, ORTHODONTICS)
└── durationMinutes: Integer

TreatmentPlanStep (Bước điều trị)
├── id: Long
├── service: Service ✅ (Liên kết dịch vụ)
├── toothNumber: String ✅ (FDI notation: "38", "16", etc.)
├── actualPrice: BigDecimal (Giá thực tế)
├── status: StepStatus (PENDING, IN_PROGRESS, COMPLETED)
└── sequenceOrder: Integer

InvoiceItem (Chi tiết hóa đơn)
├── id: Long
├── service: Service
├── toothNumber: String ✅ (Đã hỗ trợ)
├── unitPrice: BigDecimal
├── quantity: Integer
├── totalPrice: BigDecimal (unitPrice × quantity)
└── treatmentPlanStep: TreatmentPlanStep

Invoice (Hóa đơn)
├── id: Long
├── treatmentPlan: TreatmentPlan
├── items: List<InvoiceItem>
├── totalAmount: BigDecimal (SUM của tất cả items)
└── paymentStatus: InvoiceStatus
```

### 2. Dịch Vụ Hiện Tại Trong Hệ Thống

**Các loại dịch vụ theo UI Template:**
- **GENERAL**: Khám bệnh, tư vấn, vệ sinh
- **SURGERY**: Nhổ răng, phẫu thuật
- **XRAY**: Chụp X-quang
- **ORTHODONTICS**: Niềng răng

**Ví dụ dịch vụ thực tế:**
- Trám răng (GENERAL) - 500k
- Nhổ răng (SURGERY) - 300k
- Tẩy trắng (GENERAL) - 200k
- Chụp X-quang (XRAY) - 100k
- Niềng răng (ORTHODONTICS) - 5M

---

## 🎯 Phương Pháp Giải Pháp

### Luồng Xử Lý Khi Bác Sĩ Nhấp Vào Răng

```
┌─────────────────────────────────────────────────────────────┐
│ 1. Bác sĩ nhấp vào răng số 8 trên biểu đồ                  │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│ 2. OdontogramView phát hiện click                           │
│    - Gọi OnToothSelectedListener.onToothSelected(8)         │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│ 3. Activity hiển thị ToothServiceDialog                     │
│    - Danh sách dịch vụ có sẵn (từ API)                      │
│    - Bác sĩ chọn "Trám răng" (500k)                         │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│ 4. API Call: POST /api/treatment-plans/{planId}/steps       │
│    {                                                         │
│      "serviceId": 5,                                         │
│      "toothNumber": "8",                                     │
│      "sequenceOrder": 1                                      │
│    }                                                         │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│ 5. Backend xử lý:                                           │
│    - Tạo TreatmentPlanStep mới                              │
│    - Lấy giá từ Service (500k)                              │
│    - Lưu actualPrice = 500k                                 │
│    - Tính lại totalAmount của TreatmentPlan                 │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│ 6. Response trả về:                                         │
│    {                                                         │
│      "stepId": 123,                                          │
│      "toothNumber": "8",                                     │
│      "serviceName": "Trám răng",                             │
│      "price": 500000,                                        │
│      "totalPlanCost": 500000                                 │
│    }                                                         │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│ 7. Frontend cập nhật:                                       │
│    - Đổi màu răng 8 thành xanh (đã chọn dịch vụ)           │
│    - Hiển thị "Trám răng - 500k" dưới răng                  │
│    - Cập nhật tổng chi phí: 500k                            │
└─────────────────────────────────────────────────────────────┘
```

---

## 🏗️ Kiến Trúc Chi Tiết

### Backend Implementation

#### 1. Entity Mở Rộng (Không cần thay đổi, đã có sẵn)

**TreatmentPlanStep** - Đã có:
- `toothNumber`: String (FDI notation)
- `service`: Service (liên kết dịch vụ)
- `actualPrice`: BigDecimal (giá thực tế)

#### 2. Service Layer - Tính Toán Chi Phí

```java
@Service
@RequiredArgsConstructor
public class ToothServiceCalculationService {
    
    private final TreatmentPlanStepRepository stepRepository;
    private final TreatmentPlanRepository planRepository;
    private final ServiceRepository serviceRepository;
    
    /**
     * Thêm dịch vụ cho một răng
     */
    public TreatmentPlanStep addServiceToTooth(
        Long planId, 
        Long serviceId, 
        String toothNumber,
        Integer sequenceOrder
    ) {
        TreatmentPlan plan = planRepository.findById(planId)
            .orElseThrow(() -> new RuntimeException("Plan not found"));
        
        Service service = serviceRepository.findById(serviceId)
            .orElseThrow(() -> new RuntimeException("Service not found"));
        
        // Tạo step mới
        TreatmentPlanStep step = TreatmentPlanStep.builder()
            .plan(plan)
            .service(service)
            .toothNumber(toothNumber)
            .actualPrice(service.getPrice())  // Lấy giá từ service
            .sequenceOrder(sequenceOrder)
            .status(StepStatus.PENDING)
            .build();
        
        stepRepository.save(step);
        
        // Tính lại tổng chi phí của plan
        recalculatePlanTotalCost(planId);
        
        return step;
    }
    
    /**
     * Tính lại tổng chi phí của treatment plan
     */
    public BigDecimal recalculatePlanTotalCost(Long planId) {
        List<TreatmentPlanStep> steps = stepRepository.findByPlanId(planId);
        
        BigDecimal totalCost = steps.stream()
            .map(TreatmentPlanStep::getActualPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Cập nhật plan (nếu có field totalCost)
        TreatmentPlan plan = planRepository.findById(planId).orElseThrow();
        // plan.setTotalCost(totalCost);  // Nếu cần
        planRepository.save(plan);
        
        return totalCost;
    }
    
    /**
     * Lấy tất cả dịch vụ cho một răng
     */
    public List<TreatmentPlanStep> getServicesForTooth(Long planId, String toothNumber) {
        return stepRepository.findByPlanIdAndToothNumber(planId, toothNumber);
    }
    
    /**
     * Xóa dịch vụ khỏi một răng
     */
    public void removeServiceFromTooth(Long stepId) {
        TreatmentPlanStep step = stepRepository.findById(stepId)
            .orElseThrow(() -> new RuntimeException("Step not found"));
        
        Long planId = step.getPlan().getId();
        stepRepository.delete(step);
        
        // Tính lại tổng chi phí
        recalculatePlanTotalCost(planId);
    }
    
    /**
     * Cập nhật giá cho một bước (nếu bác sĩ muốn thay đổi)
     */
    public void updateStepPrice(Long stepId, BigDecimal newPrice) {
        TreatmentPlanStep step = stepRepository.findById(stepId)
            .orElseThrow(() -> new RuntimeException("Step not found"));
        
        step.setActualPrice(newPrice);
        stepRepository.save(step);
        
        // Tính lại tổng chi phí
        recalculatePlanTotalCost(step.getPlan().getId());
    }
}
```

#### 3. Controller - API Endpoints

```java
@RestController
@RequestMapping("/api/treatment-plans/{planId}/teeth")
@RequiredArgsConstructor
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
        TreatmentPlanStep step = toothService.addServiceToTooth(
            planId,
            request.getServiceId(),
            toothNumber,
            request.getSequenceOrder()
        );
        
        return ResponseEntity.ok(new ToothServiceResponse(
            step.getId(),
            toothNumber,
            step.getService().getName(),
            step.getActualPrice(),
            toothService.recalculatePlanTotalCost(planId)
        ));
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
        List<TreatmentPlanStep> steps = toothService.getServicesForTooth(planId, toothNumber);
        return ResponseEntity.ok(steps);
    }
    
    /**
     * Xóa dịch vụ khỏi một răng
     * DELETE /api/treatment-plans/1/teeth/steps/123
     */
    @DeleteMapping("/steps/{stepId}")
    public ResponseEntity<?> removeServiceFromTooth(@PathVariable Long stepId) {
        toothService.removeServiceFromTooth(stepId);
        return ResponseEntity.ok("Service removed");
    }
    
    /**
     * Cập nhật giá cho một bước
     * PUT /api/treatment-plans/1/teeth/steps/123/price
     */
    @PutMapping("/steps/{stepId}/price")
    public ResponseEntity<?> updateStepPrice(
        @PathVariable Long stepId,
        @RequestBody UpdatePriceRequest request
    ) {
        toothService.updateStepPrice(stepId, request.getNewPrice());
        return ResponseEntity.ok("Price updated");
    }
}
```

#### 4. Repository

```java
@Repository
public interface TreatmentPlanStepRepository extends JpaRepository<TreatmentPlanStep, Long> {
    List<TreatmentPlanStep> findByPlanId(Long planId);
    List<TreatmentPlanStep> findByPlanIdAndToothNumber(Long planId, String toothNumber);
    List<TreatmentPlanStep> findByPlanIdOrderBySequenceOrder(Long planId);
}
```

---

### Frontend Implementation (Android)

#### 1. Dialog - Chọn Dịch Vụ Cho Răng

```java
public class ToothServiceDialog extends DialogFragment {
    
    private Long planId;
    private String toothNumber;
    private List<ServiceItem> services;
    private OnServiceSelectedListener listener;
    
    public interface OnServiceSelectedListener {
        void onServiceSelected(Long serviceId, String serviceName, double price);
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        
        // Fetch services từ API
        fetchServices();
        
        // Tạo adapter cho danh sách dịch vụ
        ServiceSelectionAdapter adapter = new ServiceSelectionAdapter(services);
        
        builder.setTitle("Chọn dịch vụ cho răng " + toothNumber)
            .setAdapter(adapter, (dialog, which) -> {
                ServiceItem selected = services.get(which);
                if (listener != null) {
                    listener.onServiceSelected(
                        selected.getId(),
                        selected.getName(),
                        selected.getPrice()
                    );
                }
                addServiceToTooth(selected.getId());
            })
            .setNegativeButton("Hủy", null);
        
        return builder.create();
    }
    
    private void fetchServices() {
        ApiService apiService = RetrofitClient.getApiService();
        apiService.getActiveServices().enqueue(new Callback<List<ServiceItem>>() {
            @Override
            public void onResponse(Call<List<ServiceItem>> call, Response<List<ServiceItem>> response) {
                if (response.isSuccessful()) {
                    services = response.body();
                }
            }
            
            @Override
            public void onFailure(Call<List<ServiceItem>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi tải dịch vụ", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void addServiceToTooth(Long serviceId) {
        ApiService apiService = RetrofitClient.getApiService();
        AddToothServiceRequest request = new AddToothServiceRequest(serviceId, 1);
        
        apiService.addServiceToTooth(planId, toothNumber, request)
            .enqueue(new Callback<ToothServiceResponse>() {
                @Override
                public void onResponse(Call<ToothServiceResponse> call, Response<ToothServiceResponse> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "Đã thêm dịch vụ", Toast.LENGTH_SHORT).show();
                        dismiss();
                    }
                }
                
                @Override
                public void onFailure(Call<ToothServiceResponse> call, Throwable t) {
                    Toast.makeText(getContext(), "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }
}
```

#### 2. Activity - Tích Hợp OdontogramView

```java
public class DoctorWorkflowActivity extends AppCompatActivity {
    
    private OdontogramView odontogramView;
    private TextView tvTotalCost;
    private Long treatmentPlanId;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_workflow);
        
        odontogramView = findViewById(R.id.odontogramView);
        tvTotalCost = findViewById(R.id.tvTotalCost);
        
        treatmentPlanId = getIntent().getLongExtra("planId", -1);
        
        // Thiết lập listener cho OdontogramView
        odontogramView.setOnToothSelectedListener(toothNumber -> {
            showToothServiceDialog(String.valueOf(toothNumber));
        });
        
        // Load dữ liệu hiện tại
        loadTreatmentPlanData();
    }
    
    private void showToothServiceDialog(String toothNumber) {
        ToothServiceDialog dialog = new ToothServiceDialog();
        dialog.setPlanId(treatmentPlanId);
        dialog.setToothNumber(toothNumber);
        dialog.setOnServiceSelectedListener((serviceId, serviceName, price) -> {
            // Cập nhật UI
            odontogramView.setToothStatus(Integer.parseInt(toothNumber), "selected");
            updateTotalCost();
        });
        dialog.show(getSupportFragmentManager(), "ToothServiceDialog");
    }
    
    private void loadTreatmentPlanData() {
        ApiService apiService = RetrofitClient.getApiService();
        apiService.getTreatmentPlan(treatmentPlanId)
            .enqueue(new Callback<TreatmentPlan>() {
                @Override
                public void onResponse(Call<TreatmentPlan> call, Response<TreatmentPlan> response) {
                    if (response.isSuccessful()) {
                        TreatmentPlan plan = response.body();
                        
                        // Cập nhật biểu đồ răng
                        for (TreatmentPlanStep step : plan.getSteps()) {
                            if (step.getToothNumber() != null) {
                                int toothNum = Integer.parseInt(step.getToothNumber());
                                odontogramView.setToothStatus(toothNum, "selected");
                            }
                        }
                        
                        updateTotalCost();
                    }
                }
                
                @Override
                public void onFailure(Call<TreatmentPlan> call, Throwable t) {
                    Toast.makeText(DoctorWorkflowActivity.this, 
                        "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                }
            });
    }
    
    private void updateTotalCost() {
        ApiService apiService = RetrofitClient.getApiService();
        apiService.getTreatmentPlan(treatmentPlanId)
            .enqueue(new Callback<TreatmentPlan>() {
                @Override
                public void onResponse(Call<TreatmentPlan> call, Response<TreatmentPlan> response) {
                    if (response.isSuccessful()) {
                        TreatmentPlan plan = response.body();
                        BigDecimal total = plan.getSteps().stream()
                            .map(TreatmentPlanStep::getActualPrice)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                        
                        tvTotalCost.setText(String.format("Tổng chi phí: %,.0f đ", total));
                    }
                }
                
                @Override
                public void onFailure(Call<TreatmentPlan> call, Throwable t) {}
            });
    }
}
```

---

## 📊 Ví Dụ Thực Tế

### Scenario: Bác sĩ kiểm tra bệnh nhân

**Bước 1:** Bác sĩ tạo treatment plan mới
- Plan ID: 1
- Patient: Nguyễn Văn A
- Status: IN_PROGRESS

**Bước 2:** Bác sĩ nhấp vào răng 8 (cửa trên phải)
- Dialog hiển thị dịch vụ:
  - Trám răng (500k)
  - Tẩy trắng (200k)
  - Chụp X-quang (100k)

**Bước 3:** Bác sĩ chọn "Trám răng"
- API: `POST /api/treatment-plans/1/teeth/8/services`
- Request: `{ "serviceId": 5, "sequenceOrder": 1 }`
- Response: `{ "stepId": 123, "toothNumber": "8", "serviceName": "Trám răng", "price": 500000, "totalPlanCost": 500000 }`

**Bước 4:** Bác sĩ nhấp vào răng 16 (hàm trên trái)
- Chọn "Nhổ răng" (300k)
- API: `POST /api/treatment-plans/1/teeth/16/services`
- Response: `{ "stepId": 124, "toothNumber": "16", "serviceName": "Nhổ răng", "price": 300000, "totalPlanCost": 800000 }`

**Bước 5:** Bác sĩ nhấp vào răng 36 (hàm dưới phải)
- Chọn "Trám răng" (500k)
- Response: `{ "stepId": 125, "toothNumber": "36", "serviceName": "Trám răng", "price": 500000, "totalPlanCost": 1300000 }`

**Kết quả cuối cùng:**
```
Biểu đồ răng:
- Răng 8:  Trám răng (500k) - Màu xanh
- Răng 16: Nhổ răng (300k) - Màu xanh
- Răng 36: Trám răng (500k) - Màu xanh

Tổng chi phí: 1,300,000 đ
```

---

## ✅ Checklist Triển Khai

### Backend
- [ ] Tạo `ToothServiceCalculationService`
- [ ] Tạo `ToothServiceController` với endpoints
- [ ] Mở rộng `TreatmentPlanStepRepository`
- [ ] Tạo Request/Response DTOs
- [ ] Viết unit tests

### Frontend
- [ ] Tạo `ToothServiceDialog`
- [ ] Tích hợp dialog vào `DoctorWorkflowActivity`
- [ ] Cập nhật `OdontogramView` để hiển thị dịch vụ
- [ ] Thêm API calls vào `ApiService`
- [ ] Cập nhật UI để hiển thị tổng chi phí

### Database
- [ ] Kiểm tra migration (không cần, đã có sẵn)
- [ ] Seed dữ liệu dịch vụ mẫu

---

## 🎨 Màu Sắc Biểu Đồ Răng

```
Trạng thái          Màu sắc         Ý nghĩa
─────────────────────────────────────────────
Healthy (mặc định)  Trắng (#FFFFFF) Không có vấn đề
Selected            Xanh (#1A56DB)  Đã chọn dịch vụ
Caries              Đỏ (#D32F2F)    Sâu răng
Filled              Xanh đậm (#1565C0) Đã trám
RCT                 Cam (#E65100)   Điều trị tủy
```

---

## 🔗 Liên Kết Dữ Liệu

```
Service (Dịch vụ)
    ↓
TreatmentPlanStep (Bước điều trị)
    ├── toothNumber: "8"
    ├── service: Service
    └── actualPrice: 500000
    ↓
InvoiceItem (Chi tiết hóa đơn)
    ├── toothNumber: "8"
    ├── serviceName: "Trám răng"
    ├── unitPrice: 500000
    └── totalPrice: 500000
    ↓
Invoice (Hóa đơn)
    └── totalAmount: SUM(InvoiceItem.totalPrice)
```

---

## 📝 Ghi Chú Quan Trọng

1. **FDI Tooth Numbering**: Hệ thống sử dụng FDI (1-32)
   - 11-18: Hàm trên phải
   - 21-28: Hàm trên trái
   - 31-38: Hàm dưới trái
   - 41-48: Hàm dưới phải

2. **Giá Dịch Vụ**: Lấy từ `Service.price`, có thể override bằng `actualPrice`

3. **Tính Toán Tự Động**: Mỗi khi thêm/xóa dịch vụ, tự động tính lại tổng

4. **Hóa Đơn**: Tạo từ `TreatmentPlan` → `InvoiceItem` → `Invoice`

5. **Audit Trail**: Tất cả thay đổi được ghi lại qua `AuditLog`
