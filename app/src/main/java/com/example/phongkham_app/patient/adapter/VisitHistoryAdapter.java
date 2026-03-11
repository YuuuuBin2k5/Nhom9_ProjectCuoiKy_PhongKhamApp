package com.example.phongkham_app.patient.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.phongkham_app.R;
import com.example.phongkham_app.data.model.VisitHistory;

import java.util.ArrayList;
import java.util.List;

public class VisitHistoryAdapter extends RecyclerView.Adapter<VisitHistoryAdapter.VisitHistoryViewHolder> {

    private List<VisitHistory> visitHistoryList = new ArrayList<>();

    public void setVisits(List<VisitHistory> visits) {
        this.visitHistoryList = visits;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VisitHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.patient_item_visit_history, parent, false);
        return new VisitHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VisitHistoryViewHolder holder, int position) {
        holder.bind(visitHistoryList.get(position));
    }

    @Override
    public int getItemCount() {
        return visitHistoryList != null ? visitHistoryList.size() : 0;
    }

    class VisitHistoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvDate, tvVisitType, tvDoctorName, tvVisitStatus;

        public VisitHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvVisitType = itemView.findViewById(R.id.tvVisitType);
            tvDoctorName = itemView.findViewById(R.id.tvDoctorName);
            tvVisitStatus = itemView.findViewById(R.id.tvVisitStatus);
        }

        public void bind(VisitHistory visit) {
            tvDate.setText(visit.getDate());
            tvVisitType.setText(visit.getVisitType());
            tvDoctorName.setText(visit.getDoctorName());
            tvVisitStatus.setText(visit.getStatus());

            if (visit.getStatus().equalsIgnoreCase("Hoàn thành")) {
                tvVisitStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.status_completed_text));
            } else {
                tvVisitStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.status_active_text));
            }
        }
    }
}
