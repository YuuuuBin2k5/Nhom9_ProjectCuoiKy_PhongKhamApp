package com.hcmute.clinic.enums;

public enum PaymentStatus {
    PENDING,    // Chưa thanh toán
    PAID,       // Đã thanh toán
    SUCCESS,    // Thanh toán thành công (for Payment entity)
    CANCELLED   // Đã hủy
}
