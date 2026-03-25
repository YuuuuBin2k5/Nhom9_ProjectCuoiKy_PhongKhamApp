package com.hcmute.mobile_android.network.models.request;

public class ToothTreatmentDTO {
    private String toothNumber;
    private String treatmentNote;

    public ToothTreatmentDTO() {}

    public ToothTreatmentDTO(String toothNumber, String treatmentNote) {
        this.toothNumber = toothNumber;
        this.treatmentNote = treatmentNote;
    }

    public String getToothNumber() { return toothNumber; }
    public void setToothNumber(String toothNumber) { this.toothNumber = toothNumber; }

    public String getTreatmentNote() { return treatmentNote; }
    public void setTreatmentNote(String treatmentNote) { this.treatmentNote = treatmentNote; }
}
