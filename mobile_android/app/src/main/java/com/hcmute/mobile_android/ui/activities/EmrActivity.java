package com.hcmute.mobile_android.ui.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.models.MedicalRecordResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmrActivity extends AppCompatActivity {

    private ProgressBar progress;
    private RecyclerView rvTimeline;
    private TextView tvEmpty;
    private EmrAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_emr);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.appBarLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        progress = findViewById(R.id.progress);
        rvTimeline = findViewById(R.id.rvTimeline);
        tvEmpty = findViewById(R.id.tvEmpty);
        
        rvTimeline.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EmrAdapter(new ArrayList<>(), new EmrAdapter.OnItemClickListener() {
            @Override
            public void onDetailClick(MedicalRecordResponse item) {
                Intent intent = new Intent(EmrActivity.this, MedicalRecordDetailActivity.class);
                intent.putExtra("medicalRecordId", item.getId());
                startActivity(intent);
            }

            @Override
            public void onPrescriptionClick(MedicalRecordResponse item) {
                if (item.getPrescription() == null || item.getPrescription().getId() == null) {
                    Toast.makeText(EmrActivity.this, "Không có đơn thuốc", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(EmrActivity.this, PrescriptionDetailActivity.class);
                intent.putExtra("doctorName", item.getDoctorName());
                intent.putExtra("date", formatDate(item.getDate()));
                intent.putExtra("prescriptionId", item.getPrescription().getId());
                startActivity(intent);
            }
        });
        rvTimeline.setAdapter(adapter);

        loadEmrData();
    }

    private void loadEmrData() {
        progress.setVisibility(View.VISIBLE);
        rvTimeline.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.GONE);

        ApiService api = RetrofitClient.getApiService(this);
        api.getMyMedicalRecords().enqueue(new Callback<com.hcmute.mobile_android.network.models.PagedResponse<MedicalRecordResponse>>() {
            @Override
            public void onResponse(Call<com.hcmute.mobile_android.network.models.PagedResponse<MedicalRecordResponse>> call, Response<com.hcmute.mobile_android.network.models.PagedResponse<MedicalRecordResponse>> response) {
                progress.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<MedicalRecordResponse> list = response.body().getContent();
                    if (list == null || list.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                    } else {
                        adapter.setItems(list);
                        rvTimeline.setVisibility(View.VISIBLE);
                    }
                } else {
                    tvEmpty.setText("Không thể tải dữ liệu bệnh án");
                    tvEmpty.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<com.hcmute.mobile_android.network.models.PagedResponse<MedicalRecordResponse>> call, Throwable t) {
                progress.setVisibility(View.GONE);
                tvEmpty.setText("Lỗi kết nối: " + t.getMessage());
                tvEmpty.setVisibility(View.VISIBLE);
            }
        }       );
    }

    private static String formatDate(String iso) {
        if (iso == null || iso.length() < 10) return "";
        return iso.substring(0, 10);
    }

    private static class EmrAdapter extends RecyclerView.Adapter<EmrAdapter.Holder> {
        private List<MedicalRecordResponse> items;
        private final OnItemClickListener listener;

        interface OnItemClickListener {
            void onDetailClick(MedicalRecordResponse item);
            void onPrescriptionClick(MedicalRecordResponse item);
        }

        EmrAdapter(List<MedicalRecordResponse> items, OnItemClickListener listener) {
            this.items = items;
            this.listener = listener;
        }

        void setItems(List<MedicalRecordResponse> items) {
            this.items = items != null ? items : new ArrayList<>();
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_emr_timeline, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            MedicalRecordResponse item = items.get(position);
            holder.tvDate.setText(formatDate(item.getDate()));
            holder.tvStatus.setText("HOÀN THÀNH");
            
            holder.tvDiagnosisTitle.setText(item.getDiagnosis());
            holder.tvDoctorName.setText(item.getDoctorName());
            holder.tvDocSpec.setText(item.getDoctorSpecialty());
            
            if (item.getPrescription() != null && item.getPrescription().getDetails() != null && !item.getPrescription().getDetails().isEmpty()) {
                List<MedicalRecordResponse.PrescriptionDetail> details = item.getPrescription().getDetails();
                holder.tvDrug1.setText(details.get(0).getMedicineName());
                holder.tvDrug1.setVisibility(View.VISIBLE);
                
                if (details.size() > 1) {
                    holder.tvDrug2.setText(details.get(1).getMedicineName());
                    holder.tvDrug2.setVisibility(View.VISIBLE);
                } else {
                    holder.tvDrug2.setVisibility(View.GONE);
                }
                
                if (details.size() > 2) {
                    holder.tvDrugExtra.setText("+" + (details.size() - 2) + " khác");
                    holder.tvDrugExtra.setVisibility(View.VISIBLE);
                } else {
                    holder.tvDrugExtra.setVisibility(View.GONE);
                }
            } else {
                holder.tvDrug1.setVisibility(View.GONE);
                holder.tvDrug2.setVisibility(View.GONE);
                holder.tvDrugExtra.setVisibility(View.GONE);
            }
            
            holder.ivXrayThumbnail.setVisibility(View.GONE); // No X-ray thumb support yet in API

            holder.btnDetails.setOnClickListener(v -> listener.onDetailClick(item));
            holder.btnPrescription.setOnClickListener(v -> listener.onPrescriptionClick(item));
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class Holder extends RecyclerView.ViewHolder {
            TextView tvDate, tvStatus, tvDiagnosisTitle, tvDoctorName, tvDocSpec;
            TextView tvDrug1, tvDrug2, tvDrugExtra;
            ImageView ivXrayThumbnail;
            MaterialCardView cvTimelineDot;
            MaterialButton btnDetails, btnPrescription;

            Holder(View v) {
                super(v);
                tvDate = v.findViewById(R.id.tvDate);
                tvStatus = v.findViewById(R.id.tvStatus);
                tvDiagnosisTitle = v.findViewById(R.id.tvDiagnosisTitle);
                tvDoctorName = v.findViewById(R.id.tvDoctorName);
                tvDocSpec = v.findViewById(R.id.tvDocSpec);
                tvDrug1 = v.findViewById(R.id.tvDrug1);
                tvDrug2 = v.findViewById(R.id.tvDrug2);
                tvDrugExtra = v.findViewById(R.id.tvDrugExtra);
                ivXrayThumbnail = v.findViewById(R.id.ivXrayThumbnail);
                cvTimelineDot = v.findViewById(R.id.cvTimelineDot);
                btnDetails = v.findViewById(R.id.btnDetails);
                btnPrescription = v.findViewById(R.id.btnPrescription);
            }
        }
    }
}
