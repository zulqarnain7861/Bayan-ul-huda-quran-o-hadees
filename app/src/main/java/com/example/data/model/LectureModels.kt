package com.example.data.model

/**
 * Domain models, provenance tiers, verification states, playback data profiles,
 * and Watch-Time Topic Recommendation signals for Section A (Engineer Muhammad Ali Mirza
 * Video Lecture Library since 2013).
 *
 * Strictly adheres to Bayan al-Huda Build Spec v4.0:
 * - Zero fabricated fields; unknown fields remain null.
 * - Strict 11-character YouTube ID validation (`^[A-Za-z0-9_-]{11}$`).
 * - Official source tier assigned ONLY to verified official channels/portals.
 */

enum class SourceTier(
  val badgeLabel: String,
  val contentDescription: String
) {
  OFFICIAL(
    badgeLabel = "OFFICIAL",
    contentDescription = "Official verified channel of Engineer Muhammad Ali Mirza"
  ),
  FAN_CHANNEL(
    badgeLabel = "FAN CHANNEL",
    contentDescription = "Community or student clip channel — not an official upload"
  ),
  THIRD_PARTY_ARCHIVE(
    badgeLabel = "ARCHIVE",
    contentDescription = "Third-party public archive mirror"
  ),
  UNKNOWN_PENDING(
    badgeLabel = "UNVERIFIED SOURCE",
    contentDescription = "Source channel pending maintainer verification"
  );

  companion object {
    val OFFICIAL_CHANNEL_HANDLES = setOf(
      "@engineermuhammadalimirzaclips",
      "@engineermuhammadalimirzacomp",
      "ucthbmtlfbedgrzlb9dntkdg"
    )

    val OFFICIAL_HOSTS = setOf(
      "ahlesunnatpak.com",
      "www.ahlesunnatpak.com",
      "askemam.pk"
    )

    fun fromRaw(raw: String?, channelHandle: String? = null, channelUrl: String? = null): SourceTier {
      val normalizedHandle = channelHandle?.trim()?.lowercase() ?: ""
      val normalizedUrl = channelUrl?.trim()?.lowercase() ?: ""
      val isOfficialIdentity = OFFICIAL_CHANNEL_HANDLES.contains(normalizedHandle) ||
        OFFICIAL_CHANNEL_HANDLES.any { normalizedUrl.contains(it) } ||
        OFFICIAL_HOSTS.any { normalizedUrl.contains(it) }

      return when (raw?.trim()?.uppercase()) {
        "OFFICIAL" -> if (isOfficialIdentity) OFFICIAL else UNKNOWN_PENDING
        "FAN_CHANNEL" -> FAN_CHANNEL
        "THIRD_PARTY_ARCHIVE" -> THIRD_PARTY_ARCHIVE
        else -> UNKNOWN_PENDING
      }
    }
  }
}

enum class CountryStatus(val label: String) {
  VERIFIED_IN("Verified playable in India & globally"),
  VERIFIED_OTHER("Verified in select regions"),
  UNKNOWN("Regional playability unprobed"),
  PLAYABLE_GLOBAL("Playable in India & globally"),
  GEO_RESTRICTED("Region restricted in some countries (Use Fan Channel alternate)"),
  REMOVED_OR_PRIVATE("Removed or private on YouTube"),
  UNVERIFIED("Playability not yet probed");

  companion object {
    fun fromRaw(raw: String?): CountryStatus =
      entries.firstOrNull { it.name.equals(raw?.trim(), ignoreCase = true) } ?: UNKNOWN
  }
}

enum class RecordVerificationState(val label: String) {
  CONFIRMED_LIVE("Confirmed Live"),
  UNPROBED("Unverified Seed Record"),
  BROKEN("Reported Broken / Unavailable");

  companion object {
    fun fromRaw(raw: String?): RecordVerificationState =
      entries.firstOrNull { it.name.equals(raw?.trim(), ignoreCase = true) } ?: UNPROBED
  }
}

enum class LectureFormat(val label: String) {
  CLIP("Short Clip"),
  FULL_LECTURE("Full Lecture");

  companion object {
    fun fromRaw(raw: String?): LectureFormat =
      entries.firstOrNull { it.name.equals(raw?.trim(), ignoreCase = true) } ?: CLIP
  }
}

enum class TranscriptStatus(val label: String) {
  VERIFIED("Verified Transcript"),
  NONE("No verified transcript attached");

  companion object {
    fun fromRaw(raw: String?): TranscriptStatus =
      entries.firstOrNull { it.name.equals(raw?.trim(), ignoreCase = true) } ?: NONE
  }
}

enum class ReferenceStatus(val label: String) {
  VERIFIED("Verified Primary References"),
  UNVERIFIED("References Pending Verification");

  companion object {
    fun fromRaw(raw: String?): ReferenceStatus =
      entries.firstOrNull { it.name.equals(raw?.trim(), ignoreCase = true) } ?: UNVERIFIED
  }
}

enum class ClaimType(val badgeLabel: String) {
  DIRECT_QUOTE("Direct Quote"),
  TOPIC_SUMMARY("Topic Summary"),
  EDITORIAL_NOTE("Editorial Note")
}

enum class PlaybackDataProfile(
  val label: String,
  val targetResolutionHint: String,
  val approxKbps: Int,
  val approxMbPerHour: Int
) {
  SAVER("Data Saver", "~240p–360p", 350, 158),
  BALANCED("Balanced", "~360p–480p", 650, 292),
  QUALITY("High Clarity", "~720p", 1800, 810),
  AUTO("Auto Adaptive", "~360p–720p", 900, 405);

  companion object {
    fun fromRaw(raw: String?): PlaybackDataProfile =
      entries.firstOrNull { it.name.equals(raw?.trim(), ignoreCase = true) } ?: BALANCED
  }
}

data class LectureSeedCounts(
  val total: Int,
  val confirmedLive: Int,
  val unprobed: Int,
  val clips: Int,
  val fullLectures: Int,
  val tafseerMinLectureNumber: Int,
  val tafseerMaxLectureNumber: Int
)

data class LectureRecord(
  val youtubeId: String,
  val title: String,
  val verifiedTitle: String?,
  val speaker: String,
  val channelHandle: String?,
  val channelUrl: String?,
  val sourceTier: SourceTier,
  val countryStatus: CountryStatus,
  val verificationState: RecordVerificationState,
  val recordFormat: LectureFormat,
  val lectureNumber: Int?,
  val seriesName: String?,
  val recordedYear: Int?,
  val durationSec: Int?,
  val topics: List<String>,
  val relatedYoutubeIds: List<String>,
  val summary: String?,
  val transcriptStatus: TranscriptStatus,
  val referenceStatus: ReferenceStatus,
  val references: List<String>,
  val alternateYoutubeIds: List<String>
) {
  val displayTitle: String
    get() = verifiedTitle?.takeIf { it.isNotBlank() } ?: title

  val channelDisplayName: String
    get() = when (channelHandle?.lowercase()) {
      "@raah-e-hidayat-emam" -> "RAAH-e-HIDAYAT [EMAM]"
      "@noor-e-hidayat" -> "NOOR-e-HIDAYAT [EMAM]"
      "@raahehidayatengineermuhammadal" -> "Raah E Hidayat (EMAM)"
      "@naik-hidayat" -> "Naik Hidayat"
      "@truedawat" -> "True Dawat [EMAM]"
      "@taleem-e-islam-t7e" -> "Taleem-e-Islam"
      "@firqa_free_islam" -> "Firqa Free Islam"
      "@muhammadehtishamawan" -> "Way To Hidayah"
      "@emanstudioofficial" -> "Eman Studios"
      "@emam321" -> "Kitab O Sunnah [EMAM]"
      "@fikreakhirat81" -> "Fikr e Akhirat"
      "@knowledgebookmuslim" -> "Knowledge Book Muslim"
      "@truedeenshort" -> "True Deen Clips"
      "@studentbyemam" -> "Student of EMAM"
      "@hidayahportal" -> "Hidayah Portal"
      "@quranaursunnat" -> "Quran Aur Sunnat"
      "@hidayah_vibes" -> "Hidayah Vibes"
      "@emam-online" -> "EMAM Online"
      "@therealislam." -> "The Real Islam"
      "@misbah_edt" -> "Misbah Clips"
      "@engineermuhammadalimirzaclips" -> "Official Clips Channel"
      "@engineermuhammadalimirzacomp" -> "Official Complete Lectures"
      else -> channelHandle ?: "Community Channel"
    }

  /**
   * Stable composite video identity combining source tier, playlist/series identity, and
   * validated 11-character YouTube ID (`sourceId:playlistId:itemId` per Section 4.1).
   * Used as the canonical key for thumbnails, list items, search results, deep links,
   * stream resolution validation, and resume position storage.
   */
  val stableVideoId: String
    get() {
      val normalizedSeries = (seriesName ?: "general")
        .trim()
        .lowercase()
        .replace(Regex("[^a-z0-9]+"), "_")
        .trim('_')
        .ifEmpty { "general" }
      return "${sourceTier.name}:$normalizedSeries:$youtubeId"
    }

  fun matchesVideoIdentity(
    candidateYoutubeId: String?,
    candidateStableId: String? = null
  ): Boolean {
    if (!isValidYoutubeId(youtubeId) || !isValidYoutubeId(candidateYoutubeId)) return false
    if (youtubeId != candidateYoutubeId) return false
    if (!candidateStableId.isNullOrBlank() && candidateStableId != stableVideoId) return false
    return true
  }

  fun shouldShowInUi(isDebugBuild: Boolean): Boolean {
    return isDebugBuild || sourceTier != SourceTier.UNKNOWN_PENDING
  }

  companion object {
    val YOUTUBE_ID_REGEX = Regex("^[A-Za-z0-9_-]{11}$")

    fun isValidYoutubeId(id: String?): Boolean {
      return id != null && YOUTUBE_ID_REGEX.matches(id)
    }
  }
}

data class LectureSeedDataset(
  val schemaVersion: Int,
  val generatedUtc: String,
  val coverageDisclosure: String,
  val officialChannels: List<String>,
  val officialPortals: List<String>,
  val counts: LectureSeedCounts,
  val records: List<LectureRecord>
)

data class DiscoveryTopicDefinition(
  val id: String,
  val titleEn: String,
  val titleUr: String,
  val minimumRecordsToShow: Int,
  val relatedTopicIds: List<String>,
  val keywords: List<String>
)

data class DiscoverySeedConfig(
  val schemaVersion: Int,
  val defaultPlaybackProfile: PlaybackDataProfile,
  val wifiOnlyDefault: Boolean,
  val playerMenuHelperNote: String,
  val qualityLadderApproxKbps: Map<String, Int>,
  val bm25Weights: Map<String, Double>,
  val topics: List<DiscoveryTopicDefinition>
)

/**
 * Captures why a lecture is recommended at the top of the user's feed based on
 * local watch-time telemetry (e.g., watching a video about Imam Hussain A.S.
 * promotes Karbala, Reality of Yazeed, and Ahl al-Bayt lectures to the top).
 */
data class RecommendedLectureItem(
  val lecture: LectureRecord,
  val score: Double,
  val reasonLabel: String,
  val matchedTopicIds: List<String>,
  val userWatchedSecondsOnTopic: Int
)

enum class IslamicVideoCategory(
  val displayLabel: String,
  val mappedTopicIds: Set<String>,
  val titleKeywords: List<String>
) {
  ALL("All Categories", emptySet(), emptyList()),
  QURAN_TAFSIR(
    "Quran / Tafsir",
    setOf("tafseer_quran"),
    listOf("tafseer", "tafsir", "quran", "surah", "surat", "ayah", "ayat")
  ),
  HADITH(
    "Hadith",
    setOf("hadith_sciences"),
    listOf("hadith", "hadees", "bukhari", "muslim", "tirmidhi", "mishkat", "sunan", "isnad", "sahih")
  ),
  AQEEDAH(
    "Aqeedah",
    setOf("aqeedah_tawheed"),
    listOf("aqeedah", "tawheed", "tauheed", "shirk", "biddah", "bidat", "qabar", "barzakh", "iman", "imaan", "taqleed", "firqa")
  ),
  FIQH(
    "Fiqh",
    setOf("salah_azan", "tahara_wudu"),
    listOf("fiqh", "namaz", "salah", "salat", "azan", "wudu", "wuzu", "ghusl", "tayammum", "roza", "zakat", "hajj", "talaq", "nikah", "raf-ul-yadain", "rafa", "witr")
  ),
  SEERAH(
    "Seerah",
    setOf("seerah_prophets", "sahaba_unity"),
    listOf("seerah", "seerat", "prophet", "muhammad", "nabi", "rasool", "miraj", "meraj", "hijrat", "badar", "uhud", "ibrahim", "musa", "isa")
  ),
  ISLAMIC_HISTORY(
    "Islamic History",
    setOf("islamic_history", "karbala_hussain", "reality_of_yazeed", "ahl_al_bayt"),
    listOf("history", "karbala", "hussain", "husain", "yazeed", "yazid", "khilafat", "sahaba", "usman", "uthman", "umar", "abu bakr", "ali", "muawiya", "siffin", "jamal", "ahl al-bayt", "ahle bait")
  ),
  COMPARATIVE_RELIGION(
    "Comparative Religion",
    setOf("comparative_religion"),
    listOf("comparative", "christian", "jew", "bible", "torah", "hindu", "atheist", "science", "evolution", "non-muslim", "interfaith", "zakir")
  ),
  QA(
    "Q&A",
    setOf("qa_sessions"),
    listOf("q&a", "question", "sawal", "jawab", "session", "ilmi", "mulaqat", "interview", "podcast", "discussion")
  ),
  CONTEMPORARY_ISSUES(
    "Contemporary Issues",
    setOf("contemporary_issues", "ethical_reform"),
    listOf("contemporary", "modern", "bank", "interest", "riba", "crypto", "forex", "job", "politics", "rights", "women", "ethics", "akhlaq", "parents", "halal", "haram")
  ),
  SPECIAL_LECTURES(
    "Special Lectures",
    setOf("special_lectures", "dua_adhkar"),
    listOf("special", "mas'alah", "masalah", "dua", "adhkar", "azkar", "dajjal", "qayamat", "ramadan", "ashura", "eid", "khutbah")
  );

  val label: String
    get() = displayLabel

  fun matchesLecture(lecture: LectureRecord): Boolean {
    if (this == ALL) return true
    if (lecture.topics.any { it in mappedTopicIds }) return true
    val combined = "${lecture.displayTitle} ${lecture.summary.orEmpty()} ${lecture.seriesName.orEmpty()}".lowercase()
    return titleKeywords.any { kw -> combined.contains(kw) }
  }
}

enum class LectureDateFilter(val label: String) {
  ALL("All Dates"),
  NEWEST_2023_2026("2023–2026"),
  RECENT_2019_2022("2019–2022"),
  ARCHIVE_2010_2018("2010–2018");

  fun matchesYear(year: Int?): Boolean {
    val y = year ?: 2018
    return when (this) {
      ALL -> true
      NEWEST_2023_2026 -> y >= 2023
      RECENT_2019_2022 -> y in 2019..2022
      ARCHIVE_2010_2018 -> y <= 2018
    }
  }
}

enum class LectureDurationFilter(val label: String) {
  ALL("All Durations"),
  SHORT_UNDER_15M("< 15m"),
  MEDIUM_15_TO_60M("15–60m"),
  LONG_OVER_60M("> 60m");

  fun matchesDuration(durationSec: Int?): Boolean {
    val sec = durationSec ?: 900
    return when (this) {
      ALL -> true
      SHORT_UNDER_15M -> sec < 900
      MEDIUM_15_TO_60M -> sec in 900..3600
      LONG_OVER_60M -> sec > 3600
    }
  }
}

enum class LectureSortOption(val label: String) {
  RELEVANCE("Relevance"),
  NEWEST_FIRST("Newest First"),
  LONGEST_FIRST("Longest First")
}

enum class RecommendationShelfTab(val label: String) {
  RECOMMENDED("Recommended"),
  CONTINUE_WATCHING("Continue Watching"),
  RECENTLY_ADDED("Recently Added"),
  NEW_LECTURES("New Lectures"),
  RELATED_TOPICS("Related Topics"),
  SIMILAR_LECTURES("Similar Lectures"),
  UNWATCHED("Unwatched")
}

