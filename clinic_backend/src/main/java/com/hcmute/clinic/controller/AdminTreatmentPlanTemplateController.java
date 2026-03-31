package com.hcmute.clinic.controller;

import com.hcmute.clinic.dto.TreatmentPlanTemplateRequest;
import com.hcmute.clinic.dto.TreatmentPlanTemplateResponseDTO;
import com.hcmute.clinic.service.TreatmentPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/admin/treatment-templates")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminTreatmentPlanTemplateController {

    private final TreatmentPlanService treatmentPlanService;

    @GetMapping
    public ResponseEntity<List<TreatmentPlanTemplateResponseDTO>> getAllTemplates() {
        return ResponseEntity.ok(treatmentPlanService.listAllTemplates());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TreatmentPlanTemplateResponseDTO> getTemplateById(@PathVariable Long id) {
        return ResponseEntity.ok(treatmentPlanService.getTemplateById(id));
    }

    @PostMapping
    public ResponseEntity<TreatmentPlanTemplateResponseDTO> createTemplate(@RequestBody TreatmentPlanTemplateRequest request) {
        return ResponseEntity.ok(treatmentPlanService.saveTemplate(null, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TreatmentPlanTemplateResponseDTO> updateTemplate(@PathVariable Long id, @RequestBody TreatmentPlanTemplateRequest request) {
        return ResponseEntity.ok(treatmentPlanService.saveTemplate(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTemplate(@PathVariable Long id) {
        try {
            treatmentPlanService.deleteTemplate(id);
            return ResponseEntity.ok(Collections.singletonMap("message", "Xóa mẫu liệu trình thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", e.getMessage()));
        }
    }
}
