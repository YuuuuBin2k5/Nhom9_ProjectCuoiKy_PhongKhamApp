package com.hcmute.clinic.service;

import com.hcmute.clinic.dto.*;
import com.hcmute.clinic.entity.*;
import com.hcmute.clinic.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {
    
    private final ReviewRepository reviewRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final ServiceRepository serviceRepository;
    private final AppointmentRepository appointmentRepository;
    
    @Transactional
    public ReviewDto createReview(ReviewRequest request, Long patientId) {
        // Validate patient
        Patient patient = patientRepository.findById(patientId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found"));
        
        // Validate appointment
        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));
        
        // Check if already reviewed
        if (reviewRepository.existsByAppointmentId(request.getAppointmentId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Appointment already reviewed");
        }
        
        // Validate rating
        if (request.getRating() < 1 || request.getRating() > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rating must be between 1 and 5");
        }
        
        // Get doctor and service
        Doctor doctor = request.getDoctorId() != null ? 
            doctorRepository.findById(request.getDoctorId()).orElse(null) : null;
        com.hcmute.clinic.entity.Service service = request.getServiceId() != null ?
            serviceRepository.findById(request.getServiceId()).orElse(null) : null;
        
        // Create review
        Review review = Review.builder()
            .patient(patient)
            .doctor(doctor)
            .service(service)
            .appointment(appointment)
            .rating(request.getRating())
            .comment(request.getComment())
            .build();
        
        review = reviewRepository.save(review);
        
        return toDto(review);
    }
    
    public List<ReviewDto> getPatientReviews(Long patientId) {
        return reviewRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
            .stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }
    
    public List<ReviewDto> getDoctorReviews(Long doctorId) {
        return reviewRepository.findByDoctorIdOrderByCreatedAtDesc(doctorId)
            .stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }
    
    public List<ReviewDto> getServiceReviews(Long serviceId) {
        return reviewRepository.findByServiceIdOrderByCreatedAtDesc(serviceId)
            .stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }
    
    private ReviewDto toDto(Review review) {
        return ReviewDto.builder()
            .id(review.getId())
            .patientId(review.getPatient().getId())
            .patientName(review.getPatient().getFirstName() + " " + review.getPatient().getLastName())
            .doctorId(review.getDoctor() != null ? review.getDoctor().getId() : null)
            .doctorName(review.getDoctor() != null ? 
                review.getDoctor().getFirstName() + " " + review.getDoctor().getLastName() : null)
            .serviceId(review.getService() != null ? review.getService().getId() : null)
            .serviceName(review.getService() != null ? review.getService().getName() : null)
            .appointmentId(review.getAppointment().getId())
            .rating(review.getRating())
            .comment(review.getComment())
            .createdAt(review.getCreatedAt())
            .build();
    }
}
