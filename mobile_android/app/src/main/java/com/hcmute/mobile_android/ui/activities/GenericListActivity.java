package com.hcmute.mobile_android.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.adapters.PatientServiceAdapter;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.models.DoctorItem;
import com.hcmute.mobile_android.network.models.ServiceItem;
import com.hcmute.mobile_android.network.models.UpcomingAppointment;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GenericListActivity extends AppCompatActivity {

    public static final String EXTRA_MODE = "mode";
    public static final String MODE_APPOINTMENTS = "appointments";
    public static final String MODE_SERVICES = "services";
    public static final String MODE_DOCTORS = "doctors";

    private TextView tvEmpty;
    private RecyclerView recycler;
    private SimpleAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generic_list);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        tvEmpty = findViewById(R.id.tvEmpty);
        recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SimpleAdapter();
        recycler.setAdapter(adapter);

        String mode = getIntent().getStringExtra(EXTRA_MODE);
        if (MODE_APPOINTMENTS.equals(mode)) {
            toolbar.setTitle("Lịch hẹn");
            loadAppointments();
        } else if (MODE_SERVICES.equals(mode)) {
            toolbar.setTitle("Dịch vụ");
            loadServices();
        } else {
            toolbar.setTitle("Bác sĩ");
            loadDoctors();
        }
    }

    private void loadAppointments() {
        ApiService api = RetrofitClient.getApiService(this);
        api.getUpcomingAppointments().enqueue(new Callback<List<UpcomingAppointment>>() {
            @Override
            public void onResponse(Call<List<UpcomingAppointment>> call, Response<List<UpcomingAppointment>> response) {
                List<SimpleRow> rows = new ArrayList<>();
                if (response.isSuccessful() && response.body() != null) {
                    for (UpcomingAppointment a : response.body()) {
                        rows.add(new SimpleRow(
                                a.getServiceName() != null ? a.getServiceName() : "Khám tổng quát",
                                "BS. " + (a.getDoctorName() != null ? a.getDoctorName() : "") + " • " + (a.getDatetime() != null ? a.getDatetime() : ""),
                                a
                        ));
                    }
                }
                showRows(rows);
            }

            @Override
            public void onFailure(Call<List<UpcomingAppointment>> call, Throwable t) {
                showRows(new ArrayList<>());
            }
        });
    }

    private void loadServices() {
        ApiService api = RetrofitClient.getApiService(this);
        api.getServices().enqueue(new Callback<List<ServiceItem>>() {
            @Override
            public void onResponse(Call<List<ServiceItem>> call, Response<List<ServiceItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ServiceItem> services = response.body();
                    tvEmpty.setVisibility(services.isEmpty() ? View.VISIBLE : View.GONE);
                    recycler.setVisibility(services.isEmpty() ? View.GONE : View.VISIBLE);
                    
                    PatientServiceAdapter serviceAdapter = new PatientServiceAdapter(services, service -> {
                        Intent intent = new Intent(GenericListActivity.this, ServiceDetailActivity.class);
                        intent.putExtra("id", service.getId());
                        intent.putExtra("name", service.getName());
                        intent.putExtra("price", service.getPrice());
                        intent.putExtra("duration", service.getDurationMinutes() != null ? service.getDurationMinutes() : 0);
                        intent.putExtra("description", service.getDescription());
                        intent.putExtra("category", service.getCategoryName());
                        if (service.getImageUrls() != null) {
                            intent.putStringArrayListExtra("imageUrls", new java.util.ArrayList<>(service.getImageUrls()));
                        }
                        startActivity(intent);
                    });
                    recycler.setAdapter(serviceAdapter);
                } else {
                    showRows(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<List<ServiceItem>> call, Throwable t) {
                showRows(new ArrayList<>());
            }
        });
    }

    private void loadDoctors() {
        ApiService api = RetrofitClient.getApiService(this);
        api.getDoctors().enqueue(new Callback<List<DoctorItem>>() {
            @Override
            public void onResponse(Call<List<DoctorItem>> call, Response<List<DoctorItem>> response) {
                List<SimpleRow> rows = new ArrayList<>();
                if (response.isSuccessful() && response.body() != null) {
                    for (DoctorItem d : response.body()) {
                        rows.add(new SimpleRow(
                                "BS. " + d.getFullName(),
                                d.getSpecialization() != null ? d.getSpecialization() : "Bác sĩ đa khoa",
                                d
                        ));
                    }
                }
                showRows(rows);
            }

            @Override
            public void onFailure(Call<List<DoctorItem>> call, Throwable t) {
                showRows(new ArrayList<>());
            }
        });
    }

    private void showRows(List<SimpleRow> rows) {
        adapter.setRows(rows);
        tvEmpty.setVisibility(rows.isEmpty() ? View.VISIBLE : View.GONE);
        recycler.setVisibility(rows.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void onRowClick(SimpleRow row) {
        if (row.data instanceof ServiceItem) {
            ServiceItem s = (ServiceItem) row.data;
            Intent intent = new Intent(this, ServiceDetailActivity.class);
            intent.putExtra("id", s.getId());
            intent.putExtra("name", s.getName());
            intent.putExtra("price", s.getPrice());
            intent.putExtra("duration", s.getDurationMinutes() != null ? s.getDurationMinutes() : 0);
            intent.putExtra("description", s.getDescription());
            intent.putExtra("category", s.getCategoryName());
            if (s.getImageUrls() != null) {
                intent.putStringArrayListExtra("imageUrls", new java.util.ArrayList<>(s.getImageUrls()));
            }
            startActivity(intent);
        } else if (row.data instanceof DoctorItem) {
            DoctorItem d = (DoctorItem) row.data;
            Intent intent = new Intent(this, DoctorDetailActivity.class);
            intent.putExtra("doctorId", d.getId());
            intent.putExtra("doctorName", "BS. " + d.getFullName());
            intent.putExtra("specialization", d.getSpecialization());
            startActivity(intent);
        } else if (row.data instanceof UpcomingAppointment) {
            UpcomingAppointment a = (UpcomingAppointment) row.data;
            Intent intent = new Intent(this, AppointmentDetailActivity.class);
            intent.putExtra("appointmentId", a.getId());
            intent.putExtra("datetime", a.getAppointmentTime());
            intent.putExtra("serviceName", a.getServiceName());
            intent.putExtra("doctorName", a.getDoctorName());
            intent.putExtra("status", a.getStatus());
            startActivity(intent);
        }
    }

    private static class SimpleRow {
        final String title;
        final String subtitle;
        final Object data;
        SimpleRow(String title, String subtitle, Object data) {
            this.title = title;
            this.subtitle = subtitle;
            this.data = data;
        }
    }

    private class SimpleAdapter extends RecyclerView.Adapter<SimpleAdapter.Holder> {
        private List<SimpleRow> rows = new ArrayList<>();

        void setRows(List<SimpleRow> rows) {
            this.rows = rows;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_simple_row, parent, false);
            return new Holder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            SimpleRow row = rows.get(position);
            holder.tvTitle.setText(row.title);
            holder.tvSubtitle.setText(row.subtitle);
            holder.itemView.setOnClickListener(v -> onRowClick(row));
        }

        @Override
        public int getItemCount() {
            return rows.size();
        }

        class Holder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvSubtitle;
            Holder(@NonNull View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                tvSubtitle = itemView.findViewById(R.id.tvSubtitle);
            }
        }
    }
}
