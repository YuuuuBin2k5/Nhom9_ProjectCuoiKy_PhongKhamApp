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

import com.google.android.material.card.MaterialCardView;
import android.widget.ImageView;
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
    private MaterialCardView cardOverall;
    private com.google.android.material.progressindicator.CircularProgressIndicator cpOverall;
    private TextView tvOverallPercent, tvOverallLabel;
    private JourneyAdapter adapter;

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
        
        cardOverall = view.findViewById(R.id.cardOverallProgress);
        cpOverall = view.findViewById(R.id.cpOverall);
        tvOverallPercent = view.findViewById(R.id.tvOverallProgressPercent);
        tvOverallLabel = view.findViewById(R.id.tvOverallProgressLabel);

        adapter = new JourneyAdapter(new ArrayList<>());
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
        cardOverall.setVisibility(View.GONE);
        recyclerPlans.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.GONE);

        ApiService api = RetrofitClient.getApiService(requireContext());
        api.getMyTreatmentPlans().enqueue(new Callback<List<TreatmentPlanSummary>>() {
            @Override
            public void onResponse(Call<List<TreatmentPlanSummary>> call, Response<List<TreatmentPlanSummary>> response) {
                progress.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<TreatmentPlanSummary> plans = response.body();
                    if (plans.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                    } else {
                        // Display the most recent/active plan as the "Journey"
                        TreatmentPlanSummary activePlan = plans.get(0);
                        bindHeroCard(activePlan);
                        adapter.setSteps(activePlan.getSteps(), activePlan.getId(), new com.google.gson.Gson().toJson(activePlan));
                        recyclerPlans.setVisibility(View.VISIBLE);
                        cardOverall.setVisibility(View.VISIBLE);
                    }
                } else {
                    tvError.setText("Vui lòng đăng nhập để xem phác đồ");
                    tvError.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onFailure(Call<List<TreatmentPlanSummary>> call, Throwable t) {
                progress.setVisibility(View.GONE);
                tvError.setText("Lỗi kết nối");
                tvError.setVisibility(View.VISIBLE);
            }
        });
    }

    private void bindHeroCard(TreatmentPlanSummary plan) {
        if (plan.getTotalSteps() > 0) {
            int percentage = (int) ((plan.getCompletedSteps() * 100.0) / plan.getTotalSteps());
            cpOverall.setProgress(percentage, true);
            tvOverallPercent.setText(percentage + "%");
            tvOverallLabel.setText(plan.getStatus().equals("COMPLETED") ? "HOÀN THÀNH" : "ĐANG THỰC HIỆN");
        }
    }

    private class JourneyAdapter extends RecyclerView.Adapter<JourneyAdapter.StepHolder> {
        private List<TreatmentStepSummary> steps = new ArrayList<>();
        private Long planId;
        private String planJson;

        JourneyAdapter(List<TreatmentStepSummary> steps) { this.steps = steps; }

        void setSteps(List<TreatmentStepSummary> list, Long id, String json) {
            this.steps = list != null ? list : new ArrayList<>();
            this.planId = id;
            this.planJson = json;
            notifyDataSetChanged();
        }

        @NonNull @Override
        public StepHolder onCreateViewHolder(@NonNull ViewGroup p, int vt) {
            return new StepHolder(LayoutInflater.from(p.getContext()).inflate(R.layout.item_treatment_plan, p, false));
        }

        @Override
        public void onBindViewHolder(@NonNull StepHolder h, int pos) {
            TreatmentStepSummary step = steps.get(pos);
            h.tvName.setText(step.getServiceName());
            h.tvNumber.setText(String.valueOf(pos + 1));
            
            String status = step.getStatus();
            boolean isDone = "COMPLETED".equals(status);
            boolean isDoing = "IN_PROGRESS".equals(status);
            
            h.tvStatus.setText(isDone ? "HOÀN THÀNH" : (isDoing ? "ĐANG THỰC HIỆN" : "CHỜ"));
            h.tvStatus.setTextColor(isDone ? 0xFF10B981 : (isDoing ? 0xFF3B82F6 : 0xFF64748B));
            h.timelineDot.setBackgroundTintList(android.content.res.ColorStateList.valueOf(isDone ? 0xFFDCFCE7 : (isDoing ? 0xFFDBEAFE : 0xFFF1F5F9)));
            h.tvNumber.setTextColor(isDone ? 0xFF10B981 : (isDoing ? 0xFF3B82F6 : 0xFF64748B));
            
            h.lineTop.setVisibility(pos == 0 ? View.INVISIBLE : View.VISIBLE);
            h.lineBottom.setVisibility(pos == steps.size() - 1 ? View.INVISIBLE : View.VISIBLE);
            h.lineTop.setBackgroundColor(isDone ? 0xFF10B981 : 0xFFE2E8F0);
            h.lineBottom.setBackgroundColor(isDone ? 0xFF10B981 : 0xFFE2E8F0);

            // Removed hardcoded fallbacks like "Hệ thống" and "P.Khám"
            StringBuilder sb = new StringBuilder();
            if (step.getDoctorName() != null && !step.getDoctorName().isEmpty()) {
                sb.append("BS. ").append(step.getDoctorName());
            } else {
                sb.append("Chưa phân công BS");
            }
            
            if (step.getRoomName() != null && !step.getRoomName().isEmpty()) {
                sb.append(" • ").append(step.getRoomName());
            }

            h.tvMeta.setText(sb.toString());
            
            // Map Icons
            String name = step.getServiceName().toLowerCase();
            if (name.contains("khám")) h.ivIcon.setImageResource(R.drawable.ic_doctor);
            else if (name.contains("răng")) h.ivIcon.setImageResource(R.drawable.ic_tooth);
            else h.ivIcon.setImageResource(R.drawable.ic_treatment_plan);
            
            h.itemView.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(v.getContext(), com.hcmute.mobile_android.ui.activities.TreatmentPlanDetailActivity.class);
                intent.putExtra("planId", planId);
                intent.putExtra("planFallbackJson", planJson);
                v.getContext().startActivity(intent);
            });
        }

        @Override public int getItemCount() { return steps.size(); }

        class StepHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvStatus, tvNumber, tvMeta;
            ImageView ivIcon;
            View lineTop, lineBottom, timelineDot;
            StepHolder(View v) {
                super(v);
                tvName = v.findViewById(R.id.tvStepName);
                tvStatus = v.findViewById(R.id.tvStepStatus);
                tvNumber = v.findViewById(R.id.tvStepNumber);
                tvMeta = v.findViewById(R.id.tvStepMeta);
                ivIcon = v.findViewById(R.id.ivStepIcon);
                lineTop = v.findViewById(R.id.timelineLineTop);
                lineBottom = v.findViewById(R.id.timelineLineBottom);
                timelineDot = v.findViewById(R.id.timelineDot);
            }
        }
    }
}
