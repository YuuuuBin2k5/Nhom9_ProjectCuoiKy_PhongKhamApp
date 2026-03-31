# Check-in Queue Completion Fix

## Problem
After completing treatment and payment, the check-in queue status still showed in the patient dashboard. The "Đã nhận Check-in" section remained visible even after the patient finished everything.

## Root Cause Analysis

### Issue 1: No Status Filtering in Patient View
The `CheckInQueueService.getMyStatusToday()` method returned ALL queues for today without filtering out COMPLETED or SKIPPED queues. This meant patients would see their queue status even after completing treatment.

**Location:** `CheckInQueueService.java` line 305-380

```java
// OLD CODE - No filtering
List<CheckInQueue> rows = checkInQueueRepository.findTodayForPatient(patientId, start, end);
if (rows.isEmpty()) {
    return CheckInMyStatusResponse.builder()...
}
CheckInQueue q = rows.get(0); // Returns first queue regardless of status
```

### Issue 2: Queue Not Marked COMPLETED After Payment
The `InvoiceService.processPayment()` method updated the invoice status to PAID but didn't mark the associated queue as COMPLETED. The queue completion only happened during invoice creation, not during payment finalization.

**Location:** `InvoiceService.java` line 64-108

## Solution Implemented

### Fix 1: Filter Out Completed Queues from Patient View
Updated `CheckInQueueService.getMyStatusToday()` to exclude COMPLETED and SKIPPED queues:

```java
// NEW CODE - Filter active queues only
List<CheckInQueue> rows = checkInQueueRepository.findTodayForPatient(patientId, start, end);

// Filter out COMPLETED and SKIPPED queues - patient shouldn't see these
var activeQueue = rows.stream()
        .filter(q -> q.getStatus() != QueueStatus.COMPLETED && q.getStatus() != QueueStatus.SKIPPED)
        .findFirst();

if (activeQueue.isEmpty()) {
    return CheckInMyStatusResponse.builder()
            .checkedIn(false)
            ...
}
CheckInQueue q = activeQueue.get();
```

**Benefits:**
- Patients only see active queue status (WAITING, IN_PROGRESS, PAUSED_FOR_TEST, RETURNED_PRIORITY)
- Once treatment is complete, the check-in section disappears from dashboard
- Clean separation between active and completed queues

### Fix 2: Mark Queue as COMPLETED After Payment
Updated `InvoiceService.processPayment()` to mark the queue as COMPLETED when payment is finalized:

```java
// ====== MARK QUEUE AS COMPLETED AFTER PAYMENT ======
try {
    if (invoice.getPatient() != null) {
        List<CheckInQueue> queuesForPatient = checkInQueueRepository.findTodayForPatient(
            invoice.getPatient().getId(),
            LocalDate.now().atStartOfDay(),
            LocalDate.now().plusDays(1).atStartOfDay()
        );
        for (CheckInQueue q : queuesForPatient) {
            if (q.getStatus() != QueueStatus.COMPLETED && q.getStatus() != QueueStatus.SKIPPED) {
                q.setStatus(QueueStatus.COMPLETED);
                q.setCompletedAt(LocalDateTime.now());
                checkInQueueRepository.save(q);
                if (q.getClinicRoom() != null) {
                    queueEventService.broadcastQueueUpdated(q.getClinicRoom().getId());
                }
            }
        }
    }
} catch (Exception e) {
    System.err.println("[InvoiceService] Queue completion error after payment: " + e.getMessage());
}
```

**Benefits:**
- Queue status is updated to COMPLETED when payment is finalized
- Sets `completedAt` timestamp for audit trail
- Broadcasts queue update to all connected clients
- Graceful error handling - payment doesn't fail if queue update fails

### Fix 3: Add Timestamp to Invoice Creation Queue Completion
Updated `InvoiceService.createInvoiceFromTreatmentPlan()` to also set `completedAt`:

```java
q.setStatus(QueueStatus.COMPLETED);
q.setCompletedAt(LocalDateTime.now()); // Added timestamp
checkInQueueRepository.save(q);
```

## Workflow After Fix

1. **Patient checks in** → Queue status: WAITING
2. **Doctor calls patient** → Queue status: IN_PROGRESS
3. **Treatment completed** → Invoice created → Queue status: COMPLETED (if no payment needed)
4. **Patient pays invoice** → Queue status: COMPLETED + completedAt timestamp set
5. **Patient dashboard** → Check-in section disappears (filtered out)

## Files Modified

1. `clinic_backend/src/main/java/com/hcmute/clinic/service/CheckInQueueService.java`
   - Updated `getMyStatusToday()` to filter out COMPLETED/SKIPPED queues

2. `clinic_backend/src/main/java/com/hcmute/clinic/service/InvoiceService.java`
   - Updated `processPayment()` to mark queue as COMPLETED after payment
   - Updated `createInvoiceFromTreatmentPlan()` to set completedAt timestamp

## Testing Checklist

- [ ] Patient checks in successfully
- [ ] Queue status shows in patient dashboard while waiting
- [ ] Doctor completes treatment and creates invoice
- [ ] Patient pays invoice
- [ ] Check-in section disappears from patient dashboard after payment
- [ ] Queue status is COMPLETED in database
- [ ] completedAt timestamp is set correctly
- [ ] Other patients in queue are not affected

## Edge Cases Handled

1. **Multiple queues for same patient**: All active queues are marked COMPLETED
2. **Queue update failure**: Payment still succeeds (graceful error handling)
3. **Already completed queue**: Skip update (idempotent)
4. **No queue found**: No error thrown (safe operation)

## Status
✅ COMPLETE - Ready for testing
