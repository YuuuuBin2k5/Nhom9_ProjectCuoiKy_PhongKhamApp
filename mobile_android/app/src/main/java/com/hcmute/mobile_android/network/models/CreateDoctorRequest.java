package com.hcmute.mobile_android.network.models;

public class CreateDoctorRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String specialty;
    private int experienceYears;
    private String bio;
    private Long assignedRoomId;

    public CreateDoctorRequest(String firstName, String lastName, String email, 
                              String password, String specialty, int experienceYears, String bio, Long assignedRoomId) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.specialty = specialty;
        this.experienceYears = experienceYears;
        this.bio = bio;
        this.assignedRoomId = assignedRoomId;
    }

    // Getters
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getSpecialty() { return specialty; }
    public int getExperienceYears() { return experienceYears; }
    public String getBio() { return bio; }
    public Long getAssignedRoomId() { return assignedRoomId; }
}