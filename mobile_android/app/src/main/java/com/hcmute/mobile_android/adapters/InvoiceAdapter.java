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

public class InvoiceAdapter extends RecyclerView.Adapter<InvoiceAdapter.ViewHolder> {
    
    private Context context;
    private List<Invoice> invoices;
    private OnInvoiceClickListener listener;
    
    public interface OnInvoiceClickListener {
        void onInvoiceClick(Invoice invoice);
    }
    
    public InvoiceAdapter(Context context, List<Invoice> invoices, OnInvoiceClickListener listener) {
        this.context = context;
        this.invoices = invoices;
        this.listener = listener;
    }
    
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_invoice, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Invoice invoice = invoices.get(position);
        
        holder.tvInvoiceId.setText("Hóa đơn #" + invoice.getId());
        holder.tvPatientName.setText(invoice.getPatientName());
        
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        holder.tvAmount.setText(formatter.format(invoice.getTotalAmount()));
        
        holder.tvStatus.setText(invoice.getPaymentStatus());
        holder.tvStatus.setBackgroundResource(
            invoice.getPaymentStatus().equals("PAID") ? 
            R.drawable.bg_status_paid : R.drawable.bg_status_unpaid
        );
        
        holder.itemView.setOnClickListener(v -> listener.onInvoiceClick(invoice));
    }
    
    @Override
    public int getItemCount() {
        return invoices.size();
    }
    
    public void updateData(List<Invoice> newInvoices) {
        this.invoices = newInvoices;
        notifyDataSetChanged();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvInvoiceId, tvPatientName, tvAmount, tvStatus;
        
        ViewHolder(View itemView) {
            super(itemView);
            tvInvoiceId = itemView.findViewById(R.id.tvInvoiceId);
            tvPatientName = itemView.findViewById(R.id.tvPatientName);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
