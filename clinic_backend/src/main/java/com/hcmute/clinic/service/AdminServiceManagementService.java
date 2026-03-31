package com.hcmute.clinic.service;

import com.hcmute.clinic.entity.ServiceCategory;
import com.hcmute.clinic.entity.ServiceImage;
import com.hcmute.clinic.repository.*;
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
    private final AppointmentRepository appointmentRepository;
    private final TreatmentPlanStepRepository treatmentPlanStepRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final TreatmentPlanTemplateStepRepository templateStepRepository;

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

    public List<com.hcmute.clinic.entity.Service> getAllServices() {
        return serviceRepository.findAllByOrderByNameAsc();
    }

    @Transactional
    public com.hcmute.clinic.entity.Service updateService(
            Long id,
            Long categoryId,
            String name,
            String description,
            Double price,
            Integer duration
    ) {
        com.hcmute.clinic.entity.Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Service not found"));

        if (categoryId != null) {
            ServiceCategory category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new IllegalArgumentException("Category not found"));
            service.setCategory(category);
        }

        if (name != null) {
            service.setName(name);
        }
        if (description != null) {
            service.setDescription(description);
        }
        if (price != null) {
            service.setPrice(BigDecimal.valueOf(price));
        }
        if (duration != null) {
            service.setDurationMinutes(duration);
        }

        return serviceRepository.save(service);
    }

    @Transactional
    public void deleteService(Long id) {
        com.hcmute.clinic.entity.Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Service not found"));
        
        // UC: Nếu muốn xóa dịch vụ thì dịch vụ đó chưa được thực hiện (hóa đơn, treatment step, ..) và chưa được đặt lịch.
        if (appointmentRepository.existsByServiceId(id)) {
            throw new IllegalArgumentException("Không thể xóa dịch vụ này vì đã có lịch hẹn liên quan. Hãy chuyển sang trạng thái Ngừng hoạt động (Active=false) nếu cần.");
        }
        if (treatmentPlanStepRepository.existsByServiceId(id)) {
            throw new IllegalArgumentException("Không thể xóa dịch vụ này vì đã có trong hồ sơ điều trị của bệnh nhân.");
        }
        if (invoiceItemRepository.existsByServiceId(id)) {
            throw new IllegalArgumentException("Không thể xóa dịch vụ này vì đã có trong hóa đơn đã thanh toán/chờ thanh toán.");
        }
        if (templateStepRepository.existsByServiceId(id)) {
            throw new IllegalArgumentException("Không thể xóa dịch vụ này vì đang nằm trong danh sách phác đồ mẫu.");
        }

        serviceRepository.delete(service);
    }

    @Transactional
    public ServiceCategory updateCategory(Long id, String name, String description) {
        ServiceCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        
        if (name != null) {
            category.setName(name);
        }
        if (description != null) {
            category.setDescription(description);
        }
        
        return categoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        ServiceCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        
        // Check if category has services
        List<com.hcmute.clinic.entity.Service> services = serviceRepository.findByCategoryId(id);
        if (!services.isEmpty()) {
            throw new IllegalArgumentException("Cannot delete category with existing services");
        }
        
        categoryRepository.delete(category);
    }
}
