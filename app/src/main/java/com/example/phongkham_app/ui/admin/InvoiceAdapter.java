package com.example.phongkham_app.ui.admin;

import com.example.phongkham_app.R;
import com.example.phongkham_app.data.model.Invoice;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class InvoiceAdapter extends RecyclerView.Adapter<InvoiceAdapter.InvoiceViewHolder> {

    private List<Invoice> invoices;
    private OnInvoiceClickListener listener;

    public interface OnInvoiceClickListener {
        void onClick(Invoice invoice);
    }

    public InvoiceAdapter(List<Invoice> invoices, OnInvoiceClickListener listener) {
        this.invoices = invoices;
        this.listener = listener;
    }

    public void updateData(List<Invoice> newInvoices) {
        this.invoices = newInvoices;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public InvoiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.admin_item_invoice, parent, false);
        return new InvoiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InvoiceViewHolder holder, int position) {
        Invoice invoice = invoices.get(position);
        holder.bind(invoice);
    }

    @Override
    public int getItemCount() {
        return invoices.size();
    }

    class InvoiceViewHolder extends RecyclerView.ViewHolder {
        TextView tvInvoiceCode, tvPatientName, tvDoctorName, tvServiceName, tvInvoiceDate, tvAmount;

        public InvoiceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInvoiceCode = itemView.findViewById(R.id.tvInvoiceCode);
            tvPatientName = itemView.findViewById(R.id.tvPatientName);
            tvDoctorName = itemView.findViewById(R.id.tvDoctorName);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvInvoiceDate = itemView.findViewById(R.id.tvInvoiceDate);
            tvAmount = itemView.findViewById(R.id.tvAmount);
        }

        public void bind(Invoice invoice) {
            tvInvoiceCode.setText(invoice.getCode());
            tvPatientName.setText(invoice.getPatientName());
            tvDoctorName.setText(invoice.getDoctorName());
            tvServiceName.setText(invoice.getServiceName());
            tvInvoiceDate.setText(invoice.getDate());
            tvAmount.setText(invoice.getAmount());

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onClick(invoice);
                }
            });
        }
    }
}
