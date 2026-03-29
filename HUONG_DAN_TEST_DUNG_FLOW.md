# ⚠️ HƯỚNG DẪN TEST ĐÚNG FLOW

## 🔴 Vấn Đề Hiện Tại

Từ log, tôi thấy bạn đang:
1. Edit step 2 (Chụp X-quang răng)
2. Press nút Back (mũi tên)
3. Step 2 được save với status=PENDING (chưa complete)

**Đây KHÔNG phải là flow để test fix của chúng ta!**

## ✅ Flow Test ĐÚNG

### Scenario 1: Edit Step Đã COMPLETED (Test fix chính)

**Setup:**
- Cần có treatment plan với 3 steps
- TẤT CẢ 3 steps đều đã COMPLETED

**Các bước:**
1. Vào DoctorWorkflowActivity
2. Load treatment plan (tất cả steps đã COMPLETED)
3. Click nút "Sửa" (icon bút chì) trên step 1
4. Thay đổi dữ liệu (ví dụ: thêm ghi chú, thêm ảnh)
5. **Click "Hoàn thành bước"** ← QUAN TRỌNG
6. Kiểm tra status của step 3

**Kết quả mong đợi:**
- Step 1: COMPLETED (updated)
- Step 2: COMPLETED (không đổi)
- Step 3: COMPLETED (không đổi) ← KHÔNG phải IN_PROGRESS

### Scenario 2: Complete Step Lần Đầu (Flow bình thường)

**Setup:**
- Treatment plan với 3 steps
- Step 1: COMPLETED
- Step 2: IN_PROGRESS (đang làm)
- Step 3: PENDING

**Các bước:**
1. Làm việc với step 2 (thêm dữ liệu)
2. Click "Hoàn thành bước"
3. Kiểm tra status

**Kết quả mong đợi:**
- Step 1: COMPLETED
- Step 2: COMPLETED
- Step 3: IN_PROGRESS ← Auto-advance ĐÚNG

## 🎯 Cách Tạo Test Data

### Cách 1: Complete tất cả steps trước

```sql
-- Kiểm tra treatment plan hiện tại
SELECT id, service_id, sequence_order, status 
FROM treatment_plan_step 
WHERE plan_id = 1 
ORDER BY sequence_order;

-- Set tất cả steps thành COMPLETED
UPDATE treatment_plan_step 
SET status = 'COMPLETED', 
    completed_at = NOW() 
WHERE plan_id = 1;

-- Verify
SELECT id, service_id, sequence_order, status 
FROM treatment_plan_step 
WHERE plan_id = 1 
ORDER BY sequence_order;
```

### Cách 2: Complete từng step trong app

1. Load treatment plan mới
2. Complete step 1 → step 2 auto IN_PROGRESS
3. Complete step 2 → step 3 auto IN_PROGRESS
4. Complete step 3 → plan COMPLETED
5. Bây giờ edit step 1 để test fix

## 🐛 Lỗi Bạn Đang Gặp

Từ log của bạn:
```
editingStep: Chụp X-quang răng (ID: 2)
currentStep: null
→ Step 2: EDITING - status=PENDING
```

**Phân tích:**
- Bạn đang edit step 2
- Step 2 có status=PENDING (chưa complete lần nào)
- Khi press Back, mobile save step 2 với PENDING
- Đây là flow EDIT step chưa complete, KHÔNG phải re-complete step đã COMPLETED

**Fix của chúng ta chỉ áp dụng khi:**
- Step đã COMPLETED trước đó
- Bạn edit lại step đó
- Rồi complete lại (re-complete)

## 📝 Test Case Chi Tiết

### Test Case 1: Re-complete Step 1 (đã COMPLETED)

**Pre-condition:**
```
Step 1: COMPLETED
Step 2: COMPLETED  
Step 3: COMPLETED
```

**Steps:**
1. Click "Sửa" trên step 1
2. Thêm ghi chú: "Test re-complete"
3. Click "Hoàn thành bước"

**Expected:**
```
Step 1: COMPLETED (updated với ghi chú mới)
Step 2: COMPLETED (không đổi)
Step 3: COMPLETED (không đổi) ← FIX: Không auto IN_PROGRESS
```

**Backend log mong đợi:**
```
Re-completing step 1 - không auto-advance
```

### Test Case 2: Complete Step 2 Lần Đầu

**Pre-condition:**
```
Step 1: COMPLETED
Step 2: IN_PROGRESS (đang làm)
Step 3: PENDING
```

**Steps:**
1. Làm việc với step 2
2. Thêm dữ liệu
3. Click "Hoàn thành bước"

**Expected:**
```
Step 1: COMPLETED
Step 2: COMPLETED
Step 3: IN_PROGRESS ← Auto-advance ĐÚNG (lần đầu complete)
```

## 🚀 Cách Test Nhanh

### Option A: Dùng SQL

```sql
-- 1. Set tất cả steps thành COMPLETED
UPDATE treatment_plan_step 
SET status = 'COMPLETED', completed_at = NOW() 
WHERE plan_id = 1;

-- 2. Trong app: Edit step 1, complete lại
-- 3. Kiểm tra step 3 vẫn COMPLETED

SELECT id, service_id, status FROM treatment_plan_step WHERE plan_id = 1;
```

### Option B: Trong App

1. Tạo treatment plan mới
2. Complete tất cả 3 steps (step by step)
3. Sau khi tất cả COMPLETED, edit step 1
4. Complete lại step 1
5. Kiểm tra step 3

## ❌ Sai Lầm Thường Gặp

1. **Press Back thay vì "Hoàn thành bước"**
   - Back chỉ save draft, không trigger complete logic
   - Phải press "Hoàn thành bước" để test fix

2. **Edit step chưa complete**
   - Fix chỉ áp dụng cho re-complete (step đã COMPLETED)
   - Không áp dụng cho complete lần đầu

3. **Không verify pre-condition**
   - Phải đảm bảo tất cả steps đã COMPLETED trước khi test
   - Check database hoặc UI để confirm

## ✅ Checklist Trước Khi Test

- [ ] Backend đã restart với code mới
- [ ] Mobile app đã install version mới
- [ ] Treatment plan có 3 steps
- [ ] TẤT CẢ 3 steps đều COMPLETED (verify bằng SQL hoặc UI)
- [ ] Sẽ click "Hoàn thành bước", KHÔNG press Back
- [ ] Sẽ kiểm tra step 3 sau khi complete

## 🎯 Kết Luận

**Flow ĐÚNG để test fix:**
```
1. Tất cả steps COMPLETED
2. Edit step 1
3. Click "Hoàn thành bước" (KHÔNG phải Back)
4. Kiểm tra step 3 vẫn COMPLETED
```

**Flow SAI (không test được fix):**
```
1. Step 2 đang PENDING
2. Edit step 2
3. Press Back
4. Step 2 vẫn PENDING (đúng, vì chưa complete)
```
