package com.hcmute.clinic.enums;

public enum QueueStatus {
    WAITING,           // Chờ ở sảnh
    IN_PROGRESS,       // Đang khám
    PAUSED_FOR_TEST,   // Đi chụp X-Quang / xét nghiệm
    RETURNED_PRIORITY, // Đã về, ưu tiên lên đầu
    COMPLETED,
    SKIPPED,
    CANCELLED
}
