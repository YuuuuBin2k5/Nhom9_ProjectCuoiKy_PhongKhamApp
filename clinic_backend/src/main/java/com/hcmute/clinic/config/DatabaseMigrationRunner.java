package com.hcmute.clinic.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseMigrationRunner implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        log.info("Checking and adjusting database constraints...");
        try {
            // Drop check constraint on check_in_queue status if it exists and prevents PAUSED_FOR_TEST
            jdbcTemplate.execute("ALTER TABLE check_in_queue DROP CONSTRAINT IF EXISTS check_in_queue_status_check");
            log.info("Successfully dropped check_in_queue_status_check constraint.");
        } catch (Exception e) {
            log.warn("Could not drop check_in_queue_status_check constraint (might not exist or other error): {}", e.getMessage());
        }

        try {
            // Drop check constraint on treatment_plan_steps status just in case
            jdbcTemplate.execute("ALTER TABLE treatment_plan_steps DROP CONSTRAINT IF EXISTS treatment_plan_steps_status_check");
            log.info("Successfully dropped treatment_plan_steps_status_check constraint.");
        } catch (Exception e) {
            log.warn("Could not drop treatment_plan_steps_status_check constraint: {}", e.getMessage());
        }

        try {
            log.info("Adjusting otp_challenges table for Email support...");
            // 1. Drop constraints that might block new features
            jdbcTemplate.execute("ALTER TABLE otp_challenges ALTER COLUMN phone_e164 DROP NOT NULL");
            jdbcTemplate.execute("ALTER TABLE otp_challenges DROP CONSTRAINT IF EXISTS otp_challenges_purpose_check");
            
            // 2. Add email column if not exists
            jdbcTemplate.execute("ALTER TABLE otp_challenges ADD COLUMN IF NOT EXISTS email VARCHAR(255)");
            
            // 3. Create index for email-based lookups
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_otp_email_purpose ON otp_challenges (email, purpose)");
            
            log.info("Successfully adjusted otp_challenges table.");
        } catch (Exception e) {
            log.warn("Error adjusting otp_challenges table: {}", e.getMessage());
        }
    }
}
