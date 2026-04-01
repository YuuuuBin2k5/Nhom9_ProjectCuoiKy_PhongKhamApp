-- Service Duration History for data-driven queue estimation
CREATE TABLE service_duration_history (
    id BIGSERIAL PRIMARY KEY,
    service_id BIGINT NOT NULL REFERENCES services(id),
    appointment_id BIGINT REFERENCES appointments(id),
    scheduled_duration_minutes INT NOT NULL,
    actual_duration_minutes INT NOT NULL,
    started_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP NOT NULL,
    doctor_id BIGINT REFERENCES doctors(id),
    had_complications BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_service_duration_service_id ON service_duration_history(service_id);
CREATE INDEX idx_service_duration_completed_at ON service_duration_history(completed_at);
CREATE INDEX idx_service_duration_doctor_id ON service_duration_history(doctor_id);

-- Add started_at column to check_in_queue for tracking actual service start time
ALTER TABLE check_in_queue ADD COLUMN IF NOT EXISTS started_at TIMESTAMP;
ALTER TABLE check_in_queue ADD COLUMN IF NOT EXISTS completed_at TIMESTAMP;

-- Add index for queue queries
CREATE INDEX IF NOT EXISTS idx_check_in_queue_room_status ON check_in_queue(clinic_room_id, status);
CREATE INDEX IF NOT EXISTS idx_check_in_queue_check_in_time ON check_in_queue(check_in_time);
