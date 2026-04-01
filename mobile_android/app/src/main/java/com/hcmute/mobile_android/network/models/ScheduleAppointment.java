package com.hcmute.mobile_android.network.models;

public class ScheduleAppointment {
    private Long id;
    private String patientName;
    private String patientPhone;
    private String serviceName;
    private String datetime;
    private String status;

    public Long getId() { return id; }
    public String getPatientName() { return patientName; }
    public String getPatientPhone() { return patientPhone; }
    public String getServiceName() { return serviceName; }
    public String getDatetime() { return datetime; }
    public String getStatus() { return status; }
}
