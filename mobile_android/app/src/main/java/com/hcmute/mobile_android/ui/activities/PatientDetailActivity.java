package com.hcmute.mobile_android.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.adapters.MedicalHistoryAdapter;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.models.MedicalRecordResponse;
import com.hcmute.mobile_android.network.models.PatientInfo;
import com.hcmute.mobile_android.ui.activities.staff.DoctorWorkflowActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PatientDetailActivity extends AppCompatActivity {

    public static final String EXTRA_PATIENT_INFO = "patient_info";
    public static final String EXTRA_FROM_WORKFLOW = "from_workflow";

    private TextView tvPatientName;
    private TextView tvPatientPhone;
    private TextView tvPatientDob;
    private TextView tvPatientGender;
    private TextView tvPatientAddress;
    private TextView tvPatientNotes;
    private MaterialCardView cardPatientNotes;
    private RecyclerView rvMedicalHistory;
    private ProgressBar progressBar;
    private TextView tvEmptyHistory;
    private MaterialButton btnStartExamination;
    private MaterialButton btnViewInvoices;

    private PatientInfo patientInfo;
    private MedicalHistoryAdapter historyAdapter;
    private ApiService apiService;
    private boolean fromWorkflow = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_detail);

        // Get patient info from intent
        patientInfo = (PatientInfo) getIntent().getSerializableExtra(EXTRA_PATIENT_INFO);
        fromWorkflow = getIntent().getBooleanExtra(EXTRA_FROM_WORKFLOW, false);

        if (patientInfo == null) {
            Toast.makeText(this, "Không tìm thấy thông tin bệnh nhân", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        apiService = RetrofitClient.getApiService(this);

        initViews();
        displayPatientInfo();
        loadMedicalHistory();
    }

    private void initViews() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        tvPatientName = findViewById(R.id.tvPatientName);
        tvPatientPhone = findViewById(R.id.tvPatientPhone);
        tvPatientDob = findViewById(R.id.tvPatientDob);
        tvPatientGender = findViewById(R.id.tvPatientGender);
        tvPatientAddress = findViewById(R.id.tvPatientAddress);
        tvPatientNotes = findViewById(R.id.tvPatientNotes);
        cardPatientNotes = findViewById(R.id.cardPatientNotes);
        
        rvMedicalHistory = findViewById(R.id.rvMedicalHistory);
        progressBar = findViewById(R.id.progressBar);
        tvEmptyHistory = findViewById(R.id.tvEmptyHistory);
        btnStartExamination = findViewById(R.id.btnStartExamination);
        btnViewInvoices = findViewById(R.id.btnViewInvoices);

        // Setup RecyclerView
        historyAdapter = new MedicalHistoryAdapter();
        rvMedicalHistory.setLayoutManager(new LinearLayoutManager(this));
        rvMedicalHistory.setAdapter(historyAdapter);

        // Button clicks
        btnStartExamination.setOnClickListener(v -> startExamination());
        btnViewInvoices.setOnClickListener(v -> viewInvoices());

        // If opened from workflow, change button text
        if (fromWorkflow) {
            btnStartExamination.setText("Quay Lại");
            btnStartExamination.setIcon(null);
        }
    }

    private void displayPatientInfo() {
        tvPatientName.setText(patientInfo.getFullName());
        tvPatientPhone.setText(patientInfo.getPhone() != null ? patientInfo.getPhone() : "Chưa có");
        
        // Format date of birth with age
        if (patientInfo.getDateOfBirth() != null) {
            String dobWithAge = patientInfo.getDateOfBirth();
            int age = calculateAge(patientInfo.getDateOfBirth());
            if (age > 0) {
                dobWithAge += " (" + age + " tuổi)";
            }
            tvPatientDob.setText(dobWithAge);
        } else {
            tvPatientDob.setText("Chưa có");
        }

        tvPatientGender.setText(patientInfo.getGenderDisplay());
        tvPatientAddress.setText(patientInfo.getAddress() != null ? patientInfo.getAddress() : "Chưa có");

        // TODO: Load patient notes from API when available
        // For now, hide the notes card
        cardPatientNotes.setVisibility(View.GONE);
    }

    private int calculateAge(String dateOfBirth) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date birthDate = sdf.parse(dateOfBirth);
            if (birthDate != null) {
                Calendar birth = Calendar.getInstance();
                birth.setTime(birthDate);
                Calendar today = Calendar.getInstance();
                
                int age = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR);
                if (today.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
                    age--;
                }
                return age;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void loadMedicalHistory() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmptyHistory.setVisibility(View.GONE);
        rvMedicalHistory.setVisibility(View.GONE);

        apiService.getPatientMedicalRecords(patientInfo.getId()).enqueue(new Callback<List<MedicalRecordResponse>>() {
            @Override
            public void onResponse(Call<List<MedicalRecordResponse>> call, Response<List<MedicalRecordResponse>> response) {
                progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null) {
                    List<MedicalRecordResponse> records = response.body();
                    if (records.isEmpty()) {
                        tvEmptyHistory.setVisibility(View.VISIBLE);
                    } else {
                        rvMedicalHistory.setVisibility(View.VISIBLE);
                        historyAdapter.setRecords(records);
                    }
                } else {
                    tvEmptyHistory.setVisibility(View.VISIBLE);
                    tvEmptyHistory.setText("Không thể tải lịch sử khám");
                }
            }

            @Override
            public void onFailure(Call<List<MedicalRecordResponse>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                tvEmptyHistory.setVisibility(View.VISIBLE);
                tvEmptyHistory.setText("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void startExamination() {
        if (fromWorkflow) {
            // Just go back to workflow
            finish();
        } else {
            // Mark patient as visited
            markPatientVisited();

            // Start DoctorWorkflowActivity
            Intent intent = new Intent(this, DoctorWorkflowActivity.class);
            intent.putExtra("patient_info", patientInfo);
            intent.putExtra("from_detail", true);
            startActivity(intent);
            finish();
        }
    }

    private void viewInvoices() {
        Intent intent = new Intent(this, InvoiceListActivity.class);
        intent.putExtra("patient_id", patientInfo.getId());
        intent.putExtra("patient_name", patientInfo.getFullName());
        startActivity(intent);
    }

    private void markPatientVisited() {
        SharedPreferences prefs = getSharedPreferences("doctor_visits", MODE_PRIVATE);
        
        // Get current doctor ID from shared preferences
        SharedPreferences authPrefs = getSharedPreferences("auth", MODE_PRIVATE);
        Long doctorId = authPrefs.getLong("user_id", 0L);
        
        String key = "doctor_" + doctorId + "_patient_" + patientInfo.getId() + "_visited";
        prefs.edit().putBoolean(key, true).apply();
    }
}
