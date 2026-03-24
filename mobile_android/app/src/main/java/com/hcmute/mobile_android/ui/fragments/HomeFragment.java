package com.hcmute.mobile_android.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.models.DoctorItem;
import com.hcmute.mobile_android.network.models.PatientMeResponse;
import com.hcmute.mobile_android.network.models.ServiceItem;
import com.hcmute.mobile_android.network.models.UpcomingAppointment;
import com.hcmute.mobile_android.ui.activities.AppointmentDetailActivity;
import com.hcmute.mobile_android.ui.activities.BookAppointmentActivity;
import com.hcmute.mobile_android.ui.activities.GenericListActivity;
import com.hcmute.mobile_android.ui.activities.LoginActivity;
import com.hcmute.mobile_android.ui.activities.MainActivity;
import com.hcmute.mobile_android.ui.activities.ProfileActivity;
import com.hcmute.mobile_android.ui.activities.ServiceDetailActivity;
import com.hcmute.mobile_android.util.TokenManager;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    public interface HomeCallbacks {
        void onNavigateToQr();
        void onNavigateToNotifications();
    }

    private HomeCallbacks callbacks;
    private TextView tvGreeting, tvUserName, tvApptDoctorName, tvApptService, tvApptTime;
    private CardView cardAppointment;
    private LinearLayout layoutNoAppointment;
    private LinearLayout layoutAppointmentDetail;
    private RecyclerView rvServices, rvDoctors, rvCategories;
    private ServiceAdapter serviceAdapter;
    private DoctorAdapter doctorAdapter;
    private CategoryAdapter categoryAdapter;
    private UpcomingAppointment latestUpcoming;
    private List<ServiceItem> allServices = new ArrayList<>();

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof HomeCallbacks) {
            callbacks = (HomeCallbacks) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvGreeting = view.findViewById(R.id.tv_greeting);
        tvUserName = view.findViewById(R.id.tv_user_name);
        cardAppointment = view.findViewById(R.id.cv_appointment);
        layoutNoAppointment = view.findViewById(R.id.layout_no_appointment);
        layoutAppointmentDetail = view.findViewById(R.id.layout_appointment_detail);
        tvApptDoctorName = view.findViewById(R.id.tv_appointment_doctor_name);
        tvApptService = view.findViewById(R.id.tv_appointment_service_name);
        tvApptTime = view.findViewById(R.id.tv_appointment_time);
        rvServices = view.findViewById(R.id.rv_services);
        rvDoctors = view.findViewById(R.id.rv_doctors);
        rvCategories = view.findViewById(R.id.rv_categories);

        MaterialButton btnViewDetail = view.findViewById(R.id.btn_view_appointment_detail);

        View btnQrScan = view.findViewById(R.id.btn_qr_scan);
        if (btnQrScan != null) {
            btnQrScan.setOnClickListener(v -> navigateToQr());
        }
        view.findViewById(R.id.iv_avatar).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), ProfileActivity.class)));
        view.findViewById(R.id.iv_notification).setOnClickListener(v -> {
            if (callbacks != null) callbacks.onNavigateToNotifications();
        });
        view.findViewById(R.id.ivHeaderLogout).setOnClickListener(v -> doLogout());
        view.findViewById(R.id.all_appointment).setOnClickListener(v -> openList(GenericListActivity.MODE_APPOINTMENTS));
        view.findViewById(R.id.all_dv).setOnClickListener(v -> openList(GenericListActivity.MODE_SERVICES));
        view.findViewById(R.id.all_bs).setOnClickListener(v -> openList(GenericListActivity.MODE_DOCTORS));
        view.findViewById(R.id.all_dv_cat).setOnClickListener(v -> openList(GenericListActivity.MODE_SERVICES));
        btnViewDetail.setOnClickListener(v -> openAppointmentDetail());

        ExtendedFloatingActionButton fabEmergency = view.findViewById(R.id.fabEmergency);
        if (fabEmergency != null) {
            fabEmergency.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Đang kết nối đường dây khẩn cấp...", Toast.LENGTH_SHORT).show());
        }

        rvServices.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        rvDoctors.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        rvCategories.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        
        serviceAdapter = new ServiceAdapter();
        doctorAdapter = new DoctorAdapter();
        categoryAdapter = new CategoryAdapter();
        
        rvServices.setAdapter(serviceAdapter);
        rvDoctors.setAdapter(doctorAdapter);
        rvCategories.setAdapter(categoryAdapter);

        loadData();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getView() != null) loadData();
    }

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

    private void loadData() {
        ApiService api = RetrofitClient.getApiService(requireContext());
        api.getPatientMe().enqueue(new Callback<PatientMeResponse>() {
            @Override
            public void onResponse(Call<PatientMeResponse> call, Response<PatientMeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PatientMeResponse p = response.body();
                    String name = (p.getFirstName() != null ? p.getFirstName() + " " : "") + (p.getLastName() != null ? p.getLastName() : "");
                    if (name.trim().isEmpty()) name = "Bạn";
                    tvGreeting.setText(getGreetingShort());
                    tvUserName.setText(name);
                } else {
                    tvGreeting.setText(getGreetingShort());
                    tvUserName.setText("Bạn");
                }
                loadUpcoming(api);
                loadServices(api);
                loadDoctors(api);
            }

            @Override
            public void onFailure(Call<PatientMeResponse> call, Throwable t) {
                tvGreeting.setText(getGreetingShort());
                tvUserName.setText("Bạn");
                loadUpcoming(api);
                loadServices(api);
                loadDoctors(api);
            }
        });
    }

    private String getGreetingShort() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour < 12) return "Chào sáng,";
        if (hour < 18) return "Chào chiều,";
        return "Chào tối,";
    }

    private void loadUpcoming(ApiService api) {
        api.getUpcomingAppointments().enqueue(new Callback<List<UpcomingAppointment>>() {
            @Override
            public void onResponse(Call<List<UpcomingAppointment>> call, Response<List<UpcomingAppointment>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    UpcomingAppointment a = response.body().get(0);
                    latestUpcoming = a;
                    cardAppointment.setVisibility(View.VISIBLE);
                    layoutNoAppointment.setVisibility(View.GONE);
                    layoutAppointmentDetail.setVisibility(View.VISIBLE);
                    tvApptDoctorName.setText("BS. " + (a.getDoctorName() != null ? a.getDoctorName() : ""));
                    tvApptService.setText(a.getServiceName() != null ? a.getServiceName() : "Khám");
                    tvApptTime.setText(formatDatetime(a.getDatetime()));
                } else {
                    latestUpcoming = null;
                    cardAppointment.setVisibility(View.VISIBLE);
                    layoutNoAppointment.setVisibility(View.VISIBLE);
                    layoutAppointmentDetail.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<List<UpcomingAppointment>> call, Throwable t) {
                latestUpcoming = null;
                cardAppointment.setVisibility(View.VISIBLE);
                layoutNoAppointment.setVisibility(View.VISIBLE);
                layoutAppointmentDetail.setVisibility(View.GONE);
            }
        });
    }

    private void openList(String mode) {
        Intent i = new Intent(requireContext(), GenericListActivity.class);
        i.putExtra(GenericListActivity.EXTRA_MODE, mode);
        startActivity(i);
    }

    private void openAppointmentDetail() {
        if (latestUpcoming == null) {
            Toast.makeText(requireContext(), "Chưa có lịch hẹn để xem chi tiết", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent i = new Intent(requireContext(), AppointmentDetailActivity.class);
        i.putExtra("appointmentId", latestUpcoming.getId());
        i.putExtra("datetime", latestUpcoming.getDatetime());
        i.putExtra("serviceName", latestUpcoming.getServiceName());
        i.putExtra("doctorName", latestUpcoming.getDoctorName());
        i.putExtra("status", latestUpcoming.getStatus());
        startActivity(i);
    }

    private void loadServices(ApiService api) {
        api.getServices().enqueue(new Callback<List<ServiceItem>>() {
            @Override
            public void onResponse(Call<List<ServiceItem>> call, Response<List<ServiceItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allServices = response.body();
                    serviceAdapter.updateItems(allServices);
                    extractCategories(allServices);
                }
            }

            @Override
            public void onFailure(Call<List<ServiceItem>> call, Throwable t) {
                allServices = new ArrayList<>();
                serviceAdapter.updateItems(allServices);
            }
        });
    }

    private void loadDoctors(ApiService api) {
        api.getDoctors().enqueue(new Callback<List<DoctorItem>>() {
            @Override
            public void onResponse(Call<List<DoctorItem>> call, Response<List<DoctorItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    doctorAdapter.updateItems(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<DoctorItem>> call, Throwable t) {
                doctorAdapter.updateItems(new ArrayList<>());
            }
        });
    }

    private String formatDatetime(String dt) {
        if (dt == null || dt.isEmpty()) return "";
        try {
            if (dt.length() >= 16) {
                return dt.substring(0, 10) + " lúc " + dt.substring(11, 16);
            }
            return dt;
        } catch (Exception e) {
            return dt;
        }
    }

    private void filterServices(String category) {
        if (category == null || category.equals("All")) {
            serviceAdapter.updateItems(allServices);
            return;
        }
        List<ServiceItem> filtered = new ArrayList<>();
        for (ServiceItem s : allServices) {
            if (category.equals(s.getCategoryName())) {
                filtered.add(s);
            }
        }
        serviceAdapter.updateItems(filtered);
    }

    private void extractCategories(List<ServiceItem> list) {
        List<String> cats = new ArrayList<>();
        for (ServiceItem s : list) {
            String c = s.getCategoryName();
            if (c != null && !c.isEmpty() && !cats.contains(c)) {
                cats.add(c);
            }
        }
        if (categoryAdapter != null) {
            categoryAdapter.updateItems(cats);
        }
    }

    private static class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.Holder> {
        private List<ServiceItem> items = new ArrayList<>();

        ServiceAdapter() {}

        void updateItems(List<ServiceItem> list) {
            items = list != null ? list : new ArrayList<>();
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_service_premium, parent, false);
            return new Holder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            ServiceItem s = items.get(position);
            holder.tvName.setText(s.getName() != null ? s.getName() : "Dịch vụ");
            holder.tvPrice.setText(formatPrice(s.getPrice()));
            int dur = s.getDurationMinutes() != null ? s.getDurationMinutes() : 0;
            holder.tvDuration.setText(dur + " phút •");
            
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), ServiceDetailActivity.class);
                intent.putExtra("id", s.getId());
                intent.putExtra("name", s.getName());
                intent.putExtra("price", s.getPrice());
                intent.putExtra("duration", s.getDurationMinutes() != null ? s.getDurationMinutes() : 0);
                intent.putExtra("description", s.getDescription());
                intent.putExtra("category", s.getCategoryName());
                v.getContext().startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        private String formatPrice(double p) {
            return NumberFormat.getNumberInstance(new Locale("vi", "VN")).format((long) p) + "đ";
        }

        static class Holder extends RecyclerView.ViewHolder {
            TextView tvName, tvPrice, tvDuration;

            Holder(View v) {
                super(v);
                tvName = v.findViewById(R.id.tvServiceName);
                tvPrice = v.findViewById(R.id.tvServicePrice);
                tvDuration = v.findViewById(R.id.tvDuration);
            }
        }
    }

    private static class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.Holder> {
        private List<DoctorItem> items = new ArrayList<>();

        DoctorAdapter() {}

        void updateItems(List<DoctorItem> list) {
            items = list != null ? list : new ArrayList<>();
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_doctor_suggested_premium, parent, false);
            return new Holder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            DoctorItem d = items.get(position);
            holder.tvName.setText("BS. " + d.getFullName());
            holder.tvSpecialization.setText(d.getSpecialization() != null && !d.getSpecialization().isEmpty()
                    ? d.getSpecialization() : "Bác sĩ Gia đình");
            holder.tvRating.setText("4." + (8 - (position % 4))); // Mock rating
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class Holder extends RecyclerView.ViewHolder {
            TextView tvName, tvSpecialization, tvRating;

            Holder(View v) {
                super(v);
                tvName = v.findViewById(R.id.tvDoctorName);
                tvSpecialization = v.findViewById(R.id.tvSpecialization);
                tvRating = v.findViewById(R.id.tvRating);
            }
        }
    }
    
    private class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.Holder> {
        private List<String> items = new ArrayList<>();
        private int selectedPosition = 0;

        CategoryAdapter() {}

        void updateItems(List<String> list) {
            items = new ArrayList<>(list != null ? list : new ArrayList<>());
            if (!items.contains("All")) {
                items.add(0, "All");
            }
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_premium, parent, false);
            return new Holder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            String cat = items.get(position);
            holder.tvName.setText(cat.equals("All") ? "Tất cả" : cat);

            if (position == selectedPosition) {
                holder.flBg.setBackgroundResource(R.drawable.bg_category_icon_premium);
                holder.tvName.setTextColor(android.graphics.Color.parseColor("#3B82F6"));
            } else {
                holder.flBg.setBackgroundColor(android.graphics.Color.TRANSPARENT);
                holder.tvName.setTextColor(android.graphics.Color.parseColor("#757575"));
            }

            holder.itemView.setOnClickListener(v -> {
                int oldPos = selectedPosition;
                selectedPosition = position;
                notifyItemChanged(oldPos);
                notifyItemChanged(selectedPosition);

                // Navigate to BookAppointmentActivity
                Intent intent = new Intent(v.getContext(), BookAppointmentActivity.class);
                String catArg = cat.equals("All") ? "" : cat;
                intent.putExtra(BookAppointmentActivity.EXTRA_CATEGORY, catArg);
                v.getContext().startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class Holder extends RecyclerView.ViewHolder {
            TextView tvName;
            View flBg;

            Holder(View v) {
                super(v);
                tvName = v.findViewById(R.id.tvCategoryName);
                flBg = v.findViewById(R.id.flCategoryBg);
            }
        }
    }
}
