package com.example.phongkham_app.doctor.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.phongkham_app.R;
import com.example.phongkham_app.data.model.WaitingPatient;

import java.util.List;

public class WaitingPatientAdapter extends RecyclerView.Adapter<WaitingPatientAdapter.ViewHolder> {

    private List<WaitingPatient> patients;
    private OnItemClickListener listener;

    // Interface để xử lý sự kiện click
    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public WaitingPatientAdapter(List<WaitingPatient> patients) {
        this.patients = patients;
    }

    public void updateData(List<WaitingPatient> newPatients) {
        this.patients = newPatients;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.doctor_item_waiting_patient, parent, false);
        return new ViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WaitingPatient patient = patients.get(position);
        holder.tvPatientName.setText(patient.getName());
        holder.tvAppointmentTime.setText(patient.getTime());
    }

    @Override
    public int getItemCount() {
        return patients.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPatientName;
        TextView tvAppointmentTime;

        public ViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            tvPatientName = itemView.findViewById(R.id.tvPatientName);
            tvAppointmentTime = itemView.findViewById(R.id.tvAppointmentTime);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
                }
            });
        }
    }
}
