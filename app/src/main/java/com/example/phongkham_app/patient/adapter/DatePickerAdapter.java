package com.example.phongkham_app.patient.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.phongkham_app.R;
import com.example.phongkham_app.data.model.DateItem;

import java.util.ArrayList;
import java.util.List;

public class DatePickerAdapter extends RecyclerView.Adapter<DatePickerAdapter.DateViewHolder> {

    private List<DateItem> dates = new ArrayList<>();
    private final OnDateSelectedListener listener;

    public interface OnDateSelectedListener {
        void onDateSelected(int position);
    }

    public DatePickerAdapter(OnDateSelectedListener listener) {
        this.listener = listener;
    }

    public void setDates(List<DateItem> dates) {
        this.dates = dates;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.patient_item_date_picker, parent, false);
        return new DateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DateViewHolder holder, int position) {
        holder.bind(dates.get(position), position);
    }

    @Override
    public int getItemCount() {
        return dates != null ? dates.size() : 0;
    }

    class DateViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvDayLabel, tvDateNumber;
        private final View viewCircleBg;

        public DateViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDayLabel = itemView.findViewById(R.id.tvDayLabel);
            tvDateNumber = itemView.findViewById(R.id.tvDateNumber);
            viewCircleBg = itemView.findViewById(R.id.viewCircleBg);
        }

        public void bind(final DateItem dateItem, final int position) {
            tvDayLabel.setText(dateItem.getDayOfWeek());
            tvDateNumber.setText(dateItem.getDayNumber());

            int primaryColor = ContextCompat.getColor(itemView.getContext(), R.color.primary);
            int whiteColor = ContextCompat.getColor(itemView.getContext(), R.color.white);
            int grayColor = ContextCompat.getColor(itemView.getContext(), R.color.text_secondary);
            int textColor = ContextCompat.getColor(itemView.getContext(), R.color.text_primary);

            if (dateItem.isSelected()) {
                viewCircleBg.setVisibility(View.VISIBLE);
                tvDateNumber.setTextColor(whiteColor);
                tvDayLabel.setTextColor(primaryColor);
            } else {
                viewCircleBg.setVisibility(View.GONE);
                tvDateNumber.setTextColor(textColor);
                tvDayLabel.setTextColor(grayColor);
            }

            itemView.setOnClickListener(v -> listener.onDateSelected(position));
        }
    }
}
