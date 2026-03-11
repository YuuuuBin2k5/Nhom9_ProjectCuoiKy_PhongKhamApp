package com.example.phongkham_app.patient.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.phongkham_app.data.model.Doctor;
import com.example.phongkham_app.data.model.Patient;
import com.example.phongkham_app.data.repository.DoctorRepository;
import com.example.phongkham_app.data.repository.PatientRepository;

import java.util.List;

public class MedicalRecordViewModel extends ViewModel {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    private final MutableLiveData<Patient> patientLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Doctor>> doctorsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public MedicalRecordViewModel() {
        patientRepository = PatientRepository.getInstance();
        doctorRepository = DoctorRepository.getInstance();
        loadData();
    }

    public void loadData() {
        isLoading.setValue(true);
        try {
            patientLiveData.setValue(patientRepository.getPatientProfile());
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
