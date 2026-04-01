-- Migration: Add is_general_service column to treatment_plan_steps
-- Date: 2026-03-30
-- Description: Add field to distinguish between general services and tooth-specific services

-- Add column
ALTER TABLE treatment_plan_steps
ADD COLUMN is_general_service BOOLEAN DEFAULT false NOT NULL;

-- Add comment
COMMENT ON COLUMN treatment_plan_steps.is_general_service IS 
'true = general service (toothNumber = null), false = tooth-specific service (toothNumber != null)';

-- Create index for better query performance
CREATE INDEX idx_treatment_plan_steps_is_general_service 
ON treatment_plan_steps(is_general_service);

-- Update existing data: if toothNumber is null, mark as general service
UPDATE treatment_plan_steps 
SET is_general_service = true 
WHERE tooth_number IS NULL;

-- Verify the update
SELECT 
    COUNT(*) as total_steps,
    SUM(CASE WHEN is_general_service = true THEN 1 ELSE 0 END) as general_services,
    SUM(CASE WHEN is_general_service = false THEN 1 ELSE 0 END) as tooth_specific_services
FROM treatment_plan_steps;
