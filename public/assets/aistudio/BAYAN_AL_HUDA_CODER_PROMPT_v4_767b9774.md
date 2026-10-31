# BAYAN AL-HUDA — BUILD SPEC v4.0 (single-pass coder prompt)

> **For the coding agent.** Model-agnostic; tuned for a frontier coding model with repo access.
> Read once, then execute §3 → §11 in order. Never restate this file. Never ask for permission to begin.
> Stop only where a `⛔ STOP` marker appears.
> **v4.0 supersedes v2/v3**: it fixes a dead-UI bug (V1), a factual error about the Maududi dataset (V2), a licence gate the earlier drafts missed (V3), and a false-reporting trap in §12.

**Target:** `https://github.com/zulqarnain7861/Bayan-ul-huda-quran-o-hadees.git`
**Baseline:** `1837db5f196cea468d1cb561bfb95b14a5b2f4bb` · versionCode 8 · versionName 1.0.2
**Existing assets you must reuse, not recreate:** `mirza_lectures_v3.json` (169 records), `discovery_seed_v1.json` (playback profiles, topic registry, BM25 weights).

---

## 1. EFFICIENCY PROTOCOL (how to spend your context)

| # | Rule |
|---|---|
| E1 | Preflight reads are capped: `git log -1`, `ls app/src/main/java/**`, the 6 files you will actually edit, `build.gradle(.kts)`, `AndroidManifest.xml`. Do not read the whole tree, do not read assets byte-by-byte. |
| E2 | One file per message after preflight. Full file only when creating; otherwise a unified diff with 3 lines of context. |
| E3 | Emit code, not commentary. No plan restatement, no "as requested", no apology loops. |
| E4 | Never re-derive a fact marked **VERIFIED (V\*)** in §2, and never re-fetch a URL in §7 whose status is `CHECKED`. |
| E5 | Batch independent work in one message (e.g. all model classes together), then stop for compile. |
| E6 | If two requirements conflict, apply the lower-numbered rule in §2 and note it once in the final report. Do not silently drop a requirement. |

---

## 2. HARD CONSTRAINTS — every row is a gate, each is unit-testable

| # | Constraint |
|---|---|
| C1 | **Preserve.** Never modify, renumber, reformat or regenerate: the Arabic Quran text, verse numbering, Quran audio/reciters, existing translations/tafseer, Sunni hadith records, Shia hadith records, hadith numbering and source refs, prayer-time calculation, Azan. Locate each owner file in preflight and list it in `docs/baseline.md` as `FROZEN`. |
| C2 | **Never touch `AppDatabase`.** No new entity, no schema bump, no `exportSchema` flip, no migration. All new tables live in a new `KnowledgeDatabase` (`knowledge.db`, version 1). |
| C3 | **No new Gradle dependency.** No Retrofit, Hilt, Coil, Room KTX beyond what exists. `HttpURLConnection` + `org.json` only. |
| C4 | **No fabricated data — ever.** Never invent a `youtubeId`, timestamp, transcript, hadith number, page number, quotation, publication year, licence, or DOI. Unknown ⇒ `null` + `referenceStatus = NONE` + the UI says so. |
| C5 | **Two content states only:** `VERIFIED` (backed by a row in `docs/evidence.md`) or `LINK_ONLY`. No "likely", no "probably", no third state. |
| C6 | **No byte-moving.** No download, cache, re-encode, mirror, proxy or region-bypass of any video or audio stream. Embed the official player; link out. |
| C7 | **Authenticity claims render from data.** No tier, licence or reference text may originate in a code literal or a `strings.xml` entry. |
| C8 | **Additive navigation only.** Append routes; do not rename or reorder existing ones, do not restructure the bottom bar. |
| C9 | **Reuse before create.** The lecture catalogue is `mirza_lectures_v3.json` + its existing screen. Extend it; never build a second parallel lecture stack. Same for Noorani Qaida and du'a content — never duplicate. |
| C10 | **Honest build reporting.** See §12. Claiming a build that did not run is a critical failure. |

---

## 3. VERIFIED FACTS — settled this session; do not re-research

| id | Fact | Consequence for your code |
|---|---|---|
| **V1** | The YouTube IFrame Player API **no longer supports setting or retrieving playback quality** (`setPlaybackQuality` / `getAvailableQualityLevels` are gone). Player error **153** = the request carried no `HTTP Referer` or equivalent client identification. `modestbranding` is a no-op. | A quality radio list is a **dead control** — do not ship one. Implement `SAVER / BALANCED / QUALITY / AUTO` as a *data policy* (§6.2). Always load the embed with a real base URL **and** a `Referer` header; `loadData()` with a null base ships a black box. |
| **V2** | `fawazahmed0/quran-api` **does** carry Maududi editions, but as **verse-by-verse translation only** — schema `{chapter, verse, text}`. Editions present: `urd-abulaalamaududi` (Urdu, rtl, source `tanzil.net`), `urd-abulaalamaududi-la` (Roman Urdu, source `quranromanurdu.com`), `eng-abulalamaududi` (English, source `tanzil.net`), `eng-maududi` (English, **`source` field empty**). | **Tafheem-ul-Quran is not in that repo** — no surah introductions, no multi-page commentary. Never label a translation as Tafheem. The two English entries duplicate an author under different strings and one has no provenance: resolve it or drop it; never ship both. |
| **V3** | Tanzil's translation terms: the translations are **for non-commercial use only**, other uses need the translator's/publisher's permission; a website or app using **more than three** Tanzil translations **must link back to** `https://tanzil.net/trans/`; redistributing Tanzil's own translation list is not allowed. | Three gates, all recorded in `Edition.licenceId` + evidence: (a) a monetised build (ads/IAP/paid) **cannot** ship the Tanzil-sourced Maududi translation — ⛔ STOP and ask; (b) if the app already carries >3 Tanzil translations, add the visible link back; (c) never copy Tanzil's catalogue into the app. |
| **V4** | Corpus reality: **169** records — **10** `CONFIRMED_LIVE`, **159** `UNPROBED`; **120** `CLIP` / **49** `FULL_LECTURE`; Tafseer `lectureNumber` spans 1–344; exactly **one** title names Maududi (`wgd4axKYutc`, `UNKNOWN_PENDING`). | Coverage footer prints facts, never placeholder numbers. Fikr-e-Maududi ships as *collecting*, not as a stocked library. |
| **V5** | `readmaududi.com` is a **reading platform** (books, video series, reading lists, its own offline/download and language features). It is not a text licence and not raw source data. | Link out only. Scraping or mirroring it would ship someone else's service — prohibited by C6's spirit and by ordinary terms-of-service risk. |
| **V6** | `fawazahmed0/quran-api`'s `LICENSE` is a public-domain / Unlicense-style dedication of **the software**. It does not by itself license every third-party translation text the repo redistributes. | Record software rights and text rights in two separate fields. Per-edition licence is unresolved until an evidence row exists. |
| **V7** | Fan channels supplied for India-accessibility are **not** confirmed India-based (at least one public directory shows a Pakistan location). Channel location does not predict video availability. | Add `countryStatus ∈ {VERIFIED_IN, VERIFIED_OTHER, UNKNOWN}`. Default `UNKNOWN`. Never print "Indian channel", never geodetect, never proxy. |

⛔ **STOP** if a stated requirement contradicts V1–V7. Ask exactly one question and continue with everything else meanwhile.

---

## 4. PREFLIGHT — emit before the first diff

Deliver `docs/baseline.md` with two tables, then proceed without waiting.

**Table A — inventory:** `path | responsibility | FROZEN / EDITABLE | why`
Cover at minimum: Quran screen + text provider, verse-numbering source, Quran audio controller, hadith providers (Sunni + Shia), hadith numbering/source refs, prayer-time + Azan, `AppDatabase` and its entities, `NavGraph`/bottom bar, `DailyDuasDataProvider`, `LearnIslamCurriculumData`, the existing Mirza lecture screen (if present), `AndroidManifest.xml`, Gradle files.

**Table B — allow set** (create or append; nothing else may change):
```
data/model/{LectureModels,MaududiModels}.kt
data/local/{KnowledgeDatabase,*Dao}.kt
data/repo/{LectureRepository,MaududiRepository,DiscoverRepository}.kt
data/seed/                     (assets copied in, not rewritten)
ui/discover/{DiscoverScreen,LectureDetailScreen,LibraryScreen,ReaderScreen,SearchViewModel}.kt
player/YtEmbedHost.kt
net/IndexSync.kt
app/src/test/**                (new test files only)
tools/**                       (python, not shipped in the APK)
docs/**                        (baseline, evidence, report)
```
Any file outside Table B needs a one-line justification in the report.

---

## 5. DATA LAYER — one new database

`KnowledgeDatabase` (`knowledge.db`, v1), **separate from `AppDatabase`**, plus these entities (explicit nullability, no shared primary keys with the old DB):
`Author` · `Work(type ∈ BOOK|TAFSEER|ESSAY|COLLECTION|CORRESPONDENCE)` · `Edition(languageTag, translator, publisher, year, licenceId, completeness ∈ FULL|PARTIAL|UNKNOWN)` · `Chapter(workId, ordinal, title, sourceUrl)` · `VerseRef(surah, ayah)` · `CommentaryEntry(editionId, verseRefId, page, text)` · `Lecture` · `Topic` · `SourceLink(ownerType, ownerId, url, tier, countryStatus, checkedOn)` · `Progress` · `Bookmark`.

Search: `@Fts4` external-content tables `LectureIndex` and `CommentaryIndex`, `tokenizer = unicode61`.
Hardening: build every FTS query through one sanitiser — strip `" ' * ( ) : ^ -`, wrap in double quotes, `Locale.ROOT` lowercasing, NFKD diacritic fold. Never pass user text to `MATCH` raw.

Limits: parse assets off the main thread via streaming (`JsonReader`), `MAX_ASSET_BYTES = 5 MiB`, `MAX_INDEX_BYTES = 2 MiB`, abort to the bundled asset on any parse error. Recents/bookmarks capped at 200; total local signal store ≤ 96 KB; keys under `mirza_*` and `disc_*`. No PII, no analytics, no upload of view history, no network call on the recommendation path.

---

## 6. SECTION A — MIRZA LECTURE LIBRARY

### 6.1 Player
`WebView` + YouTube IFrame API.
`src = https://www.youtube-nocookie.com/embed/{id}?rel=0&enablejsapi=1&origin=<assetDomain>` loaded with `loadUrl()` + `mapOf("Referer" to <assetDomain>)` — never `loadData()` (V1/error 153). `Open in YouTube` = `ACTION_VIEW` on `youtube://watch?v={id}` with https fallback, `resolveActivity()` checked, `<queries>` entry added.

### 6.2 Data policy (this is the "quality/data saver" feature)

| Profile | Autoplay Wi-Fi | Autoplay mobile | Confirm on mobile data | Thumbnail | Prebuffer budget |
|---|---|---|---|---|---|
| `SAVER` | no | no | yes | 320 px | 0 |
| `BALANCED` *(default)* | yes | no | no | 320 px | 2 MiB |
| `QUALITY` | yes | yes | yes | 1280 px | 6 MiB |
| `AUTO` | yes | no | no | 320 px | 1 MiB |

Rules: exactly one helper line — *"Picture quality is set inside the YouTube player's ⋮ menu."* Data estimates come from `discovery_seed_v1.json → qualityLadderApproxKbps` and always render with `~` and the word *approx*. `WIFI_ONLY` switch available; `offline` state ⇒ an offline card, never a blank player. No quality setter call anywhere in the codebase (grep must return zero hits for `setPlaybackQuality`).

### 6.3 Source tiers
Rendered strictly from the record's stored `sourceTier`: `OFFICIAL` green · `FAN_CHANNEL` amber · `THIRD_PARTY_ARCHIVE` grey · `UNKNOWN_PENDING` red **and hidden when `!BuildConfig.DEBUG`**. Only `@EngineerMuhammadAliMirzaClips` and `@EngineerMuhammadAliMirzaComp` are official (V-facts in §7). Every badge carries a `contentDescription` naming the tier in words — colour alone is not a label.

### 6.4 Coverage honesty
Footer: `169 seed records · 159 unverified · Tafseer series runs to 344 — tap to load more` (disabled until the ingest tool runs). Category chips come from `discovery_seed_v1.json → topics`; a chip renders only when ≥ `minimumRecordsToShow` verified records exist, so the requested "Hadith explanations", "Seerah", "Sahaba & Ahl al-Bayt", "Q&A" chips stay hidden until the data exists (V4).

### 6.5 Unavailable video
IFrame `onError` 2/5/100/101/150/153 ⇒ card, never a blank WebView:
`This video is not playable in your region or has been removed.` + `Find another version` (alternates from the index only; else the honest empty state `No verified alternate copy indexed`) + `Open on YouTube` + `Report broken` (local counter only).
No country detector, no availability prediction, no circumvention, no synthesised search URL presented as an alternate.

### 6.6 Per-record fields you must add
`publishedAt` (null unless actually seen), `durationSec` (`Long`, learned once via `getDuration()` at first play — never scraped), `transcriptStatus ∈ {NONE, PARTIAL, VERIFIED}`. Quranic ayah and hadith references live in their own typed fields with their own `referenceStatus`; they never merge into the lecturer's explanation text.

---

## 7. LINK REGISTRY — keep every link, with handling

Status: `CHECKED` = verified this session (§3 fact id) · `TO-AUDIT` = you must probe and log one evidence row (§8). A link resolving is **never** proof of rights, completeness or authenticity.

### 7.1 Official Mirza sources
| id | URL | Handling |
|---|---|---|
| L01 | `https://www.youtube.com/channel/UCThbMTLFbeDGrZlb9DnTKDg` | `OFFICIAL` after cross-check with L05/L06 (channel ID is the identity — handles change) |
| L02 | `https://www.youtube.com/@EngineerMuhammadAliMirzaClips` | `OFFICIAL` (clips) — resolve to its channel ID and store the ID |
| L03 | `https://www.youtube.com/@EngineerMuhammadAliMirzaComp` | `OFFICIAL` (full lectures) — resolve to its channel ID and store the ID |
| L04 | `https://ahlesunnatpak.com/` | primary site — TO-AUDIT |
| L05 | `https://ahlesunnatpak.com/video` | catalogue source — TO-AUDIT |
| L06 | `https://www.ahlesunnatpak.com/biography` | identity reference — TO-AUDIT |
| L07 | `https://askemam.pk/` | secondary catalogue candidate — TO-AUDIT (verify ownership/provenance) |

### 7.2 Fan-channel candidates — `FAN_CHANNEL` at best, never `OFFICIAL`, never "Indian"
| id | URL | Handling |
|---|---|---|
| L08 | `https://www.youtube.com/channel/UC5VYosjbcRb3yKfvl-YuJFw` | candidate — TO-AUDIT, `countryStatus = UNKNOWN` |
| L09 | `https://www.youtube.com/@RAAH-e-HIDAYAT-EMAM` | same entity as L08 — store both, canonicalise to the channel ID |
| L10 | `https://www.youtube.com/channel/UC9eI4WttwsCScbh_T0WGsKg` | candidate — TO-AUDIT; a public directory places it outside India, so `countryStatus` must not be `VERIFIED_IN` without evidence (V7) |
| L11 | `https://www.youtube.com/channel/UCcX8yu38GgU6Z5ICPG0Uevg` | small candidate — TO-AUDIT |

### 7.3 Lecture test vectors (schema/timestamp handling only — not a catalogue substitute)
| id | URL |
|---|---|
| L12 | `https://www.youtube.com/watch?v=DPvekksFmBs` |
| L13 | `https://www.youtube.com/watch?v=XANsNmxxE90` |

### 7.4 Maududi datasets and repositories
| id | URL | Handling |
|---|---|---|
| L14 | `https://github.com/fawazahmed0/quran-api` | software = public-domain dedication (**CHECKED**, V6); text rights per edition unresolved (V2) |
| L15 | `https://cdn.jsdelivr.net/gh/fawazahmed0/quran-api@1/editions.json` | edition catalogue (**CHECKED**, V2) — translation only |
| L16 | `https://tanzil.net/trans/` | upstream terms (**CHECKED**, V3) — non-commercial + >3 link-back gate |
| L17 | `https://github.com/Sheople3/data-quran` | TO-AUDIT; reported CC BY-NC-ND 4.0 — NC **and** ND block commercial use and adaptation; read the actual `LICENSE` before any use |
| L18 | `https://github.com/mamun-al-abdullah/quran` | TO-AUDIT; same reported licence — same caution |
| L19 | `https://quran-bash-scripts.github.io/` | resource index, not a licensor — TO-AUDIT, then link only |

### 7.5 Maududi books, catalogues, reading routes
| id | URL | Handling |
|---|---|---|
| L20 | `https://www.readmaududi.com/` | reading route only (V5, `CHECKED`) — never scrape, mirror or re-host |
| L21 | `https://archive.org/search?query=Abul+Ala+Maududi` | item-by-item rights; default `LINK_ONLY` |
| L22 | `https://openlibrary.org/search?q=Abul+Ala+Maududi` | **bibliographic only** — a catalogue record grants no text rights |
| L23 | `https://archive.org/search?query=Abul%20Ala%20Maududi` | duplicate of L21 in encoded form — keep it, resolve both to the same handler |

### 7.6 GitHub discovery queries (run ≤ 4 of them, then stop)
| id | URL |
|---|---|
| L24 | `https://github.com/search?q=Engineer+Muhammad+Ali+Mirza&type=repositories` |
| L25 | `https://github.com/search?q=Islamic+lectures&type=repositories` |
| L26 | `https://github.com/search?q=Maududi&type=repositories` |
| L27 | `https://github.com/search?q=Abul+Ala+Maududi&type=repositories` |
| L28 | `https://github.com/search?q=Abul+Al+a+Maududi&type=code` |
| L29 | `https://github.com/search?q=Tafheem-ul-Quran&type=repositories` |
| L30 | `https://github.com/search?q=Tafheem+ul+Quran&type=code` |
| L31 | `https://github.com/search?q=Urdu+Islamic+books&type=repositories` |
| L32 | `https://github.com/search?q=Urdu+Tafseer+Quran&type=repositories` |
| L33 | `https://github.com/search?q=Islamic+books+dataset&type=repositories` |
| L34 | `https://github.com/search?q=Quran+translations+dataset&type=repositories` |
| L35 | `https://github.com/search?q=Quran+Tafseer+JSON&type=repositories` |

A search-result hit is a **lead**, never evidence. Promote a lead only after opening the repo, its `LICENSE`, and the actual data file.

---

## 8. SECTION B — FIKR-E-MAUDUDI LIBRARY ( licence state machine )

Every Maududi work starts `LINK_ONLY`. Text may ship **only** when one state holds, with an evidence row:

| State | Ships text? | Gate |
|---|---|---|
| `LINK_ONLY` *(default)* | no | bibliographic row + outbound link |
| `LICENSED_EXCERPT` | ≤ 300-word quoted passage | written permission reference stored |
| `FULL_TEXT_PUBLIC_DOMAIN` | yes | explicit public-domain determination incl. jurisdiction |
| `OWN_PERMISSION` | yes | dated statement + conditions stored verbatim |

Rules:
1. **A repo that only links PDFs, or whose "data" is scanned images, is not a data source.** Verified text requires machine-readable text **plus** a licence permitting offline storage and redistribution.
2. CC BY-NC-ND material (L17, L18 — if confirmed) cannot be adapted, excerpted into a modified UI, or used in a monetised build. Default those to `LINK_ONLY`.
3. Tanzil-sourced translation obeys V3 in full.
4. Keep the Arabic Quran text of the existing app untouched (C1); a Maududi translation is a **separate layer** joined by `VerseRef`, never merged into the existing text column or the existing tafseer table.

### 8.1 Library categories (navigation only — never a claim of completeness)
- **Cat 1 — Tafheem-ul-Quran:** Urdu and English editions *where a lawful source permits*; surah navigation, verse-linked commentary, search within commentary, bookmarks, reading progress; show author, translator, edition, source. If no lawful text exists: bibliographic card + `Read at source` link (L20) + `availability = NOT_IN_APP`.
- **Cat 2 — Books & writings** (discovery targets only, each `LINK_ONLY` until verified): Tafheem-ul-Quran · Tafhimat · Khutbat/Tasreehat/Tanqeehaat · Islami Nizam-e-Zindagi / Islamic Way of Life · political & social thought · essays · correspondence. Store title, language, edition, translator/editor, publisher, verified year, completeness, `licenceId`, source URL.
- **Cat 3 — Ideas & themes:** Quranic understanding · Islamic worldview · Tawheed & Risalah · Islamic society · ethics · education & character · social/political thought · Islamic history · contemporary questions · religion & modern life. Each theme links to a book/chapter/essay or an authorised source. **No quotation may be authored by you** (C4).
- **Cat 4 — Research & references:** title, edition, chapter/section, page *only when verified*, language, publication data, source link, licence notice; and an explicit `quotation | summary | editorial note` tag on every text block.

### 8.2 Reality check to print in the app
`Tafheem-ul-Quran text is not bundled. This section provides bibliographic records and links to authorised reading sources until a lawful edition is obtained.` — driven by a data flag, not a literal.

---

## 9. DISCOVERY & SYNC PROTOCOL

**Budget:** ≤ 25 network retrievals for the whole task. Every retrieval appends one row to `docs/evidence.md`:
`id | claim | url | retrievedUtc | method (oEmbed/HEAD/GET/README/LICENSE/data-file) | verdict SUPPORTS|CONTRADICTS|INCONCLUSIVE | ships in app yes|no`

**Lecture verification (offline tool `tools/mirza/ingest.py`, never shipped):** catalogue crawl → YouTube **oEmbed** (`title` + `author_url`) as the only verifier → tier from `author_url` allowlist → 404 drops the record. A record may move `UNPROBED → CONFIRMED_LIVE` **only** through this script; `counts` are recomputed, never typed.

**Topic proposals:** offline n-gram (1–3), ≥ 3 distinct videos, IDF ≥ 3.5 → `tools/mirza/topic_proposals.json`, `requireHumanApproval = true`, app shows nothing until a human approves.

**Runtime sync (`net/IndexSync.kt`):** HTTPS-only host allowlist, `HttpURLConnection`, ETag/Last-Modified, `MAX_INDEX_BYTES`, SHA-256 verify, additive-only merge, **no deletes**, on mismatch keep the bundled asset and log locally — never surface a partial index.

---

## 10. NEUTRALITY, SECURITY, CRASH GATES

**Neutrality.** The app records *what a speaker said and which sources he cited*. It issues no verdict, declares no school, and must never print "pure Islam", "the only correct sect", "this is the true Islam", "guaranteed authentic", "100% sahih". Every row carries `speaker`, `sourceTier`, `claimType ∈ {QURAN_AYAH, HADITH, FIQH_RULING, HISTORICAL_REPORT, SPEAKER_OPINION}`, `verificationStatus`. A sensitive topic (Fikr-e-Maudoodi, Replies & comparative views) renders only with ≥ 3 verified records; below that it shows a *collecting* empty state. Nothing is fabricated to fill a category.

**WebView.** `javaScriptEnabled = true` (required by IFrame); `allowFileAccess = false`; `allowContentAccess = false`; `mixedContentMode = NEVER_ALLOW`; exactly one `@JavascriptInterface` reporter, no more; add on `onCreate`, remove + release on `onDestroy`; pause on `onPause`. No cleartext traffic anywhere.

**Intents & data.** `resolveActivity()` before every launch; `setPackage` where known; no exported Activity reachable from an untrusted deep link; validate every remote id against `^[A-Za-z0-9_-]{11}$`; length-cap every remote string; ignore unknown JSON fields; size-cap before parse.

**Kotlin hygiene.** No `!!` on parsed JSON; `try/catch` around every parse and every player callback; `runBlocking` banned on main; coroutines bound to `viewModelScope`; `DiffUtil`/`ListAdapter` in lists; `Locale.ROOT` for all lowercasing; `durationSec` as `Long`; no screenshot or export of reading state; no `RECORD_AUDIO`, no external-storage permission.

---

## 11. TESTS — extend `app/src/test`, then build

| id | Assertion |
|---|---|
| T1 | Lecture asset parses; 169 records; `counts` equal array length |
| T2 | No duplicate `youtubeId`; all match the 11-char regex; exactly 10 `CONFIRMED_LIVE` |
| T3 | Release variant exposes zero `UNKNOWN_PENDING` rows |
| T4 | Search finds "AZAN" across `title` and `verifiedTitle` |
| T5 | Four tier values map 1:1 to four badges + four `contentDescription`s |
| T6 | Bookmarks/recents survive simulated process death; cap 200 enforced |
| T7 | IFrame error 150 **and** 153 produce the error card, not a blank WebView |
| T8 | No entity holds non-null book text while `licenceId = LINK_ONLY` |
| T9 | No row with `referenceStatus = NONE` carries a non-null reference |
| T10 | FTS sanitiser neutralises `" OR *` style input without throwing |
| T11 | Index with a bad SHA-256 is discarded and the bundled asset stays active |
| T12 | Zero occurrences of `setPlaybackQuality` / `getAvailableQualityLevels` in the source tree |
| T13 | No new entity, migration or version bump in `AppDatabase` (diff assertion) |
| T14 | All pre-existing Quran / hadith / audio / prayer-time tests still pass |

Then, **only if an Android toolchain is actually available in your environment:** `./gradlew assembleDebug` and `./gradlew testDebugUnitTest`.

---

## 12. FINAL REPORT — evidence, or it did not happen

Write `docs/report.md` with these eleven sections, each one line per item:

1. Files added · files modified (paths).
2. `docs/evidence.md` reproduced as a table.
3. Repositories/datasets **inspected** (list).
4. Repositories/datasets **integrated** — a strict subset of §3, with `licenceId` each.
5. Maududi works: title · edition · language · completeness · `licenceId` · ships in app yes/no.
6. Lecture coverage: verified count · working links · dead links · unverified count.
7. Still unavailable, with the reason for each.
8. Copyright/attribution checks performed; unresolved legal questions (state them plainly, do not resolve them by guessing).
9. Tests run: id · pass/fail (a test not run is reported as `NOT RUN`, never as pass).
10. Build status — **honest**: if you could not run Gradle, write `No APK produced in this environment` and list the exact commands the release owner must run. Expected local path once built: `app/build/outputs/apk/debug/app-debug.apk`. **Never state an APK path you did not produce.**
11. Versioning note: `versionCode` stays 8 in this change set; the release owner bumps it.

**Any claim without an evidence row belongs in §7 of the report, not in the app.**

---

## 13. ANTI-PATTERNS — a hit on any line means revert and redo

- A quality dropdown or "1080p" button that does nothing (V1).
- Calling a verse translation "Tafheem-ul-Quran" (V2).
- Bundling text whose licence was never read (V3, V6).
- Scraping or mirroring `readmaududi.com` (V5).
- Labelling a fan channel "official", or "Indian" without evidence (V7).
- A Room migration on the frozen DB (C2).
- A fabricated `youtubeId`, timestamp, hadith number, page number or quote (C4).
- A topic chip that opens an empty list presented as populated (C5).
- `loadData()` with a null base URL, or a WebView left alive after `onDestroy` (V1).
- A build or APK path reported but never produced (C10).
