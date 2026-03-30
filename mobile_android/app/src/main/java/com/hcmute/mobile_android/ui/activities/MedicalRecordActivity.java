package com.hcmute.mobile_android.ui.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.models.PatientMeResponse;
import com.hcmute.mobile_android.ui.activities.LoginActivity;
import com.hcmute.mobile_android.util.TokenManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MedicalRecordActivity extends AppCompatActivity {

    private TextView tvBloodVal, tvAllergyVal, tvConditionVal;
    private View layoutEmptyHistory;
    private RecyclerView rvPastAppointments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_medical_record);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.appBarLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        tvBloodVal = findViewById(R.id.tvBloodVal);
        tvAllergyVal = findViewById(R.id.tvAllergyVal);
        tvConditionVal = findViewById(R.id.tvConditionVal);
        layoutEmptyHistory = findViewById(R.id.layoutEmptyHistory);
        rvPastAppointments = findViewById(R.id.rvPastAppointments);

        rvPastAppointments.setLayoutManager(new LinearLayoutManager(this));

        // Setup Actions
        findViewById(R.id.btnEditProfile).setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });

        toolbar.inflateMenu(R.menu.menu_medical_record);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            } else if (item.getItemId() == R.id.action_logout) {
                new TokenManager(this).clearToken();
                startActivity(new Intent(this, LoginActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                finish();
                return true;
            }
            return false;
        });

        // Setup Mock Data for Lists to match the UI precisely
        setupPastAppointments();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Tải hồ sơ mỗi khi vào màn (và sau khi quay lại từ ProfileActivity).
        loadUserInfo();
    }

    private void loadUserInfo() {
        TextView tvName = findViewById(R.id.tvPatientName);
        TextView tvInfo = findViewById(R.id.tvPatientInfo);
        ImageView ivProfile = findViewById(R.id.ivAvatar);

        ApiService api = RetrofitClient.getApiService(this);
        api.getPatientMe().enqueue(new Callback<PatientMeResponse>() {
            @Override
            public void onResponse(Call<PatientMeResponse> call, Response<PatientMeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PatientMeResponse p = response.body();
                    String firstName = p.getFirstName() != null ? p.getFirstName() : "";
                    String lastName = p.getLastName() != null ? p.getLastName() : "";
                    String fullName = (firstName + " " + lastName).trim();
                    tvName.setText(fullName.isEmpty() ? "Bệnh nhân" : fullName);
                    
                    String gender = p.getGender() != null ? p.getGender() : "Chưa cập nhật";
                    String dob = p.getDob() != null ? p.getDob() : "";
                    String ageStr = "";
                    if (!dob.isEmpty()) {
                        try {
                            String[] parts = dob.split("-");
                            int birthYear = Integer.parseInt(parts[0]);
                            int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
                            ageStr = " • " + (currentYear - birthYear) + " tuổi";
                        } catch (Exception ignored) {}
                    }
                    tvInfo.setText(gender + ageStr);
                    
                    if (p.getAvatarUrl() != null && !p.getAvatarUrl().isEmpty()) {
                        Glide.with(MedicalRecordActivity.this)
                                .load(p.getAvatarUrl())
                                .placeholder(R.drawable.ic_doctor)
                                .into(ivProfile);
                    }
                    
                    setupMedicalHistory(p);
                }
            }
            @Override
            public void onFailure(Call<PatientMeResponse> call, Throwable t) {}
        });
    }

    private void setupMedicalHistory(PatientMeResponse p) {
        String blood = (p.getBloodType() != null && !p.getBloodType().isEmpty()) ? p.getBloodType() : "--";
        String allergies = (p.getAllergies() != null && !p.getAllergies().isEmpty()) ? p.getAllergies() : "Không";
        String conditions = (p.getUnderlyingConditions() != null && !p.getUnderlyingConditions().isEmpty()) ? p.getUnderlyingConditions() : "Không";

        tvBloodVal.setText(blood);
        tvAllergyVal.setText(allergies);
        tvConditionVal.setText(conditions);
    }

    private void setupPastAppointments() {
        ApiService api = RetrofitClient.getApiService(this);
        api.getMyMedicalRecords().enqueue(new Callback<List<com.hcmute.mobile_android.network.models.MedicalRecordResponse>>() {
            @Override
            public void onResponse(Call<List<com.hcmute.mobile_android.network.models.MedicalRecordResponse>> call, Response<List<com.hcmute.mobile_android.network.models.MedicalRecordResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<com.hcmute.mobile_android.network.models.MedicalRecordResponse> records = response.body();
                    
                    if (records.isEmpty()) {
                        if (layoutEmptyHistory != null) layoutEmptyHistory.setVisibility(View.VISIBLE);
                        rvPastAppointments.setVisibility(View.GONE);
                    } else {
                        if (layoutEmptyHistory != null) layoutEmptyHistory.setVisibility(View.GONE);
                        rvPastAppointments.setVisibility(View.VISIBLE);

                        List<PastApptItem> list = new ArrayList<>();
                        for (com.hcmute.mobile_android.network.models.MedicalRecordResponse r : records) {
                            list.add(new PastApptItem(
                                    r.getId(),
                                    r.getDoctorName() != null ? r.getDoctorName() : "Bác sĩ",
                                    r.getDoctorSpecialty() != null ? r.getDoctorSpecialty() : "Nha sĩ",
                                    r.getDate() != null ? r.getDate().split("T")[0] : "",
                                    r.getDiagnosis() != null ? r.getDiagnosis() : "Chưa có chẩn đoán"
                            ));
                        }

                        rvPastAppointments.setAdapter(new PastApptAdapter(list, new PastApptAdapter.OnItemClickListener() {
                            @Override
                            public void onDetailClick(PastApptItem appt) {
                                Intent intent = new Intent(MedicalRecordActivity.this, MedicalRecordDetailActivity.class);
                                intent.putExtra("recordId", appt.id);
                                startActivity(intent);
                            }

                            @Override
                            public void onPrescriptionClick(PastApptItem appt) {
                                Intent intent = new Intent(MedicalRecordActivity.this, PrescriptionDetailActivity.class);
                                intent.putExtra("recordId", appt.id);
                                intent.putExtra("doctorName", appt.doctorName);
                                intent.putExtra("date", appt.date);
                                startActivity(intent);
                            }
                        }));
                    }
                }
            }

            @Override
            public void onFailure(Call<List<com.hcmute.mobile_android.network.models.MedicalRecordResponse>> call, Throwable t) {
            }
        });
    }

    // --- Inner Models and Adapters ---

    private static class PastApptItem {
        Long id;
        String doctorName, doctorSpec, date, diagnosis;
        PastApptItem(Long id, String n, String s, String d, String diag) {
            this.id = id; doctorName = n; doctorSpec = s; date = d; diagnosis = diag;
        }
    }

    private static class PastApptAdapter extends RecyclerView.Adapter<PastApptAdapter.Holder> {
        private final List<PastApptItem> items;
        private final OnItemClickListener listener;

        interface OnItemClickListener { 
            void onDetailClick(PastApptItem item); 
            void onPrescriptionClick(PastApptItem item);
        }

        PastApptAdapter(List<PastApptItem> items, OnItemClickListener listener) {
            this.items = items;
            this.listener = listener;
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_past_appointment, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            PastApptItem item = items.get(position);
            holder.tvDocName.setText(item.doctorName);
            holder.tvDocSpec.setText(item.doctorSpec);
            holder.tvDate.setText(item.date);
            holder.tvDiagnosis.setText(item.diagnosis);
            
            holder.btnDetails.setOnClickListener(v -> listener.onDetailClick(item));
            holder.btnPrescription.setOnClickListener(v -> listener.onPrescriptionClick(item));
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class Holder extends RecyclerView.ViewHolder {
            TextView tvDocName, tvDocSpec, tvDate, tvDiagnosis;
            MaterialButton btnDetails, btnPrescription;

            Holder(View v) {
                super(v);
                tvDocName = v.findViewById(R.id.tvDocName);
                tvDocSpec = v.findViewById(R.id.tvDocSpec);
                tvDate = v.findViewById(R.id.tvDate);
                tvDiagnosis = v.findViewById(R.id.tvDiagnosis);
                btnDetails = v.findViewById(R.id.btnDetails);
                btnPrescription = v.findViewById(R.id.btnPrescription);
            }
        }
    }
}
