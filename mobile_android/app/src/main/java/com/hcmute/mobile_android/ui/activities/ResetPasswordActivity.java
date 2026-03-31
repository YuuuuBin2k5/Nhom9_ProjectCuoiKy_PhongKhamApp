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
import com.hcmute.mobile_android.network.models.ResetPasswordRequest;
import com.hcmute.mobile_android.util.IntentExtras;
import com.hcmute.mobile_android.util.ToastUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResetPasswordActivity extends AppCompatActivity {

    private String email;
    private String otp;
    private EditText etNewPassword;
    private EditText etConfirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reset_password);

        email = getIntent().getStringExtra(IntentExtras.EXTRA_EMAIL);
        otp = getIntent().getStringExtra(IntentExtras.EXTRA_OTP);

        if (email == null || otp == null) {
            finish();
            return;
        }

        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        findViewById(R.id.btnResetPassword).setOnClickListener(v -> {
            String pass = etNewPassword.getText().toString().trim();
            String confirm = etConfirmPassword.getText().toString().trim();

            if (pass.length() < 6) {
                ToastUtils.showCenteredToast(this, "Mật khẩu phải từ 6 ký tự");
                return;
            }
            if (!pass.equals(confirm)) {
                ToastUtils.showCenteredToast(this, "Mật khẩu xác nhận không khớp");
                return;
            }

            resetPassword(pass);
        });
    }

    private void resetPassword(String newPassword) {
        ApiService api = RetrofitClient.getApiService(this);
        ResetPasswordRequest request = new ResetPasswordRequest(null, email, otp, newPassword);

        api.resetPassword(request).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if (response.isSuccessful()) {
                    ToastUtils.showCenteredToastLong(ResetPasswordActivity.this, "Đặt lại mật khẩu thành công! Vui lòng đăng nhập lại.");
                    Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    ToastUtils.showCenteredToast(ResetPasswordActivity.this, "Lỗi: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                ToastUtils.showCenteredToast(ResetPasswordActivity.this, "Lỗi kết nối: " + t.getMessage());
            }
        });
    }
}
