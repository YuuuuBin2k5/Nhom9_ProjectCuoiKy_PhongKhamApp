package com.hcmute.clinic.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hcmute.clinic.dto.ChatMessageDto;
import com.hcmute.clinic.entity.Doctor;
import com.hcmute.clinic.entity.DoctorPatientMessage;
import com.hcmute.clinic.entity.Patient;
import com.hcmute.clinic.repository.DoctorPatientMessageRepository;
import com.hcmute.clinic.repository.DoctorRepository;
import com.hcmute.clinic.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private static final DateTimeFormatter ISO_FMT = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    private final DoctorPatientMessageRepository messageRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final ChatWebSocketRegistry webSocketRegistry;
    private final ObjectMapper objectMapper;

    public static String roomKey(Long patientId, Long doctorId) {
        return patientId + "_" + doctorId;
    }

    @Transactional(readOnly = true)
    public List<ChatMessageDto> listMessages(Long patientId, Long doctorId) {
        return messageRepository.findByPatient_IdAndDoctor_IdOrderByCreatedAtAsc(patientId, doctorId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ChatMessageDto sendFromPatient(Long patientId, Long doctorId, String content) {
        if (content == null || content.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nội dung tin nhắn không được để trống");
        }
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bệnh nhân"));
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bác sĩ"));
        return saveAndBroadcast(patient, doctor, true, content.trim());
    }

    @Transactional
    public ChatMessageDto sendFromDoctor(Long doctorId, Long patientId, String content) {
        if (content == null || content.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nội dung tin nhắn không được để trống");
        }
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bác sĩ"));
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bệnh nhân"));
        return saveAndBroadcast(patient, doctor, false, content.trim());
    }

    private ChatMessageDto saveAndBroadcast(Patient patient, Doctor doctor, boolean fromPatient, String content) {
        DoctorPatientMessage m = DoctorPatientMessage.builder()
                .patient(patient)
                .doctor(doctor)
                .fromPatient(fromPatient)
                .content(content)
                .build();
        m = messageRepository.save(m);
        ChatMessageDto dto = toDto(m);
        try {
            ObjectNode root = objectMapper.createObjectNode();
            root.put("type", "CHAT_MESSAGE");
            root.set("payload", objectMapper.valueToTree(dto));
            webSocketRegistry.broadcast(roomKey(patient.getId(), doctor.getId()), objectMapper.writeValueAsString(root));
        } catch (Exception ignored) {
            // vẫn lưu DB dù broadcast lỗi
        }
        return dto;
    }

    private ChatMessageDto toDto(DoctorPatientMessage m) {
        String ts = m.getCreatedAt() != null
                ? ISO_FMT.format(m.getCreatedAt().atZone(ZoneId.systemDefault()))
                : "";
        return ChatMessageDto.builder()
                .id(m.getId())
                .patientId(m.getPatient().getId())
                .doctorId(m.getDoctor().getId())
                .fromPatient(m.isFromPatient())
                .content(m.getContent())
                .createdAt(ts)
                .build();
    }
}
