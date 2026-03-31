package com.hcmute.mobile_android.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.models.MedicalRecordResponse;

import java.util.ArrayList;
import java.util.List;

public class MedicalHistoryAdapter extends RecyclerView.Adapter<MedicalHistoryAdapter.ViewHolder> {

    private List<MedicalRecordResponse> records;
    private java.util.Set<Long> expandedRecordIds = new java.util.HashSet<>();

    public MedicalHistoryAdapter() {
        this.records = new ArrayList<>();
    }

    public void setRecords(List<MedicalRecordResponse> records) {
        this.records = records != null ? records : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_medical_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MedicalRecordResponse record = records.get(position);
        boolean isExpanded = expandedRecordIds.contains(record.getId());
        holder.bind(record, isExpanded, () -> {
            if (expandedRecordIds.contains(record.getId())) {
                expandedRecordIds.remove(record.getId());
            } else {
                expandedRecordIds.add(record.getId());
            }
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvDate;
        private TextView tvDoctorName;
        private TextView tvDiagnosis;
        private TextView tvSymptoms;
        private TextView tvAdvice;
        private LinearLayout layoutSymptoms;
        private LinearLayout layoutAdvice;
        private LinearLayout layoutTreatmentSteps;
        private RecyclerView rvTreatmentSteps;
        private MaterialButton btnExpand;
        private TreatmentStepDetailAdapter stepAdapter;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvDoctorName = itemView.findViewById(R.id.tvDoctorName);
            tvDiagnosis = itemView.findViewById(R.id.tvDiagnosis);
            tvSymptoms = itemView.findViewById(R.id.tvSymptoms);
            tvAdvice = itemView.findViewById(R.id.tvAdvice);
            layoutSymptoms = itemView.findViewById(R.id.layoutSymptoms);
            layoutAdvice = itemView.findViewById(R.id.layoutAdvice);
            layoutTreatmentSteps = itemView.findViewById(R.id.layoutTreatmentSteps);
            rvTreatmentSteps = itemView.findViewById(R.id.rvTreatmentSteps);
            btnExpand = itemView.findViewById(R.id.btnExpand);
            
            // Setup treatment steps RecyclerView
            stepAdapter = new TreatmentStepDetailAdapter();
            rvTreatmentSteps.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(itemView.getContext()));
            rvTreatmentSteps.setAdapter(stepAdapter);
        }

        public void bind(MedicalRecordResponse record, boolean isExpanded, Runnable onExpandClick) {
            // Set basic info
            tvDate.setText(record.getDate() != null ? record.getDate() : "N/A");
            tvDoctorName.setText(record.getDoctorName() != null ? "BS. " + record.getDoctorName() : "");
            tvDiagnosis.setText(record.getDiagnosis() != null ? record.getDiagnosis() : "Chưa có chẩn đoán");

            // Set symptoms
            if (record.getSymptoms() != null && !record.getSymptoms().trim().isEmpty()) {
                tvSymptoms.setText(record.getSymptoms());
                layoutSymptoms.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
            } else {
                layoutSymptoms.setVisibility(View.GONE);
            }

            // Set advice
            if (record.getAdvice() != null && !record.getAdvice().trim().isEmpty()) {
                tvAdvice.setText(record.getAdvice());
                layoutAdvice.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
            } else {
                layoutAdvice.setVisibility(View.GONE);
            }
            
            // Set treatment steps
            if (record.getTreatmentSteps() != null && !record.getTreatmentSteps().isEmpty()) {
                stepAdapter.setSteps(record.getTreatmentSteps());
                layoutTreatmentSteps.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
            } else {
                layoutTreatmentSteps.setVisibility(View.GONE);
            }

            // Show/hide expand button
            boolean hasDetails = (record.getSymptoms() != null && !record.getSymptoms().trim().isEmpty())
                    || (record.getAdvice() != null && !record.getAdvice().trim().isEmpty())
                    || (record.getTreatmentSteps() != null && !record.getTreatmentSteps().isEmpty());
            
            if (hasDetails) {
                btnExpand.setVisibility(View.VISIBLE);
                btnExpand.setText(isExpanded ? "Thu gọn" : "Xem chi tiết");
                btnExpand.setOnClickListener(v -> onExpandClick.run());
            } else {
                btnExpand.setVisibility(View.GONE);
            }
        }
    }
}
