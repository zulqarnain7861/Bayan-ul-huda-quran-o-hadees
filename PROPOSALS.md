# Deferred Remediation Proposals — Bayan al-Huda (`1.0.2` → `1.1.0`)

Per the Release Audit Contract and Phase 3 Fix Rules, the following changes were identified during the `1.0.2` audit and deliberately **deferred** to avoid any risk of breaking update-install compatibility, silent data loss on legacy database versions, or reflective model stripping without an exhaustive device-farm verification matrix.

---

## Proposal 1: Room Schema History Export (`exportSchema = true`) & Removal of `fallbackToDestructiveMigration(true)`

### Target Files
- `app/src/main/java/com/example/data/local/AppDatabase.kt:55-56, 160-161`
- `app/build.gradle.kts` (`ksp` arguments block)

### Finding Summary
`AppDatabase` is currently at `version = 8` with `exportSchema = false` and `.fallbackToDestructiveMigration(true)`. Explicit non-destructive SQL `Migration` objects exist for `MIGRATION_3_6`, `MIGRATION_4_6`, `MIGRATION_5_6`, `MIGRATION_6_8`, and `MIGRATION_7_8`, preserving user `bookmarks`, `notes`, `prayer_schedules`, and `download_package_manifests`. However, no migration paths exist from versions `1` or `2`, and `exportSchema = false` prevented historical `1.json` and `2.json` schema snapshots from being committed to the repository.

### Why Deferred in `1.0.2`
- **Contract / Data-Loss Risk**: Removing `.fallbackToDestructiveMigration(true)` without exact `1.json` and `2.json` Room schema definitions would cause Room's `RoomOpenHelper.onValidateSchema` to throw an unrecoverable `IllegalStateException: Migration didn't properly handle...` on startup for any user upgrading from v1/v2 or if any column default mismatches.
- Conversely, synthesizing a guessed `MIGRATION_1_3` / `MIGRATION_2_3` without the original v1/v2 table DDL risks corrupting `bookmarks` or `notes` during an update install.

### Recommended `1.1.0` Implementation Plan
1. Configure the Room KSP schema location in `app/build.gradle.kts`:
   ```kotlin
   ksp {
     arg("room.schemaLocation", "$projectDir/schemas")
   }
   ```
2. Set `exportSchema = true` in `@Database(...)` on `AppDatabase` and commit `app/schemas/com.example.data.local.AppDatabase/8.json` to version control.
3. Replace `.fallbackToDestructiveMigration(true)` with a custom `RoomDatabase.Callback` / recovery handler that backs up `bookmarks` and `notes` via raw `SupportSQLiteDatabase` cursor queries before rebuilding canonical read-only scripture tables (`surahs`, `ayahs`, `hadiths`, `translations`, `tafsirs`), guaranteeing zero loss of user-authored records even across unexpected legacy schema states.

---

## Proposal 2: Enabling R8 Code Shrinking (`isMinifyEnabled = true`) & Resource Shrinking (`isShrinkResources = true`) in Release Builds

### Target Files
- `app/build.gradle.kts:47-55`
- `app/proguard-rules.pro`

### Finding Summary
In `app/build.gradle.kts`, the `release` build type has `isMinifyEnabled = false`. The uncompressed DEX footprint across `classes.dex`–`classes16.dex` is `47,382,100` bytes (`18,287,711` bytes compressed).

### Why Deferred in `1.0.2`
- **Contract Risk**: The app relies on:
  - Firebase Firestore reflective POJO deserialization (`DocumentSnapshot.toObject(CloudBookmark::class.java)`, `CloudNote::class.java`) in `CloudSyncRepository.kt`.
  - Dynamic resource lookup (`resources.getIdentifier(...)`) for the 14 offline Adhan audio recordings (`adhan_makkah_1`, `adhan_madinah_1`, `adhan_shia_1`, etc.) in `AzanAudioController.kt`.
  - Room FTS4 virtual table entities and Kotlinx Serialization / Gson adapters.
- Enabling R8 + `isShrinkResources = true` in a point release without an end-to-end instrumented release test pass risks R8 stripping `res/raw/adhan_*` files (breaking the 14 Adhan files contract) or obfuscating `CloudBookmark` / `CloudNote` field names (breaking cloud sync).

### Recommended `1.1.0` Implementation Plan
1. Add explicit keep rules to `app/proguard-rules.pro`:
   ```proguard
   # Keep Firestore Cloud Sync POJOs
   -keep class com.example.data.repository.CloudBookmark { *; }
   -keep class com.example.data.repository.CloudNote { *; }

   # Keep Room Entities & DAOs
   -keep class com.example.data.local.entities.** { *; }
   -keep class com.example.data.local.dao.** { *; }
   ```
2. Add `res/raw/keep.xml` with `tools:keep="@raw/adhan_*"` so `isShrinkResources = true` never strips any of the 14 Adhan audio files.
3. Enable `isMinifyEnabled = true` and `isShrinkResources = true` in `release` and verify all 14 Adhans and Firestore sync in a release-signed APK.

---

## Proposal 3: Play Asset Delivery (PAD) / App Bundle Configuration for 57.1 MB Offline Scripture Corpus

### Target Files
- `app/build.gradle.kts`
- `app/src/main/assets/` (`57,131,617` bytes)

### Finding Summary
The APK bundles `57.1 MB` of canonical JSON corpora (`quran_full.json`, `qul_translations_offline_v1.json`, `quran_translations_extended.json`, `tafsirs_extended.json`, and 19 `hadith_corpus/*.json` files) directly in `app/src/main/assets/`.

### Why Deferred in `1.0.2`
- **Contract Risk**: The Release Contract strictly requires that offline mode remain 100% offline out-of-the-box with all 114 Surahs, 6,236 Ayahs, 127 QUL translations, and 19 Hadith collections immediately available without network access. Moving assets to on-demand Play Asset Delivery packs would break zero-network first-launch offline completeness for sideloaded APKs.
