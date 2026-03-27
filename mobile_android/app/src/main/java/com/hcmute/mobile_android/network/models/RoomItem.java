package com.hcmute.mobile_android.network.models;

import com.google.gson.annotations.SerializedName;

public class RoomItem {
    private Long id;
    private String name;
    
    @SerializedName("active")
    private boolean isActive;
    
    private int waitingCount;

    public RoomItem() {}

    public RoomItem(Long id, String name, boolean isActive, int waitingCount) {
        this.id = id;
        this.name = name;
        this.isActive = isActive;
        this.waitingCount = waitingCount;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public int getWaitingCount() { return waitingCount; }
    public void setWaitingCount(int waitingCount) { this.waitingCount = waitingCount; }
}