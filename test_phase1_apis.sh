#!/bin/bash

# Phase 1 API Testing Script
# Usage: ./test_phase1_apis.sh

BASE_URL="http://localhost:8080"
DOCTOR_TOKEN="your_doctor_jwt_token_here"

echo "========================================="
echo "PHASE 1: API TESTING"
echo "========================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Test 1: Get Patient by QR - Check new fields
echo -e "${YELLOW}TEST 1: Get Patient by QR${NC}"
echo "Endpoint: GET /api/doctor/patient?qr=123"
echo ""

RESPONSE=$(curl -s -X GET "${BASE_URL}/api/doctor/patient?qr=123" \
  -H "Authorization: Bearer ${DOCTOR_TOKEN}")

echo "Response:"
echo "$RESPONSE" | jq '.'
echo ""

# Check if new fields exist
if echo "$RESPONSE" | jq -e '.treatmentPlanId' > /dev/null; then
  echo -e "${GREEN}✓ treatmentPlanId field exists${NC}"
else
  echo -e "${RED}✗ treatmentPlanId field missing${NC}"
fi

if echo "$RESPONSE" | jq -e '.hasTreatmentPlan' > /dev/null; then
  echo -e "${GREEN}✓ hasTreatmentPlan field exists${NC}"
else
  echo -e "${RED}✗ hasTreatmentPlan field missing${NC}"
fi

if echo "$RESPONSE" | jq -e '.treatmentPlanStatus' > /dev/null; then
  echo -e "${GREEN}✓ treatmentPlanStatus field exists${NC}"
else
  echo -e "${RED}✗ treatmentPlanStatus field missing${NC}"
fi

echo ""
echo "========================================="
echo ""

# Test 2: Create Treatment Plan from Appointment
echo -e "${YELLOW}TEST 2: Create Treatment Plan from Appointment${NC}"
echo "Endpoint: POST /api/treatment-plans/from-appointment"
echo ""

RESPONSE=$(curl -s -X POST "${BASE_URL}/api/treatment-plans/from-appointment" \
  -H "Authorization: Bearer ${DOCTOR_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "appointmentId": 1,
    "templateId": 1
  }')

echo "Response:"
echo "$RESPONSE" | jq '.'
echo ""

# Check if plan was created
if echo "$RESPONSE" | jq -e '.id' > /dev/null; then
  PLAN_ID=$(echo "$RESPONSE" | jq -r '.id')
  echo -e "${GREEN}✓ Treatment Plan created with ID: ${PLAN_ID}${NC}"
  
  # Check if appointmentId is linked
  if echo "$RESPONSE" | jq -e '.appointmentId' > /dev/null; then
    echo -e "${GREEN}✓ appointmentId is linked${NC}"
  else
    echo -e "${RED}✗ appointmentId not linked${NC}"
  fi
else
  echo -e "${RED}✗ Failed to create Treatment Plan${NC}"
fi

echo ""
echo "========================================="
echo ""

# Test 3: Complete Step - Check no auto-generate
echo -e "${YELLOW}TEST 3: Complete Step (Last Step)${NC}"
echo "Endpoint: PATCH /api/treatment-plans/steps/{stepId}/complete"
echo ""

# You need to replace STEP_ID with actual step ID
STEP_ID="1"

RESPONSE=$(curl -s -X PATCH "${BASE_URL}/api/treatment-plans/steps/${STEP_ID}/complete" \
  -H "Authorization: Bearer ${DOCTOR_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "doctorConclusion": "Test completion",
    "imageUrls": []
  }')

echo "Response:"
echo "$RESPONSE" | jq '.'
echo ""

if echo "$RESPONSE" | jq -e '.message' > /dev/null; then
  echo -e "${GREEN}✓ Step completed successfully${NC}"
  
  # Check if nextRoomName exists (should be null for last step)
  if echo "$RESPONSE" | jq -e '.nextRoomName' > /dev/null; then
    echo -e "${YELLOW}⚠ nextRoomName exists (there's a next step)${NC}"
  else
    echo -e "${GREEN}✓ No nextRoomName (plan completed, no auto-generated step)${NC}"
  fi
else
  echo -e "${RED}✗ Failed to complete step${NC}"
fi

echo ""
echo "========================================="
echo ""

# Test 4: Database Check (requires mysql client)
echo -e "${YELLOW}TEST 4: Database Schema Check${NC}"
echo "Checking if new columns exist..."
echo ""

# Check if mysql is available
if command -v mysql &> /dev/null; then
  echo "Checking treatment_plans.appointment_id..."
  mysql -u root -p clinic_db -e "DESCRIBE treatment_plans;" | grep "appointment_id" && \
    echo -e "${GREEN}✓ appointment_id column exists${NC}" || \
    echo -e "${RED}✗ appointment_id column missing${NC}"
  
  echo ""
  echo "Checking prescriptions.step_id..."
  mysql -u root -p clinic_db -e "DESCRIBE prescriptions;" | grep "step_id" && \
    echo -e "${GREEN}✓ step_id column exists${NC}" || \
    echo -e "${RED}✗ step_id column missing${NC}"
  
  echo ""
  echo "Checking treatment_plan_steps.completed_at..."
  mysql -u root -p clinic_db -e "DESCRIBE treatment_plan_steps;" | grep "completed_at" && \
    echo -e "${GREEN}✓ completed_at column exists${NC}" || \
    echo -e "${RED}✗ completed_at column missing${NC}"
else
  echo -e "${YELLOW}⚠ mysql client not found, skipping database checks${NC}"
  echo "Please run manually:"
  echo "  mysql -u root -p clinic_db -e 'DESCRIBE treatment_plans;'"
  echo "  mysql -u root -p clinic_db -e 'DESCRIBE prescriptions;'"
  echo "  mysql -u root -p clinic_db -e 'DESCRIBE treatment_plan_steps;'"
fi

echo ""
echo "========================================="
echo ""
echo -e "${GREEN}Testing completed!${NC}"
echo ""
echo "Next steps:"
echo "1. Review the output above"
echo "2. Check for any RED (✗) marks"
echo "3. If all GREEN (✓), Phase 1 is successful!"
echo "4. Run integration tests with real data"
echo ""
