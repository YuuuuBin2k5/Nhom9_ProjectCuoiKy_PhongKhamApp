package com.hcmute.clinic.service;

import com.hcmute.clinic.entity.ClinicRoom;
import com.hcmute.clinic.entity.Service;
import com.hcmute.clinic.repository.ClinicRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Centralized service for determining appropriate clinic rooms for services
 * This is the SINGLE SOURCE OF TRUTH for service-to-room mapping logic
 * 
 * DESIGN PRINCIPLES:
 * - All room assignment logic should go through this service
 * - Uses string matching on service names (temporary solution)
 * - Future: Will use database mapping (room_type, default_room_id)
 * 
 * USAGE:
 * - TreatmentPlanService uses this for template-based plans
 * - ToothServiceCalculationService uses this for odontogram services
 * - Any other service that needs room assignment should use this
 */
@org.springframework.stereotype.Service
@RequiredArgsConstructor
@Slf4j
public class ServiceRoomAssignmentService {
    
    private final ClinicRoomRepository clinicRoomRepository;
    
    /**
     * Determine the appropriate clinic room for a given service
     * 
     * ALGORITHM:
     * 1. Check service name against known patterns
     * 2. Find matching room by name
     * 3. Return first match or null
     * 
     * FUTURE ENHANCEMENTS:
     * - Priority 1: Service category default room
     * - Priority 2: Query by room type
     * - Priority 3: Fallback to string matching (current)
     * 
     * @param service The service to find a room for
     * @return ClinicRoom if found, null otherwise (will use current doctor's room)
     */
    public ClinicRoom determineRoomForService(Service service) {
        if (service == null || service.getName() == null) {
            log.warn("Cannot determine room: service or service name is null");
            return null;
        }
        
        String serviceName = service.getName().toLowerCase();
        log.debug("Determining room for service: {}", service.getName());
        
        // X-Ray services → X-Ray room
        ClinicRoom room = findRoomForXRayService(serviceName);
        if (room != null) {
            log.info("Assigned X-Ray room '{}' for service '{}'", room.getName(), service.getName());
            return room;
        }
        
        // Surgery services → Surgery room
        room = findRoomForSurgeryService(serviceName);
        if (room != null) {
            log.info("Assigned Surgery room '{}' for service '{}'", room.getName(), service.getName());
            return room;
        }
        
        // Orthodontics services → Orthodontics room
        room = findRoomForOrthodonticsService(serviceName);
        if (room != null) {
            log.info("Assigned Orthodontics room '{}' for service '{}'", room.getName(), service.getName());
            return room;
        }
        
        // Cosmetic services → Cosmetic room (if exists)
        room = findRoomForCosmeticService(serviceName);
        if (room != null) {
            log.info("Assigned Cosmetic room '{}' for service '{}'", room.getName(), service.getName());
            return room;
        }
        
        // Default: no specific room (will use current doctor's room)
        log.debug("No specific room found for service '{}' - will use current doctor's room", service.getName());
        return null;
    }
    
    /**
     * Find X-Ray room for X-Ray services
     */
    private ClinicRoom findRoomForXRayService(String serviceName) {
        if (serviceName.contains("x-quang") || 
            serviceName.contains("xquang") || 
            serviceName.contains("x quang") ||
            serviceName.contains("chụp phim") ||
            serviceName.contains("x-ray")) {
            
            return clinicRoomRepository.findAll().stream()
                    .filter(r -> r.getName() != null && 
                                (r.getName().toLowerCase().contains("x-quang") ||
                                 r.getName().toLowerCase().contains("x quang") ||
                                 r.getName().toLowerCase().contains("xquang") ||
                                 r.getName().toLowerCase().contains("x-ray")))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }
    
    /**
     * Find Surgery room for surgery services
     */
    private ClinicRoom findRoomForSurgeryService(String serviceName) {
        if (serviceName.contains("nhổ răng") || 
            serviceName.contains("nhổ") ||
            serviceName.contains("phẫu thuật") || 
            serviceName.contains("tiểu phẫu") ||
            serviceName.contains("cắt") ||
            serviceName.contains("mổ")) {
            
            return clinicRoomRepository.findAll().stream()
                    .filter(r -> r.getName() != null && 
                                (r.getName().toLowerCase().contains("phẫu") ||
                                 r.getName().toLowerCase().contains("surgery") ||
                                 r.getName().toLowerCase().contains("tiểu phẫu")))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }
    
    /**
     * Find Orthodontics room for orthodontics services
     */
    private ClinicRoom findRoomForOrthodonticsService(String serviceName) {
        if (serviceName.contains("niềng") || 
            serviceName.contains("chỉnh nha") || 
            serviceName.contains("ortho") ||
            serviceName.contains("mắc cài") ||
            serviceName.contains("invisalign")) {
            
            return clinicRoomRepository.findAll().stream()
                    .filter(r -> r.getName() != null && 
                                (r.getName().toLowerCase().contains("chỉnh nha") ||
                                 r.getName().toLowerCase().contains("niềng") ||
                                 r.getName().toLowerCase().contains("ortho")))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }
    
    /**
     * Find Cosmetic room for cosmetic services
     */
    private ClinicRoom findRoomForCosmeticService(String serviceName) {
        if (serviceName.contains("thẩm mỹ") || 
            serviceName.contains("làm trắng") || 
            serviceName.contains("tẩy trắng") ||
            serviceName.contains("veneer") ||
            serviceName.contains("bọc răng sứ")) {
            
            return clinicRoomRepository.findAll().stream()
                    .filter(r -> r.getName() != null && 
                                (r.getName().toLowerCase().contains("thẩm mỹ") ||
                                 r.getName().toLowerCase().contains("cosmetic")))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }
    
    /**
     * Validate that a treatment plan step has an appropriate room assigned
     * Used for validation before completing steps
     * 
     * @param service The service of the step
     * @param assignedRoom The currently assigned room (can be null)
     * @return true if room assignment is valid, false otherwise
     */
    public boolean validateRoomAssignment(Service service, ClinicRoom assignedRoom) {
        if (service == null) {
            return false;
        }
        
        // If service requires specific room but none assigned, invalid
        ClinicRoom expectedRoom = determineRoomForService(service);
        if (expectedRoom != null && assignedRoom == null) {
            log.warn("Service '{}' requires room '{}' but no room is assigned", 
                    service.getName(), expectedRoom.getName());
            return false;
        }
        
        // Otherwise valid (either has correct room or doesn't need specific room)
        return true;
    }
    
    /**
     * Get a human-readable explanation of why a room was assigned
     * Useful for debugging and logging
     * 
     * @param service The service
     * @param room The assigned room
     * @return Explanation string
     */
    public String explainRoomAssignment(Service service, ClinicRoom room) {
        if (service == null) {
            return "Service is null";
        }
        
        if (room == null) {
            return String.format("Service '%s' does not require a specific room - will use current doctor's room", 
                    service.getName());
        }
        
        String serviceName = service.getName().toLowerCase();
        
        if (serviceName.contains("x-quang") || serviceName.contains("chụp phim")) {
            return String.format("Service '%s' is an X-Ray service → assigned to '%s'", 
                    service.getName(), room.getName());
        }
        
        if (serviceName.contains("nhổ") || serviceName.contains("phẫu thuật")) {
            return String.format("Service '%s' is a surgery service → assigned to '%s'", 
                    service.getName(), room.getName());
        }
        
        if (serviceName.contains("niềng") || serviceName.contains("chỉnh nha")) {
            return String.format("Service '%s' is an orthodontics service → assigned to '%s'", 
                    service.getName(), room.getName());
        }
        
        if (serviceName.contains("thẩm mỹ") || serviceName.contains("làm trắng")) {
            return String.format("Service '%s' is a cosmetic service → assigned to '%s'", 
                    service.getName(), room.getName());
        }
        
        return String.format("Service '%s' assigned to '%s'", service.getName(), room.getName());
    }
}
