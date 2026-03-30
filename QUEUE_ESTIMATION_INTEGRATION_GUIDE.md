# Queue Estimation - Integration Guide

## Quick Integration Steps

### Step 1: Integrate Duration Tracking

Add to `CheckInQueueService.java`:

```java
@Autowired
private ServiceDurationTracker durationTracker;

// When starting a patient (status → IN_PROGRESS)
public void startPatient(Long queueId) {
    CheckInQueue queue = queueRepo.findById(queueId).orElseThrow();
    queue.setStatus(QueueStatus.IN_PROGRESS);
    durationTracker.markStarted(queue); // ADD THIS
    queueRepo.save(queue);
}

// When completing a patient (status → COMPLETED)
public void completePatient(Long queueId) {
    CheckInQueue queue = queueRepo.findById(queueId).orElseThrow();
    queue.setStatus(QueueStatus.COMPLETED);
    durationTracker.markCompleted(queue); // ADD THIS
    queueRepo.save(queue);
}
```

### Step 2: Add Estimates to Queue Responses

Update `CheckInQueueService.mapToQueueItemDto()`:

```java
@Autowired
private QueueEstimationService estimationService;

private QueueItemDto mapToQueueItemDto(CheckInQueue q) {
    QueueItemDto dto = new QueueItemDto();
    // ... existing mapping ...
    
    // Add estimate
    try {
        QueueEstimateDTO estimate = estimationService.calculateEstimate(q);
        dto.setEstimateDisplayType(estimate.getDisplayType());
        dto.setEstimatedMinutes(estimate.getEstimatedMinutes());
        dto.setMinMinutes(estimate.getMinMinutes());
        dto.setMaxMinutes(estimate.getMaxMinutes());
        dto.setEstimateMessage(estimate.getMessage());
        dto.setEstimateConfidence(estimate.getConfidence());
        dto.setShowApproximateLabel(estimate.getShowApproximateLabel());
        dto.setEstimateTitle(estimate.getTitle());
        dto.setEstimateSubtitle(estimate.getSubtitle());
    } catch (Exception e) {
        log.error("Failed to calculate estimate for queue {}", q.getId(), e);
        // Graceful degradation: continue without estimate
    }
    
    return dto;
}
```

### Step 3: Mobile UI Update

Update `PatientQueueActivity.java`:

```java
private void loadQueueStatus() {
    apiService.getQueueEstimateByAppointment(appointmentId)
        .enqueue(new Callback<QueueItem>() {
            @Override
            public void onResponse(Call<QueueItem> call, Response<QueueItem> response) {
                if (response.isSuccessful() && response.body() != null) {
                    displayQueueEstimate(response.body());
                }
            }
            
            @Override
            public void onFailure(Call<QueueItem> call, Throwable t) {
                // Fallback to old display
                showBasicQueueInfo();
            }
        });
}

private void displayQueueEstimate(QueueItem item) {
    String displayType = item.getEstimateDisplayType();
    
    if ("IN_PROGRESS".equals(displayType)) {
        tvStatus.setText(item.getEstimateTitle());
        tvWaitTime.setText(item.getEstimateSubtitle());
        tvStatus.setTextColor(Color.parseColor("#4CAF50"));
        
    } else if ("SOFT_COUNTDOWN".equals(displayType)) {
        tvStatus.setText(item.getEstimateTitle());
        tvWaitTime.setText(item.getEstimateSubtitle());
        tvMessage.setText(item.getEstimateMessage());
        tvStatus.setTextColor(Color.parseColor("#FF9800"));
        
    } else if ("RANGE".equals(displayType)) {
        tvPosition.setText(item.getEstimateTitle());
        tvWaitTime.setText(item.getEstimateSubtitle());
        tvMessage.setText(item.getEstimateMessage());
        tvConfidence.setText("Độ tin cậy: " + item.getEstimateConfidence());
        tvStatus.setTextColor(Color.parseColor("#2196F3"));
    }
}
```

### Step 4: Add Auto-Refresh

```java
private Handler refreshHandler = new Handler();
private Runnable refreshRunnable = new Runnable() {
    @Override
    public void run() {
        loadQueueStatus();
        refreshHandler.postDelayed(this, 120000); // Refresh every 2 minutes
    }
};

@Override
protected void onResume() {
    super.onResume();
    loadQueueStatus();
    refreshHandler.postDelayed(refreshRunnable, 120000);
}

@Override
protected void onPause() {
    super.onPause();
    refreshHandler.removeCallbacks(refreshRunnable);
}
```

## Testing

### 1. Test Data Collection
```sql
-- Check if duration history is being recorded
SELECT 
    s.name as service_name,
    COUNT(*) as sample_count,
    AVG(actual_duration_minutes) as avg_actual,
    AVG(scheduled_duration_minutes) as avg_scheduled,
    STDDEV(actual_duration_minutes) as std_dev
FROM service_duration_history h
JOIN services s ON h.service_id = s.id
WHERE h.completed_at >= NOW() - INTERVAL '7 days'
GROUP BY s.id, s.name;
```

### 2. Test Estimation API
```bash
# Get estimate for queue
curl -H "Authorization: Bearer {token}" \
  http://localhost:8080/api/queue/estimate/123

# Get estimate by appointment
curl -H "Authorization: Bearer {token}" \
  http://localhost:8080/api/queue/estimate/appointment/456
```

### 3. Test Mobile Display
- Check position 0 (IN_PROGRESS) shows correct status
- Check position 1 shows soft countdown with "~"
- Check position 2+ shows range (X-Y minutes)
- Verify confidence badge displays
- Test refresh functionality

## Rollout Strategy

### Phase 1: Silent Data Collection (Week 1)
- Deploy duration tracking only
- Collect historical data
- Don't show estimates to users yet
- Monitor data quality

### Phase 2: Backend Testing (Week 2)
- Enable estimation API
- Test with internal users
- Verify accuracy
- Tune confidence thresholds

### Phase 3: Mobile Beta (Week 3)
- Deploy to beta testers
- Gather feedback
- Monitor user behavior
- Adjust messaging if needed

### Phase 4: Full Rollout (Week 4)
- Deploy to all users
- Monitor metrics
- Iterate based on feedback

## Troubleshooting

### Issue: No historical data
**Solution**: System needs 1-2 weeks of data collection. Use default estimates initially.

### Issue: Estimates too inaccurate
**Solution**: Check sample size. Adjust percentiles (use P90 instead of P75 for more conservative).

### Issue: Performance slow
**Solution**: Verify cache is working. Check database indexes.

### Issue: Mobile app crashes
**Solution**: Add null checks. Gracefully degrade to basic display if estimate fails.

## Configuration

Add to `application.properties`:

```properties
# Queue estimation settings
queue.estimation.history-days=30
queue.estimation.cache-ttl=300
queue.estimation.min-samples-for-high-confidence=100
queue.estimation.min-samples-for-medium-confidence=30
```

## Monitoring

Track these metrics:
- Historical data collection rate
- Estimation API response time
- Cache hit rate
- Estimate accuracy (compare to actual)
- User engagement (refresh rate)

---

**Ready to integrate!** Start with Step 1 (duration tracking) and test thoroughly before moving to next steps.
