# VitaAI — UI/UX Consistency Audit & Profile Redesign Proposal

**Scope:** Read-only design-system audit of the Android Compose app at
`C:\Users\rk107\wellbeing_firebase`. All 10 production screens + 2 newly-added
detail screens (Meditation, HydrationDetail) were inspected. No code was
modified. This report cites absolute paths and line numbers.

---

## 1. Visual Language Inventory

### 1.1 Recurring patterns (the "spec")

| Token | Canonical value | Source of truth |
|---|---|---|
| Page header kicker | `13sp / SemiBold / letterSpacing 4.4.sp / Color.Black alpha 0.40f` | `ui/components/PageHeader.kt:38-45` |
| Page header title | `40sp / SemiBold / letterSpacing (-2).sp / Color(0xFF0F172A)` | `ui/components/PageHeader.kt:47-55` |
| Glass card fill | `Color.White.copy(alpha = 0.78f)` | `ui/components/GlassCard.kt:75` |
| Glass card border | `1.dp` / `Color.Black.copy(alpha = 0.07f)` | `ui/components/GlassCard.kt:76-80` |
| Glass card corner | `30.dp` default | `ui/components/GlassCard.kt:28` |
| Card content padding | `20.dp` default | `ui/components/GlassCard.kt:29` |
| Card outer shadow | `12f` elevation, alpha 0.12/0.15 | `ui/components/GlassCard.kt:62-65` |
| Outline token | `Color(0x12000000)` (7 % black) | `ui/theme/Color.kt:63` |
| OutlineVariant | `Color(0x0F000000)` (6 % black) | `ui/theme/Color.kt:64` |
| Primary action button | `RoundedCornerShape(20.dp)`, h=48, Slate 900 fill, white text | `ui/components/ModernButtons.kt:72-83` |
| Secondary glass button | `RoundedCornerShape(20.dp)`, h≈48, glass fill | `ui/components/ModernButtons.kt:153-170` |
| Pill chip / "AI Sync" badge | `RoundedCornerShape(20.dp)`, fill `Color.White alpha 0.8f`, border `Color.Black alpha 0.08f`, 8dp dot | `ui/components/PageHeader.kt:60-87` |
| Text-color hierarchy | `Color(0xFF0F172A)` body, `Color.Black.copy(alpha = 0.5f)` secondary, `0.4f` tertiary | repeated ~80× |
| Ambient bg | `AuraBackground` (green/blue/cyan blobs + slate-50 bottom gradient) | `ui/components/AuraBackground.kt:24-83` |

### 1.2 Inconsistencies found

| # | Where | Deviation from spec |
|---|---|---|
| I-1 | `ActivityScreen.kt:169` (re-implemented header) | Header is built **inline** with `Text(...,40sp,letterSpacing (-2).sp,Color(0xFF0F172A))` instead of using `PageHeader`. Same pattern in `NutritionScreen.kt:140-167`. |
| I-2 | `NutritionScreen.kt:150-153` and many chips/dialogs in `ProfileScreen.kt` | Uses `RoundedCornerShape(12.dp)` for `OutlinedTextField` while spec family is 20-30 dp. Mixing 10/12/20/24/28/30 dp radii for visually equivalent surfaces. |
| I-3 | `ProfileScreen.kt:430, 555, 663, 789` (all four `AlertDialog`s) | `containerColor = Color.White` (no translucency) + `RoundedCornerShape(24.dp)` while the rest of the app uses 78 % glass fill. The dialogs look pasted-in. |
| I-4 | `ProfileScreen.kt:286-301` and `AuthScreen.kt:520-560` | Segmented gender/activity pickers re-implemented with `RoundedCornerShape(12.dp)`, `Color(0xFF0F172A)` background and 0.03/0.08 alphas. Should be a shared `SegmentedPicker` DS component. |
| I-5 | `ProfileScreen.kt:497-511` (achievement row) | Builds its own shadow + fill + border + radius 12dp. Could use `GlassCardInset` from `ui/components/ds/GlassCardVariants.kt:120-144` (radius 20dp — close enough). |
| I-6 | `ProfileScreen.kt:94` (avatar circle) | Uses `RoundedCornerShape(28.dp)` for an 80dp square — almost-circular. Other "icon tile" patterns use 20dp. |
| I-7 | `HydrationDetailScreen.kt:96-101` and `MeditationScreen.kt:95-97` | Hand-roll a "frosted card" with `RoundedCornerShape(28.dp)`, `Color.White alpha 0.78f`, `1.dp / Black alpha 0.07f`. **Identical to GlassCard signature values** but written inline. |
| I-8 | `DashboardScreen.kt:414-417, 477-479` (insight-color lookup) | Inline `Color(0xFFFEF3C7)` `Color(0xFFCFFAFE)` pastels for badges — fine semantically, but no token; same pastels duplicated in `ProfileScreen.kt:589-590` (`0xFFECFDF5`/`0xFFA7F3D0`). Promote to `AccentPastel.*` in `Color.kt`. |
| I-9 | `MeditationScreen.kt:347` | Uses `Text("✕", ...)` raw glyph for close instead of `Icon(Icons.Default.Close, …)`. |
| I-10 | `MeditationScreen.kt:78-82`, `HydrationDetailScreen.kt:80-83` | Use the new `PageHeader` correctly — good. But both still use a hard-coded 40dp round back button. Make it a `BackHeader` variant or a parameter on `PageHeader`. |
| I-11 | `CircadianScreen.kt:633-635` | Sleep-quality color thresholds use `Color(0xFF4CAF50)` (Material green) and `Color(0xFFD32F2F)` (Material red) which are *off-palette* vs `AccentGreen`/`Tertiary`. |
| I-12 | `AnalyticsScreen.kt:865`, `MetricDetailScreen.kt` | Defines `val accentAmberColor = Color(0xFFFFB300)` local to the file, while `Color.kt` has `AccentAmber = 0xFFF59E0B`. Two amber tokens. |

### 1.3 Typography (sp / weights)

The kicker/title pair from `PageHeader.kt` is the only *enforced* header style.
Other notable type usage (no central token):

- `displaySmall.copy(fontSize = 20.sp, letterSpacing = (-0.8).sp)` — ProfileScreen.kt:109-113 (hero name)
- `fontSize = 13.sp, fontWeight = Bold, letterSpacing = 0.34.sp` — ad-hoc in `ProfileScreen.kt:596-598`
- `fontSize = 9.sp` — `ProfileScreen.kt:328, 630`, `ActivityScreen.kt` — tiny labels (probably accessibility risk)

---

## 2. Theme Color Usage Audit

### 2.1 Hard-coded `Color(0xFFXXXXXX)` literals per screen

Counts from `search_files pattern="Color\(0xFF[0-9A-Fa-f]{6}\)"` over `ui/screens/`:

| Rank | File | Hard-coded literals |
|---|---|---|
| 1 | `C:\Users\rk107\wellbeing_firebase\app\src\main\java\com\example\vitaai\ui\screens\NutritionScreen.kt` | 36 |
| 2 | `C:\Users\rk107\wellbeing_firebase\app\src\main\java\com\example\vitaai\ui\screens\ActivityScreen.kt` | 29 |
| 3 | `C:\Users\rk107\wellbeing_firebase\app\src\main\java\com\example\vitaai\ui\screens\DashboardScreen.kt` | 27 |
| 4 | `C:\Users\rk107\wellbeing_firebase\app\src\main\java\com\example\vitaai\ui\screens\ProfileScreen.kt` | 23 |
| 5 | `C:\Users\rk107\wellbeing_firebase\app\src\main\java\com\example\vitaai\ui\screens\HydrationDetailScreen.kt` | 18 |
| 6 | `C:\Users\rk107\wellbeing_firebase\app\src\main\java\com\example\vitaai\ui\screens\AnalyticsScreen.kt` | 16 |
| 7 | `C:\Users\rk107\wellbeing_firebase\app\src\main\java\com\example\vitaai\ui\screens\AuthScreen.kt` | 12 |
| 8 | `C:\Users\rk107\wellbeing_firebase\app\src\main\java\com\example\vitaai\ui\screens\SessionScreen.kt` | 11 |
| 9 | `C:\Users\rk107\wellbeing_firebase\app\src\main\java\com\example\vitaai\ui\screens\MetricDetailScreen.kt` | 11 |
| 10 | `C:\Users\rk107\wellbeing_firebase\app\src\main\java\com\example\vitaai\ui\screens\CircadianScreen.kt` | 9 |
| 11 | `C:\Users\rk107\wellbeing_firebase\app\src\main\java\com\example\vitaai\ui\screens\ChatScreen.kt` | 9 |
| 12 | `C:\Users\rk107\wellbeing_firebase\app\src\main\java\com\example\vitaai\ui\screens\MeditationScreen.kt` | 7 |

**Total: 208 hard-coded color literals across screens.**

The most repeated literal is `Color(0xFF0F172A)` (slate-900 "ink" colour) which
appears 80+ times; should be exposed as `OnSurface`/`InkPrimary` token and
re-imported. `Primary` (`0xFF06B6D4`) is duplicated 17 times and `Tertiary`
(`0xFFF43F5E`) at least 4 times in custom lookup tables (e.g.
`AnalyticsScreen.kt:767-774`, `MetricDetailScreen.kt:357-364`) that should
become a shared `MetricPalette` object.

### 2.2 Worst offenders by density (literal per kLOC)

`AuthScreen.kt` (733 lines, 12 literals) and `ChatScreen.kt` (505 lines,
9 literals) are dense per file. `SessionScreen.kt:75, 110` uses **Material
defaults** (`0xFF00E676`, `0xFFFF3D00`, `0xFFFFB300`) that are *off-palette*
relative to the rest of the app.

---

## 3. Component Reuse Audit

| Screen | Path | `AuraBackground` | `PageHeader` | `GlassCard` | `GlassCardGlow` | `ApexCard` | `ActionRow` | `ProgressRing` | `GlowButton*` | Reimplements inline |
|---|---|---|---|---|---|---|---|---|---|---|
| Dashboard | `screens/DashboardScreen.kt` | ✅:105 | ❌ (inline) | ✅:518,672 | ✅:518 | – | – | – | ❌ | Pull-to-refresh, info pills, weekly chip rows, stat tiles |
| Activity | `screens/ActivityScreen.kt` | ✅:81 | ❌ inline:150-171 | ✅: multiple | ✅: 246, 338, 386 | – | ✅:470, 949, 996, 1070 | ✅:700 | – | Custom category chips, session tiles, custom-protocol dialog |
| Nutrition | `screens/NutritionScreen.kt` | ✅ | ❌ inline header:140-167 | ✅: multiple | – | – | – | – | – | Macro rings, search bar, drink tile, water capsule |
| Analytics | `screens/AnalyticsScreen.kt` | ✅:66 | ✅:140 | ✅: multiple | – | – | – | – | – | Timeframe chips, color lookup table duplicated here and in `MetricDetailScreen.kt` |
| Circadian | `screens/CircadianScreen.kt` | ✅ | – | ✅: 196, 274 | ✅: 177 | – | – | – | – | Sleep-quality colour thresholds, dial, custom Switch |
| Profile | `screens/ProfileScreen.kt` | ✅:68 | ✅:78 | ✅:83 (hero only) | – | – | ✅: 136, 145, 161, 171, 181, 191 | – | – | Hero card, segmented pickers, 4 `AlertDialog`s |
| Chat | `screens/ChatScreen.kt` | ✅ | ✅:76 | – | – | – | – | – | – | Message bubbles, input row, typing dots |
| Auth | `screens/AuthScreen.kt` | – | – | – | – | – | – | – | – | Entire screen — should be wrapped in `AuraBackground` for visual consistency with the rest of the app |
| History | `screens/HistoryScreen.kt` | ✅ | – | – | – | – | – | – | – | – |
| Session | `screens/SessionScreen.kt` | ✅ | – | – | – | – | – | – | – | Close button, status pill |
| MetricDetail | `screens/MetricDetailScreen.kt` | ✅ | – | ✅: 113 | – | – | – | – | – | "Insight" list rows |
| Meditation (new) | `screens/MeditationScreen.kt` | ✅:54 | ✅:78 | – | – | – | – | – | – | Hero card, list tile, back button |
| HydrationDetail (new) | `screens/HydrationDetailScreen.kt` | ✅:57 | ✅:80 | – | – | – | – | – | – | Hero card, +/- buttons, time-row — **identical to GlassCard pattern** |

### 3.1 Top refactor candidates (most reuse value, lowest risk)

1. **`HydrationDetailScreen.kt:96-101`** and **`MeditationScreen.kt:95-97`** — these are *literally* `GlassCard` with default params, written out by hand. Replace with `GlassCard {}` — saves ~30 LOC, fixes I-7.
2. **`ProfileScreen.kt` segmented pickers:282-301 and 309-332** — exact duplicate of `AuthScreen.kt:514-565`. Extract a `SegmentedPickerRow<T>(items, selected, onSelect)` DS component; saves ~80 LOC, fixes I-4.
3. **`ActivityScreen.kt:150-171` & `NutritionScreen.kt:140-167`** — the page-header reimplementation. Replace with `PageHeader(title=..., kicker=...)`; saves ~40 LOC, fixes I-1.
4. **`AnalyticsScreen.kt:767-774` & `MetricDetailScreen.kt:357-364`** — identical metric-color lookup. Extract `MetricPalette.colorFor(title: String): Color` to a new `ui/components/ds/MetricPalette.kt`.
5. **`ProfileScreen.kt` four `AlertDialog`s:220, 465, 566, 679** — all use `Color.White` solid + `RoundedCornerShape(24.dp)`. Wrap in a `VitaDialog(...)` DS helper that uses glass fill + 30dp radius; fixes I-3 and gives consistent dialog look.

---

## 4. ProfileScreen Redesign Proposal

**Current state:** `C:\Users\rk107\wellbeing_firebase\app\src\main\java\com\example\vitaai\ui\screens\ProfileScreen.kt` is **793 lines**, contains:

- 1 root `ProfileScreen()` (52-206) with 4 dialog `if` flags (47-50).
- 4 dialog composables inline (220, 455, 559, 667) — each is its own `AlertDialog` with bespoke styling.
- One 80-line `AchievementsDialog` (455-557) that re-builds a chip + card + label stack.

### 4.1 New file structure

```
ui/screens/profile/
  ProfileScreen.kt           (~80 lines, just orchestration)
  ProfileHeroSection.kt      (~110 lines)
  ProfileIdentitySection.kt  (~80 lines)  ← contains Name field
  ProfileGoalsSection.kt     (~60 lines)
  ProfileSyncSection.kt      (~90 lines)
  ProfileAccountSection.kt   (~70 lines)
  ProfileDangerZone.kt       (~40 lines)
  dialogs/
    BiometricCalibrationDialog.kt  (~120 lines)
    AchievementsDialog.kt          (~110 lines)
    HealthConnectSyncDialog.kt     (~90 lines)
    LinkAccountDialog.kt           (~110 lines)
```

### 4.2 Top-level composition

```kotlin
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var dialog by remember { mutableStateOf<ProfileDialog?>(null) }

    AuraBackground {
        LazyColumn(
            contentPadding = PaddingValues(20.dp, 20.dp, 20.dp, 100.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { PageHeader("Profile", "Personalization") }
            item { ProfileHeroSection(state, onEditName = { dialog = ProfileDialog.EditName }) }
            item { ProfileIdentitySection(state, onCalibrate = { dialog = ProfileDialog.Biometric }) }
            item { ProfileGoalsSection(state, onEdit = { dialog = ProfileDialog.Biometric }) }
            item { ProfileSyncSection(state, onSync = { dialog = ProfileDialog.Sync }) }
            item { ProfileAccountSection(state, onLink = { dialog = ProfileDialog.Link }) }
            item { ProfileDangerZone(onLogout = { viewModel.logout { navController.navigate("auth") { popUpTo(0) } } }) }
        }
    }

    // Single dialog slot — replaces 4 boolean flags
    when (val d = dialog) {
        is ProfileDialog.EditName    -> NameEditDialog(state.displayName, viewModel::updateName, onDismiss = { dialog = null })
        is ProfileDialog.Biometric   -> BiometricCalibrationDialog(state, viewModel::calibrate, onDismiss = { dialog = null })
        is ProfileDialog.Achievements-> AchievementsDialog(onDismiss = { dialog = null })
        is ProfileDialog.Sync        -> HealthConnectSyncDialog(state, viewModel::syncToCloud, onDismiss = { dialog = null })
        is ProfileDialog.Link        -> LinkAccountDialog(viewModel, onDismiss = { dialog = null })
        null -> Unit
    }
}

sealed interface ProfileDialog { object EditName; object Biometric; object Achievements; object Sync; object Link }
```

### 4.3 Section-by-section responsibilities

| Section | Composes | State needed from `ProfileUiState` | Methods called on ViewModel |
|---|---|---|---|
| `ProfileHeroSection` | `GlassCardElevated` + 80dp avatar + display name + sync status | `permissionsGranted`, `isLoggedIntoFirebase` | `updateName(new: String)` (new) |
| `ProfileIdentitySection` | Achievements row + 4 biometric sub-tiles (Age, Gender, Activity, Weight/Height) | `age`, `gender`, `activityLevel`, `weightKg`, `heightMeters` | `onEdit()` → opens Biometric dialog |
| `ProfileGoalsSection` | 2×2 `StatChip` grid (steps, water, exercise, cal-burn) — values + progress bars | `stepGoal`, `hydrationGoalLiters`, `exerciseMinutesGoal`, `caloriesBurnGoal` | `onEdit()` → same dialog |
| `ProfileSyncSection` | `ActionRow(Health Connect)`, "Last sync at …" | `permissionsGranted`, `lastSyncStatus`, `isSaving` | `getRequestedPermissions()`, `onSync()` → opens Sync dialog |
| `ProfileAccountSection` | `ActionRow(Link email)` (only if `isAnonymous`), `ActionRow(Email/Password)` | `isLoggedIntoFirebase` | `onLink()` → opens Link dialog |
| `ProfileDangerZone` | `ActionRow(Log out)` styled in `TertiaryContainer` | – | `logout(onSuccess)` |

### 4.4 VM changes required

Add to `ProfileViewModel.kt`:

```kotlin
val displayName: StateFlow<String>  // from auth.displayName ?: "Alex"
// New:
fun updateName(new: String)         // updates FirebaseAuth profile + uiState
fun observeDisplayName()            // initial load alongside loadProfile()
```

Currently the name is **read inline in the screen** from
`FirebaseAuth.getInstance().currentUser` (line 53-55) — that should move into
the ViewModel and become part of the `uiState`.

---

## 5. Name-Input UX — Two Options

### Option A — Tap-to-edit pencil on the Hero card (recommended)

```
┌──────────────────────────────────────────────┐
│ ┌────┐  Alex                       ✎  edit  │
│ │ 👤 │  Health Connect synced · Premium trial │
│ └────┘                                        │
└──────────────────────────────────────────────┘
```

- **Pros**
  - Zero clicks to *view*; one tap to *edit*.
  - Edit can be inline (TextField replaces the label, autosave on blur), or
    a small modal that mirrors the existing `AlertDialog` pattern.
  - Discoverable: pencil icon is the universal "edit" affordance.
  - Reuses the existing `ProfileHeroSection` — minimal layout cost.
- **Cons**
  - Editing your own name from a "settings" page is slightly unusual; users
    may not realise the pencil is tappable without a label.
  - A misclick on the hero card (currently non-clickable) might confuse — we
    need to mark the pencil as the only clickable target.

### Option B — First-run modal sheet, hidden in IdentitySection after that

- Trigger: `if (state.displayName == null || displayName.isBlank())` on first
  composition, show a `ModalBottomSheet` with a single `OutlinedTextField` and
  a "Continue" CTA.
- After save, write to FirebaseAuth and persist in `ProfileUiState`.
- Subsequent edits live behind a *non-hero* "Display name" row inside
  `ProfileIdentitySection`.

- **Pros**
  - Onboarding is the most natural moment to ask.
  - Doesn't crowd the hero with chrome.
  - Matches Material guidelines (collect profile info at sign-in).
- **Cons**
  - Two paths to edit later (modal vs. inline) is harder to keep in sync.
  - Anonymous users have no first-run moment — the sheet would have to be
    triggered on permission grant or first goal set.

**Recommendation: Option A** for v1 (less code, fewer state machines) and
revisit B if user research shows the pencil is missed. To mitigate the
discoverability concern, animate the pencil with a single 1-shot pulse on
first composition of the screen.

---

## 6. Cross-Screen Data Flow Map

```
                ┌────────────────────────┐
                │  HealthConnectManager  │  (room + Health Connect)
                │  WorkoutRepository     │  (Room)
                │  GoalsRepository       │  (in-memory StateFlow)
                │  VitaRepository        │  (room)
                │  FirebaseRepository    │  (cloud)
                │  FirebaseAuth          │
                └─────────┬──────────────┘
                          │ (injected via Hilt)
   ┌──────────────┬───────┴──────┬───────────────┬───────────────┐
   ▼              ▼              ▼               ▼               ▼
DashboardVM   ActivityVM    NutritionVM     AnalyticsVM    ProfileVM
   │              │              │               │               │
   │  steps,     │  templates,  │  macros,      │  hourly steps,│  weight, height,
   │  calories,  │  sessions    │  hydration    │  hourly HR,   │  goals, sync,
   │  sleep,     │              │               │  hydration    │  auth user
   │  hr,        │              │               │  series,      │
   │  nutrition, │              │               │  daily steps  │
   │  workouts   │              │               │               │
   ▼              ▼              ▼               ▼               ▼
Dashboard      Activity       Nutrition        Analytics       Profile
(reads its     (reads Dash    (reads Dash      (independent    (independent
 own VM)       VM for snap    VM for snap)     re-aggregation) re-fetch)
               fallback)
```

### 6.1 Sync candidates (same data, computed differently)

| Metric | Where it diverges | Risk |
|---|---|---|
| **Steps today** | `DashboardViewModel` → `HealthSnapshot.steps` used in `ActivityScreen.kt:212, 240` (via shared VM instance — OK). `AnalyticsViewModel` recomputes from a 7-day rolling series (`AnalyticsViewModel.kt:269`) and shows the **day-bucket sum** (`:236`). | If `HealthSnapshot.steps` is updated after a `refresh()`, the day-bucket total in Analytics may not match. |
| **Hydration today (ml)** | `NutritionViewModel` uses `NutritionSummary.hydrationMl`; `HydrationDetailViewModel` reads `state.todayMl` from a **different** source (VitaDao or HealthConnect water logs) — see `HydrationDetailScreen.kt:53`. The goal denominator also differs: `goalsRepository.hydrationGoalLiters` (2.5 L) vs. `state.goalMl` in `HydrationDetail`. | Could display 1100 ml on Nutrition, 1500 ml on HydrationDetail for the same day. |
| **Active calories** | `DashboardScreen.kt` reads `snapshot.calories`; `AnalyticsViewModel.kt` builds its own time-series. `MetricDetailViewModel.kt:200` formats `snapshot.calories` against `10000` step goal — **wrong goal**, should be `caloriesBurnGoal` from `ProfileViewModel`. | The "Goal Progress" stat on the steps metric detail is hard-coded to 10 000, ignoring the user-set `stepGoal`. **This is a real bug.** |
| **Sleep hours** | `DashboardScreen.kt` → `snapshot.sleepDurationHours`; `AnalyticsViewModel` aggregates per-night from Health Connect. `CircadianViewModel` recomputes debt from raw sessions (`CircadianScreen.kt:226`). | Possible off-by-one at midnight boundary. |
| **Workout count** | `ProfileScreen.kt:90` reads `workoutRepository.observeRecentSessions(limit = 50).first().size` — capped at 50, so Level calculation (`ProfileViewModel.kt:93`) is wrong for users with 51+ workouts. | Level plateaus at 110 forever. |
| **Step goal denominator** | `ProfileScreen.kt:174` (calibration dialog) shows `stepGoal`; `DashboardScreen.kt` (line ~?) and `MetricDetailViewModel.kt:200` both still use the **literal** `10000f` / `10000.0`. | Three places, one constant; refactor needed. |

### 6.2 Recommended consolidation

1. **Single source of truth for today's snapshot:** keep `DashboardViewModel`
   as canonical, but have other VMs `collectAsState` on it via
   `hiltViewModel<DashboardViewModel>()` (which `ActivityScreen` already does
   at line 60).
2. **Promote `HealthSnapshot` getters to a `TodaySummary` object** in
   `data/` that exposes `steps`, `activeCalories`, `hydrationMl`,
   `sleepHours`, `avgHr` — with all denominators sourced from
   `GoalsRepository` (which is already a `StateFlow`).
3. **Bug fix for `MetricDetailViewModel.kt:200`** — change literal `10000.0`
   to `goalsRepository.stepGoal` value.

---

## Appendix — Quick-Reference File Paths

- Design system components: `C:\Users\rk107\wellbeing_firebase\app\src\main\java\com\example\vitaai\ui\components\`
- New DS variants: `C:\Users\rk107\wellbeing_firebase\app\src\main\java\com\example\vitaai\ui\components\ds\`
- Theme tokens: `C:\Users\rk107\wellbeing_firebase\app\src\main\java\com\example\vitaai\ui\theme\Color.kt`
- Backgrounds: `C:\Users\rk107\wellbeing_firebase\app\src\main\java\com\example\vitaai\ui\components\AuraBackground.kt`, `…\ShaderBackground.kt`
- Screens audited: `C:\Users\rk107\wellbeing_firebase\app\src\main\java\com\example\vitaai\ui\screens\`
- Profile (target of redesign): `C:\Users\rk107\wellbeing_firebase\app\src\main\java\com\example\vitaai\ui\screens\ProfileScreen.kt`, `…\ProfileViewModel.kt`

