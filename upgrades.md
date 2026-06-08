# VitaAI UI/UX Upgrades Plan

**Date:** June 8, 2026  
**Project:** VitaAI Android App  
**Focus Areas:** Chat Section, Circadian Alignment, Chart Components

---

## Executive Summary

This upgrades plan identifies **25+ specific UI/UX improvements** across three key areas: Chat Section, Circadian Alignment, and Chart Components. The improvements address layout stability, visual hierarchy, readability, and user interaction issues discovered through codebase analysis.

### Priority Breakdown
- **High Priority:** Layout fixes (chat bar), accessibility (chart labels), critical visual bugs  
- **Medium Priority:** Visual polish, consistency improvements, enhanced interactions  
- **Low Priority:** Nice-to-have animations, advanced features

---

## 1. Chat Section UI/UX Improvements

### 1.1 Critical Layout Fix: Chat Bar Keyboard Issue

**Problem:** The chat input bar jumps upward when clicked due to `imePadding()` modifier without proper anchoring.

**Current Implementation:**  
- File: `ChatScreen.kt` lines 413-418  
- Issue: `imePadding()` combined with `navigationBarsPadding()` causes unexpected layout shifts

**Solution:**
```kotlin
// Replace current ChatInput Row modifier:
Row(
    modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 12.dp)
        .navigationBarsPadding()
        .imePadding(),  // ❌ PROBLEMATIC
    // ...
)

// WITH FIXED VERSION:
Row(
    modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp)
        .navigationBarsPadding()
        .then(
            if (LocalWindowInsets.current.ime.isVisible) {
                Modifier.imePadding()
            } else {
                Modifier
            }
        ),
    // ...
)
```

**Alternative Solution (Simpler):**
```kotlin
// Use WindowInsets.ime for more predictable behavior
Row(
    modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp)
        .windowInsetsPadding(WindowInsets(16.dp, 0.dp, 16.dp, 0.dp))
        .then(
            Modifier.windowInsetsPadding(
                WindowInsets.ime.union(WindowInsets.navigationBars)
            )
        ),
    // ...
)
```

### 1.2 Chat Bubble Visual Enhancements

**Improvements:**
1. **Add Timestamp to Messages** - Display time above each message bubble
2. **Message Status Indicators** - Add read receipts (✓✓) for user messages
3. **Improved Typing Indicator** - Make the blob animation more subtle and professional
4. **Message Grouping** - Group consecutive messages from same sender
5. **Swipe Actions** - Add swipe-to-delete or swipe-to-reply for messages

**Implementation Details:**
```kotlin
// Enhanced ChatBubble with timestamp
@Composable
private fun ChatBubble(message: Message) {
    // ... existing code ...
    
    // Add timestamp row
    Text(
        text = formatMessageTime(message.timestamp),
        style = MaterialTheme.typography.labelSmall.copy(
            fontSize = 10.sp,
            color = Color.Black.copy(alpha = 0.3f)
        ),
        modifier = Modifier.padding(top = 4.dp)
    )
}
```

### 1.3 Chat Input Field Improvements

**Enhancements:**
1. **Multi-line Support** - Allow longer messages with auto-expanding input
2. **Character Counter** - Show character count for long messages
3. **Quick Actions** - Add attachment button, voice memo button
4. **Suggestion Chips** - Show quick reply suggestions based on context
5. **Better Placeholder** - More engaging placeholder text that rotates

**Implementation:**
```kotlin
// Multi-line input support
BasicTextField(
    value = text,
    onValueChange = onTextChange,
    modifier = Modifier
        .weight(1f)
        .heightIn(min = 52.dp, max = 120.dp) // Allow expansion
        .padding(horizontal = 12.dp, vertical = 12.dp),
    // ...
    singleLine = false, // Changed from true
    maxLines = 4
)
```

### 1.4 Chat Screen Navigation & Layout

**Improvements:**
1. **Pull to Refresh** - Add pull-to-refresh for message sync
2. **Empty State** - Show friendly empty state when no messages exist
3. **Error State** - Better error handling with retry option
4. **Scroll to Bottom Button** - Floating action button to scroll to latest message
5. **Message Search** - Add search functionality within chat

---

## 2. Circadian Alignment UI Revamp

### 2.1 Biological Phase Clock Improvements

**Current Issues:**
- Clock labels have poor contrast in dark theme (hardcoded colors)
- Drag interaction could be smoother
- Sleep arc visual could be more informative

**Enhancements:**

#### 2.1.1 Fix Label Contrast Issue
```kotlin
// File: CircadianClockDial.kt line 94-102
// CURRENT (problematic):
val labelPaint = remember(onSurface, density) {
    androidx.compose.ui.graphics.Paint().asFrameworkPaint().apply {
        isAntiAlias = true
        color = onSurface.toArgb() // Doesn't adapt well to all themes
        // ...
    }
}

// IMPROVED:
val labelPaint = remember(density) {
    androidx.compose.ui.graphics.Paint().asFrameworkPaint().apply {
        isAntiAlias = true
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f).toArgb()
        textSize = with(density) { 12.sp.toPx() }
        textAlign = android.graphics.Paint.Align.CENTER
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }
}
```

#### 2.1.2 Enhanced Clock Visuals
1. **Current Time Indicator** - Add pulsing dot for current time
2. **Sleep Quality Arc** - Color-code sleep arc based on quality
3. **Optimal Window Highlight** - Show ideal sleep window in green
4. **Time Zone Support** - Add visual indicator for time zone
5. **Touch Feedback** - Add haptic feedback when adjusting times

### 2.2 Circadian Vitals Bento Grid Enhancements

**Current Implementation:** Basic 2x2 grid with Sleep Regularity, Sleep Debt, Social Jetlag, Circadian Disruption

**Improvements:**

#### 2.2.1 Enhanced Card Designs
```kotlin
// Add trend indicators to each card
BentoStatCard(
    title = "Sleep Regularity",
    value = "${uiState.sleepRegularityIndex}%",
    statusText = getSRIStatus(uiState.sleepRegularityIndex),
    trend = getTrend(uiState.sleepRegularityHistory), // NEW
    icon = Icons.Default.Bedtime,
    color = Color(0xFF3F51B5),
    modifier = Modifier.fillMaxHeight()
)
```

#### 2.2.2 Additional Vitals to Add
1. **Sleep Efficiency** - Percentage of time asleep vs in bed
2. **Deep Sleep Percentage** - Amount of restorative sleep
3. **REM Sleep Percentage** - Dream sleep quality indicator
4. **Sleep Latency** - Time taken to fall asleep
5. **Wake After Sleep Onset** - Time spent awake during night

#### 2.2.3 Interactive Elements
1. **Tap to Expand** - Each card expands to show detailed history
2. **Sparkline Charts** - Mini charts showing 7-day trend
3. **Color Coding** - Dynamic colors based on status (green=good, red=poor)
4. **Goal Progress** - Show progress toward sleep goals

### 2.3 Sleep Tracking Section Redesign

**Current Issues:**
- Basic toggle switch without context
- No visual feedback during tracking
- Missing sleep session history

**Improvements:**

#### 2.3.1 Enhanced Tracking Interface
```kotlin
// Replace basic switch with interactive tracking card
GlassCardGlow(
    modifier = Modifier.fillMaxWidth(),
    glowColor = if (uiState.isTrackingSleep) Primary else Color.Transparent
) {
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Animated sleep tracking indicator
        if (uiState.isTrackingSleep) {
            SleepingAnimation(
                duration = uiState.currentSessionDuration
            )
        }
        
        // Improved toggle button
        GlowPrimaryButton(
            text = if (uiState.isTrackingSleep) "Wake Up" else "Start Sleep",
            onClick = { viewModel.toggleSleepTracking(context) },
            glowColor = if (uiState.isTrackingSleep) Tertiary else Primary
        )
    }
}
```

#### 2.3.2 Sleep Session Preview
1. **Tonight's Plan** - Show scheduled bedtime with countdown
2. **Recent Sessions** - Show last 3 sleep sessions
3. **Sleep Score Preview** - Estimated sleep quality score
4. **Environment Factors** - Room temperature, noise level indicators

### 2.4 Light Exposure Timeline Enhancements

**Current:** Basic `LuminousLuxTimeline` component

**Improvements:**
1. **Interactive Timeline** - Drag to adjust light exposure goals
2. **Color Coding** - Blue light vs warm light indication
3. **Optimal Windows** - Highlight ideal light exposure times
4. **Sun Integration** - Show sunrise/sunset times
5. **Indoor vs Outdoor** - Differentiate light sources

### 2.5 Chronotype Integration

**Add New Section:**
```kotlin
// Chronotype profile card
ApexCard(
    title = "Your Chronotype"
) {
    ChronotypeProfile(
        chronotype = uiState.chronotype,
        peakProductivityHours = uiState.peakHours,
        recommendedSchedule = uiState.recommendedSchedule
    )
}
```

---

## 3. Chart Component Improvements

### 3.1 Axis Styling & Readability

**Current Issues:**
- X and Y axis labels are too cramped
- Padding calculations are too tight
- Labels lack visual hierarchy
- Grid lines can be confusing

**Solutions:**

#### 3.1.1 Improved Axis Padding
```kotlin
// File: LuminousChart.kt - Improve padding calculations
// CURRENT (lines 76-80):
val bottomPaddingPx = if (xAxisLabels.isNotEmpty()) {
    with(density) { maxOf(20.dp.toPx(), labelTextSizePx * 2.2f) }
} else {
    with(density) { 8.dp.toPx() }
}

// IMPROVED:
val bottomPaddingPx = if (xAxisLabels.isNotEmpty()) {
    with(density) { maxOf(32.dp.toPx(), labelTextSizePx * 3.5f) } // Increased padding
} else {
    with(density) { 12.dp.toPx() }
}

// Also improve left padding for Y-axis
val leftPaddingPx = remember(yLabels, showAxes, labelTextSizePx, axisTickLengthPx, labelPaddingPx, dp8Px, dp28Px) {
    val paint = Paint().apply {
        textSize = labelTextSizePx
    }
    val maxLabelWidth = if (yLabels.isNotEmpty()) yLabels.maxOf { paint.measureText(it) } else 0f
    if (showAxes || yLabels.isNotEmpty()) {
        maxOf(40.dp.toPx(), maxLabelWidth + labelPaddingPx * 1.5f + axisTickLengthPx) // Increased
    } else {
        dp8Px
    }
}
```

#### 3.1.2 Better Label Spacing & Visibility
```kotlin
// Add smart label skipping for crowded X-axis
val xLabelSkipStep = remember(xAxisLabels.size, chartWidth) {
    val estimatedLabelWidth = labelTextSizePx * 4f // Approximate
    val maxLabels = (chartWidth / estimatedLabelWidth).toInt().coerceAtLeast(2)
    (xAxisLabels.size / maxLabels).coerceAtLeast(1)
}

// Only draw every n-th label
xAxisLabels.forEachIndexed { index, label ->
    if (index % xLabelSkipStep == 0) {
        // Draw label
    }
}
```

#### 3.1.3 Axis Line Styling
```kotlin
// Make axis lines more subtle and professional
if (showAxes) {
    // X-axis
    drawLine(
        color = axisColor.copy(alpha = 0.3f), // Reduced alpha
        start = Offset(chartLeft, chartBottom),
        end = Offset(chartRight, chartBottom),
        strokeWidth = 1.5f // Slightly thicker for visibility
    )
    
    // Y-axis (optional - can be disabled for cleaner look)
    if (showYAxis) {
        drawLine(
            color = axisColor.copy(alpha = 0.3f),
            start = Offset(chartLeft, chartTop),
            end = Offset(chartLeft, chartBottom),
            strokeWidth = 1.5f
        )
    }
}
```

### 3.2 Bar Chart Shape Fix

**Current Issues:**
- Bar corners may be too rounded or not rounded enough
- Bar width calculations may produce uneven spacing
- Missing bar hover effects

**Solutions:**

#### 3.2.1 Improved Bar Shape
```kotlin
// In LuminousBarChart component
// Add configurable corner radius
@Composable
fun LuminousBarChart(
    // ... existing params
    barCornerRadius: Dp = 6.dp, // NEW: Configurable corners
    barWidthRatio: Float = 0.6f, // NEW: Control bar width relative to spacing
    // ...
)

// Drawing bars with improved shape
val barWidth = (chartWidth / dataPoints.size) * barWidthRatio
val cornerRadiusPx = with(density) { barCornerRadius.toPx() }

dataPoints.forEachIndexed { index, value ->
    val x = chartLeft + (index * (chartWidth / dataPoints.size)) + 
            ((chartWidth / dataPoints.size) - barWidth) / 2
    val barHeight = ((value - axisMin) / range) * chartHeight
    val y = chartBottom - barHeight
    
    // Draw rounded rect
    drawRoundRect(
        color = barColor,
        topLeft = Offset(x, y),
        size = Size(barWidth, barHeight),
        cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
        style = Fill
    )
}
```

#### 3.2.2 Bar Hover Effects
```kotlin
// Add interactive bar highlighting
val hoveredIndex by remember { mutableStateOf<Int?>(null) }

// In drawing logic
dataPoints.forEachIndexed { index, value ->
    val isHovered = hoveredIndex == index
    val alpha = if (isHovered) 1f else 0.8f
    val scale = if (isHovered) 1.05f else 1f
    
    // Apply hover effect
    drawRoundRect(
        color = barColor.copy(alpha = alpha),
        // ... with scale transformation
    )
}
```

### 3.3 Grid Line Improvements

**Current:** Basic dashed grid lines

**Improvements:**
```kotlin
// Enhanced grid styling
val gridColor = axisColor.copy(alpha = 0.08f) // More subtle
val dashArray = floatArrayOf(8f, 8f) // Smaller dashes

// Alternating grid line weights for visual hierarchy
yTickValues.forEachIndexed { index, value ->
    val y = chartBottom - ((value - axisMin) / range) * chartHeight
    val isMajorLine = index % 2 == 0
    val lineWidth = if (isMajorLine) 1.5f else 0.8f
    val lineAlpha = if (isMajorLine) 0.1f else 0.05f
    
    drawLine(
        color = axisColor.copy(alpha = lineAlpha),
        start = Offset(chartLeft, y),
        end = Offset(chartRight, y),
        strokeWidth = lineWidth,
        pathEffect = if (isMajorLine) {
            PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
        } else {
            PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
        }
    )
}
```

### 3.4 Tooltip Enhancements

**Current Issues:**
- Tooltip disappears too quickly on finger lift
- Tooltip styling could be more polished
- Missing tooltip for bar charts

**Improvements:**
```kotlin
// Add persistent tooltip with delay before hiding
val tooltipVisible by remember { mutableStateOf(false) }
val hideTooltipJob = remember { mutableStateOf<Job?>(null) }

// On drag end
onDragEnd = {
    // Keep tooltip visible for 2 seconds after drag ends
    hideTooltipJob.value?.cancel()
    hideTooltipJob.value = scope.launch {
        delay(2000)
        tooltipVisible = false
    }
}

// Enhanced tooltip styling
drawRoundRect(
    color = Color.White.copy(alpha = 0.95f),
    topLeft = Offset(tooltipX - paddingX, tooltipY - paddingY - tooltipHeight),
    size = Size(tooltipWidth + paddingX * 2, tooltipHeight + paddingY * 2),
    cornerRadius = CornerRadius(12f, 12f), // More rounded
    style = Fill
)

// Add subtle shadow
drawRoundRect(
    color = Color.Black.copy(alpha = 0.1f),
    topLeft = Offset(tooltipX - paddingX + 2f, tooltipY - paddingY - tooltipHeight + 2f),
    size = Size(tooltipWidth + paddingX * 2, tooltipHeight + paddingY * 2),
    cornerRadius = CornerRadius(12f, 12f),
    style = Fill
)
```

### 3.5 Chart Type Specific Improvements

#### 3.5.1 Line Chart
- **Smooth Curving:** Add tension control for curve smoothing
- **Area Fill:** Improve gradient fill under line
- **Data Point Markers:** Add option to show/hide point markers
- **Threshold Lines:** Add horizontal threshold lines for goals

#### 3.5.2 Bar Chart
- **Grouped Bars:** Support for grouped bar charts
- **Stacked Bars:** Improve stacked bar visualization
- **Bar Labels:** Add value labels on top of bars
- **Negative Values:** Proper handling of negative values

#### 3.5.3 Donut Chart
- **Center Text:** Add dynamic center text
- **Segment Spacing:** Add spacing between segments
- **Animation:** Add entry animation for segments
- **Interaction:** Add segment selection/expansion

### 3.6 Responsive Chart Sizing

**Current:** Fixed height charts

**Improvement:**
```kotlin
// Add responsive height based on data density
@Composable
fun LuminousLineChart(
    // ... existing params
    height: Dp? = null, // Make optional
    minHeight: Dp = 120.dp,
    maxHeight: Dp = 300.dp,
    // ...
)

// Calculate optimal height
val optimalHeight = when {
    dataPoints.size > 20 -> maxHeight
    dataPoints.size > 10 -> (minHeight + maxHeight) / 2
    else -> minHeight
}

val chartHeight = height ?: optimalHeight
```

---

## 4. Cross-Component Improvements

### 4.1 Theme Consistency

**Issue:** Hardcoded colors throughout components break dark theme support

**Solution:** Replace all hardcoded colors with design system tokens
- `Color(0xFF0F172A)` → `MaterialTheme.colorScheme.onSurface`
- `Color(0xFF06B6D4)` → `Primary` or `MaterialTheme.colorScheme.primary`
- `Color.White.copy(alpha = 0.78f)` → `LocalVitaColors.current.glassFill`

### 4.2 Accessibility

**Improvements:**
1. **Add contentDescriptions** to all interactive elements
2. **Increase Touch Targets** - Ensure minimum 48dp touch targets
3. **Color Contrast** - Verify WCAG AA compliance
4. **Screen Reader Support** - Proper semantic labeling
5. **Reduced Motion** - Respect user's reduce motion settings

### 4.3 Performance

**Optimizations:**
1. **Paint Object Caching** - Cache Paint objects in remember
2. **Path Reuse** - Reuse Path objects where possible
3. **Animation Optimization** - Use hardware-accelerated animations
4. **Lazy Loading** - Implement lazy loading for chart data

---

## 5. Implementation Priority

### Phase 1: Critical Fixes (Week 1)
1. ✅ Fix chat bar keyboard layout issue
2. ✅ Fix CircadianClockDial label contrast
3. ✅ Improve chart axis padding and spacing
4. ✅ Fix bar chart shape and spacing

### Phase 2: Visual Enhancements (Week 2)
1. ✅ Enhance chat bubble design with timestamps
2. ✅ Improve chart grid line styling
3. ✅ Add tooltip enhancements
4. ✅ Redesign circadian vitals cards

### Phase 3: Interactive Features (Week 3)
1. ✅ Add multi-line chat input
2. ✅ Implement chart hover effects
3. ✅ Add sleep tracking animations
4. ✅ Implement quick reply suggestions

### Phase 4: Polish & Advanced Features (Week 4)
1. ✅ Add chart responsive sizing
2. ✅ Implement chronotype integration
3. ✅ Add message search functionality
4. ✅ Implement accessibility improvements

---

## 6. Testing Checklist

### Chat Section
- [ ] Chat bar stays fixed when keyboard opens
- [ ] Multi-line input expands properly
- [ ] Messages display timestamps correctly
- [ ] Swipe actions work smoothly
- [ ] Empty state displays correctly

### Circadian Alignment
- [ ] Clock labels readable in both themes
- [ ] Drag interaction is smooth
- [ ] Sleep arc colors indicate quality
- [ ] Bento cards expand on tap
- [ ] Tracking animation plays correctly

### Charts
- [ ] Axis labels don't overlap
- [ ] Padding is adequate for label visibility
- [ ] Bar shapes are consistent
- [ ] Tooltips display correctly
- [ ] Grid lines are subtle but visible
- [ ] Charts resize responsively

---

## 7. Success Metrics

- **User Satisfaction:** Target 4.5/5 rating on UI improvements
- **Accessibility:** WCAG AA compliance for all components
- **Performance:** <16ms render time for all animations
- **Crash Rate:** <0.1% crash rate related to UI changes
- **User Engagement:** 15% increase in chat usage
- **Sleep Tracking:** 20% increase in tracking session starts

---

## 8. Notes

- All changes should maintain backward compatibility with existing data
- Dark theme support must be thoroughly tested
- Performance impact should be monitored with profiling tools
- User feedback should be collected after each phase
- Consider A/B testing for major UI changes

---

## 9. Additional Screen & Component Improvements

### 9.1 Dashboard Screen Enhancements

**Current Issues:**
- Basic permissions screen could be more engaging
- Metric cards lack visual hierarchy and interaction feedback
- Readiness score card could be more informative
- Tip cards don't have interactive elements

**Improvements:**

#### 9.1.1 Enhanced Permissions Screen
```kotlin
// Replace basic permissions request with educational carousel
@Composable
private fun PermissionsScreen(viewModel: DashboardViewModel) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animated illustration instead of static icon
        PermissionsAnimation()
        
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Connect Your Health",
            style = MaterialTheme.typography.headlineMedium,
            color = Primary
        )
        
        // Benefits carousel showing what data will be synced
        PermissionsBenefitsCarousel(
            benefits = listOf(
                "Track workouts from Samsung Health & Google Fit",
                "Sync nutrition data automatically",
                "Monitor sleep patterns from wearables",
                "Get personalized AI insights"
            )
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Improved CTA button with trust indicators
        GlassCardGlow(
            glowColor = Primary,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = { /* request permissions */ },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Shield, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Connect Securely")
                }
            }
        }
        
        // Trust indicators
        Row(
            modifier = Modifier.padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TrustBadge(text = "Privacy First", icon = Icons.Default.Lock)
            TrustBadge(text = "Local Storage", icon = Icons.Default.Storage)
        }
    }
}
```

#### 9.1.2 Interactive Metric Cards
```kotlin
// Enhanced MetricCard with micro-interactions
@Composable
private fun MetricCard(
    icon: ImageVector,
    label: String,
    value: String,
    tone: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    
    GlassCard(
        modifier = modifier
            .clickable { onClick() }
            .graphicsLayer {
                scaleX = if (isPressed) 0.95f else 1f
                scaleY = if (isPressed) 0.95f else 1f
            },
        cornerRadius = 24.dp,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // Icon with background glow
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(getToneColor(tone).copy(alpha = 0.15f))
                    .padding(12.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = label,
                    tint = getToneColor(tone),
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                ),
                color = Color(0xFF0F172A)
            )
            
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = Color.Black.copy(alpha = 0.5f)
            )
            
            // Mini trend indicator
            TrendIndicator(trend = "up", percentage = "+12%")
        }
    }
}
```

#### 9.1.3 Enhanced Readiness Score Card
```kotlin
// Add more visual feedback and context to readiness score
@Composable
private fun ReadinessScoreCard(
    score: Int,
    snapshot: HealthSnapshot,
    insight: String,
    onClick: () -> Unit
) {
    GlassCardGlow(
        modifier = Modifier.fillMaxWidth(),
        glowColor = when {
            score >= 80 -> AccentGreen
            score >= 60 -> Primary
            else -> Tertiary
        }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = getReadinessColor(score),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Readiness Score",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.Black.copy(alpha = 0.6f)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    "$score%",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 48.sp
                    ),
                    color = getReadinessColor(score)
                )
                
                Text(
                    getReadinessLabel(score),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Black.copy(alpha = 0.5f)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    insight,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black.copy(alpha = 0.7f),
                    maxLines = 2
                )
            }
            
            // Mini donut chart visualization
            Box(
                modifier = Modifier.size(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Draw readiness arc
                }
                Text(
                    "${score.coerceAtMost(100)}",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
        
        // Contributing factors breakdown
        Spacer(modifier = Modifier.height(16.dp))
        ReadinessFactorsRow(
            sleepQuality = snapshot.sleepQuality,
            recovery = snapshot.recovery,
            stress = snapshot.stressLevel
        )
    }
}
```

### 9.2 Activity Screen Improvements

**Current Issues:**
- Workout templates list could be more visual
- Health data visualization needs enhancement
- Search functionality is basic
- Category filters are static

**Improvements:**

#### 9.2.1 Enhanced Workout Template Cards
```kotlin
// Add visual preview, difficulty rating, and last used info
@Composable
private fun WorkoutTemplateCard(
    template: WorkoutTemplateEntity,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        cornerRadius = 20.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Workout type icon with gradient background
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = getWorkoutTypeColors(template.mode)
                        )
                    )
                    .padding(12.dp)
            ) {
                Icon(
                    getWorkoutIcon(template.mode),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    template.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFF0F172A)
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DifficultyBadge(difficulty = template.difficulty)
                    DurationBadge(minutes = template.estimatedDuration)
                    ExercisesBadge(count = template.exerciseCount)
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    template.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Black.copy(alpha = 0.5f),
                    maxLines = 1
                )
            }
            
            // Last used indicator
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "Last used",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Black.copy(alpha = 0.4f)
                )
                Text(
                    formatLastUsed(template.lastUsedAt),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Black.copy(alpha = 0.7f)
                )
            }
        }
    }
}
```

#### 9.2.2 Enhanced Category Filters
```kotlin
// Replace static filters with interactive chips
@Composable
private fun CategoryFilterRow(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    val categories = listOf(
        "All" to Icons.Default.Dashboard,
        "Cardio" to Icons.Default.DirectionsRun,
        "Strength" to Icons.Default.FitnessCenter,
        "HIIT" to Icons.Default.Whatshot,
        "Yoga" to Icons.Default.SelfImprovement
    )
    
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 20.dp)
    ) {
        items(categories) { (category, icon) ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = { Text(category) },
                leadingIcon = {
                    Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
                },
                modifier = Modifier.height(40.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Primary,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}
```

### 9.3 Nutrition Screen Improvements

**Current Issues:**
- Food categories are hardcoded and limited
- Nutrition visualization could be more engaging
- Water tracking is basic
- Custom food entry is not intuitive

**Improvements:**

#### 9.3.1 Dynamic Food Categories
```kotlin
// Replace hardcoded categories with dynamic system based on food properties
@Composable
private fun FoodCategorySystem(
    foods: List<FoodCatalogItem>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    // Dynamically categorize foods based on macronutrient profile
    val dynamicCategories = remember(foods) {
        val categories = mutableListOf<Pair<String, List<FoodCatalogItem>>>()
        
        // High protein (>20g per serving)
        categories.add("High Protein" to foods.filter { it.proteinGrams > 20 })
        
        // Low carb (<10g per serving)
        categories.add("Low Carb" to foods.filter { it.carbsGrams < 10 })
        
        // High fiber (>5g per serving)
        categories.add("High Fiber" to foods.filter { it.fiberGrams > 5 })
        
        // Meal types
        categories.add("Breakfast" to foods.filter { it.mealType == "breakfast" })
        categories.add("Lunch" to foods.filter { it.mealType == "lunch" })
        categories.add("Dinner" to foods.filter { it.mealType == "dinner" })
        categories.add("Snacks" to foods.filter { it.mealType == "snack" })
        
        categories.filter { it.second.isNotEmpty() }
    }
    
    // Display category chips with counts
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 20.dp)
    ) {
        item {
            CategoryChip(
                category = "All",
                count = foods.size,
                isSelected = selectedCategory == "ALL",
                onClick = { onCategorySelected("ALL") }
            )
        }
        
        items(dynamicCategories) { (category, items) ->
            CategoryChip(
                category = category,
                count = items.size,
                isSelected = selectedCategory == category,
                onClick = { onCategorySelected(category) }
            )
        }
    }
}
```

#### 9.3.2 Enhanced Nutrition Visualization
```kotlin
// Add more detailed breakdown with goal progress
@Composable
private fun NutritionBreakdownCard(
    summary: NutritionSummary,
    goals: DailyGoals
) {
    ApexCard(
        title = "Today's Nutrition",
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Main macros with progress bars
            MacroProgressRow(
                label = "Protein",
                current = summary.proteinGrams,
                goal = goals.proteinGrams,
                color = AccentGreen
            )
            
            MacroProgressRow(
                label = "Carbs",
                current = summary.carbsGrams,
                goal = goals.carbsGrams,
                color = Primary
            )
            
            MacroProgressRow(
                label = "Fat",
                current = summary.fatGrams,
                goal = goals.fatGrams,
                color = Tertiary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Secondary metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SecondaryMetric(
                    label = "Calories",
                    value = "${summary.totalCalories}",
                    goal = goals.calories,
                    unit = "kcal"
                )
                SecondaryMetric(
                    label = "Fiber",
                    value = "${summary.fiberGrams}",
                    goal = 25f,
                    unit = "g"
                )
                SecondaryMetric(
                    label = "Sugar",
                    value = "${summary.sugarGrams}",
                    goal = 30f,
                    unit = "g"
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Meal distribution timeline
            MealDistributionTimeline(
                meals = summary.meals
            )
        }
    }
}
```

#### 9.3.3 Enhanced Water Tracking
```kotlin
// Add more engaging water tracking with visual feedback
@Composable
private fun EnhancedWaterTracking(
    currentIntake: Float,
    goal: Float,
    onAddWater: (Float) -> Unit
) {
    GlassCardGlow(
        modifier = Modifier.fillMaxWidth(),
        glowColor = if (currentIntake >= goal) AccentGreen else Primary.copy(alpha = 0.3f)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.WaterDrop,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Hydration",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                
                Text(
                    "${currentIntake.toInt()} / ${goal.toInt()} ml",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (currentIntake >= goal) AccentGreen else Primary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Enhanced water capsule with waves
            SloshingWaterCapsule(
                progress = currentIntake / goal,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Quick add buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickAddButton(
                    amount = 150f,
                    label = "150ml",
                    icon = Icons.Default.LocalDrink,
                    onClick = { onAddWater(150f) }
                )
                QuickAddButton(
                    amount = 250f,
                    label = "250ml",
                    icon = Icons.Default.WaterDrop,
                    onClick = { onAddWater(250f) }
                )
                QuickAddButton(
                    amount = 500f,
                    label = "500ml",
                    icon = Icons.Default.Coffee,
                    onClick = { onAddWater(500f) }
                )
                CustomAddButton(onClick = { /* show custom dialog */ })
            }
        }
    }
}
```

### 9.4 Profile Screen Improvements

**Current Issues:**
- Profile editing could be more intuitive
- Settings organization is basic
- Achievement display is static
- Health metrics visualization needs improvement

**Improvements:**

#### 9.4.1 Enhanced Profile Editor
```kotlin
// Replace multiple dialogs with unified profile editor
@Composable
private fun ProfileEditorSheet(
    currentUser: ProfileData,
    onSave: (ProfileData) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.9f)
            .background(Color.White)
            .padding(24.dp)
    ) {
        // Header with avatar upload
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Edit Profile",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Avatar with edit button
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color.Gray.copy(alpha = 0.2f))
                .clickable { /* trigger image picker */ },
            contentAlignment = Alignment.Center
        ) {
            if (currentUser.avatarUrl != null) {
                // Load avatar image
            } else {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Color.Gray
                )
            }
            
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Primary)
                    .padding(6.dp)
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit avatar",
                    tint = Color.White,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Form fields with validation
        ProfileTextField(
            label = "Display Name",
            value = currentUser.displayName,
            onValueChange = { /* update */ },
            placeholder = "Enter your name"
        )
        
        ProfileTextField(
            label = "Bio",
            value = currentUser.bio,
            onValueChange = { /* update */ },
            placeholder = "Tell us about yourself",
            maxLines = 3
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Health metrics section
        Text(
            "Health Metrics",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            )
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricInputField(
                label = "Weight (kg)",
                value = currentUser.weightKg?.toString(),
                onValueChange = { /* update */ },
                modifier = Modifier.weight(1f)
            )
            MetricInputField(
                label = "Height (cm)",
                value = currentUser.heightCm?.toString(),
                onValueChange = { /* update */ },
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Save button
        Button(
            onClick = { onSave(currentUser) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary)
        ) {
            Text("Save Changes", style = MaterialTheme.typography.labelLarge)
        }
    }
}
```

#### 9.4.2 Enhanced Settings Organization
```kotlin
// Group settings logically with visual hierarchy
@Composable
private fun EnhancedSettingsSection(
    viewModel: ProfileViewModel
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Account Section
        item {
            SectionHeader("Account")
            GlassCard {
                Column {
                    SettingsActionRow(
                        icon = Icons.Default.Email,
                        title = "Email",
                        subtitle = viewModel.email,
                        onClick = { /* navigate to email settings */ }
                    )
                    HorizontalDivider()
                    SettingsActionRow(
                        icon = Icons.Default.Lock,
                        title = "Password",
                        subtitle = "Change password",
                        onClick = { /* show password dialog */ }
                    )
                    HorizontalDivider()
                    SettingsActionRow(
                        icon = Icons.Default.Shield,
                        title = "Privacy",
                        subtitle = "Manage privacy settings",
                        onClick = { /* navigate to privacy */ }
                    )
                }
            }
        }
        
        // Health Data Section
        item {
            SectionHeader("Health Data")
            GlassCard {
                Column {
                    SettingsActionRow(
                        icon = Icons.Default.Sync,
                        title = "Sync Health Connect",
                        subtitle = "Manage connected apps",
                        onClick = { /* show sync dialog */ }
                    )
                    HorizontalDivider()
                    SettingsActionRow(
                        icon = Icons.Default.CloudUpload,
                        title = "Cloud Backup",
                        subtitle = "Last backup: 2 hours ago",
                        onClick = { /* trigger backup */ }
                    )
                    HorizontalDivider()
                    SettingsActionRow(
                        icon = Icons.Default.Delete,
                        title = "Export Data",
                        subtitle = "Download your health data",
                        onClick = { /* export data */ }
                    )
                }
            }
        }
        
        // Preferences Section
        item {
            SectionHeader("Preferences")
            GlassCard {
                Column {
                    SettingsToggleRow(
                        icon = Icons.Default.Notifications,
                        title = "Notifications",
                        subtitle = "Push notifications for reminders",
                        checked = viewModel.notificationsEnabled,
                        onCheckedChange = { viewModel.setNotificationsEnabled(it) }
                    )
                    HorizontalDivider()
                    SettingsToggleRow(
                        icon = Icons.Default.DarkMode,
                        title = "Dark Mode",
                        subtitle = "Use dark theme",
                        checked = viewModel.darkModeEnabled,
                        onCheckedChange = { viewModel.setDarkModeEnabled(it) }
                    )
                    HorizontalDivider()
                    SettingsActionRow(
                        icon = Icons.Default.Language,
                        title = "Units",
                        subtitle = if (viewModel.useMetric) "Metric (kg, km)" else "Imperial (lbs, mi)",
                        onClick = { viewModel.toggleUnits() }
                    )
                }
            }
        }
        
        // Support Section
        item {
            SectionHeader("Support")
            GlassCard {
                Column {
                    SettingsActionRow(
                        icon = Icons.Default.Help,
                        title = "Help Center",
                        subtitle = "FAQs and support articles",
                        onClick = { /* open help */ }
                    )
                    HorizontalDivider()
                    SettingsActionRow(
                        icon = Icons.Default.Feedback,
                        title = "Send Feedback",
                        subtitle = "Tell us what you think",
                        onClick = { /* open feedback */ }
                    )
                    HorizontalDivider()
                    SettingsActionRow(
                        icon = Icons.Default.Info,
                        title = "About",
                        subtitle = "Version 1.0.0",
                        onClick = { /* show about dialog */ }
                    )
                }
            }
        }
        
        // Danger Zone
        item {
            SectionHeader("Danger Zone")
            GlassCardGlow(glowColor = Tertiary) {
                SettingsActionRow(
                    icon = Icons.Default.ExitToApp,
                    title = "Log Out",
                    subtitle = "Sign out of your account",
                    onClick = { viewModel.logout() },
                    titleColor = Tertiary
                )
                HorizontalDivider()
                SettingsActionRow(
                    icon = Icons.Default.DeleteForever,
                    title = "Delete Account",
                    subtitle = "Permanently delete your data",
                    onClick = { /* show delete confirmation */ },
                    titleColor = Tertiary
                )
            }
        }
    }
}
```

### 9.5 Auth Screen Improvements

**Current Issues:**
- Basic authentication flow could be more engaging
- Missing social login options beyond Google
- No password recovery flow
- Loading states are basic

**Improvements:**

#### 9.5.1 Enhanced Auth Flow
```kotlin
// Add more engaging onboarding with progress indicator
@Composable
private fun EnhancedAuthScreen(
    onAuthSuccess: () -> Unit,
    viewModel: AuthViewModel
) {
    var authStep by remember { mutableStateOf(AuthStep.WELCOME) }
    
    when (authStep) {
        AuthStep.WELCOME -> WelcomeScreen(
            onGetStarted = { authStep = AuthStep.SIGN_UP },
            onSignIn = { authStep = AuthStep.SIGN_IN }
        )
        AuthStep.SIGN_UP -> SignUpScreen(
            onSignUp = { onAuthSuccess() },
            onBack = { authStep = AuthStep.WELCOME },
            onHaveAccount = { authStep = AuthStep.SIGN_IN }
        )
        AuthStep.SIGN_IN -> SignInScreen(
            onSignIn = { onAuthSuccess() },
            onBack = { authStep = AuthStep.WELCOME },
            onForgotPassword = { authStep = AuthStep.FORGOT_PASSWORD },
            onNoAccount = { authStep = AuthStep.SIGN_UP }
        )
        AuthStep.FORGOT_PASSWORD -> ForgotPasswordScreen(
            onResetSent = { authStep = AuthStep.CHECK_EMAIL },
            onBack = { authStep = AuthStep.SIGN_IN }
        )
        AuthStep.CHECK_EMAIL -> CheckEmailScreen(
            onResend = { /* resend email */ },
            onBack = { authStep = AuthStep.SIGN_IN }
        )
    }
}

enum class AuthStep {
    WELCOME,
    SIGN_UP,
    SIGN_IN,
    FORGOT_PASSWORD,
    CHECK_EMAIL
}
```

#### 9.5.2 Enhanced Social Login Options
```kotlin
// Add more social login options with consistent styling
@Composable
private fun SocialLoginRow(
    onGoogleLogin: () -> Unit,
    onAppleLogin: () -> Unit,
    onFacebookLogin: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = Color.Black.copy(alpha = 0.1f)
            )
            Text(
                " or continue with ",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Black.copy(alpha = 0.4f)
            )
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = Color.Black.copy(alpha = 0.1f)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SocialLoginButton(
                icon = Icons.Default.AccountCircle, // Replace with Google icon
                label = "Google",
                onClick = onGoogleLogin,
                modifier = Modifier.weight(1f),
                backgroundColor = Color.White,
                textColor = Color.Black
            )
            SocialLoginButton(
                icon = Icons.Default.Apple, // Replace with Apple icon
                label = "Apple",
                onClick = onAppleLogin,
                modifier = Modifier.weight(1f),
                backgroundColor = Color.Black,
                textColor = Color.White
            )
            SocialLoginButton(
                icon = Icons.Default.Facebook, // Replace with Facebook icon
                label = "Facebook",
                onClick = onFacebookLogin,
                modifier = Modifier.weight(1f),
                backgroundColor = Color(0xFF1877F2),
                textColor = Color.White
            )
        }
    }
}
```

### 9.6 Component Library Enhancements

**Current Issues:**
- Some components lack consistent theming
- Missing variants for different use cases
- Animation performance could be optimized
- Accessibility support is inconsistent

**Improvements:**

#### 9.6.1 Enhanced GlassCard System
```kotlin
// Add more glass card variants for different contexts
@Composable
fun GlassCardElevated(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 30.dp,
    elevation: Dp = 24.dp,
    contentPadding: Dp = 24.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val shadowColor = Color.Black.copy(alpha = 0.15f).toArgb()
    val elevPx = elevation.value
    
    Column(
        modifier = modifier
            .drawBehind {
                val paint = Paint().apply {
                    asFrameworkPaint().apply {
                        isAntiAlias = true
                        color = android.graphics.Color.TRANSPARENT
                        setShadowLayer(elevPx * 2.5f, 0f, elevPx * 1.2f, shadowColor)
                    }
                }
                drawIntoCanvas { canvas ->
                    canvas.drawRoundRect(
                        left = 0f, top = 0f,
                        right = size.width, bottom = size.height,
                        radiusX = cornerRadius.toPx(),
                        radiusY = cornerRadius.toPx(),
                        paint = paint
                    )
                }
            }
            .graphicsLayer(
                shadowElevation = elevation.value,
                shape = shape,
                ambientShadowColor = Color.Black.copy(alpha = 0.15f),
                spotShadowColor = Color.Black.copy(alpha = 0.2f)
            )
            .clip(shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .background(Color.White.copy(alpha = 0.85f))
            .border(
                width = 1.5.dp,
                color = Color.Black.copy(alpha = 0.08f),
                shape = shape
            )
            .padding(contentPadding),
        content = content
    )
}

@Composable
fun GlassCardCompact(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    contentPadding: Dp = 16.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    // Smaller, less prominent version for UI elements
    GlassCard(
        modifier = modifier,
        cornerRadius = cornerRadius,
        contentPadding = contentPadding,
        onClick = onClick,
        content = content
    )
}
```

#### 9.6.2 Enhanced Button System
```kotlin
// Add more button variants and states
@Composable
fun GlowButtonSecondary(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    val shape = RoundedCornerShape(24.dp)
    
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = shape,
        enabled = enabled && !loading,
        colors = ButtonDefaults.buttonColors(
            containerColor = Primary.copy(alpha = 0.1f),
            contentColor = Primary,
            disabledContainerColor = Primary.copy(alpha = 0.05f),
            disabledContentColor = Primary.copy(alpha = 0.3f)
        ),
        border = BorderStroke(
            width = 1.5.dp,
            color = if (enabled) Primary else Primary.copy(alpha = 0.3f)
        )
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = Primary
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
fun GlowButtonDanger(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val shape = RoundedCornerShape(24.dp)
    
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = shape,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = Tertiary,
            contentColor = Color.White,
            disabledContainerColor = Tertiary.copy(alpha = 0.5f),
            disabledContentColor = Color.White.copy(alpha = 0.5f)
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}
```

#### 9.6.3 Enhanced Loading States
```kotlin
// Add more sophisticated loading and empty states
@Composable
fun LoadingStateCard(
    message: String = "Loading...",
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = 32.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Animated loading blob
            AmbientGlowBlob(
                color = Primary,
                modifier = Modifier.size(80.dp),
                durationMillis = 2000
            )
            
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun EmptyStateCard(
    icon: ImageVector,
    title: String,
    message: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = 32.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.05f))
                    .padding(16.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = Color.Black.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            Text(
                title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color(0xFF0F172A)
            )
            
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
            
            if (actionText != null && onAction != null) {
                GlowButtonSecondary(
                    text = actionText,
                    onClick = onAction,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
```

### 9.7 Navigation & App Structure Improvements

**Current Issues:**
- Bottom navigation could be more informative
- Transitions between screens could be smoother
- Missing deep linking support
- No gesture-based navigation

**Improvements:**

#### 9.7.1 Enhanced Bottom Navigation
```kotlin
// Add badges, notifications, and better visual feedback
@Composable
private fun EnhancedBottomNavigationBar(
    navController: NavController,
    currentRoute: String?
) {
    val items = listOf(
        BottomNavItem(
            route = "chat",
            icon = Icons.Default.Chat,
            label = "Coach",
            badge = 0
        ),
        BottomNavItem(
            route = "activity",
            icon = Icons.Default.DirectionsRun,
            label = "Activity",
            badge = 3
        ),
        BottomNavItem(
            route = "dashboard",
            icon = Icons.Default.Dashboard,
            label = "Home",
            badge = 0
        ),
        BottomNavItem(
            route = "analytics",
            icon = Icons.Default.Analytics,
            label = "Insights",
            badge = 1
        ),
        BottomNavItem(
            route = "profile",
            icon = Icons.Default.Person,
            label = "Profile",
            badge = 0
        )
    )
    
    NavigationBar(
        containerColor = Color.White.copy(alpha = 0.95f),
        tonalElevation = 8.dp
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    BadgedBox(
                        badge = {
                            if (item.badge > 0) {
                                Badge {
                                    Text(
                                        item.badge.toString(),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(
                            item.icon,
                            contentDescription = item.label,
                            tint = if (selected) Primary else Color.Black.copy(alpha = 0.5f)
                        )
                    }
                },
                label = {
                    Text(
                        item.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) Primary else Color.Black.copy(alpha = 0.5f)
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Primary,
                    selectedTextColor = Primary,
                    unselectedIconColor = Color.Black.copy(alpha = 0.5f),
                    unselectedTextColor = Color.Black.copy(alpha = 0.5f),
                    indicatorColor = Primary.copy(alpha = 0.1f)
                )
            )
        }
    }
}
```

#### 9.7.2 Enhanced Screen Transitions
```kotlin
// Add more sophisticated transitions between screens
@Composable
private fun EnhancedNavigationHost(
    navController: NavController,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            slideInHorizontally(
                animationSpec = tween(400, easing = FastOutSlowInEasing)
            ) { it } + fadeIn(
                animationSpec = tween(400, easing = FastOutSlowInEasing)
            )
        },
        exitTransition = {
            slideOutHorizontally(
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            ) { -it / 3 } + fadeOut(
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            )
        },
        popEnterTransition = {
            slideInHorizontally(
                animationSpec = tween(400, easing = FastOutSlowInEasing)
            ) { -it } + fadeIn(
                animationSpec = tween(400, easing = FastOutSlowInEasing)
            )
        },
        popExitTransition = {
            slideOutHorizontally(
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            ) { it / 3 } + fadeOut(
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            )
        }
    ) {
        // Navigation graph
    }
}
```

---

## 10. Implementation Priority (Updated)

### Phase 1: Critical Fixes (Week 1)
1. ✅ Fix chat bar keyboard layout issue
2. ✅ Fix CircadianClockDial label contrast
3. ✅ Improve chart axis padding and spacing
4. ✅ Fix bar chart shape and spacing
5. ✅ **NEW:** Fix permissions screen engagement
6. ✅ **NEW:** Improve loading states consistency

### Phase 2: Visual Enhancements (Week 2)
1. ✅ Enhance chat bubble design with timestamps
2. ✅ Improve chart grid line styling
3. ✅ Add tooltip enhancements
4. ✅ Redesign circadian vitals cards
5. ✅ **NEW:** Enhance metric cards interactivity
6. ✅ **NEW:** Improve profile editor UX

### Phase 3: Interactive Features (Week 3)
1. ✅ Add multi-line chat input
2. ✅ Implement chart hover effects
3. ✅ Add sleep tracking animations
4. ✅ Implement quick reply suggestions
5. ✅ **NEW:** Add dynamic food categories
6. ✅ **NEW:** Implement social login options

### Phase 4: Polish & Advanced Features (Week 4)
1. ✅ Add chart responsive sizing
2. ✅ Implement chronotype integration
3. ✅ Add message search functionality
4. ✅ Implement accessibility improvements
5. ✅ **NEW:** Enhanced bottom navigation
6. ✅ **NEW:** Component library variants

---

## 11. Additional Testing Checklist

### Dashboard
- [ ] Permissions carousel displays correctly
- [ ] Metric cards show proper feedback
- [ ] Readiness score calculation is accurate
- [ ] Pull-to-refresh works smoothly
- [ ] Empty states display properly

### Activity
- [ ] Workout template cards load quickly
- [ ] Category filters work correctly
- [ ] Search functionality is responsive
- [ ] Health data visualizations update
- [ ] Template creation flow is smooth

### Nutrition
- [ ] Dynamic categories populate correctly
- [ ] Food search is fast and accurate
- [ ] Water tracking adds correctly
- [ ] Macro progress bars animate
- [ ] Custom food entry works

### Profile
- [ ] Profile editor saves correctly
- [ ] Settings changes persist
- [ ] Avatar upload works
- [ ] Health metrics display properly
- [ ] Logout functionality works

### Auth
- [ ] Welcome screen transitions smoothly
- [ ] Social login options work
- [ ] Password recovery sends email
- [ ] Form validation works
- [ ] Loading states display

### Components
- [ ] GlassCard variants render correctly
- [ ] Button states work properly
- [ ] Loading states are consistent
- [ ] Empty states display correctly
- [ ] Skeleton loaders animate smoothly

---

**Generated with [Devin](https://cli.devin.ai/docs)**