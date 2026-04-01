package com.hcmute.mobile_android.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.materialswitch.MaterialSwitch;
import android.widget.Filter;
import android.widget.Filterable;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.models.Receptionist;
import java.util.ArrayList;
import java.util.List;

public class ReceptionistAdapter extends RecyclerView.Adapter<ReceptionistAdapter.ViewHolder> implements Filterable {

    private List<Receptionist> list = new ArrayList<>();
    private List<Receptionist> listFull = new ArrayList<>();
    private final OnReceptionistClickListener listener;

    public interface OnReceptionistClickListener {
        void onStatusChange(Receptionist receptionist, boolean active);
        void onDelete(Receptionist receptionist);
    }

    public ReceptionistAdapter(OnReceptionistClickListener listener) {
        this.listener = listener;
    }

    public void setList(List<Receptionist> list) {
        this.list = list;
        this.listFull = new ArrayList<>(list);
        notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<Receptionist> filteredList = new ArrayList<>();
                if (constraint == null || constraint.length() == 0) {
                    filteredList.addAll(listFull);
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();
                    for (Receptionist item : listFull) {
                        if (item.getFullName().toLowerCase().contains(filterPattern) ||
                            item.getEmail().toLowerCase().contains(filterPattern)) {
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
                list = (List<Receptionist>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_receptionist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Receptionist r = list.get(position);
        holder.tvName.setText(r.getFullName());
        holder.tvEmail.setText(r.getEmail());
        holder.tvPhone.setText(r.getPhoneNumber() != null ? r.getPhoneNumber() : "Chưa cập nhật SĐT");
        
        holder.switchStatus.setOnCheckedChangeListener(null);
        holder.switchStatus.setChecked(r.getActive() != null ? r.getActive() : true);
        
        holder.switchStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) listener.onStatusChange(r, isChecked);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(r);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvPhone;
        MaterialSwitch switchStatus;
        ImageButton btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            switchStatus = itemView.findViewById(R.id.switchStatus);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
