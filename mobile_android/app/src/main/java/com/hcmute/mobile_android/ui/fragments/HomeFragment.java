package com.hcmute.mobile_android.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.models.QueueItem;
import com.hcmute.mobile_android.network.models.TreatmentPlanSummary;
import com.hcmute.mobile_android.network.models.UpcomingAppointment;
import com.hcmute.mobile_android.ui.activities.GenericListActivity;
import com.hcmute.mobile_android.ui.activities.LoginActivity;
import com.hcmute.mobile_android.ui.activities.MainActivity;
import com.hcmute.mobile_android.ui.activities.staff.DoctorWorkflowActivity;
import com.hcmute.mobile_android.util.TokenManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    public interface HomeCallbacks {
        void onNavigateToQr();
        void onNavigateToNotifications();
    }

    private HomeCallbacks callbacks;

    // View references
    private TextView tvDoctorName;
    private TextView tvStatToday;
    private TextView tvStatAppointments;
    private TextView tvStatPlans;
    private RecyclerView rvQueue;
    private RecyclerView rvUpcoming;
    private TextView tvQueueEmpty;
    private TextView tvUpcomingEmpty;

    // Adapters
    private HomeQueueAdapter queueAdapter;
    private HomeAppointmentAdapter appointmentAdapter;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof HomeCallbacks) {
            callbacks = (HomeCallbacks) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind views
        tvDoctorName = view.findViewById(R.id.tv_doctor_name);
        tvStatToday = view.findViewById(R.id.tv_stat_today);
        tvStatAppointments = view.findViewById(R.id.tv_stat_appointments);
        tvStatPlans = view.findViewById(R.id.tv_stat_plans);
        rvQueue = view.findViewById(R.id.rv_queue);
        rvUpcoming = view.findViewById(R.id.rv_upcoming);
        tvQueueEmpty = view.findViewById(R.id.tv_queue_empty);
        tvUpcomingEmpty = view.findViewById(R.id.tv_upcoming_empty);

        // Setup RecyclerViews
        queueAdapter = new HomeQueueAdapter();
        appointmentAdapter = new HomeAppointmentAdapter();

        rvQueue.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvQueue.setAdapter(queueAdapter);
        rvQueue.setNestedScrollingEnabled(false);

        rvUpcoming.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvUpcoming.setAdapter(appointmentAdapter);
        rvUpcoming.setNestedScrollingEnabled(false);

        // Load doctor name from TokenManager
        TokenManager tm = new TokenManager(requireContext());
        String userName = tm.getUserName();
        if (userName != null && !userName.isEmpty()) {
            tvDoctorName.setText(userName);
        } else {
            // Fallback: show email prefix
            String role = tm.getUserRole();
            tvDoctorName.setText("Bác sĩ");
        }

        // Wire up quick action buttons
        view.findViewById(R.id.btn_qr_scan).setOnClickListener(v -> navigateToQr());
        view.findViewById(R.id.btn_new_record).setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), DoctorWorkflowActivity.class));
        });
        view.findViewById(R.id.btn_tooth_chart).setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Sơ đồ răng nhanh - Đang phát triển", Toast.LENGTH_SHORT).show();
        });

        // Notification click
        view.findViewById(R.id.iv_notification).setOnClickListener(v -> {
            if (callbacks != null) callbacks.onNavigateToNotifications();
        });

        // Settings click → logout for now
        view.findViewById(R.id.iv_settings).setOnClickListener(v -> doLogout());

        // Upcoming header: "Xem tất cả"
        view.findViewById(R.id.tv_all_appointments).setOnClickListener(v ->
                openList(GenericListActivity.MODE_APPOINTMENTS));

        // Load data
        loadData();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getView() != null) loadData();
    }

    // ─── Navigation helpers ────────────────────────────────────────────────────

    private void navigateToQr() {
        if (callbacks != null) {
            callbacks.onNavigateToQr();
        } else if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).onNavigateToQr();
        }
    }

    private void doLogout() {
        new TokenManager(requireContext()).clearToken();
        startActivity(new Intent(requireContext(), LoginActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        requireActivity().finish();
    }

    private void openList(String mode) {
        Intent i = new Intent(requireContext(), GenericListActivity.class);
        i.putExtra(GenericListActivity.EXTRA_MODE, mode);
        startActivity(i);
    }

    // ─── Data Loading ──────────────────────────────────────────────────────────

    private void loadData() {
        ApiService api = RetrofitClient.getApiService(requireContext());

        // Load queue (room 1 by default)
        loadQueue(api);

        // Load upcoming appointments
        loadUpcoming(api);

        // Load treatment plans for stats
        loadTreatmentPlans(api);
    }

    private void loadQueue(ApiService api) {
        api.getQueueByRoom(1L).enqueue(new Callback<List<QueueItem>>() {
            @Override
            public void onResponse(Call<List<QueueItem>> call, Response<List<QueueItem>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    List<QueueItem> items = response.body();
                    // Filter out COMPLETED
                    List<QueueItem> active = new ArrayList<>();
                    for (QueueItem q : items) {
                        if (!"COMPLETED".equals(q.getStatus()) && !"SKIPPED".equals(q.getStatus())) {
                            active.add(q);
                        }
                    }
                    updateQueueUI(active);
                    // Update "Hôm nay" stat
                    tvStatToday.setText("Hôm nay: " + items.size() + " BN");
                } else {
                    updateQueueUI(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<List<QueueItem>> call, Throwable t) {
                if (!isAdded()) return;
                updateQueueUI(new ArrayList<>());
            }
        });
    }

    private void updateQueueUI(List<QueueItem> items) {
        if (items.isEmpty()) {
            rvQueue.setVisibility(View.GONE);
            tvQueueEmpty.setVisibility(View.VISIBLE);
        } else {
            rvQueue.setVisibility(View.VISIBLE);
            tvQueueEmpty.setVisibility(View.GONE);
            queueAdapter.updateItems(items);
        }
    }

    private void loadUpcoming(ApiService api) {
        api.getUpcomingAppointments().enqueue(new Callback<List<UpcomingAppointment>>() {
            @Override
            public void onResponse(Call<List<UpcomingAppointment>> call,
                                   Response<List<UpcomingAppointment>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    List<UpcomingAppointment> list = response.body();
                    tvStatAppointments.setText("Lịch hẹn: " + list.size());
                    updateUpcomingUI(list);
                } else {
                    tvStatAppointments.setText("Lịch hẹn: 0");
                    updateUpcomingUI(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<List<UpcomingAppointment>> call, Throwable t) {
                if (!isAdded()) return;
                tvStatAppointments.setText("Lịch hẹn: 0");
                updateUpcomingUI(new ArrayList<>());
            }
        });
    }

    private void updateUpcomingUI(List<UpcomingAppointment> items) {
        if (items.isEmpty()) {
            rvUpcoming.setVisibility(View.GONE);
            tvUpcomingEmpty.setVisibility(View.VISIBLE);
        } else {
            rvUpcoming.setVisibility(View.VISIBLE);
            tvUpcomingEmpty.setVisibility(View.GONE);
            appointmentAdapter.updateItems(items);
        }
    }

    private void loadTreatmentPlans(ApiService api) {
        api.getMyTreatmentPlans().enqueue(new Callback<List<TreatmentPlanSummary>>() {
            @Override
            public void onResponse(Call<List<TreatmentPlanSummary>> call,
                                   Response<List<TreatmentPlanSummary>> response) {
                if (!isAdded()) return;
                int count = (response.isSuccessful() && response.body() != null)
                        ? response.body().size() : 0;
                tvStatPlans.setText("Phác đồ: " + count);
            }

            @Override
            public void onFailure(Call<List<TreatmentPlanSummary>> call, Throwable t) {
                if (!isAdded()) return;
                tvStatPlans.setText("Phác đồ: 0");
            }
        });
    }

    // ─── Queue Adapter ─────────────────────────────────────────────────────────

    private class HomeQueueAdapter extends RecyclerView.Adapter<HomeQueueAdapter.Holder> {
        private List<QueueItem> items = new ArrayList<>();

        void updateItems(List<QueueItem> list) {
            items = list != null ? list : new ArrayList<>();
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_home_queue, parent, false);
            return new Holder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            QueueItem q = items.get(position);
            ApiService api = RetrofitClient.getApiService(requireContext());

            // Avatar: first letter of patient name
            String name = q.getPatientName() != null ? q.getPatientName() : "?";
            holder.tvAvatar.setText(name.length() > 0
                    ? String.valueOf(name.charAt(0)).toUpperCase() : "?");

            // Choose avatar color based on position
            int[] colors = {
                    Color.parseColor("#1565C0"), Color.parseColor("#00695C"),
                    Color.parseColor("#6A1B9A"), Color.parseColor("#BF360C"),
                    Color.parseColor("#37474F")
            };
            holder.tvAvatar.getBackground().setTint(colors[position % colors.length]);

            // Name
            holder.tvPatientName.setText(name);

            // STT + reason
            String stt = q.getQueueNumber() != null
                    ? String.format("STT: %02d", q.getQueueNumber()) : "STT: --";
            String reason = q.getServiceName() != null ? q.getServiceName() : "Khám bệnh";
            holder.tvSttReason.setText(stt + " • Lý do: " + reason);

            // Status badge
            String status = q.getStatus() != null ? q.getStatus() : "";
            switch (status) {
                case "IN_PROGRESS":
                    holder.tvStatusBadge.setText("Đang khám");
                    holder.tvStatusBadge.setTextColor(Color.parseColor("#2E7D32"));
                    holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_status_in_progress);
                    break;
                case "PAUSED_FOR_TEST":
                    holder.tvStatusBadge.setText("Chụp X-Quang");
                    holder.tvStatusBadge.setTextColor(Color.parseColor("#E65100"));
                    holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_status_waiting);
                    break;
                default: // WAITING or others
                    holder.tvStatusBadge.setText("Đang chờ");
                    holder.tvStatusBadge.setTextColor(Color.parseColor("#E65100"));
                    holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_status_waiting);
                    break;
            }

            // "Khám ngay" button: show only for WAITING
            if ("WAITING".equals(status)) {
                holder.btnKhamNgay.setVisibility(View.VISIBLE);
                holder.btnKhamNgay.setOnClickListener(v -> {
                    holder.btnKhamNgay.setEnabled(false);
                    api.callPatient(q.getId()).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (!isAdded()) return;
                            if (response.isSuccessful()) {
                                Toast.makeText(requireContext(),
                                        "Đã gọi: " + name, Toast.LENGTH_SHORT).show();
                                loadData();
                            } else {
                                holder.btnKhamNgay.setEnabled(true);
                                Toast.makeText(requireContext(),
                                        "Gọi thất bại", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            if (!isAdded()) return;
                            holder.btnKhamNgay.setEnabled(true);
                            Toast.makeText(requireContext(),
                                    "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            } else {
                holder.btnKhamNgay.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() { return items.size(); }

        class Holder extends RecyclerView.ViewHolder {
            TextView tvAvatar, tvPatientName, tvSttReason, tvStatusBadge;
            com.google.android.material.button.MaterialButton btnKhamNgay;

            Holder(View v) {
                super(v);
                tvAvatar = v.findViewById(R.id.tv_queue_avatar);
                tvPatientName = v.findViewById(R.id.tv_patient_name);
                tvSttReason = v.findViewById(R.id.tv_stt_reason);
                tvStatusBadge = v.findViewById(R.id.tv_status_badge);
                btnKhamNgay = v.findViewById(R.id.btn_kham_ngay);
            }
        }
    }

    // ─── Upcoming Appointment Adapter ──────────────────────────────────────────

    private static class HomeAppointmentAdapter
            extends RecyclerView.Adapter<HomeAppointmentAdapter.Holder> {
        private List<UpcomingAppointment> items = new ArrayList<>();

        void updateItems(List<UpcomingAppointment> list) {
            items = list != null ? list : new ArrayList<>();
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_home_appointment, parent, false);
            return new Holder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            UpcomingAppointment a = items.get(position);

            String dt = a.getAppointmentTime() != null ? a.getAppointmentTime()
                    : (a.getDatetime() != null ? a.getDatetime() : "");
            holder.tvTime.setText(formatTime(dt));
            holder.tvService.setText(a.getServiceName() != null ? a.getServiceName() : "Khám");
            String doctorName = a.getDoctorName() != null ? a.getDoctorName() : "";
            holder.tvPatient.setText(doctorName.isEmpty() ? "" : "BS. " + doctorName);
        }

        private String formatTime(String dt) {
            if (dt == null || dt.length() < 16) return dt != null ? dt : "";
            try {
                return dt.substring(11, 16) + " • " + dt.substring(0, 10);
            } catch (Exception e) {
                return dt;
            }
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class Holder extends RecyclerView.ViewHolder {
            TextView tvTime, tvService, tvPatient;

            Holder(View v) {
                super(v);
                tvTime = v.findViewById(R.id.tv_appt_time);
                tvService = v.findViewById(R.id.tv_appt_service);
                tvPatient = v.findViewById(R.id.tv_appt_patient);
            }
        }
    }
}
