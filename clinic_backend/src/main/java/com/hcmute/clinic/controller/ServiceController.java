package com.hcmute.clinic.controller;

import com.hcmute.clinic.entity.Service;
import com.hcmute.clinic.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServiceController {

    private final ServiceRepository serviceRepository;

    @GetMapping
    public ResponseEntity<?> listActive() {
        List<Service> all = serviceRepository.findByActiveTrueOrderByNameAsc();
        List<ServiceDto> dtos = all.stream()
                .map(s -> new ServiceDto(
                        s.getId(),
                        s.getName(),
                        s.getDescription(),
                        s.getPrice() != null ? s.getPrice().doubleValue() : 0,
                        s.getDurationMinutes(),
                        s.getUiTemplateType() != null ? s.getUiTemplateType().name() : "GENERAL",
                        s.getCategory() != null ? s.getCategory().getName() : null
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    public record ServiceDto(Long id, String name, String description, double price, Integer durationMinutes, String uiTemplateType, String categoryName) {}
}
