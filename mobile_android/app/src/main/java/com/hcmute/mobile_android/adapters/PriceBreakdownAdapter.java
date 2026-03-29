package com.hcmute.mobile_android.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.models.TreatmentPlan;

import java.util.List;

public class PriceBreakdownAdapter extends RecyclerView.Adapter<PriceBreakdownAdapter.ViewHolder> {
    
    private List<TreatmentPlan.Step> steps;
    
    public PriceBreakdownAdapter(List<TreatmentPlan.Step> steps) {
        this.steps = steps;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_price_breakdown, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TreatmentPlan.Step step = steps.get(position);
        
        String serviceName = step.getServiceName();
        if (step.getToothNumber() != null && !step.getToothNumber().isEmpty()) {
            serviceName += " (Răng " + step.getToothNumber() + ")";
        }
        
        holder.tvServiceName.setText(serviceName);
        
        Double price = step.getEstimatedPrice();
        if (price != null) {
            holder.tvServicePrice.setText(String.format("%,.0f VNĐ", price));
        } else {
            holder.tvServicePrice.setText("0 VNĐ");
        }
    }
    
    @Override
    public int getItemCount() {
        return steps.size();
    }
    
    public void updateSteps(List<TreatmentPlan.Step> newSteps) {
        this.steps = newSteps;
        notifyDataSetChanged();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvServiceName;
        TextView tvServicePrice;
        
        ViewHolder(View itemView) {
            super(itemView);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvServicePrice = itemView.findViewById(R.id.tvServicePrice);
        }
    }
}
