package com.hcmute.mobile_android.ui.fragments;

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
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.models.DoctorItem;
import com.hcmute.mobile_android.ui.activities.DoctorDetailActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DoctorListFragment extends Fragment {

    private RecyclerView rvDoctors, rvFilters;
    private EditText etSearch;
    private DoctorFullAdapter doctorAdapter;
    private FilterAdapter filterAdapter;
    private ApiService apiService;
    private List<DoctorItem> allDoctors = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_doctor_list_tab, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        apiService = RetrofitClient.getApiService(requireContext());

        initViews(view);
        setupRecyclerViews();
        loadDoctors();
    }

    private void initViews(View view) {
        etSearch = view.findViewById(R.id.etSearch);
        rvDoctors = view.findViewById(R.id.rvDoctors);
        rvFilters = view.findViewById(R.id.rvFilters);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterDoctors(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupRecyclerViews() {
        rvFilters.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        List<String> categories = new ArrayList<>();
        categories.add("Tất cả");
        filterAdapter = new FilterAdapter(categories, this::onFilterSelected);
        rvFilters.setAdapter(filterAdapter);

        rvDoctors.setLayoutManager(new LinearLayoutManager(requireContext()));
        doctorAdapter = new DoctorFullAdapter();
        rvDoctors.setAdapter(doctorAdapter);
    }

    private void loadDoctors() {
        apiService.getDoctors().enqueue(new Callback<List<DoctorItem>>() {
            @Override
            public void onResponse(Call<List<DoctorItem>> call, Response<List<DoctorItem>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    allDoctors = response.body();
                    doctorAdapter.updateItems(allDoctors);
                    extractCategories(allDoctors);
                }
            }
            @Override
            public void onFailure(Call<List<DoctorItem>> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Lỗi tải danh sách bác sĩ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void extractCategories(List<DoctorItem> list) {
        List<String> cats = new ArrayList<>();
        cats.add("Tất cả");
        for (DoctorItem d : list) {
            String spec = d.getSpecialization();
            if (spec != null && !spec.isEmpty() && !cats.contains(spec)) {
                cats.add(spec);
            }
        }
        filterAdapter.updateData(cats);
    }

    private void filterDoctors(String query) {
        List<DoctorItem> filtered = new ArrayList<>();
        for (DoctorItem d : allDoctors) {
            String spec = d.getSpecialization() != null ? d.getSpecialization().toLowerCase() : "";
            if (d.getFullName().toLowerCase().contains(query.toLowerCase()) || 
                spec.contains(query.toLowerCase())) {
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
        List<DoctorItem> filtered = new ArrayList<>();
        for (DoctorItem d : allDoctors) {
            String spec = d.getSpecialization() != null ? d.getSpecialization() : "";
            if (spec.equalsIgnoreCase(category)) {
                filtered.add(d);
            }
        }
        doctorAdapter.updateItems(filtered);
    }

    // --- Adapters ---

    private static class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.Holder> {
        private List<String> items;
        private final OnFilterClickListener listener;
        private int selectedPos = 0;

        interface OnFilterClickListener { void onFilterClick(String cat); }

        FilterAdapter(List<String> list, OnFilterClickListener l) {
            this.items = new ArrayList<>(list);
            this.listener = l;
        }

        void updateData(List<String> list) {
            this.items = new ArrayList<>(list);
            this.selectedPos = 0;
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
            holder.tv.setText(cat);
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
            holder.tvName.setText(d.getFullName().startsWith("BS.") ? d.getFullName() : "BS. " + d.getFullName());
            holder.tvSpecialty.setText(d.getSpecialization() != null ? d.getSpecialization() : "Khám tổng quát");
            
            holder.tvLocation.setText((position % 2 == 0) ? "Cơ sở 1" : "Cơ sở 2");
            holder.tvTrusted.setText("4." + (9 - (position % 3)) + "k Đánh giá");

            int avatarRes = (position % 2 == 0) ? R.drawable.doctor_avatar_1 : R.drawable.doctor_avatar_2;
            holder.imgDoctor.setImageResource(avatarRes);

            // Hide booking button for this tab
            if (holder.btnSwipe != null) {
                holder.btnSwipe.setVisibility(View.GONE);
            }

            View.OnClickListener action = v -> {
                Intent intent = new Intent(v.getContext(), DoctorDetailActivity.class);
                intent.putExtra("doctorId", d.getId());
                intent.putExtra("doctorName", "BS. " + d.getFullName());
                intent.putExtra("specialization", d.getSpecialization());
                v.getContext().startActivity(intent);
            };

            holder.itemView.setOnClickListener(action);
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
