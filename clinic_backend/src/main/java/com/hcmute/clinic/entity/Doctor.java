package com.hcmute.clinic.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "doctors")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
public class Doctor extends User {
    @ManyToOne
    @JoinColumn(name = "clinic_room_id")
    private ClinicRoom clinicRoom;

    private String specialization;
    
    @Column(name = "license_number")
    private String licenseNumber;
    
    @Column(columnDefinition = "TEXT")
    private String biography;
    
    @Column(name = "experience_years")
    private Integer experienceYears;
}
