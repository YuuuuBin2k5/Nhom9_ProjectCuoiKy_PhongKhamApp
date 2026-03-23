package com.hcmute.clinic.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.Builder;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Table(name = "patients")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
public class Patient extends User {
    private String phone;
    private LocalDate dob;
    private String gender;
    private String address;
    
    @Builder.Default
    @Column(name = "reward_points")
    private Integer rewardPoints = 0;
    
    @Column(name = "qr_code_data")
    private String qrCodeData;

    @OneToOne(mappedBy = "patient", cascade = CascadeType.ALL)
    private PatientProfile profile;
}
