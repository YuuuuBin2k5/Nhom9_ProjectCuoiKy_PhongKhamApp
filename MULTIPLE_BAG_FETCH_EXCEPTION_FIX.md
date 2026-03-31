# MultipleBagFetchException Fix - COMPLETE

## Lỗi

```
org.hibernate.loader.MultipleBagFetchException: 
cannot simultaneously fetch multiple bags: 
[com.hcmute.clinic.entity.MedicalRecord.details, 
 com.hcmute.clinic.entity.Prescription.details]
```

## Nguyên nhân

Hibernate không cho phép fetch nhiều **collection (bags)** cùng lúc trong một query với JOIN FETCH. 

Trong trường hợp này:
- `MedicalRecord.details` → List<MedicalRecordDetail>
- `Prescription.details` → List<PrescriptionDetail>

Cả hai đều là **OneToMany** collections, không thể fetch cùng lúc.

## Giải pháp

### Approach: Multiple Queries (Recommended)

Thay vì 1 query lớn, chia thành 3 queries nhỏ:

1. **Query 1**: Fetch medical records với basic relations (không có collections)
2. **Query 2**: Fetch medical record details
3. **Query 3**: Fetch prescription details

### Implementation

**File**: `clinic_backend/src/main/java/com/hcmute/clinic/repository/MedicalRecordRepository.java`

```java
// Step 1: Basic relations (no bags)
@Query("""
    SELECT DISTINCT mr FROM MedicalRecord mr
    LEFT JOIN FETCH mr.appointment a
    LEFT JOIN FETCH mr.doctor d
    LEFT JOIN FETCH d.clinicRoom
    LEFT JOIN FETCH mr.prescription p
    WHERE mr.patient.id = :patientId
    ORDER BY mr.createdAt DESC
    """)
List<MedicalRecord> findByPatientIdWithBasicRelations(@Param("patientId") Long patientId);

// Step 2: Fetch details separately
@Query("""
    SELECT DISTINCT mr FROM MedicalRecord mr
    LEFT JOIN FETCH mr.details det
    LEFT JOIN FETCH det.service s
    LEFT JOIN FETCH s.category
    WHERE mr IN :records
    """)
List<MedicalRecord> fetchDetails(@Param("records") List<MedicalRecord> records);

// Step 3: Fetch prescription details separately
@Query("""
    SELECT DISTINCT mr FROM MedicalRecord mr
    LEFT JOIN FETCH mr.prescription p
    LEFT JOIN FETCH p.details
    WHERE mr IN :records
    """)
List<MedicalRecord> fetchPrescriptionDetails(@Param("records") List<MedicalRecord> records);
```

### Usage in Controller

```java
// Step 1: Get basic medical records
List<MedicalRecord> medicalRecords = 
    medicalRecordRepository.findByPatientIdWithBasicRelations(id);

if (!medicalRecords.isEmpty()) {
    // Step 2: Fetch details
    medicalRecordRepository.fetchDetails(medicalRecords);
    
    // Step 3: Fetch prescription details
    medicalRecordRepository.fetchPrescriptionDetails(medicalRecords);
}
```

## Tại sao không dùng @BatchSize?

**Alternative approach** (không dùng):
```java
@OneToMany(mappedBy = "medicalRecord")
@BatchSize(size = 10)
private List<MedicalRecordDetail> details;
```

**Lý do không dùng**:
- Vẫn có N+1 queries (chỉ giảm từ N xuống N/10)
- Không giải quyết được MultipleBagFetchException
- Multiple queries approach rõ ràng và dễ debug hơn

## Performance Comparison

### Before (1 query - FAILED)
```
❌ MultipleBagFetchException
```

### After (3 queries - SUCCESS)
```
✅ Query 1: SELECT medical_records + appointment + doctor + prescription (no details)
✅ Query 2: SELECT medical_record_details + services
✅ Query 3: SELECT prescription_details
```

**Total**: 3 queries thay vì N+1 queries

### Example với 10 medical records:

**Old approach (N+1)**:
- 1 query: Get medical records
- 10 queries: Get details for each record
- 10 queries: Get prescription details for each record
- **Total: 21 queries**

**New approach (Multiple queries)**:
- 1 query: Get medical records with basic relations
- 1 query: Get all details for all records
- 1 query: Get all prescription details for all records
- **Total: 3 queries** ✅

## Alternative Solutions (Not Used)

### 1. Use Set instead of List
```java
@OneToMany(mappedBy = "medicalRecord")
private Set<MedicalRecordDetail> details;  // Set thay vì List
```
**Pros**: Có thể fetch multiple collections
**Cons**: Mất thứ tự, không phù hợp với use case

### 2. Use @Fetch(FetchMode.SUBSELECT)
```java
@OneToMany(mappedBy = "medicalRecord")
@Fetch(FetchMode.SUBSELECT)
private List<MedicalRecordDetail> details;
```
**Pros**: Tự động tạo subquery
**Cons**: Khó control, performance không tốt bằng explicit queries

### 3. DTO Projection
```java
@Query("SELECT new MedicalRecordDTO(...) FROM MedicalRecord mr ...")
```
**Pros**: Không có lazy loading issues
**Cons**: Phức tạp, mất entity features

## Files đã sửa

1. ✅ `clinic_backend/src/main/java/com/hcmute/clinic/repository/MedicalRecordRepository.java`
   - Thêm 3 methods: findByPatientIdWithBasicRelations, fetchDetails, fetchPrescriptionDetails

2. ✅ `clinic_backend/src/main/java/com/hcmute/clinic/controller/DoctorController.java`
   - Cập nhật getPatientMedicalRecords() để dùng 3-step fetching

## Testing

### 1. Enable SQL logging
```properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

### 2. Test API
```bash
curl -X GET "http://localhost:8081/api/doctor/patients/1/medical-records" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 3. Verify queries
Kiểm tra log, phải thấy đúng 3 queries:
```
Hibernate: SELECT DISTINCT mr ... (basic relations)
Hibernate: SELECT DISTINCT mr ... (details)
Hibernate: SELECT DISTINCT mr ... (prescription details)
```

## Status

✅ **FIXED** - MultipleBagFetchException đã được giải quyết bằng multiple queries approach.

## Rebuild & Test

```bash
# Rebuild backend
cd clinic_backend
./gradlew clean build

# Restart server
./gradlew bootRun

# Test API
curl -X GET "http://localhost:8081/api/doctor/patients/1/medical-records" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```
