-- ============================================
-- SETUP TEST DATA CHO FIX STEP 3 AUTO-ADVANCE
-- ============================================

-- Bước 1: Kiểm tra treatment plan hiện tại
SELECT 
    tps.id,
    tps.sequence_order,
    s.name as service_name,
    tps.status,
    tps.completed_at
FROM treatment_plan_step tps
JOIN service s ON tps.service_id = s.id
WHERE tps.plan_id = 1
ORDER BY tps.sequence_order;

-- Bước 2: Set TẤT CẢ steps thành COMPLETED
-- (Để test re-complete step đã COMPLETED)
UPDATE treatment_plan_step 
SET 
    status = 'COMPLETED',
    completed_at = NOW()
WHERE plan_id = 1;

-- Bước 3: Verify tất cả steps đã COMPLETED
SELECT 
    tps.id,
    tps.sequence_order,
    s.name as service_name,
    tps.status,
    tps.completed_at
FROM treatment_plan_step tps
JOIN service s ON tps.service_id = s.id
WHERE tps.plan_id = 1
ORDER BY tps.sequence_order;

-- Expected output:
-- id | sequence_order | service_name              | status    | completed_at
-- ---+----------------+---------------------------+-----------+-------------
--  1 |              0 | Khám và tư vấn răng miệng | COMPLETED | 2026-03-29...
--  2 |              1 | Chụp X-quang răng         | COMPLETED | 2026-03-29...
--  3 |              2 | Nhổ răng khôn             | COMPLETED | 2026-03-29...

-- ============================================
-- BÂY GIỜ TEST TRONG APP:
-- ============================================
-- 1. Vào DoctorWorkflowActivity
-- 2. Load treatment plan ID = 1
-- 3. Click "Sửa" trên step 1
-- 4. Thêm ghi chú hoặc thay đổi dữ liệu
-- 5. Click "Hoàn thành bước"
-- 6. Kiểm tra step 3 vẫn COMPLETED (KHÔNG phải IN_PROGRESS)

-- ============================================
-- VERIFY KẾT QUẢ SAU KHI TEST:
-- ============================================
SELECT 
    tps.id,
    tps.sequence_order,
    s.name as service_name,
    tps.status,
    tps.doctor_conclusion
FROM treatment_plan_step tps
JOIN service s ON tps.service_id = s.id
WHERE tps.plan_id = 1
ORDER BY tps.sequence_order;

-- Expected:
-- Step 1: COMPLETED (có doctor_conclusion mới)
-- Step 2: COMPLETED (không đổi)
-- Step 3: COMPLETED (không đổi) ← QUAN TRỌNG: Không phải IN_PROGRESS

-- ============================================
-- RESET VỀ TRẠNG THÁI BAN ĐẦU (NẾU CẦN):
-- ============================================
-- Nếu muốn test lại từ đầu:
UPDATE treatment_plan_step 
SET 
    status = CASE 
        WHEN sequence_order = 0 THEN 'COMPLETED'
        WHEN sequence_order = 1 THEN 'IN_PROGRESS'
        ELSE 'PENDING'
    END,
    completed_at = CASE 
        WHEN sequence_order = 0 THEN NOW()
        ELSE NULL
    END
WHERE plan_id = 1;
