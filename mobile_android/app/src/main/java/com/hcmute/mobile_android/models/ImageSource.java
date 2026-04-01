package com.hcmute.mobile_android.models;

import android.net.Uri;

public class ImageSource {
    private Uri uri;
    private String url;
    private boolean isUrl;

    public ImageSource(Uri uri) {
        this.uri = uri;
        this.isUrl = false;
    }

    public ImageSource(String url) {
        this.url = url;
        this.isUrl = true;
    }

    public boolean isUrl() {
        return isUrl;
    }

    public Uri getUri() {
        return uri;
    }

    public String getUrl() {
        return url;
    }

    public Object getSource() {
        return isUrl ? url : uri;
    }
}
