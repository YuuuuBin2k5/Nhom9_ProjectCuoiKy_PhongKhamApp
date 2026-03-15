package com.example.phongkham_app.ui.patient.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.phongkham_app.data.model.Appointment;
import com.example.phongkham_app.data.model.DateItem;
import com.example.phongkham_app.data.model.Service;
import com.example.phongkham_app.data.model.TimeSlot;
import com.example.phongkham_app.data.repository.PatientRepository;
import com.example.phongkham_app.data.repository.ServiceRepository;

import java.util.List;
import java.util.UUID;

import androidx.lifecycle.AndroidViewModel;
import android.app.Application;
import androidx.annotation.NonNull;

public class BookingViewModel extends AndroidViewModel {

    private final PatientRepository patientRepository;
    private final ServiceRepository serviceRepository;

    private final MutableLiveData<List<DateItem>> datesLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<TimeSlot>> timeSlotsLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Service>> servicesLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> bookingSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> validationError = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    private Integer selectedServiceId = null;

    public BookingViewModel(@NonNull Application application) {
        super(application);
        patientRepository = PatientRepository.getInstance(application);
        serviceRepository = ServiceRepository.getInstance(application);
        loadBookingData();
    }

    private void loadBookingData() {
        datesLiveData.setValue(patientRepository.getAvailableDates());
        timeSlotsLiveData.setValue(patientRepository.getTimeSlots());
        servicesLiveData.setValue(serviceRepository.getServices());
    }

    public void selectDate(int position) {
        List<DateItem> dates = datesLiveData.getValue();
        if (dates != null) {
            for (int i = 0; i < dates.size(); i++) {
                dates.get(i).setSelected(i == position);
            }
            datesLiveData.setValue(dates);
        }
    }

    public void selectService(int position) {
        List<Service> services = servicesLiveData.getValue();
        if (services != null && position >= 0 && position < services.size()) {
            selectedServiceId = services.get(position).getId();
        }
    }

    public void selectTimeSlot(int position) {
        List<TimeSlot> slots = timeSlotsLiveData.getValue();
        if (slots != null) {
            TimeSlot selectedSlot = slots.get(position);
            if (!selectedSlot.isAvailable()) return;
            
            for (int i = 0; i < slots.size(); i++) {
                slots.get(i).setSelected(i == position);
            }
            timeSlotsLiveData.setValue(slots);
        }
    }

    public void confirmBooking(String fullName, String ageString, String gender, String description) {
        if (fullName == null || fullName.trim().isEmpty()) {
            validationError.setValue("Vui lòng nhập họ và tên");
            return;
        }

        try {
            int age = Integer.parseInt(ageString);
            if (age <= 0 || age > 150) {
                validationError.setValue("Tuổi không hợp lệ (1-150)");
                return;
            }
        } catch (NumberFormatException e) {
            validationError.setValue("Vui lòng nhập tuổi hợp lệ");
            return;
        }

        if (selectedServiceId == null) {
            // Mặc định "Khám và tư vấn" (ID 1) theo yêu cầu
            selectedServiceId = 1;
        }

        DateItem selectedDate = getSelectedDate();
        if (selectedDate == null) {
            validationError.setValue("Vui lòng chọn ngày khám");
            return;
        }

        TimeSlot selectedSlot = getSelectedSlot();
        if (selectedSlot == null) {
            validationError.setValue("Vui lòng chọn giờ khám");
            return;
        }

        isLoading.setValue(true);
        new Thread(() -> {
            try {
                android.content.SharedPreferences pref = getApplication().getSharedPreferences("UserSession", android.content.Context.MODE_PRIVATE);
                long userId = pref.getLong("USER_ID", 1);
                
                String datetime = selectedDate.getDayNumber() + "/" + selectedDate.getMonth() + "/" + selectedDate.getYear() + " " + selectedSlot.getTime();
                
                // Doctor ID có thể null/tùy chọn, mặc định gán 1 cho demo hoặc gán động
                long result = patientRepository.addAppointment((int) userId, 1, selectedServiceId, datetime, description);
                
                Thread.sleep(1000); 
                bookingSuccess.postValue(result != -1);
            } catch (Exception e) {
                e.printStackTrace();
                bookingSuccess.postValue(false);
            } finally {
                isLoading.postValue(false);
            }
        }).start();
    }

    private DateItem getSelectedDate() {
        List<DateItem> dates = datesLiveData.getValue();
        if (dates != null) {
            for (DateItem item : dates) if (item.isSelected()) return item;
        }
        return null;
    }

    private TimeSlot getSelectedSlot() {
        List<TimeSlot> slots = timeSlotsLiveData.getValue();
        if (slots != null) {
            for (TimeSlot item : slots) if (item.isSelected()) return item;
        }
        return null;
    }

    public LiveData<List<Service>> getServices() { return servicesLiveData; }
    public LiveData<List<DateItem>> getDates() { return datesLiveData; }
    public LiveData<List<TimeSlot>> getTimeSlots() { return timeSlotsLiveData; }
    public LiveData<Boolean> getBookingSuccess() { return bookingSuccess; }
    public LiveData<String> getValidationError() { return validationError; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
}
