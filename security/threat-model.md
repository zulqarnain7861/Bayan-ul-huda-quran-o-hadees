# Threat Model — Bayan ul Huda (`com.bayanulhuda.com`)

## 1. Architecture & System Overview
Bayan ul Huda is an Android client application (`applicationId = "com.bayanulhuda.com"`, `namespace = "com.example"`, `minSdk = 24`, `targetSdk = 35`) built with Kotlin and Jetpack Compose. It provides offline-capable Islamic educational modules (Quran reader & recitation, Hadith collections, Namaz-e-Muhammadi step-by-step guide, Wudu, Noorani Qaida, Qibla compass, Prayer times, Maududi library, and an Engineer Muhammad Ali Mirza lecture discovery & playback subsystem) backed by:
- **On-device storage**: Bundled assets (`app/src/main/assets/`), `SharedPreferences` (`bayan_prefs`, `mirza_lecture_prefs`, `bayan_secure_prefs_fallback`), internal app files (`filesDir/offline_videos/`), and cache (`cacheDir`).
- **Centralized security layer**: `GiantWallSecuritySuite.kt` (`RuntimeIntegrityGuard`, `SecureKeystoreVault`, `SecureNetworkClient`, `SsrfRedirectValidator`, `AdShieldConfig`, `InputSanitizer`, `DoSResourceShield`, `UserFacingErrorMapper`, `SanitizedLogger`, `SessionIntegrityManager`).
- **External APIs & Cloud Services**: Firebase Auth / Firestore (`firestore.rules` present at project root), YouTube Data API v3 (`googleapis.com`), YouTube InnerTube / YouTube Music (`youtube.com`, `music.youtube.com`, `youtube-nocookie.com`, `googlevideo.com`), Piped/Invidious fallback clusters, Archive.org media hosting, Quran/Hadith/Prayer APIs (`api.alquran.cloud`, `everyayah.com`, `api.aladhan.com`, `fawazahmed0/hadith-api` on jsDelivr), and OpenStreetMap Nominatim (`nominatim.openstreetmap.org`).

---

## 2. Assets

| Asset ID | Asset Name | Location / Storage | Sensitivity | Description |
| :--- | :--- | :--- | :--- | :--- |
| **A-01** | Build-time API Keys (`GEMINI_API_KEY`, `YOUTUBE_API_KEY`, `QURAN_API_KEY`, `HADITH_API_KEY`) | `app/build.gradle.kts` (`BuildConfig`), `.env` / `.env.example` | **High** | Third-party API keys compiled into `BuildConfig` if provided at build time. |
| **A-02** | Encrypted Preferences & AES-256-GCM Master Key | `GiantWallSecuritySuite.kt` (`SecureKeystoreVault`), Android Keystore (`bayan_giant_wall_aes256_master_key`) | **High** | Hardware/software-backed Android Keystore key protecting `bayan_secure_vault_v1.xml` (or `bayan_secure_prefs_fallback` if Keystore initialization fails). |
| **A-03** | YouTube WebView Session Cookies (`SAPISID`, `SID`, `__Secure-3PAPISID`) | `android.webkit.CookieManager`, read by `VideoStreamExtractor.kt` (`readYoutubeWebViewCookies()`) | **Critical** | Optional authenticated Google/YouTube session cookies if a user signs in via `YMusicYoutubeSignInDialog` (`YtEmbedHost.kt`). Used to compute `SAPISIDHASH` authorization headers. |
| **A-04** | User Preferences, Watch History, Bookmarks & Search History | `SharedPreferences` (`mirza_lecture_prefs` in `LectureRepository.kt`, `bayan_prefs` in `PreferencesManager.kt`) | **Medium** | User's city/coordinates for prayer times, watch-time per video/topic, bookmarked video IDs, and recent search queries. |
| **A-05** | Firestore User Profile & Private Subcollections | Cloud Firestore (`/users/{userId}` and subcollections `bookmarks`, `progress`, `notes`, `favorites` in `firestore.rules`) | **High** | Cloud-synced user records governed by `firestore.rules`. |
| **A-06** | Authentic Islamic Curriculum & Hadith Datasets | `app/src/main/assets/*.json` (`namaz_muhammadi_v1.json`, `mirza_lectures_v3.json`, `quran_complete.json`, etc.) | **Medium (Integrity)** | Verified religious texts, Hadith gradings, and lecture metadata where unauthorized tampering or stream substitution degrades educational integrity. |
| **A-07** | Offline Video & Audio Cache | `context.filesDir/offline_videos/` (`OfflineNamazVideoManager.kt`) | **Low–Medium** | Downloaded MP4 streams for offline Namaz instruction and lecture playback. |

---

## 3. Trust Boundaries

1. **TB-01: Android App Process ↔ Untrusted Network / Internet**
   - Crossed whenever `SecureNetworkClient` (`OkHttpClient`), `ExoPlayer` (`DefaultHttpDataSource`), or `WebView` communicates with external endpoints (`youtube.com`, `googleapis.com`, Piped/Invidious nodes, `archive.org`, `aladhan.com`, `alquran.cloud`).
2. **TB-02: Native Kotlin Layer ↔ Embedded WebView (`AdShieldedBridgeWebView` / `YMusicYoutubeSignInDialog`)**
   - Crossed via `@JavascriptInterface` (`YtPlayerJsBridge` exposed as `window.YtPlayerBridge` in `YtEmbedHost.kt`), `evaluateJavascript(...)`, and shared `CookieManager` state.
3. **TB-03: Android App Process ↔ Device OS & Local Storage**
   - Crossed when reading/writing `SharedPreferences`, `EncryptedSharedPreferences`, internal `filesDir`, Android Keystore, and hardware sensors (`ACCESS_FINE_LOCATION`, `Sensor.TYPE_ROTATION_VECTOR`).
4. **TB-04: Android Client ↔ Firebase Backend (Auth & Firestore)**
   - Crossed when authenticating users and reading/writing Firestore documents enforced by `/firestore.rules`.

---

## 4. Actors

| Actor | Trust Level | Capabilities |
| :--- | :--- | :--- |
| **Legitimate End User** | Semi-Trusted | Interacts with Compose UI, searches lectures/Hadith, plays videos, optionally signs into YouTube WebView, configures location/prayer settings. |
| **Network Adversary (MITM / Rogue Wi-Fi / ISP)** | Untrusted | Can inspect/modify cleartext traffic (mitigated by `usesCleartextTraffic="false"` and HTTPS enforcement), spoof DNS, or block specific domains. |
| **Third-Party Community Proxy Operators (Piped / Invidious nodes)** | Untrusted | Operates community API endpoints listed in `VideoStreamExtractor.kt` (`PIPED_CLUSTER_ENDPOINTS`, `INVIDIOUS_CLUSTER_ENDPOINTS`); could return malformed JSON, redirect to internal IPs, or serve mismatched video streams. |
| **Malicious Co-Installed App / Local Attacker (Physical or ADB)** | Untrusted | Attempts intent spoofing on exported components, backup extraction, root-based inspection of unencrypted `SharedPreferences` or WebView cookie databases. |
| **Compromised Web Content / Third-Party Scripts inside WebView** | Untrusted | Executes JavaScript within `WebView` origin and can invoke methods annotated with `@JavascriptInterface` on `window.YtPlayerBridge`. |

---

## 5. Entry Points

1. **Main Activity Intent Filter (`com.example.MainActivity`)**:
   - Declared with `android:exported="true"` in `app/src/main/AndroidManifest.xml` (lines 20–29) for `android.intent.action.MAIN` / `android.intent.category.LAUNCHER`. No custom deep-link `<data>` schemes are exposed.
2. **Search & Filter Inputs (`DiscoverScreen.kt`, `SearchViewModel.kt`, `HadithScreen.kt`, `QuranScreen.kt`)**:
   - User-supplied search queries passed to `InputSanitizer.sanitizeSearchQuery(...)` before local filtering or YouTube search requests.
3. **Multi-Pipeline Video Stream Extractor (`VideoStreamExtractor.kt`)**:
   - Consumes JSON responses from YouTube InnerTube (`/youtubei/v1/player`), YouTube Music (`music.youtube.com/youtubei/v1/player`), Piped (`/streams/{videoId}`), Invidious (`/api/v1/videos/{videoId}`), and `oembed` endpoints.
4. **WebView JavaScript Bridge (`YtPlayerJsBridge` in `YtEmbedHost.kt`, lines 491–513)**:
   - Exposes `@JavascriptInterface` methods `onError(code: Int)`, `onDuration(seconds: Int)`, and `onEmbedRestricted(code: Int)` to WebView frames.
5. **In-App YouTube Sign-In WebView (`YMusicYoutubeSignInDialog` in `YtEmbedHost.kt`, lines 1869–1982)**:
   - Loads `https://accounts.google.com/ServiceLogin?service=youtube...` inside an in-app `WebView` and reads resulting cookies via `CookieManager.getInstance()`.
6. **Location & Nominatim Geocoding (`PrayerTimesRepository.kt`, `QiblaScreen.kt`)**:
   - Consumes device GPS coordinates and reverse-geocoding JSON responses from `nominatim.openstreetmap.org`.

---

## 6. Threats, Attack Scenarios, Impact, Likelihood & Risk Severity

### T-01: Stream Substitution / Media Integrity Mismatch via Third-Party Extraction Pipelines or Static Fallback
- **Attack Scenario**: A third-party Piped/Invidious instance or fallback mapper returns a stream URL belonging to a different video ID (or a static Archive.org lecture fallback), causing the player to stream content that does not match the video thumbnail selected by the user.
- **Impact**: Medium (Integrity violation; user sees mismatched religious lecture content compared to the selected title/thumbnail).
- **Likelihood**: High (Observed when extraction pipelines failed or returned mismatched metadata).
- **Risk Severity**: **High**
- **Existing Mitigations**:
  - `LectureRecord.isValidYoutubeId` enforces strict 11-character regex `^[a-zA-Z0-9_-]{11}$`.
  - `VideoStreamExtractor.parseInnerTubeStreamingData` enforces `returnedVideoId == videoId`.
  - `YtEmbedHost` gates `useNativeDirectExoPlayer` strictly on `hasLiveDirectTrack` (`streamBundle.youtubeId == lecture.youtubeId && streamBundle.hasDirectYoutubeStreams && activeResolvedTrack.isDirectYoutubeStream`) or explicit offline file playback, falling back to `AdShieldedBridgeWebView` locked to `lecture.youtubeId` rather than substituting unrelated Archive.org MP4s.
- **Missing Mitigations**:
  - Cryptographic content-hash verification is not feasible for dynamic live-transcoded YouTube HLS/DASH segments; Piped/Invidious responses do not always include signed `videoDetails.videoId` fields.

### T-02: Client-Side API Key Extraction from Decompiled APK (`BuildConfig`)
- **Attack Scenario**: An attacker decompiles the distributed APK (`classes.dex`) and extracts `BuildConfig.YOUTUBE_API_KEY` or `BuildConfig.GEMINI_API_KEY` defined in `app/build.gradle.kts` (lines 38–42).
- **Impact**: Medium–High (Quota exhaustion or unauthorized billing on the developer's Google Cloud project).
- **Likelihood**: High (If production keys are populated in `.env` at build time without Android package/SHA-1 restrictions).
- **Risk Severity**: **Medium**
- **Existing Mitigations**:
  - Default values in `app/build.gradle.kts` are empty strings (`""`).
  - R8/ProGuard minification and resource shrinking are enabled for release builds (`isMinifyEnabled = true`, `isShrinkResources = true` in `app/build.gradle.kts`, lines 49–50).
  - The app operates without requiring `YOUTUBE_API_KEY` by using keyless InnerTube/oEmbed discovery (`LectureRepository.kt`).
- **Missing Mitigations**:
  - If keys are injected in release builds, Google Cloud Console API key restrictions (Android application package name `com.bayanulhuda.com` + SHA-1 signing certificate fingerprint) must be enforced server-side, or requests should be proxied via a backend/Firebase Function.

### T-03: In-App WebView Google Sign-In Session Exposure (`YMusicYoutubeSignInDialog`)
- **Attack Scenario**: A user signs into their Google/YouTube account inside the embedded `WebView` (`YMusicYoutubeSignInDialog` in `YtEmbedHost.kt`). Because `CookieManager` stores cookies in the app's local WebView SQLite database, a rooted device compromise or malicious JS executing in a shared WebView could access `SAPISID` / `SID` cookies. Furthermore, `ExoPlayer` (`DefaultHttpDataSource` in `YtEmbedHost.kt` line 899) had `allowCrossProtocolRedirects = true` alongside default `Cookie` headers.
- **Impact**: High (Unauthorized access to user's YouTube session if device is rooted or cross-origin redirect leaks `Cookie` header).
- **Likelihood**: Low–Medium
- **Risk Severity**: **Medium**
- **Existing Mitigations**:
  - Sign-in is strictly optional; users can sign out and clear cookies via `CookieManager.getInstance().removeAllCookies(null)`.
  - `VideoStreamExtractor.isStreamUrlPlayableHttp200Or206` only attaches `Cookie` headers when `url.contains("googlevideo.com")`.
  - `AdShieldConfig.hardenWebViewSettings` disables file/content access (`allowFileAccess = false`, `allowContentAccess = false`, `MIXED_CONTENT_NEVER_ALLOW`).
- **Missing Mitigations**:
  - `DefaultHttpDataSource.Factory()` in `YtEmbedHost.kt` (lines 897–906) sets `setDefaultRequestProperties(mapOf("Cookie" to cookieHeader))` and `setAllowCrossProtocolRedirects(true)`. Restricting `Cookie` attachment only to verified `.googlevideo.com` / `.youtube.com` hosts and disabling cross-protocol redirects further reduces header leakage risk.

### T-04: Server-Side Request Forgery (SSRF) & Malicious Redirects from Third-Party Video Proxies
- **Attack Scenario**: A rogue Piped or Invidious server in `PIPED_CLUSTER_ENDPOINTS` / `INVIDIOUS_CLUSTER_ENDPOINTS` returns a stream URL pointing to `http://127.0.0.1`, `http://169.254.169.254`, or an RFC-1918 LAN address (`192.168.x.x`), attempting to probe the user's local network or loopback services.
- **Impact**: Medium (Local network port scanning or interaction with local router/device endpoints).
- **Likelihood**: Low–Medium
- **Risk Severity**: **Low (Mitigated)**
- **Existing Mitigations**:
  - `SsrfRedirectValidator` (`GiantWallSecuritySuite.kt`, lines 263–371) enforces HTTPS-only schemes, validates hostnames against `ALLOWED_HOST_SUFFIXES`, blocks loopback/link-local/site-local/multicast IPs, and is wired into `SecureNetworkClient` as both an application interceptor and network redirect interceptor (`MAX_REDIRECTS = 3`).
- **Missing Mitigations**:
  - `ExoPlayer`'s `DefaultHttpDataSource` in `YtEmbedHost.kt` uses Android's standard `HttpURLConnection` stack rather than `SecureNetworkClient`'s `OkHttpDataSource`, though `VideoStreamExtractor` validates all candidate URLs through `SsrfRedirectValidator.validateUrlSafety` before passing them to `ExoPlayer`.

### T-05: Unencrypted Local Storage of Non-Secret User Preferences (`SharedPreferences`)
- **Attack Scenario**: On a rooted device, an attacker reads `/data/data/com.bayanulhuda.com/shared_prefs/mirza_lecture_prefs.xml` or `bayan_prefs.xml` to inspect watch history, recent search queries, or cached prayer coordinates.
- **Impact**: Low (Privacy exposure of lecture watch history and approximate city/coordinates).
- **Likelihood**: Low (Requires rooted device or physical forensic access; `android:allowBackup="false"` prevents `adb backup` extraction).
- **Risk Severity**: **Low**
- **Existing Mitigations**:
  - `android:allowBackup="false"` is set in `AndroidManifest.xml` (line 13).
  - `SecureKeystoreVault` (`EncryptedSharedPreferences` with AES-256-GCM) is implemented in `GiantWallSecuritySuite.kt` (lines 134–205).
- **Missing Mitigations**:
  - `LectureRepository.kt` (line 30) and `PreferencesManager.kt` (line 13) currently use standard `context.getSharedPreferences(...)` instead of routing through `SecureKeystoreVault`.
  - When `EncryptedSharedPreferences` throws an initialization exception (e.g., broken Keystore on certain OEM ROMs), `SecureKeystoreVault.getEncryptedPrefs` falls back to standard `context.getSharedPreferences(FALLBACK_PREFS_NAME, Context.MODE_PRIVATE)` (`GiantWallSecuritySuite.kt`, line 161).
