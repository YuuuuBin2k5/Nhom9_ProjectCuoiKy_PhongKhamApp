package com.hcmute.mobile_android.ui.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.models.ScheduleAppointment;
import java.util.ArrayList;
import java.util.List;

public class ScheduleSlotAdapter extends RecyclerView.Adapter<ScheduleSlotAdapter.ViewHolder> {

    private final List<String> timeSlots;
    private List<ScheduleAppointment> appointments = new ArrayList<>();

    public ScheduleSlotAdapter() {
        timeSlots = new ArrayList<>();
        // Generate slots from 08:00 to 16:30
        int hour = 8;
        int minute = 0;
        while (hour < 16 || (hour == 16 && minute <= 30)) {
            timeSlots.add(String.format("%02d:%02d", hour, minute));
            minute += 30;
            if (minute >= 60) {
                hour++;
                minute = 0;
            }
        }
    }

    public void setAppointments(List<ScheduleAppointment> appointments) {
        this.appointments = appointments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_schedule_slot, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String time = timeSlots.get(position);
        // Find appointments for this slot
        List<ScheduleAppointment> matches = new ArrayList<>();
        for (ScheduleAppointment a : appointments) {
            if (a.getDatetime().contains(time)) {
                matches.add(a);
            }
        }

        if (!matches.isEmpty()) {
            StringBuilder patientNames = new StringBuilder();
            StringBuilder services = new StringBuilder();
            for (int i = 0; i < matches.size(); i++) {
                ScheduleAppointment m = matches.get(i);
                patientNames.append(m.getPatientName());
                services.append(m.getServiceName());
                if (i < matches.size() - 1) {
                    patientNames.append(", ");
                    services.append(", ");
                }
            }

            holder.tvSlotInfo.setText(services.toString());
            holder.tvPatientName.setText("Bệnh nhân: " + patientNames.toString());
            holder.tvPatientName.setVisibility(View.VISIBLE);
            holder.tvStatus.setText(matches.size() > 1 ? "TRÙNG LỊCH (" + matches.size() + ")" : matches.get(0).getStatus());
            holder.tvStatus.setVisibility(View.VISIBLE);
            
            holder.cardView.setStrokeColor(Color.parseColor(matches.size() > 1 ? "#F44336" : "#4285F4")); // Red if overlap
            holder.cardView.setCardBackgroundColor(Color.parseColor(matches.size() > 1 ? "#FFF5F5" : "#F0F4FF"));
        } else {
            holder.tvSlotInfo.setText("Trống");
            holder.tvPatientName.setVisibility(View.GONE);
            holder.tvStatus.setVisibility(View.GONE);
            
            holder.cardView.setStrokeColor(Color.parseColor("#EEEEEE"));
            holder.cardView.setCardBackgroundColor(Color.WHITE);
        }
    }

    @Override
    public int getItemCount() {
        return timeSlots.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        TextView tvTime, tvSlotInfo, tvPatientName, tvStatus;

        ViewHolder(View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            tvTime = itemView.findViewById(R.id.tvTime);
            tvSlotInfo = itemView.findViewById(R.id.tvSlotInfo);
            tvPatientName = itemView.findViewById(R.id.tvPatientName);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
