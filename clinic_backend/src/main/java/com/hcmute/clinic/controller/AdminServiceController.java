package com.hcmute.clinic.controller;

import com.hcmute.clinic.entity.ServiceCategory;
import com.hcmute.clinic.service.AdminServiceManagementService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/services")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminServiceController {

    private final AdminServiceManagementService adminService;

    @PostMapping("/categories")
    public ResponseEntity<?> createCategory(@RequestBody CategoryRequest request) {
        ServiceCategory category = adminService.createCategory(request.getName(), request.getDescription());
        return ResponseEntity.ok(Map.of(
                "id", category.getId(),
                "name", category.getName(),
                "message", "Category created successfully"
        ));
    }

    @PostMapping
    public ResponseEntity<?> createService(@RequestBody ServiceRequest request) {
        try {
            com.hcmute.clinic.entity.Service service = adminService.createService(
                    request.getCategoryId(),
                    request.getName(),
                    request.getDescription(),
                    request.getPrice(),
                    request.getDurationMinutes(),
                    request.getImageUrls()
            );
            return ResponseEntity.ok(Map.of(
                    "id", service.getId(),
                    "name", service.getName(),
                    "message", "Service created successfully"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable("id") Long id, @RequestParam("active") boolean active) {
        try {
            adminService.toggleServiceStatus(id, active);
            return ResponseEntity.ok(Map.of("message", "Status updated successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @Data
    public static class CategoryRequest {
        private String name;
        private String description;
    }

    @Data
    public static class ServiceRequest {
        private Long categoryId;
        private String name;
        private String description;
        private Double price;
        private Integer durationMinutes;
        private List<String> imageUrls;
    }
}
