package com.hcmute.mobile_android.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.models.AuditLog;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AuditLogAdapter extends RecyclerView.Adapter<AuditLogAdapter.ViewHolder> {

    private List<AuditLog> auditLogs = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());

    public void setLogs(List<AuditLog> logs) {
        this.auditLogs = logs;
        notifyDataSetChanged();
    }

    public void addLogs(List<AuditLog> logs) {
        int startPos = this.auditLogs.size();
        this.auditLogs.addAll(logs);
        notifyItemRangeInserted(startPos, logs.size());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_audit_log, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AuditLog log = auditLogs.get(position);
        holder.tvAction.setText(log.getAction());
        holder.tvTimestamp.setText(dateFormat.format(new Date(log.getTimestamp())));
        holder.tvUser.setText("Bởi: " + (log.getUserName() != null ? log.getUserName() : "Hệ thống") + " (ID: " + log.getUserId() + ")");
        holder.tvEntity.setText("Đối tượng: " + log.getEntityType() + " (ID: " + log.getEntityId() + ")");
        holder.tvDetails.setText("Chi tiết: " + log.getDetails());
        
        // Highlight specific actions
        if (log.getAction().contains("DELETE")) {
            holder.tvAction.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.admin_error));
        } else if (log.getAction().contains("UPDATE")) {
            holder.tvAction.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.admin_warning));
        } else {
            holder.tvAction.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.admin_primary));
        }
    }

    @Override
    public int getItemCount() {
        return auditLogs.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAction, tvTimestamp, tvUser, tvEntity, tvDetails;

        ViewHolder(View itemView) {
            super(itemView);
            tvAction = itemView.findViewById(R.id.tvAction);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvUser = itemView.findViewById(R.id.tvUser);
            tvEntity = itemView.findViewById(R.id.tvEntity);
            tvDetails = itemView.findViewById(R.id.tvDetails);
        }
    }
}
