package com.hcmute.mobile_android.ui.dialogs;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.models.MedicineItem;
import com.hcmute.mobile_android.network.models.PrescriptionResponse;
import com.hcmute.mobile_android.network.models.SavePrescriptionRequest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * BottomSheetDialogFragment for doctors to prescribe medicines and configure monitoring period.
 *
 * Show after a step is completed:
 * - Section 1: Medicine list (always shown, skippable)
 * - Section 2: Monitoring period (shown only when defaultMonitoringDays != null)
 *
 * Callbacks:
 * - onSkip()        → step is completed without prescription
 * - onSaved()       → prescription saved (and monitoring set if applicable)
 * - onResumeNeeded() → step was set to MONITORING; doctor must load fresh plan
 */
public class PrescriptionBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_STEP_ID = "step_id";
    private static final String ARG_DEFAULT_MONITORING_DAYS = "default_monitoring_days";
    private static final String ARG_SERVICE_NAME = "service_name";

    private Long stepId;
    private Integer defaultMonitoringDays;
    private String serviceName;
    private int currentMonitoringDays;

    private RecyclerView rvMedicines;
    private MedicineInputAdapter medicineAdapter;
    private View layoutMonitoring;
    private TextView tvMonitoringDays;
    private TextView tvResumeDate;
    private MaterialButton btnDecrement;
    private MaterialButton btnIncrement;
    private MaterialButton btnSkip;
    private MaterialButton btnSave;

    private ApiService apiService;
    private Listener listener;

    public interface Listener {
        void onSkip();
        void onPrescriptionSaved(boolean hasMonitoring, String scheduledResumeDate);
    }

    public static PrescriptionBottomSheet newInstance(Long stepId, Integer defaultMonitoringDays, String serviceName) {
        PrescriptionBottomSheet sheet = new PrescriptionBottomSheet();
        Bundle args = new Bundle();
        args.putLong(ARG_STEP_ID, stepId);
        if (defaultMonitoringDays != null) args.putInt(ARG_DEFAULT_MONITORING_DAYS, defaultMonitoringDays);
        args.putString(ARG_SERVICE_NAME, serviceName != null ? serviceName : "");
        sheet.setArguments(args);
        return sheet;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            stepId = getArguments().getLong(ARG_STEP_ID);
            defaultMonitoringDays = getArguments().containsKey(ARG_DEFAULT_MONITORING_DAYS)
                    ? getArguments().getInt(ARG_DEFAULT_MONITORING_DAYS) : null;
            serviceName = getArguments().getString(ARG_SERVICE_NAME, "");
        }
        currentMonitoringDays = defaultMonitoringDays != null ? defaultMonitoringDays : 7;
        apiService = RetrofitClient.getApiService(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_prescription, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvMedicines = view.findViewById(R.id.rvMedicines);
        layoutMonitoring = view.findViewById(R.id.layoutMonitoring);
        tvMonitoringDays = view.findViewById(R.id.tvMonitoringDays);
        tvResumeDate = view.findViewById(R.id.tvResumeDate);
        btnDecrement = view.findViewById(R.id.btnDecrementDays);
        btnIncrement = view.findViewById(R.id.btnIncrementDays);
        btnSkip = view.findViewById(R.id.btnSkipPrescription);
        btnSave = view.findViewById(R.id.btnSavePrescription);
        MaterialButton btnAddMedicine = view.findViewById(R.id.btnAddMedicine);

        // Setup medicine list
        List<MedicineItem> medicines = new ArrayList<>();
        medicines.add(new MedicineItem()); // one blank row to start
        medicineAdapter = new MedicineInputAdapter(medicines);
        rvMedicines.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvMedicines.setAdapter(medicineAdapter);

        btnAddMedicine.setOnClickListener(v -> medicineAdapter.addEmptyRow());

        // Show monitoring section if service has defaultMonitoringDays
        if (defaultMonitoringDays != null) {
            layoutMonitoring.setVisibility(View.VISIBLE);
            updateMonitoringUI();

            btnDecrement.setOnClickListener(v -> {
                if (currentMonitoringDays > 1) {
                    currentMonitoringDays--;
                    updateMonitoringUI();
                }
            });
            btnIncrement.setOnClickListener(v -> {
                currentMonitoringDays++;
                updateMonitoringUI();
            });
        } else {
            layoutMonitoring.setVisibility(View.GONE);
        }

        // Skip – complete step without prescription
        btnSkip.setOnClickListener(v -> {
            dismiss();
            if (listener != null) listener.onSkip();
        });

        // Save prescription (and optionally set monitoring)
        btnSave.setOnClickListener(v -> savePrescription());
    }

    private void updateMonitoringUI() {
        tvMonitoringDays.setText(String.valueOf(currentMonitoringDays));
        LocalDate resumeDate = LocalDate.now().plusDays(currentMonitoringDays);
        tvResumeDate.setText(resumeDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    }

    private void savePrescription() {
        btnSave.setEnabled(false);

        List<MedicineItem> medicines = medicineAdapter.getValidMedicines();

        // Monitoring days — only if section is visible
        Integer monitoringDays = (defaultMonitoringDays != null) ? currentMonitoringDays : null;
        String startDate = LocalDate.now().toString(); // "yyyy-MM-dd"

        SavePrescriptionRequest request = new SavePrescriptionRequest(
                medicines,
                monitoringDays,
                monitoringDays != null ? startDate : null
        );

        apiService.savePrescriptionForStep(stepId, request).enqueue(new Callback<PrescriptionResponse>() {
            @Override
            public void onResponse(@NonNull Call<PrescriptionResponse> call, @NonNull Response<PrescriptionResponse> response) {
                btnSave.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    PrescriptionResponse resp = response.body();
                    boolean hasMonitoring = resp.getMonitoringDays() != null && resp.getMonitoringDays() > 0;
                    String resumeDate = resp.getScheduledResumeDate();
                    dismiss();
                    if (listener != null) {
                        listener.onPrescriptionSaved(hasMonitoring, resumeDate);
                    }
                } else {
                    Toast.makeText(requireContext(), "Lỗi lưu đơn thuốc", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<PrescriptionResponse> call, @NonNull Throwable t) {
                btnSave.setEnabled(true);
                Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Simple RecyclerView Adapter for editable medicine input rows.
     * Each row holds a MedicineItem that is edited in-place.
     */
    public static class MedicineInputAdapter extends RecyclerView.Adapter<MedicineInputAdapter.VH> {

        private final List<MedicineItem> items;

        MedicineInputAdapter(List<MedicineItem> items) {
            this.items = items;
        }

        void addEmptyRow() {
            items.add(new MedicineItem());
            notifyItemInserted(items.size() - 1);
        }

        List<MedicineItem> getValidMedicines() {
            List<MedicineItem> valid = new ArrayList<>();
            for (MedicineItem item : items) {
                if (!TextUtils.isEmpty(item.getMedicineName())) {
                    valid.add(item);
                }
            }
            return valid;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_medicine_input, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            MedicineItem item = items.get(position);
            holder.bind(item, () -> {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_ID && items.size() > 1) {
                    items.remove(pos);
                    notifyItemRemoved(pos);
                }
            });
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class VH extends RecyclerView.ViewHolder {
            com.google.android.material.textfield.TextInputEditText etName, etDosage, etFrequency, etDuration, etPrice;
            android.widget.ImageButton btnDelete;

            VH(@NonNull View v) {
                super(v);
                etName = v.findViewById(R.id.etMedicineName);
                etDosage = v.findViewById(R.id.etDosage);
                etFrequency = v.findViewById(R.id.etFrequency);
                etDuration = v.findViewById(R.id.etDuration);
                etPrice = v.findViewById(R.id.etPrice);
                btnDelete = v.findViewById(R.id.btnDelete);
            }

            void bind(MedicineItem item, Runnable onDelete) {
                // Sync edits back to item using TextWatcher
                etName.addTextChangedListener(simpleWatcher(text -> item.setMedicineName(text)));
                etDosage.addTextChangedListener(simpleWatcher(text -> item.setDosage(text)));
                etFrequency.addTextChangedListener(simpleWatcher(text -> item.setFrequency(text)));
                etDuration.addTextChangedListener(simpleWatcher(text -> item.setDuration(text)));
                etPrice.addTextChangedListener(simpleWatcher(text -> {
                    try { item.setPrice(Double.parseDouble(text)); } catch (Exception ignored) {}
                }));
                btnDelete.setOnClickListener(v -> onDelete.run());
            }

            private android.text.TextWatcher simpleWatcher(java.util.function.Consumer<String> setter) {
                return new android.text.TextWatcher() {
                    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                        setter.accept(s.toString());
                    }
                    @Override public void afterTextChanged(android.text.Editable s) {}
                };
            }
        }
    }
}
