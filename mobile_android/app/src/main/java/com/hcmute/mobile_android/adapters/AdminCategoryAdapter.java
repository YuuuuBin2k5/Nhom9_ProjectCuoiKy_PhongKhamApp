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

import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;
import java.util.List;

public class AdminCategoryAdapter extends RecyclerView.Adapter<AdminCategoryAdapter.ViewHolder> implements Filterable {

    public interface OnCategoryActionListener {
        void onEditCategory(ServiceCategory category);
        void onDeleteCategory(ServiceCategory category);
    }

    private List<ServiceCategory> categoryList;
    private List<ServiceCategory> categoryListFull;
    private OnCategoryActionListener listener;

    public AdminCategoryAdapter(List<ServiceCategory> categoryList, OnCategoryActionListener listener) {
        this.categoryList = categoryList;
        this.categoryListFull = new ArrayList<>(categoryList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ServiceCategory category = categoryList.get(position);
        holder.bind(category, listener);
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName, tvDescription, tvServiceCount;
        private ImageView ivMenu;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCategoryName);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvServiceCount = itemView.findViewById(R.id.tvServiceCount);
            ivMenu = itemView.findViewById(R.id.ivMenu);
        }

        public void bind(ServiceCategory category, OnCategoryActionListener listener) {
            tvName.setText(category.getName());
            
            if (category.getDescription() != null && !category.getDescription().isEmpty()) {
                tvDescription.setText(category.getDescription());
                tvDescription.setVisibility(View.VISIBLE);
            } else {
                tvDescription.setVisibility(View.GONE);
            }

            // Service count placeholder (backend doesn't provide this yet)
            tvServiceCount.setVisibility(View.GONE);

            // Setup menu button
            if (ivMenu != null) {
                ivMenu.setOnClickListener(v -> showContextMenu(v, category, listener));
            }
        }

        private void showContextMenu(View anchor, ServiceCategory category, OnCategoryActionListener listener) {
            androidx.appcompat.widget.PopupMenu popup = new androidx.appcompat.widget.PopupMenu(itemView.getContext(), anchor);
            popup.getMenuInflater().inflate(R.menu.menu_admin_category, popup.getMenu());
            
            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.action_edit) {
                    if (listener != null) listener.onEditCategory(category);
                    return true;
                } else if (id == R.id.action_delete) {
                    if (listener != null) listener.onDeleteCategory(category);
                    return true;
                }
                return false;
            });
            
            popup.show();
        }
    }

    public void updateList(List<ServiceCategory> newList) {
        this.categoryListFull = new ArrayList<>(newList);
        this.categoryList = newList;
        notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<ServiceCategory> filteredList = new ArrayList<>();
                if (constraint == null || constraint.length() == 0) {
                    filteredList.addAll(categoryListFull);
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();
                    for (ServiceCategory item : categoryListFull) {
                        if (item.getName().toLowerCase().contains(filterPattern) ||
                            (item.getDescription() != null && item.getDescription().toLowerCase().contains(filterPattern))) {
                            filteredList.add(item);
                        }
                    }
                }
                FilterResults results = new FilterResults();
                results.values = filteredList;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                categoryList = (List<ServiceCategory>) results.values;
                notifyDataSetChanged();
            }
        };
    }
}
