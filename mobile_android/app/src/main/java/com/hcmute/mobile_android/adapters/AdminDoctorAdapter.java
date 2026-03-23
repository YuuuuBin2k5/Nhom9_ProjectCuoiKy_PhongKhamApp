package com.hcmute.mobile_android.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.models.DoctorItem;

import java.util.List;

public class AdminDoctorAdapter extends RecyclerView.Adapter<AdminDoctorAdapter.ViewHolder> {

    private List<DoctorItem> doctorList;

    public AdminDoctorAdapter(List<DoctorItem> doctorList) {
        this.doctorList = doctorList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_doctor, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DoctorItem doctor = doctorList.get(position);
        holder.bind(doctor);
    }

    @Override
    public int getItemCount() {
        return doctorList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName, tvSpecialty, tvExperience, tvEmail;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvDoctorName);
            tvSpecialty = itemView.findViewById(R.id.tvSpecialty);
            tvExperience = itemView.findViewById(R.id.tvExperience);
            tvEmail = itemView.findViewById(R.id.tvEmail);
        }

        public void bind(DoctorItem doctor) {
            tvName.setText(doctor.getFullName());
            tvSpecialty.setText(doctor.getSpecialization());
            tvExperience.setText(doctor.getExperienceYears() + " năm kinh nghiệm");
            tvEmail.setText(doctor.getEmail() != null ? doctor.getEmail() : "N/A");
        }
    }
}