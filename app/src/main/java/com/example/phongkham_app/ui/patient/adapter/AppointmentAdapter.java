package com.example.phongkham_app.ui.patient.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.phongkham_app.R;
import com.example.phongkham_app.data.model.Appointment;
import java.util.ArrayList;
import java.util.List;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.ViewHolder> {

    private List<Appointment> appointments = new ArrayList<>();

    public void setAppointments(List<Appointment> appointments) {
        this.appointments = appointments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_appointment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Appointment app = appointments.get(position);
        holder.tvServiceName.setText(app.getPatientName()); // Dùng patientName chứa tên dịch vụ
        holder.tvDoctorName.setText("Bác sĩ: " + app.getDoctorName());
        holder.tvDateTime.setText(app.getDate() + " - " + app.getTime());
        holder.tvStatus.setText(app.getStatus());
        
        // Style status
        if ("SCHEDULED".equals(app.getStatus())) {
            holder.tvStatus.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.primary));
        } else if ("COMPLETED".equals(app.getStatus())) {
            holder.tvStatus.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.success));
        }
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvServiceName, tvDoctorName, tvDateTime, tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvDoctorName = itemView.findViewById(R.id.tvDoctorName);
            tvDateTime = itemView.findViewById(R.id.tvDateTime);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
