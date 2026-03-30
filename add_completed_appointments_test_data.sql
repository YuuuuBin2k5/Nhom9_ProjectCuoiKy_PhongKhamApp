-- Script để thêm dữ liệu test cho báo cáo Admin
-- Tạo appointments với trạng thái COMPLETED để có dữ liệu doanh thu

-- Cập nhật một số appointments hiện tại thành COMPLETED
UPDATE appointments 
SET status = 'COMPLETED'
WHERE id IN (
    SELECT id FROM appointments 
    WHERE status != 'COMPLETED'
    LIMIT 5
);

-- Thêm appointments COMPLETED mới với dữ liệu đầy đủ
-- Lấy IDs từ các bảng liên quan
DO $$
DECLARE
    patient_id_1 BIGINT;
    patient_id_2 BIGINT;
    doctor_id_1 BIGINT;
    doctor_id_2 BIGINT;
    doctor_id_3 BIGINT;
    service_id_1 BIGINT;
    service_id_2 BIGINT;
    service_id_3 BIGINT;
BEGIN
    -- Lấy patient IDs
    SELECT id INTO patient_id_1 FROM patients WHERE phone = '0123456789' LIMIT 1;
    SELECT id INTO patient_id_2 FROM patients WHERE phone != '0123456789' LIMIT 1;
    
    -- Lấy doctor IDs
    SELECT id INTO doctor_id_1 FROM doctors WHERE specialization = 'Khám tổng quát' LIMIT 1;
    SELECT id INTO doctor_id_2 FROM doctors WHERE specialization = 'Nha khoa Tổng quát' LIMIT 1;
    SELECT id INTO doctor_id_3 FROM doctors WHERE specialization = 'Chỉnh nha' LIMIT 1;
    
    -- Lấy service IDs
    SELECT id INTO service_id_1 FROM services WHERE name LIKE '%Khám%' LIMIT 1;
    SELECT id INTO service_id_2 FROM services WHERE name LIKE '%Trám%' LIMIT 1;
    SELECT id INTO service_id_3 FROM services WHERE name LIKE '%Chỉnh nha%' LIMIT 1;
    
    -- Thêm appointments COMPLETED trong tháng hiện tại
    INSERT INTO appointments (patient_id, doctor_id, service_id, appointment_datetime, status, notes, created_at, updated_at)
    VALUES 
        (patient_id_1, doctor_id_1, service_id_1, NOW() - INTERVAL '5 days', 'COMPLETED', 'Khám tổng quát - Hoàn thành', NOW() - INTERVAL '5 days', NOW()),
        (patient_id_1, doctor_id_2, service_id_2, NOW() - INTERVAL '10 days', 'COMPLETED', 'Trám răng - Hoàn thành', NOW() - INTERVAL '10 days', NOW()),
        (patient_id_2, doctor_id_2, service_id_2, NOW() - INTERVAL '15 days', 'COMPLETED', 'Trám răng - Hoàn thành', NOW() - INTERVAL '15 days', NOW()),
        (patient_id_2, doctor_id_3, service_id_3, NOW() - INTERVAL '20 days', 'COMPLETED', 'Chỉnh nha - Hoàn thành', NOW() - INTERVAL '20 days', NOW()),
        (patient_id_1, doctor_id_1, service_id_1, NOW() - INTERVAL '25 days', 'COMPLETED', 'Khám tổng quát - Hoàn thành', NOW() - INTERVAL '25 days', NOW());
    
    -- Thêm appointments COMPLETED trong tháng trước
    INSERT INTO appointments (patient_id, doctor_id, service_id, appointment_datetime, status, notes, created_at, updated_at)
    VALUES 
        (patient_id_1, doctor_id_1, service_id_1, NOW() - INTERVAL '35 days', 'COMPLETED', 'Khám tổng quát - Hoàn thành', NOW() - INTERVAL '35 days', NOW()),
        (patient_id_2, doctor_id_2, service_id_2, NOW() - INTERVAL '40 days', 'COMPLETED', 'Trám răng - Hoàn thành', NOW() - INTERVAL '40 days', NOW()),
        (patient_id_1, doctor_id_3, service_id_3, NOW() - INTERVAL '45 days', 'COMPLETED', 'Chỉnh nha - Hoàn thành', NOW() - INTERVAL '45 days', NOW());
    
    -- Thêm một số reviews cho các dịch vụ
    INSERT INTO reviews (patient_id, doctor_id, service_id, rating, comment, created_at)
    VALUES 
        (patient_id_1, doctor_id_1, service_id_1, 5, 'Bác sĩ rất tận tâm và chuyên nghiệp', NOW() - INTERVAL '4 days'),
        (patient_id_1, doctor_id_2, service_id_2, 4, 'Dịch vụ tốt, giá cả hợp lý', NOW() - INTERVAL '9 days'),
        (patient_id_2, doctor_id_2, service_id_2, 5, 'Rất hài lòng với dịch vụ', NOW() - INTERVAL '14 days'),
        (patient_id_2, doctor_id_3, service_id_3, 4, 'Kết quả tốt, sẽ quay lại', NOW() - INTERVAL '19 days'),
        (patient_id_1, doctor_id_1, service_id_1, 5, 'Xuất sắc!', NOW() - INTERVAL '24 days');
    
    RAISE NOTICE 'Đã thêm dữ liệu test thành công!';
END $$;

-- Kiểm tra kết quả
SELECT 
    status,
    COUNT(*) as total
FROM appointments
GROUP BY status
ORDER BY status;

SELECT 
    'Total Completed Appointments' as description,
    COUNT(*) as count
FROM appointments
WHERE status = 'COMPLETED';
