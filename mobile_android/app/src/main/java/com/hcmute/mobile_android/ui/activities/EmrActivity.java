package com.hcmute.mobile_android.ui.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.hcmute.mobile_android.R;

import java.util.ArrayList;
import java.util.List;

public class EmrActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_emr);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.appBarLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        RecyclerView rvTimeline = findViewById(R.id.rvTimeline);
        rvTimeline.setLayoutManager(new LinearLayoutManager(this));

        List<EmrItem> list = new ArrayList<>();
        list.add(new EmrItem(
                "15/10/2023 • 09:30 AM",
                "HOÀN THÀNH", "#E8F5E9", "#2E7D32", "#005B9F",
                "Viêm phế quản cấp tính",
                "BS. Nguyễn Văn An", "Khoa Nội tổng quát",
                "Amoxicillin 500mg", "Paracetamol 500mg", "+2 khác",
                true
        ));
        
        list.add(new EmrItem(
                "02/09/2023 • 14:15 PM",
                "LƯU TRỮ", "#E0E0E0", "#616161", "#757575",
                "Kiểm tra sức khỏe định kỳ",
                "BS. Lê Thị Mai", "Khoa Chẩn đoán hình ảnh",
                "Vitamin tổng hợp", "Omega-3 1000mg", "",
                false
        ));

        rvTimeline.setAdapter(new EmrAdapter(list, new EmrAdapter.OnItemClickListener() {
            @Override
            public void onDetailClick(EmrItem item) {
                startActivity(new Intent(EmrActivity.this, MedicalRecordDetailActivity.class));
            }

            @Override
            public void onPrescriptionClick(EmrItem item) {
                Intent intent = new Intent(EmrActivity.this, PrescriptionDetailActivity.class);
                intent.putExtra("doctorName", item.docName);
                intent.putExtra("date", item.date.split("•")[0].trim());
                startActivity(intent);
            }
        }));
    }

    private static class EmrItem {
        String date, statusText, statusBgHex, statusTextHex, dotColorHex;
        String title, docName, docSpec;
        String drug1, drug2, drugExtra;
        boolean hasXray;

        EmrItem(String d, String st, String sb, String sth, String dc, String t, String dn, String ds, String d1, String d2, String de, boolean hx) {
            date = d; statusText = st; statusBgHex = sb; statusTextHex = sth; dotColorHex = dc;
            title = t; docName = dn; docSpec = ds;
            drug1 = d1; drug2 = d2; drugExtra = de; hasXray = hx;
        }
    }

    private static class EmrAdapter extends RecyclerView.Adapter<EmrAdapter.Holder> {
        private final List<EmrItem> items;
        private final OnItemClickListener listener;

        interface OnItemClickListener {
            void onDetailClick(EmrItem item);
            void onPrescriptionClick(EmrItem item);
        }

        EmrAdapter(List<EmrItem> items, OnItemClickListener listener) {
            this.items = items;
            this.listener = listener;
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_emr_timeline, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            EmrItem item = items.get(position);
            holder.tvDate.setText(item.date);
            holder.tvStatus.setText(item.statusText);
            
            try { holder.tvStatus.setTextColor(Color.parseColor(item.statusTextHex)); } catch (Exception ignored){}
            // Ideally we'd tint the background programmatically but left simple here for brevity.
            try { holder.cvTimelineDot.setCardBackgroundColor(Color.parseColor(item.dotColorHex)); } catch (Exception ignored){}

            holder.tvDiagnosisTitle.setText(item.title);
            holder.tvDoctorName.setText(item.docName);
            holder.tvDocSpec.setText(item.docSpec);
            
            holder.tvDrug1.setText(item.drug1);
            holder.tvDrug1.setVisibility(item.drug1.isEmpty() ? View.GONE : View.VISIBLE);
            
            holder.tvDrug2.setText(item.drug2);
            holder.tvDrug2.setVisibility(item.drug2.isEmpty() ? View.GONE : View.VISIBLE);
            
            holder.tvDrugExtra.setText(item.drugExtra);
            holder.tvDrugExtra.setVisibility(item.drugExtra.isEmpty() ? View.GONE : View.VISIBLE);
            
            holder.ivXrayThumbnail.setVisibility(item.hasXray ? View.VISIBLE : View.GONE);

            holder.btnDetails.setOnClickListener(v -> listener.onDetailClick(item));
            holder.btnPrescription.setOnClickListener(v -> listener.onPrescriptionClick(item));
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class Holder extends RecyclerView.ViewHolder {
            TextView tvDate, tvStatus, tvDiagnosisTitle, tvDoctorName, tvDocSpec;
            TextView tvDrug1, tvDrug2, tvDrugExtra;
            ImageView ivXrayThumbnail;
            MaterialCardView cvTimelineDot;
            MaterialButton btnDetails, btnPrescription;

            Holder(View v) {
                super(v);
                tvDate = v.findViewById(R.id.tvDate);
                tvStatus = v.findViewById(R.id.tvStatus);
                tvDiagnosisTitle = v.findViewById(R.id.tvDiagnosisTitle);
                tvDoctorName = v.findViewById(R.id.tvDoctorName);
                tvDocSpec = v.findViewById(R.id.tvDocSpec);
                tvDrug1 = v.findViewById(R.id.tvDrug1);
                tvDrug2 = v.findViewById(R.id.tvDrug2);
                tvDrugExtra = v.findViewById(R.id.tvDrugExtra);
                ivXrayThumbnail = v.findViewById(R.id.ivXrayThumbnail);
                cvTimelineDot = v.findViewById(R.id.cvTimelineDot);
                btnDetails = v.findViewById(R.id.btnDetails);
                btnPrescription = v.findViewById(R.id.btnPrescription);
            }
        }
    }
}
