package com.hcmute.mobile_android.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.hcmute.mobile_android.R;

import java.util.List;

public class SymptomAdapter extends RecyclerView.Adapter<SymptomAdapter.ViewHolder> {

    public static class Symptom {
        public String name;
        public String emoji;
        public boolean isSelected;
        public Symptom(String name, String emoji) { 
            this.name = name; 
            this.emoji = emoji; 
            this.isSelected = false; 
        }
    }

    private List<Symptom> symptoms;

    public SymptomAdapter(List<Symptom> symptoms) {
        this.symptoms = symptoms;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_symptom, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Symptom symptom = symptoms.get(position);
        holder.tvEmoji.setText(symptom.emoji);
        holder.tvName.setText(symptom.name);
        
        if (symptom.isSelected) {
            holder.itemView.setBackgroundResource(R.drawable.bg_symptom_selected);
            holder.tvName.setTextColor(Color.WHITE);
        } else {
            holder.itemView.setBackgroundResource(R.drawable.bg_symptom_normal);
            holder.tvName.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_secondary));
        }

        holder.itemView.setOnClickListener(v -> {
            for (Symptom s : symptoms) s.isSelected = false;
            symptom.isSelected = true;
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() { return symptoms.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmoji, tvName;
        ViewHolder(View view) {
            super(view);
            tvEmoji = view.findViewById(R.id.tvSymptomEmoji);
            tvName = view.findViewById(R.id.tvSymptomName);
        }
    }
}
