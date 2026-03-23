package com.hcmute.mobile_android.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.models.TreatmentPlan;

import java.util.List;

public class TreatmentStepAdapter extends RecyclerView.Adapter<TreatmentStepAdapter.StepViewHolder> {

    private List<TreatmentPlan.Step> stepList;
    private OnStepActionListener listener;

    public interface OnStepActionListener {
        void onStepEdit(TreatmentPlan.Step step);
        void onStepComplete(TreatmentPlan.Step step);
        void onToothSelected(int toothNumber);
    }

    public TreatmentStepAdapter(List<TreatmentPlan.Step> stepList, OnStepActionListener listener) {
        this.stepList = stepList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public StepViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_treatment_step, parent, false);
        return new StepViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StepViewHolder holder, int position) {
        TreatmentPlan.Step step = stepList.get(position);
        holder.bind(step, position + 1, listener);
    }

    @Override
    public int getItemCount() {
        return stepList.size();
    }

    static class StepViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardStep;
        private TextView tvStepNumber;
        private TextView tvServiceName;
        private TextView tvStepDescription;
        private TextView tvToothNumber;
        private TextView tvActualPrice;
        private TextView tvStatus;
        private MaterialButton btnEdit;
        private MaterialButton btnComplete;

        public StepViewHolder(@NonNull View itemView) {
            super(itemView);
            cardStep = itemView.findViewById(R.id.cardStep);
            tvStepNumber = itemView.findViewById(R.id.tvStepNumber);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvStepDescription = itemView.findViewById(R.id.tvStepDescription);
            tvToothNumber = itemView.findViewById(R.id.tvToothNumber);
            tvActualPrice = itemView.findViewById(R.id.tvActualPrice);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnComplete = itemView.findViewById(R.id.btnComplete);
        }

        public void bind(TreatmentPlan.Step step, int stepNumber, OnStepActionListener listener) {
            tvStepNumber.setText(String.valueOf(stepNumber));
            tvServiceName.setText(step.getServiceName());
            tvStepDescription.setText(step.getDescription());
            
            // Tooth number
            if (step.getToothNumber() != null && step.getToothNumber() > 0) {
                tvToothNumber.setText("Răng " + step.getToothNumber());
                tvToothNumber.setVisibility(View.VISIBLE);
            } else {
                tvToothNumber.setVisibility(View.GONE);
            }
            
            // Price
            if (step.getActualPrice() != null && step.getActualPrice() > 0) {
                tvActualPrice.setText(String.format("%,.0f VNĐ", step.getActualPrice()));
            } else {
                tvActualPrice.setText("Chưa định giá");
            }
            
            // Status
            tvStatus.setText(getStatusDisplay(step.getStatus()));
            
            // Card styling based on status
            setCardStyle(step.getStatus());
            
            // Button actions
            btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onStepEdit(step);
                }
            });
            
            btnComplete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onStepComplete(step);
                }
            });
            
            // Configure buttons based on status
            configureButtons(step.getStatus());
        }

        private String getStatusDisplay(String status) {
            if (status == null) return "Chưa thực hiện";
            
            switch (status.toUpperCase()) {
                case "PENDING": return "Chờ thực hiện";
                case "IN_PROGRESS": return "Đang thực hiện";
                case "COMPLETED": return "Hoàn thành";
                case "CANCELLED": return "Đã hủy";
                default: return status;
            }
        }

        private void setCardStyle(String status) {
            if (status == null) status = "PENDING";
            
            switch (status.toUpperCase()) {
                case "COMPLETED":
                    cardStep.setCardBackgroundColor(itemView.getContext().getColor(R.color.success_background));
                    cardStep.setStrokeColor(itemView.getContext().getColor(R.color.success_green));
                    cardStep.setStrokeWidth(2);
                    break;
                case "IN_PROGRESS":
                    cardStep.setCardBackgroundColor(itemView.getContext().getColor(R.color.priority_background));
                    cardStep.setStrokeColor(itemView.getContext().getColor(R.color.warning_amber));
                    cardStep.setStrokeWidth(2);
                    break;
                case "CANCELLED":
                    cardStep.setCardBackgroundColor(itemView.getContext().getColor(R.color.error_background));
                    cardStep.setStrokeColor(itemView.getContext().getColor(R.color.alert_coral));
                    cardStep.setStrokeWidth(2);
                    break;
                default: // PENDING
                    cardStep.setCardBackgroundColor(itemView.getContext().getColor(android.R.color.white));
                    cardStep.setStrokeColor(itemView.getContext().getColor(R.color.border_gray));
                    cardStep.setStrokeWidth(1);
                    break;
            }
        }

        private void configureButtons(String status) {
            if (status == null) status = "PENDING";
            
            switch (status.toUpperCase()) {
                case "COMPLETED":
                    btnEdit.setVisibility(View.VISIBLE);
                    btnEdit.setText("Xem");
                    btnComplete.setVisibility(View.GONE);
                    break;
                case "IN_PROGRESS":
                    btnEdit.setVisibility(View.VISIBLE);
                    btnEdit.setText("Chỉnh sửa");
                    btnComplete.setVisibility(View.VISIBLE);
                    break;
                case "CANCELLED":
                    btnEdit.setVisibility(View.VISIBLE);
                    btnEdit.setText("Xem");
                    btnComplete.setVisibility(View.GONE);
                    break;
                default: // PENDING
                    btnEdit.setVisibility(View.VISIBLE);
                    btnEdit.setText("Bắt đầu");
                    btnComplete.setVisibility(View.GONE);
                    break;
            }
        }
    }
}