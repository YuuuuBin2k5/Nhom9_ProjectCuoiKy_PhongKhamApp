# Queue Estimation System - Implementation Checklist

## ✅ Phase 1: Core Implementation (COMPLETED)

### Backend
- [x] Database migration V3 created
- [x] ServiceDurationHistory entity
- [x] ServiceDurationHistoryRepository
- [x] QueueStatisticsService (data-driven variance)
- [x] QueueEstimationService (estimation logic)
- [x] ServiceDurationTracker (duration recording)
- [x] QueueEstimationController (REST API)
- [x] DTOs (QueueEstimateDTO, ServiceVarianceStats)
- [x] Backend compiles successfully ✅

### Mobile
- [x] QueueItem model extended with estimate fields
- [x] ApiService endpoints added
- [x] Mobile compiles successfully ✅

### Documentation
- [x] Implementation summary
- [x] Integration guide
- [x] Executive summary
- [x] This checklist

## 🔄 Phase 2: Integration (TODO)

### Backend Integration
- [ ] Add `@Autowired ServiceDurationTracker` to CheckInQueueService
- [ ] Call `tracker.markStarted()` when status → IN_PROGRESS
- [ ] Call `tracker.markCompleted()` when status → COMPLETED
- [ ] Add `@Autowired QueueEstimationService` to CheckInQueueService
- [ ] Update `mapToQueueItemDto()` to include estimates
- [ ] Test duration tracking is working

### Database
- [ ] Run migration V3 on development database
- [ ] Verify tables created correctly
- [ ] Check indexes are in place

### Testing
- [ ] Test API endpoint: GET /api/queue/estimate/{queueId}
- [ ] Test API endpoint: GET /api/queue/estimate/appointment/{appointmentId}
- [ ] Verify estimates return correct format
- [ ] Test with empty queue
- [ ] Test with single patient
- [ ] Test with multiple patients
- [ ] Test edge cases (no historical data, etc.)

## 🔄 Phase 3: Mobile UI (TODO)

### UI Implementation
- [ ] Update PatientQueueActivity layout
- [ ] Add estimate display views (title, subtitle, message, confidence)
- [ ] Implement displayQueueEstimate() method
- [ ] Add auto-refresh (every 2 minutes)
- [ ] Add manual refresh button
- [ ] Handle different display types (IN_PROGRESS, SOFT_COUNTDOWN, RANGE)
- [ ] Add loading states
- [ ] Add error handling

### Testing
- [ ] Test position 0 (IN_PROGRESS) display
- [ ] Test position 1 (SOFT_COUNTDOWN) display
- [ ] Test position 2+ (RANGE) display
- [ ] Test auto-refresh
- [ ] Test manual refresh
- [ ] Test error scenarios
- [ ] Test with slow network
- [ ] Test offline behavior

## 🔄 Phase 4: Data Collection (Week 1)

### Deployment
- [ ] Deploy backend with duration tracking
- [ ] Verify duration history is being recorded
- [ ] Monitor database growth
- [ ] Check for errors in logs

### Monitoring
- [ ] Query historical data daily
- [ ] Check sample sizes per service
- [ ] Verify variance calculations
- [ ] Monitor performance

### SQL Queries for Monitoring
```sql
-- Check data collection
SELECT 
    s.name,
    COUNT(*) as samples,
    AVG(actual_duration_minutes) as avg_actual,
    STDDEV(actual_duration_minutes) as std_dev
FROM service_duration_history h
JOIN services s ON h.service_id = s.id
WHERE h.completed_at >= NOW() - INTERVAL '7 days'
GROUP BY s.id, s.name;

-- Check confidence levels
SELECT 
    s.name,
    COUNT(*) as samples,
    CASE 
        WHEN COUNT(*) >= 100 THEN 'HIGH'
        WHEN COUNT(*) >= 30 THEN 'MEDIUM'
        ELSE 'LOW'
    END as confidence
FROM service_duration_history h
JOIN services s ON h.service_id = s.id
WHERE h.completed_at >= NOW() - INTERVAL '30 days'
GROUP BY s.id, s.name;
```

## 🔄 Phase 5: Beta Testing (Week 2-3)

### Internal Testing
- [ ] Enable estimation API
- [ ] Test with internal users
- [ ] Gather feedback
- [ ] Monitor accuracy
- [ ] Tune confidence thresholds if needed

### Mobile Beta
- [ ] Deploy to beta testers
- [ ] Monitor usage
- [ ] Collect feedback
- [ ] Fix bugs
- [ ] Iterate on UI/UX

### Metrics to Track
- [ ] API response times
- [ ] Cache hit rates
- [ ] Estimate accuracy
- [ ] User engagement (refresh rate)
- [ ] Error rates

## 🔄 Phase 6: Full Rollout (Week 4)

### Production Deployment
- [ ] Deploy backend to production
- [ ] Run migration on production database
- [ ] Deploy mobile app to production
- [ ] Monitor closely for first 24 hours

### Post-Deployment
- [ ] Monitor error logs
- [ ] Track user feedback
- [ ] Monitor performance metrics
- [ ] Adjust thresholds if needed

### Success Criteria
- [ ] Estimate accuracy ±10 min for position ≤ 3
- [ ] Estimate accuracy ±20 min for position > 3
- [ ] 80%+ user satisfaction
- [ ] 50%+ reduction in "where is my turn?" inquiries
- [ ] No performance degradation
- [ ] No critical bugs

## 🔄 Phase 7: Optimization (Ongoing)

### Performance
- [ ] Monitor cache effectiveness
- [ ] Optimize slow queries if any
- [ ] Consider database partitioning for history table
- [ ] Archive old data (>90 days)

### Features
- [ ] Add per-doctor statistics (optional)
- [ ] Add push notifications for "you're next" (optional)
- [ ] Add ML-based prediction (future)
- [ ] Add admin dashboard for monitoring

### Maintenance
- [ ] Set up automated data cleanup
- [ ] Document operational procedures
- [ ] Train support staff
- [ ] Create troubleshooting guide

## Quick Reference

### Key Files
```
Backend:
- clinic_backend/src/main/resources/db/migration/V3__add_service_duration_history.sql
- clinic_backend/src/main/java/com/hcmute/clinic/entity/ServiceDurationHistory.java
- clinic_backend/src/main/java/com/hcmute/clinic/service/QueueStatisticsService.java
- clinic_backend/src/main/java/com/hcmute/clinic/service/QueueEstimationService.java
- clinic_backend/src/main/java/com/hcmute/clinic/service/ServiceDurationTracker.java
- clinic_backend/src/main/java/com/hcmute/clinic/controller/QueueEstimationController.java

Mobile:
- mobile_android/app/src/main/java/com/hcmute/mobile_android/network/models/QueueItem.java
- mobile_android/app/src/main/java/com/hcmute/mobile_android/network/ApiService.java
- mobile_android/app/src/main/java/com/hcmute/mobile_android/ui/activities/PatientQueueActivity.java

Documentation:
- QUEUE_ESTIMATION_SUMMARY.md
- QUEUE_ESTIMATION_IMPLEMENTATION.md
- QUEUE_ESTIMATION_INTEGRATION_GUIDE.md
- QUEUE_ESTIMATION_CHECKLIST.md (this file)
```

### API Endpoints
```
GET /api/queue/estimate/{queueId}
GET /api/queue/estimate/appointment/{appointmentId}
```

### Integration Points
```java
// In CheckInQueueService.java
@Autowired private ServiceDurationTracker durationTracker;
@Autowired private QueueEstimationService estimationService;

// When starting patient
durationTracker.markStarted(queue);

// When completing patient
durationTracker.markCompleted(queue);

// When mapping to DTO
QueueEstimateDTO estimate = estimationService.calculateEstimate(queue);
```

## Status Summary

- ✅ **Core Implementation**: COMPLETE
- 🔄 **Integration**: READY (needs 2-3 hours)
- 🔄 **Mobile UI**: READY (needs 2-3 hours)
- 🔄 **Testing**: PENDING
- 🔄 **Deployment**: PENDING

## Next Immediate Steps

1. **Run migration** on development database
2. **Integrate duration tracking** (2 lines of code)
3. **Test API endpoints** manually
4. **Implement mobile UI** (50 lines of code)
5. **Test end-to-end**
6. **Deploy and collect data**

---

**Current Status**: Core implementation complete and compiles successfully. Ready for integration and testing.

**Estimated Time to Production**: 2-4 weeks (including data collection and beta testing)

**Risk Level**: LOW (clean architecture, graceful degradation, well-tested approach)
