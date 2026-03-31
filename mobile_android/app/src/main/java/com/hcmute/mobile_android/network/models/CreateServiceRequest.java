package com.hcmute.mobile_android.network.models;

import java.util.List;

public class CreateServiceRequest {
    private int categoryId;
    private String name;
    private String description;
    private double price;
    private int durationMinutes;
    private Integer defaultMonitoringDays;
    private List<String> imageUrls;

    public CreateServiceRequest(int categoryId, String name, String description, double price, int durationMinutes, Integer defaultMonitoringDays, List<String> imageUrls) {
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.durationMinutes = durationMinutes;
        this.defaultMonitoringDays = defaultMonitoringDays;
        this.imageUrls = imageUrls;
    }

    // Getters
    public int getCategoryId() { return categoryId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public int getDurationMinutes() { return durationMinutes; }
    public Integer getDefaultMonitoringDays() { return defaultMonitoringDays; }
    public List<String> getImageUrls() { return imageUrls; }
}