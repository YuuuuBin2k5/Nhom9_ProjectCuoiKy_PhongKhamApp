package com.hcmute.mobile_android.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.models.ServiceCategory;

import java.util.List;

public class CategoryHorizontalAdapter extends RecyclerView.Adapter<CategoryHorizontalAdapter.ViewHolder> {
    private List<ServiceCategory> categories;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(ServiceCategory category);
    }

    public CategoryHorizontalAdapter(List<ServiceCategory> categories, OnItemClickListener listener) {
        this.categories = categories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_horizontal, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ServiceCategory category = categories.get(position);
        holder.tvName.setText(category.getName());
        
        // Mock icons based on name
        int iconRes = R.drawable.ic_medical_services;
        String name = category.getName().toLowerCase();
        if (name.contains("niềng răng") || name.contains("trồng răng") || name.contains("răng") || name.contains("nha")) {
            iconRes = R.drawable.ic_tooth; // using existing ic_tooth from previous code if available
        } else {
            iconRes = R.drawable.ic_medical_services;
        }
        
        holder.ivIcon.setImageResource(iconRes);
        holder.itemView.setOnClickListener(v -> listener.onItemClick(category));
    }

    @Override
    public int getItemCount() { return categories.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvName;
        ViewHolder(View view) {
            super(view);
            ivIcon = view.findViewById(R.id.ivCategoryIcon);
            tvName = view.findViewById(R.id.tvCategoryName);
        }
    }
}
