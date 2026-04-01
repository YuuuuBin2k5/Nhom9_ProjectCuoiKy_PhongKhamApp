package com.hcmute.clinic.controller;

import com.hcmute.clinic.dto.*;
import com.hcmute.clinic.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {
    
    private final ReviewService reviewService;
    
    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ReviewDto> createReview(
        @RequestBody ReviewRequest request,
        Authentication auth
    ) {
        Long patientId = Long.parseLong(auth.getName());
        return ResponseEntity.ok(reviewService.createReview(request, patientId));
    }
    
    @GetMapping("/my")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<ReviewDto>> getMyReviews(Authentication auth) {
        Long patientId = Long.parseLong(auth.getName());
        return ResponseEntity.ok(reviewService.getPatientReviews(patientId));
    }
    
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<ReviewDto>> getDoctorReviews(@PathVariable Long doctorId) {
        return ResponseEntity.ok(reviewService.getDoctorReviews(doctorId));
    }
    
    @GetMapping("/service/{serviceId}")
    public ResponseEntity<List<ReviewDto>> getServiceReviews(@PathVariable Long serviceId) {
        return ResponseEntity.ok(reviewService.getServiceReviews(serviceId));
    }
}
