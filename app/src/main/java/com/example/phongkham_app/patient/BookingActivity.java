package com.example.phongkham_app.patient;

import com.example.phongkham_app.R;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.phongkham_app.patient.adapter.DatePickerAdapter;
import com.example.phongkham_app.patient.adapter.ServiceAdapter;
import com.example.phongkham_app.patient.adapter.TimeSlotAdapter;
import com.example.phongkham_app.data.model.Service;
import com.example.phongkham_app.patient.viewmodel.BookingViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;

public class BookingActivity extends AppCompatActivity implements 
        DatePickerAdapter.OnDateSelectedListener, 
        TimeSlotAdapter.OnTimeSlotSelectedListener,
        ServiceAdapter.OnServiceSelectedListener {

    private BookingViewModel viewModel;
    private DatePickerAdapter dateAdapter;
    private TimeSlotAdapter timeSlotAdapter;
    private ServiceAdapter serviceAdapter;

    // View References
    private EditText etFullName, etAge, etDescription;
    private RadioGroup rgGender;
    private Button btnConfirm;
    private View rootLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.patient_activity_booking);

        initViews();
        setupToolbar();
        setupRecyclerViews();
        setupViewModel();
        setupClickListeners();
    }

    private void initViews() {
        rootLayout = findViewById(android.R.id.content);
        etFullName = findViewById(R.id.etFullName);
        etAge = findViewById(R.id.etAge);
        etDescription = findViewById(R.id.etDescription);
        rgGender = findViewById(R.id.rgGender);
        btnConfirm = findViewById(R.id.btnConfirm);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerViews() {
        // Services (Grid 3 columns)
        RecyclerView rvServices = findViewById(R.id.rvServices);
        serviceAdapter = new ServiceAdapter(this);
        rvServices.setLayoutManager(new GridLayoutManager(this, 3));
        rvServices.setAdapter(serviceAdapter);
        rvServices.setNestedScrollingEnabled(false);

        // Date Picker (Horizontal)
        RecyclerView rvDatePicker = findViewById(R.id.rvDatePicker);
        dateAdapter = new DatePickerAdapter(this);
        rvDatePicker.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvDatePicker.setAdapter(dateAdapter);

        // Time Slots (Grid 3 columns)
        RecyclerView rvTimeSlots = findViewById(R.id.rvTimeSlots);
        timeSlotAdapter = new TimeSlotAdapter(this);
        rvTimeSlots.setLayoutManager(new GridLayoutManager(this, 3));
        rvTimeSlots.setAdapter(timeSlotAdapter);
        rvTimeSlots.setNestedScrollingEnabled(false);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(BookingViewModel.class);

        // Observers
        viewModel.getServices().observe(this, services -> {
            if (services != null) {
                serviceAdapter.setServices(services);
            }
        });

        viewModel.getDates().observe(this, dates -> {
            if (dates != null) {
                dateAdapter.setDates(dates);
            }
        });

        viewModel.getTimeSlots().observe(this, timeSlots -> {
            if (timeSlots != null) {
                timeSlotAdapter.setTimeSlots(timeSlots);
            }
        });

        viewModel.getBookingSuccess().observe(this, success -> {
            if (success) {
                Snackbar.make(rootLayout, getString(R.string.msg_booking_success), Snackbar.LENGTH_LONG)
                        .setBackgroundTint(getResources().getColor(R.color.success, getTheme()))
                        .setTextColor(getResources().getColor(R.color.white, getTheme()))
                        .show();
                
                // Finish activity after a short delay
                btnConfirm.postDelayed(this::finish, 1500);
            }
        });

        viewModel.getValidationError().observe(this, error -> {
            if (error != null) {
                Snackbar.make(rootLayout, error, Snackbar.LENGTH_LONG)
                        .setBackgroundTint(getResources().getColor(R.color.error, getTheme()))
                        .setTextColor(getResources().getColor(R.color.white, getTheme()))
                        .show();
            }
        });

        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading) {
                btnConfirm.setEnabled(false);
                btnConfirm.setAlpha(0.5f);
                btnConfirm.setText("Đang xử lý...");
            } else {
                btnConfirm.setEnabled(true);
                btnConfirm.setAlpha(1.0f);
                btnConfirm.setText(getString(R.string.btn_confirm_booking));
            }
        });
    }

    private void setupClickListeners() {
        btnConfirm.setOnClickListener(v -> {
            String fullName = etFullName.getText().toString();
            String age = etAge.getText().toString();
            
            // Get selected gender
            String gender = getString(R.string.gender_male); // Default
            int checkedId = rgGender.getCheckedRadioButtonId();
            if (checkedId == R.id.rbFemale) {
                gender = getString(R.string.gender_female);
            } else if (checkedId == R.id.rbOtherGender) {
                gender = getString(R.string.gender_other);
            }

            String description = etDescription.getText().toString();

            viewModel.confirmBooking(fullName, age, gender, description);
        });
    }

    @Override
    public void onServiceSelected(Service service, int position) {
        viewModel.selectService(position);
    }

    @Override
    public void onDateSelected(int position) {
        viewModel.selectDate(position);
    }

    @Override
    public void onTimeSlotSelected(int position) {
        viewModel.selectTimeSlot(position);
    }
}
