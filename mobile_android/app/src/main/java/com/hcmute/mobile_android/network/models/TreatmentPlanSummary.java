package com.hcmute.mobile_android.network.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TreatmentPlanSummary {
    private Long id;
    private String title;
    private String status;
    @SerializedName("createdAt")
    private String createdAt;
    private List<TreatmentStepSummary> steps;
    private int totalSteps;
    private int completedSteps;
    private String nextStepName;

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }
    public List<TreatmentStepSummary> getSteps() { return steps; }
    public int getTotalSteps() { return totalSteps; }
    public int getCompletedSteps() { return completedSteps; }
    public String getNextStepName() { return nextStepName; }
}
