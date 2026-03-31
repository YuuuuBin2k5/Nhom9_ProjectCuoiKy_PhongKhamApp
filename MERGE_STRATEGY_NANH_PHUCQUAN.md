# Merge Strategy: nanh ← phucquan

## Current Situation

You are on branch `nanh` (HIGH PRIORITY - correct business logic) and want to merge changes from `phucquan` (UI improvements and architectural refactoring).

## Problem

There are uncommitted changes on `nanh` from recent critical fixes:
- Task 6: Check-in queue completion fix
- Medical history real data implementation  
- N+1 query optimization
- Multiple bag fetch exception fix
- Patient detail screen implementation

## Changes in phucquan Branch

### Architectural Improvements (GOOD - Should Merge)
1. **Centralized Room Assignment Service**
   - New file: `ServiceRoomAssignmentService.java`
   - Refactors room assignment logic from `TreatmentPlanService`
   - Better separation of concerns

2. **Removed Draft Mode**
   - Plans are now auto-activated (`isDraft = false`)
   - `activatePlan()` method deprecated (kept for backward compatibility)

3. **Logging Improvements**
   - Added `@Slf4j` annotation
   - Better logging throughout

4. **UI Improvements**
   - Multiple layout and fragment updates
   - Better user experience

### Potential Conflicts with nanh Business Logic

The main conflict is in `TreatmentPlanService.java`:
- **nanh** has critical fixes for:
  - Step completion workflow
  - Queue management integration
  - Invoice validation before editing
  - Parallel workflow support
  - Auto-complete plan removal

- **phucquan** has:
  - Room assignment refactoring
  - Draft mode removal
  - Method deprecations

## Recommended Merge Strategy

### Step 1: Commit Current Changes on nanh
```bash
git add .
git commit -m "Task 6: Check-in queue completion fix and related improvements"
```

### Step 2: Merge phucquan with Manual Resolution
```bash
git merge phucquan --no-ff
```

### Step 3: Resolve Conflicts Intelligently

For `TreatmentPlanService.java`:

#### KEEP from nanh (Business Logic - HIGH PRIORITY):
- All step completion logic in `completeStepAndAdvance()`
- Queue management integration
- Invoice validation in `cancelStep()`
- Parallel workflow support in `updateSteps()`
- Auto-complete plan removal logic
- All validation checks

#### ADOPT from phucquan (Architecture):
- Add `@Slf4j` annotation
- Add `ServiceRoomAssignmentService` dependency injection
- Change `isDraft(true)` to `isDraft(false)` in `createFromTemplate()`
- Deprecate `activatePlan()` but keep nanh's implementation
- Refactor `findRoomForService()` to delegate to `ServiceRoomAssignmentService`

#### Merged Code Pattern:
```java
@Service
@RequiredArgsConstructor
@Slf4j  // FROM phucquan
public class TreatmentPlanService {
    
    // ... existing dependencies from nanh ...
    private final ServiceRoomAssignmentService roomAssignmentService;  // FROM phucquan
    
    @Transactional
    public TreatmentPlan createFromTemplate(...) {
        // ... nanh logic ...
        TreatmentPlan plan = TreatmentPlan.builder()
                // ... other fields ...
                .isDraft(false)  // FROM phucquan (was true in nanh)
                .build();
        // ... rest of nanh logic ...
    }
    
    /**
     * DEPRECATED: Plans are now auto-activated on creation
     * @deprecated Use auto-activation instead
     */
    @Deprecated  // FROM phucquan
    @Transactional
    public void activatePlan(Long planId) {
        // KEEP nanh implementation for backward compatibility
        // ... full nanh implementation ...
    }
    
    private ClinicRoom findRoomForService(Service service) {
        // REFACTOR to use centralized service (phucquan pattern)
        return roomAssignmentService.determineRoomForService(service);
    }
    
    // ALL OTHER METHODS: Keep nanh implementation completely
}
```

### Step 4: Test After Merge
1. Build backend: `./gradlew build`
2. Run tests
3. Test critical workflows:
   - Treatment plan creation
   - Step completion
   - Room assignment
   - Queue management
   - Invoice creation

## Files Requiring Manual Merge

### High Priority (Business Logic Conflicts)
1. `clinic_backend/src/main/java/com/hcmute/clinic/service/TreatmentPlanService.java`
   - **Strategy**: Keep ALL nanh business logic, adopt phucquan architecture improvements

### Medium Priority (UI/Structure)
2. `mobile_android/app/src/main/java/com/hcmute/mobile_android/network/ApiService.java`
   - **Strategy**: Merge both changes if compatible

3. `mobile_android/app/src/main/res/layout/activity_doctor_workflow.xml`
   - **Strategy**: Keep nanh if it has recent fixes, otherwise take phucquan

### Low Priority (New Files from phucquan)
4. `clinic_backend/src/main/java/com/hcmute/clinic/service/ServiceRoomAssignmentService.java`
   - **Strategy**: Accept as-is (new file, no conflict)

5. Multiple layout and fragment files
   - **Strategy**: Accept phucquan changes (UI improvements)

## Summary

The merge should:
1. ✅ Preserve ALL business logic from nanh (HIGH PRIORITY)
2. ✅ Adopt architectural improvements from phucquan (centralized services, logging)
3. ✅ Keep UI improvements from phucquan
4. ✅ Maintain backward compatibility (deprecated methods kept)
5. ❌ Never lose critical fixes from nanh (queue management, invoice validation, etc.)

## Next Steps

1. Commit your current changes on nanh
2. Run: `git merge phucquan --no-ff`
3. Resolve conflicts following the strategy above
4. Test thoroughly
5. Commit the merge

Would you like me to help with the actual merge resolution?
