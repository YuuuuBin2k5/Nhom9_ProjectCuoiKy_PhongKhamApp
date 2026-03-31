package com.hcmute.mobile_android.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.models.TreatmentTemplate;

import java.util.ArrayList;
import java.util.List;

public class AdminTreatmentTemplateAdapter extends RecyclerView.Adapter<AdminTreatmentTemplateAdapter.ViewHolder> {

    private List<TreatmentTemplate> templates = new ArrayList<>();
    private final OnTemplateClickListener listener;

    public interface OnTemplateClickListener {
        void onTemplateClick(TreatmentTemplate template);
        void onMenuClick(View view, TreatmentTemplate template);
    }

    public AdminTreatmentTemplateAdapter(OnTemplateClickListener listener) {
        this.listener = listener;
    }

    public void setTemplates(List<TreatmentTemplate> templates) {
        this.templates = templates != null ? templates : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_treatment_template, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TreatmentTemplate template = templates.get(position);
        holder.tvName.setText(template.getName());
        holder.tvDescription.setText(template.getDescription());
        
        int stepCount = template.getSteps() != null ? template.getSteps().size() : 0;
        holder.tvStepCount.setText(stepCount + " bước");
        
        boolean isActive = template.getIsActive() != null && template.getIsActive();
        holder.tvStatus.setText(isActive ? "Đang hoạt động" : "Ngừng hoạt động");
        holder.tvStatus.setTextColor(isActive ? 0xFF2E7D32 : 0xFFD32F2F);

        // Service summary
        if (template.getSteps() != null && !template.getSteps().isEmpty()) {
            StringBuilder summary = new StringBuilder();
            for (int i = 0; i < template.getSteps().size(); i++) {
                summary.append(template.getSteps().get(i).getServiceName());
                if (i < template.getSteps().size() - 1) {
                    summary.append(", ");
                }
            }
            holder.tvServiceSummary.setText(summary.toString());
            holder.tvServiceSummary.setVisibility(View.VISIBLE);
        } else {
            holder.tvServiceSummary.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> listener.onTemplateClick(template));
        holder.ivMenu.setOnClickListener(v -> listener.onMenuClick(v, template));
    }

    @Override
    public int getItemCount() {
        return templates.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDescription, tvStepCount, tvStatus, tvServiceSummary;
        ImageView ivMenu;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvTemplateName);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvStepCount = itemView.findViewById(R.id.tvStepCount);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvServiceSummary = itemView.findViewById(R.id.tvServiceSummary);
            ivMenu = itemView.findViewById(R.id.ivMenu);
        }
    }
}
