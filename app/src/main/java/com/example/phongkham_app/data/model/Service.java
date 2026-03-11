package com.example.phongkham_app.data.model;

public class Service {
    private String name;
    private boolean enabled;
    private String price;
    private int iconResId;

    public Service() {
    }

    public Service(String name, boolean enabled) {
        this.name = name;
        this.enabled = enabled;
        this.price = "500,000 VNĐ";
    }

    public Service(String name, boolean enabled, String price) {
        this.name = name;
        this.enabled = enabled;
        this.price = price;
    }

    public Service(String name, String price, int iconResId) {
        this.name = name;
        this.price = price;
        this.iconResId = iconResId;
        this.enabled = true;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public int getIconResId() {
        return iconResId;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }
}
