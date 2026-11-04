# Secrets & Credential Handling Audit — Bayan ul Huda (`com.bayanulhuda.com`)

> **Security Policy Notice**: In accordance with strict security auditing standards, **no raw secret values, tokens, API keys, or credentials are printed or reproduced in this document**. Only file locations, configuration mechanisms, key names, and associated risks are documented.

---

## 1. Where Secrets Are Expected

| Identifier | Expected Purpose | Expected Location | Current Status in Repository |
| :--- | :--- | :--- | :--- |
| `GEMINI_API_KEY` | Optional Gemini API key | `.env` (injected via `app/build.gradle.kts` line 38 into `BuildConfig.GEMINI_API_KEY`) | Empty placeholder in `.env.example`; defaults to `""` in `app/build.gradle.kts`. |
| `YOUTUBE_API_KEY` | Optional YouTube Data API v3 key for live channel search | `.env` (injected via `app/build.gradle.kts` line 39 into `BuildConfig.YOUTUBE_API_KEY`) | Empty placeholder in `.env.example`; defaults to `""` in `app/build.gradle.kts`. |
| `QURAN_API_KEY` | Optional Quran API key | `.env` (injected via `app/build.gradle.kts` line 40 into `BuildConfig.QURAN_API_KEY`) | Empty placeholder in `.env.example`; defaults to `""` in `app/build.gradle.kts`. |
| `HADITH_API_KEY` | Optional Hadith API key | `.env` (injected via `app/build.gradle.kts` line 41 into `BuildConfig.HADITH_API_KEY`) | Empty placeholder in `.env.example`; defaults to `""` in `app/build.gradle.kts`. |
| `bayan_giant_wall_aes256_master_key` | Android Keystore AES-256-GCM MasterKey alias | Generated at runtime inside hardware/OS Android Keystore (`GiantWallSecuritySuite.kt` line 136) | Dynamic per-device hardware-backed key; never stored in source code. |
| YouTube WebView Session Cookies (`SAPISID`, `__Secure-3PAPISID`, `SID`) | Optional user-authenticated YouTube session cookies | Runtime WebView `CookieManager` (`VideoStreamExtractor.kt` lines 324–360) | Dynamic runtime session state; never hardcoded. |
| Android Signing Keystore (`debug.keystore`) | APK signing credentials | `app/build.gradle.kts` lines 25–36 (`signingConfigs`) | Uses standard Android debug keystore with default Android debug credentials when `debug.keystore` is present. |

---

## 2. Environment & Configuration Handling

- **Gradle `.env` Parser (`app/build.gradle.kts`, lines 7–21)**:
  - The build script reads key-value pairs from a root `.env` file (if present) and falls back to `System.getenv(key)` before defaulting to `""`.
  - Lines 38–42 inject four `BuildConfigField` strings (`GEMINI_API_KEY`, `YOUTUBE_API_KEY`, `QURAN_API_KEY`, `HADITH_API_KEY`).
- **Template File (`.env.example`, lines 1–9)**:
  - Contains only empty variable declarations (`GEMINI_API_KEY=`, `YOUTUBE_API_KEY=`, `QURAN_API_KEY=`, `HADITH_API_KEY=`) with no populated secret values.

---

## 3. Hardcoded-Secret Risks

- **Source Code Audit**:
  - A full-repository inspection of `app/src/main/java/com/example/**/*.kt`, `app/src/main/assets/**`, and `app/src/main/res/**` confirmed **zero hardcoded production API keys, private keys, OAuth client secrets, or bearer tokens**.
- **Debug Signing Configuration (`app/build.gradle.kts`, lines 25–36)**:
  - **Evidence**: Both `signingConfigs.create("release")` and `signingConfigs.getByName("debug")` reference `file("debug.keystore")` with standard Android debug keystore passwords.
  - **Risk**: Acceptable for cloud emulator preview builds, but a dedicated production release keystore managed via CI/CD secrets is required before publishing to Google Play.

---

## 4. Client-Side Exposure Risks

1. **`BuildConfig` Constant Pool Exposure (`app/build.gradle.kts`, lines 38–42)**:
   - **Evidence**: `buildConfigField("String", "YOUTUBE_API_KEY", ...)` compiles any key supplied in `.env` directly into `com.example.BuildConfig` bytecode.
   - **Risk**: Any value placed in `BuildConfig` can be extracted from the APK using `jadx` or `apktool`.
   - **Current Impact**: Low in the current build because all four keys default to `""` and the app's primary Quran, Hadith, Prayer Times, and Video Extraction flows operate without requiring those keys.
2. **In-App WebView Cookie & `SAPISIDHASH` Handling (`VideoStreamExtractor.kt`, lines 324–375)**:
   - **Evidence**: When a user optionally signs into YouTube via `YMusicYoutubeSignInDialog` (`YtEmbedHost.kt`, line 1869), `VideoStreamExtractor.readYoutubeWebViewCookies()` reads cookies from `android.webkit.CookieManager` for `https://www.youtube.com` and `https://music.youtube.com`, and `computeSapisidHashHeader()` computes a SHA-1 `SAPISIDHASH` `Authorization` header.
   - **Risk**: `applyYoutubeSessionHeaders()` is only called for `https://www.youtube.com`, `https://m.youtube.com`, and `https://music.youtube.com` requests (never sent to third-party Piped/Invidious servers). However, in `YtEmbedHost.kt` (lines 896–905), `DefaultHttpDataSource.Factory()` attaches the `Cookie` header to `ExoPlayer` requests; if `ExoPlayer` loads a third-party URL, the `Cookie` header should not be attached.

---

## 5. Git / History Exposure Risks

- **`.gitignore` Verification**:
  - Verify that `.env`, `*.jks`, and `local.properties` remain excluded from version control so developer-populated secrets are never committed.
- **Asset & Resource Inspection**:
  - No `google-services.json` service account private keys or `.env` files containing live credentials exist in the workspace.

---

## 6. Logging & Error Exposure Risks

- **Sanitized Logging (`GiantWallSecuritySuite.kt`, lines 660–713)**:
  - **Evidence**: `SanitizedLogger` automatically redacts URLs (`https?://[^\s"']+`), bearer tokens, `key=`, `token=`, `secret=`, `password=`, `apikey=`, `authorization=`, and email addresses before delegating to `android.util.Log`. Furthermore, debug/info logs (`SanitizedLogger.d`, `SanitizedLogger.i`) are completely suppressed when `!BuildConfig.DEBUG`.
  - **User-Facing Error Sanitization (`GiantWallSecuritySuite.kt`, lines 625–654)**:
    - `UserFacingErrorMapper.toSafeUserMessage(throwable)` maps network/SSL/HTTP exceptions to generic user-friendly guidance without leaking stack traces, internal class names, or raw URLs.

---

## 7. Secret Rotation Requirements

1. **API Keys (`YOUTUBE_API_KEY`, `GEMINI_API_KEY`, `QURAN_API_KEY`, `HADITH_API_KEY`)**:
   - Rotate immediately if ever committed to version control or distributed in an unrestricted APK.
   - Apply Android package (`com.bayanulhuda.com`) + SHA-1 certificate restrictions in Google Cloud Console.
2. **YouTube WebView Session Cookies**:
   - Users can invalidate and purge in-app YouTube session cookies at any time via the in-app **Sign Out & Clear Cookies** action (`YtEmbedHost.kt`, lines 1916–1921), which invokes `CookieManager.getInstance().removeAllCookies(null)` and `flush()`.
3. **Android Keystore Master Key (`bayan_giant_wall_aes256_master_key`)**:
   - Bound to the device hardware/OS keystore and automatically invalidated if app data is cleared or the app is uninstalled.

---

## 8. Actual Findings with Evidence

| Finding ID | Severity | Location | Evidence | Risk & Remediation |
| :--- | :--- | :--- | :--- | :--- |
| **SEC-S01** | **Medium** | `app/src/main/java/com/example/player/YtEmbedHost.kt` (lines 895–907) | `DefaultHttpDataSource.Factory()` sets `setDefaultRequestProperties(mapOf("Cookie" to cookieHeader))` globally on the ExoPlayer data source factory when `cookieHeader.isNotBlank()`. | If `ExoPlayer` ever requests a non-YouTube URL (e.g., a Piped/Invidious proxy stream), YouTube cookies could be included in the request headers. **Fix**: Only attach YouTube cookies when the target stream host is `*.googlevideo.com` or `*.youtube.com`. |
| **SEC-S02** | **Low** | `app/src/main/java/com/example/security/GiantWallSecuritySuite.kt` (lines 147–163) | `SecureKeystoreVault.getEncryptedPrefs(context)` catches Keystore exceptions and falls back to unencrypted `context.getSharedPreferences("bayan_secure_prefs_fallback", Context.MODE_PRIVATE)`. | On devices with corrupted hardware Keystores, values written to `SecureKeystoreVault` are stored in plaintext XML inside the app's private data directory. **Fix**: Avoid storing high-value bearer tokens in the fallback preference file or encrypt values with an ephemeral/software-derived key. |
| **SEC-S03** | **Low** | `app/build.gradle.kts` (lines 38–42) | `GEMINI_API_KEY`, `YOUTUBE_API_KEY`, `QURAN_API_KEY`, and `HADITH_API_KEY` are injected as `BuildConfig` string fields. | Currently empty (`""`), so no secret is exposed. If populated in production, restrict keys by package name + SHA-1 fingerprint in Google Cloud Console. |
