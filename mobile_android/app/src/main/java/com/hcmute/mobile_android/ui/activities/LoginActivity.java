package com.hcmute.mobile_android.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.models.LoginRequest;
import com.hcmute.mobile_android.network.models.LoginResponse;
import com.hcmute.mobile_android.util.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.tvSignUp).setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });

        findViewById(R.id.btnSignIn).setOnClickListener(v -> performLogin());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
    }

    private void performLogin() {
        String email = ((EditText) findViewById(R.id.etEmail)).getText().toString().trim();
        String password = ((EditText) findViewById(R.id.etPassword)).getText().toString();
        
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            return;
        }

        android.util.Log.d("LoginActivity", "=== LOGIN ATTEMPT ===");
        android.util.Log.d("LoginActivity", "Email: " + email);
        
        ApiService api = RetrofitClient.getApiService(this);
        api.login(new LoginRequest(email, password)).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                android.util.Log.d("LoginActivity", "=== LOGIN RESPONSE ===");
                android.util.Log.d("LoginActivity", "Response code: " + response.code());
                android.util.Log.d("LoginActivity", "Response successful: " + response.isSuccessful());
                
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse body = response.body();
                    
                    android.util.Log.d("LoginActivity", "=== LOGIN RESPONSE BODY ===");
                    android.util.Log.d("LoginActivity", "Token: " + (body.getToken() != null ? "present" : "null"));
                    android.util.Log.d("LoginActivity", "Email: " + body.getEmail());
                    android.util.Log.d("LoginActivity", "Role: " + body.getRole());
                    android.util.Log.d("LoginActivity", "UserId: " + body.getUserId());
                    
                    if (body.getToken() != null) {
                        TokenManager tm = new TokenManager(LoginActivity.this);
                        tm.saveToken(body.getToken());
                        if (body.getRefreshToken() != null) {
                            tm.saveRefreshToken(body.getRefreshToken());
                        }
                        
                        // Save user role
                        if (body.getRole() != null) {
                            android.util.Log.d("LoginActivity", "Saving role: " + body.getRole());
                            tm.saveUserRole(body.getRole());
                            
                            // Verify it was saved
                            String savedRole = tm.getUserRole();
                            android.util.Log.d("LoginActivity", "Verified saved role: " + savedRole);
                        }

                        // Save email as display name (shown in doctor greeting)
                        if (body.getEmail() != null) {
                            String emailStr = body.getEmail();
                            // Use part before @ as name
                            String displayName = emailStr.contains("@")
                                    ? emailStr.substring(0, emailStr.indexOf('@')) : emailStr;
                            tm.saveUserName(displayName);
                        }
                        
                        // Save patient ID if not admin
                        if (body.getUserId() != null && !"ADMIN".equals(body.getRole())) {
                            tm.savePatientId(body.getUserId());
                        }
                    }
                    
                    // Navigate based on user role
                    Intent intent;
                    if ("ADMIN".equals(body.getRole())) {
                        android.util.Log.d("LoginActivity", "Navigating to AdminMainActivity");
                        intent = new Intent(LoginActivity.this, AdminMainActivity.class);
                    } else {
                        android.util.Log.d("LoginActivity", "Navigating to MainActivity");
                        intent = new Intent(LoginActivity.this, MainActivity.class);
                    }
                    
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    android.util.Log.e("LoginActivity", "Login failed: " + response.code());
                    Toast.makeText(LoginActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                android.util.Log.e("LoginActivity", "=== LOGIN FAILURE ===");
                android.util.Log.e("LoginActivity", "Error: " + t.getMessage(), t);
                String msg = t.getMessage() != null ? t.getMessage() : "";
                Toast.makeText(LoginActivity.this,
                        getString(R.string.login_network_error) + "\n" + msg,
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
