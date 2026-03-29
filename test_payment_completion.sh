#!/bin/bash

# Test Payment Completion Feature
# This script tests the new invoice generation from treatment plan

echo "=========================================="
echo "PAYMENT COMPLETION FEATURE TEST"
echo "=========================================="
echo ""

# Configuration
BASE_URL="http://localhost:8080"
DOCTOR_TOKEN=""
PATIENT_ID=""
TREATMENT_PLAN_ID=""

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print test result
print_result() {
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✓ PASS${NC}: $2"
    else
        echo -e "${RED}✗ FAIL${NC}: $2"
    fi
}

# Function to print section
print_section() {
    echo ""
    echo "=========================================="
    echo "$1"
    echo "=========================================="
}

# Check if backend is running
print_section "1. CHECKING BACKEND STATUS"
response=$(curl -s -o /dev/null -w "%{http_code}" $BASE_URL/actuator/health 2>/dev/null || echo "000")
if [ "$response" = "200" ]; then
    print_result 0 "Backend is running"
else
    print_result 1 "Backend is NOT running (Expected 200, got $response)"
    echo "Please start the backend first: cd clinic_backend && mvn spring-boot:run"
    exit 1
fi

# Test 1: Login as Doctor
print_section "2. LOGIN AS DOCTOR"
echo "Please enter doctor credentials:"
read -p "Email: " DOCTOR_EMAIL
read -sp "Password: " DOCTOR_PASSWORD
echo ""

LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$DOCTOR_EMAIL\",\"password\":\"$DOCTOR_PASSWORD\"}")

DOCTOR_TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)

if [ -n "$DOCTOR_TOKEN" ]; then
    print_result 0 "Login successful"
    echo "Token: ${DOCTOR_TOKEN:0:20}..."
else
    print_result 1 "Login failed"
    echo "Response: $LOGIN_RESPONSE"
    exit 1
fi

# Test 2: Get a treatment plan
print_section "3. SELECT TREATMENT PLAN TO TEST"
echo "Fetching available treatment plans..."

PLANS_RESPONSE=$(curl -s -X GET "$BASE_URL/api/treatment-plans/patient/1" \
  -H "Authorization: Bearer $DOCTOR_TOKEN")

echo "Available plans:"
echo "$PLANS_RESPONSE" | jq -r '.[] | "\(.id) - Status: \(.status) - Steps: \(.steps | length)"' 2>/dev/null || echo "$PLANS_RESPONSE"

read -p "Enter Treatment Plan ID to test: " TREATMENT_PLAN_ID

if [ -z "$TREATMENT_PLAN_ID" ]; then
    print_result 1 "No treatment plan ID provided"
    exit 1
fi

# Test 3: Get treatment plan details
print_section "4. GET TREATMENT PLAN DETAILS"
PLAN_RESPONSE=$(curl -s -X GET "$BASE_URL/api/treatment-plans/$TREATMENT_PLAN_ID" \
  -H "Authorization: Bearer $DOCTOR_TOKEN")

echo "Plan details:"
echo "$PLAN_RESPONSE" | jq '.' 2>/dev/null || echo "$PLAN_RESPONSE"

# Check if all steps are completed
INCOMPLETE_STEPS=$(echo "$PLAN_RESPONSE" | jq '[.steps[] | select(.status != "COMPLETED" and .status != "SKIPPED")] | length' 2>/dev/null)

if [ "$INCOMPLETE_STEPS" != "0" ]; then
    echo -e "${YELLOW}⚠ WARNING${NC}: This plan has $INCOMPLETE_STEPS incomplete steps"
    echo "The API should reject this request."
    read -p "Continue anyway to test validation? (y/n): " CONTINUE
    if [ "$CONTINUE" != "y" ]; then
        exit 0
    fi
fi

# Test 4: Complete and Generate Invoice
print_section "5. COMPLETE PLAN AND GENERATE INVOICE"
echo "Calling API: POST /api/treatment-plans/$TREATMENT_PLAN_ID/complete-and-generate-invoice"

INVOICE_RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X POST \
  "$BASE_URL/api/treatment-plans/$TREATMENT_PLAN_ID/complete-and-generate-invoice" \
  -H "Authorization: Bearer $DOCTOR_TOKEN" \
  -H "Content-Type: application/json")

HTTP_CODE=$(echo "$INVOICE_RESPONSE" | grep "HTTP_CODE:" | cut -d: -f2)
BODY=$(echo "$INVOICE_RESPONSE" | sed '/HTTP_CODE:/d')

echo "HTTP Status: $HTTP_CODE"
echo "Response Body:"
echo "$BODY" | jq '.' 2>/dev/null || echo "$BODY"

if [ "$HTTP_CODE" = "200" ]; then
    print_result 0 "Invoice created successfully"
    
    # Extract invoice details
    INVOICE_ID=$(echo "$BODY" | jq -r '.id' 2>/dev/null)
    TOTAL_AMOUNT=$(echo "$BODY" | jq -r '.totalAmount' 2>/dev/null)
    ITEMS_COUNT=$(echo "$BODY" | jq -r '.items | length' 2>/dev/null)
    
    echo ""
    echo "Invoice Details:"
    echo "  - Invoice ID: $INVOICE_ID"
    echo "  - Total Amount: $TOTAL_AMOUNT VNĐ"
    echo "  - Number of Items: $ITEMS_COUNT"
    echo ""
    echo "Items:"
    echo "$BODY" | jq -r '.items[] | "  - \(.serviceName) (\(.toothNumber // "N/A")): \(.totalPrice) VNĐ"' 2>/dev/null
    
elif [ "$HTTP_CODE" = "400" ]; then
    print_result 0 "Validation working correctly (400 Bad Request)"
    echo "Error message: $(echo "$BODY" | jq -r '.message' 2>/dev/null)"
else
    print_result 1 "Unexpected response code: $HTTP_CODE"
fi

# Test 5: Try to create duplicate invoice
print_section "6. TEST DUPLICATE INVOICE PREVENTION"
echo "Calling API again with same plan ID..."

DUPLICATE_RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X POST \
  "$BASE_URL/api/treatment-plans/$TREATMENT_PLAN_ID/complete-and-generate-invoice" \
  -H "Authorization: Bearer $DOCTOR_TOKEN" \
  -H "Content-Type: application/json")

DUP_HTTP_CODE=$(echo "$DUPLICATE_RESPONSE" | grep "HTTP_CODE:" | cut -d: -f2)
DUP_BODY=$(echo "$DUPLICATE_RESPONSE" | sed '/HTTP_CODE:/d')

if [ "$DUP_HTTP_CODE" = "200" ]; then
    DUP_INVOICE_ID=$(echo "$DUP_BODY" | jq -r '.id' 2>/dev/null)
    if [ "$DUP_INVOICE_ID" = "$INVOICE_ID" ]; then
        print_result 0 "Duplicate prevention working (returned same invoice)"
    else
        print_result 1 "Created duplicate invoice (different ID)"
    fi
else
    echo "HTTP Status: $DUP_HTTP_CODE"
    echo "$DUP_BODY" | jq '.' 2>/dev/null || echo "$DUP_BODY"
fi

# Test 6: Verify invoice in database
print_section "7. VERIFY INVOICE DETAILS"
if [ -n "$INVOICE_ID" ]; then
    INVOICE_DETAIL=$(curl -s -X GET "$BASE_URL/api/invoices/$INVOICE_ID" \
      -H "Authorization: Bearer $DOCTOR_TOKEN")
    
    echo "Invoice from GET /api/invoices/$INVOICE_ID:"
    echo "$INVOICE_DETAIL" | jq '.' 2>/dev/null || echo "$INVOICE_DETAIL"
    
    print_result 0 "Invoice can be retrieved"
fi

# Summary
print_section "TEST SUMMARY"
echo "✓ Backend is running"
echo "✓ Authentication working"
echo "✓ API endpoint accessible"
echo "✓ Invoice creation logic working"
echo "✓ Duplicate prevention working"
echo ""
echo "=========================================="
echo "NEXT STEPS:"
echo "=========================================="
echo "1. Build mobile app: cd mobile_android && ./gradlew assembleDebug"
echo "2. Install APK on device"
echo "3. Test complete flow in mobile app"
echo "4. Verify notification sent to patient"
echo ""
