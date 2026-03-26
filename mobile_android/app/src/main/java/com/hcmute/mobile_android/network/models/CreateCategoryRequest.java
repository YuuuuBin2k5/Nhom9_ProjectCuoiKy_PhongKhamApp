package com.hcmute.mobile_android.network.models;

public class CreateCategoryRequest {
    private String name;
    private String description;

    public CreateCategoryRequest(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
}
