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
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            return;
        }
        if (pass.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!pass.equals(passConfirm)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService api = RetrofitClient.getApiService(this);
        RegisterRequest request = new RegisterRequest(email, pass, fName, lName);
        api.register(request).enqueue(new Callback<RegisterResultResponse>() {
            @Override
            public void onResponse(Call<RegisterResultResponse> call, Response<RegisterResultResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    RegisterResultResponse body = response.body();
                    Toast.makeText(RegisterActivity.this, body.getMessage(), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(RegisterActivity.this, "Failed: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RegisterResultResponse> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
