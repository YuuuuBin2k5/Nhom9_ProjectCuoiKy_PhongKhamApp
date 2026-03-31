package com.hcmute.mobile_android.adapters;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.models.ServiceItem;

import java.util.ArrayList;
import java.util.List;

public class TemplateStepEditAdapter extends RecyclerView.Adapter<TemplateStepEditAdapter.ViewHolder> {

    private List<StepEditModel> steps = new ArrayList<>();
    private List<ServiceItem> availableServices = new ArrayList<>();
    private OnStepRemovedListener listener;

    public interface OnStepRemovedListener {
        void onStepRemoved(int position);
    }

    public static class StepEditModel {
        public Long serviceId;
        public String serviceName;
        public String medicationDetails;

        public StepEditModel() {}
        public StepEditModel(Long serviceId, String serviceName, String medicationDetails) {
            this.serviceId = serviceId;
            this.serviceName = serviceName;
            this.medicationDetails = medicationDetails;
        }
    }

    public TemplateStepEditAdapter(OnStepRemovedListener listener) {
        this.listener = listener;
    }

    public void setSteps(List<StepEditModel> steps) {
        this.steps = steps;
        notifyDataSetChanged();
    }

    public List<StepEditModel> getSteps() {
        return steps;
    }

    public void setAvailableServices(List<ServiceItem> services) {
        this.availableServices = services;
        notifyDataSetChanged();
    }

    public void addStep() {
        steps.add(new StepEditModel());
        notifyItemInserted(steps.size() - 1);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_template_step_edit, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StepEditModel step = steps.get(position);
        holder.tvStepNumber.setText(String.valueOf(position + 1));
        
        // Service AutoComplete
        String[] serviceNames = new String[availableServices.size()];
        for (int i = 0; i < availableServices.size(); i++) {
            serviceNames[i] = availableServices.get(i).getName();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(holder.itemView.getContext(), android.R.layout.simple_dropdown_item_1line, serviceNames);
        holder.autoService.setAdapter(adapter);
        
        if (step.serviceName != null) {
            holder.autoService.setText(step.serviceName, false);
            holder.tvStepTitle.setText("Bước " + (position + 1) + ": " + step.serviceName);
        } else {
            holder.autoService.setText("");
            holder.tvStepTitle.setText("Chi tiết bước " + (position + 1));
        }

        holder.autoService.setOnItemClickListener((parent, view, pos, id) -> {
            String selectedName = (String) parent.getItemAtPosition(pos);
            for (ServiceItem s : availableServices) {
                if (s.getName().equals(selectedName)) {
                    step.serviceId = s.getId();
                    step.serviceName = s.getName();
                    holder.tvStepTitle.setText("Bước " + (position + 1) + ": " + step.serviceName);
                    break;
                }
            }
        });

        // Medication View
        holder.etMedication.setText(step.medicationDetails);
        
        // Update model on text change
        if (holder.textWatcher != null) {
            holder.etMedication.removeTextChangedListener(holder.textWatcher);
        }
        holder.textWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                step.medicationDetails = s.toString();
            }
        };
        holder.etMedication.addTextChangedListener(holder.textWatcher);

        holder.ivDeleteStep.setOnClickListener(v -> {
            if (listener != null) {
                listener.onStepRemoved(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return steps.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStepNumber, tvStepTitle;
        AutoCompleteTextView autoService;
        EditText etMedication;
        ImageView ivDeleteStep;
        TextWatcher textWatcher;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStepNumber = itemView.findViewById(R.id.tvStepNumber);
            tvStepTitle = itemView.findViewById(R.id.tvStepTitle);
            autoService = itemView.findViewById(R.id.autoService);
            etMedication = itemView.findViewById(R.id.etMedication);
            ivDeleteStep = itemView.findViewById(R.id.ivDeleteStep);
        }
    }
}
