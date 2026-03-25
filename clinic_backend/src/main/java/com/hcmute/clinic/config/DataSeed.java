package com.hcmute.clinic.config;

import com.hcmute.clinic.entity.*;
import com.hcmute.clinic.enums.*;
import com.hcmute.clinic.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Order(10)
@RequiredArgsConstructor
@Slf4j
public class DataSeed implements ApplicationRunner {

    private final ServiceCategoryRepository categoryRepository;
    private final ServiceRepository serviceRepository;
    private final ServiceImageRepository serviceImageRepository;
    private final ClinicRoomRepository roomRepository;
    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final CheckInQueueRepository queueRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final NotificationRepository notificationRepository;
    private final OtpChallengeRepository otpChallengeRepository;
    private final ScanLogRepository scanLogRepository;
    private final TreatmentPlanRepository treatmentPlanRepository;
    private final TreatmentPlanStepRepository treatmentPlanStepRepository;
    private final TreatmentPlanTemplateRepository treatmentPlanTemplateRepository;
    private final TreatmentPlanTemplateStepRepository treatmentPlanTemplateStepRepository;
    private final PasswordEncoder passwordEncoder;
    private final DataSource dataSource;

    @Override
    public void run(ApplicationArguments args) {
        clearAll();
        seed();
        log.info("DataSeed completed. Starting with fresh database state.");
    }

    public void clearAll() {
        log.info("Truncating all tables via JDBC...");
        String sql = "TRUNCATE TABLE " +
            "prescriptions, " +
            "medical_records, " +
            "check_in_queue, " +
            "appointments, " +
            "step_images, " +
            "treatment_plan_steps, " +
            "treatment_plans, " +
            "treatment_plan_template_steps, " +
            "treatment_plan_templates, " +
            "otp_challenges, " +
            "scan_logs, " +
            "notifications, " +
            "service_images, " +
            "services, " +
            "service_categories, " +
            "doctors, " +
            "clinic_rooms, " +
            "admins, " +
            "patients " +
            "RESTART IDENTITY CASCADE";
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(true);
            conn.createStatement().execute(sql);
            log.info("All tables truncated.");
        } catch (Exception e) {
            log.error("TRUNCATE failed: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public void seed() {
        log.info("Starting Fresh DataSeed...");
        String defaultPass = passwordEncoder.encode("123456");

        // 1. Categories
        ServiceCategory catDiag = categoryRepository.save(new ServiceCategory(null, "Khám & Chẩn đoán", "Khám tổng quát và chẩn đoán hình ảnh"));
        ServiceCategory catGen = categoryRepository.save(new ServiceCategory(null, "Nha khoa Tổng quát", "Các dịch vụ điều trị nha khoa cơ bản"));
        ServiceCategory catSurg = categoryRepository.save(new ServiceCategory(null, "Tiểu phẫu", "Các thủ thuật nhổ răng và phẫu thuật nhỏ"));
        ServiceCategory catCosm = categoryRepository.save(new ServiceCategory(null, "Thẩm mỹ", "Các dịch vụ làm đẹp răng miệng"));
        ServiceCategory catOrtho = categoryRepository.save(new ServiceCategory(null, "Chỉnh nha", "Niềng răng và điều chỉnh khớp cắn"));

        // 2. Services & Images
        Service svcConsult = addService(catDiag, "Khám và tư vấn răng miệng", "Khám tổng quát và lập kế hoạch.", 100000, 20, UiTemplateType.GENERAL, 
                "khamvatuvan_anh1.png", "khamvatuvan_anh2.png", "khamvatuvan_anh3.png", "khamvatuvan_anh4.png");
        Service svcXray = addService(catDiag, "Chụp X-quang răng", "Chụp phim kỹ thuật số panorama.", 200000, 15, UiTemplateType.GENERAL, 
                "chupxquang_anh1.png", "chupxquang_anh2.png", "chupxquang_anh3.png", "chupxquang_anh4.png");
        
        Service svcScale = addService(catGen, "Lấy cao răng & đánh bóng", "Vệ sinh răng miệng chuyên sâu.", 250000, 30, UiTemplateType.PERIO, 
                "laycaorangdanhbong_anh1.png", "laycaorangdanhbong_anh2.png", "laycaorangdanhbong_anh3.png", "laycaorangdanhbong_anh4.png");
        Service svcFill = addService(catGen, "Trám răng sâu", "Phục hồi răng sâu bằng composite.", 300000, 30, UiTemplateType.GENERAL, 
                "tramrangsau_anh1.png", "tramrangsau_anh2.png", "tramrangsau_anh3.png", "tramrangsau_anh4.png");
        addService(catGen, "Điều trị tủy răng", "Nội nha lấy tủy và hàn ống tủy.", 1500000, 60, UiTemplateType.GENERAL, 
                "dieutrituyrang_anh1.png", "dieutrituyrang_anh2.png", "dieutrituyrang_anh3.png", "dieutrituyrang_anh4.png");

        addService(catSurg, "Nhổ răng thường", "Nhổ răng lung lay hoặc hư tổn.", 300000, 20, UiTemplateType.SURGERY, 
                "nhorangthuong_anh1.png", "nhorangthuong_anh2.png", "nhorangthuong_anh3.png", "nhorangthuong_anh4.png");
        Service svcWisdom = addService(catSurg, "Nhổ răng khôn", "Phẫu thuật nhổ răng khôn mọc lệch.", 2000000, 45, UiTemplateType.SURGERY, 
                "nhorangkhon_anh1.png", "nhorangkhon_anh2.png", "nhorangkhon_anh3.png", "nhorangkhon_anh4.png");

        Service svcWhite = addService(catCosm, "Tẩy trắng răng", "Làm trắng răng Laser.", 2500000, 60, UiTemplateType.GENERAL, 
                "taytrangrang_anh1.png", "taytrangrang_anh2.png", "taytrangrang_anh3.png", "taytrangrang_anh4.png");
        addService(catCosm, "Bọc răng sứ thẩm mỹ", "Phục hình răng bằng sứ cao cấp.", 5000000, 90, UiTemplateType.GENERAL, 
                "bocrangxu_anh1.png", "bocrangxu_anh2.png", "bocrangxu_anh3.png", "bocrangxu_anh4.png");

        Service svcBraces = addService(catOrtho, "Niềng răng", "Chỉnh nha mắc cài toàn hàm.", 30000000, 60, UiTemplateType.ORTHO, 
                "niengrang_anh1.png", "niengrang_anh2.png", "niengrang_anh3.png", "niengrang_anh4.png");

        // 3. Rooms & Doctors
        Doctor d1 = createRoomAndDoc("Phòng khám 01", "Tầng G — Sảnh chính", "Nguyễn Văn A", "doc01@gmail.com", "Khám & Chẩn đoán", defaultPass);
        Doctor d2 = createRoomAndDoc("Phòng khám 02", "Tầng 1 — Hành lang trái", "Trần Thị B", "doc02@gmail.com", "Nha khoa Tổng quát", defaultPass);
        Doctor d3 = createRoomAndDoc("Phòng khám 03", "Tầng 1 — Hành lang phải", "Lê Văn C", "doc03@gmail.com", "Chỉnh nha", defaultPass);
        Doctor dx = createRoomAndDoc("Phòng X-quang", "Tầng 1 — Khu kỹ thuật", "Phạm Văn D", "doc_xray@gmail.com", "Chẩn đoán hình ảnh", defaultPass);
        Doctor ds = createRoomAndDoc("Phòng tiểu phẫu", "Tầng 2 — Khu Phẫu thuật", "Hoàng Thị E", "doc_surg@gmail.com", "Tiểu phẫu", defaultPass);
        Doctor dt1 = createRoomAndDoc("Phòng điều trị 01", "Tầng 2 — Khu Điều trị", "Ngô Văn F", "doc_treat01@gmail.com", "Nha khoa Tổng quát", defaultPass);
        Doctor dt2 = createRoomAndDoc("Phòng điều trị 02", "Tầng 2 — Khu Điều trị", "Đỗ Thị G", "doc_treat02@gmail.com", "Thẩm mỹ", defaultPass);

        // 4. Admin
        adminRepository.save(Admin.builder().email("admin@gmail.com").passwordHash(defaultPass).firstName("Admin").lastName("System").isActive(true).build());

        // 5. Patients & Queues
        Patient p1 = createPatient("Nguyễn Văn An", "patient01@gmail.com", "0911111111", defaultPass);
        Patient p2 = createPatient("Trần Văn Bình", "patient02@gmail.com", "0922222222", defaultPass);
        Patient p3 = createPatient("Lê Thị Chi", "patient03@gmail.com", "0933333333", defaultPass);

        // 6. Templates
        seedTemplates(svcConsult, svcXray, svcScale, svcFill, svcWisdom, svcWhite, svcBraces, d1, dx, ds, dt2);

        // Add some queue entries
        addToQueue(p1, d1, svcConsult, 1);
        addToQueue(p2, d1, svcConsult, 2);
        addToQueue(p3, d2, svcScale, 1);

        log.info("Refined DataSeed completed successfully.");
    }

    private void seedTemplates(Service svcConsult, Service svcXray, Service svcScale, Service svcFill, Service svcWisdom, Service svcWhite, Service svcBraces, Doctor d1, Doctor dx, Doctor ds, Doctor dt2) {
        log.info("Seeding Treatment Plan Templates...");

        // 1. Nhổ răng khôn
        TreatmentPlanTemplate tplWisdom = treatmentPlanTemplateRepository.save(TreatmentPlanTemplate.builder()
                .name("Phác đồ Nhổ răng khôn")
                .description("Quy trình chuẩn cho việc nhổ răng khôn mọc lệch/ngầm.")
                .active(true).build());
        
        treatmentPlanTemplateStepRepository.save(new TreatmentPlanTemplateStep(null, tplWisdom, svcConsult, d1.getClinicRoom(), 1));
        treatmentPlanTemplateStepRepository.save(new TreatmentPlanTemplateStep(null, tplWisdom, svcXray, dx.getClinicRoom(), 2));
        treatmentPlanTemplateStepRepository.save(new TreatmentPlanTemplateStep(null, tplWisdom, svcWisdom, ds.getClinicRoom(), 3));

        // 2. Niềng răng Chỉnh nha
        TreatmentPlanTemplate tplBraces = treatmentPlanTemplateRepository.save(TreatmentPlanTemplate.builder()
                .name("Phác đồ Niềng răng Chỉnh nha")
                .description("Quy trình chỉnh nha mắc cài toàn hàm.")
                .active(true).build());

        treatmentPlanTemplateStepRepository.save(new TreatmentPlanTemplateStep(null, tplBraces, svcConsult, d1.getClinicRoom(), 1));
        treatmentPlanTemplateStepRepository.save(new TreatmentPlanTemplateStep(null, tplBraces, svcXray, dx.getClinicRoom(), 2));
        treatmentPlanTemplateStepRepository.save(new TreatmentPlanTemplateStep(null, tplBraces, svcScale, d1.getClinicRoom(), 3));
        treatmentPlanTemplateStepRepository.save(new TreatmentPlanTemplateStep(null, tplBraces, svcBraces, d1.getClinicRoom(), 4));

        // 3. Tẩy trắng răng
        TreatmentPlanTemplate tplWhite = treatmentPlanTemplateRepository.save(TreatmentPlanTemplate.builder()
                .name("Phác đồ Tẩy trắng răng")
                .description("Làm trắng răng Laser cao cấp.")
                .active(true).build());

        treatmentPlanTemplateStepRepository.save(new TreatmentPlanTemplateStep(null, tplWhite, svcConsult, d1.getClinicRoom(), 1));
        treatmentPlanTemplateStepRepository.save(new TreatmentPlanTemplateStep(null, tplWhite, svcScale, d1.getClinicRoom(), 2));
        treatmentPlanTemplateStepRepository.save(new TreatmentPlanTemplateStep(null, tplWhite, svcWhite, dt2.getClinicRoom(), 3));

        // 4. Trám răng sâu
        TreatmentPlanTemplate tplFill = treatmentPlanTemplateRepository.save(TreatmentPlanTemplate.builder()
                .name("Phác đồ Trám răng thẩm mỹ")
                .description("Quy trình trám phục hồi răng sâu.")
                .active(true).build());

        treatmentPlanTemplateStepRepository.save(new TreatmentPlanTemplateStep(null, tplFill, svcConsult, d1.getClinicRoom(), 1));
        treatmentPlanTemplateStepRepository.save(new TreatmentPlanTemplateStep(null, tplFill, svcFill, d1.getClinicRoom(), 2));
    }

    private Service addService(ServiceCategory cat, String name, String desc, double price, int duration, UiTemplateType type, String... images) {
        Service svc = serviceRepository.save(Service.builder()
                .category(cat).name(name).description(desc).price(BigDecimal.valueOf(price))
                .durationMinutes(duration).uiTemplateType(type).active(true).build());
        for (String url : images) {
            serviceImageRepository.save(ServiceImage.builder().service(svc).imageUrl(url).build());
        }
        return svc;
    }

    private Doctor createRoomAndDoc(String rName, String rDesc, String docName, String email, String spec, String pass) {
        ClinicRoom room = roomRepository.save(new ClinicRoom(null, rName, rDesc, 1));
        String[] parts = docName.split(" ");
        String lastName = parts[parts.length - 1];
        String firstName = docName.substring(0, docName.length() - lastName.length()).trim();
        return doctorRepository.save(Doctor.builder()
                .email(email).passwordHash(pass).firstName(firstName).lastName(lastName)
                .specialization(spec).licenseNumber("BS-" + email.split("@")[0].toUpperCase())
                .clinicRoom(room).experienceYears(5).isActive(true).build());
    }

    private Patient createPatient(String name, String email, String phone, String pass) {
        String[] parts = name.split(" ");
        String lastName = parts[parts.length - 1];
        String firstName = name.substring(0, name.length() - lastName.length()).trim();
        Patient p = patientRepository.save(Patient.builder()
                .email(email).passwordHash(pass).firstName(firstName).lastName(lastName)
                .phone(phone).isActive(true).build());
        p.setQrCodeData("patient:" + p.getId());
        return patientRepository.save(p);
    }

    private void addToQueue(Patient p, Doctor d, Service s, int num) {
        Appointment app = appointmentRepository.save(Appointment.builder()
                .patient(p).doctor(d).service(s)
                .appointmentDatetime(LocalDateTime.now())
                .status(AppointmentStatus.SCHEDULED).bookingType(BookingType.WALK_IN).build());
        
        queueRepository.save(CheckInQueue.builder()
                .appointment(app).clinicRoom(d.getClinicRoom())
                .queueNumber(num).checkInTime(LocalDateTime.now())
                .status(QueueStatus.WAITING).priorityLevel(0).build());
    }
}
