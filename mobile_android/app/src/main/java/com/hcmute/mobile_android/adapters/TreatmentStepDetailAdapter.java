package com.hcmute.mobile_android.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.models.MedicalRecordResponse;

import java.util.ArrayList;
import java.util.List;

public class TreatmentStepDetailAdapter extends RecyclerView.Adapter<TreatmentStepDetailAdapter.ViewHolder> {

    private List<MedicalRecordResponse.TreatmentStepDetail> steps;

    public TreatmentStepDetailAdapter() {
        this.steps = new ArrayList<>();
    }

    public void setSteps(List<MedicalRecordResponse.TreatmentStepDetail> steps) {
        this.steps = steps != null ? steps : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_treatment_step_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MedicalRecordResponse.TreatmentStepDetail step = steps.get(position);
        holder.bind(step, position + 1);
    }

    @Override
    public int getItemCount() {
        return steps.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvStepNumber;
        private TextView tvServiceName;
        private TextView tvStepNotes;
        private TextView tvCompletedAt;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStepNumber = itemView.findViewById(R.id.tvStepNumber);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvStepNotes = itemView.findViewById(R.id.tvStepNotes);
            tvCompletedAt = itemView.findViewById(R.id.tvCompletedAt);
        }

        public void bind(MedicalRecordResponse.TreatmentStepDetail step, int stepNumber) {
            tvStepNumber.setText(String.valueOf(stepNumber));
            
            // Service name with tooth number if available
            String serviceName = step.getServiceName();
            if (step.getToothNumber() != null && !step.getToothNumber().isEmpty()) {
                serviceName += " (Răng " + step.getToothNumber() + ")";
            }
            tvServiceName.setText(serviceName);
            
            // Notes
            tvStepNotes.setText(step.getNotes() != null ? step.getNotes() : "Không có ghi chú");
            
            // Completed time
            tvCompletedAt.setText(step.getCompletedAt() != null ? step.getCompletedAt() : "");
        }
    }
}
