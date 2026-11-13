# Cross-Platform Feature Specification Template (Android Primary → iOS Mirror)

## 1. Feature Overview
- **Feature ID & Name**: `FEAT-XXX: <Feature Name>`
- **Target Release Version**: `versionName = 1.X.Y` / `versionCode = N` (Android) | `CFBundleShortVersionString = 1.X.Y` / `CFBundleVersion = N` (iOS)
- **Estimated Complexity**: `Low | Medium | High`
- **Summary**: `<Concise description of user-facing functionality and offline/online behavior>`

## 2. Shared Assets & API Contracts
- **Shared Bundled Assets (Must be byte-for-byte identical in both repos)**:
  - `app/src/main/assets/<file>` ↔ `BayanAlHuda/Resources/<file>`
  - SHA-256 Checksum: `<sha256>`
- **Remote API Endpoints (HTTPS allowlisted)**:
  - `<GET https://...>`
- **Persistence Keys (`SharedPreferences` ↔ `@AppStorage`)**:
  - `<key_name>`: `<type & default value>`

## 3. Plan A — Android Primary Implementation (`Bayan-ul-huda-quran-o-hadees`)
- **New Files**:
  - `app/src/main/java/com/example/...`
- **Modified Files**:
  - `app/src/main/AndroidManifest.xml`
  - `app/src/main/java/com/example/MainActivity.kt`
- **UI & Architecture Notes**:
  - Jetpack Compose M3 components, `ViewModel` + `StateFlow`, `BackHandler`, `Modifier.testTag(...)`.
- **Verification**:
  - `gradle :app:testDebugUnitTest`

## 4. Plan B — iOS Mirrored Native Implementation (`Bayan-Al-Huda-iOS`)
- **New Files**:
  - `BayanAlHuda/Modules/<Module>/...`
- **Modified Files**:
  - `BayanAlHuda/ContentView.swift`
  - `project.yml`
- **UI & Architecture Notes**:
  - SwiftUI (`iOS 15.0+`), `ObservableObject` / `@Published`, `@AppStorage`, VoiceOver `.accessibilityLabel(...)`, RTL `.environment(\.layoutDirection, .rightToLeft)`.
- **Verification**:
  - `xcodegen generate && xcodebuild test -scheme BayanAlHuda -destination 'platform=iOS Simulator,name=iPhone 15'`

---

# Sample Completed Dual Implementation Plan: FEAT-004 Native HD Noorani Qaida PDF Reader & MCQ Tests

## 1. Feature Overview
- **Feature ID & Name**: `FEAT-004: Native HD Noorani Qaida 53-Page PDF Reader & Exercise MCQ Tests`
- **Target Release Version**: `1.0.2` (`8`)
- **Estimated Complexity**: `High`
- **Summary**: Presents the authentic 53-page *Mukammal Noorani Qaida* PDF in a decorated Islamic card with horizontal swipe paging, pinch-to-zoom (`1x`–`4x`) & pan, bundled `Amiri-Regular.ttf` RTL Arabic typography, a searchable 53-page / 28-Takhti Lesson Index bottom sheet, persistent last-read page state, and 35 interactive MCQs (Basic, Intermediate, Hard) mapped to the printed top book page numbers (`1, 5..56`).

## 2. Shared Assets & API Contracts
- **Shared Bundled Assets**:
  - `noorani_qaida.pdf` (53 pages, 4.30 MB)
  - `noorani_qaida_text.json` (53 page records with `pageNumber`, `bookPageNumber`, `takhtiNumber`, `titleArabic`, `titleUrdu`, `titleEnglish`, `arabicText`, `urduNotes`)
  - `Amiri-Regular.ttf` & `ScheherazadeNew-Regular.ttf`
- **Persistence Keys**:
  - `last_read_noorani_qaida_page` (`Int`, default `1`, range `1...53`)

## 3. Plan A — Android Primary Implementation
- **Files Created**:
  - `app/src/main/java/com/example/ui/qaida/NooraniQaidaPdfReader.kt` (`NooraniQaidaReaderActivity`, `NooraniQaidaPdfController` using `PdfRenderer` + 5-page `LruCache<Int, Bitmap>`, `DecoratedZoomableQaidaPageCard`, `NooraniQaidaLessonIndexBottomSheet`, `NooraniQaidaExerciseMcqBottomSheet`)
  - `app/src/main/java/com/example/ui/qaida/NooraniQaidaExerciseQuizCatalog.kt`
- **Files Modified**:
  - `app/src/main/AndroidManifest.xml`, `MainActivity.kt`, `TopIslamicBar.kt`, `HomeScreen.kt`, `LearnIslamScreen.kt`

## 4. Plan B — iOS Mirrored Native Implementation
- **Files Created**:
  - `BayanAlHuda/Modules/NooraniQaida/NooraniQaidaPDFKitView.swift` (`PDFDocument` + `NSCache<NSNumber, UIImage>` + `UIScrollView` pinch-to-zoom `1.0...4.0` & double-tap zoom)
  - `BayanAlHuda/Modules/NooraniQaida/NooraniQaidaReaderView.swift` (`TabView` page style, `IslamicCardDecorator`, `Amiri-Regular` RTL header & selectable text sheet, searchable 53-page `LazyVGrid` index sheet, `@AppStorage("last_read_noorani_qaida_page")`)
  - `BayanAlHuda/Modules/NooraniQaida/NooraniQaidaQuizCatalog.swift` (35 Basic/Intermediate/Hard MCQs + `NooraniQaidaMcqSheetView`)
