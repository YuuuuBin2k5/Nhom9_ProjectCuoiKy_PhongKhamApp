package com.hcmute.mobile_android.ui.activities;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.adapters.ImageSliderAdapter;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminServiceDetailActivity extends AppCompatActivity {

    private ViewPager2 vpServiceImages;
    private TabLayout tabLayoutIndicator;
    private TextView tvServicePrice, tvServiceDuration, tvServiceDesc, tvCategoryDetail;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_service_detail);

        initViews();
        displayData();
    }

    private void initViews() {
        vpServiceImages = findViewById(R.id.vpServiceImages);
        tabLayoutIndicator = findViewById(R.id.tabLayoutIndicator);
        tvServicePrice = findViewById(R.id.tvServicePriceDetail);
        tvServiceDuration = findViewById(R.id.tvServiceDurationDetail);
        tvServiceDesc = findViewById(R.id.tvServiceDescDetail);
        tvCategoryDetail = findViewById(R.id.tvCategoryDetail);
        toolbar = findViewById(R.id.toolbar);
        
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void displayData() {
        String name = getIntent().getStringExtra("name");
        double price = getIntent().getDoubleExtra("price", 0);
        int duration = getIntent().getIntExtra("duration", 0);
        String desc = getIntent().getStringExtra("description");
        String category = getIntent().getStringExtra("category");
        List<String> imageUrls = getIntent().getStringArrayListExtra("imageUrls");

        if (toolbar != null && name != null) {
            toolbar.setTitle(name);
        }
        
        if (tvCategoryDetail != null) {
            tvCategoryDetail.setText(category != null ? category : "Dịch vụ");
        }
        
        tvServiceDesc.setText(desc != null && !desc.isEmpty() ? desc : "Không có mô tả chi tiết.");
        
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        String priceFormatted = formatter.format(price).replace("₫", "đ");
        tvServicePrice.setText(priceFormatted);
        
        tvServiceDuration.setText(duration + " phút");

        if (imageUrls != null && !imageUrls.isEmpty()) {
            ImageSliderAdapter adapter = new ImageSliderAdapter(imageUrls);
            vpServiceImages.setAdapter(adapter);
            new TabLayoutMediator(tabLayoutIndicator, vpServiceImages, (tab, position) -> {}).attach();
        } else {
            // Fallback to placeholder if no images
            List<String> placeholders = new ArrayList<>();
            placeholders.add(""); // Empty string will trigger Glide placeholder
            ImageSliderAdapter adapter = new ImageSliderAdapter(placeholders);
            vpServiceImages.setAdapter(adapter);
            tabLayoutIndicator.setVisibility(android.view.View.GONE);
        }
    }
}
