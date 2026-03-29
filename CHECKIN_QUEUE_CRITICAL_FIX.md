# CRITICAL FIX: Check-in Queue originalRoomId Missing

## ISSUE FOUND
During comprehensive code review of check-in and queue management system, discovered critical bug in `CheckInQueueService.processSelfScan()`.

## BUG DESCRIPTION
**Location**: `clinic_backend/src/main/java/com/hcmute/clinic/service/CheckInQueueService.java` Line 630

**Problem**: When patient checks in via mobile app (self-scan), the `originalRoomId` field is NOT set in the queue entry.

**Impact**: 
- When patient is transferred to X-ray room and completes X-ray
- `completeXRay()` method tries to return patient to original room
- Without `originalRoomId`, it falls back to `appointment.doctor.clinicRoom`
- If doctor's room assignment changed, patient returns to wrong room
- Breaks the room transfer workflow

## CODE COMPARISON

### ❌ BEFORE (Bug)
```java
// Line 630 - processSelfScan()
CheckInQueue queue = CheckInQueue.builder()
        .appointment(appointment)
        .clinicRoom(room)
        // ❌ Missing originalRoomId
        .queueNumber(nextNumber)
        .checkInTime(LocalDateTime.now())
        .status(QueueStatus.WAITING)
        .priorityLevel(0)
        .build();
```

### ✅ AFTER (Fixed)
```java
// Line 630 - processSelfScan()
CheckInQueue queue = CheckInQueue.builder()
        .appointment(appointment)
        .clinicRoom(room)
        .originalRoomId(room.getId()) // ✅ ADDED
        .queueNumber(nextNumber)
        .checkInTime(LocalDateTime.now())
        .status(QueueStatus.WAITING)
        .priorityLevel(0)
        .build();
```

## WHY THIS MATTERS

### Room Transfer Flow
1. Patient checks in → Assigned to Room 01 (originalRoomId = 1)
2. Doctor completes Step 1 → Patient transferred to X-ray room
3. Patient completes X-ray → Should return to Room 01 (originalRoomId)
4. Without originalRoomId → Returns to wrong room or fails

### completeXRay() Logic
```java
// Line 465-485
public void completeXRay(Long queueId) {
    CheckInQueue q = checkInQueueRepository.findById(queueId)...;
    q.setStatus(QueueStatus.RETURNED_PRIORITY);
    q.setPriorityLevel((q.getPriorityLevel() != null ? q.getPriorityLevel() : 0) + 10);
    
    // ⚠️ This relies on originalRoomId being set correctly
    var examRoom = q.getAppointment() != null && q.getAppointment().getDoctor() != null
            ? q.getAppointment().getDoctor().getClinicRoom()
            : null;
    
    if (examRoom != null) {
        q.setClinicRoom(examRoom); // Returns to exam room
    }
}
```

## FIX APPLIED
✅ Added `.originalRoomId(room.getId())` to queue builder in `processSelfScan()`
✅ Now consistent with `processScan()` which already had this field set

## TESTING REQUIRED
1. Patient checks in via mobile app
2. Doctor transfers patient to X-ray
3. Patient completes X-ray
4. Verify patient returns to correct original room
5. Verify originalRoomId is preserved in database

## STATUS
✅ FIXED - Code updated in CheckInQueueService.java
