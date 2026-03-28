package com.hcmute.mobile_android.ui.activities;

import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.ui.fragments.HomeFragment;
import com.hcmute.mobile_android.ui.fragments.PatientDashboardFragment;
import com.hcmute.mobile_android.ui.fragments.NotificationsFragment;
import com.hcmute.mobile_android.ui.fragments.QrCheckInFragment;
import com.hcmute.mobile_android.ui.fragments.TreatmentPlanFragment;
import com.hcmute.mobile_android.util.TokenManager;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

public class MainActivity extends AppCompatActivity
        implements HomeFragment.HomeCallbacks {

    private BottomNavigationView bottomNav;
    private boolean isDoctor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Determine role at startup
        TokenManager tm = new TokenManager(this);
        String role = tm.getUserRole();
        isDoctor = "DOCTOR".equalsIgnoreCase(role);

        bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Fragment f;
            if (id == R.id.nav_home) {
                // Route by role
                f = isDoctor ? new HomeFragment() : new PatientDashboardFragment();
            } else if (id == R.id.nav_qr) {
                f = new QrCheckInFragment();
            } else if (id == R.id.nav_plan) {
                f = new TreatmentPlanFragment();
            } else if (id == R.id.nav_notifications) {
                f = new NotificationsFragment();
            } else if (id == R.id.nav_profile) {
                if (isDoctor) {
                    f = new com.hcmute.mobile_android.ui.fragments.DoctorSettingsFragment();
                } else {
                    startActivity(new android.content.Intent(MainActivity.this, MedicalRecordActivity.class));
                    return false; // Don't change selected tab visually
                }
            } else {
                f = isDoctor ? new HomeFragment() : new PatientDashboardFragment();
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, f)
                    .commit();
            return true;
        });

        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }

        if (isDoctor) {
            bottomNav.getMenu().findItem(R.id.nav_plan).setVisible(false);
            bottomNav.getMenu().findItem(R.id.nav_qr).setVisible(false);
        }

        // Padding handled by layout margins
    }

    // ─── HomeFragment.HomeCallbacks ─────────────────────────────────────────────


    @Override
    public void onNavigateToNotifications() {
        bottomNav.setSelectedItemId(R.id.nav_notifications);
    }
}
