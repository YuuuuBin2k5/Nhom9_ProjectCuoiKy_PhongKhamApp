package com.hcmute.mobile_android.ui.activities;

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
                                "BS. " + (a.getDoctorName() != null ? a.getDoctorName() : "") + " • " + (a.getDatetime() != null ? a.getDatetime() : "")
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
                List<SimpleRow> rows = new ArrayList<>();
                if (response.isSuccessful() && response.body() != null) {
                    for (ServiceItem s : response.body()) {
                        String subtitle = "Giá: " + ((long) s.getPrice()) + "đ";
                        rows.add(new SimpleRow(
                                s.getName() != null ? s.getName() : "Dịch vụ",
                                subtitle
                        ));
                    }
                }
                showRows(rows);
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
                                d.getSpecialization() != null ? d.getSpecialization() : "Bác sĩ đa khoa"
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

    private static class SimpleRow {
        final String title;
        final String subtitle;
        SimpleRow(String title, String subtitle) {
            this.title = title;
            this.subtitle = subtitle;
        }
    }

    private static class SimpleAdapter extends RecyclerView.Adapter<SimpleAdapter.Holder> {
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
        }

        @Override
        public int getItemCount() {
            return rows.size();
        }

        static class Holder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvSubtitle;
            Holder(@NonNull View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                tvSubtitle = itemView.findViewById(R.id.tvSubtitle);
            }
        }
    }
}
