package com.hcmute.clinic.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
public class RoomRequest {
    @NotBlank
    private String name;
    
    private String description;
}
