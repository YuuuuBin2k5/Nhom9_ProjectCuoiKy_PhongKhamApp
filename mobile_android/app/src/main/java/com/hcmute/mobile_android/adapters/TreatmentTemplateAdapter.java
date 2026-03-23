package com.hcmute.mobile_android.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.models.TreatmentTemplate;

import java.util.List;

public class TreatmentTemplateAdapter extends RecyclerView.Adapter<TreatmentTemplateAdapter.TemplateViewHolder> {

    private List<TreatmentTemplate> templateList;
    private OnTemplateSelectedListener listener;

    public interface OnTemplateSelectedListener {
        void onTemplateSelected(TreatmentTemplate template);
    }

    public TreatmentTemplateAdapter(List<TreatmentTemplate> templateList, OnTemplateSelectedListener listener) {
        this.templateList = templateList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TemplateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_treatment_template, parent, false);
        return new TemplateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TemplateViewHolder holder, int position) {
        TreatmentTemplate template = templateList.get(position);
        holder.bind(template, listener);
    }

    @Override
    public int getItemCount() {
        return templateList.size();
    }

    static class TemplateViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardTemplate;
        private TextView tvTemplateName;
        private TextView tvTemplateDescription;
        private TextView tvTemplateType;
        private TextView tvStepCount;

        public TemplateViewHolder(@NonNull View itemView) {
            super(itemView);
            cardTemplate = itemView.findViewById(R.id.cardTemplate);
            tvTemplateName = itemView.findViewById(R.id.tvTemplateName);
            tvTemplateDescription = itemView.findViewById(R.id.tvTemplateDescription);
            tvTemplateType = itemView.findViewById(R.id.tvTemplateType);
            tvStepCount = itemView.findViewById(R.id.tvStepCount);
        }

        public void bind(TreatmentTemplate template, OnTemplateSelectedListener listener) {
            tvTemplateName.setText(template.getName());
            tvTemplateDescription.setText(template.getDescription());
            tvTemplateType.setText(getTemplateTypeDisplay(template.getUiTemplateType()));
            
            int stepCount = template.getSteps() != null ? template.getSteps().size() : 0;
            tvStepCount.setText(stepCount + " bước");

            // Set card color based on template type
            int backgroundColor = getTemplateColor(template.getUiTemplateType());
            cardTemplate.setCardBackgroundColor(itemView.getContext().getColor(backgroundColor));

            cardTemplate.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTemplateSelected(template);
                }
            });
        }

        private String getTemplateTypeDisplay(String uiTemplateType) {
            if (uiTemplateType == null) return "Tổng quát";
            
            switch (uiTemplateType.toUpperCase()) {
                case "SURGERY": return "Tiểu phẫu";
                case "ORTHO": return "Chỉnh nha";
                case "IMPLANT": return "Cấy ghép";
                case "PERIO": return "Nha chu";
                default: return "Tổng quát";
            }
        }

        private int getTemplateColor(String uiTemplateType) {
            if (uiTemplateType == null) return android.R.color.white;
            
            switch (uiTemplateType.toUpperCase()) {
                case "SURGERY": return R.color.surgery_background;
                case "ORTHO": return R.color.ortho_background;
                case "IMPLANT": return R.color.implant_background;
                case "PERIO": return R.color.perio_background;
                default: return android.R.color.white;
            }
        }
    }
}