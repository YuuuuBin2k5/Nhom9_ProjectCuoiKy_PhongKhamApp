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

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.models.PatientMeResponse;

import java.util.ArrayList;
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

        // Load Header User Info
        loadUserInfo();

        // Setup Mock Data for Lists to match the UI precisely
        setupMedicalHistory();
        setupPastAppointments();
    }

    private void loadUserInfo() {
        TextView tvName = findViewById(R.id.tvPatientName);
        TextView tvInfo = findViewById(R.id.tvPatientInfo);

        ApiService api = RetrofitClient.getApiService(this);
        api.getPatientMe().enqueue(new Callback<PatientMeResponse>() {
            @Override
            public void onResponse(Call<PatientMeResponse> call, Response<PatientMeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PatientMeResponse p = response.body();
                    String fullName = ((p.getFirstName() != null ? p.getFirstName() : "") + " " +
                            (p.getLastName() != null ? p.getLastName() : "")).trim();
                    tvName.setText(fullName.isEmpty() ? "Bệnh nhân" : fullName);
                    // Mock Age and ID for now, since it might not be in the model
                    tvInfo.setText("Nam • 28 tuổi • ID: 10293847");
                }
            }

            @Override
            public void onFailure(Call<PatientMeResponse> call, Throwable t) {
                // Keep default layout text
            }
        });
    }

    private void setupMedicalHistory() {
        List<HistoryItem> list = new ArrayList<>();
        list.add(new HistoryItem("Dị ứng", "Không có", android.R.drawable.ic_menu_close_clear_cancel, "#FFEAEAEA", "#D32F2F"));
        list.add(new HistoryItem("Bệnh lý nền", "Tiểu đường Type 2", android.R.drawable.ic_menu_sort_by_size, "#FFF3E0", "#E64A19"));
        list.add(new HistoryItem("Thai kỳ", "Không có", android.R.drawable.ic_menu_info_details, "#E3F2FD", "#1976D2"));
        
        // Setup Adapter
        rvMedicalHistory.setAdapter(new HistoryAdapter(list));
    }

    private void setupPastAppointments() {
        List<PastApptItem> list = new ArrayList<>();
        list.add(new PastApptItem(
                "BS. Lê Minh Tâm", 
                "Chuyên khoa Nội tiết", 
                "14/10/2023", 
                "Kiểm tra định kỳ Tiểu đường Type 2, chỉ số HbA1c ổn định."
        ));
        
        rvPastAppointments.setAdapter(new PastApptAdapter(list, new PastApptAdapter.OnItemClickListener() {
            @Override
            public void onDetailClick(PastApptItem appt) {
                Intent intent = new Intent(MedicalRecordActivity.this, MedicalRecordDetailActivity.class);
                startActivity(intent);
            }

            @Override
            public void onPrescriptionClick(PastApptItem appt) {
                Intent intent = new Intent(MedicalRecordActivity.this, PrescriptionDetailActivity.class);
                intent.putExtra("doctorName", appt.doctorName);
                intent.putExtra("date", appt.date);
                startActivity(intent);
            }
        }));
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
            // Ignore strict resource checking for generic android icons used as placeholders
            try { holder.ivIcon.setImageResource(item.iconRes); } catch (Exception ignored) {}
            
            try { holder.ivIcon.setColorFilter(Color.parseColor(item.iconColor)); } catch (Exception ignored){}
            // Placeholder background color parsing isn't directly settable as a tint without gradient drawable
            // For simplicity, we skip full background tinting or assume background is just generic rounded.
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
        String doctorName, doctorSpec, date, diagnosis;
        PastApptItem(String n, String s, String d, String diag) {
            doctorName = n; doctorSpec = s; date = d; diagnosis = diag;
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
