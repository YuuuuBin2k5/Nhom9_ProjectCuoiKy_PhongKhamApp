package com.hcmute.mobile_android.network.models;

import com.google.gson.annotations.SerializedName;

public class QrTokenResponse {
    @SerializedName("token")
    private String token;

    @SerializedName("expiresIn")
    private int expiresIn;

    @SerializedName("expiresAt")
    private String expiresAt;

    public String getToken() {
        return token;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public String getExpiresAt() {
        return expiresAt;
    }
}
