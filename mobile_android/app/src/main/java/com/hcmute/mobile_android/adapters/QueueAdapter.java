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
import com.hcmute.mobile_android.network.models.QueueItem;

import java.util.List;

/**
 * Adapter quản lý danh sách hàng đợi bệnh nhân tại phòng khám.
 * Hỗ trợ các thao tác: Gọi bệnh nhân, Khám bệnh, Chuyển chụp X-Quang, Bỏ qua và Hoàn thành.
 */
public class QueueAdapter extends RecyclerView.Adapter<QueueAdapter.QueueViewHolder> {

    private List<QueueItem> queueList;
    private OnQueueActionListener listener;

    public interface OnQueueActionListener {
        void onCallPatient(QueueItem item);
        void onExaminePatient(QueueItem item);
        void onTransferToXRay(QueueItem item);
        void onSkipPatient(QueueItem item);
        void onCompletePatient(QueueItem item);
    }

    public QueueAdapter(List<QueueItem> queueList, OnQueueActionListener listener) {
        this.queueList = queueList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public QueueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_queue, parent, false);
        return new QueueViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QueueViewHolder holder, int position) {
        QueueItem item = queueList.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return queueList.size();
    }

    static class QueueViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardQueue;
        private TextView tvQueueNumber;
        private TextView tvPatientName;
        private TextView tvPatientPhone;
        private TextView tvServiceName;
        private TextView tvStatus;
        private TextView tvAppointmentTime;
        private MaterialButton btnCall;
        private MaterialButton btnExamine;
        private MaterialButton btnXRay;
        private MaterialButton btnSkip;
        private MaterialButton btnComplete;

        public QueueViewHolder(@NonNull View itemView) {
            super(itemView);
            cardQueue = itemView.findViewById(R.id.cardQueue);
            tvQueueNumber = itemView.findViewById(R.id.tvQueueNumber);
            tvPatientName = itemView.findViewById(R.id.tvPatientName);
            tvPatientPhone = itemView.findViewById(R.id.tvPatientPhone);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvAppointmentTime = itemView.findViewById(R.id.tvAppointmentTime);
            btnCall = itemView.findViewById(R.id.btnCall);
            btnExamine = itemView.findViewById(R.id.btnExamine);
            btnXRay = itemView.findViewById(R.id.btnXRay);
            btnSkip = itemView.findViewById(R.id.btnSkip);
            btnComplete = itemView.findViewById(R.id.btnComplete);
        }

        public void bind(QueueItem item, OnQueueActionListener listener) {
            tvQueueNumber.setText(String.format("%02d", item.getQueueNumber()));
            tvPatientName.setText(item.getPatientName());
            tvPatientPhone.setText(item.getPatientPhone());
            tvServiceName.setText(item.getServiceName());
            tvAppointmentTime.setText(item.getAppointmentTime());
            tvStatus.setText(item.getStatusDisplayText());

            // Show priority badge for priority > 5
            View ivPriorityBadge = itemView.findViewById(R.id.ivPriorityBadge);
            if (item.getPriority() != null && item.getPriority() > 5) {
                ivPriorityBadge.setVisibility(View.VISIBLE);
            } else {
                ivPriorityBadge.setVisibility(View.GONE);
            }

            // Show wait time (placeholder - should come from backend)
            TextView tvWaitTime = itemView.findViewById(R.id.tvWaitTime);
            if ("WAITING".equals(item.getStatus()) || "RETURNED_PRIORITY".equals(item.getStatus())) {
                // Calculate estimated wait time based on position
                int estimatedMinutes = getAdapterPosition() * 15; // 15 min per patient
                tvWaitTime.setText(String.format("~%d phút", estimatedMinutes));
                tvWaitTime.setVisibility(View.VISIBLE);
            } else {
                tvWaitTime.setVisibility(View.GONE);
            }

            // Color coding based on status
            int backgroundColor;
            switch (item.getStatus()) {
                case "WAITING":
                    backgroundColor = itemView.getContext().getColor(R.color.status_waiting_bg);
                    break;
                case "IN_PROGRESS":
                    backgroundColor = itemView.getContext().getColor(R.color.status_in_progress_bg);
                    break;
                case "RETURNED_PRIORITY":
                    backgroundColor = itemView.getContext().getColor(R.color.status_priority_bg);
                    break;
                case "PAUSED_FOR_TEST":
                    backgroundColor = itemView.getContext().getColor(R.color.status_paused_bg);
                    break;
                default:
                    backgroundColor = itemView.getContext().getColor(android.R.color.white);
            }
            cardQueue.setCardBackgroundColor(backgroundColor);

            // Handle card click to examine
            cardQueue.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onExaminePatient(item);
                }
            });

            tvPatientName.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onExaminePatient(item);
                }
            });

            // Set card style based on status and priority
            if (item.isPriority() || item.isReturnedPriority()) {
                cardQueue.setCardBackgroundColor(itemView.getContext().getColor(R.color.priority_background));
                cardQueue.setStrokeColor(itemView.getContext().getColor(R.color.warning_amber));
                cardQueue.setStrokeWidth(4);
            } else if (item.isInProgress()) {
                cardQueue.setCardBackgroundColor(itemView.getContext().getColor(R.color.success_background));
                cardQueue.setStrokeColor(itemView.getContext().getColor(R.color.secondary_calm_teal));
                cardQueue.setStrokeWidth(2);
            } else {
                cardQueue.setCardBackgroundColor(itemView.getContext().getColor(android.R.color.white));
                cardQueue.setStrokeColor(itemView.getContext().getColor(R.color.border_gray));
                cardQueue.setStrokeWidth(1);
            }

            // Set status text color
            if (item.isPriority() || item.isReturnedPriority()) {
                tvStatus.setTextColor(itemView.getContext().getColor(R.color.warning_amber));
            } else if (item.isInProgress()) {
                tvStatus.setTextColor(itemView.getContext().getColor(R.color.secondary_calm_teal));
            } else {
                tvStatus.setTextColor(itemView.getContext().getColor(R.color.text_secondary));
            }

            // Configure buttons based on status
            configureButtons(item, listener);
        }

        private void configureButtons(QueueItem item, OnQueueActionListener listener) {
            // Reset button visibility
            btnCall.setVisibility(View.GONE);
            btnExamine.setVisibility(View.GONE);
            btnXRay.setVisibility(View.GONE);
            btnSkip.setVisibility(View.GONE);
            btnComplete.setVisibility(View.GONE);

            switch (item.getStatus()) {
                case "WAITING":
                    // Can call patient
                    btnCall.setVisibility(View.VISIBLE);
                    btnCall.setOnClickListener(v -> listener.onCallPatient(item));
                    
                    // Also allow direct examination
                    btnExamine.setVisibility(View.VISIBLE);
                    btnExamine.setOnClickListener(v -> listener.onExaminePatient(item));
                    break;

                case "RETURNED_PRIORITY":
                    // Can call patient
                    btnCall.setVisibility(View.VISIBLE);
                    btnCall.setOnClickListener(v -> listener.onCallPatient(item));
                    
                    // For returned priority, MUST show examine
                    btnExamine.setVisibility(View.VISIBLE);
                    btnExamine.setOnClickListener(v -> listener.onExaminePatient(item));
                    break;

                case "IN_PROGRESS":
                    // Can examine, transfer to X-Ray, skip, or complete
                    btnExamine.setVisibility(View.VISIBLE);
                    btnXRay.setVisibility(View.VISIBLE);
                    btnSkip.setVisibility(View.VISIBLE);
                    btnComplete.setVisibility(View.VISIBLE);
                    
                    btnExamine.setOnClickListener(v -> listener.onExaminePatient(item));
                    btnXRay.setOnClickListener(v -> listener.onTransferToXRay(item));
                    btnSkip.setOnClickListener(v -> listener.onSkipPatient(item));
                    btnComplete.setOnClickListener(v -> listener.onCompletePatient(item));
                    break;

                case "PAUSED_FOR_TEST":
                    // Patient is at X-Ray, no actions available
                    break;

                case "COMPLETED":
                case "SKIPPED":
                    // No actions available
                    break;
            }
        }
    }
}