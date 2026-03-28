# PHASE 3: CẢI THIỆN VÀ TỐI ƯU HÓA

## 🟢 MEDIUM PRIORITY

### 1. Tìm kiếm Dịch vụ (UC16)

**Backend:**
```java
@GetMapping("/api/services/search")
public List<ServiceDto> searchServices(
    @RequestParam String q,
    @RequestParam(required = false) Long categoryId
) {
    if (categoryId != null) {
        return serviceRepository
            .findByCategoryIdAndNameContainingIgnoreCaseAndActiveTrue(
                categoryId, q
            );
    }
    return serviceRepository
        .findByNameContainingIgnoreCaseAndActiveTrue(q);
}
```

**Mobile:**
```kotlin
// ServiceListActivity - Thêm SearchView
override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.menu_search, menu)
    val searchItem = menu.findItem(R.id.action_search)
    val searchView = searchItem.actionView as SearchView
    
    searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String): Boolean {
            searchServices(query)
            return true
        }
        override fun onQueryTextChange(newText: String): Boolean {
            if (newText.length >= 2) searchServices(newText)
            return true
        }
    })
    return true
}
```

**Ước tính:** 1 ngày

---

### 2. In đơn thuốc PDF

**Backend:**
```java
// Thêm dependency
// implementation 'com.itextpdf:itext7-core:7.2.5'

@GetMapping("/api/prescriptions/{id}/pdf")
public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
    Prescription prescription = prescriptionRepository.findById(id)
        .orElseThrow();
    
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PdfWriter writer = new PdfWriter(baos);
    PdfDocument pdf = new PdfDocument(writer);
    Document document = new Document(pdf);
    
    // Header
    document.add(new Paragraph("ĐƠN THUỐC")
        .setFontSize(20)
        .setBold()
        .setTextAlignment(TextAlignment.CENTER));
    
    // Patient info
    document.add(new Paragraph("Bệnh nhân: " + 
        prescription.getMedicalRecord().getPatient().getFullName()));
    document.add(new Paragraph("Ngày kê đơn: " + 
        prescription.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
    document.add(new Paragraph("Bác sĩ: " + 
        prescription.getDoctor().getFullName()));
    
    // Medicine table
    Table table = new Table(5);
    table.addHeaderCell("STT");
    table.addHeaderCell("Tên thuốc");
    table.addHeaderCell("Liều lượng");
    table.addHeaderCell("Tần suất");
    table.addHeaderCell("Thời gian");
    
    int index = 1;
    for (PrescriptionDetail detail : prescription.getDetails()) {
        table.addCell(String.valueOf(index++));
        table.addCell(detail.getMedicineName());
        table.addCell(detail.getDosage());
        table.addCell(detail.getFrequency());
        table.addCell(detail.getDuration());
    }
    document.add(table);
    
    // Footer
    document.add(new Paragraph("\nChữ ký bác sĩ")
        .setTextAlignment(TextAlignment.RIGHT)
        .setMarginTop(50));
    
    document.close();
    
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_PDF);
    headers.setContentDispositionFormData("attachment", 
        "prescription_" + id + ".pdf");
    
    return ResponseEntity.ok()
        .headers(headers)
        .body(baos.toByteArray());
}
```

**Mobile:**
```kotlin
// PrescriptionDetailActivity - Thêm nút In
btnPrint.setOnClickListener {
    val url = "${ApiConfig.BASE_URL}/prescriptions/${prescriptionId}/pdf"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    startActivity(intent)
}
```

**Ước tính:** 2 ngày

---

### 3. Password Complexity Validation

**Backend:**
```java
// Validation annotation
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = StrongPasswordValidator.class)
public @interface StrongPassword {
    String message() default "Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

// Validator
public class StrongPasswordValidator 
    implements ConstraintValidator<StrongPassword, String> {
    
    private static final String PASSWORD_PATTERN = 
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
    
    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) return false;
        return password.matches(PASSWORD_PATTERN);
    }
}

// DTO
public class RegisterRequest {
    @NotBlank
    @Email
    private String email;
    
    @StrongPassword
    private String password;
    
    // ... other fields
}
```

**Mobile:**
```kotlin
// RegisterActivity - Real-time validation
etPassword.addTextChangedListener(object : TextWatcher {
    override fun afterTextChanged(s: Editable?) {
        val password = s.toString()
        val hasMinLength = password.length >= 8
        val hasUpperCase = password.any { it.isUpperCase() }
        val hasLowerCase = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecial = password.any { "!@#$%^&*()".contains(it) }
        
        tvPasswordStrength.text = when {
            hasMinLength && hasUpperCase && hasLowerCase && hasDigit && hasSpecial -> 
                "Mạnh"
            hasMinLength && (hasUpperCase || hasLowerCase) && hasDigit -> 
                "Trung bình"
            else -> 
                "Yếu"
        }
    }
})
```

**Ước tính:** 1 ngày

---

### 4. Pagination cho danh sách lớn

**Backend:**
```java
// PatientController
@GetMapping("/me/medical-records")
public Page<MedicalRecordDto> getMyRecords(
    @PageableDefault(size = 20, sort = "createdAt", direction = DESC) 
    Pageable pageable,
    Authentication auth
) {
    Long patientId = Long.parseLong(auth.getName());
    return medicalRecordRepository
        .findByPatientId(patientId, pageable)
        .map(this::toDto);
}

// AdminDoctorController
@GetMapping
public Page<DoctorDto> getAllDoctors(
    @PageableDefault(size = 20, sort = "lastName") 
    Pageable pageable
) {
    return doctorRepository.findAll(pageable)
        .map(this::toDto);
}
```

**Mobile:**
```kotlin
// MedicalRecordActivity - Endless scroll
class MedicalRecordActivity : AppCompatActivity() {
    private var currentPage = 0
    private var isLoading = false
    private var isLastPage = false
    
    private fun setupRecyclerView() {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                
                if (!isLoading && !isLastPage) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                        && firstVisibleItemPosition >= 0) {
                        loadMoreRecords()
                    }
                }
            }
        })
    }
    
    private fun loadMoreRecords() {
        isLoading = true
        currentPage++
        
        apiService.getMedicalRecords(currentPage, 20)
            .enqueue(object : Callback<Page<MedicalRecordDto>> {
                override fun onResponse(call: Call, response: Response) {
                    isLoading = false
                    if (response.isSuccessful) {
                        val page = response.body()
                        adapter.addItems(page.content)
                        isLastPage = page.last
                    }
                }
                override fun onFailure(call: Call, t: Throwable) {
                    isLoading = false
                }
            })
    }
}
```

**Ước tính:** 2 ngày

---

### 5. Admin Room CRUD đầy đủ

**Backend:**
```java
// AdminRoomController - Bổ sung
@PostMapping
public ResponseEntity<?> createRoom(@RequestBody RoomRequest request) {
    ClinicRoom room = ClinicRoom.builder()
        .name(request.getName())
        .description(request.getDescription())
        .active(true)
        .build();
    room = clinicRoomRepository.save(room);
    return ResponseEntity.ok(room);
}

@PutMapping("/{id}")
public ResponseEntity<?> updateRoom(
    @PathVariable Long id,
    @RequestBody RoomRequest request
) {
    ClinicRoom room = clinicRoomRepository.findById(id)
        .orElseThrow();
    room.setName(request.getName());
    room.setDescription(request.getDescription());
    clinicRoomRepository.save(room);
    return ResponseEntity.ok(room);
}

@DeleteMapping("/{id}")
public ResponseEntity<?> deleteRoom(@PathVariable Long id) {
    // Soft delete
    ClinicRoom room = clinicRoomRepository.findById(id)
        .orElseThrow();
    room.setActive(false);
    clinicRoomRepository.save(room);
    return ResponseEntity.ok(Map.of("message", "Đã xóa phòng"));
}
```

**Mobile:**
```kotlin
// AdminRoomActivity - Thêm dialog edit
private fun showEditDialog(room: RoomItem) {
    val dialog = Dialog(this)
    dialog.setContentView(R.layout.dialog_edit_room)
    
    val etName = dialog.findViewById<EditText>(R.id.etRoomName)
    val etDesc = dialog.findViewById<EditText>(R.id.etRoomDescription)
    val btnSave = dialog.findViewById<Button>(R.id.btnSave)
    
    etName.setText(room.name)
    etDesc.setText(room.description)
    
    btnSave.setOnClickListener {
        val request = UpdateRoomRequest(
            name = etName.text.toString(),
            description = etDesc.text.toString()
        )
        apiService.updateRoom(room.id, request).enqueue(...)
        dialog.dismiss()
    }
    
    dialog.show()
}
```

**Ước tính:** 1 ngày

---

### 6. Doctor.yearsOfExperience field

**Backend:**
```java
// Entity Doctor - Thêm field
@Entity
public class Doctor {
    // ... existing fields
    
    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;
}

// CreateDoctorRequest DTO
public class CreateDoctorRequest {
    // ... existing fields
    private Integer yearsOfExperience;
}

// AdminDoctorController
@PostMapping
public ResponseEntity<?> createDoctor(@RequestBody CreateDoctorRequest request) {
    Doctor doctor = Doctor.builder()
        // ... existing fields
        .yearsOfExperience(request.getYearsOfExperience())
        .build();
    // ...
}
```

**Mobile:**
```kotlin
// dialog_add_doctor.xml - Thêm field
<EditText
    android:id="@+id/etYearsOfExperience"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:hint="Số năm kinh nghiệm"
    android:inputType="number" />
```

**Ước tính:** 0.5 ngày

---

## 🔵 LOW PRIORITY

### 7. Audit Logging

**Backend:**
```java
@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String userId;
    private String userRole;
    private String action; // CREATE, UPDATE, DELETE, LOGIN
    private String entityType; // Patient, Doctor, Appointment
    private Long entityId;
    
    @Column(columnDefinition = "TEXT")
    private String details; // JSON
    
    @CreationTimestamp
    private LocalDateTime timestamp;
    
    private String ipAddress;
}

// AOP Aspect for automatic logging
@Aspect
@Component
public class AuditAspect {
    
    @AfterReturning(
        pointcut = "@annotation(auditable)",
        returning = "result"
    )
    public void logAudit(JoinPoint joinPoint, Auditable auditable, Object result) {
        // Log to audit_logs table
    }
}

// Usage
@PostMapping
@Auditable(action = "CREATE", entityType = "DOCTOR")
public ResponseEntity<?> createDoctor(@RequestBody CreateDoctorRequest request) {
    // ...
}
```

**Ước tính:** 2 ngày

---

## 📋 CHECKLIST PHASE 3

### Medium Priority (Tuần 4-5)
- [ ] Tìm kiếm dịch vụ (1 ngày)
- [ ] In đơn thuốc PDF (2 ngày)
- [ ] Password complexity (1 ngày)
- [ ] Pagination (2 ngày)
- [ ] Admin Room CRUD (1 ngày)
- [ ] Doctor.yearsOfExperience (0.5 ngày)

### Low Priority (Tuần 6)
- [ ] Audit Logging (2 ngày)

**Tổng ước tính:** 9.5 ngày (2 tuần)
