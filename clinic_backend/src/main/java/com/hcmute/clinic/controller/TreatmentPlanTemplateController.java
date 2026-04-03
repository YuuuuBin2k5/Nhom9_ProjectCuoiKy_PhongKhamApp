package com.hcmute.clinic.controller;

import com.hcmute.clinic.dto.TreatmentPlanTemplateResponseDTO;
import com.hcmute.clinic.service.TreatmentPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller quản lý danh sách các mẫu phác đồ điều trị định sẵn.
 */
@RestController
@RequestMapping("/api/treatment-templates")
@RequiredArgsConstructor
public class TreatmentPlanTemplateController {

    private final TreatmentPlanService treatmentPlanService;

    /**
     * Lấy danh sách các mẫu phác đồ điều trị đang hoạt động.
     *
     * @return ResponseEntity chứa danh sách các mẫu phác đồ điều trị.
     */
    @GetMapping
    public ResponseEntity<List<TreatmentPlanTemplateResponseDTO>> listActive() {
        return ResponseEntity.ok(treatmentPlanService.listActiveTemplates());
    }
}
