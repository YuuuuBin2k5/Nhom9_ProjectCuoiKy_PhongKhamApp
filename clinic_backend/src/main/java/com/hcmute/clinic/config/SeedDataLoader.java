package com.hcmute.clinic.config;

import com.hcmute.clinic.entity.*;
import com.hcmute.clinic.enums.AppointmentStatus;
import com.hcmute.clinic.enums.BookingType;
import com.hcmute.clinic.enums.UiTemplateType;
import com.hcmute.clinic.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class SeedDataLoader implements ApplicationRunner {

    private final ServiceCategoryRepository serviceCategoryRepository;
    private final ServiceRepository serviceRepository;
    private final ClinicRoomRepository clinicRoomRepository;
    private final DoctorRepository doctorRepository;
    private final TreatmentPlanTemplateRepository templateRepository;
    private final com.hcmute.clinic.repository.TreatmentPlanRepository treatmentPlanRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (serviceCategoryRepository.count() > 0) {
            log.debug("Seed data already exists, skipping");
            return;
        }
        log.info("Seeding initial data...");

        ServiceCategory catGeneral = serviceCategoryRepository.save(ServiceCategory.builder()
                .name("Khám chữa bệnh")
                .description("Các dịch vụ khám và điều trị cơ bản")
                .build());

        ServiceCategory catXRay = serviceCategoryRepository.save(ServiceCategory.builder()
                .name("Chẩn đoán hình ảnh")
                .description("Chụp X-Quang và các dịch vụ chẩn đoán")
                .build());

        ServiceCategory catCosmetic = serviceCategoryRepository.save(ServiceCategory.builder()
                .name("Thẩm mỹ răng")
                .description("Các dịch vụ làm đẹp răng miệng")
                .build());

        ServiceCategory catSurgery = serviceCategoryRepository.save(ServiceCategory.builder()
                .name("Tiểu phẫu")
                .description("Các dịch vụ phẫu thuật nhỏ")
                .build());

        Service svcKham = serviceRepository.save(Service.builder()
                .category(catGeneral)
                .name("Khám tổng quát")
                .description("Khám răng miệng tổng quát, tư vấn sức khỏe răng miệng")
                .price(new BigDecimal("100000"))
                .durationMinutes(15)
                .active(true)
                .build());

        Service svcLayVoi = serviceRepository.save(Service.builder()
                .category(catGeneral)
                .name("Lấy vôi răng")
                .description("Cạo vôi, vệ sinh răng miệng chuyên sâu")
                .price(new BigDecimal("200000"))
                .durationMinutes(30)
                .active(true)
                .uiTemplateType(UiTemplateType.PERIO)
                .build());

        Service svcNhoRang = serviceRepository.save(Service.builder()
                .category(catSurgery)
                .name("Nhổ răng khôn")
                .description("Nhổ răng khôn mọc lệch, nhổ răng khó")
                .price(new BigDecimal("500000"))
                .durationMinutes(45)
                .active(true)
                .uiTemplateType(UiTemplateType.SURGERY)
                .build());

        Service svcXQuang = serviceRepository.save(Service.builder()
                .category(catXRay)
                .name("Chụp X-Quang")
                .description("Chụp X-Quang răng, chẩn đoán hình ảnh")
                .price(new BigDecimal("150000"))
                .durationMinutes(10)
                .active(true)
                .build());

        // Thêm các dịch vụ thẩm mỹ
        Service svcTayTrang = serviceRepository.save(Service.builder()
                .category(catCosmetic)
                .name("Tẩy trắng răng")
                .description("Tẩy trắng răng bằng công nghệ Laser hiện đại")
                .price(new BigDecimal("1500000"))
                .durationMinutes(60)
                .active(true)
                .build());

        Service svcBocRang = serviceRepository.save(Service.builder()
                .category(catCosmetic)
                .name("Bọc răng sứ")
                .description("Bọc răng sứ cao cấp, thẩm mỹ hoàn hảo")
                .price(new BigDecimal("3000000"))
                .durationMinutes(90)
                .active(true)
                .build());

        Service svcTramRang = serviceRepository.save(Service.builder()
                .category(catGeneral)
                .name("Trám răng")
                .description("Trám răng sâu, phục hồi răng bị tổn thương")
                .price(new BigDecimal("300000"))
                .durationMinutes(30)
                .active(true)
                .build());

        Service svcDieuTri = serviceRepository.save(Service.builder()
                .category(catGeneral)
                .name("Điều trị tủy")
                .description("Điều trị tủy răng, chữa răng sâu nặng")
                .price(new BigDecimal("800000"))
                .durationMinutes(60)
                .active(true)
                .build());

        ClinicRoom room1 = clinicRoomRepository.save(ClinicRoom.builder()
                .name("Phòng 1")
                .description("Phòng khám chính - Tầng 1")
                .capacity(1)
                .build());

        ClinicRoom roomXRay = clinicRoomRepository.save(ClinicRoom.builder()
                .name("Phòng X-Quang")
                .description("Chụp X-Quang - Tầng 1")
                .capacity(1)
                .build());

        Doctor doctor = Doctor.builder()
                .email("doctor@gmail.com")
                .passwordHash(passwordEncoder.encode("123456"))
                .firstName("Nguyễn")
                .lastName("Văn Bác")
                .isActive(true)
                .clinicRoom(room1)
                .specialization("Nha khoa tổng quát")
                .licenseNumber("BS-001")
                .experienceYears(10)
                .build();
        doctorRepository.save(doctor);

        TreatmentPlanTemplate tmplLayVoi = TreatmentPlanTemplate.builder()
                .name("Lấy vôi răng")
                .description("Phác đồ cạo vôi, vệ sinh răng miệng")
                .active(true)
                .build();
        tmplLayVoi = templateRepository.save(tmplLayVoi);
        tmplLayVoi.setSteps(new ArrayList<>(List.of(
                TreatmentPlanTemplateStep.builder().template(tmplLayVoi).service(svcLayVoi).clinicRoom(room1).sequenceOrder(1).build()
        )));
        templateRepository.save(tmplLayVoi);

        TreatmentPlanTemplate tmplNieng = TreatmentPlanTemplate.builder()
                .name("Niềng răng")
                .description("Phác đồ chỉnh nha")
                .active(true)
                .build();
        tmplNieng = templateRepository.save(tmplNieng);
        tmplNieng.setSteps(new ArrayList<>(List.of(
                TreatmentPlanTemplateStep.builder().template(tmplNieng).service(svcXQuang).clinicRoom(roomXRay).sequenceOrder(1).build(),
                TreatmentPlanTemplateStep.builder().template(tmplNieng).service(svcLayVoi).clinicRoom(room1).sequenceOrder(2).build(),
                TreatmentPlanTemplateStep.builder().template(tmplNieng).service(svcKham).clinicRoom(room1).sequenceOrder(3).build()
        )));
        templateRepository.save(tmplNieng);

        TreatmentPlanTemplate tmplNhoRang = TreatmentPlanTemplate.builder()
                .name("Nhổ răng khôn")
                .description("Phác đồ nhổ răng khôn mọc lệch")
                .active(true)
                .build();
        tmplNhoRang = templateRepository.save(tmplNhoRang);
        tmplNhoRang.setSteps(new ArrayList<>(List.of(
                TreatmentPlanTemplateStep.builder().template(tmplNhoRang).service(svcXQuang).clinicRoom(roomXRay).sequenceOrder(1).build(),
                TreatmentPlanTemplateStep.builder().template(tmplNhoRang).service(svcNhoRang).clinicRoom(room1).sequenceOrder(2).build()
        )));
        templateRepository.save(tmplNhoRang);

        // Seed 1 test patient + appointment for today (if no patients exist)
        if (patientRepository.count() == 0) {
            Patient testPatient = Patient.builder()
                    .email("patient@gmail.com")
                    .passwordHash(passwordEncoder.encode("123456"))
                    .firstName("Test")
                    .lastName("Patient")
                    .isActive(true)
                    .build();
            testPatient = patientRepository.save(testPatient);
            testPatient.setQrCodeData("patient:" + testPatient.getId());
            patientRepository.save(testPatient);

            LocalDateTime now = LocalDateTime.now();
            Appointment apt = Appointment.builder()
                    .patient(testPatient)
                    .doctor(doctor)
                    .service(svcKham)
                    .appointmentDatetime(now)
                    .bookingType(BookingType.WALK_IN)
                    .status(AppointmentStatus.SCHEDULED)
                    .build();
            appointmentRepository.save(apt);

            com.hcmute.clinic.entity.TreatmentPlan plan = com.hcmute.clinic.entity.TreatmentPlan.builder()
                    .patient(testPatient)
                    .templateId(tmplLayVoi.getId())
                    .status(com.hcmute.clinic.enums.TreatmentPlanStatus.IN_PROGRESS)
                    .build();
            plan = treatmentPlanRepository.save(plan);
            var step1 = com.hcmute.clinic.entity.TreatmentPlanStep.builder()
                    .plan(plan)
                    .service(svcLayVoi)
                    .clinicRoom(room1)
                    .sequenceOrder(1)
                    .status(com.hcmute.clinic.enums.StepStatus.PENDING)
                    .build();
            plan.setSteps(new ArrayList<>(List.of(step1)));
            treatmentPlanRepository.save(plan);
        }

        // Backfill qrCodeData for existing patients
        patientRepository.findAll().stream()
                .filter(p -> p.getQrCodeData() == null || p.getQrCodeData().isBlank())
                .forEach(p -> {
                    p.setQrCodeData("patient:" + p.getId());
                    patientRepository.save(p);
                });

        log.info("Seed data completed");
    }
}
