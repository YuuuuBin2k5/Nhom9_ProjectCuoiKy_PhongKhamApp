package com.hcmute.clinic.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO Yêu cầu thêm dịch vụ cho một răng cụ thể.
 * Bác sĩ chọn vị trí răng trên sơ đồ và chỉ định dịch vụ tương ứng.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddToothServiceRequest {
    private Long serviceId;           // ID of the service to add
    private Integer sequenceOrder;    // Order of this step in the treatment plan
}
