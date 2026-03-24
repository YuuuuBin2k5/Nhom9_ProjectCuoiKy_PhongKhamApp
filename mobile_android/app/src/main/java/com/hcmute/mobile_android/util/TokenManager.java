package com.hcmute.mobile_android.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class TokenManager {
    private static final String PREF_NAME = "secure_prefs";
    private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_PATIENT_ID = "patient_id";
    private static final String KEY_USER_ROLE = "user_role";
    private static final String KEY_USER_NAME = "user_name";
    private SharedPreferences sharedPreferences;

    public TokenManager(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            sharedPreferences = EncryptedSharedPreferences.create(
                    context,
                    PREF_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    public void saveToken(String token) {
        if (sharedPreferences == null) {
            return;
        }
        sharedPreferences.edit().putString(KEY_TOKEN, token).commit();
    }

    public String getToken() {
        return sharedPreferences == null ? null : sharedPreferences.getString(KEY_TOKEN, null);
    }

    public void savePatientId(Long id) {
        if (sharedPreferences == null || id == null) return;
        sharedPreferences.edit().putLong(KEY_PATIENT_ID, id).commit();
    }

    public Long getPatientId() {
        if (sharedPreferences == null) return -1L;
        long id = sharedPreferences.getLong(KEY_PATIENT_ID, -1L);
        return id == -1L ? null : id;
    }

    public void saveUserRole(String role) {
        if (sharedPreferences == null || role == null) return;
        sharedPreferences.edit().putString(KEY_USER_ROLE, role).commit();
    }

    public String getUserRole() {
        return sharedPreferences == null ? null : sharedPreferences.getString(KEY_USER_ROLE, null);
    }

    public void saveUserName(String name) {
        if (sharedPreferences == null || name == null) return;
        sharedPreferences.edit().putString(KEY_USER_NAME, name).commit();
    }

    public String getUserName() {
        return sharedPreferences == null ? null : sharedPreferences.getString(KEY_USER_NAME, null);
    }

    public void clearToken() {
        if (sharedPreferences != null) {
            sharedPreferences.edit()
                    .remove(KEY_TOKEN)
                    .remove(KEY_PATIENT_ID)
                    .remove(KEY_USER_ROLE)
                    .remove(KEY_USER_NAME)
                    .commit();
        }
    }
}
