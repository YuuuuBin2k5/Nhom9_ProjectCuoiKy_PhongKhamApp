package com.hcmute.mobile_android.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.models.DoctorItem;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DoctorListActivity extends AppCompatActivity {

    private RecyclerView rvDoctors, rvFilters;
    private EditText etSearch;
    private DoctorFullAdapter doctorAdapter;
    private FilterAdapter filterAdapter;
    private ApiService apiService;
    private List<DoctorItem> allDoctors = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_list);

        apiService = RetrofitClient.getApiService(this);

        initViews();
        setupRecyclerViews();
        loadDoctors();
    }

    private void initViews() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        etSearch = findViewById(R.id.etSearch);
        rvDoctors = findViewById(R.id.rvDoctors);
        rvFilters = findViewById(R.id.rvFilters);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterDoctors(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupRecyclerViews() {
        // Filters
        rvFilters.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        List<String> categories = new ArrayList<>();
        categories.add("Tất cả");
        categories.add("Nha sĩ");
        categories.add("Phẫu thuật");
        categories.add("Chỉnh nha");
        filterAdapter = new FilterAdapter(categories, this::onFilterSelected);
        rvFilters.setAdapter(filterAdapter);

        // Doctors
        rvDoctors.setLayoutManager(new LinearLayoutManager(this));
        doctorAdapter = new DoctorFullAdapter();
        rvDoctors.setAdapter(doctorAdapter);
    }

    private void loadDoctors() {
        apiService.getDoctors().enqueue(new Callback<List<DoctorItem>>() {
            @Override
            public void onResponse(Call<List<DoctorItem>> call, Response<List<DoctorItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allDoctors = response.body();
                    doctorAdapter.updateItems(allDoctors);
                }
            }
            @Override
            public void onFailure(Call<List<DoctorItem>> call, Throwable t) {
                Toast.makeText(DoctorListActivity.this, "Lỗi tải danh sách bác sĩ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterDoctors(String query) {
        List<DoctorItem> filtered = new ArrayList<>();
        for (DoctorItem d : allDoctors) {
            if (d.getFullName().toLowerCase().contains(query.toLowerCase()) || 
                (d.getSpecialization() != null && d.getSpecialization().toLowerCase().contains(query.toLowerCase()))) {
                filtered.add(d);
            }
        }
        doctorAdapter.updateItems(filtered);
    }

    private void onFilterSelected(String category) {
        if (category.equals("Tất cả")) {
            doctorAdapter.updateItems(allDoctors);
            return;
        }
        // Simplified category filtering for mock
        List<DoctorItem> filtered = new ArrayList<>();
        for (DoctorItem d : allDoctors) {
            String spec = d.getSpecialization() != null ? d.getSpecialization().toLowerCase() : "";
            if (spec.contains(category.toLowerCase().substring(0, 3))) { // Match first 3 chars
                filtered.add(d);
            }
        }
        doctorAdapter.updateItems(filtered);
    }

    // --- Adapters ---

    private static class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.Holder> {
        private final List<String> items;
        private final OnFilterClickListener listener;
        private int selectedPos = 0;

        interface OnFilterClickListener { void onFilterClick(String cat); }

        FilterAdapter(List<String> list, OnFilterClickListener l) {
            this.items = list;
            this.listener = l;
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
            holder.tv.setText(cat);
            
            // Reusing item_category_premium logic or applying custom glass style
            holder.itemView.setAlpha(selectedPos == position ? 1.0f : 0.6f);
            holder.itemView.setOnClickListener(v -> {
                int old = selectedPos;
                selectedPos = position;
                notifyItemChanged(old);
                notifyItemChanged(selectedPos);
                listener.onFilterClick(cat);
            });
        }

        @Override public int getItemCount() { return items.size(); }
        static class Holder extends RecyclerView.ViewHolder {
            TextView tv;
            Holder(View v) { super(v); tv = v.findViewById(R.id.tvCategoryName); }
        }
    }

    private static class DoctorFullAdapter extends RecyclerView.Adapter<DoctorFullAdapter.Holder> {
        private List<DoctorItem> items = new ArrayList<>();

        void updateItems(List<DoctorItem> list) {
            this.items = list;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_doctor_premium_full, parent, false);
            return new Holder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            DoctorItem d = items.get(position);
            holder.tvName.setText(d.getFullName().startsWith("BS.") ? d.getFullName() : "Dr. " + d.getFullName());
            holder.tvSpecialty.setText(d.getSpecialization() != null ? d.getSpecialization() : "Khám & Chẩn đoán");
            
            // Premium mock info
            holder.tvLocation.setText((position % 2 == 0) ? "Hà Nội, Việt Nam" : "TP. Hồ Chí Minh");
            holder.tvTrusted.setText("4." + (9 - (position % 3)) + "k Đánh giá");

            // Premium Avatars
            int avatarRes = (position % 2 == 0) ? R.drawable.doctor_avatar_1 : R.drawable.doctor_avatar_2;
            holder.imgDoctor.setImageResource(avatarRes);

            View.OnClickListener action = v -> {
                Intent intent = new Intent(v.getContext(), DoctorDetailActivity.class);
                intent.putExtra("doctorId", d.getId());
                intent.putExtra("doctorName", "BS. " + d.getFullName());
                intent.putExtra("specialization", d.getSpecialization());
                v.getContext().startActivity(intent);
            };

            holder.itemView.setOnClickListener(action);
            holder.btnSwipe.setOnClickListener(action);
        }

        @Override public int getItemCount() { return items.size(); }
        static class Holder extends RecyclerView.ViewHolder {
            TextView tvName, tvSpecialty, tvLocation, tvTrusted;
            ImageView imgDoctor;
            View btnSwipe;
            Holder(View v) {
                super(v);
                tvName = v.findViewById(R.id.tv_doctor_name);
                tvSpecialty = v.findViewById(R.id.tv_specialty);
                tvLocation = v.findViewById(R.id.tv_location);
                tvTrusted = v.findViewById(R.id.tv_trusted);
                imgDoctor = v.findViewById(R.id.img_doctor);
                btnSwipe = v.findViewById(R.id.btn_swipe_to_book);
            }
        }
    }
}
