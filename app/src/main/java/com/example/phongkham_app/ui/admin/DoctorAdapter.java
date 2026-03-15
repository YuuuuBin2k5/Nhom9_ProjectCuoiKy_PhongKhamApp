package com.example.phongkham_app.ui.admin;

import com.example.phongkham_app.R;
import com.example.phongkham_app.data.model.Doctor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.DoctorViewHolder> {

    private List<Doctor> doctors;
    private OnDoctorActionListener listener;

    public interface OnDoctorActionListener {
        void onEdit(Doctor doctor, int position);
        void onDelete(Doctor doctor, int position);
    }

    public DoctorAdapter(List<Doctor> doctors, OnDoctorActionListener listener) {
        this.doctors = doctors;
        this.listener = listener;
    }

    public void updateData(List<Doctor> newDoctors) {
        this.doctors = newDoctors;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DoctorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.admin_item_doctor, parent, false);
        return new DoctorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DoctorViewHolder holder, int position) {
        Doctor doctor = doctors.get(position);
        holder.bind(doctor, position);
    }

    @Override
    public int getItemCount() {
        return doctors.size();
    }

    class DoctorViewHolder extends RecyclerView.ViewHolder {
        TextView tvDoctorName;
        ImageButton btnMenu;

        public DoctorViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDoctorName = itemView.findViewById(R.id.tvDoctorName);
            btnMenu = itemView.findViewById(R.id.btnMenu);
        }

        public void bind(Doctor doctor, int position) {
            tvDoctorName.setText(doctor.getName());

            btnMenu.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(v.getContext(), v);
                popup.inflate(R.menu.admin_menu_doctor_options);
                
                popup.setOnMenuItemClickListener(item -> {
                    int itemId = item.getItemId();
                    if (itemId == R.id.action_edit) {
                        if (listener != null) {
                            listener.onEdit(doctor, position);
                        }
                        return true;
                    } else if (itemId == R.id.action_delete) {
                        if (listener != null) {
                            listener.onDelete(doctor, position);
                        }
                        return true;
                    }
                    return false;
                });
                
                popup.show();
            });
        }
    }
}
