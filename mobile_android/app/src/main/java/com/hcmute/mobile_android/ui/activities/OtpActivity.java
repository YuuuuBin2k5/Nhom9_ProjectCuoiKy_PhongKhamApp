package com.hcmute.mobile_android.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

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
import com.hcmute.mobile_android.network.models.OtpVerifyRequest;
import com.hcmute.mobile_android.network.models.OtpVerifyResponse;
import com.hcmute.mobile_android.ui.widgets.ToothlyNumericKeypad;
import com.hcmute.mobile_android.util.IntentExtras;
import com.hcmute.mobile_android.util.PhoneDisplay;
import com.hcmute.mobile_android.util.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OtpActivity extends AppCompatActivity {

    private final char[] digits = new char[]{' ', ' ', ' ', ' ', ' ', ' '};
    private int cursor = 0;
    private String phone;
    private String purpose;
    private TextView[] boxes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_otp);

        phone = getIntent().getStringExtra(IntentExtras.EXTRA_PHONE);
        purpose = getIntent().getStringExtra(IntentExtras.EXTRA_OTP_PURPOSE);
        if (phone == null || purpose == null) {
            finish();
            return;
        }

        TextView sub = findViewById(R.id.tvOtpSubtitle);
        sub.setText(getString(R.string.otp_sent_to, PhoneDisplay.maskLastThree(phone)));

        boxes = new TextView[]{
                findViewById(R.id.otp0),
                findViewById(R.id.otp1),
                findViewById(R.id.otp2),
                findViewById(R.id.otp3),
                findViewById(R.id.otp4),
                findViewById(R.id.otp5),
        };

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        ToothlyNumericKeypad keypad = findViewById(R.id.keypad);
        keypad.setKeyListener(new ToothlyNumericKeypad.OnKeyListener() {
            @Override
            public void onDigit(int digit) {
                if (cursor < 6) {
                    digits[cursor] = (char) ('0' + digit);
                    cursor++;
                    refreshBoxes();
                }
            }

            @Override
            public void onBackspace() {
                if (cursor > 0) {
                    cursor--;
                    digits[cursor] = ' ';
                    refreshBoxes();
                }
            }
        });

        findViewById(R.id.tvResend).setOnClickListener(v -> resendOtp());

        findViewById(R.id.btnContinue).setOnClickListener(v -> submit());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
    }

    private void refreshBoxes() {
        for (int i = 0; i < 6; i++) {
            char c = digits[i];
            boxes[i].setText(c == ' ' ? "" : String.valueOf(c));
        }
    }

    private void resendOtp() {
        ApiService api = RetrofitClient.getApiService(this);
        api.requestOtp(new OtpRequest(phone, purpose)).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                Toast.makeText(OtpActivity.this, R.string.otp_resend, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                Toast.makeText(OtpActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submit() {
        if (cursor < 6) {
            Toast.makeText(this, "Enter 6 digits", Toast.LENGTH_SHORT).show();
            return;
        }
        String code = new String(digits).replace(" ", "");
        ApiService api = RetrofitClient.getApiService(this);
        api.verifyOtp(new OtpVerifyRequest(phone, code, purpose)).enqueue(new Callback<OtpVerifyResponse>() {
            @Override
            public void onResponse(Call<OtpVerifyResponse> call, Response<OtpVerifyResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(OtpActivity.this, "Invalid code", Toast.LENGTH_SHORT).show();
                    return;
                }
                OtpVerifyResponse body = response.body();
                if ("REGISTER".equals(purpose)) {
                    Intent i = new Intent(OtpActivity.this, RegisterActivity.class);
                    i.putExtra(IntentExtras.EXTRA_PHONE, phone);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                    finish();
                    return;
                }
                // LOGIN
                if (body.getToken() != null && !body.getToken().isEmpty()) {
                    new TokenManager(OtpActivity.this).saveToken(body.getToken());
                    startActivity(new Intent(OtpActivity.this, MainActivity.class)
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    finish();
                    return;
                }
                if (body.isNeedsRegistration()) {
                    Toast.makeText(OtpActivity.this, "No account yet — please sign up", Toast.LENGTH_LONG).show();
                    Intent i = new Intent(OtpActivity.this, PhoneLoginActivity.class);
                    i.putExtra(IntentExtras.EXTRA_REGISTER_FLOW, true);
                    startActivity(i);
                    finish();
                }
            }

            @Override
            public void onFailure(Call<OtpVerifyResponse> call, Throwable t) {
                Toast.makeText(OtpActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
