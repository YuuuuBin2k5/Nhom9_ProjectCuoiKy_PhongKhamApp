package com.hcmute.mobile_android.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import com.hcmute.mobile_android.util.ToastUtils;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.models.MessageResponse;
import com.hcmute.mobile_android.network.models.OtpRequest;
import com.hcmute.mobile_android.util.IntentExtras;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PhoneLoginActivity extends AppCompatActivity {

    private boolean registerFlow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_phone_login);

        registerFlow = getIntent().getBooleanExtra(IntentExtras.EXTRA_REGISTER_FLOW, false);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.tvSignUp).setOnClickListener(v -> {
            Intent i = new Intent(this, PhoneLoginActivity.class);
            i.putExtra(IntentExtras.EXTRA_REGISTER_FLOW, true);
            startActivity(i);
        });

        findViewById(R.id.btnContinue).setOnClickListener(v -> {
            String phone = ((android.widget.EditText) findViewById(R.id.etPhone)).getText().toString().trim();
            if (phone.isEmpty()) {
                ToastUtils.showCenteredToast(this, getString(R.string.mobile_number));
                return;
            }
            requestOtp(phone);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
    }

    private void requestOtp(String phone) {
        String purpose = registerFlow ? "REGISTER" : "LOGIN";
        ApiService api = RetrofitClient.getApiService(this);
        api.requestOtp(new OtpRequest(phone, purpose)).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if (response.isSuccessful()) {
                    Intent i = new Intent(PhoneLoginActivity.this, OtpActivity.class);
                    i.putExtra(IntentExtras.EXTRA_PHONE, phone);
                    i.putExtra(IntentExtras.EXTRA_OTP_PURPOSE, purpose);
                    startActivity(i);
                } else {
                    ToastUtils.showCenteredToast(PhoneLoginActivity.this, "Could not send code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                ToastUtils.showCenteredToastLong(PhoneLoginActivity.this, t.getMessage());
            }
        });
    }
}
