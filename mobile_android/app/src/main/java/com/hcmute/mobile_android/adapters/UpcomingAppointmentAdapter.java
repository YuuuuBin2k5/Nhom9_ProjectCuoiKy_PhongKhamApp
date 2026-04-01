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
        private TextView tvDate, tvMonth, tvTime, tvDoctor, tvService, tvStatus;

        public AppointmentViewHolder(@NonNull View itemView) {
            super(itemView);
            cardAppointment = itemView.findViewById(R.id.cardAppointment);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvMonth = itemView.findViewById(R.id.tvMonth);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvDoctor = itemView.findViewById(R.id.tvDoctor);
            tvService = itemView.findViewById(R.id.tvService);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }

        public void bind(UpcomingAppointment appointment, OnAppointmentClickListener listener) {
            // Format date and time
            String[] dateTime = formatDateTime(appointment.getAppointmentTime());
            // dateTime[0] is dd/MM
            if (dateTime[0].contains("/")) {
                String[] split = dateTime[0].split("/");
                tvDate.setText(split[0]);
                // Simple month mapping
                String month = "JAN";
                switch(split[1]) {
                    case "02": month = "FEB"; break;
                    case "03": month = "MAR"; break;
                    case "04": month = "APR"; break;
                    case "05": month = "MAY"; break;
                    case "06": month = "JUN"; break;
                    case "07": month = "JUL"; break;
                    case "08": month = "AUG"; break;
                    case "09": month = "SEP"; break;
                    case "10": month = "OCT"; break;
                    case "11": month = "NOV"; break;
                    case "12": month = "DEC"; break;
                }
                tvMonth.setText(month);
            }
            
            // 24h clock — avoids wrong labels like "15:20 AM" (VN clinics commonly use HH:mm)
            tvTime.setText(dateTime[1]);
            
            tvDoctor.setText(appointment.getDoctorName().startsWith("BS.") ? appointment.getDoctorName() : "BS. " + appointment.getDoctorName());
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
            if (dateTime == null || dateTime.isEmpty()) return new String[]{"01/01", "09:00"};
            try {
                String dt = dateTime.replace(" ", "T");
                // Remove fractional seconds or offset if present
                if (dt.contains(".")) {
                    dt = dt.substring(0, dt.indexOf("."));
                }
                if (dt.contains("+")) {
                    dt = dt.substring(0, dt.indexOf("+"));
                }
                
                // If missing seconds, append :00
                if (dt.length() == 16) {
                    dt += ":00";
                }

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    java.time.LocalDateTime ldt = java.time.LocalDateTime.parse(dt);
                    java.time.format.DateTimeFormatter dateFmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM");
                    java.time.format.DateTimeFormatter timeFmt = java.time.format.DateTimeFormatter.ofPattern("HH:mm");
                    return new String[]{ldt.format(dateFmt), ldt.format(timeFmt)};
                } else {
                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());
                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    java.util.Date date = inputFormat.parse(dt);
                    return new String[]{dateFormat.format(date), timeFormat.format(date)};
                }
            } catch (Exception e) {
                return new String[]{"01/01", "09:00"};
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