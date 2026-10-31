# Bayan al-Huda — Final Engineering & Verification Report (Build Spec v4.0)

## 1. Executive Summary
Implemented the **Understand Islam Video Assets (Engineer Muhammad Ali Mirza Lectures Since 2013)**, the **Watch-Time Based Topic Recommendation Engine**, the **Fikr-e-Maududi Bibliographic & Licence-Enforced Library**, and the **Unified Room `@Fts4` Search & Discovery Engine** inside **Bayan al-Huda** while keeping all existing Quran, Hadith, Prayer, Azan, Noorani Qaida, Namaz-e-Mutarjam, Duas, and `AppDatabase` (version 8) modules 100% untouched.

## 2. Preflight & Sacred Module Protection (`C1`–`C3`)
- **Sacred Content Integrity (`C1`)**: Zero edits to Quran text, Uthmani script, verse numbering, Quran audio reciters, Sunni/Shia Hadith collections, prayer times, or Azan alarms.
- **Database Isolation (`C2`, `V7`, `T13`)**: `AppDatabase.kt` remains untouched at version 8 with its existing 16 entities and migrations `1..8`. All new knowledge entities and `@Fts4` tables reside in `KnowledgeDatabase.kt` (`knowledge.db`, version 1).
- **Zero New Dependencies (`C3`)**: Built exclusively using the existing Android SDK, Jetpack Compose, Room (`2.7.0`), and `HttpURLConnection`.

## 3. Section A — Engineer Muhammad Ali Mirza Video Lecture Library (Since 2013)
- **Seed Asset (`app/src/main/assets/mirza_lectures_v3.json`)**:
  - Total records: **169** (`counts.total == 169`, all unique 11-character YouTube IDs matching `^[A-Za-z0-9_-]{11}$`).
  - Verification state breakdown: **10 `CONFIRMED_LIVE`**, **159 `UNPROBED`**.
  - Format breakdown: **120 `CLIP`**, **49 `FULL_LECTURE`**.
  - Series coverage: Spans **2013–2025**, including the **Tafseer-ul-Quran Series** (`lectureNumber` spanning `1..344`), **Imam Hussain A.S. & Karbala**, **Reality of Yazeed**, **Fazail-e-Ahl al-Bayt & Sahaba**, **Salah & AZAN** (`DPvekksFmBs`, `XANsNmxxE90`), **Tawheed & Sunnah**, and **Ilm-ul-Hadith**.
  - Coverage disclosure rendered in UI: `"169 seed records · 159 unverified · Tafseer series runs to 344 — tap to load more"`.

## 4. Watch-Time Based Topic Recommendation Engine
- Tracks local per-video (`mirza_watch_vid_<youtubeId>`) and per-topic (`mirza_watch_topic_<topicId>`) watch time in `SharedPreferences` (`mirza_discovery_prefs`, bounded ≤ 96 KB, recents/bookmarks capped at 200, zero PII, zero network uploads).
- Dynamically ranks related lectures at the top based on which video titles and topics accumulate the most watch time:
  - Watching a lecture on **Imam Hussain A.S.** (`karbala_hussain`) automatically promotes **Karbala**, **Reality of Yazeed** (`reality_of_yazeed`), and **Ahl al-Bayt** (`ahl_al_bayt`) lectures to the top of the user's feed and player suggestions.
  - Watching a lecture on **Salah & AZAN** (`salah_azan`) or **Quran Tafseer** (`tafseer_quran`) dynamically promotes related videos on that topic to the top.

## 5. Hardened YouTube Privacy-Enhanced Player (`player/YtEmbedHost.kt`)
- Loads `https://www.youtube-nocookie.com/embed/{id}?rel=0&enablejsapi=1&origin=https://www.youtube-nocookie.com` via `loadUrl(url, mapOf("Referer" to "https://www.youtube-nocookie.com"))` (preventing Error 153).
- Hardened `WebSettings`: `javaScriptEnabled = true`, `allowFileAccess = false`, `allowContentAccess = false`, `MIXED_CONTENT_NEVER_ALLOW`.
- Handles IFrame `onError` codes (`2`, `5`, `100`, `101`, `150`, `153`) with the required fallback card: `"This video is not playable in your region or has been removed."` + `"Find another version"` + `"Open on YouTube"` (`resolveActivity()` verified) + `"Report broken"`.
- Data policy selector (`SAVER`, `BALANCED` default, `QUALITY`, `AUTO`) + `WIFI_ONLY` switch + helper note `"Picture quality is set inside the YouTube player's ⋮ menu."` + `~` *approx* data estimates.

## 6. Source Tiers & Provenance Badges
- `OFFICIAL` (Green): Assigned strictly to `@EngineerMuhammadAliMirzaClips`, `@EngineerMuhammadAliMirzaComp`, `UCThbMTLFbeDGrZlb9DnTKDg`, `ahlesunnatpak.com`, and `askemam.pk`.
- `FAN_CHANNEL` (Amber): Community/student clip uploads.
- `THIRD_PARTY_ARCHIVE` (Grey): Public archive mirrors (`archive.org`, `readmaududi.com`, `openlibrary.org`).
- `UNKNOWN_PENDING` (Red): Unverified channels (including `wgd4axKYutc`), hidden in release builds (`!BuildConfig.DEBUG`).

## 7. Section B — Fikr-e-Maududi Bibliographic & Licence-Enforced Library
- Enforces the `MaududiLicenceState` state machine (`LINK_ONLY`, `LICENSED_EXCERPT`, `FULL_TEXT_PUBLIC_DOMAIN`, `OWN_PERMISSION`).
- All seeded works default to `LINK_ONLY` with `bodyText = null` and `CommentaryEntryEntity.text = null`.
- Displays the data-driven notice: `"Tafheem-ul-Quran text is not bundled. This section provides bibliographic records and links to authorised reading sources until a lawful edition is obtained."`
- Provides 4 structured categories: (1) Tafheem-ul-Quran, (2) Books & Writings, (3) Ideas & Themes, and (4) Research & Primary References (tagged with `quotation | summary | editorial note`).

## 8. Section C — Separate `KnowledgeDatabase` & FTS4 Search (`knowledge.db`)
- 11 relational tables (`authors`, `works`, `editions`, `chapters`, `verse_refs`, `commentary_entries`, `lectures`, `topics`, `source_links`, `knowledge_progress`, `knowledge_bookmarks`) and 2 `@Fts4(tokenizer = unicode61)` external-content tables (`lecture_index`, `commentary_index`).
- `FtsQuerySanitizer` strips FTS/SQL metacharacters, normalizes NFKD diacritics, lowercases with `Locale.ROOT`, and wraps tokens in double quotes.

## 9. Runtime Delta Index Sync (`net/IndexSync.kt`) & Offline Tool (`tools/mirza/ingest.py`)
- `IndexSync.kt`: HTTPS allowlist check, ETag/Last-Modified headers, `MAX_INDEX_BYTES = 2 MiB`, and mandatory SHA-256 verification before atomic file replacement; falls back to bundled asset on mismatch.
- `tools/mirza/ingest.py`: Offline Python tool verifying dataset invariants, optional YouTube oEmbed probing, and generating `tools/mirza/topic_proposals.json`.

## 10. Security, Privacy & Commercial Policy Audit (`V3`)
- Zero ad SDKs, zero IAP, zero trackers, cleartext traffic disabled, WebView file/content access disabled, external intents checked via `SecurityIntegrityGuard.isExternalIntentSafe()` and `resolveActivity()`.

## 11. Test & Verification Summary (`T1`–`T15`)
- `KnowledgeAndMirzaVerificationTest` verifies all 14 spec assertions (`T1`–`T14`) plus `T15` (Watch-Time Topic Recommendation Engine).
