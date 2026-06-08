# Firebase Profile Sync & Data Architecture — VitaAI Research Report

**Project:** `com.example.vitaai` (Android, Jetpack Compose, Hilt, Room, Firestore)
**Firebase project:** `wellbeing-app-45862`
**Repository root:** `C:\Users\rk107\wellbeing_firebase`
**Scope:** Read-only research. No code changes proposed here — only an architectural plan with concrete file touches.

---

## 1. Current Firebase state

### 1.1 What `FirebaseRepository` already does

Source: `C:\Users\rk107\wellbeing_firebase\app\src\main\java\com\example\vitaai\data\FirebaseRepository.kt` (310 lines).

| Method | Collection path | Doc id | Notes |
|---|---|---|---|
| `saveHealthSnapshot(snapshot)` | `users/{uid}/snapshots/{timestamp}` | `snapshot.timestamp` (Long) | Writes the whole `HealthSnapshot` POJO via `set(snapshot)`. |
| `getLatestSnapshot()` | same path, `orderBy("timestamp", DESC).limit(1)` | — | Returns latest or `null` on any exception. |
| `saveProfileGoals(...)` | `users/{uid}` (root doc) | uid | Writes a `Map<String, Any>` with 7 goal fields — **clobbers the whole document** (see §1.3). |
| `getProfileData()` | `users/{uid}` | uid | Returns `Map<String, Any>?` from `doc.data`. |
| `uploadLocalData(dao)` | `users/{uid}/{food_entries, drink_entries, sleep_sessions, workout_sessions, exercise_sets, ambient_light_logs}/{tsMillis}` | `entry.timestampMillis` | Manual fan-out — one `.set().await()` per row. |
| `downloadCloudData(dao)` | same 6 sub-collections | tsMillis | Reconstructs entities from `doc.getX(...)` with defaults; skips rows already present locally (existence check by `timestampMillis` range). |

### 1.2 Fields the existing code covers

- **Goals document (`users/{uid}`):** `age`, `gender`, `activityLevel`, `stepGoal`, `hydrationGoalLiters`, `exerciseMinutesGoal`, `caloriesBurnGoal`.
- **HealthSnapshot (see `VitaRepository.kt` line 18):** `steps`, `avgHeartRate`, `sleepDurationHours`, `calories`, `basalCalories`, `hydrationLiters`, `distanceMeters`, `exerciseMinutes`, `caloriesIntake`, `proteinGrams`, `carbsGrams`, `fatGrams`, `timestamp`, `hourlySteps` (Map), `hourlyHeartRate` (Map).
- **Synced log sub-collections:** `food_entries`, `drink_entries`, `sleep_sessions`, `workout_sessions`, `exercise_sets`, `ambient_light_logs`.

### 1.3 What is **missing**

1. **No `displayName` anywhere in Firestore.** `ProfileScreen.kt` line 55 hard-codes a fallback string:
   ```kotlin
   val userDisplayName = if (isAnonymous) "Guest Mode" else firebaseUser?.email ?: "Alex"
   ```
   This is computed locally from `FirebaseAuth.currentUser.email` — no Firestore round-trip, no custom name, no per-user display name resource.
2. **No `email` or `createdAt` field** persisted server-side; the only "email" the UI knows about comes from the auth token, which is empty for anonymous users.
3. **No per-user snapshot log feed independent of HealthSnapshot.** `users/{uid}/snapshots` exists, but it stores a single "today" roll-up; there is no event-level hydration or meditation log feed.
4. **No `meditation_log` collection** — `MeditationRepository.kt` keeps play events in-process `MutableStateFlow`s and is never persisted to Room or Firestore. A meditation session is forgotten on app kill.
5. **No `hydration_log` collection** — `HydrationRepository.kt` likewise uses an in-memory `MutableStateFlow<HydrationState>` with a `seed()` function. `DrinkEntryEntity` in `VitaEntities.kt` is the only persistence, and `uploadLocalData` writes it under `drink_entries` (different shape, nutrition-oriented, not hydration-bucketed by hour).
6. **No `sync_meta` document** — there is no record of `lastSyncedAt`, `deviceId`, or `schemaVersion`, so we cannot detect schema drift or implement delta sync.
7. **`saveProfileGoals` is destructive** — it does `document(uid).set(profileData)`, which will erase any future `displayName`, `email`, `createdAt`, etc. once we add them. This is the single biggest correctness bug for the new design (see §2.1 and §5).
8. **No offline-aware write queue** — every `await()` call throws if offline; callers (e.g. `AuthViewModel.saveProfileSettingsAndSync`) wrap the whole thing in a single `try/catch` and report a generic error.

---

## 2. Proposed Firestore data model

All paths are rooted at the authenticated `uid` so the existing Firestore security rules (which by default deny cross-user reads on `users/{uid}/...`) remain valid.

### 2.1 `users/{uid}` — User profile (single document)

| Field | Firestore type | Source today | Notes |
|---|---|---|---|
| `displayName` | `string` | none (new) | 1–40 chars; trimmed; defaults to `""` for anonymous users. |
| `email` | `string \| null` | `auth.currentUser.email` | Synced at sign-in and on `linkWithCredential`. |
| `createdAt` | `timestamp` | new | Server timestamp set on first write. |
| `updatedAt` | `timestamp` | new | `FieldValue.serverTimestamp()` on every update. |
| `age` | `number` (int) | `GoalsRepository.age` | |
| `gender` | `string` | `GoalsRepository.gender` | "Male" / "Female" / "Other" / lowercase variants. |
| `activityLevel` | `string` | `GoalsRepository.activityLevel` | "Sedentary" / "Light" / "Active" / "Very Active". |
| `goals` | `map` | new container | Replaces the 4 flat fields currently on the root. |
| `goals.stepGoal` | `number` (long) | `DailyGoals.stepGoal` | |
| `goals.hydrationGoalLiters` | `number` (double) | `DailyGoals.hydrationGoalLiters` | |
| `goals.exerciseMinutesGoal` | `number` (double) | `DailyGoals.exerciseMinutesGoal` | |
| `goals.caloriesBurnGoal` | `number` (double) | `DailyGoals.caloriesBurnGoal` | |

**Migration note:** `saveProfileGoals` (FirebaseRepository.kt line 48) currently does a top-level `set(...)`. We must change it to `update(...)` or a targeted `FieldValue` merge to avoid clobbering the new fields. Backfill: read both the legacy flat fields and the new `goals` map on first launch and merge.

### 2.2 `users/{uid}/snapshots/{timestamp}` — HealthSnapshot

Document id is `snapshot.timestamp.toString()` — keep this convention (matches `FirebaseRepository.saveHealthSnapshot`).

| Field | Type | Source |
|---|---|---|
| `steps` | long | `HealthSnapshot.steps` |
| `avgHeartRate` | double | `HealthSnapshot.avgHeartRate` |
| `sleepDurationHours` | double | `HealthSnapshot.sleepDurationHours` |
| `calories` | double | `HealthSnapshot.calories` |
| `basalCalories` | double | `HealthSnapshot.basalCalories` |
| `hydrationLiters` | double | `HealthSnapshot.hydrationLiters` |
| `distanceMeters` | double | `HealthSnapshot.distanceMeters` |
| `exerciseMinutes` | double | `HealthSnapshot.exerciseMinutes` |
| `caloriesIntake` | double | `HealthSnapshot.caloriesIntake` |
| `proteinGrams` | double | `HealthSnapshot.proteinGrams` |
| `carbsGrams` | double | `HealthSnapshot.carbsGrams` |
| `fatGrams` | double | `HealthSnapshot.fatGrams` |
| `hourlySteps` | map<string, long> | `HealthSnapshot.hourlySteps` |
| `hourlyHeartRate` | map<string, double> | `HealthSnapshot.hourlyHeartRate` |

No schema change vs. today — the existing `set(snapshot)` works because the `HealthSnapshot` POJO's field names already match. The compatibility risk is that the existing doc-ids collide with any future re-uploads, but since `set` is idempotent on identical content that's fine.

### 2.3 `users/{uid}/meditation_log/{docId}` — MeditationSession play events

Document id: `"{sessionId}-{startedAtMillis}"` (or just `UUID.randomUUID().toString()`; pick UUID and add an index on `startedAt`).

| Field | Type | Notes |
|---|---|---|
| `sessionId` | string | From `MeditationSession.id` (e.g. `"m1"` … `"m5"`). |
| `title` | string | "Morning Calm", "Focus Flow", etc. |
| `category` | string | "Calm" / "Focus" / "Relaxation" / "Sleep" / "Energy". |
| `plannedDurationSeconds` | number (long) | `MeditationSession.durationMinutes * 60`. |
| `startedAt` | timestamp | Wall-clock when `viewModel.play()` ran. |
| `timestamp` | timestamp | Alias kept for symmetry with other log sub-collections. |
| `durationSeconds` | number (long) | Actual seconds listened (computed on stop). |
| `completed` | boolean | `true` iff user listened for `>= plannedDurationSeconds` (or some threshold, e.g. 80%). |
| `deviceId` | string | From `sync_meta.deviceId` — useful for cross-device dedup. |

Source change: `MeditationViewModel.kt` (currently 40 lines, only `play`/`togglePlayback`/`stop`/`refresh`) needs to emit a "session ended" event. Hook into the existing `stop()` call from `MeditationScreen.kt` line 134.

### 2.4 `users/{uid}/hydration_log/{docId}` — HydrationEntry

Document id: `"{yyyyMMdd-HH}-{ml}-{uuid}"` or just `UUID.randomUUID().toString()` (recommended for write concurrency).

| Field | Type | Notes |
|---|---|---|
| `timestamp` | timestamp | When the user added water. |
| `ml` | number (double) | Volume added in this single event. |
| `hour` | number (int) | 0–23, denormalized so range queries don't need timestamp extraction. |
| `source` | string | "manual" / "quickAdd" / "undo" (for auditability). |

Hook: `HydrationRepository.addWater()` (line 29) and `removeLastEntry()` (line 49) must each emit a corresponding cloud write. `removeLastEntry` is tricky — the current state has no "id" on `LogEntry`, so we need to add a UUID to `LogEntry` so we can pass a doc id for deletion. Recommend adding a `data class LogEntry(val id: String = UUID.randomUUID().toString(), val hour: Int, val ml: Int, val timestamp: Long = System.currentTimeMillis())`.

### 2.5 `users/{uid}/sync_meta` — single doc

| Field | Type | Notes |
|---|---|---|
| `lastSyncedAt` | timestamp | Server timestamp of the most recent successful push or pull. |
| `deviceId` | string | `Settings.Secure.ANDROID_ID` (stable per app+device). |
| `schemaVersion` | number (int) | Start at `1`. Bump on any breaking change; client refuses to read older data without running a migration. |
| `lastUserAgent` | string | "android-vitaai-1.0" for diagnostics. |

This is the cheapest possible bookkeeping — one write per sync — and unlocks delta sync, conflict detection, and crash-safe resumability later.

---

## 3. Sync strategy

**Recommendation: push-on-write with debounce, plus pull-on-launch with last-write-wins (LWW) on the goals/profile document and union-on-`docId` for log sub-collections.**

Justification (2–3 sentences): Health data is append-mostly and high-frequency (hydration taps, meditation start/stop), so per-keystroke pushes are wasteful — a 5-second debounced `Channel<SyncOp>` in `FirebaseRepository` collapses bursts while still feeling instant. For pulls, a single full sync at `onStart()` of `MainActivity` (or after `signIn` / `linkWithCredential`) is enough because Firestore's offline cache will return the last known state instantly; conflict resolution only matters on the `users/{uid}` profile doc, where LWW keyed by `updatedAt` is the right call since the user can only meaningfully edit goals on one device at a time. Log sub-collections are effectively write-once per event, so we just union on `docId` — no conflict possible if we use UUIDs for the doc id.

Alternative rejected: **server-wins on pull** was considered for goals, but losing a 30-second-old goal edit because another device polled 10 seconds later is a worse UX than the (tiny) chance of a merge anomaly in LWW.

---

## 4. Profile / name input UX

### 4.1 Current state in `ProfileScreen.kt`

- The **Profile Hero Card** (lines 82–128) shows `userDisplayName` — currently a local string derived from `firebaseUser.email` (line 55). There is no tap-to-edit affordance on the name itself.
- The "**Biometric Settings**" `ActionRow` (line 161) opens the `AlertDialog` titled "Biometric Calibration" (line 224). This dialog already has `OutlinedTextField`s for weight, height, age, gender, activity, and 4 goals — but **no `displayName` field**.
- The "**Link Email Account**" `ActionRow` (line 181) is shown only for `isAnonymous == true` and opens `LinkAccountDialog` (line 668), which collects email + password.
- There is **no first-run onboarding flow** anywhere — `MainActivity.kt` jumps straight to `"auth"` or `"dashboard"`.

### 4.2 Recommendation: **both, but split by trigger**

1. **First-run onboarding (one-time, after auth):** for brand-new accounts, intercept navigation in `AuthViewModel.register` / `loginAnonymously` / `login` and, if `users/{uid}.displayName` is empty, route to a new `OnboardingNameScreen` (one field: "What should we call you?", a `GlowButton` "CONTINUE"). The same screen can chain the existing biometric questions (age/gender/activity) which `AuthViewModel` already collects in `currentStep` flow (line 38).
2. **Profile screen top (always):** make the hero card's name (`ProfileScreen.kt` line 107) a tappable `Modifier.clickable { showNameDialog = true }` that opens a tiny one-field `AlertDialog` mirroring the "Link Email Account" dialog style. This is the same pattern as the existing "Biometric Settings" tile and is consistent with the current design language.

Rationale: onboarding prevents a "Guest Mode" placeholder from persisting (the bug today — `ProfileScreen.kt` line 55 falls back to `"Alex"` if there is no email), and the always-editable Profile tile handles the post-onboarding rename use case without forcing a separate "Settings → Name" navigation. Both are 1-field dialogs so the implementation cost is minimal and reuses the existing `OutlinedTextField` + `AlertDialog` pattern.

### 4.3 AuthProvider `displayName` integration

`FirebaseAuth.UserProfileChangeRequest.Builder().setDisplayName(name).build()` should be called alongside the Firestore write so `FirebaseUser.displayName` is non-null and the fallback chain in `ProfileScreen.kt` line 55 can be simplified to `auth?.currentUser?.displayName ?: auth?.currentUser?.email ?: "Guest"`.

---

## 5. Risks / unknowns

1. **`saveProfileGoals` clobbering the new `displayName` field.** Today it does `document(uid).set(profileData)`. Adding `displayName` to the same doc means a single call to `saveProfileGoals` will erase it. Fix: switch to `db.collection("users").document(uid).set(profileData, SetOptions.merge())` or split into per-field `update()`. This is the highest-priority issue. (`FirebaseRepository.kt` line 70.)
2. **Anonymous → linked account UX discontinuity.** `AuthViewModel.register` (line 84) calls `linkWithCredential` if `currentUser.isAnonymous`, and `ProfileViewModel.linkAccount` (line 194) does the same on a different path. Both code paths call `uploadLocalData` (FirebaseRepository line 85) but neither pulls the profile doc first — so a user who used the app on a different device and then linked locally could have their goals overwritten by the local defaults (`ProfileViewModel.logout` line 174–184 re-seeds 25/Male/Active/10000/2.5/30/500). Need a "pull-then-merge" before any link action.
3. **Offline queue / failed writes.** `AuthViewModel.saveProfileSettingsAndSync` and `ProfileViewModel.calibrate` wrap a single `try/catch` around the whole chain. If the user is offline and configures biometrics, the calibration is lost — the local Room write happens but the Firestore push silently fails. Mitigations: (a) use `FirebaseFirestore.enableNetwork()` checks and a pending-ops table in Room, or (b) at minimum show a non-blocking snackbar "Saved locally — will sync when online" and retry on `ConnectivityManager` callback. The Firestore Android SDK does have built-in offline persistence, so the risk is really "did the call complete" not "will the data ever arrive" — but the UI has no way to surface that today.
4. **HealthSnapshot field compatibility.** `set(snapshot)` writes all 14 fields unconditionally. If we add a new field (e.g. `vo2Max`) and a user with a 1-week-old `HealthSnapshot` doc re-syncs, Firestore will not delete the missing field on the existing doc — it just merges. But if we *rename* a field (`caloriesIntake` → `caloriesConsumed`) and a 6-month-old user upgrades, their `caloriesIntake` will linger forever and break downstream queries. Need `schemaVersion` on `sync_meta` and a one-shot migration that reads old fields and rewrites under new names.
5. **Meditation / hydration in-memory state loss on app kill.** `MeditationRepository` and `HydrationRepository` are pure `MutableStateFlow` holders with no Room backing. Any meditation session in progress or hydration entry not yet written is gone the moment the process dies. For hydration, `DrinkEntryEntity` in Room gives us partial durability (the `uploadLocalData` path exists) but the in-memory `LogEntry` list and `hourlyMask` derived from it will be wrong on next launch. For meditation, there is **zero** durability today. Adding `meditation_log` / `hydration_log` writes means we also need to introduce a Room table per log so writes don't disappear if the network is down (the Firestore SDK queues, but a 24h+ outage would still lose the local `LogEntry` because it's never persisted).

---

## 6. Concrete file-touch list

### 6.1 New files

| Absolute path | One-line purpose |
|---|---|
| `C:\Users\rk107\wellbeing_firebase\app\src\main\java\com\example\vitaai\data\model\UserProfile.kt` | `@Serializable` data class for `users/{uid}` (matches §2.1, including nested `goals` map). |
| `C:\Users\rk107\wellbeing_firebase\app\src\main\java\com\example\vitaai\data\model\MeditationLogEntry.kt` | POJO for `users/{uid}/meditation_log` (matches §2.3). |
| `C:\Users\rk107\wellbeing_firebase\app\src\main\java\com\example\vitaai\data\model\HydrationLogEntry.kt` | POJO for `users/{uid}/hydration_log` (matches §2.4). |
| `C:\Users\rk107\wellbeing_firebase\app\src\main\java\com\example\vitaai\data\model\SyncMeta.kt` | POJO for `users/{uid}/sync_meta` (matches §2.5). |
| `C:\Users\rk107\wellbeing_firebase\app\src\main\java\com\example\vitaai\data\local\MeditationLogDao.kt` | New Room DAO + `MeditationLogEntity` table for offline durability of meditation events. |
| `C:\Users\rk107\wellbeing_firebase\app\src\main\java\com\example\vitaai\data\local\HydrationLogDao.kt` | New Room DAO + `HydrationLogEntity` table mirroring §2.4 for offline durability. |
| `C:\Users\rk107\wellbeing_firebase\app\src\main\java\com\example\vitaai\data\ProfileRepository.kt` | Owns `displayName` / `email` state, exposes `Flow<UserProfile?>`, and writes the new `users/{uid}` doc with merge semantics. |
| `C:\Users\rk107\wellbeing_firebase\app\src\main\java\com\example\vitaai\data\SyncCoordinator.kt` | Push-on-write debounce channel + pull-on-launch orchestrator; consults `sync_meta`. |
| `C:\Users\rk107\wellbeing_firebase\app\src\main\java\com\example\vitaai\ui\screens\OnboardingNameScreen.kt` | New 1-field name screen for first-run (§4.2 item 1). |
| `C:\Users\rk107\wellbeing_firebase\app\src\main\java\com\example\vitaai\ui\screens\EditDisplayNameDialog.kt` | Reusable `AlertDialog` (matches the visual style of `LinkAccountDialog` in `ProfileScreen.kt` line 668). |

### 6.2 Files to modify

| Absolute path | Change |
|---|---|
| `C:\Users\rk107\wellbeing_firebase\app\src\main\java\com\example\vitaai\data\FirebaseRepository.kt` | (a) Replace `saveProfileGoals` (line 48) with a `merge`-safe `updateProfile`; (b) add `saveUserProfile` / `loadUserProfile` / `saveMeditationLog` / `saveHydrationLog` / `loadSyncMeta` / `saveSyncMeta`; (c) add a `syncOp` `Channel` for debounced push; (d) extend `downloadCloudData` to pull `meditation_log` and `hydration_log` (and the new `displayName`/`email`/`createdAt` fields on the root doc). |
| `C:\Users\rk107\wellbeing_firebase\app\src\main\java\com\example\vitaai\data\VitaRepository.kt` | No structural change; `HealthSnapshot` (line 18) stays the source of truth for the `snapshots/` collection. Consider adding `schemaVersion` to the doc payload. |
| `C:\Users\rk107\wellbeing_firebase\app\src\main\java\com\example\vitaai\data\MeditationRepository.kt` | Add `id: String` to `LogEntry` (line 24), persist to Room via new `MeditationLogDao`, push to Firestore on `stop()`. |
| `C:\Users\rk107\wellbeing_firebase\app\src\main\java\com\example\vitaai\data\HydrationRepository.kt` | Add `id` and `timestamp` to `LogEntry` (line 24), persist to Room via new `HydrationLogDao`, push to Firestore on `addWater` and `removeLastEntry`. |
| `C:\Users\rk107\wellbeing_firebase\app\src\main\java\com\example\vitaai\data\GoalsRepository.kt` | Add a `displayName: StateFlow<String>` mirror backed by `SharedPreferences` so the UI shows the name instantly even before Firestore round-trip. |
| `C:\Users\rk107\wellbeing_firebase\app\src\main\java\com\example\vitaai\data\local\VitaEntities.kt` | Add `MeditationLogEntity` and `HydrationLogEntity` (or place in dedicated files). |
| `C:\Users\rk107\wellbeing_firebase\app\src\main\java\com\example\vitaai\data\local\VitaDatabase.kt` | Bump `version = 3`, add the two new `@Entity` classes to the `entities` array, add a Room migration. |
| `C:\Users\rk107\wellbeing_firebase\app\src\main\java\com\example\vitaai\data\local\VitaDao.kt` | Add `@Insert/@Query` helpers for the new `LogEntity` tables (used by repositories above). |
| `C:\Users\rk107\wellbeing_firebase\app\src\main\java\com\example\vitaai\ui\screens\ProfileScreen.kt` | (a) Replace the `userDisplayName` fallback (line 55) with a `viewModel.displayName` flow; (b) make the hero card name (line 107) tappable to open `EditDisplayNameDialog`; (c) when `displayName` is empty + non-anonymous, nudge user to set one. |
| `C:\Users\rk107\wellbeing_firebase\app\src\main\java\com\example\vitaai\ui\screens\ProfileViewModel.kt` | Add `displayName` to `ProfileUiState` (line 21), inject `ProfileRepository`, add `updateDisplayName(name: String)`, and call `UserProfileChangeRequest` on `FirebaseAuth`. |
| `C:\Users\rk107\wellbeing_firebase\app\src\main\java\com\example\vitaai\ui\screens\AuthViewModel.kt` | (a) After `login` / `register` / `signInWithGoogleCredential` / `signInWithApple`, check `users/{uid}.displayName` and if empty navigate to the new `OnboardingNameScreen` instead of `"dashboard"`; (b) replace the flat-field profile write in `saveProfileSettingsAndSync` (line 185) with the new nested-`goals`-map call. |
| `C:\Users\rk107\wellbeing_firebase\app\src\main\java\com\example\vitaai\ui\screens\MeditationViewModel.kt` | On `stop()` (line 30 of VM, called from `MeditationScreen.kt` line 134), compute `durationSeconds` + `completed` and emit to the new `meditation_log` writer. |
| `C:\Users\rk107\wellbeing_firebase\app\src\main\java\com\example\vitaai\ui\screens\HydrationDetailViewModel.kt` | Already thin — no change needed beyond the underlying repository gaining persistence. |
| `C:\Users\rk107\wellbeing_firebase\app\src\main\java\com\example\vitaai\ui\MainActivity.kt` | Add a new `composable("onboarding/name")` route (around line 224) that renders `OnboardingNameScreen`. |

### 6.3 Files NOT to touch (deferred to future work)

- `C:\Users\rk107\wellbeing_firebase\app\src\main\java\com\example\vitaai\di\FirebaseModule.kt` — already provides both `FirebaseAuth?` and `FirebaseFirestore?` as `@Singleton` (lines 20 and 31), so the new `ProfileRepository` and `SyncCoordinator` can be `@Inject constructor`-ed without any DI changes.
- `C:\Users\rk107\wellbeing_firebase\app\google-services.json` — no change.
- Tracking services (`SleepTrackingService.kt`, `WorkoutTrackingService.kt`) — out of scope for this report; their data already flows through Room → `uploadLocalData`.
