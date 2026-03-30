package com.hcmute.clinic.controller;

import com.hcmute.clinic.dto.ChatMessageDto;
import com.hcmute.clinic.dto.ChatSendRequest;
import com.hcmute.clinic.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/doctor/chat")
@RequiredArgsConstructor
public class DoctorChatReplyController {

    private final ChatService chatService;

    @PostMapping("/patient/{patientId}/messages")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<ChatMessageDto> sendDoctor(
            @PathVariable Long patientId,
            @RequestBody ChatSendRequest req,
            Authentication auth) {
        long doctorId = Long.parseLong(auth.getName());
        return ResponseEntity.ok(chatService.sendFromDoctor(doctorId, patientId, req.getContent()));
    }
}
