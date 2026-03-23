package com.hcmute.mobile_android.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.models.TreatmentPlanSummary;

import java.util.List;

public class TreatmentProgressAdapter extends RecyclerView.Adapter<TreatmentProgressAdapter.TreatmentViewHolder> {

    private List<TreatmentPlanSummary> treatmentList;
    private OnTreatmentClickListener listener;

    public interface OnTreatmentClickListener {
        void onTreatmentClick(TreatmentPlanSummary treatment);
    }

    public TreatmentProgressAdapter(List<TreatmentPlanSummary> treatmentList, OnTreatmentClickListener listener) {
        this.treatmentList = treatmentList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TreatmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_treatment_progress, parent, false);
        return new TreatmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TreatmentViewHolder holder, int position) {
        TreatmentPlanSummary treatment = treatmentList.get(position);
        holder.bind(treatment, listener);
    }

    @Override
    public int getItemCount() {
        return treatmentList.size();
    }

    static class TreatmentViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardTreatment;
        private TextView tvTitle, tvStatus, tvProgress, tvNextStep;
        private ProgressBar progressBar;

        public TreatmentViewHolder(@NonNull View itemView) {
            super(itemView);
            cardTreatment = itemView.findViewById(R.id.cardTreatment);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvProgress = itemView.findViewById(R.id.tvProgress);
            tvNextStep = itemView.findViewById(R.id.tvNextStep);
            progressBar = itemView.findViewById(R.id.progressBar);
        }

        public void bind(TreatmentPlanSummary treatment, OnTreatmentClickListener listener) {
            tvTitle.setText(treatment.getTitle());
            tvStatus.setText(getStatusDisplay(treatment.getStatus()));
            
            // Calculate progress
            int totalSteps = treatment.getTotalSteps();
            int completedSteps = treatment.getCompletedSteps();
            int progressPercent = totalSteps > 0 ? (completedSteps * 100) / totalSteps : 0;
            
            progressBar.setProgress(progressPercent);
            tvProgress.setText(completedSteps + "/" + totalSteps + " bước (" + progressPercent + "%)");
            
            // Next step
            if (treatment.getNextStepName() != null && !treatment.getNextStepName().isEmpty()) {
                tvNextStep.setText("Tiếp theo: " + treatment.getNextStepName());
                tvNextStep.setVisibility(View.VISIBLE);
            } else {
                tvNextStep.setVisibility(View.GONE);
            }
            
            // Set status color
            setStatusColor(treatment.getStatus());
            
            cardTreatment.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTreatmentClick(treatment);
                }
            });
        }

        private String getStatusDisplay(String status) {
            if (status == null) return "Đang điều trị";
            
            switch (status.toUpperCase()) {
                case "ACTIVE": return "Đang điều trị";
                case "COMPLETED": return "Hoàn thành";
                case "PAUSED": return "Tạm dừng";
                case "CANCELLED": return "Đã hủy";
                default: return status;
            }
        }

        private void setStatusColor(String status) {
            if (status == null) status = "ACTIVE";
            
            switch (status.toUpperCase()) {
                case "ACTIVE":
                    tvStatus.setTextColor(itemView.getContext().getColor(R.color.primary_trust_blue));
                    progressBar.setProgressTintList(itemView.getContext().getColorStateList(R.color.primary_trust_blue));
                    break;
                case "COMPLETED":
                    tvStatus.setTextColor(itemView.getContext().getColor(R.color.success_green));
                    progressBar.setProgressTintList(itemView.getContext().getColorStateList(R.color.success_green));
                    break;
                case "PAUSED":
                    tvStatus.setTextColor(itemView.getContext().getColor(R.color.warning_amber));
                    progressBar.setProgressTintList(itemView.getContext().getColorStateList(R.color.warning_amber));
                    break;
                case "CANCELLED":
                    tvStatus.setTextColor(itemView.getContext().getColor(R.color.alert_coral));
                    progressBar.setProgressTintList(itemView.getContext().getColorStateList(R.color.alert_coral));
                    break;
                default:
                    tvStatus.setTextColor(itemView.getContext().getColor(R.color.text_secondary));
                    break;
            }
        }
    }
}