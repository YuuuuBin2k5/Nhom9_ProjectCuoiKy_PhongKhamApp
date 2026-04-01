package com.hcmute.mobile_android.network.models;

import com.google.gson.annotations.SerializedName;

public class TimeSlot {
    @SerializedName("time")
    private String time;
    
    @SerializedName("available")
    private boolean available;
    
    // Getters and setters
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
}
