# VitaAI — Dependency Manifest for Firebase Sync + Profile Work

Read-only research of `C:\Users\rk107\wellbeing_firebase\app\build.gradle.kts` and the current `app/src` usage. The main agent should apply the changes in section 4 verbatim.

## 1. What's installed today

| Group | Library / Plugin | Version | Notes |
|---|---|---|---|
| Android Gradle Plugin | `com.android.application` | 8.9.1 | top-level |
| Kotlin | `org.jetbrains.kotlin.android` | 2.1.20 | top-level |
| Compose plugin | `org.jetbrains.kotlin.plugin.compose` | 2.1.20 | top-level |
| Hilt plugin | `com.google.dagger.hilt.android` | 2.55 | top-level + app |
| Google services | `com.google.gms.google-services` | 4.4.4 | top-level + app |
| Kapt | `kotlin-kapt` | (bundled) | app |
| Core | `androidx.core:core-ktx` | 1.12.0 | |
| Lifecycle | `androidx.lifecycle:lifecycle-runtime-ktx` | 2.7.0 | |
| Activity | `androidx.activity:activity-compose` | 1.8.2 | |
| Compose BOM | `androidx.compose:compose-bom` | **2024.02.00** | OLD — current is 2024.10.x / 2025.x |
| Compose UI / Material3 / icons / animation / fonts | (BOM-managed) | from BOM | |
| Navigation Compose | `androidx.navigation:navigation-compose` | 2.7.7 | |
| Material (views) | `com.google.android.material:material` | 1.12.0 | |
| Hilt runtime | `com.google.dagger:hilt-android` + compiler | 2.55 | |
| Hilt nav-compose | `androidx.hilt:hilt-navigation-compose` | 1.1.0 | |
| Health Connect | `androidx.health.connect:connect-client` | 1.1.0 | |
| Room | `androidx.room:room-runtime` + `-ktx` + compiler | 2.7.0-alpha13 | |
| Vico charts | `com.patrykandpatrick.vico:compose-m3` | 1.14.0 | |
| Retrofit + Gson | `retrofit` + `converter-gson` | 2.9.0 | |
| Firebase BOM | `com.google.firebase:firebase-bom` | 32.7.0 | |
| Firebase modules | `firebase-analytics-ktx`, `firebase-firestore-ktx`, `firebase-auth-ktx` | from BOM | |
| Play services auth | `com.google.android.gms:play-services-auth` | 21.1.0 | |
| Coroutines (play-services) | `kotlinx-coroutines-play-services` | 1.7.3 | |
| Glance | `glance-appwidget` + `glance-material3` | 1.0.0 | |
| Test | junit4, ext-junit, espresso, compose-test | (as listed) | |

**NOT installed (verified by full-text search of `app/src` and the gradle file):**
- `androidx.work:work-runtime-ktx` (no WorkManager usage anywhere in the codebase today)
- `androidx.datastore:datastore-preferences` (code uses raw `SharedPreferences` in 3 files: `GoalsRepository.kt`, `CircadianViewModel.kt`, `NutritionViewModel.kt`)
- `io.coil-kt:coil-compose` (no image-loading code today; no Coil or Glide imports)
- `com.google.firebase:firebase-storage-ktx` (no `FirebaseStorage` usage; `google-services.json` does declare `storage_bucket` but the SDK is not on the classpath)
- `org.jetbrains.kotlinx:kotlinx-serialization-json` (no `@Serializable`, no `Json.encode` calls)
- `org.jetbrains.kotlinx:kotlinx-datetime` (code uses `java.time.{LocalDate,Instant,ZonedDateTime}` everywhere)
- `coreLibraryDesugaring` (not configured in `compileOptions`)

**Indirect transitives likely already pulled in by Firebase BOM 32.7.0:**
- `kotlinx-coroutines-core` and `kotlinx-coroutines-android` (already used heavily via `Flow`, `delay`, `viewModelScope`)
- `kotlinx-coroutines-tasks` (via `kotlinx-coroutines-play-services`)

## 2. What's missing for the planned work

### a) Offline-first data sync (queue + retry)
- **WorkManager** — **MISSING.** Required for durable background retry of `FirebaseRepository.saveHealthSnapshot` when the network is down. Add to `app/build.gradle.kts`:
  ```kotlin
  implementation("androidx.work:work-runtime-ktx:2.9.1")
  implementation("androidx.hilt:hilt-work:1.1.0")         // HiltWorker integration
  kapt("androidx.hilt:hilt-compiler:1.1.0")               // generated HiltWorker factories
  ```
  Version 2.9.1 is stable with Kotlin 2.1.x and AGP 8.9.1.
- **kotlinx-coroutines** — **NOT MISSING.** Already pulled transitively by Firebase BOM and the app. `Flow`, `StateFlow`, `viewModelScope`, `delay` are all in use. No new line needed.
- **DataStore (for the *queue* itself, not the profile name)** — **OPTIONAL.** The current code has no persistent queue. For a durable retry queue you can use either a Room table (already present) or DataStore. The cleanest pattern: add a Room `pendingSync` table inside the existing `VitaDatabase` — no new dependency. **No new dep needed if you go Room.**

### b) Profile name persistence + reactive UI
- **DataStore-Preferences** — **MISSING for the *reactive* requirement.** The three existing call sites use `SharedPreferences`, which is not Compose-reactive (you'd have to hand-roll a `Flow` via `OnSharedPreferenceChangeListener`). For a single name + email + uid, DataStore-Preferences is the modern choice and gives you a `Flow<String>` for free. Add:
  ```kotlin
  implementation("androidx.datastore:datastore-preferences:1.1.1")
  ```
  This replaces the three raw `SharedPreferences` call sites over time (out of scope for this manifest, but flag it).
- **No new plugin required.** DataStore is plain AndroidX.

### c) Image upload (avatar) + display
- **Firebase Storage** — **MISSING.** `google-services.json` declares `storage_bucket = "wellbeing-app-45862.firebasestorage.app"`, so the project is configured for it, but the SDK is not on the classpath. Add:
  ```kotlin
  implementation("com.google.firebase:firebase-storage-ktx")
  ```
  No version needed — it's resolved by the Firebase BOM 32.7.0 already in the file.
- **Coil** — **MISSING** and the right pick for Compose. Add:
  ```kotlin
  implementation("io.coil-kt:coil-compose:2.6.0")
  ```
  2.6.0 is the last stable line and works with Compose BOM 2024.02.00's Compose UI version. Use `AsyncImage` for the avatar.
- **No plugin required** for Coil or Storage. No Internet permission change needed (already implied by Firestore + Auth).

### d) JSON serialization for snapshot payloads
- **kotlinx-serialization** — **NOT NEEDED.** `HealthSnapshot` is a `data class` with primitive fields and `Map<String, Long>` / `Map<String, Double>`. Firestore's `set(snapshot)` / `toObjects(HealthSnapshot::class.java)` (already in use at `FirebaseRepository.kt:26,42`) handles it via reflection. If you ever need JSON (e.g., for `localStorage`-style backup or a REST mirror), add `kotlinx-serialization-json` then — but that's out of scope for the Firebase sync + Profile work.
- **No plugin required** for the current plan.
- Retrofit + Gson are already on the classpath from the existing network block; leave them as-is.

### e) Date / time
- **java.time (desugared)** — **PARTIALLY MISSING.** `java.time.*` is used everywhere (`Instant`, `LocalDate`, `ZoneId`, `ZonedDateTime`) but `coreLibraryDesugaring` is **not** enabled in `compileOptions`, and `minSdk = 26` means `java.time` is available natively on every device you target. **No new dep is needed at minSdk 26 — the desugaring plugin is optional, not required.** Leave it alone unless you ever lower minSdk below 26.
- **kotlinx-datetime** — **NOT NEEDED.** Don't introduce a parallel time library. Keep using `java.time`; it interoperates cleanly with `kotlinx-coroutines` and Firestore `Timestamp` via `.toEpochMilli()`.

## 3. Risk notes

| Proposed addition | APK size impact | New plugin needed? | BOM conflict? |
|---|---|---|---|
| `androidx.work:work-runtime-ktx:2.9.1` | ~+200 KB (medium — pulls in `androidx.startup`) | No | None. WorkManager is outside the Compose BOM. |
| `androidx.hilt:hilt-work:1.1.0` + `hilt-compiler` kapt | ~+20 KB runtime + kapt build cost | No (it's a Hilt extension) | None. The 1.1.0 line is compatible with `hilt-android:2.55`. |
| `androidx.datastore:datastore-preferences:1.1.1` | ~+60 KB | No | None. |
| `com.google.firebase:firebase-storage-ktx` (BOM) | ~+150 KB (medium — adds OkHttp/Okio transitively) | No | None. BOM-managed. |
| `io.coil-kt:coil-compose:2.6.0` | ~+200 KB (medium — pulls OkHttp + Okio, which Firebase Storage also uses; R8 will dedupe) | No | **Mild concern.** Coil 2.6.0 was released against Compose UI ~1.6.x, and the project is on Compose BOM 2024.02.00 (Compose UI 1.6.1). It works, but newer Coil (2.7.0+) targets Compose UI 1.7+. **Recommendation: do NOT bump the Compose BOM just for Coil** — keep BOM 2024.02.00, use Coil 2.6.0. Bumping the Compose BOM is a much larger surgery (Material3 1.3 → 2.x, animation API changes, etc.) and out of scope for this work. |
| `kotlinx-serialization-json` | — | **YES** — needs `id("org.jetbrains.kotlin.plugin.serialization")` in BOTH `app/build.gradle.kts` AND the top-level `build.gradle.kts`. | None. **Do not add** — not needed. |
| `kotlinx-datetime` | — | No | — **Do not add** — not needed. |
| `coreLibraryDesugaring` | ~+500 KB (large) | **YES** — needs `coreLibraryDesugaring` in `compileOptions` and `coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")` | — **Do not add** — `minSdk = 26` makes it pointless. |

**Compose BOM recommendation:** Keep `2024.02.00` for this work. Bumping it is a separate, much larger refactor (Material3 1.2 → 2.x has breaking API changes in `TopAppBar`, `DatePicker`, `PullToRefreshBox`, etc.). Coil 2.6.0 + WorkManager 2.9.1 + DataStore 1.1.1 + Firebase Storage (BOM) are all compatible with the current BOM. Flag this as a follow-up task, not a blocker.

## 4. Proposed final `dependencies { ... }` block

Mark every line `[EXISTING]` or `[NEW]`. Paste as the entire `dependencies { ... }` body.

```kotlin
dependencies {
    // ----- Core -----
    implementation("androidx.core:core-ktx:1.12.0")                                          // [EXISTING]
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")                         // [EXISTING]
    implementation("androidx.activity:activity-compose:1.8.2")                                // [EXISTING]

    // ----- Compose -----
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))                       // [EXISTING]
    implementation("androidx.compose.ui:ui")                                                  // [EXISTING]
    implementation("androidx.compose.ui:ui-graphics")                                         // [EXISTING]
    implementation("androidx.compose.ui:ui-tooling-preview")                                  // [EXISTING]
    implementation("androidx.compose.material3:material3")                                    // [EXISTING]
    implementation("androidx.compose.material:material-icons-extended")                       // [EXISTING]
    implementation("androidx.compose.animation:animation")                                    // [EXISTING]
    implementation("androidx.compose.ui:ui-text-google-fonts:1.6.1")                          // [EXISTING]
    implementation("androidx.navigation:navigation-compose:2.7.7")                            // [EXISTING]
    implementation("com.google.android.material:material:1.12.0")                            // [EXISTING]

    // ----- Hilt -----
    implementation("com.google.dagger:hilt-android:2.55")                                     // [EXISTING]
    kapt("com.google.dagger:hilt-compiler:2.55")                                             // [EXISTING]
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")                            // [EXISTING]
    // [NEW] WorkManager + Hilt-Work for durable background sync queue
    implementation("androidx.work:work-runtime-ktx:2.9.1")
    implementation("androidx.hilt:hilt-work:1.1.0")
    kapt("androidx.hilt:hilt-compiler:1.1.0")

    // ----- Health Connect -----
    implementation("androidx.health.connect:connect-client:1.1.0")                            // [EXISTING]

    // ----- Local persistence -----
    implementation("androidx.room:room-runtime:2.7.0-alpha13")                                // [EXISTING]
    implementation("androidx.room:room-ktx:2.7.0-alpha13")                                    // [EXISTING]
    kapt("androidx.room:room-compiler:2.7.0-alpha13")                                         // [EXISTING]
    // [NEW] DataStore-Preferences for reactive profile name / email / uid
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // ----- Charts (Vico) -----
    implementation("com.patrykandpatrick.vico:compose-m3:1.14.0")                             // [EXISTING]

    // ----- Network -----
    implementation("com.squareup.retrofit2:retrofit:2.9.0")                                  // [EXISTING]
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")                            // [EXISTING]

    // ----- Firebase -----
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))                      // [EXISTING]
    implementation("com.google.firebase:firebase-analytics-ktx")                              // [EXISTING]
    implementation("com.google.firebase:firebase-firestore-ktx")                              // [EXISTING]
    implementation("com.google.firebase:firebase-auth-ktx")                                   // [EXISTING]
    // [NEW] Firebase Storage for avatar upload (BOM-managed version)
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")           // [EXISTING]
    implementation("com.google.android.gms:play-services-auth:21.1.0")                        // [EXISTING]

    // ----- Image loading (Compose-native) -----
    // [NEW] Coil 2.6.0 for AsyncImage avatar; compatible with Compose BOM 2024.02.00
    implementation("io.coil-kt:coil-compose:2.6.0")

    // ----- Glance for App Widget -----
    implementation("androidx.glance:glance-appwidget:1.0.0")                                  // [EXISTING]
    implementation("androidx.glance:glance-material3:1.0.0")                                  // [EXISTING]

    // ----- Test -----
    testImplementation("junit:junit:4.13.2")                                                  // [EXISTING]
    androidTestImplementation("androidx.test.ext:junit:1.1.5")                                // [EXISTING]
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")                    // [EXISTING]
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.02.00"))             // [EXISTING]
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")                            // [EXISTING]
    debugImplementation("androidx.compose.ui:ui-tooling")                                      // [EXISTING]
    debugImplementation("androidx.compose.ui:ui-test-manifest")                                // [EXISTING]
}
```

**Net additions: 6 dependency lines** (2 for WorkManager/Hilt-Work, 1 for DataStore, 1 for Firebase Storage, 1 for Coil, 1 for Hilt-Work kapt).

## 5. Top-level `build.gradle.kts` plugins

**No new plugins are required.** All additions in section 4 use either (a) plain `implementation` (no plugin) or (b) `kapt` which is already declared at the top-level implicitly via `id("com.google.dagger.hilt.android")` propagation. The `kotlin-kapt` plugin is already applied at the module level.

For reference, the top-level `build.gradle.kts` should stay exactly as it is:
```kotlin
plugins {
    id("com.android.application") version "8.9.1" apply false
    id("com.android.library") version "8.9.1" apply false
    id("org.jetbrains.kotlin.android") version "2.1.20" apply false
    id("com.google.dagger.hilt.android") version "2.55" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.20" apply false
    id("com.google.gms.google-services") version "4.4.4" apply false
}
```

Do **not** add `kotlin.plugin.serialization` — it's not needed for this work. Do **not** add `kotlinx-serialization` to the top-level plugin classpath.

## Summary of what the main agent should do

1. Paste the block in section 4 into `app/build.gradle.kts`, replacing the existing `dependencies { ... }` body.
2. Do **not** touch the top-level `build.gradle.kts` or `settings.gradle.kts`.
3. Do **not** touch `compileOptions` (no desugaring needed at minSdk 26).
4. Expect first-build growth of roughly **+600 KB** to the APK (WorkManager + Coil + Storage + DataStore + Hilt-Work), with kapt build time going up by ~3-5s.
5. Plan a follow-up task to migrate the three raw `SharedPreferences` sites to DataStore for reactivity — out of scope here.
