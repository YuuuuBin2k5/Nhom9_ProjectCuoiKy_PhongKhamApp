package com.example.phongkham_app.patient.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.phongkham_app.data.model.Doctor;
import com.example.phongkham_app.data.model.Patient;
import com.example.phongkham_app.data.model.Service;
import com.example.phongkham_app.data.repository.DoctorRepository;
import com.example.phongkham_app.data.repository.PatientRepository;
import com.example.phongkham_app.data.repository.ServiceRepository;

import java.util.List;

public class PatientHomeViewModel extends ViewModel {
    private final MutableLiveData<Patient> patientProfile = new MutableLiveData<>();
    private final MutableLiveData<List<Doctor>> topDoctors = new MutableLiveData<>();
    private final MutableLiveData<List<Service>> services = new MutableLiveData<>();

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final ServiceRepository serviceRepository;

    public PatientHomeViewModel() {
        patientRepository = PatientRepository.getInstance();
        doctorRepository = DoctorRepository.getInstance();
        serviceRepository = ServiceRepository.getInstance();
        loadData();
    }

    private void loadData() {
        patientProfile.setValue(patientRepository.getPatientProfile());
        topDoctors.setValue(doctorRepository.getDoctors());
        services.setValue(serviceRepository.getServices());
    }

    public LiveData<Patient> getPatientProfile() {
        return patientProfile;
    }

    public LiveData<List<Doctor>> getTopDoctors() {
        return topDoctors;
    }

    public LiveData<List<Service>> getServices() {
        return services;
    }
}
