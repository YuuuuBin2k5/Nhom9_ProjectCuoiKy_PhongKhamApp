package com.example.phongkham_app.ui.patient.viewmodel;

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

import androidx.lifecycle.AndroidViewModel;
import android.app.Application;
import androidx.annotation.NonNull;

public class PatientHomeViewModel extends AndroidViewModel {
    private final MutableLiveData<Patient> patientProfile = new MutableLiveData<>();
    private final MutableLiveData<List<Doctor>> topDoctors = new MutableLiveData<>();
    private final MutableLiveData<List<Service>> services = new MutableLiveData<>();
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final ServiceRepository serviceRepository;

    public PatientHomeViewModel(@NonNull Application application) {
        super(application);
        patientRepository = PatientRepository.getInstance(application);
        doctorRepository = DoctorRepository.getInstance(application);
        serviceRepository = ServiceRepository.getInstance(application);
        loadData();
    }

    private void loadData() {
        topDoctors.setValue(doctorRepository.getDoctors());
        services.setValue(serviceRepository.getServices());
    }

    public void loadPatientProfile(long userId) {
        if (userId != -1) {
            patientProfile.setValue(patientRepository.getPatientProfile(userId));
        } else {
            patientProfile.setValue(patientRepository.getPatientProfile());
        }
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
