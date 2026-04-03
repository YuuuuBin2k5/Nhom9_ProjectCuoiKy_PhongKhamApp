package com.hcmute.clinic.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Trình xử lý ngoại lệ tập trung cho toàn bộ ứng dụng.
 * Chuyển đổi các ngoại lệ thành phản hồi JSON có cấu trúc để Client dễ dàng xử lý.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Xử lý các ngoại lệ ResponseStatusException.
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(ResponseStatusException e) {
        String message = e.getReason() != null ? e.getReason() : (e.getStatusCode() instanceof HttpStatus ? ((HttpStatus) e.getStatusCode()).getReasonPhrase() : "Lỗi");
        return ResponseEntity.status(e.getStatusCode())
                .body(Map.of("message", message, "success", false));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException e) {
        log.warn("Access denied: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("message", "Không có quyền truy cập", "success", false));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest()
                .body(Map.of("message", message.isEmpty() ? "Dữ liệu không hợp lệ" : message, "success", false));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception e) {
        log.error("Unexpected error", e);
        String detailedMsg = "Lỗi hệ thống: " + e.getClass().getSimpleName() + " - " + e.getMessage();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", detailedMsg, "success", false));
    }
}
