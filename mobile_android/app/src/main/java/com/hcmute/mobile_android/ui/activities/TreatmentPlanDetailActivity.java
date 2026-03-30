package com.hcmute.mobile_android.ui.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.hcmute.mobile_android.BuildConfig;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.models.TreatmentPlan;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TreatmentPlanDetailActivity extends AppCompatActivity {

    private Long planId;
    private ProgressBar progressDetail;
    private TextView tvPlanStatus, tvPlanDate, tvPlanProgress;
    private RecyclerView rvTimeline;
    private TimelineAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_treatment_plan_detail);

        planId = getIntent().getLongExtra("planId", -1L);
        if (planId == -1L) {
            Toast.makeText(this, "Không tìm thấy phác đồ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        loadPlanDetails();
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        progressDetail = findViewById(R.id.progressPlanDetail);
        tvPlanStatus = findViewById(R.id.tvPlanStatus);
        tvPlanDate = findViewById(R.id.tvPlanDate);
        tvPlanProgress = findViewById(R.id.tvPlanProgress);
        rvTimeline = findViewById(R.id.rvTimeline);

        adapter = new TimelineAdapter(new ArrayList<>());
        rvTimeline.setLayoutManager(new LinearLayoutManager(this));
        rvTimeline.setAdapter(adapter);
    }

    private void loadPlanDetails() {
        progressDetail.setVisibility(View.VISIBLE);
        ApiService api = RetrofitClient.getApiService(this);
        api.getTreatmentPlan(planId).enqueue(new Callback<TreatmentPlan>() {
            @Override
            public void onResponse(@NonNull Call<TreatmentPlan> call, @NonNull Response<TreatmentPlan> response) {
                progressDetail.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    bindData(response.body());
                } else {
                    fallbackToOfflineJson(response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<TreatmentPlan> call, @NonNull Throwable t) {
                progressDetail.setVisibility(View.GONE);
                Toast.makeText(TreatmentPlanDetailActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fallbackToOfflineJson(int errorCode) {
        String jsonStr = getIntent().getStringExtra("planFallbackJson");
        if (jsonStr != null && !jsonStr.isEmpty()) {
            Toast.makeText(this, "Sử dụng dữ liệu offline", Toast.LENGTH_SHORT).show();
            // Convert Summary to full Plan via Gson equivalent
            com.hcmute.mobile_android.network.models.TreatmentPlanSummary summary = new com.google.gson.Gson().fromJson(jsonStr, com.hcmute.mobile_android.network.models.TreatmentPlanSummary.class);
            TreatmentPlan fakePlan = new TreatmentPlan();
            fakePlan.setStatus(summary.getStatus());
            fakePlan.setCreatedAt(summary.getCreatedAt());
            List<TreatmentPlan.Step> fakeSteps = new ArrayList<>();
            if (summary.getSteps() != null) {
                for (com.hcmute.mobile_android.network.models.TreatmentStepSummary sSum : summary.getSteps()) {
                    TreatmentPlan.Step fs = new TreatmentPlan.Step();
                    fs.setServiceName(sSum.getServiceName());
                    fs.setRoomName(sSum.getRoomName());
                    fs.setStatus(sSum.getStatus());
                    fakeSteps.add(fs);
                }
            }
            fakePlan.setSteps(fakeSteps);
            bindData(fakePlan);
        } else {
            Toast.makeText(TreatmentPlanDetailActivity.this, "Không thể tải cấu hình (Mã: " + errorCode + ")", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void bindData(TreatmentPlan plan) {
        tvPlanStatus.setText(plan.getStatusDisplay());
        
        String created = plan.getCreatedAt();
        if (created != null && created.length() >= 10) {
            tvPlanDate.setText(created.substring(0, 10));
        } else {
            tvPlanDate.setText("Chưa rõ");
        }

        List<TreatmentPlan.Step> steps = plan.getSteps();
        if (steps != null) {
            int completed = 0;
            for (TreatmentPlan.Step s : steps) {
                if (s.isCompleted()) completed++;
            }
            tvPlanProgress.setText(completed + "/" + steps.size() + " bước hoàn thành");
            adapter.updateItems(steps);
        }
    }

    private String resolveMediaUrl(String path) {
        if (path == null || path.isEmpty()) return null;
        if (path.startsWith("http://") || path.startsWith("https://")) return path;
        String base = BuildConfig.API_BASE_URL;
        if (!base.endsWith("/")) base = base + "/";
        String p = path.startsWith("/") ? path.substring(1) : path;
        return base + p;
    }

    // --- Adapters ---

    private class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.Holder> {
        private List<TreatmentPlan.Step> items;

        TimelineAdapter(List<TreatmentPlan.Step> items) {
            this.items = items;
        }

        void updateItems(List<TreatmentPlan.Step> list) {
            this.items = list;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_timeline_step, parent, false);
            return new Holder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            TreatmentPlan.Step step = items.get(position);
            
            holder.tvName.setText((position + 1) + ". " + (step.getServiceName() != null ? step.getServiceName() : "Dịch vụ"));
            
            String status = step.getStatus();
            String stDisplay = "Chờ thực hiện";
            int colorText = android.graphics.Color.parseColor("#64748B"); // slate_500
            int colorDot = android.graphics.Color.parseColor("#94A3B8"); // slate_400
            int colorBg = android.graphics.Color.parseColor("#F1F5F9"); // slate_100
            
            if ("COMPLETED".equalsIgnoreCase(status)) {
                stDisplay = "Hoàn thành";
                colorText = android.graphics.Color.parseColor("#16A34A"); // green_600
                colorDot = android.graphics.Color.parseColor("#22C55E"); // green_500
                colorBg = android.graphics.Color.parseColor("#F0FDF4"); // green_50
            } else if ("IN_PROGRESS".equalsIgnoreCase(status)) {
                stDisplay = "Đang thực hiện";
                colorText = android.graphics.Color.parseColor("#D97706"); // amber_600
                colorDot = android.graphics.Color.parseColor("#F59E0B"); // amber_500
                colorBg = android.graphics.Color.parseColor("#FFFBEB"); // amber_50
            }

            holder.tvStatus.setText(stDisplay);
            holder.tvStatus.setTextColor(colorText);
            holder.tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(colorBg));
            holder.timelineDot.setColorFilter(colorDot);
            // Hide the line for the last item
            if (position == items.size() - 1) {
                holder.timelineLine.setVisibility(View.INVISIBLE);
            } else {
                holder.timelineLine.setVisibility(View.VISIBLE);
            }

            if (step.getRoomName() != null && !step.getRoomName().isEmpty()) {
                holder.tvRoom.setVisibility(View.VISIBLE);
                holder.tvRoom.setText("Phòng khám: " + step.getRoomName());
            } else {
                holder.tvRoom.setVisibility(View.GONE);
            }

            if (step.getDoctorConclusion() != null && !step.getDoctorConclusion().trim().isEmpty()) {
                holder.tvNote.setVisibility(View.VISIBLE);
                holder.tvNote.setText("Ghi chú bác sĩ: " + step.getDoctorConclusion().trim());
            } else {
                holder.tvNote.setVisibility(View.GONE);
            }

            if (step.getImages() != null && !step.getImages().isEmpty()) {
                holder.rvImages.setVisibility(View.VISIBLE);
                holder.rvImages.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext(), LinearLayoutManager.HORIZONTAL, false));
                holder.rvImages.setAdapter(new ImageAdapter(step.getImages()));
            } else {
                holder.rvImages.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class Holder extends RecyclerView.ViewHolder {
            ImageView timelineDot;
            View timelineLine;
            TextView tvName, tvStatus, tvRoom, tvNote;
            RecyclerView rvImages;

            Holder(View v) {
                super(v);
                timelineDot = v.findViewById(R.id.timelineDot);
                timelineLine = v.findViewById(R.id.timelineLine);
                tvName = v.findViewById(R.id.tvStepName);
                tvStatus = v.findViewById(R.id.tvStepStatus);
                tvRoom = v.findViewById(R.id.tvStepRoom);
                tvNote = v.findViewById(R.id.tvDoctorConclusion);
                rvImages = v.findViewById(R.id.rvStepImages);
            }
        }
    }

    private class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImgHolder> {
        private List<TreatmentPlan.Step.ImageItem> images;

        ImageAdapter(List<TreatmentPlan.Step.ImageItem> images) {
            this.images = images;
        }

        @NonNull
        @Override
        public ImgHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ImageView iv = new ImageView(parent.getContext());
            int size = (int) (80 * parent.getContext().getResources().getDisplayMetrics().density);
            ViewGroup.MarginLayoutParams lp = new ViewGroup.MarginLayoutParams(size, size);
            lp.setMarginEnd(16);
            iv.setLayoutParams(lp);
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            return new ImgHolder(iv);
        }

        @Override
        public void onBindViewHolder(@NonNull ImgHolder holder, int position) {
            String url = resolveMediaUrl(images.get(position).getImageUrl());
            Glide.with(holder.itemView.getContext()).load(url).into(holder.iv);

            holder.iv.setOnClickListener(v -> {
                // Open dialog to view full image
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(TreatmentPlanDetailActivity.this);
                ImageView fullIv = new ImageView(TreatmentPlanDetailActivity.this);
                fullIv.setScaleType(ImageView.ScaleType.FIT_CENTER);
                Glide.with(fullIv.getContext()).load(url).into(fullIv);
                builder.setView(fullIv);
                builder.setPositiveButton("Đóng", null);
                builder.show();
            });
        }

        @Override
        public int getItemCount() { return images.size(); }
        
        class ImgHolder extends RecyclerView.ViewHolder {
            ImageView iv;
            ImgHolder(ImageView v) { super(v); iv = v; }
        }
    }
}
