-- ============================================
-- SCRIPT KIỂM TRA DỮ LIỆU COMPLETED STEPS
-- ============================================

-- 1. Kiểm tra tất cả treatment plans của bệnh nhân
SELECT 
    tp.id AS plan_id,
    tp.patient_id,
    p.first_name || ' ' || p.last_name AS patient_name,
    tp.status AS plan_status,
    tp.is_draft,
    tp.created_at,
    COUNT(tps.id) AS total_steps,
    SUM(CASE WHEN tps.status = 'COMPLETED' THEN 1 ELSE 0 END) AS completed_steps
FROM treatment_plans tp
JOIN patients p ON p.id = tp.patient_id
LEFT JOIN treatment_plan_steps tps ON tps.plan_id = tp.id
GROUP BY tp.id, tp.patient_id, p.first_name, p.last_name, tp.status, tp.is_draft, tp.created_at
ORDER BY tp.created_at DESC
LIMIT 10;

-- 2. Kiểm tra chi tiết các bước của treatment plan cụ thể
-- THAY ĐỔI: Thay ? bằng plan_id thực tế
SELECT 
    tps.id AS step_id,
    tps.sequence_order,
    s.name AS service_name,
    s.ui_template_type,
    tps.status,
    tps.doctor_conclusion,
    tps.completed_at,
    cr.name AS clinic_room_name,
    (SELECT COUNT(*) FROM step_images WHERE step_id = tps.id) AS image_count
FROM treatment_plan_steps tps
JOIN services s ON s.id = tps.service_id
LEFT JOIN clinic_rooms cr ON cr.id = tps.clinic_room_id
WHERE tps.plan_id = ?  -- THAY ? BẰNG PLAN_ID
ORDER BY tps.sequence_order;

-- 3. Kiểm tra các bước COMPLETED có đầy đủ thông tin không
SELECT 
    tps.id AS step_id,
    s.name AS service_name,
    tps.status,
    CASE 
        WHEN s.ui_template_type IS NULL THEN '❌ NULL'
        ELSE '✓ ' || s.ui_template_type
    END AS ui_template_type_status,
    CASE 
        WHEN tps.doctor_conclusion IS NULL OR tps.doctor_conclusion = '' THEN '❌ EMPTY'
        ELSE '✓ ' || SUBSTRING(tps.doctor_conclusion, 1, 50)
    END AS conclusion_status,
    (SELECT COUNT(*) FROM step_images WHERE step_id = tps.id) AS image_count
FROM treatment_plan_steps tps
JOIN services s ON s.id = tps.service_id
WHERE tps.status = 'COMPLETED'
ORDER BY tps.id DESC
LIMIT 20;

-- 4. Tìm các service KHÔNG có ui_template_type (cần fix)
SELECT 
    s.id,
    s.name,
    s.ui_template_type,
    sc.name AS category_name,
    COUNT(tps.id) AS used_in_steps
FROM services s
LEFT JOIN service_categories sc ON sc.id = s.category_id
LEFT JOIN treatment_plan_steps tps ON tps.service_id = s.id
WHERE s.ui_template_type IS NULL
GROUP BY s.id, s.name, s.ui_template_type, sc.name
ORDER BY used_in_steps DESC;

-- 5. Tìm các bước có status không chuẩn (không phải PENDING/IN_PROGRESS/COMPLETED/CANCELLED)
SELECT 
    tps.id,
    tps.status,
    s.name AS service_name,
    tp.patient_id
FROM treatment_plan_steps tps
JOIN services s ON s.id = tps.service_id
JOIN treatment_plans tp ON tp.id = tps.plan_id
WHERE tps.status NOT IN ('PENDING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')
ORDER BY tps.id DESC;

-- 6. Kiểm tra step images
SELECT 
    si.id,
    si.step_id,
    tps.status AS step_status,
    s.name AS service_name,
    si.image_url,
    si.created_at
FROM step_images si
JOIN treatment_plan_steps tps ON tps.id = si.step_id
JOIN services s ON s.id = tps.service_id
WHERE tps.status = 'COMPLETED'
ORDER BY si.created_at DESC
LIMIT 20;

-- ============================================
-- SCRIPT FIX DỮ LIỆU (NẾU CẦN)
-- ============================================

-- Fix 1: Cập nhật ui_template_type cho services
-- Chạy từng dòng tùy theo loại dịch vụ

-- Dịch vụ khám tổng quát
UPDATE services 
SET ui_template_type = 'GENERAL' 
WHERE name LIKE '%Khám%' OR name LIKE '%tổng quát%' OR name LIKE '%tư vấn%';

-- Dịch vụ X-Quang
UPDATE services 
SET ui_template_type = 'XRAY' 
WHERE name LIKE '%X-Quang%' OR name LIKE '%X-Ray%' OR name LIKE '%Chụp phim%';

-- Dịch vụ phẫu thuật
UPDATE services 
SET ui_template_type = 'SURGERY' 
WHERE name LIKE '%Nhổ%' OR name LIKE '%Phẫu thuật%' OR name LIKE '%Cắt%';

-- Dịch vụ niềng răng
UPDATE services 
SET ui_template_type = 'ORTHO' 
WHERE name LIKE '%Niềng%' OR name LIKE '%Chỉnh nha%' OR name LIKE '%Orthodontic%';

-- Fix 2: Chuẩn hóa status (nếu có status viết thường)
UPDATE treatment_plan_steps 
SET status = 'COMPLETED' 
WHERE LOWER(status) = 'completed' AND status != 'COMPLETED';

UPDATE treatment_plan_steps 
SET status = 'IN_PROGRESS' 
WHERE LOWER(status) = 'in_progress' AND status != 'IN_PROGRESS';

UPDATE treatment_plan_steps 
SET status = 'PENDING' 
WHERE LOWER(status) = 'pending' AND status != 'PENDING';

-- Fix 3: Thêm doctor_conclusion mẫu cho các bước COMPLETED không có conclusion
UPDATE treatment_plan_steps tps
SET doctor_conclusion = 'Đã hoàn thành dịch vụ: ' || (SELECT name FROM services WHERE id = tps.service_id)
WHERE tps.status = 'COMPLETED' 
  AND (tps.doctor_conclusion IS NULL OR tps.doctor_conclusion = '');

-- ============================================
-- SCRIPT TẠO DỮ LIỆU TEST
-- ============================================

-- Tạo treatment plan test với 2 bước COMPLETED
-- THAY ĐỔI: patient_id, appointment_id theo dữ liệu thực tế

-- Bước 1: Tạo treatment plan
INSERT INTO treatment_plans (patient_id, appointment_id, status, is_draft, created_at)
VALUES (?, ?, 'ACTIVE', false, NOW())
RETURNING id;  -- Lưu lại ID này

-- Bước 2: Thêm bước COMPLETED - Khám tổng quát
INSERT INTO treatment_plan_steps (
    plan_id, 
    service_id, 
    sequence_order, 
    status, 
    doctor_conclusion,
    completed_at
)
VALUES (
    ?,  -- plan_id từ bước 1
    (SELECT id FROM services WHERE ui_template_type = 'GENERAL' LIMIT 1),
    0,
    'COMPLETED',
    'Sâu răng số 6 và số 7. Cần hàn trám composite. Vệ sinh răng miệng kém, cần hướng dẫn đánh răng đúng cách.',
    NOW()
);

-- Bước 3: Thêm bước COMPLETED - X-Quang
INSERT INTO treatment_plan_steps (
    plan_id, 
    service_id, 
    sequence_order, 
    status, 
    doctor_conclusion,
    completed_at
)
VALUES (
    ?,  -- plan_id từ bước 1
    (SELECT id FROM services WHERE ui_template_type = 'XRAY' LIMIT 1),
    1,
    'COMPLETED',
    'Phim X-Quang cho thấy sâu răng sâu đến tủy răng số 6. Răng số 7 sâu nông. Không có tổn thương xương hàm.',
    NOW()
);

-- Bước 4: Thêm hình ảnh X-Quang (nếu có)
INSERT INTO step_images (step_id, image_url, created_at)
VALUES 
    (?, 'https://example.com/xray1.jpg', NOW()),
    (?, 'https://example.com/xray2.jpg', NOW());

-- Bước 5: Thêm bước IN_PROGRESS - Phẫu thuật
INSERT INTO treatment_plan_steps (
    plan_id, 
    service_id, 
    sequence_order, 
    status
)
VALUES (
    ?,  -- plan_id từ bước 1
    (SELECT id FROM services WHERE ui_template_type = 'SURGERY' LIMIT 1),
    2,
    'IN_PROGRESS'
);

-- ============================================
-- VERIFICATION QUERIES
-- ============================================

-- Kiểm tra lại sau khi fix
SELECT 
    'Services without ui_template_type' AS check_name,
    COUNT(*) AS count
FROM services 
WHERE ui_template_type IS NULL
UNION ALL
SELECT 
    'Completed steps without conclusion',
    COUNT(*)
FROM treatment_plan_steps 
WHERE status = 'COMPLETED' 
  AND (doctor_conclusion IS NULL OR doctor_conclusion = '')
UNION ALL
SELECT 
    'Steps with invalid status',
    COUNT(*)
FROM treatment_plan_steps 
WHERE status NOT IN ('PENDING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED');

-- Kết quả mong đợi: Tất cả count = 0
