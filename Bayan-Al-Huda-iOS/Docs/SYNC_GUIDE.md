# Bayan Al-Huda — Android & iOS Cross-Repository Synchronization Guide

- **Primary Source of Truth (Android)**: `https://github.com/zulqarnain7861/Bayan-ul-huda-quran-o-hadees` (`versionCode = 8`, `versionName = "1.0.2"`)
- **Mirrored Native iOS Repository**: `https://github.com/zulqarnain7861/Bayan-Al-Huda-iOS` (`CFBundleVersion = 8`, `CFBundleShortVersionString = "1.0.2"`)

---

## 1. Core Synchronization Principles

1. **Android-First Specification**:
   - Every new feature, bug fix, or content update is designed, implemented, and verified in the **Android** repository first.
   - No feature is considered release-complete until its native iOS counterpart in `Bayan-Al-Huda-iOS` is implemented and passes CI.
2. **Byte-for-Byte Identical Shared Assets**:
   - Sacred texts, PDFs, audio files, and fonts must never be altered or re-encoded differently between platforms.
   - The script `tools/sync_shared_assets_from_android.sh` copies and verifies SHA-256 checksums for:
     - `noorani_qaida.pdf` (53-page HD PDF)
     - `noorani_qaida_text.json` (53-page Arabic/Urdu/English metadata & printed top page numbers)
     - `Amiri-Regular.ttf` & `ScheherazadeNew-Regular.ttf`
     - `quran_arabic_6236.json` & QUL translations
     - `qaida_audio/*.mp3` (149 offline human voice clips)
3. **Dual Implementation Plan Requirement**:
   - Every feature PR must include a filled specification based on `Docs/FEATURE_TEMPLATE.md` containing both the Android implementation and the iOS native implementation.

---

## 2. Feature Parity Matrix

| Feature Module | Android Status (`v1.0.2` / `8`) | iOS Status (`v1.0.2` / `8`) | Shared Assets / API |
| :--- | :--- | :--- | :--- |
| **1. Quran Reader & Audio Sync** | Production (`QuranScreen.kt`, `AudioPlayerManager.kt`, `Mp3QuranTimingClient.kt`) | Production (`QuranReaderView.swift`, `QuranAudioSyncService.swift`) | `quran_arabic_6236.json`, MP3Quran v3 API (`reciters`, `ayat_timing`) |
| **2. Reciter Picker (280+ Reciters + Search)** | Production (`SelectReciterDialog` in `QuranScreen.kt`) | Production (`SelectReciterSheetView.swift`) | `@AppStorage("preferred_reciter_id")`, MP3Quran v3 live catalog |
| **3. Hadith Library (`sunnah.com` Numbering)** | Production (`HadithScreen.kt`, `ExtendedHadithDataProvider.kt`) | Production (`HadithLibraryView.swift`) | Sunni (Sihah Sittah) & Shia (Al-Kafi, Nahj al-Balagha) |
| **4. Video Library (YouTube Fallback Tiers)** | Production (`YtEmbedHost.kt`, `LibraryScreen.kt`) | Production (`VideoLibraryView.swift`, `AVPlayer` + `WKWebView` embed) | Verified Islamic lecture catalog |
| **5. Noorani Qaida HD PDF Reader & MCQ Tests** | Production (`NooraniQaidaPdfReader.kt`, `NooraniQaidaExerciseQuizCatalog.kt`) | Production (`NooraniQaidaReaderView.swift`, `NooraniQaidaPDFKitView.swift`) | `noorani_qaida.pdf`, `noorani_qaida_text.json`, `Amiri-Regular.ttf` |
| **6. Global Search** | Production (`SearchScreen.kt`) | Production (`GlobalSearchView.swift`) | Quran, Hadith, Tafsir, Noorani Qaida, Books |
| **7. Azkar / Daily Duas & Prayer Alarms** | Production (`DailyDuasScreen.kt`, `SalahAndFajrAlarmCard.kt`) | Production (`DailyDuasView.swift`, `UNUserNotificationCenter`) | 67 Duas + Hisnul Muslim audio |
| **8. Offline-First & Bookmarks Persistence** | Production (Room DB + `SharedPreferences`) | Production (Core Data / `PersistenceController.swift` + `@AppStorage`) | Local SQLite/CoreData + JSON fallback |
| **9. Islamic Aesthetic & 8 Themes** | Production (`Theme.kt`, `TopIslamicBar.kt`) | Production (`IslamicTheme.swift`, `IslamicCardDecorator.swift`) | Shared Emerald & Gold design tokens |

---

## 3. Step-by-Step Workflow When Adding a New Feature

1. **Create Feature Spec**: Copy `Docs/FEATURE_TEMPLATE.md` to `Docs/features/FEAT-<name>.md` and document both Android and iOS targets.
2. **Implement & Test on Android**:
   - Implement Kotlin/Compose code in `Bayan-ul-huda-quran-o-hadees`.
   - Run `gradle :app:testDebugUnitTest` and verify APK compilation.
3. **Sync Shared Assets to iOS**:
   - Run `./tools/sync_shared_assets_from_android.sh ../Bayan-ul-huda-quran-o-hadees` from `Bayan-Al-Huda-iOS` (or trigger `.github/workflows/sync-to-ios-repo.yml`).
4. **Implement & Test on iOS**:
   - Implement the corresponding SwiftUI views and Core services in `Bayan-Al-Huda-iOS/BayanAlHuda/Modules/<Feature>/`.
   - Run `xcodegen generate` and `xcodebuild test -scheme BayanAlHuda -destination 'platform=iOS Simulator,name=iPhone 15'`.
5. **Update Parity Docs**:
   - Add the new file mappings to `Docs/ANDROID_TO_IOS_MAPPING.md` and update the matrix in `Docs/SYNC_GUIDE.md`.
