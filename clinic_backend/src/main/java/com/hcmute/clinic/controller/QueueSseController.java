package com.hcmute.clinic.controller;

import com.hcmute.clinic.service.QueueEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/queue")
@RequiredArgsConstructor
public class QueueSseController {

    private final QueueEventService queueEventService;

    @GetMapping(value = "/stream/room/{roomId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public SseEmitter streamRoom(@PathVariable Long roomId) {
        return queueEventService.subscribe(roomId);
    }
}
