package com.hcmute.mobile_android.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.hcmute.mobile_android.util.ToastUtils;

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
        void onNavigateToNotifications();
    }

    private HomeCallbacks callbacks;

    // View references
    private TextView tvDoctorName;
    private TextView tvStatToday;
    private TextView tvStatAppointments;
    private TextView tvStatPlans;
    private RecyclerView rvQueue;
    private RecyclerView rvTransferred;
    private View layoutTransferred;
    private RecyclerView rvUpcoming;
    private TextView tvQueueEmpty;
    private TextView tvUpcomingEmpty;

    // Adapters
    private HomeQueueAdapter queueAdapter;
    private HomeQueueAdapter transferredAdapter;
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
        rvTransferred = view.findViewById(R.id.rv_transferred);
        layoutTransferred = view.findViewById(R.id.layout_transferred);
        rvUpcoming = view.findViewById(R.id.rv_upcoming);
        tvQueueEmpty = view.findViewById(R.id.tv_queue_empty);
        tvUpcomingEmpty = view.findViewById(R.id.tv_upcoming_empty);

        // Setup RecyclerViews
        queueAdapter = new HomeQueueAdapter();
        transferredAdapter = new HomeQueueAdapter();
        appointmentAdapter = new HomeAppointmentAdapter();

        rvQueue.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvQueue.setAdapter(queueAdapter);
        rvQueue.setNestedScrollingEnabled(false);

        rvTransferred.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvTransferred.setAdapter(transferredAdapter);
        rvTransferred.setNestedScrollingEnabled(false);

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
        view.findViewById(R.id.btn_new_record).setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), DoctorWorkflowActivity.class));
        });
        view.findViewById(R.id.btn_scan_qr).setOnClickListener(v -> {
            // Launch QR scanner, passing flag to open camera immediately
            Intent intent = new Intent(requireContext(), DoctorWorkflowActivity.class);
            intent.putExtra("OPEN_SCANNER", true);
            startActivity(intent);
        });
        view.findViewById(R.id.btn_tooth_chart).setOnClickListener(v -> {
            ToastUtils.showCenteredToast(requireContext(), "Sơ đồ răng nhanh - Đang phát triển");
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
        api.getDoctorQueue().enqueue(new Callback<com.hcmute.mobile_android.network.models.DoctorQueueResponse>() {
            @Override
            public void onResponse(Call<com.hcmute.mobile_android.network.models.DoctorQueueResponse> call, Response<com.hcmute.mobile_android.network.models.DoctorQueueResponse> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    com.hcmute.mobile_android.network.models.DoctorQueueResponse data = response.body();
                    
                    // Parse Queued Patients
                    List<QueueItem> active = new ArrayList<>();
                    if (data.getQueuedPatients() != null) {
                        for (QueueItem q : data.getQueuedPatients()) {
                            if (!"COMPLETED".equals(q.getStatus()) && !"SKIPPED".equals(q.getStatus())) {
                                active.add(q);
                            }
                        }
                    }
                    updateQueueUI(active);
                    tvStatToday.setText("Hôm nay: " + (data.getQueuedPatients() != null ? data.getQueuedPatients().size() : 0) + " BN");
                    
                    // Parse Transferred Patients
                    List<QueueItem> transferred = new ArrayList<>();
                    if (data.getTransferredPatients() != null) {
                        for (QueueItem q : data.getTransferredPatients()) {
                            if (!"COMPLETED".equals(q.getStatus()) && !"SKIPPED".equals(q.getStatus())) {
                                transferred.add(q);
                            }
                        }
                    }
                    updateTransferredUI(transferred);
                } else {
                    updateQueueUI(new ArrayList<>());
                    updateTransferredUI(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<com.hcmute.mobile_android.network.models.DoctorQueueResponse> call, Throwable t) {
                if (!isAdded()) return;
                updateQueueUI(new ArrayList<>());
                updateTransferredUI(new ArrayList<>());
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

    private void updateTransferredUI(List<QueueItem> items) {
        if (items.isEmpty()) {
            layoutTransferred.setVisibility(View.GONE);
        } else {
            layoutTransferred.setVisibility(View.VISIBLE);
            transferredAdapter.updateItems(items);
        }
    }

    private void loadUpcoming(ApiService api) {
        api.getDoctorUpcomingAppointments().enqueue(new Callback<List<UpcomingAppointment>>() {
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

            // --- Visual: IN_PROGRESS = bright, WAITING = slightly dim ---
            String status = q.getStatus() != null ? q.getStatus() : "";
            boolean isActive = "IN_PROGRESS".equals(status);
            holder.itemView.setAlpha(isActive ? 1.0f : 0.72f);
            holder.itemView.setClickable(true);

            // --- Open patient profile on card click ---
            holder.itemView.setOnClickListener(v -> {
                if (q.getPatientId() != null) {
                    android.content.Intent intent = new android.content.Intent(
                            requireContext(), DoctorWorkflowActivity.class);
                    intent.putExtra(DoctorWorkflowActivity.EXTRA_INITIAL_QR,
                            "patient:" + q.getPatientId());
                    startActivity(intent);
                } else {
                    ToastUtils.showCenteredToast(requireContext(), "Không tìm thấy thông tin bệnh nhân");
                }
            });

            // Avatar: first letter of patient name
            String name = q.getPatientName() != null ? q.getPatientName() : "?";
            holder.tvAvatar.setText(name.length() > 0
                    ? String.valueOf(name.charAt(0)).toUpperCase() : "?");

            int[] colors = {
                    Color.parseColor("#1565C0"), Color.parseColor("#00695C"),
                    Color.parseColor("#6A1B9A"), Color.parseColor("#BF360C"),
                    Color.parseColor("#37474F")
            };
            holder.tvAvatar.getBackground().setTint(colors[position % colors.length]);

            holder.tvPatientName.setText(name);

            String stt = q.getQueueNumber() != null
                    ? String.format("STT: %02d", q.getQueueNumber()) : "STT: --";
            String reason = q.getServiceName() != null ? q.getServiceName() : "Khám bệnh";
            holder.tvSttReason.setText(stt + " • " + reason);

            // Action buttons: only for first patient in WAITING/RETURNED_PRIORITY
            boolean isFirstWaiting = position == 0 &&
                    ("WAITING".equals(status) || "RETURNED_PRIORITY".equals(status));
            holder.llActionButtons.setVisibility(isFirstWaiting ? View.VISIBLE : View.GONE);

            if (isFirstWaiting) {
                holder.btnCall.setOnClickListener(v -> {
                    ApiService api = RetrofitClient.getApiService(holder.itemView.getContext());
                    api.callPatientToRoom(q.getId()).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> res) {
                            ToastUtils.showCenteredToast(holder.itemView.getContext(),
                                    res.isSuccessful() ? "Đã gọi: " + name : "Lỗi khi gọi");
                            if (res.isSuccessful() && isAdded()) loadData();
                        }
                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            ToastUtils.showCenteredToast(holder.itemView.getContext(), "Lỗi mạng");
                        }
                    });
                });
                holder.btnDelay.setOnClickListener(v -> {
                    ApiService api = RetrofitClient.getApiService(holder.itemView.getContext());
                    api.delayPatient(q.getId()).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> res) {
                            ToastUtils.showCenteredToast(holder.itemView.getContext(),
                                    res.isSuccessful() ? "Đã lùi lượt " + name : "Lỗi khi lùi");
                            if (res.isSuccessful() && isAdded()) loadData();
                        }
                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            ToastUtils.showCenteredToast(holder.itemView.getContext(), "Lỗi mạng");
                        }
                    });
                });
            }

            // Status badge
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
                case "RETURNED_PRIORITY":
                    holder.tvStatusBadge.setText("Ưu tiên");
                    holder.tvStatusBadge.setTextColor(Color.parseColor("#F57F17"));
                    holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_status_waiting);
                    break;
                default:
                    holder.tvStatusBadge.setText("Đang chờ");
                    holder.tvStatusBadge.setTextColor(Color.parseColor("#E65100"));
                    holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_status_waiting);
                    break;
            }
        }

        @Override
        public int getItemCount() { return items.size(); }

        class Holder extends RecyclerView.ViewHolder {
            TextView tvAvatar, tvPatientName, tvSttReason, tvStatusBadge;
            View llActionButtons;
            View btnCall, btnDelay;

            Holder(View v) {
                super(v);
                tvAvatar = v.findViewById(R.id.tv_queue_avatar);
                tvPatientName = v.findViewById(R.id.tv_patient_name);
                tvSttReason = v.findViewById(R.id.tv_stt_reason);
                tvStatusBadge = v.findViewById(R.id.tv_status_badge);
                llActionButtons = v.findViewById(R.id.ll_action_buttons);
                btnCall = v.findViewById(R.id.btn_call);
                btnDelay = v.findViewById(R.id.btn_delay);
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
            String patientName = a.getPatientName() != null ? a.getPatientName() : "";
            holder.tvPatient.setText(patientName.isEmpty() ? "" : patientName);
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
