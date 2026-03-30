# ADMIN MODULE - COMPREHENSIVE TESTING GUIDE

## 📋 TỔNG QUAN

**Mục đích:** Hướng dẫn test toàn bộ Admin Module sau khi hoàn thành Phase 1 & 2
**Thời gian test ước tính:** 2-3 giờ
**Người test:** Admin user

---

## 🎯 CHECKLIST TỔNG QUAN

### Phase 1 - Critical Fixes:
- [ ] Task 1: API Date Range Support
- [ ] Task 2: Navigation Fix
- [ ] Task 3: Room CRUD
- [ ] Task 4: Service Edit/Delete
- [ ] Task 5: Doctor Edit/Delete

### Phase 2 - Missing Features:
- [ ] Feature 1: Room Assignment for Doctors
- [ ] Feature 2: Category Management
- [ ] Feature 3: Queue Enhancement (Search & Statistics)
- [ ] Feature 4: Real-time Updates (Auto-refresh)

---

## 📝 CHI TIẾT TEST CASES

### 1. ADMIN NAVIGATION TEST

**Mục tiêu:** Kiểm tra navigation đúng role

**Steps:**
1. Login với admin account
2. Vào AdminMainActivity
3. Kiểm tra các cards hiển thị:
   - [ ] Dịch vụ (Services)
   - [ ] Danh mục (Categories) - MỚI
   - [ ] Phòng khám (Rooms)
   - [ ] Bác sĩ (Doctors)
   - [ ] Hàng đợi (Queue)
   - [ ] Dashboard
   - [ ] Cài đặt (Settings)
   - [ ] Đăng xuất (Logout)

**Expected:**
- ✅ Có 8 cards (không có "Doctor Workflow")
- ✅ Layout đẹp, icons đúng màu
- ✅ Click vào mỗi card navigate đúng

---

### 2. DASHBOARD TEST (Phase 1 - Task 1)

**Mục tiêu:** Test API date range support

**Steps:**
1. Click vào "Dashboard" card
2. Chọn date range (ví dụ: 01/03/2026 - 30/03/2026)
3. Click "Xem báo cáo"
4. Kiểm tra data hiển thị:
   - [ ] Revenue chart
   - [ ] Top services
   - [ ] Doctor performance

**Test Cases:**
- [ ] Test với date range cụ thể
- [ ] Test với year/month
- [ ] Test với current month (default)
- [ ] Test error handling (invalid dates)

**Expected:**
- ✅ API accept cả date range VÀ year/month
- ✅ Priority: date range > year/month > current month
- ✅ Data hiển thị đúng
- ✅ Charts render correctly

---

### 3. ROOM MANAGEMENT TEST (Phase 1 - Task 3)

**Mục tiêu:** Test full CRUD operations

#### 3.1 Create Room
**Steps:**
1. Click "Phòng khám" card
2. Click FAB button (+)
3. Nhập thông tin:
   - Name: "P101"
   - Description: "Phòng khám tổng quát"
4. Click "Lưu"

**Test Cases:**
- [ ] Create với valid data → Success
- [ ] Create với name < 3 chars → Error
- [ ] Create với name > 50 chars → Error
- [ ] Create với empty name → Error

**Expected:**
- ✅ Room được tạo thành công
- ✅ Hiển thị trong danh sách
- ✅ Status badge "Hoạt động" (green)

#### 3.2 Read Rooms
**Steps:**
1. Xem danh sách rooms
2. Pull to refresh

**Expected:**
- ✅ Danh sách load đúng
- ✅ Hiển thị name, description, status
- ✅ SwipeRefresh works

#### 3.3 Update Room
**Steps:**
1. Click menu (3 dots) trên room item
2. Select "Sửa"
3. Dialog hiển thị với data pre-filled
4. Sửa name thành "P101 - Updated"
5. Click "Lưu"

**Test Cases:**
- [ ] Edit với valid data → Success
- [ ] Edit với invalid data → Error
- [ ] Cancel edit → No changes

**Expected:**
- ✅ Room được update
- ✅ Danh sách refresh
- ✅ Data mới hiển thị đúng

#### 3.4 Toggle Status
**Steps:**
1. Click menu trên room item
2. Select "Tắt hoạt động"
3. Confirm

**Expected:**
- ✅ Status badge chuyển sang "Không hoạt động" (red)
- ✅ Room vẫn trong danh sách

#### 3.5 Delete Room
**Steps:**
1. Click menu trên room item
2. Select "Xóa"
3. Confirmation dialog hiển thị
4. Click "Xóa"

**Expected:**
- ✅ Confirmation dialog xuất hiện
- ✅ Room bị xóa khỏi danh sách
- ✅ Toast "Xóa phòng thành công"

---

### 4. SERVICE MANAGEMENT TEST (Phase 1 - Task 4)

**Mục tiêu:** Test Edit/Delete operations

#### 4.1 Edit Service
**Steps:**
1. Click "Dịch vụ" card
2. Click menu trên service item
3. Select "Sửa"
4. Dialog hiển thị với data pre-filled
5. Sửa name, price, description
6. Upload new image (optional)
7. Click "Lưu"

**Test Cases:**
- [ ] Edit name → Success
- [ ] Edit price → Success
- [ ] Edit description → Success
- [ ] Edit image → Success
- [ ] Edit category → Success

**Expected:**
- ✅ Service được update
- ✅ Image upload works
- ✅ Data mới hiển thị đúng

#### 4.2 Delete Service
**Steps:**
1. Click menu trên service item
2. Select "Xóa"
3. Confirmation dialog
4. Click "Xóa"

**Expected:**
- ✅ Confirmation dialog
- ✅ Service bị xóa
- ✅ Toast message

---

### 5. DOCTOR MANAGEMENT TEST (Phase 1 - Task 5 + Phase 2 - Feature 1)

**Mục tiêu:** Test Edit/Delete + Room Assignment

#### 5.1 Create Doctor with Room
**Steps:**
1. Click "Bác sĩ" card
2. Click FAB button
3. Nhập thông tin:
   - First Name: "Nguyễn"
   - Last Name: "Văn A"
   - Email: "doctor.a@clinic.com"
   - Password: "Password123!"
   - Specialty: "Nha khoa tổng quát"
   - Experience: 5
   - Bio: "Bác sĩ giàu kinh nghiệm"
   - **Room: "P101"** (MỚI - Phase 2 Feature 1)
4. Click "Lưu"

**Test Cases:**
- [ ] Create với room → Success
- [ ] Create không chọn room → Success
- [ ] Room hiển thị trong danh sách → Success

**Expected:**
- ✅ Doctor được tạo
- ✅ Room "P101" hiển thị dưới specialty
- ✅ Text màu xám: "Phòng: P101"

#### 5.2 Edit Doctor's Room
**Steps:**
1. Click menu trên doctor item
2. Select "Sửa"
3. Dialog hiển thị với room pre-selected
4. Chọn room khác: "P102"
5. Click "Lưu"

**Test Cases:**
- [ ] Change room → Success
- [ ] Remove room (chọn "Không gán phòng") → Success
- [ ] Room mới hiển thị → Success

**Expected:**
- ✅ Room được update
- ✅ Danh sách hiển thị room mới

#### 5.3 Delete Doctor
**Steps:**
1. Click menu trên doctor item
2. Select "Xóa"
3. Confirmation dialog
4. Click "Xóa"

**Expected:**
- ✅ Confirmation dialog
- ✅ Doctor bị xóa
- ✅ Toast message

---

### 6. CATEGORY MANAGEMENT TEST (Phase 2 - Feature 2)

**Mục tiêu:** Test full CRUD for categories

#### 6.1 Navigate to Category Management
**Steps:**
1. Từ AdminMainActivity
2. Click "Danh mục" card
3. AdminCategoryActivity opens

**Expected:**
- ✅ Activity opens correctly
- ✅ Toolbar có back button
- ✅ FAB button visible

#### 6.2 Create Category
**Steps:**
1. Click FAB button
2. Dialog hiển thị
3. Nhập:
   - Name: "Nha khoa thẩm mỹ"
   - Description: "Các dịch vụ làm đẹp răng"
4. Click "Lưu"

**Test Cases:**
- [ ] Create với valid data → Success
- [ ] Create với name < 3 chars → Error
- [ ] Create với name > 50 chars → Error
- [ ] Create với empty name → Error
- [ ] Description optional → Success

**Expected:**
- ✅ Category được tạo
- ✅ Hiển thị trong danh sách
- ✅ Toast "Thêm danh mục thành công"

#### 6.3 Edit Category
**Steps:**
1. Click menu (3 dots) trên category item
2. Select "Sửa"
3. Dialog với data pre-filled
4. Sửa name thành "Nha khoa thẩm mỹ - Updated"
5. Click "Lưu"

**Expected:**
- ✅ Category được update
- ✅ Danh sách refresh
- ✅ Toast "Cập nhật danh mục thành công"

#### 6.4 Delete Category
**Steps:**
1. Click menu trên category item
2. Select "Xóa"
3. Confirmation dialog:
   - Message: "Bạn có chắc muốn xóa danh mục [name]?"
   - Note: "Các dịch vụ thuộc danh mục này sẽ không bị xóa"
4. Click "Xóa"

**Expected:**
- ✅ Confirmation dialog đúng
- ✅ Category bị xóa
- ✅ Services thuộc category KHÔNG bị xóa
- ✅ Toast "Xóa danh mục thành công"

#### 6.5 Pull to Refresh
**Steps:**
1. Swipe down để refresh

**Expected:**
- ✅ Loading indicator
- ✅ Danh sách reload

---

### 7. QUEUE MANAGEMENT TEST (Phase 2 - Feature 3 & 4)

**Mục tiêu:** Test Search, Statistics, Auto-refresh

#### 7.1 View Queue
**Steps:**
1. Click "Hàng đợi" card
2. QueueManagementActivity opens
3. Chọn phòng từ spinner

**Expected:**
- ✅ Activity opens
- ✅ Statistics card hiển thị (MỚI)
- ✅ Search bar hiển thị (MỚI)
- ✅ Room spinner works
- ✅ Tabs: Đang chờ, Cận lâm sàng, Ưu tiên

#### 7.2 Statistics Display (MỚI - Feature 3)
**Steps:**
1. Xem statistics card ở top
2. Kiểm tra 3 metrics:
   - Đang chờ (blue number)
   - Phút TB (teal number)
   - Ưu tiên (amber number)

**Expected:**
- ✅ Statistics card đẹp với 3 columns
- ✅ Numbers update real-time
- ✅ Colors đúng:
  - Blue: Total waiting
  - Teal: Average time
  - Amber: Priority count
- ✅ Dividers giữa columns

#### 7.3 Search Functionality (MỚI - Feature 3)
**Steps:**
1. Gõ vào search bar
2. Test search với:
   - Tên bệnh nhân: "Nguyễn"
   - Số điện thoại: "0901"
   - Số thứ tự: "5"

**Test Cases:**
- [ ] Search by name → Filters correctly
- [ ] Search by phone → Filters correctly
- [ ] Search by queue number → Filters correctly
- [ ] Clear search (X button) → Shows all
- [ ] Empty search → Shows all

**Expected:**
- ✅ Real-time filtering khi gõ
- ✅ Results hiển thị đúng
- ✅ Statistics update theo filtered data
- ✅ Clear button works
- ✅ No results → Empty state

#### 7.4 Auto-refresh (MỚI - Feature 4)
**Steps:**
1. Mở QueueManagementActivity
2. Để activity open trong 30 giây
3. Quan sát auto-refresh

**Test Cases:**
- [ ] Auto-refresh sau 30 giây → Success
- [ ] Minimize app → Refresh stops
- [ ] Resume app → Refresh continues
- [ ] Manual refresh button → Works
- [ ] Firebase real-time → Still works

**Expected:**
- ✅ Queue tự động reload mỗi 30 giây
- ✅ Không refresh khi app minimized
- ✅ Resume lại khi app active
- ✅ Manual refresh vẫn hoạt động
- ✅ Firebase real-time vẫn hoạt động

#### 7.5 Queue Actions
**Steps:**
1. Test các actions:
   - Call patient
   - Examine patient
   - Transfer to X-Ray
   - Complete patient

**Expected:**
- ✅ Tất cả actions work
- ✅ Toast messages đúng
- ✅ Queue update sau action
- ✅ Statistics update

---

## 🧪 INTEGRATION TESTS

### Test 1: Doctor → Room → Queue Flow
**Steps:**
1. Tạo room "P101"
2. Tạo doctor gán vào "P101"
3. Vào Queue Management
4. Chọn room "P101"
5. Kiểm tra queue của room đó

**Expected:**
- ✅ Doctor được gán đúng room
- ✅ Queue hiển thị đúng room
- ✅ Workflow hoạt động end-to-end

### Test 2: Category → Service Flow
**Steps:**
1. Tạo category "Nha khoa thẩm mỹ"
2. Tạo service thuộc category đó
3. Edit category name
4. Kiểm tra service vẫn thuộc category

**Expected:**
- ✅ Service thuộc đúng category
- ✅ Edit category không ảnh hưởng service
- ✅ Delete category không xóa service

### Test 3: Search + Statistics + Auto-refresh
**Steps:**
1. Vào Queue Management
2. Quan sát statistics ban đầu
3. Search một bệnh nhân
4. Kiểm tra statistics update
5. Đợi 30 giây
6. Kiểm tra auto-refresh

**Expected:**
- ✅ Statistics đúng ban đầu
- ✅ Statistics update khi search
- ✅ Auto-refresh không break search
- ✅ All features work together

---

## 🐛 ERROR HANDLING TESTS

### Test Invalid Input
- [ ] Empty fields → Error messages
- [ ] Too short/long text → Validation errors
- [ ] Invalid email → Error
- [ ] Weak password → Error
- [ ] Duplicate names → Handle gracefully

### Test Network Errors
- [ ] No internet → Error toast
- [ ] Slow connection → Loading states
- [ ] API errors → User-friendly messages
- [ ] Timeout → Retry option

### Test Edge Cases
- [ ] Empty lists → Empty states
- [ ] Delete last item → Empty state
- [ ] Rapid clicks → No duplicate actions
- [ ] Back button → No data loss

---

## 📊 PERFORMANCE TESTS

### Load Time
- [ ] AdminMainActivity loads < 1s
- [ ] Room list loads < 2s
- [ ] Service list loads < 2s
- [ ] Doctor list loads < 2s
- [ ] Category list loads < 2s
- [ ] Queue loads < 3s

### Memory
- [ ] No memory leaks
- [ ] Auto-refresh stops when paused
- [ ] Firebase listeners cleaned up
- [ ] Images loaded efficiently

### UI Responsiveness
- [ ] Smooth scrolling
- [ ] No lag when typing search
- [ ] Animations smooth
- [ ] No ANR (Application Not Responding)

---

## ✅ ACCEPTANCE CRITERIA

### Must Pass (Critical):
- [ ] All CRUD operations work
- [ ] No crashes
- [ ] Data persists correctly
- [ ] Navigation works
- [ ] Search works
- [ ] Statistics accurate

### Should Pass (Important):
- [ ] Auto-refresh works
- [ ] Loading states show
- [ ] Empty states show
- [ ] Error messages clear
- [ ] Validation works

### Nice to Have:
- [ ] Animations smooth
- [ ] UI polished
- [ ] Performance good
- [ ] No bugs found

---

## 📝 BUG REPORT TEMPLATE

Nếu tìm thấy bug, report theo format:

```
**Bug Title:** [Short description]

**Severity:** Critical / High / Medium / Low

**Steps to Reproduce:**
1. Step 1
2. Step 2
3. Step 3

**Expected Result:**
What should happen

**Actual Result:**
What actually happened

**Screenshots:**
[Attach if possible]

**Device Info:**
- Android version:
- Device model:
- App version:

**Additional Notes:**
Any other relevant information
```

---

## 🎯 TESTING CHECKLIST SUMMARY

### Phase 1 (5 tasks):
- [ ] Task 1: Dashboard date range ✅
- [ ] Task 2: Navigation fix ✅
- [ ] Task 3: Room CRUD ✅
- [ ] Task 4: Service Edit/Delete ✅
- [ ] Task 5: Doctor Edit/Delete ✅

### Phase 2 (4 features):
- [ ] Feature 1: Room assignment ✅
- [ ] Feature 2: Category CRUD ✅
- [ ] Feature 3: Queue search & stats ✅
- [ ] Feature 4: Auto-refresh ✅

### Integration:
- [ ] Doctor → Room → Queue flow ✅
- [ ] Category → Service flow ✅
- [ ] Search + Stats + Refresh ✅

### Error Handling:
- [ ] Invalid input ✅
- [ ] Network errors ✅
- [ ] Edge cases ✅

### Performance:
- [ ] Load times ✅
- [ ] Memory usage ✅
- [ ] UI responsiveness ✅

---

## 📞 SUPPORT

Nếu cần hỗ trợ trong quá trình test:
- Xem documentation: `ADMIN_PHASE2_ALL_FEATURES_COMPLETE.md`
- Xem implementation details: `ADMIN_PHASE1_FINAL_SUMMARY.md`
- Report bugs theo template trên

---

**Happy Testing!** 🎉

Chúc bạn test thành công và tìm ra ít bugs nhất có thể!
