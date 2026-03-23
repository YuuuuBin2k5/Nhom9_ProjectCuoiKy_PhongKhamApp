package com.hcmute.mobile_android.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.models.UpcomingAppointment;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class UpcomingAppointmentAdapter extends RecyclerView.Adapter<UpcomingAppointmentAdapter.AppointmentViewHolder> {

    private List<UpcomingAppointment> appointmentList;
    private OnAppointmentClickListener listener;

    public interface OnAppointmentClickListener {
        void onAppointmentClick(UpcomingAppointment appointment);
    }

    public UpcomingAppointmentAdapter(List<UpcomingAppointment> appointmentList, OnAppointmentClickListener listener) {
        this.appointmentList = appointmentList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_upcoming_appointment, parent, false);
        return new AppointmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        UpcomingAppointment appointment = appointmentList.get(position);
        holder.bind(appointment, listener);
    }

    @Override
    public int getItemCount() {
        return appointmentList.size();
    }

    static class AppointmentViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardAppointment;
        private TextView tvDate, tvTime, tvDoctor, tvService, tvStatus;

        public AppointmentViewHolder(@NonNull View itemView) {
            super(itemView);
            cardAppointment = itemView.findViewById(R.id.cardAppointment);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvDoctor = itemView.findViewById(R.id.tvDoctor);
            tvService = itemView.findViewById(R.id.tvService);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }

        public void bind(UpcomingAppointment appointment, OnAppointmentClickListener listener) {
            // Format date and time
            String[] dateTime = formatDateTime(appointment.getAppointmentTime());
            tvDate.setText(dateTime[0]);
            tvTime.setText(dateTime[1]);
            
            tvDoctor.setText("BS. " + appointment.getDoctorName());
            tvService.setText(appointment.getServiceName());
            tvStatus.setText(getStatusDisplay(appointment.getStatus()));
            
            // Set status color
            setStatusColor(appointment.getStatus());
            
            cardAppointment.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAppointmentClick(appointment);
                }
            });
        }

        private String[] formatDateTime(String dateTime) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                
                java.util.Date date = inputFormat.parse(dateTime);
                return new String[]{dateFormat.format(date), timeFormat.format(date)};
            } catch (Exception e) {
                return new String[]{"--/--", "--:--"};
            }
        }

        private String getStatusDisplay(String status) {
            if (status == null) return "Đã đặt";
            
            switch (status.toUpperCase()) {
                case "CONFIRMED": return "Đã xác nhận";
                case "PENDING": return "Chờ xác nhận";
                case "CANCELLED": return "Đã hủy";
                case "COMPLETED": return "Hoàn thành";
                default: return status;
            }
        }

        private void setStatusColor(String status) {
            if (status == null) status = "PENDING";
            
            switch (status.toUpperCase()) {
                case "CONFIRMED":
                    tvStatus.setTextColor(itemView.getContext().getColor(R.color.success_green));
                    break;
                case "PENDING":
                    tvStatus.setTextColor(itemView.getContext().getColor(R.color.warning_amber));
                    break;
                case "CANCELLED":
                    tvStatus.setTextColor(itemView.getContext().getColor(R.color.alert_coral));
                    break;
                case "COMPLETED":
                    tvStatus.setTextColor(itemView.getContext().getColor(R.color.text_secondary));
                    break;
                default:
                    tvStatus.setTextColor(itemView.getContext().getColor(R.color.text_secondary));
                    break;
            }
        }
    }
}