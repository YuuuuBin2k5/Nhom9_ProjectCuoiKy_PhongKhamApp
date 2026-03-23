package com.hcmute.clinic.service;

import com.hcmute.clinic.entity.ScanLog;
import com.hcmute.clinic.repository.ScanLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScanLogService {

    private final ScanLogRepository scanLogRepository;

    @Transactional
    public void logFailedScan(String qrData, HttpStatusCode statusCode, String errorMessage) {
        String truncated = qrData != null && qrData.length() > 500 ? qrData.substring(0, 500) : qrData;
        int code = statusCode != null ? statusCode.value() : 0;
        ScanLog log = ScanLog.builder()
                .qrData(truncated)
                .statusCode(code)
                .errorMessage(errorMessage != null && errorMessage.length() > 512 ? errorMessage.substring(0, 512) : errorMessage)
                .build();
        scanLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public List<ScanLogDto> getRecentErrors(int limit) {
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        return scanLogRepository.findByCreatedAtAfterOrderByCreatedAtDesc(since, PageRequest.of(0, limit))
                .stream()
                .map(s -> new ScanLogDto(
                        s.getId(),
                        s.getQrData(),
                        s.getStatusCode(),
                        s.getErrorMessage(),
                        s.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    public record ScanLogDto(Long id, String qrData, Integer statusCode, String errorMessage, java.time.LocalDateTime createdAt) {}
}
