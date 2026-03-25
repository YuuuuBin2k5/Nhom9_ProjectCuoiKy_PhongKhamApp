package com.hcmute.mobile_android.network.models;

public class CheckInScanRequest {
    private String qrData;

    public CheckInScanRequest(String qrData) {
        this.qrData = qrData;
    }

    public String getQrData() {
        return qrData;
    }

    public void setQrData(String qrData) {
        this.qrData = qrData;
    }
}