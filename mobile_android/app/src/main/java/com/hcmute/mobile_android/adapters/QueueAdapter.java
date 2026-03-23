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

public class QueueAdapter extends RecyclerView.Adapter<QueueAdapter.QueueViewHolder> {

    private List<QueueItem> queueList;
    private OnQueueActionListener listener;

    public interface OnQueueActionListener {
        void onCallPatient(QueueItem item);
        void onTransferToXRay(QueueItem item);
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
        private MaterialButton btnXRay;
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
            btnXRay = itemView.findViewById(R.id.btnXRay);
            btnComplete = itemView.findViewById(R.id.btnComplete);
        }

        public void bind(QueueItem item, OnQueueActionListener listener) {
            // Set basic info
            tvQueueNumber.setText(String.valueOf(item.getQueueNumber()));
            tvPatientName.setText(item.getPatientName());
            tvPatientPhone.setText(item.getPatientPhone());
            tvServiceName.setText(item.getServiceName());
            tvStatus.setText(item.getStatusDisplayText());
            tvAppointmentTime.setText(item.getAppointmentTime());

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
            btnXRay.setVisibility(View.GONE);
            btnComplete.setVisibility(View.GONE);

            switch (item.getStatus()) {
                case "WAITING":
                case "RETURNED_PRIORITY":
                    // Can call patient
                    btnCall.setVisibility(View.VISIBLE);
                    btnCall.setOnClickListener(v -> listener.onCallPatient(item));
                    break;

                case "IN_PROGRESS":
                    // Can transfer to X-Ray or complete
                    btnXRay.setVisibility(View.VISIBLE);
                    btnComplete.setVisibility(View.VISIBLE);
                    
                    btnXRay.setOnClickListener(v -> listener.onTransferToXRay(item));
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