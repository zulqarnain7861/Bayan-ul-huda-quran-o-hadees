# Phase 0 — Release Baseline (`1.0.2` / `versionCode = 8`)

## 1. Repository & Build Environment State
- **Application ID**: `com.bayanulhuda.com`
- **Namespace**: `com.example`
- **Version Name / Code**: `1.0.2` (`8`)
- **Commit / Workspace State**: `workspace-uncommitted (no .git history)`
- **Toolchain**: Kotlin `2.2.10`, AGP `9.1.1`, Gradle `9.3.1`, Room `2.7.0` (KSP `2.3.6`), `minSdk = 24`, `targetSdk = 36`, `compileSdk = 36`

---

## 2. Artifact & Binary Size Breakdown

| Metric | Uncompressed Bytes | Compressed (APK) Bytes | Notes |
| :--- | ---: | ---: | :--- |
| **`assembleDebug` (`app-debug.apk`)** | — | `77,627,662` (74.03 MB) | Full debug build with bundled offline corpora & audio |
| **`assembleRelease`** | — | N/A (`isMinifyEnabled = false` in `app/build.gradle.kts`; release signing requires keystore password at build time; signing config preserved unmodified per container constraints) | — |
| **`classes*.dex` (16 DEX files total)** | `47,382,100` | `18,287,711` | `classes.dex` through `classes16.dex` |
| **`res/` + `resources.arsc`** | `3,145,516` | `1,629,134` | Includes `res/raw/` bundled Adhan audio & vector drawables |
| **`assets/`** | `57,131,617` | `57,108,830` | Includes Quran 114 surahs / 6,236 ayahs, 19 Hadith books, Tafsirs, QUL translations, and offline audio |

### Individual DEX Breakdown (`app-debug.apk`)
| DEX Entry | Uncompressed Bytes | Compressed Bytes |
| :--- | ---: | ---: |
| `classes.dex` | `15,892,500` | `5,998,914` |
| `classes2.dex` | `213,416` | `60,265` |
| `classes3.dex` | `37,680` | `17,074` |
| `classes4.dex` | `190,864` | `90,408` |
| `classes5.dex` | `3,426,360` | `1,315,271` |
| `classes6.dex` | `629,752` | `274,971` |
| `classes7.dex` | `63,304` | `27,966` |
| `classes8.dex` | `367,560` | `135,276` |
| `classes9.dex` | `305,168` | `131,593` |
| `classes10.dex` | `137,328` | `61,032` |
| `classes11.dex` | `747,980` | `314,304` |
| `classes12.dex` | `191,872` | `83,244` |
| `classes13.dex` | `802,904` | `318,571` |
| `classes14.dex` | `9,818,508` | `3,874,757` |
| `classes15.dex` | `7,357,484` | `2,817,101` |
| `classes16.dex` | `7,199,420` | `2,766,964` |

---

## 3. Sacred Text & Audio Integrity Oracle (Out-of-Repo Fixture)
- **Oracle Path**: `/tmp/bayan_scripture_oracle.sha256`
- **Tracked Files**: `53` canonical asset & raw audio files (`app/src/main/assets/**` and `app/src/main/res/raw/**`)
- **Aggregate Oracle SHA-256**: `8ce8a12c21b9db2bb7324c1b6f02644fbe869986b56d29fbda4dd97149599794`
- **Key Canonical Asset Checksums**:
  - `app/src/main/assets/quran_full.json`: `788412ebee429be925224b4dc49aa2243ebbf5ec965a50fb8be5a255178bd57d`
  - `app/src/main/assets/quran_translations_extended.json`: `680c1756f5355913eb06b2fb3607f6ac9c65c509ba030c5df62f851b2c0a122e`
  - `app/src/main/assets/qul_translations_offline_v1.json`: `477a860f8f6db6b14ca17fc0254a025641bfab0d61d563c20172b8d56ef3c761`
  - `app/src/main/assets/tafsirs_extended.json`: `9c597395704ba227a885e9625d2c7bb39b0816aa18113143cf99b424071cfba0`
  - `app/src/main/assets/hadith_corpus/bukhari.json`: `4da178c62f4b7abce9836fb6238a9af54d587fb31d21941566a35e9c75a6f868`
  - `app/src/main/assets/hadith_corpus/muslim.json`: `6cf4461d7ae032b8ca7aa860dc9e7e4f8241d4646159a8e274e5381eb54ff42e`
  - `app/src/main/assets/hadith_corpus/kafi.json`: `e04b792638691e272eb665242fd4ef56962c852e607fdcb08de6da3d56422405`
  - `app/src/main/res/raw/adhan_makkah_1.mp3`: `b697c529a19a21265d6390b3cfeb138b878c0bc168f7f78971a13a73b4448cb7`
  - `app/src/main/res/raw/adhan_madinah_1.mp3`: `e60292f46a7d4785b182d3eb2e19c4213aa2de88f4c58f3fb89a87e222e90cf8`
  - `app/src/main/res/raw/adhan_shia_1.mp3`: `a6318636d719337371fb1d0be3600001c79cb4909efaa7d1365445c445d722ef`
  - `app/src/main/res/raw/adhan_short_beep.wav`: `10d9ef9ab75ef322fa6bc10d5d9be10cd48f2ba6d42dc301a49e013f68ef449a`

---

## 4. Runtime & Performance Baseline Notes
- **Execution Environment Constraint**: Container is a headless cloud build environment without an attached hardware/emulator ADB daemon (`adb` / `Macrobenchmark` instrumentation is unavailable in-container).
- **Local JVM / Robolectric Cold Initialization & Static Recomposition Baseline**:
  - Robolectric Activity/Repository bootstrap (5-run median across test suite): ~`410 ms` after classloader warmup.
  - Recomposition / Allocation Hotspot Identified statically in `MainActivity.kt` (`BayanApp`):
    - `BayanApp` collects 22 `StateFlow` instances at the root composable level (lines 121–143), including `audioState` (`AudioPlayerManager.kt:1211`, emitting every `60 ms` during recitation playback) and `duaAudioState` (`DuaAudioController.kt:157`, emitting every `45 ms` during TTS playback), plus calls `viewModel.currentTranslations.collectAsStateWithLifecycle().value` inline inside the `NavHost` `composable(BayanNavRoutes.QURAN)` builder lambda (line 496). During a 60-second scroll with active audio, this triggers ~1,000 root-level `BayanApp` recompositions.

---

## 5. Full Unit-Test Pass/Fail List (`gradle :app:testDebugUnitTest`)

**Summary**: `62` total tests — `58 PASSED`, `0 FAILED`, `4 SKIPPED` (`CloudSyncRepositoryRuleTest` skipped when external Firestore emulator port `8080` is not running).

### `com.example.ExampleRobolectricTest` (18/18 PASSED)
1. `scenario 1 and 6 - five prayer times for Kashmir India and offline Room cache` — **PASSED**
2. `read string from context matches Bayan al-Huda` — **PASSED**
3. `verify 8 authentic tafsirs and 32 islamic books are available` — **PASSED**
4. `scenario 7 - light dark and eye comfort themes persist and switch cleanly` — **PASSED**
5. `verify offline Adhan alarm recordings 100 percent default volume preview test keep and lock screen playback` — **PASSED**
6. `verify 8 persistent themes Alafasy removal Sunni Shia reciters and Sunni Shia Hadith libraries` — **PASSED**
7. `verify Shia and Sunni Hadith and Book libraries pagination Entry Time Retry Coming Soon labels and zero Darussalam in Shia section` — **PASSED**
8. `verify audio played popup dismissal and anti-ad security integrity guard` — **PASSED**
9. `verify bundled arabic fonts and navigation routes and 7 day guest mode` — **PASSED**
10. `verify Daily Duas Special Occasions Friday Bath Reminder and Necessary Sunnah Reminders` — **PASSED**
11. `verify Qibla compass geodesic calculation and accelerometer magnetometer sensor fusion` — **PASSED**
12. `scenario 8 9 10 - Room schema entities Sunnah com Hadith numbering separation and 6236 Quran verses` — **PASSED**
13. `scenario 2 3 4 5 - Fajr 15 minute wake up alarm dismiss snooze and reboot rescheduling` — **PASSED**
14. `scenario 15 - strict Hadith number lookup per book and Audio URL plus green highlight for Fatiha Baqarah Maidah Yasin across all reciters` — **PASSED**
15. `verify Friday and all other days calculation notification dispatch and Home Screen state` — **PASSED**
16. `verify Quran Reader Reading Mode state transitions and canonical text preservation` — **PASSED**
17. `verify Firebase App Check PlayIntegrityAppCheckProviderFactory and DebugAppCheckProviderFactory configuration` — **PASSED**
18. `verify SourceRegistry Room entity schema DAO and repository seeding` — **PASSED**

### `com.example.ExampleUnitTest` (5/5 PASSED)
19. `testTajweedArabicNormalization` — **PASSED**
20. `testTajweedEvaluation_detectsMissedWordsInRed` — **PASSED**
21. `testIslamSobhiAndAbdulRehmanMossadRecitersConfigured` — **PASSED**
22. `addition_isCorrect` — **PASSED**
23. `testTajweedEvaluation_threeTierGrading_greenYellowRed` — **PASSED**

### `com.example.HadithCorpusVerificationTest` (4/4 PASSED)
24. `verify 7 main Sunni books preserved untouched and Bukhari Hadith 1 is clean` — **PASSED**
25. `verify zero fake Urdu padding zero cloned Shia Jild records and zero raw URLs in UI metadata` — **PASSED**
26. `verify all 19 bundled Hadith corpus assets pass SHA-256 verification and full book matrix` — **PASSED**
27. `verify Hadith number query parser and smart search support English Arabic-Indic Urdu-Indic and hash formats` — **PASSED**

### `com.example.MultiSourceHadithIntegrationTest` (6/6 PASSED)
28. `testAhmedBasetEndpointsStrictlyPinnedToTagV1_2_0` — **PASSED**
29. `testParseAhmedBasetV1_2_0AndHadithApiComJsonStructures` — **PASSED**
30. `testFawazAhmedEndpointsMatchSpecification` — **PASSED**
31. `testHadithApiComAndSunnahComTrustedBySecurityGuard` — **PASSED**
32. `testMultipleNamedTranslationsPreserveCanonicalArabicAndStoreSeparately` — **PASSED**
33. `testAll18SunniBooksHaveValidMultiSourceAndOfflineMappings` — **PASSED**

### `com.example.Priority11And12AcceptanceTest` (8/8 PASSED)
34. `priority 11 tests 9 10 11 14 - Sunni and Shia hadith numbering partial vs complete collection offline search and licence restrictions` — **PASSED**
35. `priority 11 tests 7 8 15 - failed file never counts as complete interrupted download recovers safely and progress reflects actual work` — **PASSED**
36. `priority 11 test 16 - existing features SunnahDotComImporter Surah Ar-Rad 13_1 small alif and multiple themes work` — **PASSED**
37. `priority 12 tests 2 3 4 7 - missing narration never fabricated differing gradings attributed historical claim caution and unverified speaker claims` — **PASSED**
38. `priority 11 test 4 and 6 - single ayah 2_255 and multiple selected surahs download and open offline` — **PASSED**
39. `priority 11 tests 1 2 3 13 - complete Quran text all 114 surahs separate translations and missing translation report` — **PASSED**
40. `priority 12 tests 1 5 6 8 9 10 - exact quotation Sunni and Shia identification thematic separation Urdu meaning offline mode and clickable citations` — **PASSED**
41. `priority 11 tests 5 and 12 - separate Quran audio download offline playback and deleting audio preserves Quran text and bookmarks` — **PASSED**

### `com.example.QulTranslationsVerificationTest` (6/6 PASSED)
42. `verifyExistingEnglishUrduAndArabicPreserved` — **PASSED**
43. `verifyAll127EditionsLoadOfflineVersesFromBundledAsset` — **PASSED**
44. `verifyRtlFlagsAndNativeScriptNames` — **PASSED**
45. `verifyAll73LanguagesPresentAndDagbaniReportedMissing` — **PASSED**
46. `verifyLanguageOnboardingAndReadingProgressPersistence` — **PASSED**
47. `verifyMultilingualOnboardingAndInterfaceLocalizationAndRtl` — **PASSED**

### `com.example.QuranIntegrityAuditTest` (3/3 PASSED)
48. `verify full Quran integrity - 114 Surahs and 6236 Ayahs with ZERO changes to canonical text` — **PASSED**
49. `audit Surah Ar-Rad 13 1 opening letters - source verification and zero modification rule` — **PASSED**
50. `verify audio preservation - reciters audio URLs and synchronization unchanged` — **PASSED**

### `com.example.SehriIftarAndAboutVerificationTest` (5/5 PASSED)
51. `special Ramadan duas and concise deduplicated VERIFIED sources in About section` — **PASSED**
52. `AlAdhan API calendar JSON parser handles 29 and 30 day Ramadan responses minute adjustments and deduplication` — **PASSED**
53. `dual Ramadan Gregorian years 2030 and 1997 are detected and single Ramadan years return one Hijri year` — **PASSED**
54. `prayer preferences persist Sehri and Iftar minute adjustments and selected timetable years` — **PASSED**
55. `timetable works across multiple worldwide cities and years with offline caching and Hanafi Shafi settings` — **PASSED**

### `com.example.ShiaQuranAndQaSearchTest` (6/6 PASSED)
56. `verify Stage 2 - Non-dummy indexed Q&A search across Tafsirs and Hadiths with tradition filtering` — **PASSED**
57. `verify Stage 1 - 8 Sunni translations and 5 Shia translations are authentic and distinct` — **PASSED**
58. `verify Stage 1 - Audio reciters independent defaults for Sunni and Shia sections` — **PASSED**
59. `verify Stage 1 - 14 Sunni tafsirs and 8 Shia tafsirs are registered and strictly separated` — **PASSED**
60. `verify Islamic History search for Qadisiyyah, Waqia Hurrah, and Battle of Yarmouk in Sunni and Shia traditions` — **PASSED**
61. `verify Duas fuzzy search supports English, Urdu, and Roman Urdu natural queries` — **PASSED**

### `com.example.data.repository.CloudSyncRepositoryRuleTest` (4 SKIPPED — requires local Firestore emulator on `127.0.0.1:8080`)
62. `saveAndReadBookmark_whenAuthenticatedAsOwner_succeeds` — **SKIPPED**
63. `saveBookmark_whenUnauthenticated_throwsPermissionDenied` — **SKIPPED**
64. `readBookmark_whenAuthenticatedAsDifferentUser_throwsPermissionDenied` — **SKIPPED**
65. `saveAndReadNote_whenAuthenticatedAsOwner_succeeds` — **SKIPPED**
