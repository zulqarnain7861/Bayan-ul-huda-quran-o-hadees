# Security Checklist & Audit Findings — Bayan ul Huda (`com.bayanulhuda.com`)

## 1. Domain-by-Domain Security Checklist Summary

| Security Domain | Status | Project Evidence & Implementation |
| :--- | :--- | :--- |
| **1. Authentication** | **Implemented (Optional)** | Optional YouTube WebView sign-in (`YMusicYoutubeSignInDialog` in `YtEmbedHost.kt`) restricted to Google/YouTube auth domains; Firestore rules require `request.auth != null`. |
| **2. Authorization / RBAC** | **Implemented** | `firestore.rules` enforces strict owner-only access (`isOwner(userId)`) on `/users/{userId}` and subcollections, and `allow write: if false` on all public collections. |
| **3. Session / Token Security** | **Implemented** | `SessionIntegrityManager` (`GiantWallSecuritySuite.kt`, lines 719–788) validates token format and expiration; YouTube cookies in `YtEmbedHost.kt` are scoped strictly to `googlevideo.com` and `youtube.com` hosts with `allowCrossProtocolRedirects(false)`. |
| **4. Input Validation** | **Implemented** | `InputSanitizer` (`GiantWallSecuritySuite.kt`, lines 576–619) enforces NFC normalization, length caps (120 chars), Youtube ID regex (`^[a-zA-Z0-9_-]{11}$`), and asset filename path-traversal rejection. |
| **5. Injection (SQLi / JS / Path Traversal)** | **Implemented** | No raw SQL queries are executed; asset filenames are validated via `InputSanitizer.isSafeAssetFileName`; WebView interpolates `safeId` only after `LectureRecord.isValidYoutubeId` verification. |
| **6. XSS / CSRF (WebView & Bridge)** | **Implemented** | `AdShieldConfig.hardenWebViewSettings` disables file/content access and mixed content; `AdShieldWebViewClient` blocks `javascript:` and `file:` URLs and restricts navigation to allowlisted hosts. |
| **7. API Security** | **Implemented** | `SecureNetworkClient` enforces TLS 1.2/1.3, 8s connect / 12s read timeouts, `MAX_RESPONSE_BYTES = 2 MB` response cap, and `SsrfRedirectValidator` hostname allowlisting. |
| **8. Rate Limiting / Abuse Protection** | **Implemented** | `DoSResourceShield` (`GiantWallSecuritySuite.kt`, lines 794–861) enforces token-bucket rate limiting (`30 requests / 10s`), search debounce (`350ms`), and provider cooldowns (`MAX_PROVIDER_RETRIES = 2`, `45s` cooldown). |
| **9. File Uploads** | **N/A (Not Present)** | Application does not accept or process user file uploads. |
| **10. Database Security** | **Implemented** | `firestore.rules` enforces strict schema, type, length, and key-count validation on all user documents and subcollections. |
| **11. Secrets Management** | **Implemented** | `.env.example` contains only empty placeholders; zero hardcoded keys exist in source code; `SanitizedLogger` redacts sensitive tokens from logs. |
| **12. Encryption (At Rest & In Transit)** | **Partial** | In-transit: `usesCleartextTraffic="false"` + TLS 1.2/1.3 cipher suites. At-rest: `SecureKeystoreVault` uses AES-256-GCM (`EncryptedSharedPreferences`), though `LectureRepository` and `PreferencesManager` use standard `SharedPreferences` for non-secret settings. |
| **13. CORS / Origin Isolation** | **Implemented** | Embedded player uses `https://www.youtube-nocookie.com` origin and `strict-origin-when-cross-origin` referrer policy (`YtEmbedHost.kt`, line 269). |
| **14. Dependency Security** | **Implemented (Monitor Alpha)** | Uses modern AndroidX/Compose/OkHttp 4.12/Media3 1.3.1 libraries; `androidx.security:security-crypto:1.1.0-alpha06` is an alpha artifact and should be monitored for stable migration. |
| **15. Logging & Monitoring** | **Implemented** | `SanitizedLogger` strips URLs, keys, tokens, passwords, and emails before logging and suppresses debug/info logs in non-debug builds (`!BuildConfig.DEBUG`). |
| **16. Admin Security** | **Implemented** | No client-side admin panel; public Firestore collections deny all writes (`allow write: if false`). |
| **17. Deployment & Infrastructure** | **Needs Release Keystore** | `isMinifyEnabled = true` and `isShrinkResources = true` are enabled for release builds, but `signingConfigs.create("release")` currently points to `debug.keystore`. |
| **18. Data Privacy** | **Implemented** | `android:allowBackup="false"`; fine/coarse location permissions are requested at runtime only when calculating local prayer times or Qibla direction. |
| **19. Error Handling** | **Implemented** | `UserFacingErrorMapper` converts all exceptions into safe, generic messages without exposing stack traces or internal URLs. |
| **20. Backup & Recovery** | **Implemented** | Cloud backup/device transfer of private app storage is disabled (`android:allowBackup="false"`); bundled offline JSON and video assets self-heal if cache is cleared. |

---

## 2. Categorized Security Findings

Each finding below is classified into one of five strict categories (**Confirmed Vulnerability**, **Security Weakness**, **Missing Control**, **Potential Risk Requiring Verification**, or **Recommendation**) and follows the required format:
`Severity → Evidence → Risk → Affected location → Why it matters → Recommended fix`

---

### A. Confirmed Vulnerabilities

*No exploitable remote code execution, credential leakage, or privilege escalation vulnerabilities were confirmed in the current codebase.*
*(Note: The media integrity defect where failed/unextracted YouTube streams fell back to unrelated Archive.org MP4 streams clipped via `ClippingMediaSource` in `YtEmbedHost.kt` and `VideoStreamExtractor.kt` was identified and remediated so `YtEmbedHost` strictly verifies `streamBundle.youtubeId == lecture.youtubeId` and `hasLiveDirectTrack`.)*

---

### B. Security Weaknesses

#### SW-01: Unencrypted Fallback in `SecureKeystoreVault` When Android Keystore Fails
- **Severity**: Low
- **Evidence**: In `GiantWallSecuritySuite.kt` (lines 147–163), if `EncryptedSharedPreferences.create(...)` throws an exception, `SecureKeystoreVault.getEncryptedPrefs()` catches the exception and returns `context.getSharedPreferences("bayan_secure_prefs_fallback", Context.MODE_PRIVATE)`.
- **Risk**: On devices where the hardware Keystore is unavailable or corrupted, callers of `SecureKeystoreVault.putSecureString()` silently store sensitive values in plaintext XML on disk.
- **Affected location**: `/app/src/main/java/com/example/security/GiantWallSecuritySuite.kt` (lines 147–163)
- **Why it matters**: Silent downgrade from AES-256-GCM encryption to plaintext storage weakens defense-in-depth on rooted devices.
- **Recommended fix**: Delete and recreate the corrupted Keystore keyset once on `GeneralSecurityException`, or refuse to persist high-sensitivity secrets to disk if hardware/software encryption fails (keep in volatile memory only).

#### SW-02: Release Build Type Configured with `debug.keystore`
- **Severity**: Low (Pre-Production Configuration)
- **Evidence**: In `app/build.gradle.kts` (lines 25–30), `signingConfigs.create("release")` uses `storeFile = file("debug.keystore")` with standard debug passwords (`"android"`).
- **Risk**: APKs built with the `release` variant are signed with a publicly known debug certificate if `debug.keystore` is used in production distribution.
- **Affected location**: `/app/build.gradle.kts` (lines 25–30, 48–56)
- **Why it matters**: Google Play Console rejects APKs/AABs signed with debug certificates, and publicly known signing keys allow anyone to re-sign tampered APKs with the same signature.
- **Recommended fix**: Keep `debug.keystore` for cloud preview builds as required by the build environment, and inject a dedicated production release keystore via CI environment variables before publishing to Google Play.

---

### C. Missing Controls

#### MC-01: Certificate Pinning Not Configured on `SecureNetworkClient`
- **Severity**: Low
- **Evidence**: `SecureNetworkClient` (`GiantWallSecuritySuite.kt`, lines 437–468) enforces TLS 1.2/1.3 (`ConnectionSpec.MODERN_TLS`) and hostname verification, but does not configure an `okhttp3.CertificatePinner`.
- **Risk**: On a device where a user or enterprise MDM has installed a custom trusted root CA certificate, an active MITM proxy can intercept HTTPS traffic.
- **Affected location**: `/app/src/main/java/com/example/security/GiantWallSecuritySuite.kt` (lines 437–468)
- **Why it matters**: While `AndroidManifest.xml` defaults to trusting only system CAs on Android 7.0+ (`minSdk = 24`), pinning Google Trust Services root SPKIs for `*.googleapis.com` and `*.youtube.com` provides additional protection on rooted devices with injected system CAs.
- **Recommended fix**: Add an optional `CertificatePinner` for core Google root CAs (`GTS Root R1`–`R4`) with backup pins.

#### MC-02: Standard `SharedPreferences` Used in `LectureRepository` and `PreferencesManager`
- **Severity**: Informational / Low
- **Evidence**: `LectureRepository.kt` (line 30, `mirza_lecture_prefs`) and `PreferencesManager.kt` (line 13, `bayan_prefs`) use `context.getSharedPreferences(...)` directly instead of `SecureKeystoreVault`.
- **Risk**: Watch history, recent search queries, and cached prayer coordinates are stored in unencrypted XML inside `/data/data/com.bayanulhuda.com/shared_prefs/`.
- **Affected location**: `/app/src/main/java/com/example/data/repository/LectureRepository.kt` (line 30), `/app/src/main/java/com/example/data/PreferencesManager.kt` (line 13)
- **Why it matters**: Users' religious study habits, search queries, and location coordinates are privacy-sensitive on shared or rooted devices.
- **Recommended fix**: Migrate privacy-sensitive keys (such as cached GPS coordinates and search history) to `SecureKeystoreVault`.

---

### D. Potential Risks Requiring Verification

#### PR-01: Google Cloud API Key Package & SHA-1 Restrictions
- **Severity**: Medium (If `YOUTUBE_API_KEY` or `GEMINI_API_KEY` is populated in `.env`)
- **Evidence**: `app/build.gradle.kts` (lines 38–42) exposes `YOUTUBE_API_KEY` and `GEMINI_API_KEY` in `BuildConfig`. Currently both resolve to `""`.
- **Risk**: Cannot be verified from client code alone whether any API key later supplied via the AI Studio Secrets panel has Android application restrictions (`com.bayanulhuda.com` + SHA-1 fingerprint) and API scope restrictions enabled in Google Cloud Console.
- **Affected location**: `/app/build.gradle.kts` (lines 38–42), `.env.example`
- **Why it matters**: Unrestricted keys compiled into `BuildConfig` can be extracted from the APK and abused for third-party quota consumption.
- **Recommended fix**: Verify in Google Cloud Console that any provisioned API key is restricted to the `com.bayanulhuda.com` package name, the release signing SHA-1 fingerprint, and only the specific API needed.

#### PR-02: Availability & Trustworthiness of Community Piped / Invidious Instances
- **Severity**: Low
- **Evidence**: `VideoStreamExtractor.kt` (lines 280–295) lists community-hosted Piped (`pipedapi.kavin.rocks`, etc.) and Invidious (`inv.nadeko.net`, etc.) endpoints.
- **Risk**: Community instances may change ownership, go offline, or alter response schemas over time.
- **Affected location**: `/app/src/main/java/com/example/player/VideoStreamExtractor.kt` (lines 280–295)
- **Why it matters**: Although `SsrfRedirectValidator` blocks private IP redirects and no cookies/credentials are sent to Piped/Invidious nodes, untrusted proxy nodes could return inaccurate stream URLs.
- **Recommended fix**: Periodically audit the allowlisted Piped/Invidious domains in `SsrfRedirectValidator.ALLOWED_HOST_SUFFIXES` and prioritize direct first-party InnerTube extraction (`Pipeline 1` / `Vine yMusic SDK`) and official YouTube embeds (`AdShieldedBridgeWebView`).

---

### E. Recommendations

#### REC-01: Route ExoPlayer Network Requests Through `SecureNetworkClient` (`OkHttpDataSource`)
- **Severity**: Informational
- **Evidence**: `YtEmbedHost.kt` (line 897) uses `DefaultHttpDataSource.Factory()` for `ExoPlayer`, whereas API calls use `SecureNetworkClient.httpClient` (which has `SsrfRedirectValidator` interceptors attached).
- **Risk**: While initial stream URLs are validated by `SsrfRedirectValidator` during extraction, subsequent HLS segment redirects inside `ExoPlayer` bypass `SecureNetworkClient`'s OkHttp redirect interceptor.
- **Affected location**: `/app/src/main/java/com/example/player/YtEmbedHost.kt` (lines 895–907)
- **Why it matters**: Using `androidx.media3:media3-datasource-okhttp` backed by `SecureNetworkClient.httpClient` unifies all media and API traffic under the same SSRF and TLS enforcement pipeline.
- **Recommended fix**: Replace `DefaultHttpDataSource.Factory()` with `OkHttpDataSource.Factory(SecureNetworkClient.httpClient)` if `media3-datasource-okhttp` is added to the dependency catalog.

#### REC-02: Plan Upgrade Path for `androidx.security:security-crypto`
- **Severity**: Informational
- **Evidence**: `gradle/libs.versions.toml` and `app/build.gradle.kts` depend on `androidx.security:security-crypto:1.1.0-alpha06`.
- **Risk**: Jetpack `security-crypto` 1.1.0-alpha06 is an alpha artifact that wraps Tink; Google recommends migrating directly to Android Keystore + Tink (`com.google.crypto.tink:tink-android`) or DataStore in future iterations.
- **Affected location**: `/gradle/libs.versions.toml`, `/app/build.gradle.kts` (line 113), `/app/src/main/java/com/example/security/GiantWallSecuritySuite.kt`
- **Why it matters**: Reduces long-term dependency maintenance risk and avoids deprecated `EncryptedSharedPreferences` APIs.
- **Recommended fix**: Retain `EncryptedSharedPreferences` for current compatibility, and evaluate direct `com.google.crypto.tink:tink-android` AEAD encryption with Jetpack DataStore for future major releases.
