package com.hcmute.mobile_android.ui.activities;

import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.ui.fragments.PatientDashboardFragment;
import com.hcmute.mobile_android.ui.fragments.NotificationsFragment;
import com.hcmute.mobile_android.ui.fragments.QrCheckInFragment;
import com.hcmute.mobile_android.ui.fragments.TreatmentPlanFragment;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

public class MainActivity extends AppCompatActivity {

    private com.google.android.material.bottomnavigation.BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Fragment f;
            if (id == R.id.nav_qr) {
                f = new QrCheckInFragment();
            } else if (id == R.id.nav_plan) {
                f = new TreatmentPlanFragment();
            } else if (id == R.id.nav_notifications) {
                f = new NotificationsFragment();
            } else {
                f = new PatientDashboardFragment();
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, f)
                    .commit();
            return true;
        });

        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void onNavigateToQr() {
        bottomNav.setSelectedItemId(R.id.nav_qr);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, new QrCheckInFragment())
                .commit();
    }
}