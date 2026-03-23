package com.hcmute.mobile_android.network.models;

public class CheckInMyStatusResponse {
    private boolean checkedIn;
    private Integer queueNumber;
    private Integer queuePosition;
    private Integer estimatedWaitTime;
    private String roomName;
    private String roomLocation;
    private String status;
    private String statusLabel;
    private String hint;

    public boolean isCheckedIn() {
        return checkedIn;
    }

    public Integer getQueueNumber() {
        return queueNumber;
    }

    public Integer getQueuePosition() {
        return queuePosition;
    }

    public Integer getEstimatedWaitTime() {
        return estimatedWaitTime;
    }

    public String getRoomName() {
        return roomName;
    }

    public String getRoomLocation() {
        return roomLocation;
    }

    public String getStatus() {
        return status;
    }

    public String getStatusLabel() {
        return statusLabel;
    }

    public String getHint() {
        return hint;
    }
}
