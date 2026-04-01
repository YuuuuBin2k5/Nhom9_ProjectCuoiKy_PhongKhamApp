import os

path = r"D:\Folder_Learning_2025_2026\PhongKham\mobile_android\app\src\main\java\com\hcmute\mobile_android\ui\activities\staff\DoctorWorkflowActivity.java"

with open(path, 'r', encoding='utf-8') as f:
    lines = f.readlines()

new_lines = []
skip = 0

for i in range(len(lines)):
    if skip > 0:
        skip -= 1
        continue
    
    line = lines[i]
    
    # Check for corrupted onStepSave (declaration and then next line is @Override)
    if "public void onStepSave(TreatmentPlan.Step step) {" in line and i + 1 < len(lines) and "@Override" in lines[i+1]:
        print(f"Fixing corrupted onStepSave at line {i+1}")
        
        # Add the proper onStepSave body
        new_lines.append("    @Override\n")
        new_lines.append("    public void onStepSave(TreatmentPlan.Step step) {\n")
        new_lines.append("        android.util.Log.d(\"DoctorWorkflow\", \"onStepSave called\");\n")
        new_lines.append("        final Long originalCurrentStepId = (currentStep != null) ? currentStep.getId() : null;\n")
        new_lines.append("        final Long editingStepId = step.getId();\n")
        new_lines.append("        saveTreatmentPlanInternal(false, () -> {\n")
        new_lines.append("            Toast.makeText(this, \"Đã lưu thay đổi\", Toast.LENGTH_SHORT).show();\n")
        new_lines.append("            loadTreatmentPlanForRoom(currentTreatmentPlanId);\n")
        new_lines.append("            if (originalCurrentStepId != null && !originalCurrentStepId.equals(editingStepId)) {\n")
        new_lines.append("                for (TreatmentPlan.Step s : treatmentSteps) {\n")
        new_lines.append("                    if (originalCurrentStepId.equals(s.getId())) {\n")
        new_lines.append("                        currentStep = s;\n")
        new_lines.append("                        break;\n")
        new_lines.append("                    }\n")
        new_lines.append("                }\n")
        new_lines.append("            } else if (editingStepId != null) {\n")
        new_lines.append("                for (TreatmentPlan.Step s : treatmentSteps) {\n")
        new_lines.append("                    if (editingStepId.equals(s.getId())) {\n")
        new_lines.append("                        currentStep = s;\n")
        new_lines.append("                        break;\n")
        new_lines.append("                    }\n")
        new_lines.append("                }\n")
        new_lines.append("            }\n")
        new_lines.append("            editingStep = null;\n")
        new_lines.append("        });\n")
        new_lines.append("    }\n\n")
        continue

    # Check for MANDATORY IMAGE CHECK and remove it
    if "MANDATORY IMAGE CHECK" in line:
        print(f"Removing mandatory image check at line {i+1}")
        new_lines.append("        // IMAGE CHECK: Optional per user request\n")
        new_lines.append("        if (combinedImages.isEmpty()) {\n")
        new_lines.append("            android.util.Log.d(\"DoctorWorkflow\", \"No images uploaded for this step, proceeding as optional\");\n")
        new_lines.append("        }\n")
        skip = 4 # skip if condition, toast, return, closing brace
        continue

    new_lines.append(line)

# Clean up any duplicated @Override if they were left behind
final_lines = []
for j, l in enumerate(new_lines):
    if j > 0 and "@Override" in l and "@Override" in new_lines[j-1]:
        continue
    final_lines.append(l)

with open(path, 'w', encoding='utf-8') as f:
    f.writelines(final_lines)

print("Java file fixed successfully.")
 Riverside
