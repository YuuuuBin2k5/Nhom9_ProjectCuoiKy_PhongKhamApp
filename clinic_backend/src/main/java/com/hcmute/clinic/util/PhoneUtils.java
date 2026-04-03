package com.hcmute.clinic.util;

/**
 * Tiện ích xử lý và chuẩn hóa số điện thoại theo định dạng chuẩn quốc tế.
 */
public final class PhoneUtils {

    private PhoneUtils() {
    }

    /** Chuẩn hóa số VN → E.164 dạng +84xxxxxxxxx */
    public static String normalizeVietnam(String raw) {
        if (raw == null) {
            return "";
        }
        String s = raw.replaceAll("\\s+", "").trim();
        if (s.isEmpty()) {
            return "";
        }
        if (s.startsWith("+")) {
            return s;
        }
        if (s.startsWith("0")) {
            return "+84" + s.substring(1);
        }
        if (s.startsWith("84")) {
            return "+" + s;
        }
        return "+84" + s;
    }
}
