# Phân Tích Nghiệp Vụ: Room Assignment cho Odontogram Services

## 🎯 Vấn Đề Hiện Tại

**Phát hiện:** Logic đưa bệnh nhân sang phòng (room assignment) KHÔNG được áp dụng cho các dịch vụ được thêm khi nhấp trên sơ đồ răng (odontogram).

## 📊 Phân Tích Technical Leader

### 1. Root Cause Analysis

#### A. Hai Luồng Tạo Treatment Plan Steps

```
Luồng 1: Từ Template (CÓ room assignment)
┌─────────────────────────────────────┐
│ TreatmentPlanService                │
│ .createFromTemplate()               │
│                                     │
│ for (TreatmentPlanTemplateStep ts) │
│   step.clinicRoom = ts.clinicRoom  │ ✅ CÓ
│   step.service = ts.service        │
│   step.sequenceOrder = ...         │
└─────────────────────────────────────┘

Luồng 2: Từ Odontogram (KHÔNG CÓ room assignment)
┌─────────────────────────────────────┐
│ ToothServiceCalculationService      │
│ .addServiceToTooth()                │
│ .addGeneralService()                │
│                                     │
│ step.service = service              │
│ step.toothNumber = toothNumber      │
│ step.clinicRoom = ???               │ ❌ THIẾU
│ step.sequenceOrder = ...            │
└─────────────────────────────────────┘
```

#### B. Code Evidence

**File:** `ToothServiceCalculationService.java`

```java
// Line 47-70: addServiceToTooth()
TreatmentPlanStep step = TreatmentPlanStep.builder()
    .plan(plan)
    .service(service)
    .toothNumber(toothNumber)
    .actualPrice(service.getPrice())
    .sequenceOrder(sequenceOrder)
    .status(StepStatus.PENDING)
    .isGeneralService(false)
    // ❌ THIẾU: .clinicRoom(???)
    .build();
```

```java
// Line 83-110: addGeneralService()
TreatmentPlanStep step = TreatmentPlanStep.builder()
    .plan(plan)
    .service(service)
    .toothNumber(null)
    .actualPrice(service.getPrice())
    .sequenceOrder(sequenceOrder)
    .status(StepStatus.PENDING)
    .isGeneralService(true)
    // ❌ THIẾU: .clinicRoom(???)
    .build();
```

### 2. Business Impact Analysis

#### A. Immediate Impact (Hiện Tại)

```
Khi bác sĩ thêm dịch vụ từ odontogram:
┌────────────────────────────────────────┐
│ Step được tạo với:                     │
│ • service_id: ✅ Có                    │
│ • tooth_number: ✅ Có                  │
│ • clinic_room_id: ❌ NULL              │
└────────────────────────────────────────┘
                ↓
┌────────────────────────────────────────┐
│ Khi hoàn thành step:                   │
│ completeStepAndAdvance() được gọi      │
│                                        │
│ if (nextStep.clinicRoom != null) {    │
│   // Đưa sang phòng                   │
│ } else {                               │
│   // ❌ KHÔNG đưa sang phòng          │
│ }                                      │
└────────────────────────────────────────┘
```

**Kết quả:**
- Bệnh nhân KHÔNG được tự động đưa sang phòng X-Quang
- Bệnh nhân KHÔNG được tự động đưa sang phòng Phẫu thuật
- Workflow bị gián đoạn
- Cần can thiệp thủ công

#### B. Workflow Comparison

**Template-based (Hoạt động tốt):**
```
Khám tổng quát → X-Quang → Nhổ răng khôn
   (Phòng 1)    (Phòng X)   (Phòng PT)
      ✅            ✅           ✅
   Auto move    Auto move   Auto move
```

**Odontogram-based (Bị lỗi):**
```
Khám răng 18 → Chụp X-Quang → Nhổ răng 18
  (Phòng 1)     (Phòng X)      (Phòng PT)
     ✅             ❌             ❌
  Manual       Stuck here    Never reach
```

### 3. Nghiệp Vụ Đúng Đắn (Business Logic)

#### A. Nguyên Tắc Cơ Bản

```
1. MỖI dịch vụ PHẢI có phòng thực hiện
   - Khám tổng quát → Phòng khám
   - X-Quang → Phòng X-Quang
   - Nhổ răng → Phòng phẫu thuật
   - Trám răng → Phòng khám
   - Niềng răng → Phòng chỉnh nha

2. Workflow tự động PHẢI nhất quán
   - Không phân biệt nguồn gốc (template vs odontogram)
   - Cùng dịch vụ → Cùng phòng
   - Cùng logic chuyển phòng

3. Trải nghiệm người dùng PHẢI mượt mà
   - Bác sĩ thêm dịch vụ → Hệ thống tự động assign phòng
   - Hoàn thành step → Tự động chuyển phòng tiếp theo
   - Không cần can thiệp thủ công
```

#### B. Service-to-Room Mapping

```
┌─────────────────────────────────────────────────────┐
│ Service Type          → Clinic Room                 │
├─────────────────────────────────────────────────────┤
│ Khám tổng quát        → Phòng khám (GENERAL)        │
│ X-Quang               → Phòng X-Quang (XRAY)        │
│ Nhổ răng              → Phòng phẫu thuật (SURGERY)  │
│ Trám răng             → Phòng khám (GENERAL)        │
│ Niềng răng            → Phòng chỉnh nha (ORTHO)     │
│ Tẩy trắng             → Phòng thẩm mỹ (COSMETIC)    │
│ Cạo vôi               → Phòng khám (GENERAL)        │
└─────────────────────────────────────────────────────┘
```

### 4. Architectural Issues

#### A. Inconsistent Abstraction

```
❌ BAD: Hai cách tạo step khác nhau

createFromTemplate()          addServiceToTooth()
        ↓                             ↓
   Has room logic            No room logic
        ↓                             ↓
   Works correctly           Broken workflow
```

```
✅ GOOD: Một abstraction chung

createFromTemplate()          addServiceToTooth()
        ↓                             ↓
        └─────────┬───────────────────┘
                  ↓
         assignRoomForService()
                  ↓
            Works correctly
```

#### B. Missing Domain Logic

```java
// ❌ Current: Business logic scattered
ToothServiceCalculationService {
    // Tạo step KHÔNG có room
}

TreatmentPlanService {
    // Tạo step CÓ room (từ template)
}

// ✅ Should be: Centralized domain logic
ServiceRoomAssignmentService {
    ClinicRoom determineRoomForService(Service service) {
        // Single source of truth
        // Based on service category
    }
}
```

### 5. Recommended Solution

#### Option 1: Quick Fix (Tactical)

**Ưu điểm:**
- Nhanh, dễ implement
- Ít risk
- Giải quyết immediate problem

**Nhược điểm:**
- Không giải quyết root cause
- Technical debt
- Duplicate logic

```java
// In ToothServiceCalculationService
public TreatmentPlanStep addServiceToTooth(...) {
    Service service = serviceRepository.findById(serviceId)...;
    
    // QUICK FIX: Determine room based on service
    ClinicRoom room = determineRoomForService(service);
    
    TreatmentPlanStep step = TreatmentPlanStep.builder()
        .service(service)
        .clinicRoom(room)  // ✅ Added
        .build();
}

private ClinicRoom determineRoomForService(Service service) {
    // Hardcoded logic (not ideal but works)
    String category = service.getCategory().getName();
    return switch (category) {
        case "X-Quang" -> findRoomByType("XRAY");
        case "Phẫu thuật" -> findRoomByType("SURGERY");
        default -> findRoomByType("GENERAL");
    };
}
```

#### Option 2: Proper Refactoring (Strategic)

**Ưu điểm:**
- Giải quyết root cause
- Clean architecture
- Maintainable
- Extensible

**Nhược điểm:**
- Mất thời gian hơn
- Cần test kỹ
- Có thể break existing code

```java
// New service
@Service
public class ServiceRoomAssignmentService {
    
    private final ClinicRoomRepository roomRepository;
    
    /**
     * Determine appropriate room for a service
     * Single source of truth for room assignment logic
     */
    public ClinicRoom determineRoomForService(Service service) {
        String categoryName = service.getCategory().getName();
        
        // Business rules centralized here
        String roomType = switch (categoryName) {
            case "X-Quang", "Chụp phim" -> "XRAY";
            case "Phẫu thuật", "Nhổ răng" -> "SURGERY";
            case "Chỉnh nha", "Niềng răng" -> "ORTHODONTICS";
            case "Thẩm mỹ", "Tẩy trắng" -> "COSMETIC";
            default -> "GENERAL";
        };
        
        return roomRepository.findFirstByRoomType(roomType)
            .orElseThrow(() -> new RuntimeException(
                "No room available for type: " + roomType
            ));
    }
}

// Update ToothServiceCalculationService
@Service
public class ToothServiceCalculationService {
    
    private final ServiceRoomAssignmentService roomAssignmentService;
    
    public TreatmentPlanStep addServiceToTooth(...) {
        Service service = serviceRepository.findById(serviceId)...;
        
        // ✅ Use centralized logic
        ClinicRoom room = roomAssignmentService
            .determineRoomForService(service);
        
        TreatmentPlanStep step = TreatmentPlanStep.builder()
            .service(service)
            .clinicRoom(room)
            .build();
    }
}

// Update TreatmentPlanService
@Service
public class TreatmentPlanService {
    
    private final ServiceRoomAssignmentService roomAssignmentService;
    
    public TreatmentPlan createFromTemplate(...) {
        for (TreatmentPlanTemplateStep ts : sorted) {
            // ✅ Use same logic, fallback to template room
            ClinicRoom room = ts.getClinicRoom() != null 
                ? ts.getClinicRoom()
                : roomAssignmentService.determineRoomForService(
                    ts.getService()
                );
            
            TreatmentPlanStep step = TreatmentPlanStep.builder()
                .clinicRoom(room)
                .build();
        }
    }
}
```

### 6. Testing Strategy

```java
@Test
void testOdontogramServiceHasRoom() {
    // Given: Service "Nhổ răng khôn"
    Service service = createService("Nhổ răng khôn", "Phẫu thuật");
    
    // When: Add to treatment plan via odontogram
    TreatmentPlanStep step = toothService.addServiceToTooth(
        planId, service.getId(), "18", 1
    );
    
    // Then: Should have surgery room assigned
    assertNotNull(step.getClinicRoom());
    assertEquals("SURGERY", step.getClinicRoom().getRoomType());
}

@Test
void testTemplateAndOdontogramConsistency() {
    // Given: Same service
    Service xrayService = createService("X-Quang", "X-Quang");
    
    // When: Create via template
    TreatmentPlan plan1 = createFromTemplate(templateId);
    TreatmentPlanStep step1 = plan1.getSteps().get(0);
    
    // When: Create via odontogram
    TreatmentPlanStep step2 = toothService.addServiceToTooth(
        planId, xrayService.getId(), "18", 1
    );
    
    // Then: Should have same room
    assertEquals(
        step1.getClinicRoom().getRoomType(),
        step2.getClinicRoom().getRoomType()
    );
}
```

### 7. Migration Plan

```
Phase 1: Quick Fix (1-2 days)
├─ Add room assignment to ToothServiceCalculationService
├─ Test with existing data
└─ Deploy to production

Phase 2: Refactoring (1 week)
├─ Create ServiceRoomAssignmentService
├─ Migrate ToothServiceCalculationService
├─ Migrate TreatmentPlanService
├─ Add comprehensive tests
└─ Deploy to production

Phase 3: Enhancement (Future)
├─ Add room availability check
├─ Add room scheduling
└─ Add room conflict resolution
```

## 🎯 Kết Luận Technical Leader

### Critical Issues

1. **Inconsistent Business Logic** ⚠️ HIGH
   - Hai luồng tạo step khác nhau
   - Một có room, một không
   - Gây confusion và bugs

2. **Broken Workflow** ⚠️ HIGH
   - Bệnh nhân không được chuyển phòng
   - Cần can thiệp thủ công
   - Bad UX

3. **Missing Abstraction** ⚠️ MEDIUM
   - Không có centralized room assignment logic
   - Duplicate code
   - Hard to maintain

### Recommendations

**Immediate (This Sprint):**
- Implement Quick Fix (Option 1)
- Add room assignment to odontogram services
- Test thoroughly

**Short-term (Next Sprint):**
- Refactor to ServiceRoomAssignmentService (Option 2)
- Centralize room assignment logic
- Add comprehensive tests

**Long-term (Future):**
- Add room availability checking
- Add room scheduling
- Add conflict resolution

### Success Metrics

```
✅ All services have room assigned (100%)
✅ Workflow auto-advance works for odontogram services
✅ No manual intervention needed
✅ Consistent behavior across all service creation methods
```

---

**Phân tích bởi:** Technical Leader  
**Ngày:** 31/03/2026  
**Priority:** HIGH  
**Estimated Effort:** 2-3 days (Quick Fix) + 1 week (Proper Refactoring)
