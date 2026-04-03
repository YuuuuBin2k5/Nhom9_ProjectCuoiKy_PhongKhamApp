package com.hcmute.mobile_android.network.models;

import com.google.gson.annotations.SerializedName;

/**
 * DTO chứa thông tin chi tiết về một dịch vụ nha khoa.
 */
public class ServiceItem {
    /** ID của dịch vụ */
    @SerializedName("id")
    private Long id;
    /** Tên dịch vụ */
    @SerializedName("name")
    private String name;
    /** Mô tả chi tiết về dịch vụ */
    @SerializedName("description")
    private String description;
    /** Giá của dịch vụ */
    @SerializedName("price")
    private double price;
    @SerializedName("durationMinutes")
    private Integer durationMinutes;
    @SerializedName("uiTemplateType")
    private String uiTemplateType;
    @SerializedName("categoryId")
    private Integer categoryId;
    @SerializedName("categoryName")
    private String categoryName;
    @SerializedName("imageUrls")
    private java.util.List<String> imageUrls;
    @SerializedName("active")
    private boolean active;

    public ServiceItem() {
    }

    public ServiceItem(Long id, String name, double price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public String getUiTemplateType() { return uiTemplateType; }
    public Integer getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }
    public java.util.List<String> getImageUrls() { return imageUrls; }
    public boolean isActive() { return active; }
}
