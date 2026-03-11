package com.example.phongkham_app.admin;

import com.example.phongkham_app.R;
import com.example.phongkham_app.data.model.Invoice;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ManageInvoicesActivity extends AppCompatActivity {

    private RecyclerView rvInvoices;
    private InvoiceAdapter adapter;
    private List<Invoice> invoiceList;
    private List<Invoice> filteredList;
    private EditText etSearch;
    private Spinner spinnerDate, spinnerDoctor, spinnerService;
    private LinearLayout layoutFilters;
    private boolean isFilterVisible = false;

    private String selectedDate = "Tất cả";
    private String selectedDoctor = "Tất cả";
    private String selectedService = "Tất cả";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_manage_invoices);

        initViews();
        setupRecyclerView();
        setupSpinners();
        setupListeners();
        loadInvoices();
    }

    private void initViews() {
        rvInvoices = findViewById(R.id.rvInvoices);
        etSearch = findViewById(R.id.etSearch);
        spinnerDate = findViewById(R.id.spinnerDate);
        spinnerDoctor = findViewById(R.id.spinnerDoctor);
        spinnerService = findViewById(R.id.spinnerService);
        layoutFilters = findViewById(R.id.layoutFilters);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        
        findViewById(R.id.btnToggleFilter).setOnClickListener(v -> toggleFilter());
    }

    private void toggleFilter() {
        isFilterVisible = !isFilterVisible;
        layoutFilters.setVisibility(isFilterVisible ? View.VISIBLE : View.GONE);
    }

    private void setupRecyclerView() {
        invoiceList = new ArrayList<>();
        filteredList = new ArrayList<>();

        adapter = new InvoiceAdapter(filteredList, invoice -> {
            Intent intent = new Intent(ManageInvoicesActivity.this, InvoiceDetailActivity.class);
            intent.putExtra("INVOICE_CODE", invoice.getCode());
            intent.putExtra("PATIENT_NAME", invoice.getPatientName());
            intent.putExtra("INVOICE_DATE", invoice.getDate());
            intent.putExtra("DOCTOR_NAME", invoice.getDoctorName());
            intent.putExtra("SERVICE_NAME", invoice.getServiceName());
            intent.putExtra("AMOUNT", invoice.getAmount());
            startActivity(intent);
        });

        rvInvoices.setLayoutManager(new LinearLayoutManager(this));
        rvInvoices.setAdapter(adapter);
    }

    private void setupSpinners() {
        // Spinner Ngày
        String[] dates = {"Tất cả", "Hôm nay", "Tuần này", "Tháng này"};
        ArrayAdapter<String> dateAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dates);
        dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDate.setAdapter(dateAdapter);

        // Spinner Bác sĩ
        String[] doctors = {"Tất cả", "BS. Nguyễn Văn A", "BS. Trần Thị B", "BS. Lê Văn C"};
        ArrayAdapter<String> doctorAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, doctors);
        doctorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDoctor.setAdapter(doctorAdapter);

        // Spinner Dịch vụ
        String[] services = {"Tất cả", "Khám Tổng Quát", "Khám Tim Mạch", "Khám Nội Khoa"};
        ArrayAdapter<String> serviceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, services);
        serviceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerService.setAdapter(serviceAdapter);
    }

    private void setupListeners() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterInvoices();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        spinnerDate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedDate = parent.getItemAtPosition(position).toString();
                filterInvoices();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerDoctor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedDoctor = parent.getItemAtPosition(position).toString();
                filterInvoices();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerService.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedService = parent.getItemAtPosition(position).toString();
                filterInvoices();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadInvoices() {
        // Dữ liệu mẫu
        invoiceList.add(new Invoice("HD001", "Nguyễn Văn A", "BS. Nguyễn Văn A", "Khám Tổng Quát", "01/01/2024", "500,000 VNĐ"));
        invoiceList.add(new Invoice("HD002", "Trần Thị B", "BS. Trần Thị B", "Khám Tim Mạch", "02/01/2024", "750,000 VNĐ"));
        invoiceList.add(new Invoice("HD003", "Lê Văn C", "BS. Lê Văn C", "Khám Nội Khoa", "03/01/2024", "600,000 VNĐ"));
        invoiceList.add(new Invoice("HD004", "Phạm Thị D", "BS. Nguyễn Văn A", "Khám Tổng Quát", "04/01/2024", "500,000 VNĐ"));
        invoiceList.add(new Invoice("HD005", "Hoàng Văn E", "BS. Trần Thị B", "Khám Tim Mạch", "05/01/2024", "750,000 VNĐ"));
        invoiceList.add(new Invoice("HD006", "Vũ Thị F", "BS. Lê Văn C", "Khám Nội Khoa", "06/01/2024", "600,000 VNĐ"));

        filteredList.addAll(invoiceList);
        adapter.notifyDataSetChanged();
    }

    private void filterInvoices() {
        filteredList.clear();
        String query = etSearch.getText().toString().toLowerCase();

        for (Invoice invoice : invoiceList) {
            boolean matchesSearch = query.isEmpty() || 
                invoice.getCode().toLowerCase().contains(query) ||
                invoice.getPatientName().toLowerCase().contains(query);

            boolean matchesDoctor = selectedDoctor.equals("Tất cả") || 
                invoice.getDoctorName().contains(selectedDoctor);

            boolean matchesService = selectedService.equals("Tất cả") || 
                invoice.getServiceName().equals(selectedService);

            if (matchesSearch && matchesDoctor && matchesService) {
                filteredList.add(invoice);
            }
        }

        adapter.notifyDataSetChanged();
    }
}
