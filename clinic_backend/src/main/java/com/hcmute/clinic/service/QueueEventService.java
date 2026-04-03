package com.hcmute.clinic.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lớp Dịch vụ QueueEventService (Dịch vụ Sự kiện Hàng đợi) - Quản lý các kết nối thời gian thực qua SSE.
 * Tự động phát tin (Broadcast) khi có thay đổi trong hàng đợi để cập nhật giao diện phòng khám và bệnh nhân.
 */
@Service
@Slf4j
public class QueueEventService {

    /** roomId -> list of SseEmitters */
    private final Map<Long, java.util.List<SseEmitter>> roomSubscribers = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long roomId) {
        SseEmitter emitter = new SseEmitter(3600_000L); // 1h
        emitter.onCompletion(() -> removeEmitter(roomId, emitter));
        emitter.onTimeout(() -> removeEmitter(roomId, emitter));
        roomSubscribers.computeIfAbsent(roomId, k -> new java.util.concurrent.CopyOnWriteArrayList<>()).add(emitter);
        try {
            emitter.send(SseEmitter.event().name("connected").data("room:" + roomId));
        } catch (IOException e) {
            removeEmitter(roomId, emitter);
        }
        return emitter;
    }

    private void removeEmitter(Long roomId, SseEmitter emitter) {
        var list = roomSubscribers.get(roomId);
        if (list != null) {
            list.remove(emitter);
            if (list.isEmpty()) roomSubscribers.remove(roomId);
        }
    }

    public void broadcastRoomUpdated(Long roomId, String eventType, Object payload) {
        var list = roomSubscribers.get(roomId);
        if (list == null || list.isEmpty()) return;
        var toRemove = new java.util.ArrayList<SseEmitter>();
        for (SseEmitter e : list) {
            try {
                e.send(SseEmitter.event().name(eventType).data(payload));
            } catch (IOException ex) {
                toRemove.add(e);
            }
        }
        toRemove.forEach(em -> removeEmitter(roomId, em));
    }

    /** Broadcast queue.updated to all subscribers of the room */
    public void broadcastQueueUpdated(Long roomId) {
        broadcastRoomUpdated(roomId, "queue.updated", Map.of("roomId", roomId, "timestamp", System.currentTimeMillis()));
    }

    /** Broadcast queue.priority_returned when BN returns from X-Ray */
    public void broadcastPriorityReturned(Long roomId, Long queueId, String patientName) {
        broadcastRoomUpdated(roomId, "queue.priority_returned", Map.of(
                "roomId", roomId, "queueId", queueId, "patientName", patientName != null ? patientName : ""
        ));
    }

    /** Broadcast queue.called when BN is called to room */
    public void broadcastQueueCalled(Long roomId, int queueNumber, String roomName) {
        broadcastRoomUpdated(roomId, "queue.called", Map.of(
                "roomId", roomId, "queueNumber", queueNumber, "roomName", roomName != null ? roomName : ""
        ));
    }
}
