package com.hcmute.clinic.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.Builder;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

/**
 * Lớp Thực thể Patient (Bệnh nhân) - Kế thừa từ User, trung tâm của mọi luồng nghiệp vụ.
 * Lưu trữ thông tin cá nhân và lịch sử y tế cơ bản của khách hàng.
 */
@Entity
@Table(name = "patients")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class Patient extends User {
    /** Số điện thoại liên lạc của bệnh nhân */
    private String phone;
    
    /** Ngày tháng năm sinh */
    private LocalDate dob;
    
    /** Giới tính */
    private String gender;
    
    /** Địa chỉ cư trú */
    private String address;
    
    @Builder.Default
    @Column(name = "reward_points")
    /** Điểm thưởng tích lũy của bệnh nhân */
    private Integer rewardPoints = 0;
    
    @Column(name = "qr_code_data")
    private String qrCodeData;

    @Column(name = "fcm_token")
    private String fcmToken;

    @OneToOne(mappedBy = "patient", cascade = CascadeType.ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private PatientProfile profile;
}
