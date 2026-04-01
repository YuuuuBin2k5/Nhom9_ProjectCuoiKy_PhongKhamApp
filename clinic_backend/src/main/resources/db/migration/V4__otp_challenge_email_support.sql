-- Migration V4: Add email support to otp_challenges
-- This file ensures that the 'email' column exists for OTP challenges, 
-- supporting both phone and email-based authentication.

-- Add email column if it doesn't exist (handle case where Hibernate might have added it)
DO $$ 
BEGIN 
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name='otp_challenges' AND column_name='email') THEN
        ALTER TABLE otp_challenges ADD COLUMN email VARCHAR(255);
    END IF;
END $$;

-- Add index for email and purpose
CREATE INDEX IF NOT EXISTS idx_otp_email_purpose ON otp_challenges(email, purpose);

-- Modify phone_e164 to be nullable (optional, but good if we support email-only OTP)
ALTER TABLE otp_challenges ALTER COLUMN phone_e164 DROP NOT NULL;
