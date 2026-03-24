# Mobile App Development Roadmap - PhongKham

## 🎯 CHIẾN LƯỢC: Mobile First, Web Later

**Quyết định**: Hoàn thiện toàn bộ Mobile App trước, sau đó mới phát triển Web App

**Lý do**:
- ✅ Focus vào một platform, tránh phân tán
- ✅ Mobile app có nhiều features phức tạp hơn (QR, camera, notifications)
- ✅ Test và fix bugs dễ hơn khi tập trung
- ✅ Có sản phẩm hoàn chỉnh để demo sớm
- ✅ Backend API sẽ được test kỹ qua Mobile trước khi làm Web

---

## 📱 MOBILE APP - VAI TRÒ TRONG APP

Mobile App sẽ hỗ trợ TẤT CẢ vai trò:
- **Patient**: Đầy đủ features
- **Doctor**: Đầy đủ features (Web chỉ bổ sung sau)
- **Admin**: Đầy đủ features (Web chỉ bổ sung sau)
- **Lễ Tân (Reception)**: Đầy đủ features trong Mobile ⭐

---

## 📊 PHÂN TÍCH HIỆN TRẠNG MOBILE APP

### ✅ ĐÃ CÓ (Implemented):

#### Patient Features:
- ✅ Login/Register với email + password
- ✅ OTP login với phone
- ✅ QR Check-in (đã refactor: quét QR từ lễ tân)
- ✅ Dashboard với upcoming appointments
- ✅ Treatment plans list
- ✅ Notifications list
- ✅ Queue status tracking

#### Staff Features (Doctor/Admin):
- ✅ Staff login (email + password)
- ✅ QR Scanner để check-in patient
- ✅ Queue Management (view, call, transfer)
- ✅ Doctor Workflow với Odontogram
- ✅ Treatment Plan management
- ✅ Patient lookup by QR
- ✅ Admin: Doctor management (CRUD)
- ✅ Admin: Room management (CRUD)
- ✅ Admin: Service management (CRUD)

### ❌ CHƯA CÓ (Missing):

#### Patient Features:
- ❌ Book appointment (đặt lịch hẹn)
- ❌ View medical records (xem hồ sơ bệnh án)
- ❌ View prescriptions (xem đơn thuốc)
- ❌ Payment history (lịch sử thanh toán)
- ❌ Service reviews (đánh giá dịch vụ)
- ❌ Profile edit (chỉnh sửa thông tin cá nhân)

#### Doctor Features:
- ❌ Create medical record (tạo hồ sơ bệnh án)
- ❌ Write prescription (kê đơn thuốc)
- ❌ View patient history (xem lịch sử bệnh nhân)
- ❌ Schedule management (quản lý lịch làm việc)
- ❌ Statistics (thống kê bệnh nhân của mình)

#### Admin Features:
- ❌ Service category management
- ❌ Treatment template management (CRUD)
- ❌ Analytics dashboard
- ❌ User management (Patient, Doctor, Admin)
- ❌ System settings

#### Reception Features (NEW ROLE):
- ❌ Generate QR for check-in ⭐
- ❌ Patient search & registration
- ❌ Appointment management (create, edit, cancel)
- ❌ Calendar view
- ❌ Payment & invoicing
- ❌ Print receipts

---

## 🗺️ DEVELOPMENT ROADMAP

### PHASE 1: Complete Patient Features (2-3 tuần)

#### 1.1. Book Appointment (1 tuần)
**Backend:**
- [ ] API: `POST /api/appointments` (đã có)
- [ ] API: `GET /api/services` (đã có)
- [ ] API: `GET /api/doctors` (đã có)
- [ ] API: `GET /api/doctors?serviceId={id}` (đã có)

**Mobile:**
- [ ] BookAppointmentActivity
  - [ ] Select service
  - [ ] Select doctor (filtered by service)
  - [ ] Select date & time
  - [ ] Add notes
  - [ ] Confirm booking
- [ ] Update PatientDashboardFragment để hiển thị appointments

#### 1.2. Medical Records & Prescriptions (1 tuần)
**Backend:**
- [ ] API: `GET /api/patients/me/medical-records` (đã có)
- [ ] API: `GET /api/patients/me/medical-records/{id}` (đã có)
- [ ] API: `GET /api/patients/me/prescriptions/{id}` (đã có)

**Mobile:**
- [ ] MedicalRecordActivity (list)
- [ ] MedicalRecordDetailActivity
  - [ ] View diagnosis, symptoms
  - [ ] View prescription
  - [ ] View images
- [ ] PrescriptionDetailActivity
  - [ ] List medications
  - [ ] Dosage, frequency

#### 1.3. Payment & Reviews (3-4 ngày)
**Backend:**
- [ ] API: `GET /api/patients/me/invoices`
- [ ] API: `GET /api/patients/me/payments`
- [ ] API: `POST /api/reviews`

**Mobile:**
- [ ] PaymentHistoryActivity
- [ ] InvoiceDetailActivity
- [ ] ServiceReviewActivity

#### 1.4. Profile Management (2-3 ngày)
**Backend:**
- [ ] API: `PUT /api/patients/me`
- [ ] API: `PUT /api/patients/me/profile`

**Mobile:**
- [ ] ProfileEditActivity
  - [ ] Edit personal info
  - [ ] Edit health profile (allergies, conditions)
  - [ ] Change password
  - [ ] Upload avatar

---

### PHASE 2: Complete Doctor Features (2-3 tuần)

#### 2.1. Medical Record Creation (1 tuần)
**Backend:**
- [ ] API: `POST /api/medical-records`
- [ ] API: `PUT /api/medical-records/{id}`
- [ ] API: `POST /api/medical-records/{id}/images`

**Mobile:**
- [ ] CreateMedicalRecordActivity
  - [ ] Patient info display
  - [ ] Diagnosis input
  - [ ] Symptoms input
  - [ ] Vital signs (blood pressure, temperature, etc.)
  - [ ] Notes
  - [ ] Take/upload photos
  - [ ] Link to treatment plan

#### 2.2. Prescription Management (1 tuần)
**Backend:**
- [ ] API: `POST /api/prescriptions`
- [ ] API: `PUT /api/prescriptions/{id}`
- [ ] API: `GET /api/medications` (drug database)

**Mobile:**
- [ ] CreatePrescriptionActivity
  - [ ] Search medications
  - [ ] Add medication with dosage
  - [ ] Set frequency & duration
  - [ ] Add instructions
  - [ ] Preview & save

#### 2.3. Patient History & Schedule (3-4 ngày)
**Backend:**
- [ ] API: `GET /api/doctor/patients/{id}/history`
- [ ] API: `GET /api/doctor/schedule`
- [ ] API: `GET /api/doctor/statistics`

**Mobile:**
- [ ] PatientHistoryActivity
  - [ ] Previous visits
  - [ ] Medical records timeline
  - [ ] Treatment plans
- [ ] DoctorScheduleActivity
  - [ ] Calendar view
  - [ ] Appointments list
- [ ] DoctorStatisticsActivity
  - [ ] Patients count
  - [ ] Appointments stats
  - [ ] Revenue (if applicable)

---

### PHASE 3: Complete Admin Features (1-2 tuần)

#### 3.1. Service Category Management (2-3 ngày)
**Backend:**
- [ ] API: `GET /api/admin/service-categories`
- [ ] API: `POST /api/admin/service-categories`
- [ ] API: `PUT /api/admin/service-categories/{id}`
- [ ] API: `DELETE /api/admin/service-categories/{id}`

**Mobile:**
- [ ] AdminServiceCategoryActivity (CRUD)

#### 3.2. Treatment Template Management (3-4 ngày)
**Backend:**
- [ ] API: `POST /api/admin/treatment-templates`
- [ ] API: `PUT /api/admin/treatment-templates/{id}`
- [ ] API: `DELETE /api/admin/treatment-templates/{id}`

**Mobile:**
- [ ] AdminTreatmentTemplateActivity
  - [ ] List templates
  - [ ] Create template
  - [ ] Edit template steps
  - [ ] Delete template

#### 3.3. User Management (3-4 ngày)
**Backend:**
- [ ] API: `GET /api/admin/users`
- [ ] API: `PUT /api/admin/users/{id}/status` (activate/deactivate)
- [ ] API: `POST /api/admin/patients`
- [ ] API: `POST /api/admin/admins`

**Mobile:**
- [ ] AdminUserManagementActivity
  - [ ] List all users (filter by role)
  - [ ] View user details
  - [ ] Activate/deactivate users
  - [ ] Create new users

#### 3.4. Analytics Dashboard (2-3 ngày)
**Backend:**
- [ ] API: `GET /api/admin/analytics/overview`
- [ ] API: `GET /api/admin/analytics/revenue`
- [ ] API: `GET /api/admin/analytics/appointments`

**Mobile:**
- [ ] AdminAnalyticsActivity
  - [ ] Overview cards (patients, doctors, appointments)
  - [ ] Charts (MPAndroidChart library)
  - [ ] Date range filter

---

### PHASE 4: Reception Features (NEW) (2-3 tuần) ⭐

#### 4.1. QR Generation for Check-in (3-4 ngày)
**Backend:**
- [x] API: `POST /api/reception/generate-checkin-qr` (đã làm)

**Mobile:**
- [ ] ReceptionQRGeneratorActivity
  - [ ] Search patient (phone/email/ID)
  - [ ] Display patient info
  - [ ] Show today's appointments
  - [ ] Generate QR code
  - [ ] Display large QR + code number
  - [ ] Option to share/print QR

#### 4.2. Patient Registration & Search (1 tuần)
**Backend:**
- [ ] API: `GET /api/reception/patients/search?q={query}`
- [ ] API: `POST /api/reception/patients`
- [ ] API: `PUT /api/reception/patients/{id}`

**Mobile:**
- [ ] ReceptionPatientSearchActivity
  - [ ] Search bar
  - [ ] Search results list
  - [ ] Patient detail view
- [ ] ReceptionPatientRegistrationActivity
  - [ ] Full registration form
  - [ ] Validation
  - [ ] Create account

#### 4.3. Appointment Management (1 tuần)
**Backend:**
- [ ] API: `GET /api/reception/appointments`
- [ ] API: `POST /api/reception/appointments`
- [ ] API: `PUT /api/reception/appointments/{id}`
- [ ] API: `DELETE /api/reception/appointments/{id}`
- [ ] API: `GET /api/reception/calendar?date={date}`

**Mobile:**
- [ ] ReceptionAppointmentActivity
  - [ ] Calendar view (library: Material Calendar View)
  - [ ] Day/Week view
  - [ ] Create appointment
  - [ ] Edit appointment
  - [ ] Cancel appointment
  - [ ] Filter by doctor/room

#### 4.4. Payment & Invoicing (3-4 ngày)
**Backend:**
- [ ] API: `POST /api/reception/invoices`
- [ ] API: `GET /api/reception/invoices/{id}`
- [ ] API: `POST /api/reception/payments`
- [ ] API: `GET /api/reception/invoices/{id}/print`

**Mobile:**
- [ ] ReceptionPaymentActivity
  - [ ] Create invoice
  - [ ] Select services
  - [ ] Calculate total
  - [ ] Apply discount
  - [ ] Process payment
  - [ ] Print receipt (Bluetooth printer or PDF)

---

### PHASE 5: Polish & Testing (1-2 tuần)

#### 5.1. UI/UX Improvements (3-4 ngày)
- [ ] Consistent design across all screens
- [ ] Loading states
- [ ] Error handling
- [ ] Empty states
- [ ] Animations & transitions
- [ ] Dark mode support (optional)

#### 5.2. Performance Optimization (2-3 ngày)
- [ ] Image caching (Glide/Picasso)
- [ ] API response caching
- [ ] Lazy loading for lists
- [ ] Memory leak fixes
- [ ] Battery optimization

#### 5.3. Testing (3-4 ngày)
- [ ] Unit tests for critical logic
- [ ] Integration tests for API calls
- [ ] UI tests for main flows
- [ ] Manual testing all features
- [ ] Bug fixes

#### 5.4. Documentation (2 ngày)
- [ ] Update README
- [ ] API documentation
- [ ] User guide
- [ ] Developer guide

---

## 📅 TIMELINE SUMMARY

| Phase | Duration | Features |
|-------|----------|----------|
| **Phase 1: Patient** | 2-3 tuần | Book appointment, Medical records, Payments, Profile |
| **Phase 2: Doctor** | 2-3 tuần | Medical records, Prescriptions, History, Schedule |
| **Phase 3: Admin** | 1-2 tuần | Categories, Templates, Users, Analytics |
| **Phase 4: Reception** | 2-3 tuần | QR generation, Patient search, Appointments, Payments |
| **Phase 5: Polish** | 1-2 tuần | UI/UX, Performance, Testing, Docs |
| **TOTAL** | **8-13 tuần** | **~2-3 tháng** |

---

## 🎯 PRIORITY ORDER (Nếu cần rút ngắn)

### Must Have (P0):
1. ✅ Patient: QR Check-in (done)
2. Patient: Book appointment
3. Patient: View medical records
4. Doctor: Create medical record
5. Doctor: Write prescription
6. Reception: Generate QR
7. Reception: Appointment management

### Should Have (P1):
8. Patient: Payment history
9. Doctor: Patient history
10. Admin: User management
11. Reception: Patient registration

### Nice to Have (P2):
12. Patient: Service reviews
13. Doctor: Statistics
14. Admin: Analytics dashboard
15. Reception: Print receipts

---

## 🛠️ TECHNICAL CONSIDERATIONS

### Libraries cần thêm:
```gradle
// Charts
implementation 'com.github.PhilJay:MPAndroidChart:v3.1.0'

// Calendar
implementation 'com.github.prolificinteractive:material-calendarview:2.0.1'

// Image loading
implementation 'com.github.bumptech.glide:glide:4.16.0'

// PDF generation (for receipts)
implementation 'com.itextpdf:itext7-core:7.2.5'

// Bluetooth printer (optional)
implementation 'com.github.DantSu:ESCPOS-ThermalPrinter-Android:3.3.0'
```

### Architecture:
- Continue with MVVM pattern
- Use Repository pattern for data layer
- LiveData/Flow for reactive updates
- Room database for offline caching (optional)

---

## 📝 NEXT STEPS

1. **Restart backend** để apply check-in changes
2. **Build & test** PatientQRScannerActivity
3. **Start Phase 1.1**: Book Appointment feature
4. **Weekly review** để track progress

---

## 🎉 AFTER MOBILE COMPLETION

Sau khi hoàn thành Mobile App:
1. ✅ Có sản phẩm hoàn chỉnh để demo
2. ✅ Backend APIs đã được test kỹ
3. ✅ Hiểu rõ business logic
4. ➡️ Bắt đầu Web App (dễ hơn vì đã có backend + logic)

---

**Bạn muốn bắt đầu từ Phase nào? Tôi khuyến nghị:**
1. **Test check-in flow mới** (PatientQRScannerActivity)
2. **Start Phase 1.1**: Book Appointment (feature quan trọng nhất cho patient)
