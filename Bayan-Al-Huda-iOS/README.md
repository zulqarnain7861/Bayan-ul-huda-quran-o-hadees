# Bayan-Al-Huda-iOS (البيان الهدى)

Native iOS & iPadOS companion repository for **Bayan Al-Huda: Quran, Hadith & Noorani Qaida**, mirrored from the primary Android source of truth ([`zulqarnain7861/Bayan-ul-huda-quran-o-hadees`](https://github.com/zulqarnain7861/Bayan-ul-huda-quran-o-hadees), baseline `versionCode = 8`, `versionName = "1.0.2"`).

## Repository Structure

```text
Bayan-Al-Huda-iOS/
├── BayanAlHuda/                  # Main Xcode target sources
│   ├── BayanAlHudaApp.swift
│   ├── ContentView.swift
│   ├── Core/                     # Networking, CoreData, Utilities, IslamicTheme
│   ├── Modules/                  # Feature modules: Quran, Hadith, Video, NooraniQaida, Search, Settings
│   ├── Resources/                # Byte-for-byte synced PDF, JSON, and Arabic TTF fonts
│   └── UI/                       # IslamicCardDecorator & reusable components
├── BayanAlHudaTests/             # XCTest parity unit tests
├── Docs/
│   ├── SYNC_GUIDE.md             # Living document tracking Android <-> iOS feature parity
│   ├── ANDROID_TO_IOS_MAPPING.md # Complete file/class mapping between repositories
│   ├── FEATURE_TEMPLATE.md       # Dual-platform feature specification & Noorani Qaida plan
│   └── DESIGN_SYSTEM.md          # Shared Emerald & Gold palette and Arabic typography rules
├── tools/
│   └── sync_shared_assets_from_android.sh
├── project.yml                   # XcodeGen project specification
└── .github/workflows/ios-ci.yml  # macOS 14 CI workflow building & packaging the .ipa artifact
```

## Generating the Xcode Project & Building `.ipa`

```bash
# 1. Sync shared assets from the primary Android repository
./tools/sync_shared_assets_from_android.sh ../

# 2. Generate BayanAlHuda.xcodeproj using XcodeGen
brew install xcodegen
xcodegen generate

# 3. Open in Xcode or build via CLI
open BayanAlHuda.xcodeproj
```
