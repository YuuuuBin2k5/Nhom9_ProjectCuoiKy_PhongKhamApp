package com.hcmute.mobile_android.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import com.hcmute.mobile_android.util.ToastUtils;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.models.RegisterRequest;
import com.hcmute.mobile_android.network.models.RegisterResultResponse;
import com.hcmute.mobile_android.util.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        findViewById(R.id.btnBackReg).setOnClickListener(v -> finish());

        findViewById(R.id.btnRegister).setOnClickListener(v -> performRegistration());

        findViewById(R.id.tvBackToLogin).setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
    }

    private void performRegistration() {
        String fName = ((EditText) findViewById(R.id.etFirstName)).getText().toString().trim();
        String lName = ((EditText) findViewById(R.id.etLastName)).getText().toString().trim();
        String email = ((EditText) findViewById(R.id.etRegEmail)).getText().toString().trim();
        String pass = ((EditText) findViewById(R.id.etRegPassword)).getText().toString();
        String passConfirm = ((EditText) findViewById(R.id.etRegPasswordConfirm)).getText().toString();

        if (fName.isEmpty() || lName.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            ToastUtils.showCenteredToast(this, "Please fill all fields");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            ToastUtils.showCenteredToast(this, "Please enter a valid email address");
            return;
        }
        if (pass.length() < 6) {
            ToastUtils.showCenteredToast(this, "Password must be at least 6 characters");
            return;
        }
        
        // Password complexity check: letters and numbers
        if (!pass.matches(".*[a-zA-Z].*") || !pass.matches(".*[0-9].*")) {
            ToastUtils.showCenteredToastLong(this, "Password must contain both letters and numbers");
            return;
        }

        if (!pass.equals(passConfirm)) {
            ToastUtils.showCenteredToast(this, "Passwords do not match");
            return;
        }

        ApiService api = RetrofitClient.getApiService(this);
        RegisterRequest request = new RegisterRequest(email, pass, fName, lName);
        api.register(request).enqueue(new Callback<RegisterResultResponse>() {
            @Override
            public void onResponse(Call<RegisterResultResponse> call, Response<RegisterResultResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    RegisterResultResponse body = response.body();
                    ToastUtils.showCenteredToast(RegisterActivity.this, body.getMessage());
                    if (body.getToken() != null) {
                        TokenManager tm = new TokenManager(RegisterActivity.this);
                        tm.saveToken(body.getToken());
                        if (body.getRefreshToken() != null) {
                            tm.saveRefreshToken(body.getRefreshToken());
                        }
                    }
                    startActivity(new Intent(RegisterActivity.this, MainActivity.class)
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    finish();
                } else {
                    String errorMsg = "Registration failed";
                    try {
                        if (response.errorBody() != null) {
                            String errorJson = response.errorBody().string();
                            org.json.JSONObject obj = new org.json.JSONObject(errorJson);
                            errorMsg = obj.optString("message", "Error " + response.code());
                        }
                    } catch (Exception e) {
                        errorMsg = "Error " + response.code();
                    }
                    ToastUtils.showCenteredToastLong(RegisterActivity.this, errorMsg);
                }
            }

            @Override
            public void onFailure(Call<RegisterResultResponse> call, Throwable t) {
                ToastUtils.showCenteredToastLong(RegisterActivity.this, "Network error: " + t.getMessage());
            }
        });
    }
}
