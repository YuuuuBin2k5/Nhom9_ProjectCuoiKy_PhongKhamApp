# Queue Countdown Timer - Implementation Plan

## Overview

Thêm real-time countdown timer cho patient queue với các nguyên tắc:
- **Chỉ countdown cho position #1** (người kế tiếp)
- **Luôn hiển thị dấu ~** để tránh kỳ vọng sai
- **Sync với server mỗi 30s** để điều chỉnh
- **Graceful degradation** nếu estimate sai

## Architecture

```
┌─────────────────────────────────────────┐
│  PatientQueueActivity                   │
│                                         │
│  ┌───────────────────────────────────┐ │
│  │  CountdownTimer (Handler)         │ │
│  │  • Tick every 1 second            │ │
│  │  • Update UI: MM:SS               │ │
│  │  • Stop when reaches 0            │ │
│  └───────────────────────────────────┘ │
│              ↓                          │
│  ┌───────────────────────────────────┐ │
│  │  Server Sync (30s interval)       │ │
│  │  • Fetch latest estimate          │ │
│  │  • Adjust countdown if needed     │ │
│  │  • Handle position changes        │ │
│  └───────────────────────────────────┘ │
└─────────────────────────────────────────┘
```

## Implementation Steps

### Step 1: Update Backend DTO

Add `countdownStartSeconds` field to response:

```java
// CheckInMyStatusResponse.java
private Integer countdownStartSeconds; // For position 1 only
```

### Step 2: Update Backend Service

Calculate countdown start time:

```java
// QueueEstimationService.java
private QueueEstimateDTO calculateSoftCountdown(CheckInQueue current) {
    // ... existing code ...
    
    int remainingTime = Math.max(2, stats.getP75() / 2);
    int countdownSeconds = remainingTime * 60; // Convert to seconds
    
    return QueueEstimateDTO.builder()
        .displayType("SOFT_COUNTDOWN")
        .estimatedMinutes(remainingTime)
        .countdownStartSeconds(countdownSeconds) // NEW
        // ... rest ...
        .build();
}
```

### Step 3: Update Mobile Model

```java
// CheckInMyStatusResponse.java
private Integer countdownStartSeconds;

public Integer getCountdownStartSeconds() {
    return countdownStartSeconds;
}
```

### Step 4: Implement Countdown Timer in Mobile

Update `PatientQueueActivity.java`:

```java
private Handler countdownHandler;
private Runnable countdownRunnable;
private long countdownEndTimeMillis = 0;
private boolean isCountdownActive = false;

private void startCountdown(int seconds) {
    stopCountdown(); // Stop any existing countdown
    
    countdownEndTimeMillis = System.currentTimeMillis() + (seconds * 1000L);
    isCountdownActive = true;
    
    countdownHandler = new Handler(Looper.getMainLooper());
    countdownRunnable = new Runnable() {
        @Override
        public void run() {
            updateCountdownDisplay();
            if (isCountdownActive) {
                countdownHandler.postDelayed(this, 1000); // Update every second
            }
        }
    };
    
    countdownHandler.post(countdownRunnable);
}

private void updateCountdownDisplay() {
    long remainingMillis = countdownEndTimeMillis - System.currentTimeMillis();
    
    if (remainingMillis <= 0) {
        // Countdown finished
        tvEstimatedTime.setText("~0 phút");
        stopCountdown();
        // Trigger refresh to get latest status
        loadQueueStatus();
        return;
    }
    
    int remainingSeconds = (int) (remainingMillis / 1000);
    int minutes = remainingSeconds / 60;
    int seconds = remainingSeconds % 60;
    
    // Always show ~ to indicate approximate
    tvEstimatedTime.setText(String.format("~%d:%02d", minutes, seconds));
}

private void stopCountdown() {
    isCountdownActive = false;
    if (countdownHandler != null && countdownRunnable != null) {
        countdownHandler.removeCallbacks(countdownRunnable);
    }
}

private void syncCountdownWithServer(int newSeconds) {
    if (!isCountdownActive) return;
    
    long currentRemaining = (countdownEndTimeMillis - System.currentTimeMillis()) / 1000;
    long diff = Math.abs(currentRemaining - newSeconds);
    
    // Only adjust if difference > 30 seconds (avoid jitter)
    if (diff > 30) {
        countdownEndTimeMillis = System.currentTimeMillis() + (newSeconds * 1000L);
    }
}
```

### Step 5: Update `updateQueueStatus()` Method

```java
private void updateQueueStatus(CheckInMyStatusResponse status) {
    if (status.isCheckedIn()) {
        layoutNotCheckedIn.setVisibility(View.GONE);
        layoutQueueInfo.setVisibility(View.VISIBLE);
        
        tvQueueNumber.setText(String.valueOf(status.getQueueNumber()));
        tvRoomName.setText(status.getRoomName() != null ? status.getRoomName() : "Phòng khám");
        
        String displayType = status.getEstimateDisplayType();
        
        if ("IN_PROGRESS".equals(displayType)) {
            stopCountdown(); // Stop countdown if being served
            tvQueuePosition.setText(status.getEstimateTitle() != null ? status.getEstimateTitle() : "Đang khám");
            tvEstimatedTime.setText(status.getEstimateSubtitle() != null ? status.getEstimateSubtitle() : "");
            tvStatus.setText(status.getEstimateMessage() != null ? status.getEstimateMessage() : "");
            setCardColor("IN_PROGRESS");
            
        } else if ("SOFT_COUNTDOWN".equals(displayType)) {
            // Start or sync countdown
            Integer countdownSeconds = status.getCountdownStartSeconds();
            if (countdownSeconds != null && countdownSeconds > 0) {
                if (!isCountdownActive) {
                    startCountdown(countdownSeconds);
                } else {
                    syncCountdownWithServer(countdownSeconds);
                }
            }
            
            tvQueuePosition.setText(status.getEstimateTitle() != null ? status.getEstimateTitle() : "Bạn kế tiếp");
            tvStatus.setText(status.getEstimateMessage() != null ? status.getEstimateMessage() : "Vui lòng ở gần");
            setCardColor("RETURNED_PRIORITY");
            
        } else if ("RANGE".equals(displayType)) {
            stopCountdown(); // Stop countdown for range estimates
            tvQueuePosition.setText(status.getEstimateTitle() != null ? status.getEstimateTitle() : "Vị trí: " + status.getQueuePosition());
            tvEstimatedTime.setText(status.getEstimateSubtitle() != null ? status.getEstimateSubtitle() : status.getEstimatedWaitTime() + " phút");
            String message = status.getEstimateMessage() != null ? status.getEstimateMessage() : "";
            String confidence = status.getEstimateConfidence() != null ? " (Độ tin cậy: " + status.getEstimateConfidence() + ")" : "";
            tvStatus.setText(message + confidence);
            setCardColor(status.getStatus());
            
        } else {
            stopCountdown();
            tvQueuePosition.setText("Vị trí: " + status.getQueuePosition());
            tvEstimatedTime.setText("Ước tính: " + status.getEstimatedWaitTime() + " phút");
            tvStatus.setText(getStatusDisplay(status.getStatus()));
            setCardColor(status.getStatus());
        }
        
    } else {
        stopCountdown();
        showNotCheckedIn();
    }
}
```

### Step 6: Lifecycle Management

```java
@Override
protected void onPause() {
    super.onPause();
    refreshHandler.removeCallbacks(refreshRunnable);
    stopCountdown(); // Pause countdown when not visible
}

@Override
protected void onResume() {
    super.onResume();
    refreshHandler.postDelayed(refreshRunnable, AUTO_REFRESH_INTERVAL);
    // Countdown will restart from server data on next refresh
}

@Override
protected void onDestroy() {
    super.onDestroy();
    if (refreshHandler != null) {
        refreshHandler.removeCallbacks(refreshRunnable);
    }
    stopCountdown();
}
```

## UX Design

### Position #1 (With Countdown)

```
┌─────────────────────────────┐
│  Số: 6                      │
│  🟠 Bạn kế tiếp             │
│  ~4:35                      │  ← Countdown timer
│  Vui lòng ở gần phòng khám  │
└─────────────────────────────┘
```

### Position #2+ (No Countdown)

```
┌─────────────────────────────┐
│  Số: 7                      │
│  🔵 Vị trí: #5              │
│  30-45 phút                 │  ← Static range
│  Thời gian ước tính         │
└─────────────────────────────┘
```

## Edge Cases Handled

### 1. Countdown Reaches Zero But Still Waiting

```java
if (remainingMillis <= 0) {
    tvEstimatedTime.setText("~0 phút");
    stopCountdown();
    loadQueueStatus(); // Fetch latest - maybe delayed
}
```

### 2. Position Changes During Countdown

Server sync will detect position change and stop countdown if no longer position #1.

### 3. App Goes to Background

Countdown pauses. When resumed, fetches fresh data from server.

### 4. Server Estimate Changes Significantly

```java
if (diff > 30) { // More than 30 seconds difference
    countdownEndTimeMillis = System.currentTimeMillis() + (newSeconds * 1000L);
}
```

### 5. Network Error During Countdown

Countdown continues with last known time. Will sync on next successful refresh.

## Testing Checklist

- [ ] Countdown starts when position = 1
- [ ] Countdown updates every second
- [ ] Countdown shows MM:SS format with ~ prefix
- [ ] Countdown syncs with server every 30s
- [ ] Countdown stops when position changes
- [ ] Countdown stops when called to room
- [ ] Countdown pauses when app goes to background
- [ ] Countdown handles reaching zero gracefully
- [ ] No memory leaks (handlers cleaned up)
- [ ] No crashes on rapid position changes

## Performance Considerations

- Handler updates every 1 second (minimal CPU)
- Server sync every 30 seconds (existing interval)
- Countdown stops when not needed (saves battery)
- No network calls for countdown ticks (client-side only)

## Rollback Plan

If countdown causes issues:
1. Remove countdown logic from mobile
2. Keep backend `countdownStartSeconds` field (ignored by old clients)
3. Fall back to static "~5 phút" display

## Success Metrics

- User engagement: Do patients stay near the room when countdown is active?
- Accuracy: How often does countdown reach zero vs actual call time?
- Satisfaction: User feedback on countdown feature

---

**Status**: Design complete, ready for implementation
**Estimated Time**: 2-3 hours
**Risk Level**: Low (graceful degradation built-in)
