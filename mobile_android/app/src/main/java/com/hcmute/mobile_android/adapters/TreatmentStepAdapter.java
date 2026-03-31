package com.hcmute.mobile_android.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.models.TreatmentPlan;

import java.util.List;

public class TreatmentStepAdapter extends RecyclerView.Adapter<TreatmentStepAdapter.StepViewHolder> {

    private List<TreatmentPlan.Step> stepList;
    private OnStepActionListener listener;
    private boolean isDraftMode = false;
    private java.util.Map<Long, Boolean> editModeMap = new java.util.HashMap<>();

    public interface OnStepActionListener {
        void onStepEdit(TreatmentPlan.Step step);
        void onStepComplete(TreatmentPlan.Step step);
        void onStepRemove(TreatmentPlan.Step step);
        void onStepCancel(TreatmentPlan.Step step);
        void onToothSelected(int toothNumber);
        void onStepSave(TreatmentPlan.Step step);
        void onStepTransfer(TreatmentPlan.Step step);
    }

    public TreatmentStepAdapter(List<TreatmentPlan.Step> stepList, OnStepActionListener listener) {
        this.stepList = stepList;
        this.listener = listener;
    }
    
    public void setDraftMode(boolean draftMode) {
        this.isDraftMode = draftMode;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StepViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_treatment_step, parent, false);
        return new StepViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StepViewHolder holder, int position) {
        TreatmentPlan.Step step = stepList.get(position);
        
        // Determine if this step is the next one allowed to be started
        boolean hasAnyInProgress = false;
        int firstPendingIndex = -1;
        for (int i = 0; i < stepList.size(); i++) {
            String s = stepList.get(i).getStatus();
            if (s == null) s = "PENDING";
            String statusUpper = s.toUpperCase();
            
            if ("IN_PROGRESS".equals(statusUpper)) {
                hasAnyInProgress = true;
            }
            if (firstPendingIndex == -1 && "PENDING".equals(statusUpper)) {
                firstPendingIndex = i;
            }
        }
        
        // A PENDING step is actionable ONLY if:
        // 1. We are in Draft Mode (allow planning anything)
        // 2. OR: No step is IN_PROGRESS AND it is the FIRST PENDING step
        boolean isActionable = true;
        String status = step.getStatus() != null ? step.getStatus().toUpperCase() : "PENDING";
        if (!isDraftMode && "PENDING".equals(status)) {
            if (hasAnyInProgress || (firstPendingIndex != -1 && position != firstPendingIndex)) {
                isActionable = false;
            }
        }
        
        holder.bind(step, position + 1, listener, isDraftMode, editModeMap, this, isActionable);
    }

    @Override
    public int getItemCount() {
        return stepList.size();
    }

    static class StepViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardStep;
        private TextView tvStepNumber;
        private TextView tvServiceName;
        private TextView tvStepDescription;
        private TextView tvToothNumber;
        private TextView tvActualPrice;
        private TextView tvStatus;
        private TextView tvRoomName;
        private TextView tvDoctorName;
        private TextView tvDoctorConclusion;
        private MaterialButton btnEdit;
        private MaterialButton btnComplete;
        private MaterialButton btnTransfer;
        private MaterialButton btnCancel;
        private android.widget.ImageButton btnRemoveStep;

        public StepViewHolder(@NonNull View itemView) {
            super(itemView);
            cardStep = itemView.findViewById(R.id.cardStep);
            tvStepNumber = itemView.findViewById(R.id.tvStepNumber);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvStepDescription = itemView.findViewById(R.id.tvStepDescription);
            tvToothNumber = itemView.findViewById(R.id.tvToothNumber);
            tvActualPrice = itemView.findViewById(R.id.tvActualPrice);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvRoomName = itemView.findViewById(R.id.tvRoomName);
            tvDoctorName = itemView.findViewById(R.id.tvDoctorName);
            tvDoctorConclusion = itemView.findViewById(R.id.tvDoctorConclusion);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnComplete = itemView.findViewById(R.id.btnComplete);
            btnTransfer = itemView.findViewById(R.id.btnTransfer);
            btnCancel = itemView.findViewById(R.id.btnCancel);
            btnRemoveStep = itemView.findViewById(R.id.btnRemoveStep);
        }

        public void bind(TreatmentPlan.Step step, int stepNumber, OnStepActionListener listener, boolean isDraft, 
                         java.util.Map<Long, Boolean> editModeMap, TreatmentStepAdapter adapter, boolean isActionable) {
            tvStepNumber.setText(String.valueOf(stepNumber));
            tvServiceName.setText(step.getServiceName());
            tvStepDescription.setText(step.getDescription());
            
            // Tooth number
            if (step.getToothNumber() != null && !step.getToothNumber().isEmpty()) {
                tvToothNumber.setText("Răng " + step.getToothNumber());
                tvToothNumber.setVisibility(View.VISIBLE);
            } else {
                tvToothNumber.setVisibility(View.GONE);
            }
            
            // Price
            if (step.getActualPrice() != null && step.getActualPrice() > 0) {
                tvActualPrice.setText(String.format("%,.0f VNĐ", step.getActualPrice()));
            } else {
                tvActualPrice.setText("Chưa định giá");
            }
            
            // Status
            tvStatus.setText(getStatusDisplay(step.getStatus()));
            
            // Card styling based on status
            setCardStyle(step.getStatus(), step.isEditable(), isDraft);
            
            // Check if this step is in edit mode
            boolean isInEditMode = editModeMap.containsKey(step.getId()) && editModeMap.get(step.getId());
            
            // Button actions
            btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    if (isInEditMode) {
                        // Save button clicked
                        listener.onStepSave(step);
                        editModeMap.put(step.getId(), false);
                        adapter.notifyItemChanged(getAdapterPosition());
                    } else {
                        // Edit button clicked
                        listener.onStepEdit(step);
                        if ("COMPLETED".equals(step.getStatus())) {
                            editModeMap.put(step.getId(), true);
                            adapter.notifyItemChanged(getAdapterPosition());
                        }
                    }
                }
            });
            
            btnComplete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onStepComplete(step);
                }
            });

            btnRemoveStep.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onStepRemove(step);
                    if (step.getId() != null) {
                        editModeMap.remove(step.getId());
                    }
                }
            });

            btnTransfer.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onStepTransfer(step);
                }
            });

            btnCancel.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onStepCancel(step);
                }
            });
            
            // Room Name
            if (step.getRoomName() != null && !step.getRoomName().isEmpty()) {
                tvRoomName.setText(step.getRoomName());
                tvRoomName.setVisibility(View.VISIBLE);
            } else {
                tvRoomName.setVisibility(View.GONE);
            }
            
            // Doctor Name
            if (step.getDoctorName() != null && !step.getDoctorName().isEmpty()) {
                tvDoctorName.setText(step.getDoctorName());
                tvDoctorName.setVisibility(View.VISIBLE);
            } else {
                tvDoctorName.setVisibility(View.GONE);
            }

            // Doctor Conclusion
            if (step.getDoctorConclusion() != null && !step.getDoctorConclusion().isEmpty()) {
                tvDoctorConclusion.setText("Kết luận: " + step.getDoctorConclusion());
                tvDoctorConclusion.setVisibility(View.VISIBLE);
            } else {
                tvDoctorConclusion.setVisibility(View.GONE);
            }

            // Configure buttons based on status and edit mode
            configureButtons(step, step.getStatus(), step.isEditable(), isDraft, isInEditMode, isActionable);
        }

        private String getStatusDisplay(String status) {
            if (status == null) return "Chưa thực hiện";
            
            switch (status.toUpperCase()) {
                case "PENDING": return "Chờ thực hiện";
                case "IN_PROGRESS": return "Đang thực hiện";
                case "COMPLETED": return "Hoàn thành";
                case "CANCELLED": return "Đã hủy";
                default: return status;
            }
        }

        private void setCardStyle(String status, boolean isEditable, boolean isDraft) {
            if (status == null) status = "PENDING";
            
            cardStep.setAlpha(1.0f); // Reset alpha
            
            switch (status.toUpperCase()) {
                case "COMPLETED":
                    cardStep.setCardBackgroundColor(itemView.getContext().getColor(R.color.success_background));
                    cardStep.setStrokeColor(itemView.getContext().getColor(R.color.success_green));
                    cardStep.setStrokeWidth(2);
                    break;
                case "IN_PROGRESS":
                    cardStep.setCardBackgroundColor(itemView.getContext().getColor(R.color.priority_background));
                    cardStep.setStrokeColor(itemView.getContext().getColor(R.color.warning_amber));
                    cardStep.setStrokeWidth(2);
                    break;
                case "CANCELLED":
                    cardStep.setCardBackgroundColor(itemView.getContext().getColor(R.color.error_background));
                    cardStep.setStrokeColor(itemView.getContext().getColor(R.color.alert_coral));
                    cardStep.setStrokeWidth(2);
                    break;
                default: // PENDING
                    cardStep.setCardBackgroundColor(itemView.getContext().getColor(android.R.color.white));
                    cardStep.setStrokeColor(itemView.getContext().getColor(R.color.border_gray));
                    cardStep.setStrokeWidth(1);
                    break;
            }
        }

        private void configureButtons(TreatmentPlan.Step step, String status, boolean isEditable, boolean isDraft, boolean isInEditMode, boolean isActionable) {
            if (status == null) status = "PENDING";
            
            // Determine service type for special handling (X-Ray, Surgery, etc.)
            String template = step.getUiTemplateType() != null ? step.getUiTemplateType().toUpperCase() : "";
            String svcName = step.getServiceName() != null ? step.getServiceName().toLowerCase() : "";
            
            boolean isXrayOrSurgery = template.contains("XRAY") || template.contains("X-RAY") || 
                                     template.contains("SURGERY") || svcName.contains("phim") || 
                                     svcName.contains("xquang") || svcName.contains("x quang") || 
                                     svcName.contains("x-quang") || 
                                     svcName.contains("tiểu phẫu") || svcName.contains("phẫu thuật") || 
                                     svcName.contains("nhổ");
            
            // Default visibility
            btnTransfer.setVisibility(View.GONE);
            btnCancel.setVisibility(View.GONE);

            if (isDraft) {
                // Draft mode - everything can be edited or removed (we mock "Edit" as "Change")
                btnEdit.setVisibility(View.VISIBLE);
                btnEdit.setText("Tùy chỉnh");
                btnComplete.setVisibility(View.GONE);
                btnRemoveStep.setVisibility(View.VISIBLE);
                
                // Show transfer button even in draft
                if (isXrayOrSurgery) {
                    btnTransfer.setVisibility(View.VISIBLE);
                    btnTransfer.setEnabled(isActionable);
                    btnTransfer.setAlpha(isActionable ? 1.0f : 0.5f);
                }
                
                btnEdit.setEnabled(isActionable);
                btnEdit.setAlpha(isActionable ? 1.0f : 0.5f);
                return;
            }

            btnEdit.setEnabled(true); // Reset

            switch (status.toUpperCase()) {
                case "COMPLETED":
                    btnEdit.setVisibility(View.VISIBLE);
                    btnEdit.setText(isInEditMode ? "Lưu" : "Chỉnh sửa");
                    btnComplete.setVisibility(View.GONE);
                    btnRemoveStep.setVisibility(isInEditMode ? View.VISIBLE : View.GONE);
                    break;
                case "IN_PROGRESS":
                    btnEdit.setVisibility(View.VISIBLE);
                    btnEdit.setText("Khám bệnh");
                    btnComplete.setVisibility(View.VISIBLE);
                    btnCancel.setVisibility(View.VISIBLE);
                    btnRemoveStep.setVisibility(View.GONE);
                    break;
                case "CANCELLED":
                    btnEdit.setVisibility(View.VISIBLE);
                    btnEdit.setText("Xem");
                    btnComplete.setVisibility(View.GONE);
                    btnRemoveStep.setVisibility(View.GONE);
                    break;
                default: // PENDING
                    btnEdit.setVisibility(View.VISIBLE);
                    btnEdit.setText("Bắt đầu");
                    btnComplete.setVisibility(View.GONE);
                    btnRemoveStep.setVisibility(View.VISIBLE);
                    
                    // Show transfer button for special services
                    if (isXrayOrSurgery) {
                        btnTransfer.setVisibility(View.VISIBLE);
                        btnTransfer.setEnabled(isActionable);
                        btnTransfer.setAlpha(isActionable ? 1.0f : 0.5f);
                    }
                    
                    btnEdit.setEnabled(isActionable);
                    btnEdit.setAlpha(isActionable ? 1.0f : 0.5f);
                    break;
            }
        }
    }
}