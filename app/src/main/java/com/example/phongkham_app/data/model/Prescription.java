package com.example.phongkham_app.data.model;

public class Prescription {
    private String drugName;
    private String dosage;
    private String frequency;
    private String instruction;
    private String quantity;
    private String days;

    public Prescription() {
    }

    public Prescription(String drugName, String dosage, String frequency, String instruction, String quantity, String days) {
        this.drugName = drugName;
        this.dosage = dosage;
        this.frequency = frequency;
        this.instruction = instruction;
        this.quantity = quantity;
        this.days = days;
    }

    public String getDrugName() { return drugName; }
    public void setDrugName(String drugName) { this.drugName = drugName; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public String getInstruction() { return instruction; }
    public void setInstruction(String instruction) { this.instruction = instruction; }

    public String getQuantity() { return quantity; }
    public void setQuantity(String quantity) { this.quantity = quantity; }

    public String getDays() { return days; }
    public void setDays(String days) { this.days = days; }
}
