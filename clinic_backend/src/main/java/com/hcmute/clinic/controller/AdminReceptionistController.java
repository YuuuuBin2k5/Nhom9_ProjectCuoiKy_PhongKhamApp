package com.hcmute.clinic.controller;

import com.hcmute.clinic.entity.Receptionist;
import com.hcmute.clinic.service.AdminReceptionistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller dành cho Quản trị viên (Admin) để quản lý nhân viên lễ tân.
 * Hỗ trợ các thao tác CRUD và quản lý trạng thái tài khoản lễ tân.
 */
@RestController
@RequestMapping("/api/admin/receptionists")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminReceptionistController {

    private final AdminReceptionistService adminReceptionistService;

    /**
     * Lấy danh sách tất cả nhân viên lễ tân.
     * @return ResponseEntity chứa danh sách Receptionist.
     */
    @GetMapping
    public ResponseEntity<List<Receptionist>> getAll() {
        return ResponseEntity.ok(adminReceptionistService.getAllReceptionists());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, String> request) {
        try {
            return ResponseEntity.ok(adminReceptionistService.createReceptionist(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestParam boolean active) {
        try {
            adminReceptionistService.updateStatus(id, active);
            return ResponseEntity.ok(Map.of("message", "Status updated"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            adminReceptionistService.delete(id);
            return ResponseEntity.ok(Map.of("message", "Deleted"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
