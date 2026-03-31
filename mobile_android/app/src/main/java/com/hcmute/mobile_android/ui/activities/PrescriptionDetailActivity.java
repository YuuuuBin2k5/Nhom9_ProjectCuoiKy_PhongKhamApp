package com.hcmute.mobile_android.ui.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.models.MedicineItem;
import com.hcmute.mobile_android.network.models.PrescriptionResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PrescriptionDetailActivity extends AppCompatActivity {

    private RecyclerView rvDrugs;
    private ProgressBar progress;
    private TextView tvEmpty;
    private TextView tvDoctorName;
    private TextView tvDate;
    private DrugAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_prescription_detail);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.appBarLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        tvDoctorName = findViewById(R.id.tvDoctorName);
        tvDate = findViewById(R.id.tvDate);
        progress = findViewById(R.id.progress);
        tvEmpty = findViewById(R.id.tvEmpty);
        rvDrugs = findViewById(R.id.rvDrugs);

        rvDrugs.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DrugAdapter(new ArrayList<MedicineItem>());

        rvDrugs.setAdapter(adapter);

        // Get intent extras
        String initialDocName = getIntent().getStringExtra("doctorName");
        String initialDate = getIntent().getStringExtra("date");
        Long prescriptionId = getIntent().getLongExtra("prescriptionId", -1L);

        if (initialDocName != null) tvDoctorName.setText(initialDocName);
        if (initialDate != null && tvDate != null) tvDate.setText(initialDate);

        findViewById(R.id.btnDownloadPDF).setOnClickListener(v -> {
            Toast.makeText(this, "Đang tải xuống PDF...", Toast.LENGTH_SHORT).show();
        });

        if (prescriptionId != -1L) {
            loadPrescriptionDetail(prescriptionId);
        } else {
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText("Không tìm thấy thông tin đơn thuốc");
        }
    }

    private void loadPrescriptionDetail(Long id) {
        progress.setVisibility(View.VISIBLE);
        rvDrugs.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.GONE);

        ApiService api = RetrofitClient.getApiService(this);
        api.getPrescriptionDetail(id).enqueue(new Callback<PrescriptionResponse>() {
            @Override
            public void onResponse(Call<PrescriptionResponse> call, Response<PrescriptionResponse> response) {
                progress.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    PrescriptionResponse data = response.body();
                    tvDoctorName.setText(data.getDoctorName());
                    if (tvDate != null) tvDate.setText(formatDate(data.getDate()));
                    
                    if (data.getDetails() == null || data.getDetails().isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        tvEmpty.setText("Đơn thuốc trống");
                    } else {
                        adapter.setItems(data.getDetails());
                        rvDrugs.setVisibility(View.VISIBLE);
                    }
                } else {
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("Không thể tải chi tiết đơn thuốc");
                }
            }

            @Override
            public void onFailure(Call<PrescriptionResponse> call, Throwable t) {
                progress.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
                tvEmpty.setText("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private static String formatDate(String iso) {
        if (iso == null || iso.length() < 10) return "";
        return iso.substring(0, 10);
    }

    private static class DrugAdapter extends RecyclerView.Adapter<DrugAdapter.Holder> {
        private List<MedicineItem> items;
        DrugAdapter(List<MedicineItem> items) { this.items = items; }

        void setItems(List<MedicineItem> items) {
            this.items = items != null ? items : new ArrayList<>();
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_prescription_drug, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            MedicineItem item = items.get(position);
            holder.tvName.setText(item.getMedicineName());
            holder.tvDosage.setText(item.getDosage());
            
            String instr = (item.getFrequency() != null ? item.getFrequency() : "") + " " + (item.getDuration() != null ? item.getDuration() : "");
            holder.tvInstruction.setText(instr.trim());
            holder.swReminder.setChecked(true);
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class Holder extends RecyclerView.ViewHolder {
            TextView tvName, tvDosage, tvInstruction;
            SwitchCompat swReminder;
            Holder(View v) {
                super(v);
                tvName = v.findViewById(R.id.tvDrugName);
                tvDosage = v.findViewById(R.id.tvDrugDosage);
                tvInstruction = v.findViewById(R.id.tvDrugInstruction);
                swReminder = v.findViewById(R.id.swReminder);
            }
        }
    }
}
