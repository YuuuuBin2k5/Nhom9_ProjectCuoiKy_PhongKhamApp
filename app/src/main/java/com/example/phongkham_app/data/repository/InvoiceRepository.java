package com.example.phongkham_app.data.repository;

import com.example.phongkham_app.data.model.Invoice;

import java.util.ArrayList;
import java.util.List;

public class InvoiceRepository {
    private static InvoiceRepository instance;

    private InvoiceRepository() {}

    public static synchronized InvoiceRepository getInstance() {
        if (instance == null) {
            instance = new InvoiceRepository();
        }
        return instance;
    }

    public List<Invoice> getRecentInvoices() {
        List<Invoice> invoices = new ArrayList<>();
        invoices.add(new Invoice("HD001", "Nguyễn Văn A", "BS. Trần Hoàng Nam", "Khám tổng quát", "10/03/2026", "200,000 VNĐ"));
        invoices.add(new Invoice("HD002", "Lê Thị B", "BS. Lê Thị Mai Anh", "Siêu âm", "10/03/2026", "400,000 VNĐ"));
        invoices.add(new Invoice("HD003", "Trần Văn C", "BS. Phạm Minh Tuấn", "Xét nghiệm máu", "11/03/2026", "300,000 VNĐ"));
        invoices.add(new Invoice("HD004", "Phạm Thị D", "BS. Nguyễn Thùy Linh", "Khám chuyên khoa", "11/03/2026", "500,000 VNĐ"));
        invoices.add(new Invoice("HD005", "Võ Văn E", "BS. Võ Đức Hải", "Chụp X-Quang", "11/03/2026", "350,000 VNĐ"));
        return invoices;
    }

    public String getTotalRevenue() {
        return "1,750,000 VNĐ";
    }
}
