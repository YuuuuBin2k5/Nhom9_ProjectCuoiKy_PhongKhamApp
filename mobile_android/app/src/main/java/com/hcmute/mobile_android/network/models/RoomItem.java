package com.hcmute.mobile_android.network.models;

public class RoomItem {
    private Long id;
    private String name;
    private String status;
    private int waitingCount;
    private boolean active;

    public RoomItem() {}

    public RoomItem(Long id, String name, String status, int waitingCount) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.waitingCount = waitingCount;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getWaitingCount() { return waitingCount; }
    public void setWaitingCount(int waitingCount) { this.waitingCount = waitingCount; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}