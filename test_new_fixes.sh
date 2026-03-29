#!/bin/bash

# Test Script for New Fixes
# Date: 2026-03-28
# Tests: Critical Validations + Medical Records API

BASE_URL="http://localhost:8081"
PATIENT_TOKEN=""
DOCTOR_TOKEN=""

echo "=================================="
echo "TEST NEW FIXES - 2026-03-28"
echo "=================================="
echo ""

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Test counter
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

test_result() {
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✓ PASS${NC}: $2"
        PASSED_TESTS=$((PASSED_TESTS + 1))
    else
        echo -e "${RED}✗ FAIL${NC}: $2"
        FAILED_TESTS=$((FAILED_TESTS + 1))
    fi
}

echo "=================================="
echo "CRITICAL VALIDATIONS TESTS"
echo "=================================="
echo ""

# FIX 1: Password Validation
echo "--- FIX 1: Password Validation ---"

# Test 1.1: Password without numbers (should fail)
echo "Test 1.1: Password without numbers"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test1@test.com",
    "password": "abcdefgh",
    "firstName": "Test",
    "lastName": "User",
    "phone": "0123456789"
  }')
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
if [ "$HTTP_CODE" = "400" ]; then
    test_result 0 "Password without numbers rejected"
else
    test_result 1 "Password without numbers should be rejected (got $HTTP_CODE)"
fi

# Test 1.2: Password without letters (should fail)
echo "Test 1.2: Password without letters"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test2@test.com",
    "password": "123456789",
    "firstName": "Test",
    "lastName": "User",
    "phone": "0123456789"
  }')
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
if [ "$HTTP_CODE" = "400" ]; then
    test_result 0 "Password without letters rejected"
else
    test_result 1 "Password without letters should be rejected (got $HTTP_CODE)"
fi

# Test 1.3: Valid password (should pass)
echo "Test 1.3: Valid password with letters and numbers"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "testvalid@test.com",
    "password": "password123",
    "firstName": "Test",
    "lastName": "Valid",
    "phone": "0123456789"
  }')
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "201" ]; then
    test_result 0 "Valid password accepted"
else
    test_result 1 "Valid password should be accepted (got $HTTP_CODE)"
fi

echo ""

# Login to get tokens for further tests
echo "--- Getting Auth Tokens ---"
PATIENT_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "patient01@gmail.com",
    "password": "password123"
  }')
PATIENT_TOKEN=$(echo $PATIENT_RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)

DOCTOR_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "doc01@gmail.com",
    "password": "password123"
  }')
DOCTOR_TOKEN=$(echo $DOCTOR_RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)

if [ -z "$PATIENT_TOKEN" ]; then
    echo -e "${YELLOW}Warning: Could not get patient token${NC}"
else
    echo -e "${GREEN}✓ Patient token obtained${NC}"
fi

if [ -z "$DOCTOR_TOKEN" ]; then
    echo -e "${YELLOW}Warning: Could not get doctor token${NC}"
else
    echo -e "${GREEN}✓ Doctor token obtained${NC}"
fi

echo ""

# FIX 2: Phone Validation
echo "--- FIX 2: Phone Number Validation ---"

if [ ! -z "$PATIENT_TOKEN" ]; then
    # Test 2.1: Invalid phone format
    echo "Test 2.1: Invalid phone format"
    RESPONSE=$(curl -s -w "\n%{http_code}" -X PUT "$BASE_URL/api/patients/me" \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer $PATIENT_TOKEN" \
      -d '{
        "phone": "123"
      }')
    HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
    if [ "$HTTP_CODE" = "400" ]; then
        test_result 0 "Invalid phone format rejected"
    else
        test_result 1 "Invalid phone format should be rejected (got $HTTP_CODE)"
    fi

    # Test 2.2: Valid phone format (0xxxxxxxxx)
    echo "Test 2.2: Valid phone format (0xxxxxxxxx)"
    RESPONSE=$(curl -s -w "\n%{http_code}" -X PUT "$BASE_URL/api/patients/me" \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer $PATIENT_TOKEN" \
      -d '{
        "phone": "0987654321"
      }')
    HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
    if [ "$HTTP_CODE" = "200" ]; then
        test_result 0 "Valid phone format (0xxxxxxxxx) accepted"
    else
        test_result 1 "Valid phone format should be accepted (got $HTTP_CODE)"
    fi

    # Test 2.3: Valid phone format (+84xxxxxxxxx)
    echo "Test 2.3: Valid phone format (+84xxxxxxxxx)"
    RESPONSE=$(curl -s -w "\n%{http_code}" -X PUT "$BASE_URL/api/patients/me" \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer $PATIENT_TOKEN" \
      -d '{
        "phone": "+84987654321"
      }')
    HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
    if [ "$HTTP_CODE" = "200" ]; then
        test_result 0 "Valid phone format (+84xxxxxxxxx) accepted"
    else
        test_result 1 "Valid phone format should be accepted (got $HTTP_CODE)"
    fi
else
    echo -e "${YELLOW}Skipping phone validation tests (no patient token)${NC}"
fi

echo ""

# FIX 3: Date of Birth Validation
echo "--- FIX 3: Date of Birth Validation ---"

if [ ! -z "$PATIENT_TOKEN" ]; then
    # Test 3.1: Future date (should fail)
    echo "Test 3.1: Future date of birth"
    FUTURE_DATE="2030-01-01"
    RESPONSE=$(curl -s -w "\n%{http_code}" -X PUT "$BASE_URL/api/patients/me" \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer $PATIENT_TOKEN" \
      -d "{
        \"dob\": \"$FUTURE_DATE\"
      }")
    HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
    if [ "$HTTP_CODE" = "400" ]; then
        test_result 0 "Future date of birth rejected"
    else
        test_result 1 "Future date of birth should be rejected (got $HTTP_CODE)"
    fi

    # Test 3.2: Valid date
    echo "Test 3.2: Valid date of birth"
    VALID_DATE="1990-01-01"
    RESPONSE=$(curl -s -w "\n%{http_code}" -X PUT "$BASE_URL/api/patients/me" \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer $PATIENT_TOKEN" \
      -d "{
        \"dob\": \"$VALID_DATE\"
      }")
    HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
    if [ "$HTTP_CODE" = "200" ]; then
        test_result 0 "Valid date of birth accepted"
    else
        test_result 1 "Valid date of birth should be accepted (got $HTTP_CODE)"
    fi
else
    echo -e "${YELLOW}Skipping DOB validation tests (no patient token)${NC}"
fi

echo ""

# FIX 4: Appointment Date Validation
echo "--- FIX 4: Appointment Date Validation ---"

if [ ! -z "$PATIENT_TOKEN" ]; then
    # Test 4.1: Past date (should fail)
    echo "Test 4.1: Past appointment date"
    PAST_DATE="2020-01-01T10:00:00"
    RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/appointments" \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer $PATIENT_TOKEN" \
      -d "{
        \"serviceId\": 1,
        \"appointmentDatetime\": \"$PAST_DATE\"
      }")
    HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
    if [ "$HTTP_CODE" = "400" ]; then
        test_result 0 "Past appointment date rejected"
    else
        test_result 1 "Past appointment date should be rejected (got $HTTP_CODE)"
    fi
else
    echo -e "${YELLOW}Skipping appointment date validation tests (no patient token)${NC}"
fi

echo ""
echo "=================================="
echo "HIGH PRIORITY FEATURES TESTS"
echo "=================================="
echo ""

# FIX 6: Medical Records API
echo "--- FIX 6: Medical Records API ---"

if [ ! -z "$DOCTOR_TOKEN" ]; then
    # Test 6.1: Get medical records for patient
    echo "Test 6.1: Get medical records for patient ID 1"
    RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/doctor/patients/1/medical-records" \
      -H "Authorization: Bearer $DOCTOR_TOKEN")
    HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
    BODY=$(echo "$RESPONSE" | head -n -1)
    
    if [ "$HTTP_CODE" = "200" ]; then
        test_result 0 "Medical records API accessible"
        echo "Response preview: $(echo $BODY | head -c 200)..."
    else
        test_result 1 "Medical records API should return 200 (got $HTTP_CODE)"
    fi

    # Test 6.2: Get medical records for non-existent patient
    echo "Test 6.2: Get medical records for non-existent patient"
    RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/doctor/patients/99999/medical-records" \
      -H "Authorization: Bearer $DOCTOR_TOKEN")
    HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
    
    if [ "$HTTP_CODE" = "404" ]; then
        test_result 0 "Non-existent patient returns 404"
    else
        test_result 1 "Non-existent patient should return 404 (got $HTTP_CODE)"
    fi
else
    echo -e "${YELLOW}Skipping medical records tests (no doctor token)${NC}"
fi

echo ""
echo "=================================="
echo "TEST SUMMARY"
echo "=================================="
echo ""
echo "Total Tests: $TOTAL_TESTS"
echo -e "${GREEN}Passed: $PASSED_TESTS${NC}"
echo -e "${RED}Failed: $FAILED_TESTS${NC}"
echo ""

if [ $FAILED_TESTS -eq 0 ]; then
    echo -e "${GREEN}✓ ALL TESTS PASSED!${NC}"
    exit 0
else
    echo -e "${RED}✗ SOME TESTS FAILED${NC}"
    exit 1
fi
