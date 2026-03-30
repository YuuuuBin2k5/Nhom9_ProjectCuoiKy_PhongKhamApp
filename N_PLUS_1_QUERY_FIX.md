# Sửa lỗi N+1 Query Problem - Danh sách Bác sĩ

## Vấn đề
Khi load danh sách bác sĩ, Hibernate thực hiện N+1 queries:
```sql
-- Query 1: Load tất cả doctors
SELECT d.* FROM doctors d ORDER BY d.last_name OFFSET 0 ROWS FETCH FIRST 20 ROWS ONLY

-- Query 2-10: Load clinic_room cho TỪNG doctor (N queries)
SELECT cr.* FROM clinic_rooms cr WHERE cr.id=?
SELECT cr.* FROM clinic_rooms cr WHERE cr.id=?
SELECT cr.* FROM clinic_rooms cr WHERE cr.id=?
... (9 lần nữa)
```

## Nguyên nhân
1. `Doctor` entity có quan hệ `@ManyToOne` với `ClinicRoom`
2. Mặc định, `@ManyToOne` sử dụng `FetchType.LAZY`
3. Khi map sang response, code truy cập `d.getClinicRoom().getName()`
4. Hibernate phải query riêng lẻ cho từng doctor → N+1 problem

## Giải pháp đã áp dụng

### 1. Sử dụng @EntityGraph
Thêm vào `DoctorRepository.java`:

```java
@EntityGraph(attributePaths = {"clinicRoom"})
Page<Doctor> findAll(Pageable pageable);
```

`@EntityGraph` báo cho Hibernate biết: "Hãy fetch luôn `clinicRoom` trong cùng 1 query"

### 2. Kết quả
Hibernate giờ chỉ thực hiện 1 query với LEFT JOIN:

```sql
SELECT 
    d.id, d.first_name, d.last_name, d.email, d.specialization,
    d.experience_years, d.is_active, d.avatar_url,
    cr.id, cr.name, cr.description, cr.is_active
FROM doctors d 
LEFT JOIN clinic_rooms cr ON d.clinic_room_id = cr.id
ORDER BY d.last_name
OFFSET 0 ROWS FETCH FIRST 20 ROWS ONLY
```

## So sánh hiệu năng

### Trước khi fix (N+1 queries)
- 9 bác sĩ → 10 queries (1 + 9)
- 20 bác sĩ → 21 queries (1 + 20)
- 100 bác sĩ → 101 queries (1 + 100)

### Sau khi fix (1 query)
- 9 bác sĩ → 1 query
- 20 bác sĩ → 1 query
- 100 bác sĩ → 1 query

## Lợi ích
✅ Giảm số lượng queries từ N+1 xuống còn 1
✅ Cải thiện hiệu năng đáng kể
✅ Giảm tải cho database
✅ Response time nhanh hơn

## Các file đã sửa
1. `clinic_backend/src/main/java/com/hcmute/clinic/repository/DoctorRepository.java`
   - Thêm `@EntityGraph(attributePaths = {"clinicRoom"})`
   - Override method `findAll(Pageable)`

2. `clinic_backend/src/main/java/com/hcmute/clinic/controller/AdminDoctorController.java`
   - Thêm comment giải thích

## Kiểm tra fix đã hoạt động

### Cách 1: Xem log Hibernate
Trong `application.properties`:
```properties
spring.jpa.show-sql=true
logging.level.org.hibernate.SQL=DEBUG
```

Sau khi fix, bạn sẽ thấy:
```sql
-- CHỈ 1 query với LEFT JOIN
select d1_0.id, ... cr1_0.id, cr1_0.name, ...
from doctors d1_0 
left join clinic_rooms cr1_0 on cr1_0.id=d1_0.clinic_room_id
order by d1_0.last_name
offset ? rows fetch first ? rows only
```

### Cách 2: Test API
```bash
curl http://localhost:8081/api/admin/doctors
```

Response vẫn giống như trước, nhưng nhanh hơn!

## Lưu ý kỹ thuật

### @EntityGraph vs @Query với JOIN FETCH
- `@EntityGraph`: Hoạt động tốt với pagination
- `@Query + JOIN FETCH`: Có vấn đề với pagination (count query)
- → Nên dùng `@EntityGraph` cho trường hợp này

### Khi nào dùng @EntityGraph?
✅ Khi luôn cần load relationship
✅ Khi có pagination
✅ Khi muốn tránh N+1 problem

### Khi nào KHÔNG dùng @EntityGraph?
❌ Khi relationship rất lớn (nhiều data)
❌ Khi chỉ đôi khi cần load relationship
❌ Khi có nhiều level nested relationships

## Các entity khác cần kiểm tra
Nên áp dụng tương tự cho:
- `TreatmentPlan` → `patient`, `doctor`
- `TreatmentPlanStep` → `treatmentPlan`, `service`
- `Appointment` → `patient`, `doctor`, `room`
- `Invoice` → `patient`, `items`

## Tóm tắt
🎯 Đã sửa N+1 query problem cho danh sách bác sĩ
🎯 Giảm từ 10 queries xuống còn 1 query
🎯 Cải thiện hiệu năng đáng kể
🎯 Sử dụng @EntityGraph - best practice của Spring Data JPA
