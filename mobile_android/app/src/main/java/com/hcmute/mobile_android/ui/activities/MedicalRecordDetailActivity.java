package com.hcmute.mobile_android.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.models.MedicalRecordDetailResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MedicalRecordDetailActivity extends AppCompatActivity {

    private ProgressBar progress;
    private Long medicalRecordId;
    private Long prescriptionId = 0L;
    private String doctorNameStr = "";
    private String dateStr = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_medical_record_detail);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.appBarLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        progress = findViewById(R.id.progress);
        medicalRecordId = getIntent().getLongExtra("medicalRecordId", -1L);

        findViewById(R.id.btnXemDonThuoc).setOnClickListener(v -> {
            if (prescriptionId != 0L) {
                Intent intent = new Intent(this, PrescriptionDetailActivity.class);
                intent.putExtra("doctorName", doctorNameStr);
                intent.putExtra("date", dateStr);
                intent.putExtra("prescriptionId", prescriptionId);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Không có đơn thuốc cho bệnh án này", Toast.LENGTH_SHORT).show();
            }
        });

        if (medicalRecordId != -1L) {
            loadMedicalRecordDetail();
        }
    }

    private void loadMedicalRecordDetail() {
        if (progress != null) progress.setVisibility(View.VISIBLE);
        
        ApiService api = RetrofitClient.getApiService(this);
        api.getMedicalRecordDetail(medicalRecordId).enqueue(new Callback<MedicalRecordDetailResponse>() {
            @Override
            public void onResponse(Call<MedicalRecordDetailResponse> call, Response<MedicalRecordDetailResponse> response) {
                if (progress != null) progress.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    displayData(response.body());
                } else {
                    Toast.makeText(MedicalRecordDetailActivity.this, "Không thể tải chi tiết bệnh án", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MedicalRecordDetailResponse> call, Throwable t) {
                if (progress != null) progress.setVisibility(View.GONE);
                Toast.makeText(MedicalRecordDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayData(MedicalRecordDetailResponse data) {
        doctorNameStr = data.getDoctorName();
        dateStr = formatDate(data.getDate());
        prescriptionId = data.getPrescriptionId();

        setText(R.id.tvExamDate, dateStr);
        setText(R.id.tvDoctorName, doctorNameStr);
        setText(R.id.tvDiagnosis, data.getDiagnosis());
        setText(R.id.tvSymptoms, data.getSymptoms());
        
        String vitals = "Huyết áp: " + (data.getBloodPressure() != null ? data.getBloodPressure() : "N/A") + 
                        " | Nhịp tim: " + (data.getHeartRate() != null ? data.getHeartRate() + " bpm" : "N/A");
        setText(R.id.tvVitals, vitals);
        
        setText(R.id.tvAdvice, data.getAdvice());

        LinearLayout container = findViewById(R.id.treatmentDetailsContainer);
        if (container != null && data.getDetails() != null) {
            container.removeAllViews();
            for (MedicalRecordDetailResponse.Detail d : data.getDetails()) {
                View row = LayoutInflater.from(this).inflate(android.R.layout.simple_list_item_2, container, false);
                TextView t1 = row.findViewById(android.R.id.text1);
                TextView t2 = row.findViewById(android.R.id.text2);
                t1.setText(d.getServiceName() + (d.getToothNumber() != null ? " (Răng " + d.getToothNumber() + ")" : ""));
                t2.setText(d.getNote());
                container.addView(row);
            }
        }
    }

    private void setText(int id, String text) {
        TextView tv = findViewById(id);
        if (tv != null) tv.setText(text != null ? text : "");
    }

    private static String formatDate(String iso) {
        if (iso == null || iso.length() < 10) return "";
        return iso.substring(0, 10);
    }
}
