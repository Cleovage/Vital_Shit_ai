# VitaAI upgrade research — 2026-06-14

Scope: current app at `C:\Users\rk107\wellbeing_firebase`, existing audits (`upgrades_audit.md`, `scratch/ui_consistency_audit.md`), live source inspection, and current platform docs reachable today.

## Sources checked

- Android Health Connect docs: `https://developer.android.com/health-and-fitness/guides/health-connect` and data types page.
- Android 16 docs: `https://developer.android.com/about/versions/16`.
- Firebase AI Logic docs: `https://firebase.google.com/docs/ai-logic` — page title confirmed: “Gemini API using Firebase AI Logic”.
- Firebase docs product index: confirms Firebase AI Logic, Genkit, App Distribution, Crashlytics, Analytics, In-App Messaging, Remote Config class products are available in the Firebase ecosystem.
- Gemini API docs: `https://ai.google.dev/gemini-api/docs`.
- Material Design 3 site: `https://m3.material.io/`.
- Current app dependencies: Compose BOM `2024.12.01`, Health Connect client `1.1.0`, Firebase BOM `32.7.0`, Retrofit/OpenRouter API, Room, DataStore, Glance widget.

## Highest-value upgrades for this app

### 1. Make VitaAI chat truly useful, not just a health-summary button

Current app already has `OpenRouterApi` + Retrofit wired (`NetworkModule.kt`, `OpenRouterApi.kt`), but the product problem is that chat can still feel generic/offline and today returned an HTTP 429 during testing. Upgrade path:

- Add a `ChatAiRepository` that sends the actual user prompt plus a compact health context pack: steps, sleep, hydration, calories, workout recency, goals, and display name.
- Add fallback routing: OpenRouter first, Firebase AI Logic / Gemini later, cached local template fallback if all network AI fails.
- Add “action proposals” that are already represented in UI (`LogNutrition`, `LogHydration`, `LogWorkout`) but should be backed by reliable parsing and confirmation.
- Add rate-limit UX: if API returns 429, show “AI is cooling down, but I can still log water/workouts locally” instead of a dead error bubble.

Why now: Firebase now has first-party “Gemini API using Firebase AI Logic” docs, so this can eventually sit closer to Firebase Auth/App Check/Analytics instead of relying only on a direct external key path.

### 2. Health Connect depth: use more data types and better sync status

The app already uses Health Connect permissions for steps, heart rate, sleep, calories, hydration, exercise, distance, and nutrition. Upgrade path:

- Add a Health Connect “connection quality” card: last successful read, permission gaps, and stale metrics.
- Build richer readiness from sleep + heart + activity + hydration instead of showing “NO DATA” too often.
- Add record-level explanations: “Sleep missing because permission not granted / no provider app / no records today”.
- Add a one-tap “fix Health Connect” flow from Profile and Dashboard.

This is high leverage because Health Connect is core to the wellness premise and the app already has the dependency and permissions.

### 3. Crashlytics + Analytics + Remote Config before more UI polish

The app has Firebase Auth/Firestore/Storage/Analytics, but no Crashlytics/Remote Config dependency in `app/build.gradle.kts`. Add:

- Crashlytics for real crash visibility on the Xiaomi/Poco device and future testers.
- Analytics events for chat send, action confirm/refuse, Health Connect sync, hydration log, workout start/finish, onboarding completion.
- Remote Config for AI provider/model, feature flags, and “show beta upgrade cards”.

This makes future upgrades measurable instead of guessing.

### 4. Fix performance hotspots in custom drawing components

Existing audit found repeated `Paint`, `Path`, `Brush`, gradient allocations in draw loops across canvas-heavy components (`CircadianClockDial`, `LuminousChart`, sloshing progress components, shader backgrounds). Upgrade path:

- Convert heavy `drawBehind`/`Canvas` allocation patterns to `drawWithCache`.
- Cache `Path`, `Paint`, gradient objects where size-dependent.
- Verify with simple before/after frame timing while dragging charts.

This matters because VitaAI’s visual style is premium/animated; smoothness is part of the product.

### 5. Nutrition upgrades: barcode/photo/manual meal flow

Current nutrition is mostly manual/static. Next upgrade:

- Add “quick add” recent foods and favorites.
- Add barcode/photo attachment entry points from the existing chat attach button.
- Let chat propose nutrition logs, then confirm into `NutritionRepository`.
- Improve category matching so custom foods are categorized by metadata/tags, not hardcoded exact names.

This connects nutrition, chat, and Firebase logs into one useful loop.

### 6. Firebase-backed personalization and cross-device continuity

Already started: profile display name and logs sync. Upgrade path:

- Sync chat settings and active goals per user.
- Add `users/{uid}/preferences` for units, AI tone, notification windows, dark mode preference.
- Add per-device metadata under `users/{uid}/devices/{deviceId}` for sync debugging.
- Add conflict-safe log syncing for workouts/nutrition if not already complete.

### 7. Design-system cleanup that gives visible polish fast

From `scratch/ui_consistency_audit.md`:

- Replace inline page headers in Activity/Nutrition with `PageHeader`.
- Extract shared segmented picker from Profile/Auth.
- Promote repeated colors (`InkPrimary`, accent pastels, metric palette) into theme tokens.
- Replace hand-rolled frosted cards in Meditation/HydrationDetail with `GlassCard`.
- Fix small touch targets (<48dp) and missing content descriptions.

This is less risky than a full redesign and will make screens feel like one app.

### 8. Workout/session reliability fixes

From `upgrades_audit.md` source inspection:

- Pause should stop GPS polling; resume should restart only if GPS mode is active.
- Non-cardio workout templates should clear `gpsEnabled` when category changes away from cardio.
- Move haptic/session timer side effects out of composables into ViewModel/service logic.
- Avoid DB reads inside lazy list row renderers; prefetch/session-with-sets.

These are product trust fixes, not just code cleanup.

## Recommended implementation order

1. Chat stability + AI prompt path + rate-limit fallback.
2. Crashlytics/Analytics/Remote Config instrumentation.
3. Health Connect permission/status UX.
4. Performance pass on canvas/draw components.
5. Nutrition quick-add + chat-confirmed food logs.
6. Design-system cleanup by screen.
7. Workout GPS/session reliability.

## Notes from today’s chat keyboard fix

The annoying chat input jump was caused by mixed IME handling. On this device, default Activity soft-input behavior was resizing the window while Compose `imePadding()` also applied keyboard height, causing double movement. The corrected pattern is:

- `MainActivity` manifest sets `android:windowSoftInputMode="adjustNothing"` so the OS does not resize underneath Compose.
- `ChatScreen` root uses `Modifier.imePadding()` as the single variable keyboard-size source.
- The input row does not use a fixed keyboard spacer; it only keeps `navigationBarsPadding()` for non-keyboard nav-bar safety.
- The small disclaimer hides while the keyboard is visible and the text field becomes compact.

This avoids a hardcoded bottom gap and adapts to different keyboard heights.