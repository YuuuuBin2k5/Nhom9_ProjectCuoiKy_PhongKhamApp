package com.hcmute.clinic.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Lớp Thực thể Doctor (Bác sĩ) - Kế thừa từ User, đại diện cho người thực hiện điều trị.
 * Quản lý chuyên khoa và trình độ của bác sĩ trong hệ thống.
 */
@Entity
@Table(name = "doctors")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class Doctor extends User {
    @ManyToOne
    @JoinColumn(name = "clinic_room_id")
    /** Phòng khám nơi bác sĩ làm việc */
    private ClinicRoom clinicRoom;

    /** Chuyên khoa của bác sĩ */
    private String specialization;
    
    @Column(name = "license_number")
    /** Số giấy phép hành nghề */
    private String licenseNumber;
    
    @Column(columnDefinition = "TEXT")
    private String biography;
    
    @Column(name = "experience_years")
    private Integer experienceYears;
}
