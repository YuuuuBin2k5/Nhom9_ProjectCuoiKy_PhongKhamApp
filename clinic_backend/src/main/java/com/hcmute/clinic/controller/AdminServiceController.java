<<<<<<< HEAD
package com.hcmute.clinic.controller;

=======
import com.hcmute.clinic.dto.ServiceDto;
import com.hcmute.clinic.entity.Service;
>>>>>>> 492f872343b2ce06255b5595414c8b8dfe77b756
import com.hcmute.clinic.entity.ServiceCategory;
import com.hcmute.clinic.service.AdminServiceManagementService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
<<<<<<< HEAD

import java.util.List;
import java.util.Map;
=======
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
>>>>>>> 492f872343b2ce06255b5595414c8b8dfe77b756

@RestController
@RequestMapping("/api/admin/services")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminServiceController {

    private final AdminServiceManagementService adminService;

<<<<<<< HEAD
=======
    @GetMapping
    public ResponseEntity<List<ServiceDto>> listAll() {
        List<Service> all = adminService.getAllServices();
        List<ServiceDto> dtos = all.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

>>>>>>> 492f872343b2ce06255b5595414c8b8dfe77b756
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

<<<<<<< HEAD
=======
    private ServiceDto toDto(Service s) {
        List<String> imageUrls = s.getImages().stream()
                .map(img -> ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/uploads/")
                        .path(img.getImageUrl())
                        .toUriString())
                .collect(Collectors.toList());

        return new ServiceDto(
                s.getId(),
                s.getName(),
                s.getDescription(),
                s.getPrice() != null ? s.getPrice().doubleValue() : 0,
                s.getDurationMinutes(),
                s.getUiTemplateType() != null ? s.getUiTemplateType().name() : "GENERAL",
                s.getCategory() != null ? s.getCategory().getId() : null,
                s.getCategory() != null ? s.getCategory().getName() : null,
                imageUrls,
                s.isActive()
        );
    }

>>>>>>> 492f872343b2ce06255b5595414c8b8dfe77b756
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
