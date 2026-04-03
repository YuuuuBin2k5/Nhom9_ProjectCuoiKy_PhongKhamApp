package com.hcmute.clinic.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO Yêu cầu thêm dịch vụ nha khoa tổng quát (không chỉ định răng).
 * Thường dùng cho: Khám tư vấn, Chụp X-quang, Lấy cao răng, v.v.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddGeneralServiceRequest {
    private Long serviceId;           // ID of the service to add
    private Integer sequenceOrder;    // Order of this step in the treatment plan
}
