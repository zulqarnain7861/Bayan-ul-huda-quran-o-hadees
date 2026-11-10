# BAYAN AL-HUDA — Quran, Hadith, Tafsir, Duas & Islamic Library

**Package Name:** `com.bayanulhuda.com`  
**Application Name:** `BAYAN AL-HUDA`  
**Developer:** Zulqarnain Ahmad Sheikh (`sheikhzulqar@gmail.com`)  
**Official Repository:** [https://github.com/zulqarnain7861/Bayan-ul-huda-quran-o-hadees](https://github.com/zulqarnain7861/Bayan-ul-huda-quran-o-hadees)

---

## Release and Installation

Follow the steps below to download the latest digitally signed **BAYAN AL-HUDA** Android package (`.apk`), verify its cryptographic integrity using the published SHA-256 checksum, and install or update it on your Android device.

### 1. Download the Latest Signed APK from GitHub Releases

1. Open the **[Official GitHub Releases Page](https://github.com/zulqarnain7861/Bayan-ul-huda-quran-o-hadees/releases/latest)**.
2. Scroll down to the **Assets** section of the latest release.
3. Download the signed Android APK file and its accompanying SHA-256 checksum file:

| Release Asset | Purpose |
| :--- | :--- |
| **`BAYAN-AL-HUDA-v1.1.0-release.apk`** *(or latest `BAYAN-AL-HUDA-vX.Y.Z-release.apk`)* | Primary production-signed Android application package (`.apk`) ready to install on Android 7.0+ (API 24+) devices. |
| **`Bayan-al-Huda.apk`** | Canonical direct-download signed APK alias for the latest release. |
| **`BAYAN-AL-HUDA-v1.1.0-SHA256.txt`** *(or `SHA256SUMS.txt`)* | Official SHA-256 cryptographic checksum file for verifying APK file integrity before installation. |
| **`update.json`** | Cryptographically signed (RSA-2048 + SHA-256) metadata manifest used by the built-in In-App Updater. |

> **Important — Download `.apk`, NOT Source Code Archives:**  
> GitHub automatically attaches **`Source code (zip)`** and **`Source code (tar.gz)`** to every release. Those archives contain raw Kotlin/Gradle source code for developers and **cannot** be installed on Android. Always download the **`.apk`** file (`BAYAN-AL-HUDA-v1.1.0-release.apk` or `Bayan-al-Huda.apk`).
>
> **Backup Official Updates Folder (Google Drive):**  
> If GitHub Releases is unreachable in your region, official signed APKs are also mirrored at:  
> [Official Google Drive APK Folder](https://drive.google.com/drive/folders/14i90MpNPedlnBk3l53L_yplzzFNIa4N3)

---

### 2. Verify APK Integrity Using the SHA-256 Checksum

Before installing the APK on your device, verify that the downloaded file matches the official SHA-256 hash published in `BAYAN-AL-HUDA-v1.1.0-SHA256.txt` or `SHA256SUMS.txt` on the release page.

#### Linux / macOS Terminal
Download both `BAYAN-AL-HUDA-v1.1.0-release.apk` and `BAYAN-AL-HUDA-v1.1.0-SHA256.txt` into the same directory, then run:

```bash
# Option A: Verify automatically against the checksum file
sha256sum -c BAYAN-AL-HUDA-v1.1.0-SHA256.txt

# Option B: Compute the hash manually and compare with the GitHub Release page
sha256sum BAYAN-AL-HUDA-v1.1.0-release.apk
# Or on macOS:
shasum -a 256 BAYAN-AL-HUDA-v1.1.0-release.apk
```
Confirm that the output prints `BAYAN-AL-HUDA-v1.1.0-release.apk: OK` (or that the 64-character hexadecimal digest matches the SHA-256 checksum listed in the release notes).

#### Windows (PowerShell)
Open PowerShell in your `Downloads` folder and run:

```powershell
Get-FileHash .\BAYAN-AL-HUDA-v1.1.0-release.apk -Algorithm SHA256
```
Compare the `Hash` value printed in PowerShell with the SHA-256 checksum inside `BAYAN-AL-HUDA-v1.1.0-SHA256.txt` (case-insensitive).

#### Android Device (Termux / Hash Checker)
If you downloaded the APK directly on your Android phone or tablet, run in Termux:

```bash
sha256sum ~/storage/downloads/BAYAN-AL-HUDA-v1.1.0-release.apk
```
*(Optional — Advanced Developer Check)* You can also inspect the V2/V3 X.509 signing certificate directly using Android SDK `apksigner`:

```bash
apksigner verify --print-certs -v BAYAN-AL-HUDA-v1.1.0-release.apk
```

---

### 3. Install and Update on Android Devices

#### A. Fresh Installation on Android
1. Transfer or download the verified **`BAYAN-AL-HUDA-v1.1.0-release.apk`** file onto your Android device.
2. Tap the `.apk` file in your browser's **Downloads** list or in the **Files by Google** / system file manager app.
3. If prompted with *"For your security, your phone currently isn't allowed to install unknown apps from this source"*:
   - Tap **Settings** on the prompt.
   - Toggle on **Allow from this source** for your browser or file manager.
   - Press the back button to return to the installer dialog.
4. Tap **Install** in Android's official Package Installer and wait for installation to complete, then tap **Open**.

#### B. Automatic & Manual In-App Updates (Built-In Secure Updater)
Once **BAYAN AL-HUDA** is installed, you do not need to manually download future updates from a browser:
1. Open **BAYAN AL-HUDA** and navigate to **Settings → About → Check for Updates** (the app also checks automatically on startup).
2. When a newer release (`versionCode` > installed `BuildConfig.VERSION_CODE`) is published on GitHub Releases, an update dialog appears showing the new version, APK size, and release notes.
3. Tap **Download Update**. Before launching the Android Package Installer, the app automatically performs a **7-step security verification**:
   - Rejects HTML error pages or source-code ZIP archives.
   - Validates Android APK archive structure (`AndroidManifest.xml` + `classes.dex`).
   - Verifies the RSA-2048 cryptographic signature when `update.json` is attached.
   - Computes and compares the streaming **SHA-256** checksum against the official release hash.
   - Confirms the package name matches `com.bayanulhuda.com`.
   - Enforces anti-downgrade protection (`apkVersionCode > installedVersionCode`).
   - Confirms the X.509 signing certificate SHA-256 fingerprint matches the currently installed app.
4. Confirm the update in Android's Package Installer. Updating over an existing installation preserves all your **Bookmarks, Khatam Tracker progress, Tasbih counters, Offline Quran audio, and Hadith/Library downloads** because every official release is signed with the same developer release key.

---

## Automated Signed APK Build & Release Pipeline (GitHub Actions)

This repository includes a production-ready CI/CD release pipeline in [`.github/workflows/android-release.yml`](.github/workflows/android-release.yml) that automatically compiles, signs, verifies, scans, and publishes official `.apk` assets to GitHub Releases.

### Required GitHub Actions Secrets
Configure these secrets under **GitHub Repository → Settings → Secrets and variables → Actions**:

| Secret Name | Description |
| :--- | :--- |
| `ANDROID_KEYSTORE_BASE64` | Base64-encoded string of your private release `.jks` / `.keystore` file (`base64 -w 0 release.jks`). |
| `ANDROID_KEYSTORE_PASSWORD` | Password protecting the release keystore file. |
| `ANDROID_KEY_ALIAS` | Key alias inside the release keystore. |
| `ANDROID_KEY_PASSWORD` | Password for the signing key alias. |
| `UPDATE_MANIFEST_PRIVATE_KEY_PEM` | *(Optional)* RSA-2048 PKCS#8 private key PEM used to sign `update.json` for cryptographic manifest verification. |

### How to Publish a New Signed APK Release

#### Option A: Push a Version Tag
```bash
git tag v1.1.0
git push origin v1.1.0
```

#### Option B: Trigger Manually from the GitHub Web UI
1. Open the repository on GitHub and click the **Actions** tab.
2. Select **BAYAN AL-HUDA Signed APK Release** in the left sidebar.
3. Click **Run workflow**, enter the release tag (e.g., `v1.1.0`), and click **Run workflow**.

### What the Automated Workflow Does
1. **Validates Signing Secrets**: Fails fast with clear instructions if any required `ANDROID_KEYSTORE_*` secret is missing; decodes the keystore outside the workspace (`$RUNNER_TEMP`) and deletes it in an `always()` cleanup step.
2. **Runs Security & Play Protect Pre-Publish Audits**: Checks `AndroidManifest.xml` permissions, scans for hardcoded secrets, verifies clean `WebView` configurations, and runs unit tests.
3. **Builds & Digitally Signs the Release APK**: Executes `./gradlew :app:assembleRelease` using the production release signing config.
4. **Verifies APK Signature & Package Identity**: Runs `apksigner verify --verbose --print-certs` and `aapt2 dump badging` to confirm `com.bayanulhuda.com` and V2/V3 signing schemes.
5. **Publishes Official Assets to GitHub Releases**: Generates `BAYAN-AL-HUDA-<tag>-release.apk`, `BAYAN-AL-HUDA-<tag>-SHA256.txt`, `Bayan-al-Huda.apk`, `SHA256SUMS.txt`, and `update.json` and attaches them to the GitHub Release.
