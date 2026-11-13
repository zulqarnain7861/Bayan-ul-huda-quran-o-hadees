# Bayan Al-Huda — Signed Android Release APK & Standalone iOS Source Bundle (`zip/`)

This `zip/` directory contains both the **signed, installable Android Release APK** and the **complete standalone iOS repository archive**, kept cleanly separated so there are zero conflicts when pushing the Android repository to GitHub.

## 1. Signed Installable Android App (`zip/BAYAN-AL-HUDA-v1.0.2-release.apk`)

- **APK File**: `zip/BAYAN-AL-HUDA-v1.0.2-release.apk`
- **SHA-256 Checksum**: `d030d8a252f62cdb6c62e68b7595964f946938bdd3c97d58505e7e6a23be6734` (`zip/BAYAN-AL-HUDA-v1.0.2-SHA256.txt`)
- **Signing Certificate SHA-256**: `4eb2beaf05cb3550f5480ce4fb529530a340f1473aa95305ade04b118e8a7b72`
- **Signed In-App Updater Manifest**: `zip/update.json` & `zip/update.json.sig`
- **Package Name**: `com.bayanulhuda.com` (`versionName = "1.0.2"`, `versionCode = 8`)
- **How to Install on Android**:
  1. Download `zip/BAYAN-AL-HUDA-v1.0.2-release.apk` directly from the AI Studio file explorer (or after exporting the project ZIP).
  2. Open `BAYAN-AL-HUDA-v1.0.2-release.apk` on your Android device and tap **Install**.
  3. When you push this project to GitHub (`zulqarnain7861/Bayan-ul-huda-quran-o-hadees`), `.github/workflows/android-release.yml` automatically builds, signs, and attaches `BAYAN-AL-HUDA-v1.1.0-release.apk` + `update.json` to your GitHub Releases.

---

## 2. Standalone iOS Repository Bundle (`zip/Bayan-Al-Huda-iOS-full-source.zip`)

- **Archive File**: `zip/Bayan-Al-Huda-iOS-full-source.zip` (4.43 MB)
- **Included Inside**:
  - `BayanAlHuda/` — Complete SwiftUI + PDFKit + AVPlayer + Core Data iOS source code (Quran Reader with Tajweed & 280-Reciter Picker, Searchable Hadith Library, Video Library with YouTube fallback tiers, 53-page HD Noorani Qaida PDF Reader with 35 Basic/Intermediate/Hard MCQs, Global Search, and full 6-category Azkar/Masnoon Duas with Tasbih counter & Quranic verse audio).
  - `BayanAlHuda/Resources/` — Byte-for-byte synced `noorani_qaida.pdf` (4.30 MB), `noorani_qaida_text.json` (53 pages), `Amiri-Regular.ttf`, and `ScheherazadeNew-Regular.ttf`.
  - `Docs/FEATURE_TEMPLATE.md` — Dual-platform feature specification template + complete side-by-side **Azkar/Duas** and **Noorani Qaida** implementation plans.
  - `Docs/SYNC_GUIDE.md`, `Docs/ANDROID_TO_IOS_MAPPING.md`, `Docs/DESIGN_SYSTEM.md`.
  - `project.yml` (XcodeGen specification) & `.github/workflows/ios-ci.yml` (`macos-14` CI workflow that builds and packages the installable `.ipa` file).

### How to Extract & Upload to Your New iOS GitHub Repository (`Bayan-Al-Huda-iOS`)

```bash
unzip Bayan-Al-Huda-iOS-full-source.zip
cd Bayan-Al-Huda-iOS
git init -b main
git add .
git commit -m "Initial commit: Bayan-Al-Huda-iOS v1.0.2 (build 8) with full Android parity"
git remote add origin https://github.com/zulqarnain7861/Bayan-Al-Huda-iOS.git
git push -u origin main
```
