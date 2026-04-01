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
    private static final String KEY_REFRESH_TOKEN = "jwt_refresh_token";
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
            android.util.Log.d("TokenManager", "EncryptedSharedPreferences initialized successfully");
        } catch (GeneralSecurityException | IOException e) {
            android.util.Log.e("TokenManager", "Failed to initialize EncryptedSharedPreferences", e);
            
            // If encryption fails, delete corrupted encrypted prefs and use fallback
            try {
                // Clear corrupted encrypted preferences
                context.deleteSharedPreferences(PREF_NAME);
                android.util.Log.w("TokenManager", "Deleted corrupted encrypted preferences");
            } catch (Exception deleteEx) {
                android.util.Log.e("TokenManager", "Failed to delete corrupted prefs", deleteEx);
            }
            
            // Fallback to regular SharedPreferences
            try {
                android.util.Log.w("TokenManager", "Falling back to regular SharedPreferences");
                sharedPreferences = context.getSharedPreferences(PREF_NAME + "_fallback", Context.MODE_PRIVATE);
            } catch (Exception ex) {
                android.util.Log.e("TokenManager", "Failed to initialize fallback SharedPreferences", ex);
            }
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
    
    public void saveRefreshToken(String token) {
        if (sharedPreferences == null) return;
        sharedPreferences.edit().putString(KEY_REFRESH_TOKEN, token).commit();
    }

    public String getRefreshToken() {
        return sharedPreferences == null ? null : sharedPreferences.getString(KEY_REFRESH_TOKEN, null);
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
        if (sharedPreferences == null || role == null) {
            android.util.Log.e("TokenManager", "Cannot save role - sharedPreferences or role is null");
            return;
        }
        android.util.Log.d("TokenManager", "Saving role: " + role);
        boolean success = sharedPreferences.edit().putString(KEY_USER_ROLE, role).commit();
        android.util.Log.d("TokenManager", "Save role commit result: " + success);
    }

    public String getUserRole() {
        if (sharedPreferences == null) {
            android.util.Log.e("TokenManager", "Cannot get role - sharedPreferences is null");
            return null;
        }
        String role = sharedPreferences.getString(KEY_USER_ROLE, null);
        android.util.Log.d("TokenManager", "Retrieved role: " + role);
        return role;
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
