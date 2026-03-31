package com.hcmute.mobile_android.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.models.MessageResponse;
import com.hcmute.mobile_android.network.models.OtpRequest;
import com.hcmute.mobile_android.util.IntentExtras;
import com.hcmute.mobile_android.util.ToastUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);

        etEmail = findViewById(R.id.etEmail);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnSendOtp).setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if (email.isEmpty()) {
                ToastUtils.showCenteredToast(this, "Vui lòng nhập Email");
                return;
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                ToastUtils.showCenteredToast(this, "Email không hợp lệ");
                return;
            }
            sendOtp(email);
        });
    }

    private void sendOtp(String email) {
        ApiService api = RetrofitClient.getApiService(this);
        OtpRequest request = new OtpRequest(null, email, "FORGOT_PASSWORD");
        
        api.requestOtp(request).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if (response.isSuccessful()) {
                    Intent intent = new Intent(ForgotPasswordActivity.this, OtpActivity.class);
                    intent.putExtra(IntentExtras.EXTRA_EMAIL, email);
                    intent.putExtra(IntentExtras.EXTRA_OTP_PURPOSE, "FORGOT_PASSWORD");
                    startActivity(intent);
                } else if (response.code() == 404) {
                    ToastUtils.showCenteredToast(ForgotPasswordActivity.this, "Email chưa được đăng ký");
                } else {
                    ToastUtils.showCenteredToast(ForgotPasswordActivity.this, "Lỗi: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                ToastUtils.showCenteredToast(ForgotPasswordActivity.this, "Lỗi kết nối: " + t.getMessage());
            }
        });
    }
}
