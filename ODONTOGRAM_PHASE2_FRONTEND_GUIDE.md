# 📱 ODONTOGRAM PHASE 2 - FRONTEND IMPLEMENTATION GUIDE

**Date:** 30/03/2026
**Status:** READY TO START
**Completion:** 0%

---

## ✅ Completed (Task 2.1)

### Android Models Created
1. ✅ `ToothServiceResponse.java` - Response when tooth service added
2. ✅ `GeneralServiceResponse.java` - Response when general service added
3. ✅ `AddToothServiceRequest.java` - Request to add tooth service
4. ✅ `AddGeneralServiceRequest.java` - Request to add general service

### ApiService Updated (Task 2.2)
✅ Added 7 new API methods:
- `addServiceToTooth()` - POST /api/treatment-plans/{planId}/services/teeth/{toothNumber}
- `addGeneralService()` - POST /api/treatment-plans/{planId}/services/general
- `getServicesForTooth()` - GET /api/treatment-plans/{planId}/services/teeth/{toothNumber}
- `getGeneralServices()` - GET /api/treatment-plans/{planId}/services/general
- `removeService()` - DELETE /api/treatment-plans/{planId}/services/steps/{stepId}
- `updateStepPrice()` - PUT /api/treatment-plans/{planId}/services/steps/{stepId}/price
- `getAllSteps()` - GET /api/treatment-plans/{planId}/services/all

---

## 📋 Remaining Tasks

### Task 2.3: Create ToothServiceDialog (1.5 days)
**Objective:** Dialog for selecting tooth-specific services

**Files to Create:**
1. `ToothServiceDialog.java` - Dialog class
2. `dialog_tooth_service.xml` - Layout

**Features:**
- Show 4 tooth-specific services (Trám, Nhổ thường, Nhổ khôn, Bọc sứ)
- Display service name and price
- Call API to add service
- Show loading state
- Handle errors
- Update total cost

**Integration:**
- Called from `OdontogramView.onToothSelected()`
- Pass planId and toothNumber
- Callback to update UI after service added

---

### Task 2.4: Create GeneralServicesList (1.5 days)
**Objective:** List for adding general services

**Files to Create:**
1. `GeneralServiceAdapter.java` - RecyclerView adapter
2. `item_general_service.xml` - Item layout
3. `GeneralServicesFragment.java` - Fragment (optional)

**Features:**
- Show 6 general services
- Display service name and price
- Add button for each service
- Call API to add service
- Show loading state
- Handle errors
- Update total cost

**Integration:**
- Add to `DoctorWorkflowActivity` layout
- Show below odontogram
- Callback to update UI after service added

---

### Task 2.5: Integrate into DoctorWorkflowActivity (1 day)
**Objective:** Connect all components

**Changes:**
1. Add OdontogramView listener
2. Show ToothServiceDialog on tooth click
3. Add GeneralServicesList to layout
4. Update total cost display
5. Handle service removal
6. Handle price updates

---

## 🎯 Service Classification

### Tooth-Specific Services (4)
1. Trám răng sâu (300k) - serviceId: 4
2. Nhổ răng thường (300k) - serviceId: 6
3. Nhổ răng khôn (2M) - serviceId: 7
4. Bọc răng sứ (5M) - serviceId: 9

### General Services (6)
1. Khám và tư vấn (100k) - serviceId: 1
2. Chụp X-quang (200k) - serviceId: 2
3. Lấy cao & đánh bóng (250k) - serviceId: 3
4. Điều trị tủy (1.5M) - serviceId: 5
5. Tẩy trắng (2.5M) - serviceId: 8
6. Niềng răng (30M) - serviceId: 10

---

## 🔄 Data Flow

```
1. Doctor clicks on tooth in OdontogramView
   ↓
2. onToothSelected(toothNumber) called
   ↓
3. Show ToothServiceDialog
   ↓
4. Doctor selects service
   ↓
5. Call API: addServiceToTooth(planId, toothNumber, serviceId)
   ↓
6. Backend returns ToothServiceResponse with updated totalCost
   ↓
7. Update UI: Show service in list, update total cost
   ↓
8. Doctor can add more services or click another tooth
```

---

## 📝 Implementation Notes

1. **Service IDs:** Must match backend DataSeed.java
2. **Sequence Order:** Auto-increment based on existing steps
3. **Total Cost:** Always update after each operation
4. **Error Handling:** Show toast messages for errors
5. **Loading State:** Show progress dialog during API calls
6. **Validation:** Check planId is not null before API calls

---

**Next:** Start Task 2.3 - Create ToothServiceDialog

