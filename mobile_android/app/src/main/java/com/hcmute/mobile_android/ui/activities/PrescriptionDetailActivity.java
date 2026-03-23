package com.hcmute.mobile_android.ui.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.hcmute.mobile_android.R;

import java.util.ArrayList;
import java.util.List;

public class PrescriptionDetailActivity extends AppCompatActivity {

    private RecyclerView rvDrugs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_prescription_detail);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.appBarLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        // Get intent extras
        String docName = getIntent().getStringExtra("doctorName");
        if (docName != null && !docName.isEmpty()) {
            ((TextView) findViewById(R.id.tvDoctorName)).setText(docName);
        }

        findViewById(R.id.btnDownloadPDF).setOnClickListener(v -> {
            Toast.makeText(this, "Đang tải xuống PDF...", Toast.LENGTH_SHORT).show();
        });

        rvDrugs = findViewById(R.id.rvDrugs);
        rvDrugs.setLayoutManager(new LinearLayoutManager(this));

        setupDrugs();
    }

    private void setupDrugs() {
        List<DrugItem> list = new ArrayList<>();
        list.add(new DrugItem("Amoxicillin 500mg", "2x/ngày (Sáng, Tối)", "Uống sau ăn", true));
        list.add(new DrugItem("Paracetamol 500mg", "3x/ngày (Khi sốt)", "Uống sau ăn", false));
        list.add(new DrugItem("Vitamin C 1000mg", "1x/ngày (Sáng)", "Hòa tan vào nước", true));

        rvDrugs.setAdapter(new DrugAdapter(list));
    }

    private static class DrugItem {
        String name, dosage, instruction;
        boolean reminder;
        DrugItem(String n, String d, String i, boolean r) {
            name = n; dosage = d; instruction = i; reminder = r;
        }
    }

    private static class DrugAdapter extends RecyclerView.Adapter<DrugAdapter.Holder> {
        private final List<DrugItem> items;
        DrugAdapter(List<DrugItem> items) { this.items = items; }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_prescription_drug, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            DrugItem item = items.get(position);
            holder.tvName.setText(item.name);
            holder.tvDosage.setText(item.dosage);
            holder.tvInstruction.setText(item.instruction);
            holder.swReminder.setChecked(item.reminder);
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class Holder extends RecyclerView.ViewHolder {
            TextView tvName, tvDosage, tvInstruction;
            SwitchCompat swReminder;
            Holder(View v) {
                super(v);
                tvName = v.findViewById(R.id.tvDrugName);
                tvDosage = v.findViewById(R.id.tvDrugDosage);
                tvInstruction = v.findViewById(R.id.tvDrugInstruction);
                swReminder = v.findViewById(R.id.swReminder);
            }
        }
    }
}
