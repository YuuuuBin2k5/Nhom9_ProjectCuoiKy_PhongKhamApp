package com.hcmute.mobile_android.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.models.Invoice;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class InvoiceItemAdapter extends RecyclerView.Adapter<InvoiceItemAdapter.ViewHolder> {
    
    private Context context;
    private List<Invoice.InvoiceItem> items;
    
    public InvoiceItemAdapter(Context context, List<Invoice.InvoiceItem> items) {
        this.context = context;
        this.items = items;
    }
    
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_invoice_item, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Invoice.InvoiceItem item = items.get(position);
        
        String serviceName = item.getServiceName();
        if (item.getToothNumber() != null && !item.getToothNumber().equals("null") && !item.getToothNumber().isEmpty()) {
            serviceName += " (Răng " + item.getToothNumber() + ")";
        }
        holder.tvServiceName.setText(serviceName);
        holder.tvQuantity.setText("x" + item.getQuantity());
        
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        holder.tvPrice.setText(formatter.format(item.getUnitPrice()));
        holder.tvSubtotal.setText(formatter.format(item.getTotalPrice()));
    }
    
    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvServiceName, tvQuantity, tvPrice, tvSubtotal;
        
        ViewHolder(View itemView) {
            super(itemView);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvSubtotal = itemView.findViewById(R.id.tvSubtotal);
        }
    }
}
