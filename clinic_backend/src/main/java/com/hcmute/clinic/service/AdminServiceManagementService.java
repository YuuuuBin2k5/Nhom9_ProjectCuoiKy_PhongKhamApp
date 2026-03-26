package com.hcmute.clinic.service;

import com.hcmute.clinic.entity.ServiceCategory;
import com.hcmute.clinic.entity.ServiceImage;
import com.hcmute.clinic.repository.ServiceCategoryRepository;
import com.hcmute.clinic.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminServiceManagementService {

    private final ServiceRepository serviceRepository;
    private final ServiceCategoryRepository categoryRepository;

    @Transactional
    public ServiceCategory createCategory(String name, String description) {
        ServiceCategory category = ServiceCategory.builder()
                .name(name)
                .description(description)
                .build();
        return categoryRepository.save(category);
    }

    @Transactional
    public com.hcmute.clinic.entity.Service createService(
            Long categoryId, 
            String name, 
            String description, 
            Double price, 
            Integer duration, 
            List<String> imageUrls
    ) {
        ServiceCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        com.hcmute.clinic.entity.Service service = com.hcmute.clinic.entity.Service.builder()
                .category(category)
                .name(name)
                .description(description)
                .price(BigDecimal.valueOf(price))
                .durationMinutes(duration)
                .active(true)
                .build();

        if (imageUrls != null) {
            List<ServiceImage> images = imageUrls.stream()
                    .map(url -> {
                        // Extract filename from URL if it's a full URI
                        String fileName = url.contains("/uploads/") ? 
                                url.substring(url.lastIndexOf("/") + 1) : url;
                        return ServiceImage.builder()
                                .imageUrl(fileName)
                                .service(service)
                                .build();
                    })
                    .collect(java.util.stream.Collectors.toList());
            service.setImages(images);
        }

        return serviceRepository.save(service);
    }

    @Transactional
    public com.hcmute.clinic.entity.Service toggleServiceStatus(Long id, boolean active) {
        com.hcmute.clinic.entity.Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Service not found"));
        service.setActive(active);
        return serviceRepository.save(service);
    }
<<<<<<< HEAD
=======

    public List<com.hcmute.clinic.entity.Service> getAllServices() {
        return serviceRepository.findAllByOrderByNameAsc();
    }
>>>>>>> 492f872343b2ce06255b5595414c8b8dfe77b756
}
