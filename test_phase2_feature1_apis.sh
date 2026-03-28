#!/bin/bash

# Phase 2 Feature 1: Payment & Review System - API Tests
# Date: 28/03/2026

BASE_URL="http://localhost:8081"
PATIENT_TOKEN=""
DOCTOR_TOKEN=""

echo "=========================================="
echo "PHASE 2 FEATURE 1 - API TESTS"
echo "Payment & Review System"
echo "=========================================="
echo ""

# Test 1: Get Patient Invoices
echo "Test 1: GET /api/invoices/my"
echo "Expected: List of patient invoices"
echo "Command: curl -X GET $BASE_URL/api/invoices/my -H 'Authorization: Bearer \$PATIENT_TOKEN'"
echo ""
echo "Note: Need to login as patient first to get token"
echo ""

# Test 2: Get Invoice Detail
echo "Test 2: GET /api/invoices/{id}"
echo "Expected: Invoice detail with items"
echo "Command: curl -X GET $BASE_URL/api/invoices/1"
echo ""

# Test 3: Process Payment
echo "Test 3: POST /api/invoices/{id}/pay"
echo "Expected: Payment successful"
echo "Command:"
echo "curl -X POST $BASE_URL/api/invoices/1/pay \\"
echo "  -H 'Content-Type: application/json' \\"
echo "  -H 'Authorization: Bearer \$PATIENT_TOKEN' \\"
echo "  -d '{"
echo "    \"paymentMethod\": \"CASH\","
echo "    \"amount\": 500000,"
echo "    \"note\": \"Thanh toán tiền mặt\""
echo "  }'"
echo ""

# Test 4: Create Review
echo "Test 4: POST /api/reviews"
echo "Expected: Review created successfully"
echo "Command:"
echo "curl -X POST $BASE_URL/api/reviews \\"
echo "  -H 'Content-Type: application/json' \\"
echo "  -H 'Authorization: Bearer \$PATIENT_TOKEN' \\"
echo "  -d '{"
echo "    \"appointmentId\": 1,"
echo "    \"doctorId\": 1,"
echo "    \"serviceId\": 1,"
echo "    \"rating\": 5,"
echo "    \"comment\": \"Bác sĩ rất tận tâm và chuyên nghiệp\""
echo "  }'"
echo ""

# Test 5: Get My Reviews
echo "Test 5: GET /api/reviews/my"
echo "Expected: List of patient reviews"
echo "Command: curl -X GET $BASE_URL/api/reviews/my -H 'Authorization: Bearer \$PATIENT_TOKEN'"
echo ""

# Test 6: Get Doctor Reviews
echo "Test 6: GET /api/reviews/doctor/{doctorId}"
echo "Expected: List of reviews for specific doctor"
echo "Command: curl -X GET $BASE_URL/api/reviews/doctor/1"
echo ""

# Test 7: Get Service Reviews
echo "Test 7: GET /api/reviews/service/{serviceId}"
echo "Expected: List of reviews for specific service"
echo "Command: curl -X GET $BASE_URL/api/reviews/service/1"
echo ""

echo "=========================================="
echo "MANUAL TESTING REQUIRED"
echo "=========================================="
echo ""
echo "1. Login as patient to get token"
echo "2. Create an appointment and complete it"
echo "3. Check if invoice is created"
echo "4. Test payment processing"
echo "5. Test review creation"
echo ""
echo "All endpoints are ready for testing!"
echo ""
