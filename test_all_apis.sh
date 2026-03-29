#!/bin/bash

# 🧪 Complete API Testing Script
# Tests all Phase 1 & Phase 2 APIs

echo "🧪 Starting Complete API Test Suite..."
echo "======================================"

# Configuration
BASE_URL="http://localhost:8081/api"
TOKEN=""

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Test counter
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# Function to print test result
print_result() {
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✓ PASSED${NC}: $2"
        PASSED_TESTS=$((PASSED_TESTS + 1))
    else
        echo -e "${RED}✗ FAILED${NC}: $2"
        FAILED_TESTS=$((FAILED_TESTS + 1))
    fi
}

# Function to test endpoint
test_endpoint() {
    local method=$1
    local endpoint=$2
    local description=$3
    local data=$4
    
    echo ""
    echo "Testing: $description"
    echo "Endpoint: $method $endpoint"
    
    if [ "$method" = "GET" ]; then
        response=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL$endpoint" \
            -H "Authorization: Bearer $TOKEN")
    elif [ "$method" = "POST" ]; then
        response=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL$endpoint" \
            -H "Authorization: Bearer $TOKEN" \
            -H "Content-Type: application/json" \
            -d "$data")
    elif [ "$method" = "PATCH" ]; then
        response=$(curl -s -w "\n%{http_code}" -X PATCH "$BASE_URL$endpoint" \
            -H "Authorization: Bearer $TOKEN" \
            -H "Content-Type: application/json" \
            -d "$data")
    elif [ "$method" = "PUT" ]; then
        response=$(curl -s -w "\n%{http_code}" -X PUT "$BASE_URL$endpoint" \
            -H "Authorization: Bearer $TOKEN" \
            -H "Content-Type: application/json" \
            -d "$data")
    fi
    
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" -ge 200 ] && [ "$http_code" -lt 300 ]; then
        print_result 0 "$description"
        echo "Response: $body" | head -c 200
        echo "..."
    else
        print_result 1 "$description (HTTP $http_code)"
        echo "Error: $body"
    fi
}

# Step 1: Login to get token
echo ""
echo "📝 Step 1: Authentication"
echo "========================="

login_response=$(curl -s -X POST "$BASE_URL/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"email":"patient01@gmail.com","password":"123456"}')

TOKEN=$(echo $login_response | grep -o '"token":"[^"]*' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
    echo -e "${RED}✗ Login failed! Cannot proceed with tests.${NC}"
    echo "Response: $login_response"
    exit 1
else
    echo -e "${GREEN}✓ Login successful${NC}"
    echo "Token: ${TOKEN:0:50}..."
fi

# Step 2: Test Phase 1 APIs
echo ""
echo "📝 Step 2: Phase 1 - Treatment Plan APIs"
echo "========================================="

test_endpoint "GET" "/doctor/patient-by-qr?qrCode=TEST123" \
    "Get patient by QR (with 3 new fields)"

test_endpoint "POST" "/treatment-plans/from-appointment" \
    "Create treatment plan from appointment" \
    '{"appointmentId":1,"templateId":1}'

# Step 3: Test Phase 2 - Invoice & Payment APIs
echo ""
echo "📝 Step 3: Phase 2 - Invoice & Payment APIs"
echo "============================================"

test_endpoint "GET" "/invoices/my" \
    "Get my invoices"

test_endpoint "GET" "/invoices/1" \
    "Get invoice detail"

test_endpoint "POST" "/invoices/1/pay" \
    "Process payment" \
    '{"paymentMethod":"CASH","amount":500000,"note":"Test payment"}'

# Step 4: Test Phase 2 - Review APIs
echo ""
echo "📝 Step 4: Phase 2 - Review APIs"
echo "================================="

test_endpoint "POST" "/reviews" \
    "Create review" \
    '{"appointmentId":1,"doctorId":1,"serviceId":1,"rating":5,"comment":"Excellent service!"}'

test_endpoint "GET" "/reviews/my" \
    "Get my reviews"

test_endpoint "GET" "/reviews/doctor/1" \
    "Get doctor reviews"

test_endpoint "GET" "/reviews/service/1" \
    "Get service reviews"

# Step 5: Test Phase 2 - Admin Report APIs
echo ""
echo "📝 Step 5: Phase 2 - Admin Report APIs"
echo "======================================="

test_endpoint "GET" "/admin/reports/revenue?startDate=2024-01-01&endDate=2024-12-31" \
    "Get revenue report"

test_endpoint "GET" "/admin/reports/top-services?limit=10" \
    "Get top services"

test_endpoint "GET" "/admin/reports/doctor-performance?startDate=2024-01-01&endDate=2024-12-31" \
    "Get doctor performance"

# Step 6: Test Phase 2 - Appointment APIs
echo ""
echo "📝 Step 6: Phase 2 - Appointment APIs"
echo "======================================"

test_endpoint "GET" "/appointments/available-slots?doctorId=1&date=2024-12-25" \
    "Get available time slots"

test_endpoint "PATCH" "/appointments/1/cancel" \
    "Cancel appointment" \
    '{"reason":"Personal reason"}'

test_endpoint "PUT" "/appointments/1/reschedule" \
    "Reschedule appointment" \
    '{"newDate":"2024-12-26","newTime":"10:00"}'

# Step 7: Test Phase 2 - Reception APIs
echo ""
echo "📝 Step 7: Phase 2 - Reception APIs"
echo "===================================="

test_endpoint "POST" "/reception/checkin/scan" \
    "Reception check-in scan" \
    '{"qrCode":"TEST123"}'

test_endpoint "POST" "/reception/payment/process" \
    "Reception process payment" \
    '{"invoiceId":1,"paymentMethod":"CASH","amount":500000}'

test_endpoint "GET" "/reception/queue/today" \
    "Get today's queue"

# Step 8: Test Phase 2 - Other APIs
echo ""
echo "📝 Step 8: Phase 2 - Other APIs"
echo "================================"

test_endpoint "GET" "/treatment-plans/steps/1/images" \
    "Get step images"

test_endpoint "PATCH" "/notifications/read-all" \
    "Mark all notifications as read"

# Step 9: Test Phase 3 - Search APIs
echo ""
echo "📝 Step 9: Phase 3 - Search APIs"
echo "================================="

test_endpoint "GET" "/search/patients?query=nguyen" \
    "Search patients"

test_endpoint "GET" "/search/services?query=kham" \
    "Search services"

test_endpoint "GET" "/search/appointments?date=2024-12-25" \
    "Search appointments by date"

# Final Summary
echo ""
echo "======================================"
echo "📊 TEST SUMMARY"
echo "======================================"
echo -e "Total Tests: ${YELLOW}$TOTAL_TESTS${NC}"
echo -e "Passed: ${GREEN}$PASSED_TESTS${NC}"
echo -e "Failed: ${RED}$FAILED_TESTS${NC}"

if [ $FAILED_TESTS -eq 0 ]; then
    echo ""
    echo -e "${GREEN}🎉 ALL TESTS PASSED! 🎉${NC}"
    exit 0
else
    echo ""
    echo -e "${RED}⚠️  SOME TESTS FAILED ⚠️${NC}"
    exit 1
fi
