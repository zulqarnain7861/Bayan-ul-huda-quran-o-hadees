# Attack Surface Analysis — Bayan ul Huda (`com.bayanulhuda.com`)

## 1. Public Endpoints & Exported Android Components

### 1.1 Android Manifest Surface (`app/src/main/AndroidManifest.xml`)
- **Exported Activities**:
  - `com.example.MainActivity` (`android:exported="true"`, lines 20–29):
    - Intent Filter: `android.intent.action.MAIN` + `android.intent.category.LAUNCHER`.
    - No custom URL schemes (`http://`, `https://`, or custom app schemes) or deep-link `intent-filter` blocks are registered.
- **Services, BroadcastReceivers, ContentProviders**:
  - None declared or exported in `AndroidManifest.xml`.
- **Application-Level Flags**:
  - `android:allowBackup="false"` (line 13): Prevents `adb backup` extraction of private app files.
  - `android:usesCleartextTraffic="false"` (line 14): Blocks cleartext `http://` network traffic at the OS network security config level.
- **Declared Permissions (`AndroidManifest.xml`, lines 4–9)**:
  - `android.permission.INTERNET` (Install-time)
  - `android.permission.ACCESS_NETWORK_STATE` (Install-time)
  - `android.permission.ACCESS_FINE_LOCATION` (Runtime dangerous permission — used for Qibla compass and accurate local prayer calculation)
  - `android.permission.ACCESS_COARSE_LOCATION` (Runtime dangerous permission)
  - `android.permission.VIBRATE` (Install-time — used for Tasbeeh counter tactile feedback)

---

## 2. Outbound External API & Network Surface

All outbound HTTP requests made through `SecureNetworkClient` (`GiantWallSecuritySuite.kt`, lines 397–472) are restricted by `SsrfRedirectValidator.ALLOWED_HOST_SUFFIXES` (lines 268–306):

| Category | Hostnames / Endpoints | Protocol & Auth | Purpose |
| :--- | :--- | :--- | :--- |
| **YouTube Core & InnerTube / yMusic SDK** | `www.youtube.com/youtubei/v1/player`, `m.youtube.com/youtubei/v1/player`, `music.youtube.com/youtubei/v1/player`, `www.youtube.com/youtubei/v1/search`, `www.youtube.com/oembed`, `www.youtube-nocookie.com/embed/{id}`, `*.googlevideo.com`, `i.ytimg.com` | HTTPS (TLS 1.2/1.3); Optional `Cookie` + `SAPISIDHASH` header when user signs in | Direct video stream extraction (`VideoStreamExtractor.kt`), embedded player (`YtEmbedHost.kt`), keyless live lecture search (`LectureRepository.kt`), and video thumbnails. |
| **YouTube Data API v3 (Optional)** | `www.googleapis.com/youtube/v3/search` | HTTPS; `key` query parameter (`BuildConfig.YOUTUBE_API_KEY`) if configured | Optional official YouTube Data API search fallback (`LectureRepository.kt`, lines 669–674). |
| **Piped API Cluster** | `pipedapi.kavin.rocks`, `pipedapi.leptons.xyz`, `pipedapi.nosebs.ru`, `pipedapi.adminforge.de`, `pipedapi.drgns.space`, `api-piped.mha.fi` | HTTPS (Unauthenticated GET `/streams/{videoId}`) | Pipeline 2 fallback stream extraction (`VideoStreamExtractor.kt`, lines 280–287, 922–1000). |
| **Invidious API Cluster** | `inv.nadeko.net`, `invidious.nerdvpn.de`, `inv.tux.pizza`, `invidious.privacyredirect.com`, `yewtu.be` | HTTPS (Unauthenticated GET `/api/v1/videos/{videoId}`) | Pipeline 3 fallback stream extraction (`VideoStreamExtractor.kt`, lines 289–295, 1002–1090). |
| **Archive.org Media Hosting** | `archive.org`, `*.archive.org`, `*.us.archive.org` | HTTPS (Unauthenticated GET) | Offline downloadable Namaz demonstration and full-length archival audio/video lectures (`OfflineNamazVideoManager.kt`, `YtEmbedHost.kt`). |
| **Quran Text & Audio APIs** | `api.alquran.cloud/v1/...`, `everyayah.com/data/...`, `cdn.islamic.network` | HTTPS (Unauthenticated GET) | Surah/Ayah text fallback and verse-by-verse MP3 audio recitation (`QuranRepository.kt`, `AudioRecitationManager.kt`). |
| **Hadith CDN API** | `cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1/...` | HTTPS (Unauthenticated GET) | Online Hadith collection synchronization (`HadithRepository.kt`). |
| **Prayer Times & Geocoding** | `api.aladhan.com/v1/timings...`, `nominatim.openstreetmap.org/reverse` | HTTPS (Unauthenticated GET) | Daily salah timings and reverse geocoding of GPS coordinates to city names (`PrayerTimesRepository.kt`). |

---

## 3. Internal Endpoints & WebView Bridge Surface

1. **`YtPlayerJsBridge` (`app/src/main/java/com/example/player/YtEmbedHost.kt`, lines 491–513)**:
   - Registered on `AdShieldedBridgeWebView` as `"YtPlayerBridge"`.
   - Exposed `@JavascriptInterface` methods:
     - `onError(code: Int)`: Validates `code in listOf(2, 5, 100, 101, 150, 153)` before invoking callback.
     - `onDuration(seconds: Int)`: Validates `seconds in 1..86_400` before updating duration state.
     - `onEmbedRestricted(code: Int)`: Triggers fallback from `youtube-nocookie.com/embed/{id}` to `https://m.youtube.com/watch?v={lecture.youtubeId}`.
2. **WebView Navigation & Resource Interception (`AdShieldWebViewClient` in `GiantWallSecuritySuite.kt`, lines 489–568, and `YtEmbedHost.kt`, lines 2078–2099)**:
   - Blocks all non-HTTPS schemes (`file://`, `content://`, `javascript://`, `intent://`, `tel://`, `mailto://`).
   - Blocks ad/tracker domains (`doubleclick.net`, `googlesyndication.com`, `googleadservices.com`, `youtube.com/api/stats/ads`, `youtube.com/pagead/`).
   - Blocks navigation to any `/watch?v=` URL where the `v` parameter does not match the active `lecture.youtubeId`.

---

## 4. Authentication & Authorization Surface

1. **In-App YouTube WebView Sign-In (`YMusicYoutubeSignInDialog` in `YtEmbedHost.kt`, lines 1869–1982)**:
   - Optional user-initiated Google/YouTube login flow hosted in a dialog `WebView`.
   - Restricts navigation strictly to `accounts.google.com`, `youtube.com`, `youtube.co.uk`, `google.com`, and `gstatic.com` (`allowedAuthHosts` at lines 1945–1951).
   - Resulting cookies are stored in Android's `CookieManager` and used to generate `SAPISIDHASH` headers (`VideoStreamExtractor.kt`, lines 350–360) for `youtube.com` / `music.youtube.com` InnerTube requests.
2. **Cloud Firestore Security Rules (`firestore.rules`, lines 1–123)**:
   - Enforces zero-trust default deny (`match /{document=**} { allow read, write: if false; }`).
   - Requires `request.auth != null && request.auth.uid == userId` for `/users/{userId}` and subcollections (`bookmarks`, `progress`, `notes`, `favorites`).
   - Enforces strict schema validation, field-count bounds, string-length bounds, and immutability of `uid` and `createdAt`.
   - Public content collections (`/public_content/{docId}`, `/hadiths/{hadithId}`, `/duas/{duaId}`, `/namaz_steps/{stepId}`) are strictly read-only (`allow write: if false;`).

---

## 5. User Inputs, File Uploads, Database Access & Local Storage

- **User Inputs**:
  - Search bars (Discover lectures, Quran, Hadith, Maududi works), city name input (`PrayerTimesScreen.kt`), and Tasbeeh target counter.
  - Sanitized via `InputSanitizer.sanitizeSearchQuery()` (`GiantWallSecuritySuite.kt`, lines 576–619), which enforces NFC normalization, strips control/zero-width characters, strips HTML/script/event-handler tokens, and caps length at `MAX_SEARCH_QUERY_LENGTH = 120`.
- **File Uploads**:
  - No user file upload functionality exists in the application.
- **Database & Local Storage**:
  - Bundled read-only JSON assets in `app/src/main/assets/` (verified via `InputSanitizer.isSafeAssetFileName` against path traversal `..`, `/`, `\`, `%00`).
  - Local `SharedPreferences` (`mirza_lecture_prefs`, `bayan_prefs`) and `SecureKeystoreVault` (`bayan_secure_vault_v1`).
  - Local offline video files in `context.filesDir/offline_videos/` (`OfflineNamazVideoManager.kt`), where filenames are derived solely from validated `youtubeId` alphanumeric characters (`offline_namaz_${safeId}.mp4`).

---

## 6. Webhooks, Admin Surfaces, Dependencies & Network Exposure

- **Webhooks & Listening Sockets**:
  - None. The app does not open listening TCP/UDP server sockets or register push-webhook receivers.
- **Admin Surfaces**:
  - None in the mobile client; Firestore rules explicitly block client-side writes to public collections (`allow write: if false;`).
- **Key Dependencies (`gradle/libs.versions.toml` & `app/build.gradle.kts`)**:
  - Jetpack Compose BOM `2026.01.01`, AndroidX Core KTX `1.17.0`, Activity Compose `1.12.3`, Navigation Compose `2.9.7`
  - AndroidX Media3 (`media3-exoplayer`, `media3-exoplayer-hls`, `media3-exoplayer-dash`, `media3-ui`) `1.3.1`
  - OkHttp `4.12.0`, Retrofit `2.11.0`, Coil Compose `2.7.0`
  - AndroidX Security Crypto (`security-crypto`) `1.1.0-alpha06`
  - Kotlinx Coroutines `1.10.2`, Kotlinx Serialization JSON `1.8.0`
