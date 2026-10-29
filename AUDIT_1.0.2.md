# Release Audit Report — Bayan al-Huda (`1.0.2` / `versionCode = 8`)

Repository: `zulqarnain7861/Bayan-ul-huda-quran-o-hadees`  
Application ID: `com.bayanulhuda.com`  
Target: Release Candidate `1.0.2` (`minSdk = 24`, `targetSdk = 36`)

---

## Executive Summary

This report documents all findings across the five release-audit categories prior to applying Phase 3 surgical remediations. Every fix selected for Phase 3 strictly preserves the sacred-text, audio, UI layout, offline, and update-install survival contracts. Structural or schema-level items that carry any risk of breaking update-install compatibility or require schema history exports are documented here and formally deferred in `PROPOSALS.md`.

---

## Category 1 — Data Loss & Persistence Risks

### 1.1 [Critical] Destructive Migration Fallback & Missing Historical Room Schema Exports
- **File & Lines**: `app/src/main/java/com/example/data/local/AppDatabase.kt:55-56, 71-147, 160-161`
- **Finding**:
  - `@Database(version = 8, exportSchema = false)` is configured without exported schema JSON snapshots.
  - `Room.databaseBuilder(...).addMigrations(MIGRATION_3_6, MIGRATION_4_6, MIGRATION_5_6, MIGRATION_6_8, MIGRATION_7_8).fallbackToDestructiveMigration(true)` is active.
  - While upgrades from versions `3`, `4`, `5`, `6`, and `7` to `8` are covered by explicit non-destructive SQL migrations that preserve `bookmarks`, `notes`, `prayer_schedules`, and `download_package_manifests`, any install remaining on versions `1` or `2` (or any future version bump where a migration step is omitted) triggers `.fallbackToDestructiveMigration(true)`, permanently wiping the user's local `bookmarks` and `notes` tables.
- **User-Visible Consequence**: Users upgrading from legacy schema v1/v2 or across an unmigrated future schema bump lose all locally saved bookmarks and study notes without warning.
- **Remediation / Disposition**:
  - Per Phase 3 Fix Rules (*"Do not remove `fallbackToDestructiveMigration` without also writing and testing the missing `Migration(from, to)`. If you can't write the migration safely, leave the fallback and log it in `PROPOSALS.md`"*), because historical v1 and v2 schema definitions are not present in git history (`exportSchema = false`), synthesizing `MIGRATION_1_3` / `MIGRATION_2_3` blindly risks a `IllegalStateException: Migration didn't properly handle...` crash on startup.
  - **Action**: Preserved v3→v8 migrations untouched for `1.0.2` update stability and documented the schema-export + non-destructive table-preservation migration plan in `PROPOSALS.md`.

### 1.2 [High] Silent Cloud Sync Rejection on Bookmarks & Notes Due to Client/Rule Size Mismatch
- **File & Lines**:
  - `app/src/main/java/com/example/data/repository/CloudSyncRepository.kt:78, 120, 122`
  - `firestore.rules:21, 32, 34`
- **Finding**:
  - `firestore.rules` enforces:
    - `isValidBookmark`: `bookmark.referenceKey.size() <= 100` (`firestore.rules:21`)
    - `isValidNote`: `note.referenceKey.size() <= 100` (`firestore.rules:32`) and `note.content.size() <= 5000` (`firestore.rules:34`)
  - However, `CloudSyncRepository.saveBookmark` (line 78) truncates `referenceKey` to `.take(120)`, and `CloudSyncRepository.saveNote` (lines 120, 122) truncates `referenceKey` to `.take(120)` and `content` to `.take(9999)`.
  - Additionally, `BayanViewModel.kt:1049, 1077` wraps cloud sync calls in `runCatching { ... }` with no fallback when `PERMISSION_DENIED` is thrown by Firestore rules.
- **User-Visible Consequence**: Any note between 5,001 and 9,999 characters or bookmark/note with a `referenceKey` between 101 and 120 characters fails Firestore security validation with `PERMISSION_DENIED` and is silently dropped from cloud backup.
- **Exact Fix**: Update `CloudSyncRepository.kt` line 78 (`referenceKey.take(100)`), line 120 (`referenceKey.take(100)`), and line 122 (`content.take(5000)`) to match `firestore.rules` exactly.

### 1.3 [High] Orphaned Cloud Notes on Deletion Due to Local vs. Cloud ID Mismatch
- **File & Lines**: `app/src/main/java/com/example/ui/viewmodels/BayanViewModel.kt:1058-1082, 1086-1102` and `app/src/main/java/com/example/data/local/dao/BookmarkDao.kt` (`NoteDao`)
- **Finding**:
  - In `BayanViewModel.saveNote(targetType, referenceKey, content)`:
    - Line 1066 inserts a local `NoteEntity` with `id = 0` (auto-generating a sequential Room row ID `1, 2, 3...`).
    - Line 1070 generates a cloud `noteId = "note_${System.currentTimeMillis()}"` using wall-clock milliseconds.
  - In `BayanViewModel.deleteNote(id: Long)` (line 1094), it calls `cloudRepository.deleteNote(uid, "note_$id")` using the Room sequential primary key `id`, which never matches `"note_${timestamp}"`.
- **User-Visible Consequence**: When a signed-in user creates a note and later deletes it, the cloud Firestore document (`note_<timestamp>`) is never deleted; on the next launch or cloud listener emission, the deleted note reappears.
- **Exact Fix**: Return the inserted row `Long` ID from `noteDao.insertNote(...)` (via `IslamicRepository.insertNote`) and use `"note_$insertedId"` when saving the new note to `CloudSyncRepository.saveNote`, ensuring `deleteNote(id)` (`"note_$id"`) targets the exact same Firestore document ID.

---

## Category 2 — Security Issues

### 2.1 [High] Unused Dangerous Microphone Permission (`RECORD_AUDIO`) in Manifest
- **File & Lines**: `app/src/main/AndroidManifest.xml:17`
- **Finding**:
  - `<uses-permission android:name="android.permission.RECORD_AUDIO" />` is declared in `AndroidManifest.xml`.
  - Codebase-wide inspection confirms `RECORD_AUDIO`, `AudioRecord`, `MediaRecorder`, and `SpeechRecognizer` are never used anywhere in `app/src/main`.
- **User-Visible Consequence**: Android displays Microphone permission capabilities on the app's Play Store / system permission sheet despite the app having no voice recording feature, eroding user trust and risking Play Store policy rejection for unnecessary dangerous permissions.
- **Exact Fix**: Remove `<uses-permission android:name="android.permission.RECORD_AUDIO" />` from `app/src/main/AndroidManifest.xml`.

### 2.2 [Medium] Hardcoded Developer Email Check Used for Provenance Section Title
- **File & Lines**:
  - `app/src/main/java/com/example/data/remote/SecurityIntegrityGuard.kt:18, 344-346`
  - `app/src/main/java/com/example/ui/screens/AboutScreen.kt:111-112, 793`
- **Finding**:
  - `SecurityIntegrityGuard.AUTHORIZED_DEVELOPER_EMAIL = "sheikhzulqar@gmail.com"` and `isAuthorizedDeveloper(userEmail: String?)` check a client-side email string.
  - In `AboutScreen.kt:793`, this boolean is only used to toggle a decorative heading label (`"Verified Sources & Developer Audit"` vs `"Verified Sources & Provenance"`). It does not guard sensitive operations, and `firestore.rules` properly enforces `request.auth.uid == userId` on the server.
- **User-Visible Consequence**: No privilege escalation occurs in practice, but misleading KDoc in `SecurityIntegrityGuard.kt` implies client-side email comparison is an authorization mechanism.
- **Exact Fix**: Preserve `SecurityIntegrityGuard.isAuthorizedDeveloper` signature and return behavior (required by `ExampleRobolectricTest.kt:344, 561`), and document clearly in `SecurityIntegrityGuard.kt` and `AboutScreen.kt` that it is strictly a non-security UI display helper and must never be used for access control.

### 2.3 [Medium] API Key Passed in URL Query Parameter Without Redaction Helper
- **File & Lines**:
  - `app/src/main/java/com/example/data/remote/MultiSourceHadithCatalog.kt:306-336`
  - `app/src/main/java/com/example/data/remote/IslamicRemoteService.kt:499-515`
- **Finding**:
  - `MultiSourceHadithCatalog` constructs HadithAPI.com URLs with `?apiKey=$key` because HadithAPI.com requires the key as a query parameter. If any exception handler or diagnostic log prints the request URL, the API key could leak into logcat.
- **User-Visible Consequence**: Potential exposure of `HADITH_API_KEY` in debug/crash logs if an HTTP exception message includes the full request URL.
- **Exact Fix**: Add URL query-key redaction in `SecurityIntegrityGuard` (`redactSensitiveUrl(url: String)`) and ensure `IslamicRemoteService.httpGetWithHeaders` never logs or surfaces unredacted `apiKey=` query strings in error states.

### 2.4 [Medium] Missing `package.json` for `firestore.test.js` Security Rules Test Suite
- **File & Lines**: `/firestore.test.js:1-147`
- **Finding**:
  - `/firestore.test.js` contains comprehensive unit tests for `/firestore.rules` using `node:test` and `@firebase/rules-unit-testing`, but the repository root lacks a `/package.json` defining the test script and dependencies.
- **User-Visible Consequence**: CI and developers cannot run `npm run test:rules` to validate Firestore security rules before deployment.
- **Exact Fix**: Add `/package.json` with `"test:rules": "node --test firestore.test.js"` and `@firebase/rules-unit-testing` in `devDependencies`.

---

## Category 3 — Crash Vectors

### 3.1 [High] Unbounded `readText()` on Network Streams in Translation & Timetable Fetchers (`OutOfMemoryError`)
- **File & Lines**:
  - `app/src/main/java/com/example/data/local/QulOfflineTranslationManager.kt:427`
  - `app/src/main/java/com/example/data/prayer/SehriIftarTimetableEngine.kt:1495`
- **Finding**:
  - While `IslamicRemoteService.httpGetWithHeaders` enforces `SecurityIntegrityGuard.MAX_HTTP_RESPONSE_BYTES` (8 MB), both `QulOfflineTranslationManager.executeSafeHttpGet` (line 427) and `SehriIftarTimetableEngine.httpGetAlAdhan` (line 1495) call `conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }` without checking `contentLengthLong` or capping the character buffer.
- **User-Visible Consequence**: A corrupted CDN response, captive portal stream, or oversized payload causes an unhandled `OutOfMemoryError` crash on the background coroutine thread, terminating the app process.
- **Exact Fix**: Enforce `conn.contentLengthLong <= SecurityIntegrityGuard.MAX_HTTP_RESPONSE_BYTES` and read via a bounded 8 KB chunk loop capped at `SecurityIntegrityGuard.MAX_HTTP_RESPONSE_BYTES` in both `QulOfflineTranslationManager.kt` and `SehriIftarTimetableEngine.kt`.

### 3.2 [High] Unescaped FTS4 Reserved Boolean Keywords (`AND`, `OR`, `NOT`, `NEAR`) Cause `SQLiteException`
- **File & Lines**:
  - `app/src/main/java/com/example/data/repository/IslamicRepository.kt:307-310, 327-330, 409-412, 757, 777`
- **Finding**:
  - In `searchAyahsSmart`, `searchTranslationsSmart`, and `searchHadithsSmart`, the FTS query builder strips punctuation and appends `*` to every whitespace token (`joinToString(" ") { "$it*" }`).
  - In SQLite FTS4 (`ayahs_fts`, `translations_fts`, `hadiths_fts`), uppercase boolean operators `AND`, `OR`, `NOT`, and `NEAR` are reserved syntax keywords. When a user types an uppercase query like `"NOT AVAILABLE"` or `"MERCY AND FORGIVENESS"`, the builder constructs `"NOT* AVAILABLE*"` or `"MERCY* AND* FORGIVENESS*"`, causing SQLite FTS4 to throw `android.database.sqlite.SQLiteException: malformed MATCH expression: [NOT*]`.
  - Additionally, legacy methods `IslamicRepository.searchAyahs(query)` (line 757) and `IslamicRepository.searchHadiths(query)` (line 777) pass raw user input directly to `ayahDao.searchAyahs(query)` and `hadithDao.searchHadiths(query)` without FTS sanitization.
- **User-Visible Consequence**: Searching with uppercase English operators or raw FTS special characters in legacy search paths either crashes with `SQLiteException` or silently drops FTS results.
- **Exact Fix**: Add `SecurityIntegrityGuard.toSafeFtsMatchQuery(rawQuery: String): String` that strips FTS syntax characters, filters/lowercases reserved FTS boolean keywords (`AND`, `OR`, `NOT`, `NEAR`) so they cannot form malformed `AND*`/`NOT*` operators, and apply it in `IslamicRepository.searchAyahsSmart`, `searchTranslationsSmart`, `searchHadithsSmart`, `searchAyahs`, and `searchHadiths`.

### 3.3 [Medium] `BootAndTimeChangeReceiver` Action Validation Hardening
- **File & Lines**:
  - `app/src/main/java/com/example/ui/prayer/PrayerAlarmReceivers.kt:128-147`
- **Finding**:
  - `BootAndTimeChangeReceiver` is exported to receive system boot/time broadcasts (`BOOT_COMPLETED`, `TIME_SET`, `TIMEZONE_CHANGED`, `DATE_CHANGED`, `MY_PACKAGE_REPLACED`, `SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED`). If `context` or `intent?.action` is null or spoofed with unexpected extras, the receiver should defensively validate the action and wrap work safely.
- **User-Visible Consequence**: Malformed broadcast intents could trigger unnecessary alarm rescheduling work on the main receiver thread.
- **Exact Fix**: Ensure strict action allowlist validation and defensive exception handling around receiver execution in `PrayerAlarmReceivers.kt`.

---

## Category 4 — Silent Failure & Correctness Bugs

### 4.1 [High] `PrayerAlarmScheduler.getSetupDiagnostics()` Mutates System `AlarmManager` State
- **File & Lines**: `app/src/main/java/com/example/ui/prayer/PrayerAlarmScheduler.kt:187-240` (specifically line 229)
- **Finding**:
  - `getSetupDiagnostics(context: Context, schedule: PrayerScheduleEntity?): AlarmSetupDiagnostics` is a diagnostic inspection getter called by the UI (`PrayerTimesScreen` / `BayanViewModel`) to display alarm permission status and active alarm summaries.
  - However, line 229 executes `val scheduled = scheduleAllDailyAlarms(appCtx, schedule)`, which cancels and re-registers every daily `AlarmManager` pending intent and writes to `SharedPreferences` as a side effect of reading diagnostics.
- **User-Visible Consequence**: Opening or refreshing prayer alarm diagnostics unexpectedly resets/reschedules active system alarms, potentially clobbering an active snooze or pending alarm state.
- **Exact Fix**: Extract a pure, side-effect-free `computeScheduledAlarmsSummary(context: Context, schedule: PrayerScheduleEntity?, config: PrayerReminderConfig): List<String>` helper in `PrayerAlarmScheduler.kt` and call it inside `getSetupDiagnostics()`, while keeping `scheduleAllDailyAlarms()` performing the actual scheduling and returning the same summary list.

### 4.2 [Medium] `DuaAudioController` Leaks `TextToSpeech` Service Connection on `ViewModel.onCleared()`
- **File & Lines**:
  - `app/src/main/java/com/example/ui/audio/DuaAudioController.kt:50-83, 189-196`
  - `app/src/main/java/com/example/ui/viewmodels/BayanViewModel.kt:1395-1400`
- **Finding**:
  - `DuaAudioController` binds an Android `TextToSpeech` engine in `init`.
  - When `BayanViewModel.onCleared()` runs (line 1398), it calls `duaAudioController.stop()`, which stops active utterances (`tts?.stop()`) but never calls `tts?.shutdown()`.
- **User-Visible Consequence**: Every `BayanViewModel` lifecycle teardown leaks an active `TextToSpeech` service connection bound to `applicationContext`.
- **Exact Fix**: Add a `shutdown()` method on `DuaAudioController` that calls `stop()`, shuts down `tts`, and cancels the controller's `CoroutineScope`, and invoke `duaAudioController.shutdown()` in `BayanViewModel.onCleared()`.

### 4.3 [Medium] Machine-Specific APK Deletion Task (`syncQuranApk`) in Root `build.gradle.kts`
- **File & Lines**: `build.gradle.kts:10-45`
- **Finding**:
  - Root `build.gradle.kts` defines a `syncQuranApk` task wired into `packageDebug.finalizedBy` and `assembleDebug.finalizedBy` that hardcodes `file("/app/applet")` and deletes a hardcoded list of files (`Quran 6.apk` through `Quran 21.apk`, `Bayan_al_Huda_v1.apk`, etc.) and copies `Quran.apk` into the root directory on every debug build.
- **User-Visible Consequence**: Pollutes portable builds with container-specific `/app/applet` path assumptions and unexpected file mutations in the project root during standard Gradle tasks.
- **Exact Fix**: Make `syncQuranApk` a clean, portable no-op task in `/build.gradle.kts` (preserving the task registration name so any external script invoking `syncQuranApk` succeeds without mutating the workspace).

---

## Category 5 — Performance & Compose Recomposition Defects

### 5.1 [High] Root `BayanApp` Collects 22 `StateFlow`s (Including 60ms Audio Ticks) & Inline `.collectAsStateWithLifecycle().value` Inside `NavHost`
- **File & Lines**: `app/src/main/java/com/example/MainActivity.kt:120-144, 496`
- **Finding**:
  - At the top of `BayanApp` (lines 121–143), 22 `StateFlow` properties from `BayanViewModel` are collected via `by viewModel.<flow>.collectAsStateWithLifecycle()`.
  - Because `audioState` (`AudioPlayerManager.kt:1211`) emits a new `AudioPlaybackState` with updated `currentPositionMs` every **60 ms** (~16.6 times per second) and `duaAudioState` (`DuaAudioController.kt:157`) emits every **45 ms** (~22 times per second), reading `audioState` and `duaAudioState` at the top of `BayanApp` forces the entire `BayanApp` root composable, `Scaffold`, bottom navigation bar, and `NavHost` lambda to re-evaluate on every 45–60 ms tick!
  - Furthermore, line 496 inside `composable(BayanNavRoutes.QURAN)` calls `currentTranslations = viewModel.currentTranslations.collectAsStateWithLifecycle().value` inline as a named argument rather than collecting state cleanly at the destination composable scope.
- **User-Visible Consequence**: Elevated frame-time jank and unnecessary object allocations while scrolling a 300-verse Surah during Quran audio recitation or Dua TTS playback.
- **Exact Fix**:
  - Scope destination-specific `StateFlow` collections (`surahs`, `currentAyahs`, `currentTranslations`, `hadiths`, `bookmarks`, `notes`, `searchAyahs`, `searchHadiths`, `searchTafsirs`, `sunnahRemindersState`, `duaAudioState`, `sehriIftarTimetable`, `isTimetableLoading`, `timetableScopeMode`, `selectedTimetableGregorianMonth`, `auditReport`, `sourceRegistryList`) inside their respective `composable(...)` route blocks in `MainActivity.kt` so emissions only recompose the active screen.
  - Isolate the floating audio mini-player and alarm banner into dedicated child composables that collect `audioState` and `alarmPlaybackState` locally, completely eliminating 60 ms root `BayanApp` recompositions.

### 5.2 [Medium] Unencrypted Release Build (`isMinifyEnabled = false`) & Unsplit Multi-Architecture APK Size (74 MB)
- **File & Lines**: `app/build.gradle.kts:47-55`
- **Finding**:
  - `release` buildType has `isMinifyEnabled = false`, producing `47.3 MB` of uncompressed DEX across 16 `.dex` files (`18.2 MB` compressed in APK), alongside `57.1 MB` of bundled assets.
- **User-Visible Consequence**: Larger download size and slower cold DEX verification on low-end `minSdk 24` devices.
- **Remediation / Disposition**:
  - Enabling R8 (`isMinifyEnabled = true`) across 16 DEX files containing reflective JSON parsers (`org.json`, Gson/Kotlinx serialization, Firebase Firestore custom POJOs, and dynamic asset loaders) without an exhaustive R8 keep-rule verification pass across all 127 QUL translations and 19 Hadith books carries a high risk of stripping model fields in release builds.
  - **Action**: Documented in `PROPOSALS.md` with concrete `proguard-rules.pro` keep rules for the next release cycle.
