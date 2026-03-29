# FIX 7: PRIORITY INDICATOR TRONG QUEUE - HOÀN THÀNH

## TỔNG QUAN
Đã implement priority indicator, wait time display, và color coding cho queue items theo UC18.

---

## ✅ FEATURES ĐÃ THÊM

### 1. Priority Badge (⭐ Icon)
**Location**: `item_queue.xml`
**Hiển thị**: Khi `priority > 5`
**Visual**: Star icon màu vàng ở góc trên phải của queue number

```xml
<ImageView
    android:id="@+id/ivPriorityBadge"
    android:layout_width="20dp"
    android:layout_height="20dp"
    android:src="@drawable/ic_star"
    android:layout_gravity="top|end"
    android:visibility="gone"
    app:tint="@color/warning_amber" />
```

**Logic trong QueueAdapter**:
```java
// Show priority badge for priority > 5
View ivPriorityBadge = itemView.findViewById(R.id.ivPriorityBadge);
if (item.getPriority() != null && item.getPriority() > 5) {
    ivPriorityBadge.setVisibility(View.VISIBLE);
} else {
    ivPriorityBadge.setVisibility(View.GONE);
}
```

**Priority Levels**:
- Priority 0: Bệnh nhân bình thường (không có badge)
- Priority 5: Bệnh nhân chuyển phòng (không có badge)
- Priority 10+: Bệnh nhân return từ X-quang (có ⭐ badge)

---

### 2. Estimated Wait Time
**Location**: `item_queue.xml`
**Hiển thị**: Cho status WAITING và RETURNED_PRIORITY
**Format**: "~15 phút"

```xml
<TextView
    android:id="@+id/tvWaitTime"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="~15 phút"
    android:textSize="14sp"
    android:textColor="@color/warning_amber"
    android:textStyle="bold"
    android:layout_marginTop="4dp"
    android:drawableStart="@drawable/ic_timer"
    android:drawablePadding="4dp"
    android:visibility="gone"
    app:drawableTint="@color/warning_amber" />
```

**Logic**:
```java
// Show wait time (placeholder - should come from backend)
TextView tvWaitTime = itemView.findViewById(R.id.tvWaitTime);
if ("WAITING".equals(item.getStatus()) || "RETURNED_PRIORITY".equals(item.getStatus())) {
    // Calculate estimated wait time based on position
    int estimatedMinutes = getAdapterPosition() * 15; // 15 min per patient
    tvWaitTime.setText(String.format("~%d phút", estimatedMinutes));
    tvWaitTime.setVisibility(View.VISIBLE);
} else {
    tvWaitTime.setVisibility(View.GONE);
}
```

**Note**: Hiện tại dùng formula đơn giản (position * 15 phút). Nên improve bằng cách:
- Backend tính toán dựa trên service duration
- Tính toán real-time dựa trên queue length và average service time

---

### 3. Color Coding by Status
**Colors Added**: `colors.xml`

```xml
<!-- Queue Status Background Colors -->
<color name="status_waiting_bg">#E8F5E9</color> <!-- Light green -->
<color name="status_in_progress_bg">#E3F2FD</color> <!-- Light blue -->
<color name="status_priority_bg">#FFF3E0</color> <!-- Light orange -->
<color name="status_paused_bg">#F3E5F5</color> <!-- Light purple -->
```

**Logic trong QueueAdapter**:
```java
// Color coding based on status
int backgroundColor;
switch (item.getStatus()) {
    case "WAITING":
        backgroundColor = itemView.getContext().getColor(R.color.status_waiting_bg);
        break;
    case "IN_PROGRESS":
        backgroundColor = itemView.getContext().getColor(R.color.status_in_progress_bg);
        break;
    case "RETURNED_PRIORITY":
        backgroundColor = itemView.getContext().getColor(R.color.status_priority_bg);
        break;
    case "PAUSED_FOR_TEST":
        backgroundColor = itemView.getContext().getColor(R.color.status_paused_bg);
        break;
    default:
        backgroundColor = itemView.getContext().getColor(android.R.color.white);
}
cardQueue.setCardBackgroundColor(backgroundColor);
```

**Visual Guide**:
- 🟢 Light Green: WAITING (đang chờ bình thường)
- 🔵 Light Blue: IN_PROGRESS (đang khám)
- 🟠 Light Orange: RETURNED_PRIORITY (ưu tiên - return từ X-quang)
- 🟣 Light Purple: PAUSED_FOR_TEST (đang chụp X-quang)

---

### 4. New Drawables
**Created**:
1. `ic_star.xml` - Star icon cho priority badge
2. `ic_timer.xml` - Timer icon cho wait time

---

## FILES MODIFIED

### Mobile Android
1. `mobile_android/app/src/main/res/layout/item_queue.xml`
   - Added priority badge (ImageView)
   - Added wait time (TextView)
   - Wrapped queue number in FrameLayout

2. `mobile_android/app/src/main/java/com/hcmute/mobile_android/adapters/QueueAdapter.java`
   - Added priority badge visibility logic
   - Added wait time calculation and display
   - Added color coding based on status

3. `mobile_android/app/src/main/res/values/colors.xml`
   - Added 4 status background colors

4. `mobile_android/app/src/main/res/drawable/ic_star.xml` - NEW
5. `mobile_android/app/src/main/res/drawable/ic_timer.xml` - NEW

---

## COMPILATION STATUS
✅ Mobile app compiled successfully
```
BUILD SUCCESSFUL in 31s
35 actionable tasks: 12 executed, 23 up-to-date
```
✅ APK copied to root: `app-debug.apk`

---

## TESTING SCENARIOS

### Scenario 1: Normal Patient (Priority 0)
**Setup**: Patient checks in normally
**Expected**:
- ❌ No star badge
- ✅ Light green background (WAITING)
- ✅ Wait time: "~15 phút" (if position 1)

### Scenario 2: Room Transfer Patient (Priority 5)
**Setup**: Patient transferred from Room 01 to X-ray
**Expected**:
- ❌ No star badge (priority = 5, not > 5)
- ✅ Light green background (WAITING)
- ✅ Wait time displayed

### Scenario 3: X-ray Return Patient (Priority 10+)
**Setup**: Patient completes X-ray and returns
**Expected**:
- ✅ Star badge visible (⭐)
- ✅ Light orange background (RETURNED_PRIORITY)
- ✅ Wait time: "~0 phút" (should be first in queue)

### Scenario 4: In Progress Patient
**Setup**: Doctor calls patient into room
**Expected**:
- ❌ No star badge (unless had priority before)
- ✅ Light blue background (IN_PROGRESS)
- ❌ No wait time (already being served)

### Scenario 5: Paused for Test
**Setup**: Patient sent to X-ray
**Expected**:
- ❌ No star badge
- ✅ Light purple background (PAUSED_FOR_TEST)
- ❌ No wait time

---

## VISUAL COMPARISON

### Before
```
┌─────────────────────────────┐
│ 01  Nguyễn Văn A            │
│     0901234567              │
│     Khám tổng quát          │
│     [Đang chờ]              │
└─────────────────────────────┘
```

### After
```
┌─────────────────────────────┐
│ 01⭐ Nguyễn Văn A           │ ← Orange background
│     0901234567              │
│     Khám tổng quát          │
│     [Ưu tiên]               │
│     ⏱ ~0 phút               │ ← Wait time
└─────────────────────────────┘
```

---

## IMPROVEMENTS NEEDED (Future)

### Backend API Enhancement
**Current**: Wait time calculated on client (position * 15)
**Should**: Backend calculates based on:
```java
// In CheckInQueueService.java
private int calculateEstimatedWaitTime(CheckInQueue current) {
    // Already implemented! Just need to expose via API
    // Returns accurate wait time based on:
    // - Service duration of patients ahead
    // - Current in-progress patient remaining time
}
```

**Recommendation**: Add `estimatedWaitTime` field to `QueueItemDto`

### Real-time Updates
**Current**: Wait time updates only on refresh
**Should**: Update via SSE when queue changes

---

## NEXT STEPS

### Completed (FIX 7)
- ✅ Priority badge (⭐ icon)
- ✅ Wait time display
- ✅ Color coding by status
- ✅ New drawables (star, timer)

### Remaining HIGH PRIORITY
- ❌ FIX 5: QR Scanner cho Doctor
- ❌ FIX 6: Patient History button
- ❌ FIX 8: Room transfer notification improvement
- ❌ FIX 9: Payment confirmation workflow

---

## SUMMARY
✅ Priority indicator hoàn thành
✅ Visual improvements cho queue management
✅ Dễ dàng nhận biết priority patients
✅ Estimated wait time giúp bệnh nhân biết thời gian chờ
✅ Color coding giúp staff nhanh chóng identify status

**Ready for testing!**
