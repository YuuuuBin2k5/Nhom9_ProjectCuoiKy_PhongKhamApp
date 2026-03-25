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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MedicalRecordActivity extends AppCompatActivity {

    private RecyclerView rvMedicalHistory;
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

        rvMedicalHistory = findViewById(R.id.rvMedicalHistory);
        rvPastAppointments = findViewById(R.id.rvPastAppointments);

        rvMedicalHistory.setLayoutManager(new LinearLayoutManager(this));
        rvPastAppointments.setLayoutManager(new LinearLayoutManager(this));

        // Setup Actions
        findViewById(R.id.btnEditProfile).setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });

        // Load Header User Info
        loadUserInfo();

        // Setup Mock Data for Lists to match the UI precisely
        setupPastAppointments();
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
        List<HistoryItem> list = new ArrayList<>();
        
        String blood = (p.getBloodType() != null && !p.getBloodType().isEmpty()) ? p.getBloodType() : "Chưa xác định";
        list.add(new HistoryItem("NHÓM MÁU", blood, android.R.drawable.ic_dialog_info, "#F0FDFA", "#0D9488"));

        String allergies = (p.getAllergies() != null && !p.getAllergies().isEmpty()) ? p.getAllergies() : "Không có";
        list.add(new HistoryItem("DỊ ỨNG", allergies, android.R.drawable.ic_dialog_alert, "#FEF2F2", "#EF4444"));
        
        String conditions = (p.getUnderlyingConditions() != null && !p.getUnderlyingConditions().isEmpty()) ? p.getUnderlyingConditions() : "Không có";
        list.add(new HistoryItem("BỆNH LÝ NỀN", conditions, android.R.drawable.ic_menu_agenda, "#EFF6FF", "#3B82F6"));
        
        rvMedicalHistory.setAdapter(new HistoryAdapter(list));
    }

    private void setupPastAppointments() {
        ApiService api = RetrofitClient.getApiService(this);
        api.getMyMedicalRecords().enqueue(new Callback<List<com.hcmute.mobile_android.network.models.MedicalRecordResponse>>() {
            @Override
            public void onResponse(Call<List<com.hcmute.mobile_android.network.models.MedicalRecordResponse>> call, Response<List<com.hcmute.mobile_android.network.models.MedicalRecordResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<com.hcmute.mobile_android.network.models.MedicalRecordResponse> records = response.body();
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

            @Override
            public void onFailure(Call<List<com.hcmute.mobile_android.network.models.MedicalRecordResponse>> call, Throwable t) {
                // Handle failure
            }
        });
    }

    // --- Inner Models and Adapters ---

    private static class HistoryItem {
        String title, value, bgColor, iconColor;
        int iconRes;
        HistoryItem(String t, String v, int ir, String bgC, String icC) {
            title = t; value = v; iconRes = ir; bgColor = bgC; iconColor = icC;
        }
    }

    private static class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.Holder> {
        private final List<HistoryItem> items;
        HistoryAdapter(List<HistoryItem> items) { this.items = items; }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medical_history, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            HistoryItem item = items.get(position);
            holder.tvTitle.setText(item.title);
            holder.tvValue.setText(item.value);
            
            try { 
                holder.ivIcon.setImageResource(item.iconRes);
                holder.ivIcon.setBackground(createIconBg(item.bgColor));
                holder.ivIcon.setColorFilter(Color.parseColor(item.iconColor)); 
            } catch (Exception ignored) {}
        }

        private android.graphics.drawable.Drawable createIconBg(String color) {
            android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
            gd.setColor(Color.parseColor(color));
            gd.setCornerRadius(100f); // Circle
            return gd;
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class Holder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvValue;
            ImageView ivIcon;
            Holder(View v) {
                super(v);
                tvTitle = v.findViewById(R.id.tvHistoryTitle);
                tvValue = v.findViewById(R.id.tvHistoryValue);
                ivIcon = v.findViewById(R.id.ivHistoryIcon);
            }
        }
    }

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
