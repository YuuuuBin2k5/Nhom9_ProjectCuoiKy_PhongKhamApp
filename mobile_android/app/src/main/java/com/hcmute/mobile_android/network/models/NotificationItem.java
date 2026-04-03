package com.hcmute.mobile_android.network.models;

import com.google.gson.annotations.SerializedName;

/**
 * DTO đại diện cho một mục thông báo trong hệ thống.
 */
public class NotificationItem {
    /** ID của thông báo */
    @SerializedName("id")
    private long id;

    /** Tiêu đề của thông báo */
    @SerializedName("title")
    private String title;

    /** Nội dung thông báo */
    @SerializedName("message")
    private String message;

    @SerializedName("type")
    private String type;

    @SerializedName("isRead")
    private boolean isRead;

    @SerializedName("createdAt")
    private String createdAt;

    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getType() { return type; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    public String getCreatedAt() { return createdAt; }
}
