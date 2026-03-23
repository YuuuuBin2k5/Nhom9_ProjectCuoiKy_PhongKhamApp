package com.hcmute.mobile_android.util;

public final class PhoneDisplay {

    private PhoneDisplay() {
    }

    /** Hiển thị dạng +84 ••• ••• xxx (3 số cuối) */
    public static String maskLastThree(String raw) {
        if (raw == null) {
            return "";
        }
        String d = raw.replaceAll("\\s+", "");
        if (d.length() < 3) {
            return d;
        }
        String tail = d.substring(d.length() - 3);
        return "+84 ••• ••• " + tail;
    }
}
