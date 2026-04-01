package com.hcmute.clinic.dto;

import com.hcmute.clinic.validation.StrongPassword;
import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Data
public class RegisterRequest {
    @NotBlank
    @Email
    private String email;
    
    @StrongPassword
    private String password;
    
    @NotBlank
    private String firstName;
    
    @NotBlank
    private String lastName;
    
    @NotBlank
    private String phone;
}
