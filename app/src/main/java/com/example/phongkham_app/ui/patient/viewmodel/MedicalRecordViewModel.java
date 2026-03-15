package com.example.phongkham_app.ui.patient.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.phongkham_app.data.model.Doctor;
import com.example.phongkham_app.data.model.Patient;
import com.example.phongkham_app.data.repository.DoctorRepository;
import com.example.phongkham_app.data.repository.PatientRepository;

import java.util.List;

import androidx.lifecycle.AndroidViewModel;
import android.app.Application;
import androidx.annotation.NonNull;

public class MedicalRecordViewModel extends AndroidViewModel {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    private final MutableLiveData<Patient> patientLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Doctor>> doctorsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public MedicalRecordViewModel(@NonNull Application application) {
        super(application);
        patientRepository = PatientRepository.getInstance(application);
        doctorRepository = DoctorRepository.getInstance(application);
    }

    public void loadData(long userId) {
        isLoading.setValue(true);
        try {
            if (userId != -1) {
                patientLiveData.setValue(patientRepository.getPatientProfile(userId));
            } else {
                patientLiveData.setValue(patientRepository.getPatientProfile());
            }
            doctorsLiveData.setValue(doctorRepository.getDoctors());
        } catch (Exception e) {
            errorMessage.setValue("Không thể tải dữ liệu: " + e.getMessage());
        } finally {
            isLoading.setValue(false);
        }
    }

    public void toggleFavorite(String doctorId) {
        List<Doctor> currentDoctors = doctorsLiveData.getValue();
        if (currentDoctors != null) {
            for (Doctor doc : currentDoctors) {
                if (doc.getId().equals(doctorId)) {
                    doc.setFavorite(!doc.isFavorite());
                    break;
                }
            }
            doctorsLiveData.setValue(currentDoctors);
        }
    }

    public LiveData<Patient> getPatient() { return patientLiveData; }
    public LiveData<List<Doctor>> getDoctors() { return doctorsLiveData; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
}
