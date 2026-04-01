package com.hcmute.mobile_android.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.models.ServiceStats;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ServiceStatsAdapter extends RecyclerView.Adapter<ServiceStatsAdapter.ViewHolder> {
    
    private Context context;
    private List<ServiceStats> stats;
    
    public ServiceStatsAdapter(Context context, List<ServiceStats> stats) {
        this.context = context;
        this.stats = stats;
    }
    
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_service_stats, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ServiceStats stat = stats.get(position);
        
        holder.tvServiceName.setText(stat.getServiceName());
        holder.tvBookings.setText("Lượt đặt: " + stat.getTotalBookings());
        
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        holder.tvRevenue.setText("Doanh thu: " + formatter.format(stat.getTotalRevenue()));
    }
    
    @Override
    public int getItemCount() {
        return stats != null ? stats.size() : 0;
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvServiceName, tvBookings, tvRevenue;
        
        ViewHolder(View itemView) {
            super(itemView);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvBookings = itemView.findViewById(R.id.tvBookings);
            tvRevenue = itemView.findViewById(R.id.tvRevenue);
        }
    }
}
