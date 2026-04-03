package com.hcmute.mobile_android.network.models;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

/**
 * DTO chứa thông tin nhật ký hệ thống để hiển thị phía Admin.
 */
public class AuditLog {
    private Long id;
    private String action;
    private String entityType;
    private String entityId;
    private String userId;
    private String userName;
    private String details;
    private long timestamp;

    public Long getId() { return id; }
    public String getAction() { return action; }
    public String getEntityType() { return entityType; }
    public String getEntityId() { return entityId; }
    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getDetails() { return details; }
    public long getTimestamp() { return timestamp; }
}
