package com.hcmute.mobile_android.network.models;

public class ChatSendBody {
    private final String content;

    public ChatSendBody(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }
}
