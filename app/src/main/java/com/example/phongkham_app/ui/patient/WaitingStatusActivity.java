package com.example.phongkham_app.ui.patient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.phongkham_app.R;
import com.example.phongkham_app.service.QueueBackgroundService;

public class WaitingStatusActivity extends AppCompatActivity {

    private TextView tvQueueNumber;
    private TextView tvWaitTime;
    private TextView tvStatusLabel;
    private Button btnBack;

    private final BroadcastReceiver queueReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String queueNumber = intent.getStringExtra("QUEUE_NUMBER");
            int waitTime = intent.getIntExtra("WAIT_TIME", 0);
            String status = intent.getStringExtra("STATUS");
            
            if (tvQueueNumber != null && queueNumber != null) tvQueueNumber.setText(queueNumber);
            if (tvStatusLabel != null && status != null) tvStatusLabel.setText(status);
            
            if (tvWaitTime != null) {
                if (waitTime <= 0) {
                    tvWaitTime.setText("Mời bạn vào phòng!");
                    tvWaitTime.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                } else {
                    tvWaitTime.setText(waitTime + " Phút");
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_status);

        tvQueueNumber = findViewById(R.id.tvQueueNumber);
        tvWaitTime = findViewById(R.id.tvWaitTime);
        tvStatusLabel = findViewById(R.id.tvStatusLabel);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        // Start Service
        Intent serviceIntent = new Intent(this, QueueBackgroundService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(queueReceiver, new IntentFilter("com.example.phongkham_app.QUEUE_UPDATE"), Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(queueReceiver, new IntentFilter("com.example.phongkham_app.QUEUE_UPDATE"));
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            unregisterReceiver(queueReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }
}
