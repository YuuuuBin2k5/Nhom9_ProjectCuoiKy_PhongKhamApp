package com.hcmute.mobile_android.network.models;

public class CheckInScanRequest {
    private String qrContent;

    public CheckInScanRequest(String qrContent) {
        this.qrContent = qrContent;
    }

    public String getQrContent() {
        return qrContent;
    }

    public void setQrContent(String qrContent) {
        this.qrContent = qrContent;
    }
}