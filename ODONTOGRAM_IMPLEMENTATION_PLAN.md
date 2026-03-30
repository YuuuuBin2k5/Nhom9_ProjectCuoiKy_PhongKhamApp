# 📋 Kế Hoạch Triển Khai Chi Tiết - Biểu Đồ Răng & Tính Toán Chi Phí

**Ngày lập kế hoạch:** 30/03/2026
**Phiên bản:** 1.0
**Trạng thái:** READY FOR EXECUTION

---

## 🎯 Mục Tiêu

Triển khai hệ thống biểu đồ răng với tính toán chi phí tự động, tách biệt dịch vụ cụ thể (4 dịch vụ) và dịch vụ tổng quát (6 dịch vụ).

---

## 📊 Phân Chia Công Việc

### Phase 1: Backend (5 ngày làm việc)

#### Task 1.1: Mở Rộng Entity (1 ngày)
- **Người phụ trách:** Backend Lead
- **Công việc:**
  - [ ] Mở rộng `TreatmentPlanStep` thêm field `isGeneralService`
  - [ ] Tạo migration script
  - [ ] Viết unit tests
- **Deliverable:** Entity mở rộng + migration + tests
- **Acceptance Criteria:**
  - Field `isGeneralService` được thêm vào
  - Migration chạy thành công
  - Tests pass 100%

#### Task 1.2: Tạo DTOs (1 ngày)
- **Người phụ trách:** Backend Dev
- **Công việc:**
  - [ ] Tạo `AddToothServiceRequest.java`
  - [ ] Tạo `AddGeneralServiceRequest.java`
  - [ ] Tạo `ToothServiceResponse.java`
  - [ ] Tạo `GeneralServiceResponse.java`
- **Deliverable:** 4 DTO files
- **Acceptance Criteria:**
  - Tất cả DTOs có @Data, @NoArgsConstructor, @AllArgsConstructor
  - Có @SerializedName cho JSON mapping

#### Task 1.3: Tạo Service Layer (2 ngày)
- **Người phụ trách:** Backend Lead
- **Công việc:**
  - [ ] Tạo `ToothServiceCalculationService`
  - [ ] Implement `addServiceToTooth()`
  - [ ] Implement `addGeneralService()`
  - [ ] Implement `recalculatePlanTotalCost()`
  - [ ] Implement `removeService()`
  - [ ] Viết unit tests (80% coverage)
- **Deliverable:** Service class + tests
- **Acceptance Criteria:**
  - Tất cả methods hoạt động đúng
  - Tests pass 100%
  - Code coverage >= 80%

#### Task 1.4: Tạo Controller (1 ngày)
- **Người phụ trách:** Backend Dev
- **Công việc:**
  - [ ] Tạo `ToothServiceController`
  - [ ] Implement 4 endpoints
  - [ ] Thêm error handling
  - [ ] Thêm logging
- **Deliverable:** Controller class
- **Acceptance Criteria:**
  - 4 endpoints hoạt động
  - Error handling đầy đủ
  - Logging chi tiết

#### Task 1.5: Mở Rộng Repository (1 ngày)
- **Người phụ trách:** Backend Dev
- **Công việc:**
  - [ ] Mở rộng `TreatmentPlanStepRepository`
  - [ ] Thêm query methods
  - [ ] Viết tests
- **Deliverable:** Repository mở rộng + tests
- **Acceptance Criteria:**
  - Tất cả query methods hoạt động
  - Tests pass 100%

---

### Phase 2: Frontend (5 ngày làm việc)

#### Task 2.1: Tạo Android Models (1 ngày)
- **Người phụ trách:** Frontend Dev
- **Công việc:**
  - [ ] Tạo `ToothServiceResponse.java`
  - [ ] Tạo `GeneralServiceResponse.java`
  - [ ] Tạo `AddToothServiceRequest.java`
  - [ ] Tạo `AddGeneralServiceRequest.java`
- **Deliverable:** 4 model files
- **Acceptance Criteria:**
  - Tất cả models có @SerializedName
  - Getters/setters đầy đủ

#### Task 2.2: Cập Nhật ApiService (1 ngày)
- **Người phụ trách:** Frontend Dev
- **Công việc:**
  - [ ] Thêm 4 API methods
  - [ ] Thêm error handling
  - [ ] Viết tests
- **Deliverable:** ApiService mở rộng + tests
- **Acceptance Criteria:**
  - 4 methods hoạt động
  - Tests pass 100%

#### Task 2.3: Tạo ToothServiceDialog (1.5 ngày)
- **Người phụ trách:** Frontend Lead
- **Công việc:**
  - [ ] Tạo dialog class
  - [ ] Tạo layout XML
  - [ ] Implement logic chọn dịch vụ
  - [ ] Thêm error handling
- **Deliverable:** Dialog + layout + tests
- **Acceptance Criteria:**
  - Dialog hiển thị đúng
  - Chọn dịch vụ hoạt động
  - Error handling đầy đủ

#### Task 2.4: Tạo GeneralServicesList (1.5 ngày)
- **Người phụ trách:** Frontend Dev
- **Công việc:**
  - [ ] Tạo adapter
  - [ ] Tạo layout items
  - [ ] Implement logic thêm dịch vụ
  - [ ] Thêm error handling
- **Deliverable:** Adapter + layouts + tests
- **Acceptance Criteria:**
  - List hiển thị đúng
  - Thêm dịch vụ hoạt động
  - Error handling đầy đủ

#### Task 2.5: Tích Hợp vào DoctorWorkflowActivity (1 ngày)
- **Người phụ trách:** Frontend Lead
- **Công việc:**
  - [ ] Tích hợp ToothServiceDialog
  - [ ] Tích hợp GeneralServicesList
  - [ ] Cập nhật OdontogramView
  - [ ] Thêm listeners
  - [ ] Viết tests
- **Deliverable:** Activity mở rộng + tests
- **Acceptance Criteria:**
  - Dialog hiển thị khi nhấp vào răng
  - List hiển thị dịch vụ tổng quát
  - Tính toán chi phí đúng

---

### Phase 3: Testing & QA (5 ngày làm việc)

#### Task 3.1: Unit Tests (2 ngày)
- **Người phụ trách:** QA Lead
- **Công việc:**
  - [ ] Test backend services (80% coverage)
  - [ ] Test frontend models
  - [ ] Test API integration
- **Deliverable:** Test reports
- **Acceptance Criteria:**
  - Coverage >= 80%
  - Tests pass 100%

#### Task 3.2: Integration Tests (2 ngày)
- **Người phụ trách:** QA Dev
- **Công việc:**
  - [ ] Test thêm dịch vụ cụ thể
  - [ ] Test thêm dịch vụ tổng quát
  - [ ] Test xóa dịch vụ
  - [ ] Test tính toán chi phí
  - [ ] Test UI/UX
- **Deliverable:** Test cases + results
- **Acceptance Criteria:**
  - Tất cả test cases pass
  - Không có bugs

#### Task 3.3: UAT (1 ngày)
- **Người phụ trách:** QA Lead + Bác sĩ
- **Công việc:**
  - [ ] Test với bác sĩ thực tế
  - [ ] Collect feedback
  - [ ] Fix issues
- **Deliverable:** UAT report
- **Acceptance Criteria:**
  - Bác sĩ chấp nhận
  - Không có critical bugs

---

## 📅 Timeline

```
Week 1 (30/03 - 03/04):
  Mon 30/03: Task 1.1 (Entity)
  Tue 31/03: Task 1.2 (DTOs)
  Wed 01/04: Task 1.3 (Service) - Day 1
  Thu 02/04: Task 1.3 (Service) - Day 2
  Fri 03/04: Task 1.4 + 1.5 (Controller + Repository)

Week 2 (06/04 - 10/04):
  Mon 06/04: Task 2.1 (Models)
  Tue 07/04: Task 2.2 (ApiService)
  Wed 08/04: Task 2.3 (Dialog) - Day 1
  Thu 09/04: Task 2.3 (Dialog) - Day 2
  Fri 10/04: Task 2.4 (List) - Day 1

Week 3 (13/04 - 17/04):
  Mon 13/04: Task 2.4 (List) - Day 2
  Tue 14/04: Task 2.5 (Integration)
  Wed 15/04: Task 3.1 (Unit Tests) - Day 1
  Thu 16/04: Task 3.1 (Unit Tests) - Day 2
  Fri 17/04: Task 3.2 (Integration Tests) - Day 1

Week 4 (20/04 - 24/04):
  Mon 20/04: Task 3.2 (Integration Tests) - Day 2
  Tue 21/04: Task 3.3 (UAT)
  Wed 22/04: Fix issues
  Thu 23/04: Final testing
  Fri 24/04: Deployment ready
```

**Total: 3-4 tuần**

---

## 👥 Phân Công Nhân Sự

### Backend Team (2 người)
- **Backend Lead:** Thiết kế, review code, Task 1.1, 1.3
- **Backend Dev:** Task 1.2, 1.4, 1.5

### Frontend Team (2 người)
- **Frontend Lead:** Thiết kế UI, review code, Task 2.3, 2.5
- **Frontend Dev:** Task 2.1, 2.2, 2.4

### QA Team (2 người)
- **QA Lead:** Test planning, UAT, Task 3.1, 3.3
- **QA Dev:** Task 3.2

---

## 🔍 Quality Assurance

### Code Review Checklist
- [ ] Code follows naming conventions
- [ ] Code is well-documented
- [ ] No hardcoded values
- [ ] Error handling is complete
- [ ] Logging is adequate
- [ ] Tests are comprehensive
- [ ] Performance is acceptable

### Testing Checklist
- [ ] Unit tests pass (80% coverage)
- [ ] Integration tests pass
- [ ] UI/UX tests pass
- [ ] Performance tests pass
- [ ] Security tests pass
- [ ] UAT pass

---

## 📊 Risk Management

### Risk 1: API Integration Issues
- **Probability:** Medium
- **Impact:** High
- **Mitigation:** Early integration testing, mock APIs

### Risk 2: UI/UX Issues
- **Probability:** Medium
- **Impact:** Medium
- **Mitigation:** Early UI review, user feedback

### Risk 3: Performance Issues
- **Probability:** Low
- **Impact:** High
- **Mitigation:** Performance testing, optimization

### Risk 4: Data Consistency Issues
- **Probability:** Low
- **Impact:** High
- **Mitigation:** Transaction management, data validation

---

## 📝 Deliverables

### Phase 1 Deliverables
1. Entity mở rộng + migration
2. 4 DTO files
3. Service class + tests
4. Controller class
5. Repository mở rộng

### Phase 2 Deliverables
1. 4 Android model files
2. ApiService mở rộng
3. ToothServiceDialog + layout
4. GeneralServicesList + adapter
5. DoctorWorkflowActivity mở rộng

### Phase 3 Deliverables
1. Unit test reports
2. Integration test reports
3. UAT report
4. Final documentation

---

## ✅ Success Criteria

- [ ] Tất cả tasks hoàn thành đúng hạn
- [ ] Code quality >= 80%
- [ ] Test coverage >= 80%
- [ ] Zero critical bugs
- [ ] Bác sĩ chấp nhận
- [ ] Performance acceptable
- [ ] Documentation complete

---

## 📞 Communication Plan

### Daily Standup
- **Thời gian:** 09:00 AM
- **Thời lượng:** 15 phút
- **Nội dung:** Progress, blockers, next steps

### Weekly Review
- **Thời gian:** Friday 5:00 PM
- **Thời lượng:** 30 phút
- **Nội dung:** Week summary, next week plan

### Escalation
- **Critical issues:** Immediate notification
- **Blockers:** Same day resolution
- **Questions:** Within 24 hours

---

## 🚀 Deployment Plan

### Pre-Deployment
- [ ] Final code review
- [ ] Final testing
- [ ] Documentation review
- [ ] Backup database

### Deployment
- [ ] Deploy backend
- [ ] Deploy frontend
- [ ] Smoke testing
- [ ] Monitor logs

### Post-Deployment
- [ ] Monitor performance
- [ ] Collect user feedback
- [ ] Fix issues
- [ ] Document lessons learned

---

## 📚 Documentation

### Code Documentation
- [ ] Javadoc for all public methods
- [ ] Inline comments for complex logic
- [ ] README for setup

### User Documentation
- [ ] User guide for doctors
- [ ] Admin guide
- [ ] Troubleshooting guide

### Technical Documentation
- [ ] Architecture diagram
- [ ] API documentation
- [ ] Database schema
- [ ] Deployment guide

---

## 🎯 Next Steps

1. **Phê duyệt kế hoạch** (Today)
2. **Phân công nhân sự** (Today)
3. **Bắt đầu Task 1.1** (Tomorrow)
4. **Daily standup** (Every day)
5. **Weekly review** (Every Friday)

---

**Kế hoạch này sẽ được cập nhật hàng tuần dựa trên tiến độ thực tế.**
