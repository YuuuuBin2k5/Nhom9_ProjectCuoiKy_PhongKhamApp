package com.hcmute.mobile_android.network.models;

public class MessageResponse {
    private String message;
    private Long id;
    private String nextRoomName;

    public MessageResponse() {}

    public MessageResponse(String message) {
        this.message = message;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNextRoomName() { return nextRoomName; }
    public void setNextRoomName(String nextRoomName) { this.nextRoomName = nextRoomName; }
}