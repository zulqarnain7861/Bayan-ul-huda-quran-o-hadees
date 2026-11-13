# Bayan Al-Huda Shared Design System (Android & iOS)

Both `Bayan-ul-huda-quran-o-hadees` (Android) and `Bayan-Al-Huda-iOS` (iOS) share a unified Islamic manuscript aesthetic inspired by classical Madinah mushaf illumination.

---

## 1. Shared Color Tokens

| Semantic Token | Hex Value | Android (`Theme.kt` / `Color.kt`) | iOS (`IslamicTheme.swift`) | Usage |
| :--- | :--- | :--- | :--- | :--- |
| **Emerald Primary** | `#0A4D3C` | `Color(0xFF0A4D3C)` | `IslamicPalette.emeraldPrimary` | Top navigation bars, primary buttons, active badges |
| **Emerald Deep** | `#073B2E` | `Color(0xFF073B2E)` | `IslamicPalette.emeraldDeep` | Bottom control bars, dark headers, selected text on gold |
| **Emerald Night** | `#062920` | `Color(0xFF062920)` | `IslamicPalette.emeraldNight` | Reader outer canvas gradient background |
| **Gold Primary** | `#D4AF37` | `Color(0xFFD4AF37)` | `IslamicPalette.goldPrimary` | Illuminated borders, active chips, Arabic calligraphy accents |
| **Gold Antique** | `#C59B27` | `Color(0xFFC59B27)` | `IslamicPalette.goldAntique` | Inner frame borders around white manuscript pages |
| **Parchment Light** | `#FFFBF2` | `Color(0xFFFFFBF2)` | `IslamicPalette.parchmentLight` | Center radial gradient of decorated page cards |
| **Parchment Warm** | `#F3E6CE` | `Color(0xFFF3E6CE)` | `IslamicPalette.parchmentWarm` | Mid radial gradient of decorated page cards |
| **Parchment Edge** | `#E6D4B3` | `Color(0xFFE6D4B3)` | `IslamicPalette.parchmentEdge` | Outer frame tint around white PDF/Quran pages |
| **Tajweed Highlight** | `#1B5E20` | `Color(0xFF1B5E20)` | `IslamicPalette.tajweedHighlight` | Active ayah / cue-point synchronized highlight |

---

## 2. Shared Typography & RTL Rules

1. **Bundled Arabic Fonts**:
   - `Amiri-Regular.ttf` (Primary Quranic, Hadith, Duas, and Noorani Qaida display font)
   - `ScheherazadeNew-Regular.ttf` (Secondary Uthmani & subtitle calligraphy font)
2. **Right-to-Left (RTL) Enforcement**:
   - All Arabic and Urdu containers must explicitly enforce RTL text direction:
     - **Android**: `android:textDirection="rtl"` / `TextDirection.Rtl`
     - **iOS**: `.environment(\.layoutDirection, .rightToLeft)` and `.multilineTextAlignment(.trailing)`
3. **Accessibility & VoiceOver / TalkBack**:
   - Every interactive control has a minimum `44pt` (iOS) / `48dp` (Android) touch target.
   - All Quranic verses, Hadith cards, Noorani Qaida pages, and audio transport buttons provide explicit `.accessibilityLabel` (iOS) / `contentDescription` (Android).
