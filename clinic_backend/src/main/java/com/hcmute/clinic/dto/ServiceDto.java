package com.hcmute.clinic.dto;

import java.util.List;

public record ServiceDto(
        Long id,
        String name,
        String description,
        double price,
        Integer durationMinutes,
        String uiTemplateType,
        Long categoryId,
        String categoryName,
        List<String> imageUrls,
        boolean active
) {}
