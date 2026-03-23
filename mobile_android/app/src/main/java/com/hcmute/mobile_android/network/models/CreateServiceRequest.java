package com.hcmute.mobile_android.network.models;

public class CreateServiceRequest {
    private int categoryId;
    private String name;
    private String description;
    private double price;
    private int durationMinutes;

    public CreateServiceRequest(int categoryId, String name, String description, double price, int durationMinutes) {
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.durationMinutes = durationMinutes;
    }

    // Getters
    public int getCategoryId() { return categoryId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public int getDurationMinutes() { return durationMinutes; }
}