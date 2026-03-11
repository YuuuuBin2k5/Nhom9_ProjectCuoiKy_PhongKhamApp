package com.example.phongkham_app.admin.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.phongkham_app.data.model.Doctor;
import com.example.phongkham_app.data.model.Invoice;
import com.example.phongkham_app.data.model.Service;
import com.example.phongkham_app.data.repository.DoctorRepository;
import com.example.phongkham_app.data.repository.InvoiceRepository;
import com.example.phongkham_app.data.repository.ServiceRepository;

import java.util.List;

public class AdminHomeViewModel extends ViewModel {
    private final MutableLiveData<List<Invoice>> recentInvoices = new MutableLiveData<>();
    private final MutableLiveData<String> totalRevenue = new MutableLiveData<>();
    private final MutableLiveData<List<Doctor>> doctors = new MutableLiveData<>();
    private final MutableLiveData<List<Service>> services = new MutableLiveData<>();

    private final InvoiceRepository invoiceRepository;
    private final DoctorRepository doctorRepository;
    private final ServiceRepository serviceRepository;

    public AdminHomeViewModel() {
        invoiceRepository = InvoiceRepository.getInstance();
        doctorRepository = DoctorRepository.getInstance();
        serviceRepository = ServiceRepository.getInstance();
        loadData();
    }

    private void loadData() {
        recentInvoices.setValue(invoiceRepository.getRecentInvoices());
        totalRevenue.setValue(invoiceRepository.getTotalRevenue());
        doctors.setValue(doctorRepository.getDoctors());
        services.setValue(serviceRepository.getServices());
    }

    public LiveData<List<Invoice>> getRecentInvoices() {
        return recentInvoices;
    }

    public LiveData<String> getTotalRevenue() {
        return totalRevenue;
    }

    public LiveData<List<Doctor>> getDoctors() {
        return doctors;
    }

    public LiveData<List<Service>> getServices() {
        return services;
    }
}
