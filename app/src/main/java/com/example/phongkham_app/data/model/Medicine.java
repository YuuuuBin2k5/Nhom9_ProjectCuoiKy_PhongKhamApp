package com.example.phongkham_app.data.model;

public class Medicine {
    private int id;
    private String name;
    private String dosage;
    private String instruction;

    public Medicine() {
    }

    public Medicine(int id, String name, String dosage, String instruction) {
        this.id = id;
        this.name = name;
        this.dosage = dosage;
        this.instruction = instruction;
    }

    public Medicine(String name, String dosage) {
        this.name = name;
        this.dosage = dosage;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public String getInstruction() { return instruction; }
    public void setInstruction(String instruction) { this.instruction = instruction; }
}
