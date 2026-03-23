package com.hcmute.mobile_android.ui.activities;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.hcmute.mobile_android.R;

public class AppointmentDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_detail);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        long id = getIntent().getLongExtra("appointmentId", -1);
        String datetime = getIntent().getStringExtra("datetime");
        String serviceName = getIntent().getStringExtra("serviceName");
        String doctorName = getIntent().getStringExtra("doctorName");
        String status = getIntent().getStringExtra("status");

        ((TextView) findViewById(R.id.tvApptId)).setText(String.valueOf(id));
        ((TextView) findViewById(R.id.tvApptDatetime)).setText(datetime != null ? datetime : "");
        ((TextView) findViewById(R.id.tvApptService)).setText(serviceName != null ? serviceName : "");
        ((TextView) findViewById(R.id.tvApptDoctor)).setText(doctorName != null ? doctorName : "");
        ((TextView) findViewById(R.id.tvApptStatus)).setText(status != null ? status : "");
    }
}
