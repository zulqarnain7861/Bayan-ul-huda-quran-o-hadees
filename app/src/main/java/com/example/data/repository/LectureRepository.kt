package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.JsonReader
import android.util.JsonToken
import com.example.data.local.KnowledgeBookmarkEntity
import com.example.data.local.KnowledgeDatabase
import com.example.data.local.LectureEntity
import com.example.data.local.ProgressEntity
import com.example.data.local.SourceLinkEntity
import com.example.data.local.TopicEntity
import com.example.data.model.CountryStatus
import com.example.data.model.DiscoverySeedConfig
import com.example.data.model.DiscoveryTopicDefinition
import com.example.data.model.LectureFormat
import com.example.data.model.LectureRecord
import com.example.data.model.LectureSeedCounts
import com.example.data.model.LectureSeedDataset
import com.example.data.model.PlaybackDataProfile
import com.example.data.model.RecommendedLectureItem
import com.example.data.model.RecordVerificationState
import com.example.data.model.ReferenceStatus
import com.example.data.model.SourceTier
import com.example.data.model.TranscriptStatus
import com.example.net.IndexSync
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Streaming JSON parser, Room seeder, and Watch-Time Based Topic Recommendation Engine
 * for Section A (Engineer Muhammad Ali Mirza Video Lecture Library since 2013).
 *
 * Strictly enforces:
 * - Streaming `JsonReader` parsing with `MAX_ASSET_BYTES = 5 MiB`.
 * - Local-only telemetry in `SharedPreferences` (`mirza_*` / `disc_*` keys, ≤ 96 KB total,
 *   recents and bookmarks capped at 200 entries, zero PII, zero network uploads).
 * - Dynamic watch-time recommendation ranking: whichever video title/topic receives more
 *   watch time from the user automatically boosts related videos to the top (e.g., watching
 *   a lecture on Imam Hussain A.S. boosts Karbala, Reality of Yazeed, and Ahl al-Bayt videos
 *   to the top; watching Salah/Azan or Tafseer boosts those related topics to the top).
 */
class LectureRepository(
  private val context: Context,
  private val database: KnowledgeDatabase = KnowledgeDatabase.getInstance(context)
) {

  companion object {
    const val MAX_ASSET_BYTES: Int = 5 * 1024 * 1024 // 5 MiB cap
    const val MAX_PREFS_BYTES: Int = 96 * 1024 // 96 KB cap per §9
    const val MAX_RECENTS_AND_BOOKMARKS: Int = 200
    const val ASSET_MIRZA_LECTURES = "mirza_lectures_v3.json"
    const val ASSET_DISCOVERY_SEED = "discovery_seed_v1.json"

    private const val PREFS_NAME = "mirza_discovery_prefs"
    private const val KEY_PLAYBACK_PROFILE = "disc_playback_profile"
    private const val KEY_WIFI_ONLY = "disc_wifi_only"
    private const val KEY_WATCH_BY_VIDEO_Prefix = "mirza_watch_vid_"
    private const val KEY_WATCH_BY_TOPIC_Prefix = "mirza_watch_topic_"
    private const val KEY_BROKEN_REPORTS_CSV = "mirza_broken_reports_csv"

    /**
     * Built-in Topic Association Graph so that watching a lecture on a topic (e.g.,
     * `karbala_hussain` / Imam Hussain A.S.) immediately boosts closely related topics
     * (`reality_of_yazeed`, `ahl_al_bayt`, `islamic_history`) and keywords (`karbala`,
     * `yazeed`, `hussain`, `ashura`, `kufa`) to the top.
     */
    val RELATED_TOPIC_WEIGHTS: Map<String, Map<String, Double>> = mapOf(
      "karbala_hussain" to mapOf(
        "karbala_hussain" to 1.0,
        "reality_of_yazeed" to 0.92,
        "ahl_al_bayt" to 0.85,
        "islamic_history" to 0.65,
        "sahaba_unity" to 0.55
      ),
      "reality_of_yazeed" to mapOf(
        "reality_of_yazeed" to 1.0,
        "karbala_hussain" to 0.92,
        "ahl_al_bayt" to 0.80,
        "islamic_history" to 0.70,
        "hadith_sciences" to 0.55
      ),
      "ahl_al_bayt" to mapOf(
        "ahl_al_bayt" to 1.0,
        "karbala_hussain" to 0.88,
        "reality_of_yazeed" to 0.78,
        "sahaba_unity" to 0.75,
        "hadith_sciences" to 0.55
      ),
      "salah_azan" to mapOf(
        "salah_azan" to 1.0,
        "tahara_wudu" to 0.85,
        "dua_adhkar" to 0.78,
        "hadith_sciences" to 0.60
      ),
      "tafseer_quran" to mapOf(
        "tafseer_quran" to 1.0,
        "aqeedah_tawheed" to 0.82,
        "seerah_prophets" to 0.75,
        "ethical_reform" to 0.65
      ),
      "aqeedah_tawheed" to mapOf(
        "aqeedah_tawheed" to 1.0,
        "tafseer_quran" to 0.80,
        "hadith_sciences" to 0.75,
        "sahaba_unity" to 0.65
      ),
      "hadith_sciences" to mapOf(
        "hadith_sciences" to 1.0,
        "salah_azan" to 0.72,
        "karbala_hussain" to 0.68,
        "reality_of_yazeed" to 0.68,
        "aqeedah_tawheed" to 0.70
      )
    )

    /**
     * Parses `mirza_lectures_v3.json` using streaming `JsonReader` with a 5 MiB bound
     * (with a pure-Kotlin recursive-descent fallback for offline JVM JUnit environments
     * where `android.util.JsonReader` is stubbed).
     */
    fun parseLectureSeedStream(input: InputStream): LectureSeedDataset {
      val boundedBytes = IndexSync.readBoundedBytes(input, MAX_ASSET_BYTES)
      try {
        JsonReader(InputStreamReader(ByteArrayInputStream(boundedBytes), Charsets.UTF_8)).use { reader ->
          if (reader.peek() != null) {
            var schemaVersion = 3
            var generatedUtc = ""
            var coverageDisclosure = "169 seed records · 159 unverified · Tafseer series runs to 344 — tap to load more"
            val officialChannels = mutableListOf<String>()
            val officialPortals = mutableListOf<String>()
            var counts = LectureSeedCounts(0, 0, 0, 0, 0, 1, 344)
            val records = mutableListOf<LectureRecord>()

            reader.beginObject()
            while (reader.hasNext()) {
              when (reader.nextName()) {
                "schemaVersion" -> schemaVersion = reader.nextInt()
                "generatedUtc" -> generatedUtc = reader.nextString()
                "coverageDisclosure" -> coverageDisclosure = reader.nextString()
                "officialChannels" -> officialChannels.addAll(readStringList(reader))
                "officialPortals" -> officialPortals.addAll(readStringList(reader))
                "counts" -> counts = readCounts(reader)
                "records" -> {
                  reader.beginArray()
                  while (reader.hasNext()) {
                    readLectureRecord(reader)?.let { records.add(it) }
                  }
                  reader.endArray()
                }
                else -> reader.skipValue()
              }
            }
            reader.endObject()

            if (records.isNotEmpty()) {
              return LectureSeedDataset(
                schemaVersion = schemaVersion,
                generatedUtc = generatedUtc,
                coverageDisclosure = coverageDisclosure,
                officialChannels = officialChannels,
                officialPortals = officialPortals,
                counts = counts,
                records = records
              )
            }
          }
        }
      } catch (_: Throwable) {
      }
      return parseLectureSeedPureKotlin(boundedBytes.toString(Charsets.UTF_8))
    }

    /**
     * Parses `discovery_seed_v1.json` using streaming `JsonReader` (with pure-Kotlin fallback for JVM JUnit).
     */
    fun parseDiscoverySeedStream(input: InputStream): DiscoverySeedConfig {
      val boundedBytes = IndexSync.readBoundedBytes(input, MAX_ASSET_BYTES)
      try {
        JsonReader(InputStreamReader(ByteArrayInputStream(boundedBytes), Charsets.UTF_8)).use { reader ->
          if (reader.peek() != null) {
            var schemaVersion = 1
            var defaultProfile = PlaybackDataProfile.BALANCED
            var wifiOnlyDefault = false
            var helperNote = "Picture quality is set inside the YouTube player's ⋮ menu."
            val qualityLadder = mutableMapOf<String, Int>()
            val bm25Weights = mutableMapOf<String, Double>()
            val topics = mutableListOf<DiscoveryTopicDefinition>()

            reader.beginObject()
            while (reader.hasNext()) {
              when (reader.nextName()) {
                "schemaVersion" -> schemaVersion = reader.nextInt()
                "defaultPlaybackProfile" -> defaultProfile = PlaybackDataProfile.fromRaw(reader.nextString())
                "wifiOnlyDefault" -> wifiOnlyDefault = reader.nextBoolean()
                "playerMenuHelperNote" -> helperNote = reader.nextString()
                "qualityLadderApproxKbps" -> {
                  reader.beginObject()
                  while (reader.hasNext()) {
                    qualityLadder[reader.nextName()] = reader.nextInt()
                  }
                  reader.endObject()
                }
                "bm25Weights" -> {
                  reader.beginObject()
                  while (reader.hasNext()) {
                    bm25Weights[reader.nextName()] = reader.nextDouble()
                  }
                  reader.endObject()
                }
                "topics" -> {
                  reader.beginArray()
                  while (reader.hasNext()) {
                    topics.add(readTopicDefinition(reader))
                  }
                  reader.endArray()
                }
                else -> reader.skipValue()
              }
            }
            reader.endObject()

            if (topics.isNotEmpty()) {
              return DiscoverySeedConfig(
                schemaVersion = schemaVersion,
                defaultPlaybackProfile = defaultProfile,
                wifiOnlyDefault = wifiOnlyDefault,
                playerMenuHelperNote = helperNote,
                qualityLadderApproxKbps = qualityLadder,
                bm25Weights = bm25Weights,
                topics = topics
              )
            }
          }
        }
      } catch (_: Throwable) {
      }
      return parseDiscoverySeedPureKotlin(boundedBytes.toString(Charsets.UTF_8))
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseLectureSeedPureKotlin(jsonText: String): LectureSeedDataset {
      val root = MiniJsonParser(jsonText).parseValue() as Map<String, Any?>
      val schemaVersion = (root["schemaVersion"] as? Number)?.toInt() ?: 3
      val generatedUtc = root["generatedUtc"] as? String ?: ""
      val coverageDisclosure = root["coverageDisclosure"] as? String
        ?: "169 seed records · 159 unverified · Tafseer series runs to 344 — tap to load more"
      val officialChannels = (root["officialChannels"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
      val officialPortals = (root["officialPortals"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
      val countsMap = root["counts"] as? Map<String, Any?> ?: emptyMap()
      val counts = LectureSeedCounts(
        total = (countsMap["total"] as? Number)?.toInt() ?: 0,
        confirmedLive = (countsMap["confirmedLive"] as? Number)?.toInt() ?: 0,
        unprobed = (countsMap["unprobed"] as? Number)?.toInt() ?: 0,
        clips = (countsMap["clips"] as? Number)?.toInt() ?: 0,
        fullLectures = (countsMap["fullLectures"] as? Number)?.toInt() ?: 0,
        tafseerMinLectureNumber = (countsMap["tafseerMinLectureNumber"] as? Number)?.toInt() ?: 1,
        tafseerMaxLectureNumber = (countsMap["tafseerMaxLectureNumber"] as? Number)?.toInt() ?: 344
      )
      val rawRecords = root["records"] as? List<*> ?: emptyList<Any?>()
      val records = rawRecords.mapNotNull { item ->
        val m = item as? Map<String, Any?> ?: return@mapNotNull null
        val yid = m["youtubeId"] as? String ?: ""
        val title = m["title"] as? String ?: ""
        if (!LectureRecord.isValidYoutubeId(yid) || title.isBlank()) return@mapNotNull null
        val handle = m["channelHandle"] as? String
        val cUrl = m["channelUrl"] as? String
        val tier = SourceTier.fromRaw(m["sourceTier"] as? String, handle, cUrl)
        LectureRecord(
          youtubeId = yid,
          title = title,
          verifiedTitle = m["verifiedTitle"] as? String,
          speaker = m["speaker"] as? String ?: "Engineer Muhammad Ali Mirza",
          channelHandle = handle,
          channelUrl = cUrl,
          sourceTier = tier,
          countryStatus = CountryStatus.fromRaw(m["countryStatus"] as? String),
          verificationState = RecordVerificationState.fromRaw(m["verificationState"] as? String),
          recordFormat = LectureFormat.fromRaw(m["recordFormat"] as? String),
          lectureNumber = (m["lectureNumber"] as? Number)?.toInt(),
          seriesName = m["seriesName"] as? String,
          recordedYear = (m["recordedYear"] as? Number)?.toInt(),
          durationSec = (m["durationSec"] as? Number)?.toInt(),
          topics = (m["topics"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
          relatedYoutubeIds = (m["relatedYoutubeIds"] as? List<*>)?.mapNotNull { it as? String }
            ?.filter { LectureRecord.isValidYoutubeId(it) } ?: emptyList(),
          summary = m["summary"] as? String,
          transcriptStatus = TranscriptStatus.fromRaw(m["transcriptStatus"] as? String),
          referenceStatus = ReferenceStatus.fromRaw(m["referenceStatus"] as? String),
          references = (m["references"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
          alternateYoutubeIds = (m["alternateYoutubeIds"] as? List<*>)?.mapNotNull { it as? String }
            ?.filter { LectureRecord.isValidYoutubeId(it) } ?: emptyList()
        )
      }
      return LectureSeedDataset(
        schemaVersion = schemaVersion,
        generatedUtc = generatedUtc,
        coverageDisclosure = coverageDisclosure,
        officialChannels = officialChannels,
        officialPortals = officialPortals,
        counts = counts,
        records = records
      )
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseDiscoverySeedPureKotlin(jsonText: String): DiscoverySeedConfig {
      val root = MiniJsonParser(jsonText).parseValue() as Map<String, Any?>
      val schemaVersion = (root["schemaVersion"] as? Number)?.toInt() ?: 1
      val defaultProfile = PlaybackDataProfile.fromRaw(root["defaultPlaybackProfile"] as? String)
      val wifiOnlyDefault = root["wifiOnlyDefault"] as? Boolean ?: false
      val helperNote = root["playerMenuHelperNote"] as? String
        ?: "Picture quality is set inside the YouTube player's ⋮ menu."
      val qMap = (root["qualityLadderApproxKbps"] as? Map<String, Any?>)
        ?.mapValues { (it.value as? Number)?.toInt() ?: 0 } ?: emptyMap()
      val bMap = (root["bm25Weights"] as? Map<String, Any?>)
        ?.mapValues { (it.value as? Number)?.toDouble() ?: 1.0 } ?: emptyMap()
      val rawTopics = root["topics"] as? List<*> ?: emptyList<Any?>()
      val topics = rawTopics.mapNotNull { item ->
        val m = item as? Map<String, Any?> ?: return@mapNotNull null
        DiscoveryTopicDefinition(
          id = m["id"] as? String ?: "",
          titleEn = m["titleEn"] as? String ?: "",
          titleUr = m["titleUr"] as? String ?: "",
          minimumRecordsToShow = (m["minimumRecordsToShow"] as? Number)?.toInt() ?: 1,
          relatedTopicIds = (m["relatedTopicIds"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
          keywords = (m["keywords"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
        )
      }
      return DiscoverySeedConfig(
        schemaVersion = schemaVersion,
        defaultPlaybackProfile = defaultProfile,
        wifiOnlyDefault = wifiOnlyDefault,
        playerMenuHelperNote = helperNote,
        qualityLadderApproxKbps = qMap,
        bm25Weights = bMap,
        topics = topics
      )
    }

    private class MiniJsonParser(private val s: String) {
      private var pos = 0

      fun parseValue(): Any? {
        skipWs()
        if (pos >= s.length) return null
        return when (val ch = s[pos]) {
          '{' -> parseObject()
          '[' -> parseArray()
          '"' -> parseString()
          't', 'f' -> parseBoolean()
          'n' -> {
            pos += 4
            null
          }
          else -> if (ch == '-' || ch.isDigit()) parseNumber() else null
        }
      }

      private fun parseObject(): Map<String, Any?> {
        val map = LinkedHashMap<String, Any?>()
        pos++ // '{'
        skipWs()
        if (pos < s.length && s[pos] == '}') {
          pos++
          return map
        }
        while (pos < s.length) {
          skipWs()
          val key = parseString()
          skipWs()
          if (pos < s.length && s[pos] == ':') pos++
          val value = parseValue()
          map[key] = value
          skipWs()
          if (pos < s.length && s[pos] == ',') {
            pos++
          } else if (pos < s.length && s[pos] == '}') {
            pos++
            break
          } else {
            break
          }
        }
        return map
      }

      private fun parseArray(): List<Any?> {
        val list = ArrayList<Any?>()
        pos++ // '['
        skipWs()
        if (pos < s.length && s[pos] == ']') {
          pos++
          return list
        }
        while (pos < s.length) {
          list.add(parseValue())
          skipWs()
          if (pos < s.length && s[pos] == ',') {
            pos++
          } else if (pos < s.length && s[pos] == ']') {
            pos++
            break
          } else {
            break
          }
        }
        return list
      }

      private fun parseString(): String {
        pos++ // '"'
        val sb = StringBuilder()
        while (pos < s.length) {
          val ch = s[pos++]
          if (ch == '"') break
          if (ch == '\\' && pos < s.length) {
            when (val esc = s[pos++]) {
              '"', '\\', '/' -> sb.append(esc)
              'b' -> sb.append('\b')
              'f' -> sb.append('\u000C')
              'n' -> sb.append('\n')
              'r' -> sb.append('\r')
              't' -> sb.append('\t')
              'u' -> {
                if (pos + 4 <= s.length) {
                  val hex = s.substring(pos, pos + 4)
                  sb.append(hex.toInt(16).toChar())
                  pos += 4
                }
              }
              else -> sb.append(esc)
            }
          } else {
            sb.append(ch)
          }
        }
        return sb.toString()
      }

      private fun parseBoolean(): Boolean {
        return if (s.startsWith("true", pos)) {
          pos += 4
          true
        } else {
          pos += 5
          false
        }
      }

      private fun parseNumber(): Number {
        val start = pos
        while (pos < s.length && (s[pos].isDigit() || s[pos] in "-+.eE")) {
          pos++
        }
        val raw = s.substring(start, pos)
        return if ('.' in raw || 'e' in raw || 'E' in raw) {
          raw.toDoubleOrNull() ?: 0.0
        } else {
          raw.toLongOrNull() ?: 0L
        }
      }

      private fun skipWs() {
        while (pos < s.length && s[pos].isWhitespace()) {
          pos++
        }
      }
    }

    private fun readCounts(reader: JsonReader): LectureSeedCounts {
      var total = 0
      var confirmedLive = 0
      var unprobed = 0
      var clips = 0
      var fullLectures = 0
      var minTafseer = 1
      var maxTafseer = 344
      reader.beginObject()
      while (reader.hasNext()) {
        when (reader.nextName()) {
          "total" -> total = reader.nextInt()
          "confirmedLive" -> confirmedLive = reader.nextInt()
          "unprobed" -> unprobed = reader.nextInt()
          "clips" -> clips = reader.nextInt()
          "fullLectures" -> fullLectures = reader.nextInt()
          "tafseerMinLectureNumber" -> minTafseer = reader.nextInt()
          "tafseerMaxLectureNumber" -> maxTafseer = reader.nextInt()
          else -> reader.skipValue()
        }
      }
      reader.endObject()
      return LectureSeedCounts(total, confirmedLive, unprobed, clips, fullLectures, minTafseer, maxTafseer)
    }

    private fun readLectureRecord(reader: JsonReader): LectureRecord? {
      var youtubeId = ""
      var title = ""
      var verifiedTitle: String? = null
      var speaker = "Engineer Muhammad Ali Mirza"
      var channelHandle: String? = null
      var channelUrl: String? = null
      var rawSourceTier: String? = null
      var rawCountryStatus: String? = null
      var rawVerificationState: String? = null
      var rawFormat: String? = null
      var lectureNumber: Int? = null
      var seriesName: String? = null
      var recordedYear: Int? = null
      var durationSec: Int? = null
      val topics = mutableListOf<String>()
      val relatedIds = mutableListOf<String>()
      var summary: String? = null
      var rawTranscriptStatus: String? = null
      var rawReferenceStatus: String? = null
      val references = mutableListOf<String>()
      val alternateIds = mutableListOf<String>()

      reader.beginObject()
      while (reader.hasNext()) {
        when (reader.nextName()) {
          "youtubeId" -> youtubeId = nextNullableString(reader) ?: ""
          "title" -> title = nextNullableString(reader) ?: ""
          "verifiedTitle" -> verifiedTitle = nextNullableString(reader)
          "speaker" -> speaker = nextNullableString(reader) ?: "Engineer Muhammad Ali Mirza"
          "channelHandle" -> channelHandle = nextNullableString(reader)
          "channelUrl" -> channelUrl = nextNullableString(reader)
          "sourceTier" -> rawSourceTier = nextNullableString(reader)
          "countryStatus" -> rawCountryStatus = nextNullableString(reader)
          "verificationState" -> rawVerificationState = nextNullableString(reader)
          "recordFormat" -> rawFormat = nextNullableString(reader)
          "lectureNumber" -> lectureNumber = nextNullableInt(reader)
          "seriesName" -> seriesName = nextNullableString(reader)
          "recordedYear" -> recordedYear = nextNullableInt(reader)
          "durationSec" -> durationSec = nextNullableInt(reader)
          "topics" -> topics.addAll(readStringList(reader))
          "relatedYoutubeIds" -> relatedIds.addAll(readStringList(reader))
          "summary" -> summary = nextNullableString(reader)
          "transcriptStatus" -> rawTranscriptStatus = nextNullableString(reader)
          "referenceStatus" -> rawReferenceStatus = nextNullableString(reader)
          "references" -> references.addAll(readStringList(reader))
          "alternateYoutubeIds" -> alternateIds.addAll(readStringList(reader))
          else -> reader.skipValue()
        }
      }
      reader.endObject()

      if (!LectureRecord.isValidYoutubeId(youtubeId) || title.isBlank()) {
        return null
      }

      val resolvedTier = SourceTier.fromRaw(rawSourceTier, channelHandle, channelUrl)
      return LectureRecord(
        youtubeId = youtubeId,
        title = title,
        verifiedTitle = verifiedTitle,
        speaker = speaker,
        channelHandle = channelHandle,
        channelUrl = channelUrl,
        sourceTier = resolvedTier,
        countryStatus = CountryStatus.fromRaw(rawCountryStatus),
        verificationState = RecordVerificationState.fromRaw(rawVerificationState),
        recordFormat = LectureFormat.fromRaw(rawFormat),
        lectureNumber = lectureNumber,
        seriesName = seriesName,
        recordedYear = recordedYear,
        durationSec = durationSec,
        topics = topics,
        relatedYoutubeIds = relatedIds.filter { LectureRecord.isValidYoutubeId(it) },
        summary = summary,
        transcriptStatus = TranscriptStatus.fromRaw(rawTranscriptStatus),
        referenceStatus = ReferenceStatus.fromRaw(rawReferenceStatus),
        references = references,
        alternateYoutubeIds = alternateIds.filter { LectureRecord.isValidYoutubeId(it) }
      )
    }

    private fun readTopicDefinition(reader: JsonReader): DiscoveryTopicDefinition {
      var id = ""
      var titleEn = ""
      var titleUr = ""
      var minRecords = 1
      val related = mutableListOf<String>()
      val keywords = mutableListOf<String>()
      reader.beginObject()
      while (reader.hasNext()) {
        when (reader.nextName()) {
          "id" -> id = reader.nextString()
          "titleEn" -> titleEn = reader.nextString()
          "titleUr" -> titleUr = reader.nextString()
          "minimumRecordsToShow" -> minRecords = reader.nextInt()
          "relatedTopicIds" -> related.addAll(readStringList(reader))
          "keywords" -> keywords.addAll(readStringList(reader))
          else -> reader.skipValue()
        }
      }
      reader.endObject()
      return DiscoveryTopicDefinition(id, titleEn, titleUr, minRecords, related, keywords)
    }

    private fun readStringList(reader: JsonReader): List<String> {
      if (reader.peek() == JsonToken.NULL) {
        reader.nextNull()
        return emptyList()
      }
      val list = mutableListOf<String>()
      reader.beginArray()
      while (reader.hasNext()) {
        nextNullableString(reader)?.let { if (it.isNotBlank()) list.add(it) }
      }
      reader.endArray()
      return list
    }

    private fun nextNullableString(reader: JsonReader): String? {
      return if (reader.peek() == JsonToken.NULL) {
        reader.nextNull()
        null
      } else {
        reader.nextString()
      }
    }

    private fun nextNullableInt(reader: JsonReader): Int? {
      return if (reader.peek() == JsonToken.NULL) {
        reader.nextNull()
        null
      } else {
        reader.nextInt()
      }
    }

    /**
     * Pure, deterministic Watch-Time Topic Recommendation ranking function.
     *
     * Given:
     * - `allLectures`: all loaded Engineer Muhammad Ali Mirza lectures
     * - `watchTimeByYoutubeId`: map of `youtubeId -> secondsWatched`
     * - `topicDefinitions`: topic registry from `discovery_seed_v1.json`
     * - `isDebugBuild`: whether `UNKNOWN_PENDING` records are visible
     *
     * Produces a ranked list of `RecommendedLectureItem` where lectures related to the user's
     * most-watched topics and title keywords appear at the top.
     * For example, if a user watched a lecture on **Imam Hussain A.S.** (`karbala_hussain`),
     * lectures about **Karbala**, **Reality of Yazeed**, and **Ahl al-Bayt** immediately rank
     * at the top. If a user watched more time on **Salah & Azan** or **Tafseer**, those related
     * lectures rank at the top.
     */
    fun computeWatchTimeRecommendations(
      allLectures: List<LectureRecord>,
      watchTimeByYoutubeId: Map<String, Int>,
      topicDefinitions: List<DiscoveryTopicDefinition>,
      isDebugBuild: Boolean,
      limit: Int = 18
    ): List<RecommendedLectureItem> {
      val visibleLectures = allLectures.filter { it.shouldShowInUi(isDebugBuild) }
      if (visibleLectures.isEmpty()) return emptyList()

      val topicTitleById = topicDefinitions.associate { it.id to it.titleEn }

      // Aggregate watch time per topic and extract high-interest keywords from watched video titles
      val directTopicWatchSeconds = mutableMapOf<String, Int>()
      val expandedTopicScore = mutableMapOf<String, Double>()
      val watchedKeywords = mutableMapOf<String, Int>()
      val lectureById = visibleLectures.associateBy { it.youtubeId }

      for ((watchedId, seconds) in watchTimeByYoutubeId) {
        if (seconds <= 0) continue
        val watchedLecture = lectureById[watchedId] ?: allLectures.firstOrNull { it.youtubeId == watchedId } ?: continue
        for (topicId in watchedLecture.topics) {
          directTopicWatchSeconds[topicId] = (directTopicWatchSeconds[topicId] ?: 0) + seconds
          val relatedMap = RELATED_TOPIC_WEIGHTS[topicId]
          if (relatedMap != null) {
            for ((relTopic, weight) in relatedMap) {
              expandedTopicScore[relTopic] = (expandedTopicScore[relTopic] ?: 0.0) + (seconds * weight)
            }
          } else {
            expandedTopicScore[topicId] = (expandedTopicScore[topicId] ?: 0.0) + seconds.toDouble()
          }
          // Also include relatedTopicIds from discovery_seed_v1.json
          val def = topicDefinitions.firstOrNull { it.id == topicId }
          def?.relatedTopicIds?.forEach { relId ->
            expandedTopicScore[relId] = (expandedTopicScore[relId] ?: 0.0) + (seconds * 0.8)
          }
        }

        // Extract meaningful title keywords (e.g. hussain -> karbala, yazeed, ahle bait)
        val lowerTitle = watchedLecture.displayTitle.lowercase(Locale.ROOT)
        if ("hussain" in lowerTitle || "husayn" in lowerTitle || "karbala" in lowerTitle || "ashura" in lowerTitle) {
          watchedKeywords["karbala"] = (watchedKeywords["karbala"] ?: 0) + seconds
          watchedKeywords["yazeed"] = (watchedKeywords["yazeed"] ?: 0) + seconds
          watchedKeywords["hussain"] = (watchedKeywords["hussain"] ?: 0) + seconds
          watchedKeywords["ahl al-bayt"] = (watchedKeywords["ahl al-bayt"] ?: 0) + seconds
          expandedTopicScore["karbala_hussain"] = (expandedTopicScore["karbala_hussain"] ?: 0.0) + (seconds * 1.2)
          expandedTopicScore["reality_of_yazeed"] = (expandedTopicScore["reality_of_yazeed"] ?: 0.0) + (seconds * 1.1)
          expandedTopicScore["ahl_al_bayt"] = (expandedTopicScore["ahl_al_bayt"] ?: 0.0) + (seconds * 0.95)
        }
        if ("yazeed" in lowerTitle || "yazid" in lowerTitle || "harrah" in lowerTitle) {
          watchedKeywords["yazeed"] = (watchedKeywords["yazeed"] ?: 0) + seconds
          watchedKeywords["karbala"] = (watchedKeywords["karbala"] ?: 0) + seconds
          watchedKeywords["hussain"] = (watchedKeywords["hussain"] ?: 0) + seconds
          expandedTopicScore["reality_of_yazeed"] = (expandedTopicScore["reality_of_yazeed"] ?: 0.0) + (seconds * 1.2)
          expandedTopicScore["karbala_hussain"] = (expandedTopicScore["karbala_hussain"] ?: 0.0) + (seconds * 1.1)
        }
        if ("azan" in lowerTitle || "salah" in lowerTitle || "namaz" in lowerTitle || "raf-ul-yadain" in lowerTitle) {
          watchedKeywords["salah"] = (watchedKeywords["salah"] ?: 0) + seconds
          watchedKeywords["azan"] = (watchedKeywords["azan"] ?: 0) + seconds
          watchedKeywords["wudu"] = (watchedKeywords["wudu"] ?: 0) + seconds
        }
        if ("tafseer" in lowerTitle || "surah" in lowerTitle || "quran" in lowerTitle) {
          watchedKeywords["tafseer"] = (watchedKeywords["tafseer"] ?: 0) + seconds
          watchedKeywords["surah"] = (watchedKeywords["surah"] ?: 0) + seconds
        }
      }

      val topWatchedTopicEntry = directTopicWatchSeconds.maxByOrNull { it.value }
      val hasWatchHistory = expandedTopicScore.isNotEmpty()

      return visibleLectures
        .map { candidate ->
          var score = 0.0
          val matchedTopics = mutableListOf<String>()
          var topicWatchSec = 0

          for (topicId in candidate.topics) {
            val tScore = expandedTopicScore[topicId] ?: 0.0
            if (tScore > 0.0) {
              score += tScore * 4.5
              matchedTopics.add(topicId)
              topicWatchSec += directTopicWatchSeconds[topicId] ?: 0
            }
          }

          val lowerCandidateTitle = candidate.displayTitle.lowercase(Locale.ROOT)
          for ((kw, kwSec) in watchedKeywords) {
            if (lowerCandidateTitle.contains(kw)) {
              score += kwSec * 2.5
            }
          }

          // Explicit cross-link boost if the candidate is in relatedYoutubeIds of a watched video
          for ((watchedId, sec) in watchTimeByYoutubeId) {
            if (sec > 0 && watchedId != candidate.youtubeId) {
              val watched = lectureById[watchedId]
              if (watched != null && candidate.youtubeId in watched.relatedYoutubeIds) {
                score += sec * 3.0
              }
            }
          }

          // Slightly deprioritize a video the user already watched extensively so fresh related videos surface at #1
          val alreadyWatchedSec = watchTimeByYoutubeId[candidate.youtubeId] ?: 0
          if (alreadyWatchedSec > 0 && hasWatchHistory) {
            score *= 0.35
          }

          // Base quality & regional playability signals (FAN_CHANNEL on top for India & global playability)
          if (candidate.verificationState == RecordVerificationState.CONFIRMED_LIVE) {
            score += 45.0
          }
          if (candidate.sourceTier == SourceTier.FAN_CHANNEL) {
            score += 32.0
          } else if (candidate.sourceTier == SourceTier.OFFICIAL) {
            score += 20.0
          }

          val reason = if (hasWatchHistory && matchedTopics.isNotEmpty()) {
            val primaryTopicName = topicTitleById[matchedTopics.first()] ?: matchedTopics.first()
            "Recommended · $primaryTopicName"
          } else {
            candidate.seriesName ?: candidate.channelDisplayName
          }

          RecommendedLectureItem(
            lecture = candidate,
            score = score,
            reasonLabel = reason,
            matchedTopicIds = matchedTopics,
            userWatchedSecondsOnTopic = topicWatchSec
          )
        }
        .sortedWith(
          compareByDescending<RecommendedLectureItem> { it.score }
            .thenBy { if (it.lecture.sourceTier == SourceTier.FAN_CHANNEL) 0 else 1 }
            .thenBy { if (it.lecture.verificationState == RecordVerificationState.CONFIRMED_LIVE) 0 else 1 }
            .thenByDescending { it.lecture.recordedYear ?: 2013 }
        )
        .take(limit)
    }
  }

  private val prefs: SharedPreferences by lazy {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
  }

  @Volatile
  private var cachedDataset: LectureSeedDataset? = null

  @Volatile
  private var cachedDiscoveryConfig: DiscoverySeedConfig? = null

  suspend fun loadSeedDataset(): LectureSeedDataset = withContext(Dispatchers.IO) {
    cachedDataset?.let { return@withContext it }
    val syncedFile = java.io.File(context.filesDir, IndexSync.ACTIVE_SYNCED_INDEX_FILENAME)
    val dataset = if (syncedFile.exists() && syncedFile.length() in 1..MAX_ASSET_BYTES.toLong()) {
      try {
        syncedFile.inputStream().use { parseLectureSeedStream(it) }
      } catch (_: Exception) {
        context.assets.open(ASSET_MIRZA_LECTURES).use { parseLectureSeedStream(it) }
      }
    } else {
      context.assets.open(ASSET_MIRZA_LECTURES).use { parseLectureSeedStream(it) }
    }
    cachedDataset = dataset
    dataset
  }

  suspend fun loadDiscoveryConfig(): DiscoverySeedConfig = withContext(Dispatchers.IO) {
    cachedDiscoveryConfig?.let { return@withContext it }
    val config = context.assets.open(ASSET_DISCOVERY_SEED).use { parseDiscoverySeedStream(it) }
    cachedDiscoveryConfig = config
    config
  }

  suspend fun ensureDatabaseSeeded(): LectureSeedDataset = withContext(Dispatchers.IO) {
    val dataset = loadSeedDataset()
    val config = loadDiscoveryConfig()
    val seedSig = "${dataset.records.size}_${dataset.records.take(10).joinToString(",") { it.youtubeId }}"
    val existingCount = database.lectureDao().getLectureCount()
    val lastSig = prefs.getString("disc_seed_sig", "")
    if (existingCount < dataset.records.size || lastSig != seedSig) {
      val entities = dataset.records.mapIndexed { index, record ->
        record.toEntity(rowid = index + 1)
      }
      val topicEntities = config.topics.map { topic ->
        TopicEntity(
          id = topic.id,
          titleEn = topic.titleEn,
          titleUr = topic.titleUr,
          minimumRecordsToShow = topic.minimumRecordsToShow,
          relatedTopicIdsCsv = topic.relatedTopicIds.joinToString(","),
          keywordsCsv = topic.keywords.joinToString(",")
        )
      }
      val links = dataset.records.mapNotNull { record ->
        val url = record.channelUrl ?: "https://www.youtube.com/watch?v=${record.youtubeId}"
        SourceLinkEntity(
          ownerType = "LECTURE",
          ownerId = record.youtubeId,
          url = url,
          tier = record.sourceTier.name,
          countryStatus = record.countryStatus.name,
          checkedOn = dataset.generatedUtc
        )
      }
      database.lectureDao().upsertTopics(topicEntities)
      database.lectureDao().upsertLectures(entities)
      database.lectureDao().upsertSourceLinks(links)
      database.lectureDao().rebuildLectureFtsIndex()
      prefs.edit().putString("disc_seed_sig", seedSig).apply()
    }
    dataset
  }

  /**
   * Records local watch time (in seconds) for a lecture and updates Room progress + bounded
   * `mirza_*` preferences so related topics immediately rise to the top of recommendations.
   */
  suspend fun recordWatchTime(
    youtubeId: String,
    additionalWatchedSec: Int,
    lastPositionSec: Int = 0
  ) = withContext(Dispatchers.IO) {
    if (!LectureRecord.isValidYoutubeId(youtubeId) || additionalWatchedSec <= 0) return@withContext
    val dataset = loadSeedDataset()
    val lecture = dataset.records.firstOrNull { it.youtubeId == youtubeId }

    val vidKey = "$KEY_WATCH_BY_VIDEO_Prefix$youtubeId"
    val previousSec = prefs.getInt(vidKey, 0)
    val updatedSec = (previousSec + additionalWatchedSec).coerceAtMost(86_400 * 10)

    val editor = prefs.edit()
    editor.putInt(vidKey, updatedSec)
    lecture?.topics?.forEach { topicId ->
      val tKey = "$KEY_WATCH_BY_TOPIC_Prefix$topicId"
      val prevTopicSec = prefs.getInt(tKey, 0)
      editor.putInt(tKey, (prevTopicSec + additionalWatchedSec).coerceAtMost(86_400 * 30))
    }
    enforcePreferencesSizeCap(editor)
    editor.apply()

    database.progressDao().upsertProgress(
      ProgressEntity(
        key = "LECTURE:$youtubeId",
        itemType = "LECTURE",
        itemId = youtubeId,
        positionSec = lastPositionSec.coerceAtLeast(0),
        totalWatchedSec = updatedSec,
        updatedAtEpochMs = System.currentTimeMillis()
      )
    )
    database.progressDao().pruneProgressBeyond200()
  }

  fun getWatchTimeMap(): Map<String, Int> {
    val result = mutableMapOf<String, Int>()
    for ((k, v) in prefs.all) {
      if (k.startsWith(KEY_WATCH_BY_VIDEO_Prefix) && v is Int && v > 0) {
        val yid = k.removePrefix(KEY_WATCH_BY_VIDEO_Prefix)
        result[yid] = v
      }
    }
    return result
  }

  fun getTopicWatchTimeMap(): Map<String, Int> {
    val result = mutableMapOf<String, Int>()
    for ((k, v) in prefs.all) {
      if (k.startsWith(KEY_WATCH_BY_TOPIC_Prefix) && v is Int && v > 0) {
        val topicId = k.removePrefix(KEY_WATCH_BY_TOPIC_Prefix)
        result[topicId] = v
      }
    }
    return result
  }

  fun getPlaybackDataProfile(): PlaybackDataProfile {
    return PlaybackDataProfile.fromRaw(prefs.getString(KEY_PLAYBACK_PROFILE, PlaybackDataProfile.BALANCED.name))
  }

  fun setPlaybackDataProfile(profile: PlaybackDataProfile) {
    prefs.edit().putString(KEY_PLAYBACK_PROFILE, profile.name).apply()
  }

  fun isWifiOnlyEnabled(): Boolean {
    return prefs.getBoolean(KEY_WIFI_ONLY, false)
  }

  fun setWifiOnlyEnabled(enabled: Boolean) {
    prefs.edit().putBoolean(KEY_WIFI_ONLY, enabled).apply()
  }

  suspend fun markLectureBrokenLocally(youtubeId: String) = withContext(Dispatchers.IO) {
    if (!LectureRecord.isValidYoutubeId(youtubeId)) return@withContext
    database.lectureDao().updateVerificationState(
      youtubeId = youtubeId,
      newState = RecordVerificationState.BROKEN.name,
      countryStatus = CountryStatus.REMOVED_OR_PRIVATE.name
    )
    val existing = prefs.getString(KEY_BROKEN_REPORTS_CSV, "")
      ?.split(",")
      ?.filter { it.isNotBlank() }
      ?.toMutableSet() ?: mutableSetOf()
    existing.add(youtubeId)
    val capped = existing.toList().takeLast(MAX_RECENTS_AND_BOOKMARKS).joinToString(",")
    prefs.edit().putString(KEY_BROKEN_REPORTS_CSV, capped).apply()
  }

  suspend fun toggleLectureBookmark(lecture: LectureRecord): Boolean = withContext(Dispatchers.IO) {
    val key = "LECTURE:${lecture.youtubeId}"
    val existing = database.progressDao().getBookmarks(MAX_RECENTS_AND_BOOKMARKS)
    val alreadyBookmarked = existing.any { it.key == key }
    if (alreadyBookmarked) {
      database.progressDao().deleteBookmark(key)
      false
    } else {
      database.progressDao().upsertBookmark(
        KnowledgeBookmarkEntity(
          key = key,
          itemType = "LECTURE",
          itemId = lecture.youtubeId,
          title = lecture.displayTitle,
          subtitle = "${lecture.speaker} · ${lecture.sourceTier.badgeLabel}",
          createdAtEpochMs = System.currentTimeMillis()
        )
      )
      database.progressDao().pruneBookmarksBeyond200()
      true
    }
  }

  private fun enforcePreferencesSizeCap(editor: SharedPreferences.Editor) {
    val allEntries = prefs.all
    var estimatedBytes = 0
    for ((k, v) in allEntries) {
      estimatedBytes += (k.length * 2) + (v?.toString()?.length ?: 4) * 2
    }
    if (estimatedBytes > MAX_PREFS_BYTES) {
      // Prune lowest watch-time video keys first while keeping top watch signals intact
      val videoEntries = allEntries.entries
        .filter { it.key.startsWith(KEY_WATCH_BY_VIDEO_Prefix) }
        .sortedBy { (it.value as? Int) ?: 0 }
      val toRemove = (videoEntries.size / 2).coerceAtLeast(1)
      for (entry in videoEntries.take(toRemove)) {
        editor.remove(entry.key)
      }
    }
  }

  private fun LectureRecord.toEntity(rowid: Int): LectureEntity {
    return LectureEntity(
      rowid = rowid,
      youtubeId = youtubeId,
      title = title,
      verifiedTitle = verifiedTitle,
      speaker = speaker,
      channelHandle = channelHandle,
      channelUrl = channelUrl,
      sourceTier = sourceTier.name,
      countryStatus = countryStatus.name,
      verificationState = verificationState.name,
      recordFormat = recordFormat.name,
      lectureNumber = lectureNumber,
      seriesName = seriesName,
      recordedYear = recordedYear,
      durationSec = durationSec,
      topicsCsv = topics.joinToString(","),
      relatedYoutubeIdsCsv = relatedYoutubeIds.joinToString(","),
      summary = summary,
      transcriptStatus = transcriptStatus.name,
      referenceStatus = referenceStatus.name,
      referencesCsv = references.joinToString(" | "),
      alternateYoutubeIdsCsv = alternateYoutubeIds.joinToString(",")
    )
  }
}

fun LectureEntity.toDomainModel(): LectureRecord {
  return LectureRecord(
    youtubeId = youtubeId,
    title = title,
    verifiedTitle = verifiedTitle,
    speaker = speaker,
    channelHandle = channelHandle,
    channelUrl = channelUrl,
    sourceTier = SourceTier.fromRaw(sourceTier, channelHandle, channelUrl),
    countryStatus = CountryStatus.fromRaw(countryStatus),
    verificationState = RecordVerificationState.fromRaw(verificationState),
    recordFormat = LectureFormat.fromRaw(recordFormat),
    lectureNumber = lectureNumber,
    seriesName = seriesName,
    recordedYear = recordedYear,
    durationSec = durationSec,
    topics = topicsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() },
    relatedYoutubeIds = relatedYoutubeIdsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() },
    summary = summary,
    transcriptStatus = TranscriptStatus.fromRaw(transcriptStatus),
    referenceStatus = ReferenceStatus.fromRaw(referenceStatus),
    references = referencesCsv.split("|").map { it.trim() }.filter { it.isNotEmpty() },
    alternateYoutubeIds = alternateYoutubeIdsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
  )
}
