# Google Play Developer Program Policy & Security Audit Checklist

## 1. Dual Distribution Architecture (Google Play vs. Direct APK)

Per Google Play Developer Program Policies (Device and Network Abuse / Self-Updating policy), an app distributed via Google Play may **not** download or install executable code (such as `.apk`, `.dex`, `.jar`, or `.so` files) from a source other than Google Play.

**Bayan al-Huda** implements a strict two-mode distribution architecture (`DistributionChannelDetector.kt`):

### Mode A: Google Play Store Installation (`com.android.vending`)
- **Detection:** `PackageManager.getInstallSourceInfo` / `getInstallerPackageName` identifies `com.android.vending` or `com.google.android.feedback`.
- **Behavior:**
  - Direct GitHub APK downloading and `PackageInstaller` invocation are **disabled and blocked in code**.
  - Tapping update buttons opens the official Google Play Store listing (`market://details?id=com.bayanulhuda.com`).
- **Note for Google Play Store `.aab` Submission:**
  - Before uploading a production `.aab` to Google Play Console, if Google Play Console flags `<uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />`, remove that single permission line from `app/src/main/AndroidManifest.xml` for the Play Store track (since Play Store updates are handled entirely by Google Play). Keep `<uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />` enabled for Direct APK GitHub Releases.

### Mode B: Direct APK Installation (GitHub Releases / Sideloaded)
- **Detection:** Non-Play installer source (sideloaded APK, GitHub Releases, Google Drive folder, or package installer).
- **Behavior:**
  - Queries official GitHub Releases API (`https://api.github.com/repos/zulqarnain7861/Bayan-ul-huda-quran-o-hadees/releases/latest`).
  - Downloads the APK into private application storage (`context.filesDir/apk_updates/`).
  - Performs 7-step cryptographic and certificate verification (`ApkSecurityVerifier.kt`).
  - Requests user consent via Android's official `PackageInstaller` confirmation dialog.

---

## 2. Security & Privacy Audit Verification Matrix

| Security / Policy Control | Implementation Status | Verified Location |
| :--- | :--- | :--- |
| **No Silent or Root Installation** | Enforced — always opens Android's official user confirmation screen | `AndroidPackageInstallerCoordinator.kt` |
| **No Accessibility Service Abuse** | Enforced — zero AccessibilityServices declared or used | `AndroidManifest.xml` |
| **No Device Admin Abuse** | Enforced — zero DeviceAdminReceivers declared or used | `AndroidManifest.xml` |
| **No Dynamic Code Loading (DCL)** | Enforced — blocks `.dex`, `.jar`, `.so`, `.sh`, ELF, PE, HTML payloads | `ApkSecurityVerifier.kt` & `GiantWallSecuritySuite.kt` |
| **Cryptographically Signed Update Metadata** | Enforced — `update.json` is signed with RSA-SHA256 and verified against the installed app's X.509 certificate | `UpdateManifestVerifier.kt` & `generate_signed_update_manifest.sh` |
| **SHA-256 Checksum Pinning** | Enforced — constant-time streaming SHA-256 verification before install | `ApkSecurityVerifier.kt` |
| **X.509 Signing Certificate Match** | Enforced — downloaded APK's signing certificate must match the installed app's certificate | `ApkSecurityVerifier.kt` |
| **Anti-Downgrade Protection** | Enforced — rejects any APK where `apkVersionCode <= installedVersionCode` | `ApkSecurityVerifier.kt` |
| **No Broad Storage Permissions** | Enforced — uses app-private `context.filesDir/apk_updates/` + `FileProvider` | `ApkUpdateDownloadManager.kt` & `file_provider_paths.xml` |
| **HTTPS & TLS 1.2/1.3 AEAD Only** | Enforced — cleartext traffic disabled (`usesCleartextTraffic="false"`) and GitHub host allowlist enforced | `GitHubReleaseClient.kt` & `network_security_config.xml` |
| **Zero Embedded GitHub Tokens** | Enforced — public GitHub Releases API used without client secrets | `GitHubReleaseClient.kt` |

> **Disclaimer:** Passing automated security and unit tests verifies technical controls in the codebase, but does not by itself guarantee Google Play Console review approval. Always review the latest Google Play Developer Program Policies prior to submitting a new store build.
