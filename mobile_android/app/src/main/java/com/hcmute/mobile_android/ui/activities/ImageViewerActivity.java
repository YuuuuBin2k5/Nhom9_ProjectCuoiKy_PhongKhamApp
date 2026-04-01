package com.hcmute.mobile_android.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.adapters.ImageViewerAdapter;

import java.util.ArrayList;

public class ImageViewerActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private TextView tvImageCounter;
    private ArrayList<String> imageUrls;
    private int currentPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        // Get images and position from intent
        imageUrls = getIntent().getStringArrayListExtra("images");
        currentPosition = getIntent().getIntExtra("position", 0);

        if (imageUrls == null || imageUrls.isEmpty()) {
            finish();
            return;
        }

        // Initialize views
        viewPager = findViewById(R.id.viewPager);
        tvImageCounter = findViewById(R.id.tvImageCounter);
        View btnClose = findViewById(R.id.btnClose);

        // Setup ViewPager2
        ImageViewerAdapter adapter = new ImageViewerAdapter(imageUrls);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(currentPosition, false);

        // Update counter
        updateCounter(currentPosition);

        // Listen to page changes
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateCounter(position);
            }
        });

        // Close button
        btnClose.setOnClickListener(v -> finish());
    }

    private void updateCounter(int position) {
        if (tvImageCounter != null) {
            tvImageCounter.setText((position + 1) + " / " + imageUrls.size());
        }
    }
}
