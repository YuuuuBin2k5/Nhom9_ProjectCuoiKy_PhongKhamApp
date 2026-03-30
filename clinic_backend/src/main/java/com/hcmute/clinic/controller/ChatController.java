package com.hcmute.clinic.controller;

import com.hcmute.clinic.dto.ChatMessageDto;
import com.hcmute.clinic.dto.ChatSendRequest;
import com.hcmute.clinic.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/doctor/{doctorId}/messages")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<ChatMessageDto>> list(@PathVariable Long doctorId, Authentication auth) {
        long patientId = Long.parseLong(auth.getName());
        return ResponseEntity.ok(chatService.listMessages(patientId, doctorId));
    }

    @PostMapping("/doctor/{doctorId}/messages")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ChatMessageDto> sendPatient(
            @PathVariable Long doctorId,
            @RequestBody ChatSendRequest req,
            Authentication auth) {
        long patientId = Long.parseLong(auth.getName());
        return ResponseEntity.ok(chatService.sendFromPatient(patientId, doctorId, req.getContent()));
    }
}
