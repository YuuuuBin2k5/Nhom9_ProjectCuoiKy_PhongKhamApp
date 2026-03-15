package com.example.phongkham_app.ui.doctor.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.phongkham_app.data.model.Appointment;
import com.example.phongkham_app.data.model.WaitingPatient;
import com.example.phongkham_app.data.repository.PatientRepository;

import java.util.List;

import androidx.lifecycle.AndroidViewModel;
import android.app.Application;
import androidx.annotation.NonNull;

public class DoctorMainViewModel extends AndroidViewModel {
    private final MutableLiveData<List<Appointment>> upcomingAppointments = new MutableLiveData<>();
    private final MutableLiveData<List<WaitingPatient>> waitingPatients = new MutableLiveData<>();
    
    private final PatientRepository patientRepository;

    public DoctorMainViewModel(@NonNull Application application) {
        super(application);
        patientRepository = PatientRepository.getInstance(application);
        loadData();
    }

    private void loadData() {
        upcomingAppointments.setValue(patientRepository.getUpcomingAppointments());
        waitingPatients.setValue(patientRepository.getWaitingPatients());
    }

    public LiveData<List<Appointment>> getUpcomingAppointments() {
        return upcomingAppointments;
    }

    public LiveData<List<WaitingPatient>> getWaitingPatients() {
        return waitingPatients;
    }
}
