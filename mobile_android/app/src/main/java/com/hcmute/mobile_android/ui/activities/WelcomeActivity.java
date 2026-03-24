package com.hcmute.mobile_android.ui.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.hcmute.mobile_android.R;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        checkSession();

        findViewById(R.id.btnCreateAccount).setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });

        findViewById(R.id.tvSignIn).setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
    }

    private void checkSession() {
        com.hcmute.mobile_android.util.TokenManager tm = new com.hcmute.mobile_android.util.TokenManager(this);
        String token = tm.getToken();
        String role = tm.getUserRole();
        
        if (token != null && !token.isEmpty()) {
            // Token exists, navigate based on role
            Intent intent;
            if ("ADMIN".equals(role)) {
                intent = new Intent(this, AdminMainActivity.class);
            } else {
                // Default to patient/user main screen
                intent = new Intent(this, MainActivity.class);
            }
            
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }
}
