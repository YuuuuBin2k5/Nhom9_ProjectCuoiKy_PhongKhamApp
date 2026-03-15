package com.example.phongkham_app.ui.admin.viewmodel;

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

import androidx.lifecycle.AndroidViewModel;
import android.app.Application;
import androidx.annotation.NonNull;

public class AdminHomeViewModel extends AndroidViewModel {
    private final MutableLiveData<List<Invoice>> recentInvoices = new MutableLiveData<>();
    private final MutableLiveData<String> totalRevenue = new MutableLiveData<>();
    private final MutableLiveData<List<Doctor>> doctors = new MutableLiveData<>();
    private final MutableLiveData<List<Service>> services = new MutableLiveData<>();
    private final MutableLiveData<List<com.example.phongkham_app.data.model.WaitingPatient>> waitingPatients = new MutableLiveData<>();
    
    private final com.example.phongkham_app.data.repository.PatientRepository patientRepository;


    private final InvoiceRepository invoiceRepository;
    private final DoctorRepository doctorRepository;
    private final ServiceRepository serviceRepository;

    public AdminHomeViewModel(@NonNull Application application) {
        super(application);
        invoiceRepository = InvoiceRepository.getInstance(application);
        doctorRepository = DoctorRepository.getInstance(application);
        serviceRepository = ServiceRepository.getInstance(application);
        patientRepository = com.example.phongkham_app.data.repository.PatientRepository.getInstance(application);
        loadData();
    }

    private void loadData() {
        recentInvoices.setValue(invoiceRepository.getRecentInvoices());
        totalRevenue.setValue(invoiceRepository.getTotalRevenue());
        doctors.setValue(doctorRepository.getDoctors());
        services.setValue(serviceRepository.getServices());
        waitingPatients.setValue(patientRepository.getWaitingPatients(1));
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

    public LiveData<List<com.example.phongkham_app.data.model.WaitingPatient>> getWaitingPatients() {
        return waitingPatients;
    }
}
