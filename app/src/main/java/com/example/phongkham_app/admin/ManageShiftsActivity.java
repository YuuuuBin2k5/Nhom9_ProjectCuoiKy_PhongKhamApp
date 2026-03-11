package com.example.phongkham_app.admin;

import com.example.phongkham_app.R;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ManageShiftsActivity extends AppCompatActivity {

    private TextView tvWeekRange;
    private TableLayout tableShifts, tableSchedule;
    private Calendar currentWeekStart;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat dayFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_manage_shifts);

        initViews();
        setupListeners();
        initializeWeek();
        loadShiftData();
    }

    private void initViews() {
        tvWeekRange = findViewById(R.id.tvWeekRange);
        tableShifts = findViewById(R.id.tableShifts);
        tableSchedule = findViewById(R.id.tableSchedule);
        dateFormat = new SimpleDateFormat("d/M", Locale.getDefault());
        dayFormat = new SimpleDateFormat("EEEE", new Locale("vi", "VN"));

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void setupListeners() {
        findViewById(R.id.btnPreviousWeek).setOnClickListener(v -> {
            currentWeekStart.add(Calendar.WEEK_OF_YEAR, -1);
            updateWeekRange();
            loadShiftData();
            loadScheduleData();
        });

        findViewById(R.id.btnNextWeek).setOnClickListener(v -> {
            currentWeekStart.add(Calendar.WEEK_OF_YEAR, 1);
            updateWeekRange();
            loadShiftData();
            loadScheduleData();
        });
    }

    private void initializeWeek() {
        currentWeekStart = Calendar.getInstance();
        // Set to Monday of current week
        currentWeekStart.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        updateWeekRange();
        loadScheduleData();
    }

    private void updateWeekRange() {
        Calendar weekEnd = (Calendar) currentWeekStart.clone();
        weekEnd.add(Calendar.DAY_OF_YEAR, 6);

        String startDate = dateFormat.format(currentWeekStart.getTime());
        String endDate = dateFormat.format(weekEnd.getTime());
        tvWeekRange.setText(startDate + " - " + endDate);
    }

    private void loadShiftData() {
        // Clear existing rows except header
        int childCount = tableShifts.getChildCount();
        if (childCount > 1) {
            tableShifts.removeViews(1, childCount - 1);
        }

        // Sample data
        String[][] shiftData = {
                {"BS. Nguyễn Văn A", "5"},
                {"BS. Trần Thị B", "4"},
                {"BS. Lê Văn C", "6"},
                {"BS. Phạm Thị D", "3"},
                {"BS. Hoàng Văn E", "5"}
        };

        for (int i = 0; i < shiftData.length; i++) {
            addTableRow(shiftData[i][0], shiftData[i][1], i % 2 == 0);
        }
    }

    private void addTableRow(String doctorName, String shiftCount, boolean isEvenRow) {
        TableRow row = new TableRow(this);
        row.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        ));

        if (isEvenRow) {
            row.setBackgroundColor(Color.parseColor("#F5F5F5"));
        } else {
            row.setBackgroundColor(Color.WHITE);
        }

        // Doctor name column
        TextView tvDoctor = new TextView(this);
        tvDoctor.setText(doctorName);
        tvDoctor.setTextSize(14);
        tvDoctor.setTextColor(Color.parseColor("#333333"));
        tvDoctor.setPadding(32, 32, 32, 32);
        row.addView(tvDoctor);

        // Shift count column
        TextView tvShifts = new TextView(this);
        tvShifts.setText(shiftCount);
        tvShifts.setTextSize(14);
        tvShifts.setTextColor(Color.parseColor("#333333"));
        tvShifts.setPadding(32, 32, 32, 32);
        row.addView(tvShifts);

        tableShifts.addView(row);
    }

    private void loadScheduleData() {
        // Clear existing rows except header
        int childCount = tableSchedule.getChildCount();
        if (childCount > 1) {
            tableSchedule.removeViews(1, childCount - 1);
        }

        // Sample doctors for shifts
        String[] doctors = {
                "BS. Nguyễn Văn A", "BS. Trần Thị B", "BS. Lê Văn C",
                "BS. Phạm Thị D", "BS. Hoàng Văn E"
        };

        // Generate schedule for 7 days
        Calendar day = (Calendar) currentWeekStart.clone();
        for (int i = 0; i < 7; i++) {
            String dayOfWeek = getDayOfWeekInVietnamese(day.get(Calendar.DAY_OF_WEEK));
            String date = dateFormat.format(day.getTime());
            String dateText = dayOfWeek + "\n" + date;

            // Assign doctors - leave some shifts empty
            String morningDoctor = (i == 2 || i == 5) ? null : doctors[i % doctors.length];
            String afternoonDoctor = (i == 1 || i == 4) ? null : doctors[(i + 2) % doctors.length];

            addScheduleRow(dateText, morningDoctor, afternoonDoctor, i % 2 == 0);
            day.add(Calendar.DAY_OF_YEAR, 1);
        }
    }

    private String getDayOfWeekInVietnamese(int dayOfWeek) {
        switch (dayOfWeek) {
            case Calendar.MONDAY: return "Thứ 2";
            case Calendar.TUESDAY: return "Thứ 3";
            case Calendar.WEDNESDAY: return "Thứ 4";
            case Calendar.THURSDAY: return "Thứ 5";
            case Calendar.FRIDAY: return "Thứ 6";
            case Calendar.SATURDAY: return "Thứ 7";
            case Calendar.SUNDAY: return "Chủ nhật";
            default: return "";
        }
    }

    private void addScheduleRow(String dateText, String morningDoctor, String afternoonDoctor, boolean isEvenRow) {
        TableRow row = new TableRow(this);
        row.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        ));

        if (isEvenRow) {
            row.setBackgroundColor(Color.parseColor("#F5F5F5"));
        } else {
            row.setBackgroundColor(Color.WHITE);
        }

        // Date column
        TextView tvDate = new TextView(this);
        tvDate.setText(dateText);
        tvDate.setTextSize(14);
        tvDate.setTextColor(Color.parseColor("#333333"));
        tvDate.setPadding(24, 24, 24, 24);
        tvDate.setGravity(Gravity.CENTER);
        row.addView(tvDate);

        // Doctors column (morning and afternoon)
        LinearLayout doctorsLayout = new LinearLayout(this);
        doctorsLayout.setOrientation(LinearLayout.VERTICAL);
        doctorsLayout.setPadding(24, 24, 24, 24);

        // Morning shift
        LinearLayout morningLayout = new LinearLayout(this);
        morningLayout.setOrientation(LinearLayout.HORIZONTAL);
        morningLayout.setGravity(Gravity.CENTER_VERTICAL);
        
        TextView tvMorningLabel = new TextView(this);
        tvMorningLabel.setText("S");
        tvMorningLabel.setTextSize(16);
        tvMorningLabel.setTextColor(Color.parseColor("#4CAF50"));
        tvMorningLabel.setTypeface(null, android.graphics.Typeface.BOLD);
        tvMorningLabel.setPadding(0, 0, 16, 0);
        morningLayout.addView(tvMorningLabel);

        if (morningDoctor != null) {
            TextView tvMorningDoctor = new TextView(this);
            tvMorningDoctor.setText(morningDoctor);
            tvMorningDoctor.setTextSize(14);
            tvMorningDoctor.setTextColor(Color.parseColor("#333333"));
            LinearLayout.LayoutParams doctorParams = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            tvMorningDoctor.setLayoutParams(doctorParams);
            morningLayout.addView(tvMorningDoctor);
            
            ImageView ivDeleteMorning = new ImageView(this);
            ivDeleteMorning.setImageResource(R.drawable.ic_delete_red);
            LinearLayout.LayoutParams deleteParams = new LinearLayout.LayoutParams(40, 40);
            deleteParams.setMargins(8, 0, 0, 0);
            ivDeleteMorning.setLayoutParams(deleteParams);
            ivDeleteMorning.setOnClickListener(v -> {
                // TODO: Remove doctor from shift
                android.widget.Toast.makeText(this, "Xóa " + morningDoctor + " khỏi ca sáng", android.widget.Toast.LENGTH_SHORT).show();
            });
            morningLayout.addView(ivDeleteMorning);
        } else {
            ImageView ivAddMorning = new ImageView(this);
            ivAddMorning.setImageResource(R.drawable.ic_user_plus);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(48, 48);
            ivAddMorning.setLayoutParams(params);
            ivAddMorning.setOnClickListener(v -> {
                // TODO: Open dialog to assign doctor
                android.widget.Toast.makeText(this, "Thêm bác sĩ ca sáng", android.widget.Toast.LENGTH_SHORT).show();
            });
            morningLayout.addView(ivAddMorning);
        }

        doctorsLayout.addView(morningLayout);

        // Afternoon shift
        LinearLayout afternoonLayout = new LinearLayout(this);
        afternoonLayout.setOrientation(LinearLayout.HORIZONTAL);
        afternoonLayout.setGravity(Gravity.CENTER_VERTICAL);
        afternoonLayout.setPadding(0, 12, 0, 0);

        TextView tvAfternoonLabel = new TextView(this);
        tvAfternoonLabel.setText("C");
        tvAfternoonLabel.setTextSize(16);
        tvAfternoonLabel.setTextColor(Color.parseColor("#FF9800"));
        tvAfternoonLabel.setTypeface(null, android.graphics.Typeface.BOLD);
        tvAfternoonLabel.setPadding(0, 0, 16, 0);
        afternoonLayout.addView(tvAfternoonLabel);

        if (afternoonDoctor != null) {
            TextView tvAfternoonDoctor = new TextView(this);
            tvAfternoonDoctor.setText(afternoonDoctor);
            tvAfternoonDoctor.setTextSize(14);
            tvAfternoonDoctor.setTextColor(Color.parseColor("#333333"));
            LinearLayout.LayoutParams doctorParams = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            tvAfternoonDoctor.setLayoutParams(doctorParams);
            afternoonLayout.addView(tvAfternoonDoctor);
            
            ImageView ivDeleteAfternoon = new ImageView(this);
            ivDeleteAfternoon.setImageResource(R.drawable.ic_delete_red);
            LinearLayout.LayoutParams deleteParams = new LinearLayout.LayoutParams(40, 40);
            deleteParams.setMargins(8, 0, 0, 0);
            ivDeleteAfternoon.setLayoutParams(deleteParams);
            ivDeleteAfternoon.setOnClickListener(v -> {
                // TODO: Remove doctor from shift
                android.widget.Toast.makeText(this, "Xóa " + afternoonDoctor + " khỏi ca chiều", android.widget.Toast.LENGTH_SHORT).show();
            });
            afternoonLayout.addView(ivDeleteAfternoon);
        } else {
            ImageView ivAddAfternoon = new ImageView(this);
            ivAddAfternoon.setImageResource(R.drawable.ic_user_plus);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(48, 48);
            ivAddAfternoon.setLayoutParams(params);
            ivAddAfternoon.setOnClickListener(v -> {
                // TODO: Open dialog to assign doctor
                android.widget.Toast.makeText(this, "Thêm bác sĩ ca chiều", android.widget.Toast.LENGTH_SHORT).show();
            });
            afternoonLayout.addView(ivAddAfternoon);
        }

        doctorsLayout.addView(afternoonLayout);

        row.addView(doctorsLayout);
        tableSchedule.addView(row);
    }
}
