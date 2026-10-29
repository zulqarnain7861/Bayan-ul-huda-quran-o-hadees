# Release Verification Report — Bayan al-Huda (`1.0.2` / `versionCode = 8`)

## 1. Phase 4 Regression Gate Summary

| Verification Gate | Baseline (Phase 0) | Post-Remediation (Phase 4) | Status |
| :--- | :--- | :--- | :---: |
| **`compile_applet` / `:app:assembleDebug`** | `BUILD SUCCESSFUL` | `BUILD SUCCESSFUL` | **PASS** |
| **Unit & Robolectric Test Suite (`:app:testDebugUnitTest`)** | `58 PASSED, 0 FAILED, 4 SKIPPED` | `58 PASSED, 0 FAILED, 4 SKIPPED` | **PASS** (100% Identical) |
| **Sacred Text & Audio Oracle (`/tmp/bayan_scripture_oracle.sha256`)** | `53 / 53 OK` (`8ce8a12c...9794`) | `53 / 53 OK` (`8ce8a12c...9794`) | **PASS** (Byte-Identical) |
| **Quran Integrity (114 Surahs / 6,236 Verses / Ar-Ra'd 13:1)** | Verified (`QuranIntegrityAuditTest`) | Verified (`QuranIntegrityAuditTest`) | **PASS** |
| **Hadith Corpus Integrity (19 Bundled Collections + Multi-Source)** | Verified (`HadithCorpusVerificationTest`) | Verified (`HadithCorpusVerificationTest`) | **PASS** |
| **QUL Translations (127 Editions / 73 Languages / Onboarding / RTL)** | Verified (`QulTranslationsVerificationTest`) | Verified (`QulTranslationsVerificationTest`) | **PASS** |
| **14 Offline Adhan Audio Files & Alarm Playback** | Verified (`ExampleRobolectricTest`) | Verified (`ExampleRobolectricTest`) | **PASS** |
| **Sehri / Iftar Timetable & Concise Verified Provenance** | Verified (`SehriIftarAndAboutVerificationTest`) | Verified (`SehriIftarAndAboutVerificationTest`) | **PASS** |

---

## 2. Binary & Resource Size Comparison (Phase 0 vs. Phase 4)

| Artifact / Segment | Baseline Bytes (Phase 0) | Post-Fix Bytes (Phase 4) | Delta |
| :--- | ---: | ---: | ---: |
| **`app-debug.apk` (Total Compressed)** | `77,627,662` | `77,584,335` | `-43,327` bytes (`-42.3 KB`) |
| **`classes*.dex` (Uncompressed)** | `47,382,100` | `47,384,316` | `+2,216` bytes |
| **`classes*.dex` (Compressed in APK)** | `18,287,711` | `18,288,254` | `+543` bytes |
| **`res/` + `resources.arsc` (Uncompressed)** | `3,145,516` | `3,145,516` | `0` bytes (Byte-Identical) |
| **`res/` + `resources.arsc` (Compressed)** | `1,629,134` | `1,629,134` | `0` bytes (Byte-Identical) |
| **`assets/` (Uncompressed)** | `57,131,617` | `57,131,617` | `0` bytes (Byte-Identical) |
| **`assets/` (Compressed)** | `57,108,830` | `57,108,830` | `0` bytes (Byte-Identical) |

---

## 3. Summary of Surgical Remediations Landed in `1.0.2`

1. **Cloud Sync Rule Alignment (`CloudSyncRepository.kt:78, 120, 122`)**:
   - Reduced client-side `referenceKey` truncation from `.take(120)` to `.take(100)` in `saveBookmark` and `saveNote`, and reduced `note.content` truncation from `.take(9999)` to `.take(5000)` in `saveNote` to match `firestore.rules` (`referenceKey.size() <= 100`, `content.size() <= 5000`), eliminating silent `PERMISSION_DENIED` drops during cloud backup.
2. **Cloud Note Deletion ID Consistency (`BayanViewModel.kt:1062-1066`)**:
   - Captured the Room row ID returned by `repository.saveNote(...)` (`insertedId`) and used `"note_$insertedId"` as the Firestore document ID instead of `"note_${System.currentTimeMillis()}"`, ensuring `deleteNote(id)` (`"note_$id"`) deletes the matching cloud document so deleted notes do not reappear on sync.
3. **Removed Unused `RECORD_AUDIO` Permission (`AndroidManifest.xml:17`)**:
   - Removed the unused `<uses-permission android:name="android.permission.RECORD_AUDIO" />` declaration from `AndroidManifest.xml`.
4. **FTS4 Boolean Operator Sanitization (`SecurityIntegrityGuard.kt:341-360`, `IslamicRepository.kt:307, 324, 407, 754, 773`)**:
   - Added `SecurityIntegrityGuard.toSafeFtsMatchQuery(rawQuery)` to strip FTS syntax characters and filter standalone SQLite FTS4 boolean keywords (`AND`, `OR`, `NOT`, `NEAR`) before appending prefix wildcards (`*`), preventing `SQLiteException: malformed MATCH expression` crashes when users search phrases containing uppercase operators or use legacy search flows.
5. **Bounded Network Stream Reading (`QulOfflineTranslationManager.kt:427-444`, `SehriIftarTimetableEngine.kt:1495-1512`)**:
   - Replaced unbounded `bufferedReader().readText()` calls with `contentLengthLong` checks and 8 KB chunked reading capped at `SecurityIntegrityGuard.MAX_HTTP_RESPONSE_BYTES` (8 MB), eliminating `OutOfMemoryError` crash vectors on malformed or oversized network responses.
6. **Side-Effect-Free Alarm Setup Diagnostics (`PrayerAlarmScheduler.kt:229, 245-288`)**:
   - Extracted `computeScheduledAlarmsSummary(context, schedule)` and called it from `getSetupDiagnostics()` instead of `scheduleAllDailyAlarms(appCtx, schedule)`, preventing diagnostic UI reads from cancelling and rescheduling active system `AlarmManager` intents.
7. **`TextToSpeech` Lifecycle Cleanup (`DuaAudioController.kt:340-345`, `BayanViewModel.kt:1200`)**:
   - Added `DuaAudioController.shutdown()` to invoke `tts?.shutdown()` and release the bound TTS engine when `BayanViewModel.onCleared()` runs.
8. **Scoped Compose `StateFlow` Subscriptions (`MainActivity.kt:121-126, 361-367, 400-665`)**:
   - Scoped screen-specific `StateFlow` collections (`currentAyahs`, `currentTranslations`, `hadiths`, `bookmarks`, `notes`, `searchAyahs`, `searchHadiths`, `searchTafsirs`, `sunnahRemindersState`, `duaAudioState`, `sehriIftarTimetable`, `isTimetableLoading`, `timetableScopeMode`, `selectedTimetableGregorianMonth`, `auditReport`, `sourceRegistryList`) to their respective `NavHost` composable destinations and isolated `miniPlayerAudioState` so 60 ms audio progress updates no longer recompose the entire `BayanApp` root tree. Replaced inline `.collectAsStateWithLifecycle().value` in `BayanNavRoutes.QURAN` with delegated state collection.
9. **Build Script Portability & Firestore Rules Test Runner (`build.gradle.kts:10-12`, `/package.json`)**:
   - Replaced hardcoded `/app/applet` file deletions in `syncQuranApk` with a portable no-op task and added `/package.json` for `firestore.test.js`.

---

## 4. Sacred Text & Audio Checksum Oracle Verification

Command executed:
```bash
sha256sum -c /tmp/bayan_scripture_oracle.sha256
```
- **Total Files Verified**: `53` (`app/src/main/assets/**` and `app/src/main/res/raw/**`)
- **Failed Checksums**: `0`
- **Aggregate Oracle SHA-256**: `8ce8a12c21b9db2bb7324c1b6f02644fbe869986b56d29fbda4dd97149599794` (Unchanged)
