# Queue Estimation System - Implementation Summary

## Overview
Production-grade queue estimation system with data-driven variance calculation and honest UX.

## Architecture

### Layer 1: Data Layer
- **ServiceDurationHistory** entity: Tracks actual vs scheduled service durations
- **Migration V3**: Adds history table + timestamps to check_in_queue
- **Repository**: Query historical data for statistics

### Layer 2: Core Logic
- **QueueStatisticsService**: Calculates variance from historical data
  - Uses 30-day rolling window
  - Calculates mean, stdDev, percentiles (P50, P75, P90)
  - Confidence levels based on sample size
  - Cached for performance

- **QueueEstimationService**: Generates queue estimates
  - Position 0: IN_PROGRESS display
  - Position 1: Soft countdown (~X minutes)
  - Position 2+: Range estimation (X-Y minutes)
  - Uses P75/P90 for conservative estimates

- **ServiceDurationTracker**: Records actual durations
  - Marks started_at when IN_PROGRESS
  - Marks completed_at when done
  - Saves to history for future calculations

### Layer 3: API & Presentation
- **QueueEstimationController**: REST endpoints
  - GET /api/queue/estimate/{queueId}
  - GET /api/queue/estimate/appointment/{appointmentId}

- **QueueEstimateDTO**: Response format
  - displayType: IN_PROGRESS | SOFT_COUNTDOWN | RANGE
  - position, estimatedMinutes, minMinutes, maxMinutes
  - message, confidence, title, subtitle
  - showApproximateLabel (always true for countdown)

### Layer 4: Mobile Integration
- **QueueItem** model extended with estimate fields
- **ApiService** endpoints added
- Ready for UI integration

## Key Design Decisions

### 1. Data-Driven Variance
✅ Uses historical data (not hardcoded)
✅ Percentiles (P50, P75, P90) for realistic estimates
✅ Confidence levels based on sample size

### 2. Honest UX
✅ No hard countdown (always shows "~")
✅ Range estimates for positions 2+
✅ Conservative estimates (P75/P90)
✅ Clear messaging about uncertainty

### 3. Edge Case Handling
✅ Patient joins late: Range adjusts naturally
✅ Patient cancels: Recalculate on next request
✅ Patient skips: Position recalculated
✅ Doctor time varies: Historical data captures this
✅ Multiple doctors: Per-room queue (can extend to per-doctor)

### 4. Layered Architecture
✅ Core logic separate from presentation
✅ Easy to change UI without touching backend
✅ Statistics service is reusable
✅ Clean separation of concerns

### 5. Event Consistency
✅ Server is authoritative (stateless client)
✅ Client polls or uses SSE for updates
✅ No client-side countdown ticking

## Implementation Status

### ✅ Completed
- [x] Database migration
- [x] Entity classes
- [x] Repositories
- [x] Statistics service (data-driven)
- [x] Estimation engine
- [x] Duration tracker
- [x] API controller
- [x] DTOs
- [x] Mobile model updates
- [x] API service endpoints

### 🔄 Next Steps (Integration)
1. **Integrate duration tracking into CheckInQueueService**
   - Call `tracker.markStarted()` when status → IN_PROGRESS
   - Call `tracker.markCompleted()` when status → COMPLETED

2. **Update existing queue endpoints to include estimates**
   - Modify QueueItemDto mapper to call estimationService
   - Include estimate in queue list responses

3. **Mobile UI implementation**
   - Update PatientQueueActivity to display estimates
   - Show different UI based on displayType
   - Add refresh button / auto-refresh

4. **Testing & Data Collection**
   - Run system to collect historical data
   - Monitor variance calculations
   - Adjust confidence thresholds if needed

5. **Optional Enhancements**
   - SSE for real-time updates
   - Push notifications when "you're next"
   - Per-doctor statistics
   - ML-based prediction (future)

## API Usage Examples

### Get Estimate by Queue ID
```http
GET /api/queue/estimate/123
Authorization: Bearer {token}

Response:
{
  "displayType": "RANGE",
  "position": 5,
  "minMinutes": 30,
  "maxMinutes": 45,
  "estimatedMinutes": 37,
  "message": "Vui lòng chờ trong khu vực",
  "confidence": "MEDIUM",
  "showApproximateLabel": true,
  "title": "Vị trí: #5",
  "subtitle": "30-45 phút",
  "lastUpdated": "2026-03-31T10:30:00"
}
```

### Get Estimate by Appointment ID
```http
GET /api/queue/estimate/appointment/456
Authorization: Bearer {token}

Response: (same format as above)
```

## Mobile Integration Example

```java
// In PatientQueueActivity
apiService.getQueueEstimateByAppointment(appointmentId)
    .enqueue(new Callback<QueueItem>() {
        @Override
        public void onResponse(Call<QueueItem> call, Response<QueueItem> response) {
            if (response.isSuccessful() && response.body() != null) {
                QueueItem estimate = response.body();
                updateUI(estimate);
            }
        }
    });

private void updateUI(QueueItem estimate) {
    switch (estimate.getEstimateDisplayType()) {
        case "IN_PROGRESS":
            tvTitle.setText(estimate.getEstimateTitle());
            tvSubtitle.setText(estimate.getEstimateSubtitle());
            break;
            
        case "SOFT_COUNTDOWN":
            tvTitle.setText(estimate.getEstimateTitle());
            tvSubtitle.setText(estimate.getEstimateSubtitle()); // "~5 phút"
            tvMessage.setText(estimate.getEstimateMessage());
            break;
            
        case "RANGE":
            tvTitle.setText(estimate.getEstimateTitle()); // "Vị trí: #5"
            tvSubtitle.setText(estimate.getEstimateSubtitle()); // "30-45 phút"
            tvMessage.setText(estimate.getEstimateMessage());
            tvConfidence.setText(estimate.getEstimateConfidence());
            break;
    }
}
```

## Performance Considerations

- **Caching**: ServiceVarianceStats cached per service
- **Query Optimization**: Indexed queries on service_id, completed_at
- **Lazy Calculation**: Estimates calculated on-demand (not pre-computed)
- **Historical Window**: 30 days (configurable)

## Maintenance Notes

- **Cache Invalidation**: Clear cache when new duration data added (or use TTL)
- **Data Cleanup**: Consider archiving old history (>90 days)
- **Monitoring**: Track confidence levels, adjust thresholds if needed
- **Feedback Loop**: Monitor actual vs estimated times, tune percentiles

## Testing Checklist

- [ ] Database migration runs successfully
- [ ] Historical data is recorded correctly
- [ ] Statistics calculation works with various sample sizes
- [ ] Estimation handles all edge cases (empty queue, single patient, etc.)
- [ ] API endpoints return correct format
- [ ] Mobile app displays estimates correctly
- [ ] Performance is acceptable under load

## Success Metrics

- Estimate accuracy within ±10 minutes for position ≤ 3
- Estimate accuracy within ±20 minutes for position > 3
- User satisfaction with wait time transparency
- Reduced "where is my turn?" inquiries

---

**Status**: Core implementation complete, ready for integration and testing.
**Next**: Integrate duration tracking into existing queue workflow.
