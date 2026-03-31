package com.hcmute.mobile_android.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.models.TreatmentPlanSummary;
import com.hcmute.mobile_android.network.models.TreatmentStepSummary;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TreatmentPlanFragment extends Fragment {

    private ProgressBar progress;
    private TextView tvError;
    private RecyclerView recyclerPlans;
    private View tvEmpty;
    private PlansAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_treatment_plan, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        progress = view.findViewById(R.id.progress);
        tvError = view.findViewById(R.id.tvError);
        recyclerPlans = view.findViewById(R.id.recyclerPlans);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        adapter = new PlansAdapter(new ArrayList<>());
        recyclerPlans.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerPlans.setAdapter(adapter);

        loadPlans();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadPlans();
    }

    private void loadPlans() {
        progress.setVisibility(View.VISIBLE);
        tvError.setVisibility(View.GONE);
        recyclerPlans.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.GONE);

        ApiService api = RetrofitClient.getApiService(requireContext());
        api.getMyTreatmentPlans().enqueue(new Callback<List<TreatmentPlanSummary>>() {
            @Override
            public void onResponse(Call<List<TreatmentPlanSummary>> call,
                                   Response<List<TreatmentPlanSummary>> response) {
                progress.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<TreatmentPlanSummary> plans = response.body();
                    if (plans.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                    } else {
                        adapter.setPlans(plans);
                        recyclerPlans.setVisibility(View.VISIBLE);
                    }
                } else {
                    tvError.setText("Vui lòng đăng nhập để xem phác đồ");
                    tvError.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<List<TreatmentPlanSummary>> call, Throwable t) {
                progress.setVisibility(View.GONE);
                tvError.setText(t.getMessage() != null ? t.getMessage() : "Lỗi kết nối");
                tvError.setVisibility(View.VISIBLE);
            }
        });
    }

    private static String formatStatus(String status) {
        if (status == null) return "";
        switch (status) {
            case "IN_PROGRESS": return "Đang điều trị";
            case "COMPLETED": return "Hoàn thành";
            case "CANCELLED": return "Đã hủy";
            case "PENDING": return "Chờ";
            case "SKIPPED": return "Bỏ qua";
            default: return status;
        }
    }

    private static String formatStepStatus(String status) {
        if (status == null) return "";
        switch (status) {
            case "PENDING": return "Chờ";
            case "IN_PROGRESS": return "Đang thực hiện";
            case "COMPLETED": return "Hoàn thành";
            case "SKIPPED": return "Bỏ qua";
            default: return status;
        }
    }

    private static String formatDate(String iso) {
        if (iso == null || iso.length() < 10) return "";
        return iso.substring(0, 10);
    }

    private static class PlansAdapter extends RecyclerView.Adapter<PlansAdapter.PlanHolder> {
        private List<TreatmentPlanSummary> plans;

        PlansAdapter(List<TreatmentPlanSummary> plans) {
            this.plans = plans;
        }

        void setPlans(List<TreatmentPlanSummary> plans) {
            this.plans = plans != null ? plans : new ArrayList<>();
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public PlanHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_treatment_plan, parent, false);
            return new PlanHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull PlanHolder h, int position) {
            TreatmentPlanSummary plan = plans.get(position);
            h.tvStatus.setText(formatStatus(plan.getStatus()));
            h.tvDate.setText("Ngày lập: " + formatDate(plan.getCreatedAt()));

            // Show progress and next step if available
            String progressText = plan.getCompletedSteps() + "/" + plan.getTotalSteps() + " bước";
            TextView tvProgress = h.itemView.findViewById(R.id.tvPlanProgress);
            if (tvProgress != null) {
                tvProgress.setText(progressText);
            }

            com.google.android.material.progressindicator.LinearProgressIndicator progressIndicator = h.itemView.findViewById(R.id.progressIndicatorHorizontal);
            if (progressIndicator != null && plan.getTotalSteps() > 0) {
                int percentage = (int) ((plan.getCompletedSteps() * 100.0) / plan.getTotalSteps());
                progressIndicator.setProgress(percentage);
            }
            
            h.itemView.setOnClickListener(v -> {
                String fallbackJson = new com.google.gson.Gson().toJson(plan);
                android.content.Intent intent = new android.content.Intent(v.getContext(), com.hcmute.mobile_android.ui.activities.TreatmentPlanDetailActivity.class);
                intent.putExtra("planId", plan.getId());
                intent.putExtra("planFallbackJson", fallbackJson);
                v.getContext().startActivity(intent);
            });

            TextView tvNextStep = h.itemView.findViewById(R.id.tvNextStep);
            if (tvNextStep != null) {
                if (plan.getNextStepName() != null && !plan.getNextStepName().isEmpty()) {
                    tvNextStep.setText("Tiếp theo: " + plan.getNextStepName());
                    tvNextStep.setVisibility(View.VISIBLE);
                } else {
                    tvNextStep.setVisibility(View.GONE);
                }
            }

            LinearLayout stepsContainer = h.itemView.findViewById(R.id.stepsContainer);
            stepsContainer.removeAllViews();
            List<TreatmentStepSummary> steps = plan.getSteps();
            if (steps != null) {
                int shown = 0;
                for (TreatmentStepSummary step : steps) {
                    // Hide SKIPPED/CANCELLED steps from patient view
                    if ("SKIPPED".equals(step.getStatus()) || "CANCELLED".equals(step.getStatus())) {
                        continue;
                    }
                    if (shown >= 3) break;

                    android.widget.LinearLayout row = new android.widget.LinearLayout(h.itemView.getContext());
                    row.setOrientation(android.widget.LinearLayout.HORIZONTAL);
                    row.setGravity(android.view.Gravity.CENTER_VERTICAL);
                    row.setPadding(12, 8, 12, 8);

                    TextView tvOrder = new TextView(h.itemView.getContext());
                    tvOrder.setText((step.getOrder() != null ? step.getOrder() : 0) + ".");
                    tvOrder.setTextColor(android.graphics.Color.parseColor("#64748B"));
                    tvOrder.setTextSize(13);
                    row.addView(tvOrder);

                    TextView tvService = new TextView(h.itemView.getContext());
                    android.widget.LinearLayout.LayoutParams serviceLp =
                            new android.widget.LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                    tvService.setLayoutParams(serviceLp);
                    tvService.setPadding(12, 0, 8, 0);
                    tvService.setText(step.getServiceName() != null ? step.getServiceName() : "Dịch vụ");
                    tvService.setTextSize(13);
                    tvService.setTextColor(android.graphics.Color.parseColor("#334155"));
                    row.addView(tvService);

                    TextView tvStatusChip = new TextView(h.itemView.getContext());
                    tvStatusChip.setText(formatStepStatus(step.getStatus()));
                    tvStatusChip.setTextSize(11);
                    tvStatusChip.setPadding(14, 6, 14, 6);
                    if ("COMPLETED".equals(step.getStatus())) {
                        tvStatusChip.setTextColor(android.graphics.Color.parseColor("#16A34A"));
                        tvStatusChip.setBackgroundColor(android.graphics.Color.parseColor("#DCFCE7"));
                    } else if ("IN_PROGRESS".equals(step.getStatus())) {
                        tvStatusChip.setTextColor(android.graphics.Color.parseColor("#2563EB"));
                        tvStatusChip.setBackgroundColor(android.graphics.Color.parseColor("#DBEAFE"));
                    } else {
                        tvStatusChip.setTextColor(android.graphics.Color.parseColor("#64748B"));
                        tvStatusChip.setBackgroundColor(android.graphics.Color.parseColor("#E2E8F0"));
                    }
                    row.addView(tvStatusChip);

                    stepsContainer.addView(row);
                    shown++;
                }

                int visibleTotal = (int) steps.stream()
                        .filter(s -> !"SKIPPED".equals(s.getStatus()) && !"CANCELLED".equals(s.getStatus()))
                        .count();
                if (visibleTotal > shown) {
                    TextView tvMore = new TextView(h.itemView.getContext());
                    tvMore.setText("+" + (visibleTotal - shown) + " bước khác");
                    tvMore.setTextSize(12);
                    tvMore.setTextColor(android.graphics.Color.parseColor("#64748B"));
                    tvMore.setPadding(12, 6, 12, 0);
                    stepsContainer.addView(tvMore);
                }
            }
        }

        @Override
        public int getItemCount() {
            return plans.size();
        }

        static class PlanHolder extends RecyclerView.ViewHolder {
            TextView tvStatus;
            TextView tvDate;

            PlanHolder(@NonNull View itemView) {
                super(itemView);
                tvStatus = itemView.findViewById(R.id.tvPlanStatus);
                tvDate = itemView.findViewById(R.id.tvPlanDate);
            }
        }
    }
}
