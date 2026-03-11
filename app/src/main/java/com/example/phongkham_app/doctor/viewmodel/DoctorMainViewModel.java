package com.example.phongkham_app.doctor.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.phongkham_app.data.model.Appointment;
import com.example.phongkham_app.data.model.WaitingPatient;
import com.example.phongkham_app.data.repository.PatientRepository;

import java.util.List;

public class DoctorMainViewModel extends ViewModel {
    private final MutableLiveData<List<Appointment>> upcomingAppointments = new MutableLiveData<>();
    private final MutableLiveData<List<WaitingPatient>> waitingPatients = new MutableLiveData<>();
    
    private final PatientRepository patientRepository;

    public DoctorMainViewModel() {
        patientRepository = PatientRepository.getInstance();
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
