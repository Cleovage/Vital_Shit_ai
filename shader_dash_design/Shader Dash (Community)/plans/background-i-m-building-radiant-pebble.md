# Plan: Analytics Time Range Selector + Tab Switching Animation

## Context
VitaAI already has a Day/Week/Month/Year range selector inside `AnalyticDetail` (the drilled-in chart view), but the **analytics summary view** (the ChartCard grid) has no time-range control — all three charts always show the same hardcoded dataset. Additionally, the tab-switching animation is a generic fade+slide-up that doesn't reflect navigation direction or feel premium enough for a touch-optimized app.

## Changes

### 1. Analytics Summary View — Time Range Selector
**File**: `src/app/screens/VitaApp.tsx`

- Lift the `AnalyticRange` state up to the `Analytics` component (currently it only lives inside `AnalyticDetail`)
- Add a pill-style Day / Week / Month / Year selector at the top of the summary view, matching the existing selector style in `AnalyticDetail`
- Pass the selected range down into each `ChartCard` so the displayed data reflects the chosen time period
- `ChartCard` already receives `color` and `label` props; add a `range` prop and look up the correct dataset from `analyticSeries` inside it
- The range selector should be sticky/visible at all times in the summary view, not just after drill-in

### 2. Tab Switching Animation
**File**: `src/app/screens/VitaApp.tsx` (screen wrapper `motion.div`), `src/app/Layout.tsx`

Current behavior: all screens enter with `{ opacity: 0, y: 12 }` regardless of which tab was tapped.

New behavior — **directional slide based on navigation order**:
- Tabs are ordered: Chat(0) → Health(1) → Home(2) → Analytics(3) → Profile(4)
- Navigating right (higher index): new screen slides in from right (`x: 40`), old exits left
- Navigating left (lower index): new screen slides in from left (`x: -40`), old exits right
- Use `AnimatePresence mode="wait"` with a `custom` prop for direction
- Transition: `duration: 0.28`, `ease: [0.32, 0.72, 0, 1]` (iOS-style curve) — snappy and touch-appropriate
- Keep vertical `y` motion removed (feels better for horizontal tab nav)

**Implementation detail**:
- Store `previousTab` in state alongside `activeTab` in the top-level `VitaApp` component
- Compute direction = `activeTab > previousTab ? 1 : -1`
- Pass direction as `custom` prop to `AnimatePresence`
- Each screen `motion.div` uses `variants` keyed on direction for enter/exit

## Files to Modify
- `src/app/screens/VitaApp.tsx` — main changes (Analytics range state lift, ChartCard range prop, screen animation variants)
- `src/app/Layout.tsx` — pass tab-change callbacks or read active index for direction tracking (may not need changes if direction is tracked in VitaApp)

## Verification
- Tap each nav item in order and in reverse — screens should slide appropriately left/right
- In Analytics summary, tap each time range button — all three ChartCards update their data simultaneously
- Drill into a chart — the existing range selector inside AnalyticDetail should still work independently
- No jank or flash between transitions on touch
