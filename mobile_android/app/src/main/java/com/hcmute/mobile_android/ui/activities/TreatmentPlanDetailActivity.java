package com.hcmute.mobile_android.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.hcmute.mobile_android.BuildConfig;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.models.TreatmentPlan;
import com.hcmute.mobile_android.network.models.TreatmentPlanSummary;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TreatmentPlanDetailActivity extends AppCompatActivity {

    private Long planId;
    private ProgressBar progressDetail;
    private com.google.android.material.progressindicator.CircularProgressIndicator cpOverallProgress;
    private TextView tvProgressPercent, tvProgressBadge;
    private RecyclerView rvTimeline;
    private View llMedicalSummary;
    private TextView tvDiagnosisMain, tvAdviceMain;
    private TimelineAdapter adapter;
    private MaterialButton btnBottomAction;

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
        cpOverallProgress = findViewById(R.id.cpOverallProgress);
        tvProgressPercent = findViewById(R.id.tvProgressPercent);
        tvProgressBadge = findViewById(R.id.tvProgressBadge);
        rvTimeline = findViewById(R.id.rvTimeline);
        llMedicalSummary = findViewById(R.id.llMedicalSummary);
        tvDiagnosisMain = findViewById(R.id.tvDiagnosisMain);
        tvAdviceMain = findViewById(R.id.tvAdviceMain);
        btnBottomAction = findViewById(R.id.btnBottomAction);

        adapter = new TimelineAdapter(new ArrayList<>());
        rvTimeline.setLayoutManager(new LinearLayoutManager(this));
        rvTimeline.setAdapter(adapter);

        btnBottomAction.setOnClickListener(v -> {
            // Shortcut to chat with doctor/clinic
            Intent intent = new Intent(this, ChatActivity.class);
            startActivity(intent);
        });
    }

    private void loadPlanDetails() {
        progressDetail.setVisibility(View.VISIBLE);
        ApiService api = RetrofitClient.getApiService(this);
        api.getTreatmentPlan(planId).enqueue(new Callback<TreatmentPlan>() {
            @Override
            public void onResponse(@NonNull Call<TreatmentPlan> call, @NonNull Response<TreatmentPlan> response) {
                progressDetail.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    displayPlan(response.body());
                } else {
                    fallbackToOfflineJson(response.code());
                }
            }
            @Override
            public void onFailure(@NonNull Call<TreatmentPlan> call, @NonNull Throwable t) {
                progressDetail.setVisibility(View.GONE);
                Toast.makeText(TreatmentPlanDetailActivity.this, "Lỗi mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fallbackToOfflineJson(int errorCode) {
        String jsonStr = getIntent().getStringExtra("planFallbackJson");
        if (jsonStr != null && !jsonStr.isEmpty()) {
            TreatmentPlanSummary summary = new com.google.gson.Gson().fromJson(jsonStr, TreatmentPlanSummary.class);
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
                    fs.setDoctorName(sSum.getDoctorName());
                    fakeSteps.add(fs);
                }
            }
            fakePlan.setSteps(fakeSteps);
            displayPlan(fakePlan);
        }
    }

    private void displayPlan(TreatmentPlan plan) {
        // Display Main Summary
        if ((plan.getDiagnosis() != null && !plan.getDiagnosis().isEmpty())
            || (plan.getAdvice() != null && !plan.getAdvice().isEmpty())) {
            llMedicalSummary.setVisibility(View.VISIBLE);
            tvDiagnosisMain.setText(plan.getDiagnosis() != null ? plan.getDiagnosis() : "Chưa có chẩn đoán");
            tvAdviceMain.setText(plan.getAdvice() != null ? plan.getAdvice() : "Không có lời dặn");
        } else {
            llMedicalSummary.setVisibility(View.GONE);
        }

        // Progress logic
        List<TreatmentPlan.Step> steps = plan.getSteps();
        if (steps == null || steps.isEmpty()) return;

        // Calculate Progress
        int completed = 0;
        int total = steps.size();
        for (TreatmentPlan.Step s : steps) if (s.isCompleted()) completed++;
        int percentage = (int) ((completed * 100.0) / total);

        cpOverallProgress.setProgress(percentage, true);
        tvProgressPercent.setText(percentage + "%");
        tvProgressBadge.setText(percentage == 100 ? "HOÀN THÀNH" : "TỔNG TIẾN ĐỘ");

        // Group into timeline display items
        List<TimelineItem> displayItems = groupStepsIntoTimeline(steps);
        adapter.updateItems(displayItems);
    }

    private List<TimelineItem> groupStepsIntoTimeline(List<TreatmentPlan.Step> steps) {
        List<TimelineItem> items = new ArrayList<>();
        String lastDate = "";
        int sessionCount = 0;

        for (TreatmentPlan.Step step : steps) {
            String date = step.getCreatedAt() != null && step.getCreatedAt().length() >= 10 
                    ? step.getCreatedAt().substring(0, 10) : "Chưa rõ";
            
            // Artificial grouping logic: if date changes, start new session
            if (!date.equals(lastDate)) {
                sessionCount++;
                String title = sessionCount == 1 ? "ĐỢT 1 - KHỞI ĐẦU" : ("ĐỢT " + sessionCount + " - TÁI KHÁM");
                if (step.isPending() && sessionCount > 1) title = "ĐỢT " + sessionCount + " (DỰ KIẾN)";
                
                items.add(new TimelineItem(TimelineItem.TYPE_HEADER, title, date));
                lastDate = date;
            }
            items.add(new TimelineItem(TimelineItem.TYPE_STEP, step));
        }
        return items;
    }

    // --- Timeline Adapter & Models ---

    private static class TimelineItem {
        static final int TYPE_HEADER = 0;
        static final int TYPE_STEP = 1;

        int type;
        String headerTitle, headerDate;
        TreatmentPlan.Step step;

        TimelineItem(int t, String title, String date) { this.type = t; this.headerTitle = title; this.headerDate = date; }
        TimelineItem(int t, TreatmentPlan.Step s) { this.type = t; this.step = s; }
    }

    private class TimelineAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private List<TimelineItem> items;

        TimelineAdapter(List<TimelineItem> items) { this.items = items; }
        void updateItems(List<TimelineItem> list) { this.items = list; notifyDataSetChanged(); }

        @Override public int getItemViewType(int pos) { return items.get(pos).type; }

        @NonNull @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int vt) {
            if (vt == TimelineItem.TYPE_HEADER) {
                return new HeaderHolder(LayoutInflater.from(p.getContext()).inflate(R.layout.item_timeline_session_header, p, false));
            }
            return new StepHolder(LayoutInflater.from(p.getContext()).inflate(R.layout.item_timeline_step, p, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int pos) {
            TimelineItem item = items.get(pos);
            if (h instanceof HeaderHolder) {
                HeaderHolder hh = (HeaderHolder) h;
                hh.tvTitle.setText(item.headerTitle);
                hh.tvDate.setText(item.headerDate.substring(5)); // Show MM-DD
            } else {
                StepHolder sh = (StepHolder) h;
                TreatmentPlan.Step step = item.step;
                
                sh.tvName.setText(step.getServiceName());
                sh.tvNumber.setText(String.valueOf(pos)); // Simplified step number logic
                
                // Styling based on status
                boolean isDone = step.isCompleted();
                sh.tvStatus.setText(isDone ? "Hoàn thành" : (step.isInProgress() ? "Đang thực hiện" : "Chờ"));
                int color = isDone ? 0xFF10B981 : (step.isInProgress() ? 0xFF3B82F6 : 0xFFD97706);
                int bg = isDone ? 0xFFD1FAE5 : (step.isInProgress() ? 0xFFDBEAFE : 0xFFFEF3C7);
                sh.tvStatus.setTextColor(color);
                sh.tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(bg));
                sh.ivStatusIcon.setImageResource(isDone ? R.drawable.ic_check_circle : R.drawable.ic_schedule);
                sh.ivStatusIcon.setColorFilter(color);

                sh.tvDoctor.setText(step.getDoctorName() != null ? step.getDoctorName() : "Hệ thống chỉ định");
                sh.tvDuration.setText("30-45 phút"); // Placeholder or dynamic if exists
                
                if (step.getDoctorConclusion() != null && !step.getDoctorConclusion().isEmpty()) {
                    sh.llNote.setVisibility(View.VISIBLE);
                    sh.tvNote.setText(step.getDoctorConclusion());
                } else {
                    sh.llNote.setVisibility(View.GONE);
                }

                // Medicines
                if (step.getPrescriptionDetails() != null && !step.getPrescriptionDetails().isEmpty()) {
                    sh.llPrescription.setVisibility(View.VISIBLE);
                    sh.llPrescriptionItems.removeAllViews();
                    for (TreatmentPlan.Step.PrescriptionDetail detail : step.getPrescriptionDetails()) {
                        TextView tv = new TextView(TreatmentPlanDetailActivity.this);
                        String instr = "• " + detail.getMedicineName() + ": " + detail.getDosage() + " " + detail.getUnit() 
                                + " (" + detail.getFrequency() + (detail.getDuration() != null ? " - " + detail.getDuration() : "") + ")";
                        tv.setText(instr);
                        tv.setTextColor(0xFF334155);
                        tv.setTextSize(12);
                        sh.llPrescriptionItems.addView(tv);
                    }
                } else {
                    sh.llPrescription.setVisibility(View.GONE);
                }

                // Node coloring
                sh.nodeLine.setBackgroundColor(isDone ? 0xFF10B981 : 0xFFCBD5E1);
                sh.tvNumber.setBackgroundTintList(android.content.res.ColorStateList.valueOf(isDone ? 0xFF10B981 : 0xFFCBD5E1));

                // Icons based on service name
                String name = step.getServiceName().toLowerCase();
                if (name.contains("khám")) sh.ivIcon.setImageResource(R.drawable.ic_treatment_plan);
                else if (name.contains("răng")) sh.ivIcon.setImageResource(R.drawable.ic_clinic);
                else sh.ivIcon.setImageResource(R.drawable.ic_treatment_plan);
                
                sh.btnAction.setVisibility(step.isPending() ? View.VISIBLE : View.GONE);
                sh.btnAction.setOnClickListener(v -> {
                    Intent intent = new Intent(TreatmentPlanDetailActivity.this, BookAppointmentActivity.class);
                    startActivity(intent);
                });
            }
        }

        @Override public int getItemCount() { return items.size(); }

        class HeaderHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvDate;
            HeaderHolder(View v) { super(v); tvTitle = v.findViewById(R.id.tvSessionTitle); tvDate = v.findViewById(R.id.tvSessionDate); }
        }

        class StepHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvStatus, tvDoctor, tvDuration, tvNote, tvNumber;
            ImageView ivIcon, ivStatusIcon;
            View nodeLine, llNote, llPrescription;
            LinearLayout llPrescriptionItems;
            MaterialButton btnAction;
            StepHolder(View v) {
                super(v);
                tvName = v.findViewById(R.id.tvStepName);
                tvStatus = v.findViewById(R.id.tvStepStatus);
                tvDoctor = v.findViewById(R.id.tvStepDoctor);
                tvDuration = v.findViewById(R.id.tvStepDuration);
                tvNote = v.findViewById(R.id.tvDoctorConclusion);
                tvNumber = v.findViewById(R.id.tvStepNumber);
                ivIcon = v.findViewById(R.id.ivStepIcon);
                ivStatusIcon = v.findViewById(R.id.ivStepStatusIcon);
                nodeLine = v.findViewById(R.id.timelineLine);
                llNote = v.findViewById(R.id.llNoteInfo);
                llPrescription = v.findViewById(R.id.llPrescriptionInfo);
                llPrescriptionItems = v.findViewById(R.id.llPrescriptionItems);
                btnAction = v.findViewById(R.id.btnStepAction);
            }
        }
    }
}
