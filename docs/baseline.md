# Bayan al-Huda — Preflight Baseline & Allow Set (Build Spec v4.0)

## Table A — Existing Module & File Classification (`FROZEN` vs `EDITABLE`)

| Path / Module | Classification | Rationale |
|---|---|---|
| `app/src/main/java/com/example/data/local/AppDatabase.kt` | **FROZEN** | Hard constraint C2: Version 8 schema (16 entities, migrations 1..8) must never be touched. |
| `app/src/main/java/com/example/data/local/Entities.kt` | **FROZEN** | Existing Room entities for Quran, Hadith, Prayer, Bookmarks, Notes, Duas. |
| `app/src/main/java/com/example/data/local/IslamicDao.kt` | **FROZEN** | Existing DAO for `AppDatabase`. |
| `app/src/main/java/com/example/ui/quran/**` | **FROZEN** | Hard constraint C1: Quran reader, Uthmani text, verse numbering, reciter audio untouched. |
| `app/src/main/java/com/example/ui/screens/QuranScreen.kt` | **FROZEN** | Hard constraint C1: Quran UI untouched. |
| `app/src/main/java/com/example/ui/screens/HadithScreen.kt` | **FROZEN** | Hard constraint C1: Sunni & Shia Hadith collections untouched. |
| `app/src/main/java/com/example/ui/screens/HomeScreen.kt` | **FROZEN** | Hard constraint C1: Prayer schedules, Azan, Daily Ayah/Hadith untouched. |
| `app/src/main/java/com/example/ui/screens/DailyDuasScreen.kt` | **FROZEN** | Existing Duas screen untouched. |
| `app/src/main/java/com/example/ui/screens/LearnIslamScreen.kt` | **FROZEN** | Existing Noorani Qaida, Kalimas, Wudu & Namaz-e-Mutarjam untouched. |
| `app/src/main/java/com/example/ui/screens/QiblaCompassScreen.kt` | **FROZEN** | Qibla compass untouched. |
| `app/src/main/java/com/example/ui/screens/SehriIftarTimetableScreen.kt` | **FROZEN** | Ramadan & yearly Sehri/Iftar timetable untouched. |
| `app/src/main/java/com/example/data/prayer/**` | **FROZEN** | Prayer calculation & Azan alarm engine untouched. |
| `app/src/main/assets/quran_complete_v1.dat` | **FROZEN** | Canonical 6,236-ayah Quran offline asset untouched. |
| `app/src/main/assets/qul_translations_offline_v1.dat` | **FROZEN** | Canonical offline translations untouched. |
| `app/src/main/assets/hadith_corpus/**` | **FROZEN** | Canonical offline Hadith corpus untouched. |
| `app/build.gradle.kts` | **FROZEN** | Hard constraint C3: Zero new Gradle dependencies added; `AppDatabase` / signing configs untouched. |
| `app/src/main/AndroidManifest.xml` | **EDITABLE** | Add `<queries>` intent declaration for `https://www.youtube.com` / `vnd.youtube` so `resolveActivity()` works on API 30+ (Spec §6.1). |
| `app/src/main/java/com/example/data/remote/SecurityIntegrityGuard.kt` | **EDITABLE** | Allowlist `youtube.com`, `www.youtube.com`, `www.youtube-nocookie.com`, `youtu.be`, `ahlesunnatpak.com`, `www.ahlesunnatpak.com`, `askemam.pk`, `readmaududi.com`, `www.readmaududi.com`, `openlibrary.org` in `ALLOWED_HTTPS_HOSTS`. |
| `app/src/main/java/com/example/ui/screens/LibraryScreen.kt` | **EDITABLE** | Integrate Video Assets ("Understand Islam — Engr. Muhammad Ali Mirza Lectures since 2013" with Watch-Time Topic Recommendations) & Fikr-e-Maududi Library tabs into the Library experience. |

---

## Table B — Allow Set of Files to Create or Modify

| File Path | Action | Purpose |
|---|---|---|
| `docs/baseline.md` | Create | Preflight inventory (`FROZEN` vs `EDITABLE`) and allow set. |
| `docs/evidence.md` | Create | Verification & link audit table (`L01`–`L35`, `V1`–`V7`). |
| `docs/report.md` | Create | Final 11-section engineering & verification report (§12). |
| `app/src/main/assets/mirza_lectures_v3.json` | Create | 169-record seed dataset of Engineer Muhammad Ali Mirza lectures (since 2013), source tiers, verification states, topics, and Tafseer series (`1..344`). |
| `app/src/main/assets/discovery_seed_v1.json` | Create | Playback data profiles (`SAVER`, `BALANCED`, `QUALITY`, `AUTO`), `qualityLadderApproxKbps`, topic taxonomy with `minimumRecordsToShow`, BM25 weights, and watch-time topic similarity graph. |
| `app/src/main/java/com/example/data/model/LectureModels.kt` | Create | Domain models & enums for Engineer Muhammad Ali Mirza lectures, source tiers, verification states, playback profiles, and watch-time recommendation signals. |
| `app/src/main/java/com/example/data/model/MaududiModels.kt` | Create | Domain models & licence state machine (`LINK_ONLY`, `LICENSED_EXCERPT`, `FULL_TEXT_PUBLIC_DOMAIN`, `OWN_PERMISSION`) for Fikr-e-Maududi works. |
| `app/src/main/java/com/example/data/local/KnowledgeDatabase.kt` | Create | Separate Room database (`knowledge.db`, version 1) with 11 entities and `@Fts4` external-content tables `LectureIndexEntity` and `CommentaryIndexEntity`. |
| `app/src/main/java/com/example/data/local/KnowledgeDaos.kt` | Create | DAOs for `KnowledgeDatabase` (`LectureDao`, `MaududiDao`, `KnowledgeSearchDao`, `KnowledgeProgressDao`) and `FtsQuerySanitizer`. |
| `app/src/main/java/com/example/data/repository/LectureRepository.kt` | Create | Streaming `JsonReader` parser (`MAX_ASSET_BYTES = 5 MiB`), Room seeder, and Watch-Time Based Topic Recommendation Engine (`mirza_*` prefs ≤ 96 KB, recents/bookmarks capped at 200). |
| `app/src/main/java/com/example/data/repository/MaududiRepository.kt` | Create | Bibliographic & licence-enforced repository for Fikr-e-Maududi works (Tafheem-ul-Quran, Books, Themes, Research & References). |
| `app/src/main/java/com/example/data/repository/DiscoverRepository.kt` | Create | Unified FTS4 BM25 + watch-time topic-boosted discovery & search repository across lectures and works. |
| `app/src/main/java/com/example/net/IndexSync.kt` | Create | HTTPS-only ETag/Last-Modified delta index sync with `MAX_INDEX_BYTES = 2 MiB` and SHA-256 verification before atomic swap. |
| `app/src/main/java/com/example/player/YtEmbedHost.kt` | Create | Hardened `youtube-nocookie.com` IFrame player (`Referer` header, `allowFileAccess = false`, `MIXED_CONTENT_NEVER_ALLOW`, watch-time progress tracking, error card for codes 2/5/100/101/150/153). |
| `app/src/main/java/com/example/ui/discover/SearchViewModel.kt` | Create | ViewModel managing lectures, watch-time topic recommendations, playback data policy (`SAVER/BALANCED/QUALITY/AUTO`), Fikr-e-Maududi library, and FTS4 search. |
| `app/src/main/java/com/example/ui/discover/DiscoverScreen.kt` | Create | Understand Islam video & book discovery hub with "Suggested For You (Based on Watch Time)", Topic filters, Tafseer 1..344 browser, and FTS4 search. |
| `app/src/main/java/com/example/ui/discover/LectureDetailScreen.kt` | Create | Lecture player screen with `YtEmbedHost`, watch-time tracker, source tier badge, data-policy selector, related videos on top, and provenance disclosure. |
| `app/src/main/java/com/example/ui/discover/LibraryScreen.kt` | Create | Fikr-e-Maududi 4-category library screen (`Tafheem-ul-Quran`, `Books & Writings`, `Ideas & Themes`, `Research & References`). |
| `app/src/main/java/com/example/ui/discover/ReaderScreen.kt` | Create | Licence-state-aware reader screen enforcing `LINK_ONLY` (`text == null` -> outbound verified link card) vs licensed text rendering. |
| `app/src/main/java/com/example/ui/screens/LibraryScreen.kt` | Modify | Add "Video Lectures (Understand Islam)" and "Fikr-e-Maududi" tabs inside the existing Library screen so users can access all video assets and Islamic understanding books directly in Library. |
| `app/src/main/AndroidManifest.xml` | Modify | Add `<queries>` block for `https://www.youtube.com` and `vnd.youtube` intents. |
| `app/src/main/java/com/example/data/remote/SecurityIntegrityGuard.kt` | Modify | Add verified HTTPS domains to `ALLOWED_HTTPS_HOSTS`. |
| `tools/mirza/ingest.py` | Create | Offline Python verification script (oEmbed probe + n-gram topic proposal generator). |
| `tools/mirza/topic_proposals.json` | Create | Initial offline topic proposal artifact generated for maintainer curation. |
| `app/src/test/java/com/example/KnowledgeAndMirzaVerificationTest.kt` | Create | Automated JVM/Robolectric test suite verifying all 14 assertions `T1`–`T14`. |
