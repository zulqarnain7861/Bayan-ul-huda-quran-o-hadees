# Bayan Al-Huda — Dual-Platform Feature Specification & Implementation Plan (`FEATURE_TEMPLATE.md`)

- **Primary Source of Truth (Android)**: `https://github.com/zulqarnain7861/Bayan-ul-huda-quran-o-hadees` (`Kotlin` + `Jetpack Compose` + `Room` + `Media3 ExoPlayer` + `PdfRenderer`)
- **Companion Mirrored Repository (iOS/iPadOS)**: `https://github.com/zulqarnain7861/Bayan-Al-Huda-iOS` (`Swift 5.9` + `SwiftUI` + `Core Data` + `AVPlayer` + `PDFKit`)
- **Baseline Version**: `versionCode = 8` (`CURRENT_PROJECT_VERSION = 8`), `versionName = "1.0.2"` (`MARKETING_VERSION = 1.0.2`)

---

## Part 1: Reusable Dual-Platform Feature Specification Template

Use this template for every new feature before writing code. All features must be specified and verified on **Android first**, then ported to **iOS** with full visual, data, and behavioral parity.

```markdown
# Feature ID: FEAT-XXX — <Feature Title>
- **Target Release**: vX.Y.Z (Build N)
- **Status**: [Draft | Android Verified | iOS Ported | Parity Complete]

## 1. Shared Data & Asset Contract
- **Bundled Assets (Byte-for-Byte Identical)**: `app/src/main/assets/<file>` <-> `BayanAlHuda/Resources/<file>`
- **Preference Keys (`SharedPreferences` <-> `@AppStorage`)**: `<key_name>`
- **Database Entities (`Room` <-> `Core Data`)**: `<entity_name>`
- **Authenticity & Security Rules**: Zero fabricated religious text; strict RTL (`rtl` / `.rightToLeft`) for Arabic; HTTPS-only network calls.

## 2. Side-by-Side Architecture Mapping
| Architectural Layer | Android (`Kotlin` / `Jetpack Compose`) | iOS (`Swift` / `SwiftUI`) |
| :--- | :--- | :--- |
| **Data Model & Provider** | `com.example.data.model.<Model>.kt` | `BayanAlHuda/Modules/<Module>/<Model>.swift` |
| **Local Persistence** | `com.example.data.local.<Dao>.kt` (`Room`) | `BayanAlHuda/Core/Database/PersistenceController.swift` (`Core Data`) |
| **Media / Rendering** | `Media3 ExoPlayer` / `PdfRenderer` | `AVFoundation AVPlayer` / `PDFKit` |
| **UI Container & Screen** | `com.example.ui.<module>.<Screen>.kt` | `BayanAlHuda/Modules/<Module>/<View>.swift` |
| **Verification Test** | `app/src/test/java/com/example/<Test>.kt` (`Robolectric`) | `BayanAlHudaTests/BayanAlHudaParityTests.swift` (`XCTest`) |
```

---

## Part 2: Complete Feature Implementation Plan — `FEAT-005: Azkar & Masnoon Duas (الأذكار والأدعية المسنونة)`

### 1. Feature Scope & Shared Domain Rules
1. **Six Authentic Categories (`DuaCategory`)**:
   - `MORNING_EVENING` ("Morning & Evening" • `أذكار الصباح والمساء`)
   - `AFTER_SALAH` ("After Salah (Namaz)" • `أذكار بعد الصلاة`)
   - `SLEEP_WAKE` ("Sleep, Wake & Dress" • `أذكار النوم والاستيقاظ`)
   - `HOME_MASJID` ("Home, Masjid & Daily" • `أذكار المنزل والمسجد`)
   - `QURANIC_RABBANA` ("Quranic & Prophetic" • `الأدعية القرآنية والنبوية`)
   - `SPECIAL_OCCASIONS` ("Special Occasions & Ramadan" • `أدعية المناسبات الخاصة`)
2. **Multilingual & Roman-Urdu Search**:
   - Every `DailyDuaItem` includes:
     - `id`: Stable identifier (e.g., `dua_ayat_al_kursi`, `dua_sayyidul_istighfar`, `dua_ramadan_sehri_niyyah`, `dua_ramadan_iftar`)
     - `title`: English + Urdu script title
     - `arabicText`: Full vocalized Uthmani/Indo-Pak Arabic text
     - `transliteration`: Accurate Roman transliteration
     - `englishTranslation` & `urduTranslation`: Complete verified translations
     - `authenticReference`: Canonical Surah:Ayah or Hadith citation (e.g., `Surah Al-Baqarah 2:255`, `Sahih al-Bukhari 6306`, `Sunan Abi Dawud 2357`)
     - `recommendedCount`: Sunnah repetition target (e.g., `1x`, `3x`, `33x`, `100x`) with an interactive per-dua Tasbih counter
     - `quranSurahAyah`: Optional `(surah, ayah)` pair for Quranic Duas (`2:255`, `2:201`, `20:114`, `43:13`)
     - `searchKeywordsRomanUrduAndAliases`: Roman Urdu search aliases (e.g., `"sone ki dua"`, `"khana khane ki dua"`, `"sehri ki dua"`, `"iftar ki dua"`, `"safar ki dua"`)
3. **Strict Audio Authenticity Rule (Zero AI/Synthetic Voice)**:
   - **Quranic Duas** (where `quranSurahAyah != null`) stream authentic human Qari recitation mapped to that exact Surah and Ayah (or human-recorded Hisn al-Muslim audio URL).
   - **Non-Quranic Hadith Duas** without a verified human recording must NEVER use synthetic TTS or map to a fake Surah/Ayah; they must display `"No audio available"` (`DuaAudioController.REAL_VOICE_COMING_SOON_MESSAGE`).

### 2. Side-by-Side Architecture: Android (`Kotlin/Compose`) vs. iOS (`Swift/SwiftUI`)

| Component | Android Implementation (`Bayan-ul-huda-quran-o-hadees`) | iOS Implementation (`Bayan-Al-Huda-iOS`) |
| :--- | :--- | :--- |
| **Domain Models & Catalog** | `app/src/main/java/com/example/data/model/DailyDuasDataProvider.kt` (`DuaCategory`, `SpecialOccasionType`, `DailyDuaItem`, `DailyDuasDataProvider.allDuas`, `ramadanSpecialDuas`) | `BayanAlHuda/Modules/Settings/DuasAndSettingsView.swift` (`DuaCategory`, `SpecialOccasionType`, `DailyDuaItem`, `DailyDuasCatalog.allDuas`) |
| **Audio Controller** | `app/src/main/java/com/example/ui/audio/DuaAudioController.kt` using `Media3 ExoPlayer` for Quranic verse / human audio + `"No audio available"` guard | `DuaAudioPlayerCoordinator` in `DuasAndSettingsView.swift` using `AVFoundation.AVPlayer` + `"No audio available"` banner for non-audio Hadith duas |
| **Bookmark & Tasbih Persistence** | `BayanDatabase` (`BookmarkDao` with `itemType = "DUA"`) + `SharedPreferences` for Tasbih counts | `PersistenceController.swift` (`Core Data` `BookmarkedItemEntity` with `category = "dua"`) + `@AppStorage` for Tasbih progress |
| **UI Layout & Cards** | Jetpack Compose `LazyColumn`, category `FilterChip` row, decorated Islamic card with RTL `Amiri`/`ScheherazadeNew` Arabic text, interactive Tasbih counter chip, Share & Bookmark actions | SwiftUI `ScrollView` + `LazyVStack`, horizontal category chip bar, `.islamicIlluminatedCard()` modifier, `.forceArabicRTL()` with `Amiri-Regular.ttf`, Tasbih counter button, `ShareLink` / `UIActivityViewController`, and Bookmark toggle |
| **Unit / Parity Tests** | `SehriIftarAndAboutVerificationTest.kt` (`special Ramadan duas and concise deduplicated VERIFIED sources`, `Quranic duas map only to exact Quran verses`) | `BayanAlHudaTests/BayanAlHudaParityTests.swift` (`testAzkarAndMasnoonDuasCatalogAndQuranicAudioMapping`) |

### 3. Side-by-Side Code Comparison (Android Kotlin/Compose vs. iOS Swift/SwiftUI)

#### A. Data Model & Audio Guard
**Android (`Kotlin`)**:
```kotlin
data class DailyDuaItem(
  val id: String,
  val title: String,
  val category: DuaCategory,
  val occasionBadge: String,
  val arabicText: String,
  val transliteration: String,
  val englishTranslation: String,
  val urduTranslation: String,
  val authenticReference: String,
  val virtueAndTiming: String,
  val recommendedCount: Int = 1,
  val quranSurahAyah: Pair<Int, Int>? = null,
  val searchKeywordsRomanUrduAndAliases: String = "",
  val humanAudioUrl: String? = null
)
```

**iOS (`Swift`)**:
```swift
struct DailyDuaItem: Identifiable, Equatable {
    let id: String
    let title: String
    let category: DuaCategory
    let occasionBadge: String
    let arabicText: String
    let transliteration: String
    let englishTranslation: String
    let urduTranslation: String
    let authenticReference: String
    let virtueAndTiming: String
    let recommendedCount: Int
    let quranSurahAyah: (surah: Int, ayah: Int)?
    let searchKeywordsRomanUrduAndAliases: String
    let humanAudioUrl: String?
}
```

#### B. Declarative Card UI with RTL Arabic, Tasbih Counter & Bookmark
**Android (`Jetpack Compose`)**:
```kotlin
ElevatedCard(modifier = Modifier.fillMaxWidth()) {
  Column(modifier = Modifier.padding(16.dp)) {
    Text(text = dua.title, style = MaterialTheme.typography.titleMedium)
    Text(
      text = dua.arabicText,
      fontFamily = AmiriFontFamily,
      fontSize = 24.sp,
      textAlign = TextAlign.Right,
      modifier = Modifier.fillMaxWidth()
    )
    Text(text = dua.urduTranslation, textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth())
    Text(text = dua.englishTranslation, style = MaterialTheme.typography.bodyMedium)
  }
}
```

**iOS (`SwiftUI`)**:
```swift
VStack(alignment: .trailing, spacing: 10) {
    Text(dua.title)
        .font(.system(size: 16, weight: .bold))
        .frame(maxWidth: .infinity, alignment: .leading)
    Text(dua.arabicText)
        .font(IslamicTheme.arabicFont(size: 25))
        .foregroundColor(IslamicTheme.emeraldDeep)
        .forceArabicRTL()
    Text(dua.urduTranslation)
        .font(IslamicTheme.arabicFont(size: 18))
        .foregroundColor(IslamicTheme.emeraldPrimary)
        .forceArabicRTL()
    Text(dua.englishTranslation)
        .font(.system(size: 14))
        .foregroundColor(.secondary)
        .frame(maxWidth: .infinity, alignment: .leading)
}
.islamicIlluminatedCard(cornerRadius: 18, shadowRadius: 5)
```

---

## Part 3: Complete Feature Implementation Plan — `FEAT-004: Native HD Noorani Qaida PDF Reader & 3-Tier Quizzes`

| Layer / Requirement | Android (`Bayan-ul-huda-quran-o-hadees`) | iOS (`Bayan-Al-Huda-iOS`) |
| :--- | :--- | :--- |
| **Source PDF & JSON** | `app/src/main/assets/noorani_qaida.pdf` (53 pages, 4.30 MB) + `noorani_qaida_text.json` | `BayanAlHuda/Resources/noorani_qaida.pdf` (byte-for-byte identical) + `noorani_qaida_text.json` |
| **Native PDF Renderer** | `android.graphics.pdf.PdfRenderer` with `LruCache<Int, Bitmap>` (`NooraniQaidaPdfReader.kt`) | `PDFKit` (`PDFDocument` / `PDFPage`) with `NSCache<NSNumber, UIImage>` (`NooraniQaidaPDFKitView.swift`) |
| **Pinch-to-Zoom & Pager** | `HorizontalPager` + `detectTransformGestures` (`1f..4f` zoom) + double-tap toggle | `TabView` (`PageTabViewStyle`) + `UIScrollView` (`minimumZoomScale = 1.0`, `maximumZoomScale = 4.0`) + double-tap |
| **3-Tier Exercise Quizzes** | `NooraniQaidaExerciseQuizCatalog.kt` (35 MCQs across Basic, Intermediate, Hard) | `NooraniQaidaQuizCatalog.swift` (35 identical MCQs across Basic, Intermediate, Hard) |
