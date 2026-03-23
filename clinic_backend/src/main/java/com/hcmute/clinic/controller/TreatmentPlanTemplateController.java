package com.hcmute.clinic.controller;

import com.hcmute.clinic.service.TreatmentPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/treatment-templates")
@RequiredArgsConstructor
public class TreatmentPlanTemplateController {

    private final TreatmentPlanService treatmentPlanService;

    @GetMapping
    public ResponseEntity<?> list() {
        return ResponseEntity.ok(treatmentPlanService.listActiveTemplates());
    }
}
