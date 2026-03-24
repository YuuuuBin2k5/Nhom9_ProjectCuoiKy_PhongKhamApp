# Doctor and Patient Login Fix

## Problem
Doctor and patient accounts could not log in with credentials:
- Doctor: `doctor@gmail.com` / `123456` ❌
- Patient: `patient@gmail.com` / `123456` ❌
- Admin: `admin@gmail.com` / `123456` ✅ (worked)

## Root Cause
The `SeedDataLoader` class (which creates doctor and patient accounts) only runs if `serviceCategoryRepository.count() == 0`. If the database already had service categories from a previous run, the entire seed process was skipped, meaning doctor and patient accounts were never created.

The `AdminSeedRunner` worked because it runs independently and only checks if admin accounts exist.

## Solution
Created a new `DoctorPatientSeedRunner.java` that:
- Runs independently with `@Order(3)` (after SeedDataLoader and AdminSeedRunner)
- Checks specifically for doctor and patient accounts by email
- Creates them if they don't exist, regardless of other data in the database
- Uses the same password encoding as admin (`passwordEncoder.encode("123456")`)

## Files Changed
- **NEW**: `clinic_backend/src/main/java/com/hcmute/clinic/config/DoctorPatientSeedRunner.java`

## How to Apply
1. Backend has been rebuilt with `./mvnw clean package -DskipTests`
2. **RESTART the backend server** to trigger the new seed runner
3. The seed runner will automatically create doctor and patient accounts if they don't exist
4. Check the logs for: "Seeded doctor@gmail.com / 123456" and "Seeded patient@gmail.com / 123456"

## Test Credentials After Restart
- Admin: `admin@gmail.com` / `123456`
- Doctor: `doctor@gmail.com` / `123456`
- Patient: `patient@gmail.com` / `123456`

All accounts should now work correctly!
