package com.example.phongkham_app.data.repository;

import com.example.phongkham_app.data.model.Appointment;
import com.example.phongkham_app.data.model.DateItem;
import com.example.phongkham_app.data.model.Patient;
import com.example.phongkham_app.data.model.TimeSlot;
import com.example.phongkham_app.data.model.WaitingPatient;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class PatientRepository {
    private static PatientRepository instance;

    private PatientRepository() {}

    public static synchronized PatientRepository getInstance() {
        if (instance == null) {
            instance = new PatientRepository();
        }
        return instance;
    }

    public Patient getPatientProfile() {
        return new Patient(
                "BN00923041",
                "Nguyễn Văn A",
                "O+",
                28,
                "Nam",
                "Đang điều trị",
                0,
                "BHYT123456",
                "31/12/2026"
        );
    }

    public List<Appointment> getUpcomingAppointments() {
        List<Appointment> appointments = new ArrayList<>();
        appointments.add(new Appointment("Le Thi C", "10:00 AM - 11:00 AM"));
        appointments.add(new Appointment("Tran Van D", "11:00 AM - 12:00 PM"));
        appointments.add(new Appointment("Hoang Thi E", "1:00 PM - 2:00 PM"));
        return appointments;
    }

    public List<WaitingPatient> getWaitingPatients() {
        List<WaitingPatient> waitingPatients = new ArrayList<>();
        waitingPatients.add(new WaitingPatient("Nguyen Van A", "11:30 AM"));
        waitingPatients.add(new WaitingPatient("Lý Thị B", "11:45 AM"));
        waitingPatients.add(new WaitingPatient("Pham Van F", "12:00 PM"));
        waitingPatients.add(new WaitingPatient("Vo Thi G", "12:15 PM"));
        waitingPatients.add(new WaitingPatient("Bui Van H", "12:30 PM"));
        return waitingPatients;
    }

    public List<DateItem> getAvailableDates() {
        List<DateItem> dateItems = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        String[] dayNames = {"CN", "Th 2", "Th 3", "Th 4", "Th 5", "Th 6", "Th 7"};
        for (int i = 0; i < 7; i++) {
            int dayOfWeekStrIndex = calendar.get(Calendar.DAY_OF_WEEK) - 1;
            String dayOfWeek = dayNames[dayOfWeekStrIndex];
            String dayNumber = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
            int month = calendar.get(Calendar.MONTH) + 1;
            int year = calendar.get(Calendar.YEAR);
            boolean isSelected = (i == 0);
            dateItems.add(new DateItem(dayOfWeek, dayNumber, month, year, isSelected));
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        return dateItems;
    }

    public List<TimeSlot> getTimeSlots() {
        List<TimeSlot> slots = new ArrayList<>();
        String[] times = {"09:00 AM", "10:00 AM", "11:00 AM", "01:00 PM", "02:00 PM", "03:00 PM", "04:00 PM", "05:00 PM"};
        for (int i = 0; i < times.length; i++) {
            boolean isAvailable = !times[i].equals("03:00 PM") && !times[i].equals("05:00 PM");
            boolean isSelected = (i == 0);
            slots.add(new TimeSlot(times[i], isAvailable, isSelected));
        }
        return slots;
    }
}
