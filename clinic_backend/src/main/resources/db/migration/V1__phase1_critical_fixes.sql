-- Phase 1: Critical Fixes Migration
-- Date: 2026-03-28
-- Description: Fix relationships between Appointment, TreatmentPlan, MedicalRecord, and Prescription

-- Fix 2: Add appointment_id to treatment_plans
ALTER TABLE treatment_plans 
ADD COLUMN appointment_id BIGINT;

ALTER TABLE treatment_plans
ADD CONSTRAINT fk_treatment_plan_appointment 
FOREIGN KEY (appointment_id) REFERENCES appointments(id);

-- Fix 3: Add step_id to prescriptions (for linking prescription to specific step)
ALTER TABLE prescriptions
ADD COLUMN step_id BIGINT;

ALTER TABLE prescriptions
ADD CONSTRAINT fk_prescription_step
FOREIGN KEY (step_id) REFERENCES treatment_plan_steps(id);

-- Fix 3: Add completed_at to treatment_plan_steps
ALTER TABLE treatment_plan_steps
ADD COLUMN completed_at TIMESTAMP;

-- Create index for better query performance
CREATE INDEX idx_treatment_plans_appointment_id ON treatment_plans(appointment_id);
CREATE INDEX idx_treatment_plans_patient_status ON treatment_plans(patient_id, status);
CREATE INDEX idx_prescriptions_step_id ON prescriptions(step_id);
CREATE INDEX idx_appointments_patient_datetime ON appointments(patient_id, appointment_datetime);

-- Add comments for documentation
COMMENT ON COLUMN treatment_plans.appointment_id IS 'Links treatment plan to the appointment that initiated it';
COMMENT ON COLUMN prescriptions.step_id IS 'Links prescription to specific treatment plan step';
COMMENT ON COLUMN treatment_plan_steps.completed_at IS 'Timestamp when the step was completed';
