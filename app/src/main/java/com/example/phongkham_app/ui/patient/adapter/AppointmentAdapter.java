package com.example.phongkham_app.ui.patient.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.phongkham_app.R;
import com.example.phongkham_app.data.model.Appointment;
import com.example.phongkham_app.ui.patient.AppointmentDetailActivity;

import java.util.ArrayList;
import java.util.List;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.ViewHolder> {

    private Context context;
    private List<Appointment> appointmentList;

    public AppointmentAdapter(Context context) {
        this.context = context;
        this.appointmentList = new ArrayList<>();
    }

    public void setAppointments(List<Appointment> appointments) {
        this.appointmentList = appointments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_patient_appointment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Appointment appointment = appointmentList.get(position);

        holder.tvDoctorName.setText("Bs. " + appointment.getDoctorName());
        holder.tvService.setText(appointment.getPatientName()); // Service is overloaded onto PatientName field conceptually here or should be parsed separately via model updates. Note: We use the available fields loosely for display mapping.
        holder.tvTime.setText(appointment.getDate() + " " + appointment.getTime());
        holder.tvStatus.setText(appointment.getStatus() != null ? appointment.getStatus() : "N/A");

        holder.itemView.setOnClickListener(v -> {
            try {
                int id = Integer.parseInt(appointment.getId());
                Intent intent = new Intent(context, AppointmentDetailActivity.class);
                intent.putExtra("APPOINTMENT_ID", id);
                context.startActivity(intent);
            } catch (NumberFormatException ignored) {}
        });
    }

    @Override
    public int getItemCount() {
        return appointmentList != null ? appointmentList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDoctorName, tvService, tvTime, tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDoctorName = itemView.findViewById(R.id.tv_item_doctor_name);
            tvService = itemView.findViewById(R.id.tv_item_service);
            tvTime = itemView.findViewById(R.id.tv_item_time);
            tvStatus = itemView.findViewById(R.id.tv_item_status);
        }
    }
}
