package com.hcmute.clinic.controller;
import com.hcmute.clinic.entity.ServiceCategory;
import com.hcmute.clinic.entity.Service;
import com.hcmute.clinic.repository.ServiceCategoryRepository;
import com.hcmute.clinic.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServiceController {

    private final ServiceRepository serviceRepository;
    private final ServiceCategoryRepository categoryRepository;

    @GetMapping
    public ResponseEntity<?> listActive() {
        List<Service> all = serviceRepository.findByActiveTrueOrderByNameAsc();
        List<ServiceDto> dtos = all.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/categories")
    public ResponseEntity<?> listByCategories() {
        List<ServiceCategory> categories = categoryRepository.findAll();
        List<CategoryWithServicesDto> dtos = categories.stream()
                .map(cat -> new CategoryWithServicesDto(
                        cat.getId(),
                        cat.getName(),
                        cat.getDescription(),
                        cat.getServices().stream()
                                .filter(Service::isActive)
                                .map(this::toDto)
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

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
                s.isActive(),
                imageUrls
        );
    }

    public record ServiceDto(
            Long id, 
            String name, 
            String description, 
            double price, 
            Integer durationMinutes, 
            String uiTemplateType, 
            Long categoryId,
            String categoryName,
            boolean active,
            List<String> imageUrls
    ) {}

    public record CategoryWithServicesDto(
            Long id,
            String name,
            String description,
            List<ServiceDto> services
    ) {}
}
