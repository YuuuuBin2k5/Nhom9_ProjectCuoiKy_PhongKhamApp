package com.hcmute.mobile_android.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.models.DoctorStats;
import java.util.List;

public class DoctorStatsAdapter extends RecyclerView.Adapter<DoctorStatsAdapter.ViewHolder> {
    
    private Context context;
    private List<DoctorStats> stats;
    
    public DoctorStatsAdapter(Context context, List<DoctorStats> stats) {
        this.context = context;
        this.stats = stats;
    }
    
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_doctor_stats, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        DoctorStats stat = stats.get(position);
        
        holder.tvDoctorName.setText(stat.getDoctorName());
        holder.tvAppointments.setText("Tổng lượt: " + stat.getTotalAppointments());
        holder.tvCompleted.setText("Hoàn thành: " + stat.getCompletedAppointments());
        
        if (stat.getAverageRating() != null) {
            holder.tvRating.setText(String.format("Đánh giá: %.1f/5.0", stat.getAverageRating()));
        } else {
            holder.tvRating.setText("Đánh giá: Chưa có");
        }
    }
    
    @Override
    public int getItemCount() {
        return stats != null ? stats.size() : 0;
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDoctorName, tvAppointments, tvCompleted, tvRating;
        
        ViewHolder(View itemView) {
            super(itemView);
            tvDoctorName = itemView.findViewById(R.id.tvDoctorName);
            tvAppointments = itemView.findViewById(R.id.tvAppointments);
            tvCompleted = itemView.findViewById(R.id.tvCompleted);
            tvRating = itemView.findViewById(R.id.tvRating);
        }
    }
}
