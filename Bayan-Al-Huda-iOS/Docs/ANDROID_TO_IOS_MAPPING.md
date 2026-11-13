# Android to iOS Component, Architecture & File Mapping

This living reference maps every core architectural layer, UI component, persistence store, and source file in the primary Android repository (`Bayan-ul-huda-quran-o-hadees`) to its native iOS equivalent in `Bayan-Al-Huda-iOS`.

---

## 1. Framework & System API Mapping

| Capability | Android Primary (`Kotlin` / `Jetpack Compose`) | iOS Native Equivalent (`Swift 5.9+` / `SwiftUI` iOS 15+) |
| :--- | :--- | :--- |
| **Declarative UI** | Jetpack Compose (`@Composable`, Material 3) | SwiftUI (`View`, `NavigationStack` / `NavigationView`) |
| **Root Navigation** | `Scaffold` + `NavigationBar` + `TopIslamicBar` (`MainActivity.kt`) | `TabView` + `IslamicTopHeaderBar` (`ContentView.swift`) |
| **Horizontal Paging** | `ViewPager2` / Compose `HorizontalPager(beyondViewportPageCount = 2)` | `TabView` with `.tabViewStyle(.page(indexDisplayMode: .never))` + `UIScrollView` zoom bridge |
| **Scrolling Lists / Grids** | `RecyclerView` / `LazyColumn` / `LazyVerticalGrid` | `List`, `ScrollView` + `LazyVStack`, `LazyVGrid` |
| **PDF Rendering** | `android.graphics.pdf.PdfRenderer` + `LruCache<Int, Bitmap>` | `PDFKit` (`PDFDocument`, `PDFPage.thumbnail(of:for:)`, `NSCache<NSNumber, UIImage>`) |
| **Pinch-to-Zoom & Pan** | `rememberTransformableState` + `detectTapGestures(onDoubleTap)` | `UIScrollView` (`minimumZoomScale = 1.0`, `maximumZoomScale = 4.0`) + `MagnificationGesture` |
| **Audio & Video Playback** | `Media3 ExoPlayer` + `MediaPlayer` + `YouTube IFrame Player` | `AVFoundation` (`AVPlayer`, `AVQueuePlayer`, `AVAudioSession`) + `WKWebView` YouTube Fallback |
| **Key-Value Preferences** | `SharedPreferences` (`bayan_learn_islam_prefs`, `bayan_prayer_and_alarm_prefs`) | `@AppStorage` / `UserDefaults.standard` |
| **Structured Database** | `Room` (`BayanDatabase`, `AyahEntity`, `HadithEntity`, `BookmarkEntity`) | `Core Data` (`PersistenceController.swift`) + JSON Bundle Seed Loader |
| **Custom Bundled Fonts** | `Typeface.createFromAsset(assets, "fonts/Amiri-Regular.ttf")` | `UIAppFonts` in `Info.plist` + `Font.custom("Amiri-Regular", size:)` |
| **RTL Layout Enforcement** | `android:textDirection="rtl"` / `LocalLayoutDirection provides LayoutDirection.Rtl` | `.environment(\.layoutDirection, .rightToLeft)` + `.multilineTextAlignment(.trailing)` |
| **Prayer & Fajr Alarms** | `AlarmManager` + `AzanForegroundService` | `UNUserNotificationCenter` + `UNCalendarNotificationTrigger` with custom Adhan sound |

---

## 2. File-by-File Repository Mapping

| Module | Android Source File (`Bayan-ul-huda-quran-o-hadees`) | iOS Source File (`Bayan-Al-Huda-iOS`) |
| :--- | :--- | :--- |
| **App Entry** | `app/src/main/java/com/example/MainActivity.kt` | `BayanAlHuda/BayanAlHudaApp.swift` & `BayanAlHuda/ContentView.swift` |
| **Theme & Palette** | `app/src/main/java/com/example/ui/theme/Theme.kt`, `Color.kt`, `Type.kt` | `BayanAlHuda/Core/Theme/IslamicTheme.swift` |
| **Decorated Cards** | `app/src/main/java/com/example/ui/components/IslamicComponents.kt`, `TopIslamicBar.kt` | `BayanAlHuda/UI/IslamicCardDecorator.swift` |
| **Database & Storage** | `app/src/main/java/com/example/data/local/BayanDatabase.kt`, `InitialDataLoader.kt` | `BayanAlHuda/Core/Database/PersistenceController.swift` |
| **Networking & Security** | `app/src/main/java/com/example/data/remote/SecurityIntegrityGuard.kt`, `Mp3QuranTimingClient.kt` | `BayanAlHuda/Core/Networking/NetworkClient.swift` |
| **Noorani Qaida Reader** | `app/src/main/java/com/example/ui/qaida/NooraniQaidaPdfReader.kt` | `BayanAlHuda/Modules/NooraniQaida/NooraniQaidaReaderView.swift` & `NooraniQaidaPDFKitView.swift` |
| **Noorani Qaida MCQs** | `app/src/main/java/com/example/ui/qaida/NooraniQaidaExerciseQuizCatalog.kt` | `BayanAlHuda/Modules/NooraniQaida/NooraniQaidaQuizCatalog.swift` |
| **Quran & Audio Sync** | `app/src/main/java/com/example/ui/screens/QuranScreen.kt`, `AudioPlayerManager.kt` | `BayanAlHuda/Modules/Quran/QuranReaderView.swift` & `QuranAudioSyncService.swift` |
| **Hadith Library** | `app/src/main/java/com/example/ui/screens/HadithScreen.kt`, `ExtendedHadithDataProvider.kt` | `BayanAlHuda/Modules/Hadith/HadithLibraryView.swift` |
| **Video Library** | `app/src/main/java/com/example/player/YtEmbedHost.kt`, `LibraryScreen.kt` | `BayanAlHuda/Modules/Video/VideoLibraryView.swift` |
| **Global Search** | `app/src/main/java/com/example/ui/screens/SearchScreen.kt` | `BayanAlHuda/Modules/Search/GlobalSearchView.swift` |
| **Duas & Settings** | `app/src/main/java/com/example/ui/screens/DailyDuasScreen.kt`, `AboutScreen.kt` | `BayanAlHuda/Modules/Settings/DuasAndSettingsView.swift` |
| **Shared PDF Asset** | `app/src/main/assets/noorani_qaida.pdf` | `BayanAlHuda/Resources/noorani_qaida.pdf` |
| **Shared Qaida JSON** | `app/src/main/assets/noorani_qaida_text.json` | `BayanAlHuda/Resources/noorani_qaida_text.json` |
| **Shared Fonts** | `app/src/main/assets/fonts/Amiri-Regular.ttf`, `ScheherazadeNew-Regular.ttf` | `BayanAlHuda/Resources/Fonts/Amiri-Regular.ttf`, `ScheherazadeNew-Regular.ttf` |
