package com.hcmute.clinic.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Lớp Thực thể Admin (Quản trị viên) - Kế thừa từ User.
 * Có quyền quản lý toàn bộ cấu hình hệ thống, nhân sự và báo cáo.
 */
@Entity
@Table(name = "admins")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
public class Admin extends User {
    // Admins currently only have common user fields
}
