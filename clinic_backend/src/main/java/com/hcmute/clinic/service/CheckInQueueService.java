package com.hcmute.clinic.service;

import com.hcmute.clinic.dto.CheckInMyStatusResponse;
import com.hcmute.clinic.entity.Appointment;
import com.hcmute.clinic.entity.CheckInQueue;
import com.hcmute.clinic.entity.ClinicRoom;
import com.hcmute.clinic.entity.Notification;
import com.hcmute.clinic.entity.Patient;
import com.hcmute.clinic.enums.QueueStatus;
import com.hcmute.clinic.repository.AppointmentRepository;
import com.hcmute.clinic.repository.CheckInQueueRepository;
import com.hcmute.clinic.repository.ClinicRoomRepository;
import com.hcmute.clinic.repository.NotificationRepository;
import com.hcmute.clinic.repository.PatientRepository;
import com.hcmute.clinic.security.JwtService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckInQueueService {

    private static final Pattern QR_PATTERN = Pattern.compile("^patient:(\\d+)$");

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final CheckInQueueRepository checkInQueueRepository;
    private final ClinicRoomRepository clinicRoomRepository;
    private final JwtService jwtService;
    private final NotificationRepository notificationRepository;
    private final QueueEventService queueEventService;

    @Transactional
    public CheckInResult processScan(String qrData) {
        if (qrData == null || qrData.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "QR data is required");
        }
        qrData = qrData.trim();

        long patientId = resolvePatientId(qrData);

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mã không hợp lệ"));
        if (!patient.isActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tài khoản không hoạt động");
        }

        List<Appointment> todayAppointments = appointmentRepository.findTodayByPatientId(patientId);
        if (todayAppointments.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không có lịch hẹn hôm nay. Vui lòng gặp Lễ tân.");
        }

        Appointment appointment = todayAppointments.get(0);
        var existing = checkInQueueRepository.findByAppointmentId(appointment.getId());
        if (existing.isPresent()) {
            CheckInQueue q = existing.get();
            return CheckInResult.alreadyCheckedIn(q.getQueueNumber(), getRoomName(q));
        }

        ClinicRoom room = appointment.getDoctor() != null ? appointment.getDoctor().getClinicRoom() : null;
        if (room == null) {
            room = clinicRoomRepository.findAll().stream().findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Chưa cấu hình phòng khám. Liên hệ quản trị."));
        }

        int nextNumber = getNextQueueNumber(room.getId());
        CheckInQueue queue = CheckInQueue.builder()
                .appointment(appointment)
                .clinicRoom(room)
                .queueNumber(nextNumber)
                .checkInTime(LocalDateTime.now())
                .status(QueueStatus.WAITING)
                .priorityLevel(0)
                .build();
        checkInQueueRepository.save(queue);

        Patient p = appointment.getPatient();
        if (p != null) {
            Notification notif = Notification.builder()
                    .patient(p)
                    .title("Check-in thành công")
                    .message("Bạn đã check-in thành công. Số thứ tự của bạn là " + nextNumber + ". " + (room.getName() != null ? "Phòng: " + room.getName() : ""))
                    .type("CHECK_IN")
                    .build();
            notificationRepository.save(notif);
        }

        try {
            queueEventService.broadcastQueueUpdated(room.getId());
        } catch (Exception e) {
            log.warn("Broadcast failed: {}", e.getMessage());
        }
        return CheckInResult.success(nextNumber, room.getName(), room.getDescription() != null ? room.getDescription() : "");
    }

    private long resolvePatientId(String qrData) {
        if (qrData.startsWith("eyJ")) {
            try {
                Claims claims = jwtService.parseClaims(qrData);
                String purpose = claims.get("purpose", String.class);
                if (!"QR_CHECKIN".equals(purpose)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã QR không hợp lệ");
                }
                return Long.parseLong(claims.getSubject());
            } catch (Exception e) {
                log.warn("Invalid QR JWT: {}", e.getMessage());
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã QR hết hạn hoặc không hợp lệ");
            }
        }
        Matcher m = QR_PATTERN.matcher(qrData);
        if (!m.matches()) {
            log.warn("Invalid QR format: {}", qrData);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Mã không hợp lệ");
        }
        return Long.parseLong(m.group(1));
    }

    private int getNextQueueNumber(Long roomId) {
        LocalDate today = LocalDate.now();
        var todayQueues = checkInQueueRepository.findByClinicRoomIdAndStatusInAndCheckInTimeBetweenOrderByPriorityLevelDescQueueNumberAsc(
                roomId,
                List.of(QueueStatus.WAITING, QueueStatus.IN_PROGRESS, QueueStatus.PAUSED_FOR_TEST, QueueStatus.RETURNED_PRIORITY, QueueStatus.COMPLETED),
                today.atStartOfDay(),
                today.plusDays(1).atStartOfDay());
        int max = todayQueues.stream()
                .mapToInt(CheckInQueue::getQueueNumber)
                .max()
                .orElse(0);
        return max + 1;
    }

    private String getRoomName(CheckInQueue q) {
        return q.getClinicRoom() != null ? q.getClinicRoom().getName() : "";
    }

    public record CheckInResult(boolean success, int queueNumber, String roomName, String roomLocation, String message, boolean alreadyCheckedIn) {
        static CheckInResult success(int num, String room, String location) {
            return new CheckInResult(true, num, room, location, "Check-in thành công", false);
        }
        static CheckInResult alreadyCheckedIn(int num, String room) {
            return new CheckInResult(true, num, room, "", "Bạn đã check-in trước đó", true);
        }
    }

    @Transactional(readOnly = true)
    public CheckInMyStatusResponse getMyStatusToday(long patientId) {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();
        List<CheckInQueue> rows = checkInQueueRepository.findTodayForPatient(patientId, start, end);
        if (rows.isEmpty()) {
            return CheckInMyStatusResponse.builder()
                    .checkedIn(false)
                    .queueNumber(null)
                    .roomName(null)
                    .roomLocation(null)
                    .status(null)
                    .statusLabel(null)
                    .hint("Đưa mã QR qua máy quét tại quầy tiếp nhận khi đến phòng khám.")
                    .build();
        }
        CheckInQueue q = rows.get(0);
        ClinicRoom room = q.getClinicRoom();
        String roomName = room != null ? room.getName() : "";
        String roomLoc = room != null && room.getDescription() != null ? room.getDescription() : "";
        QueueStatus st = q.getStatus() != null ? q.getStatus() : QueueStatus.WAITING;
        return CheckInMyStatusResponse.builder()
                .checkedIn(true)
                .queueNumber(q.getQueueNumber())
                .roomName(roomName)
                .roomLocation(roomLoc)
                .status(st.name())
                .statusLabel(patientQueueStatusLabel(st))
                .hint(patientQueueHint(st))
                .build();
    }

    private static String patientQueueStatusLabel(QueueStatus st) {
        return switch (st) {
            case WAITING -> "Đang chờ đến lượt";
            case IN_PROGRESS -> "Đang trong phòng khám";
            case PAUSED_FOR_TEST -> "Đang chụp X-Quang / xét nghiệm";
            case RETURNED_PRIORITY -> "Ưu tiên — vui lòng vào phòng";
            case COMPLETED -> "Đã hoàn thành tiếp nhận";
            case SKIPPED -> "Đã bỏ qua";
        };
    }

    private static String patientQueueHint(QueueStatus st) {
        return switch (st) {
            case WAITING, RETURNED_PRIORITY -> "Theo dõi màn hình hoặc chờ nhân viên gọi số.";
            case IN_PROGRESS -> "Vui lòng làm theo hướng dẫn trong phòng.";
            case PAUSED_FOR_TEST -> "Sau khi xong, quay lại phòng khám theo hướng dẫn.";
            case COMPLETED -> "Cảm ơn bạn đã check-in.";
            case SKIPPED -> "Vui lòng liên hệ lễ tân nếu cần hỗ trợ.";
        };
    }

    @Transactional(readOnly = true)
    public List<QueueItemDto> getRoomQueue(Long roomId) {
        var queues = checkInQueueRepository.findTodayByClinicRoomId(roomId);
        return queues.stream()
                .map(q -> new QueueItemDto(
                        q.getId(),
                        q.getQueueNumber(),
                        q.getStatus().name(),
                        q.getPriorityLevel() != null ? q.getPriorityLevel() : 0,
                        q.getAppointment() != null && q.getAppointment().getPatient() != null
                                ? (q.getAppointment().getPatient().getLastName() + " " + q.getAppointment().getPatient().getFirstName()).trim()
                                : "",
                        q.getAppointment() != null && q.getAppointment().getPatient() != null ? q.getAppointment().getPatient().getId() : null
                ))
                .toList();
    }

    @Transactional
    public void updateStatus(Long queueId, String statusStr) {
        CheckInQueue q = checkInQueueRepository.findById(queueId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy hàng đợi"));
        QueueStatus status = QueueStatus.valueOf(statusStr.toUpperCase());
        q.setStatus(status);
        checkInQueueRepository.save(q);
        broadcastRoom(q.getClinicRoom());
    }

    @Transactional
    public void callToRoom(Long queueId) {
        CheckInQueue q = checkInQueueRepository.findById(queueId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy hàng đợi"));
        q.setStatus(QueueStatus.IN_PROGRESS);
        checkInQueueRepository.save(q);
        ClinicRoom r = q.getClinicRoom();
        if (r != null) {
            broadcastRoom(r);
            queueEventService.broadcastQueueCalled(r.getId(), q.getQueueNumber(), r.getName());
        }
    }

    @Transactional
    public void transferToXRay(Long queueId, Long xRayRoomId) {
        CheckInQueue q = checkInQueueRepository.findById(queueId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy hàng đợi"));
        ClinicRoom oldRoom = q.getClinicRoom();
        ClinicRoom xrayRoom = xRayRoomId != null
                ? clinicRoomRepository.findById(xRayRoomId).orElse(null)
                : clinicRoomRepository.findAll().stream()
                        .filter(r -> r.getName() != null && r.getName().toLowerCase().contains("x-quang"))
                        .findFirst()
                        .orElse(clinicRoomRepository.findAll().stream().findFirst().orElse(null));
        if (xrayRoom == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chưa cấu hình phòng X-Quang");
        }
        q.setStatus(QueueStatus.PAUSED_FOR_TEST);
        q.setClinicRoom(xrayRoom);
        checkInQueueRepository.save(q);
        if (oldRoom != null) queueEventService.broadcastQueueUpdated(oldRoom.getId());
        queueEventService.broadcastQueueUpdated(xrayRoom.getId());
    }

    @Transactional
    public void completeXRay(Long queueId) {
        CheckInQueue q = checkInQueueRepository.findById(queueId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy hàng đợi"));
        q.setStatus(QueueStatus.RETURNED_PRIORITY);
        q.setPriorityLevel((q.getPriorityLevel() != null ? q.getPriorityLevel() : 0) + 10);
        var examRoom = q.getAppointment() != null && q.getAppointment().getDoctor() != null
                ? q.getAppointment().getDoctor().getClinicRoom()
                : null;
        if (examRoom != null) {
            q.setClinicRoom(examRoom);
        }
        checkInQueueRepository.save(q);
        if (examRoom != null) {
            String pn = q.getAppointment() != null && q.getAppointment().getPatient() != null
                    ? (q.getAppointment().getPatient().getLastName() + " " + q.getAppointment().getPatient().getFirstName()).trim()
                    : "";
            queueEventService.broadcastPriorityReturned(examRoom.getId(), q.getId(), pn);
            queueEventService.broadcastQueueUpdated(examRoom.getId());
        }
    }

    private void broadcastRoom(ClinicRoom r) {
        try {
            if (r != null) queueEventService.broadcastQueueUpdated(r.getId());
        } catch (Exception e) {
            log.warn("Broadcast failed: {}", e.getMessage());
        }
    }

    public record QueueItemDto(Long id, Integer queueNumber, String status, Integer priorityLevel, String patientName, Long patientId) {}

    @Transactional
    public CheckInResult processSelfScan(long authenticatedPatientId, String qrData) {
        if (qrData == null || qrData.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã QR hoặc mã số không được để trống");
        }
        qrData = qrData.trim();

        // Parse QR data: format "CHECKIN:appointmentId:timestamp" or "CHECKIN:patientId"
        Long appointmentId = null;
        Long targetPatientId = null;

        if (qrData.startsWith("CHECKIN:")) {
            String[] parts = qrData.split(":");
            if (parts.length >= 2) {
                try {
                    // Try to parse as appointmentId first
                    appointmentId = Long.parseLong(parts[1]);
                } catch (NumberFormatException e) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã không hợp lệ");
                }
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã không hợp lệ");
            }
        } else {
            // Try direct code input (just numbers)
            try {
                appointmentId = Long.parseLong(qrData);
            } catch (NumberFormatException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã số không hợp lệ");
            }
        }

        // Find appointment
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy lịch hẹn"));

        // Verify this appointment belongs to the authenticated patient
        if (appointment.getPatient() == null || appointment.getPatient().getId() != authenticatedPatientId) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Mã này không thuộc về bạn");
        }

        // Check if appointment is today
        LocalDate today = LocalDate.now();
        LocalDate appointmentDate = appointment.getAppointmentDatetime().toLocalDate();
        if (!appointmentDate.equals(today)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lịch hẹn không phải hôm nay");
        }

        Patient patient = appointment.getPatient();
        if (!patient.isActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tài khoản không hoạt động");
        }

        // Check if already checked in
        var existing = checkInQueueRepository.findByAppointmentId(appointment.getId());
        if (existing.isPresent()) {
            CheckInQueue q = existing.get();
            return CheckInResult.alreadyCheckedIn(q.getQueueNumber(), getRoomName(q));
        }

        // Create queue entry
        ClinicRoom room = appointment.getDoctor() != null ? appointment.getDoctor().getClinicRoom() : null;
        if (room == null) {
            room = clinicRoomRepository.findAll().stream().findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Chưa cấu hình phòng khám"));
        }

        int nextNumber = getNextQueueNumber(room.getId());
        CheckInQueue queue = CheckInQueue.builder()
                .appointment(appointment)
                .clinicRoom(room)
                .queueNumber(nextNumber)
                .checkInTime(LocalDateTime.now())
                .status(QueueStatus.WAITING)
                .priorityLevel(0)
                .build();
        checkInQueueRepository.save(queue);

        // Send notification
        Notification notif = Notification.builder()
                .patient(patient)
                .title("Check-in thành công")
                .message("Bạn đã check-in thành công. Số thứ tự: " + nextNumber + ". Phòng: " + room.getName())
                .type("CHECK_IN")
                .build();
        notificationRepository.save(notif);

        try {
            queueEventService.broadcastQueueUpdated(room.getId());
        } catch (Exception e) {
            log.warn("Broadcast failed: {}", e.getMessage());
        }

        return CheckInResult.success(nextNumber, room.getName(), room.getDescription() != null ? room.getDescription() : "");
    }

    @Transactional
    public com.hcmute.clinic.dto.GenerateCheckInQRResponse generateCheckInQR(com.hcmute.clinic.dto.GenerateCheckInQRRequest request) {
        Appointment appointment = null;
        Patient patient = null;

        // Find appointment by ID
        if (request.getAppointmentId() != null) {
            appointment = appointmentRepository.findById(request.getAppointmentId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy lịch hẹn"));
            patient = appointment.getPatient();
        } 
        // Find patient by ID
        else if (request.getPatientId() != null) {
            patient = patientRepository.findById(request.getPatientId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bệnh nhân"));
            
            // Find today's appointment for this patient
            List<Appointment> todayAppointments = appointmentRepository.findTodayByPatientId(patient.getId());
            if (todayAppointments.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bệnh nhân không có lịch hẹn hôm nay");
            }
            appointment = todayAppointments.get(0);
        }
        // Find patient by phone
        else if (request.getPatientPhone() != null && !request.getPatientPhone().isBlank()) {
            patient = patientRepository.findByPhone(request.getPatientPhone())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bệnh nhân với số điện thoại này"));
            
            List<Appointment> todayAppointments = appointmentRepository.findTodayByPatientId(patient.getId());
            if (todayAppointments.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bệnh nhân không có lịch hẹn hôm nay");
            }
            appointment = todayAppointments.get(0);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cần cung cấp appointmentId, patientId hoặc patientPhone");
        }

        // Generate QR data
        String qrData = "CHECKIN:" + appointment.getId();
        String displayCode = String.valueOf(appointment.getId());
        String patientName = (patient.getLastName() + " " + patient.getFirstName()).trim();

        return com.hcmute.clinic.dto.GenerateCheckInQRResponse.builder()
                .qrData(qrData)
                .displayCode(displayCode)
                .patientName(patientName)
                .appointmentId(appointment.getId())
                .expiresAt(appointment.getAppointmentDatetime().toLocalDate().plusDays(1).toString())
                .build();
    }
}
