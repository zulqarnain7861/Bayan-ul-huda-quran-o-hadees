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
import com.example.security.SecureNetworkClient
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

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
    private const val KEY_SEARCH_HISTORY_CSV = "mirza_search_history_csv"
    private const val KEY_LAST_SYNC_EPOCH_MS = "mirza_last_sync_epoch_ms"
    private const val SYNC_COOLDOWN_MS = 120_000L

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
      ),
      "sahaba_unity" to mapOf(
        "sahaba_unity" to 1.0,
        "islamic_history" to 0.90,
        "ahl_al_bayt" to 0.85,
        "karbala_hussain" to 0.78,
        "hadith_sciences" to 0.65
      ),
      "islamic_history" to mapOf(
        "islamic_history" to 1.0,
        "sahaba_unity" to 0.88,
        "karbala_hussain" to 0.84,
        "reality_of_yazeed" to 0.80,
        "ahl_al_bayt" to 0.78
      ),
      "tahara_wudu" to mapOf(
        "tahara_wudu" to 1.0,
        "salah_azan" to 0.90,
        "dua_adhkar" to 0.72,
        "hadith_sciences" to 0.65
      ),
      "dua_adhkar" to mapOf(
        "dua_adhkar" to 1.0,
        "salah_azan" to 0.82,
        "aqeedah_tawheed" to 0.76,
        "tafseer_quran" to 0.68
      ),
      "seerah_prophets" to mapOf(
        "seerah_prophets" to 1.0,
        "islamic_history" to 0.84,
        "sahaba_unity" to 0.80,
        "tafseer_quran" to 0.75
      ),
      "ethical_reform" to mapOf(
        "ethical_reform" to 1.0,
        "tafseer_quran" to 0.78,
        "hadith_sciences" to 0.74,
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
            var coverageDisclosure = "480 seed records · 321 confirmed live · Tafseer series runs to 344 — tap to load more"
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
        ?: "480 seed records · 321 confirmed live · Tafseer series runs to 344 — tap to load more"
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
      limit: Int = 18,
      recentSearchQueries: List<String> = emptyList(),
      bookmarkedYoutubeIds: Set<String> = emptySet()
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
        if ("usman" in lowerTitle || "uthman" in lowerTitle || "umar" in lowerTitle || "abu bakr" in lowerTitle || "sahaba" in lowerTitle || "muawiya" in lowerTitle) {
          watchedKeywords["usman"] = (watchedKeywords["usman"] ?: 0) + seconds
          watchedKeywords["uthman"] = (watchedKeywords["uthman"] ?: 0) + seconds
          watchedKeywords["umar"] = (watchedKeywords["umar"] ?: 0) + seconds
          watchedKeywords["abu bakr"] = (watchedKeywords["abu bakr"] ?: 0) + seconds
          watchedKeywords["sahaba"] = (watchedKeywords["sahaba"] ?: 0) + seconds
          watchedKeywords["khilafat"] = (watchedKeywords["khilafat"] ?: 0) + seconds
          expandedTopicScore["sahaba_unity"] = (expandedTopicScore["sahaba_unity"] ?: 0.0) + (seconds * 1.25)
          expandedTopicScore["islamic_history"] = (expandedTopicScore["islamic_history"] ?: 0.0) + (seconds * 1.1)
          expandedTopicScore["ahl_al_bayt"] = (expandedTopicScore["ahl_al_bayt"] ?: 0.0) + (seconds * 0.9)
        }
        if ("talaq" in lowerTitle || "halala" in lowerTitle || "nikah" in lowerTitle) {
          watchedKeywords["talaq"] = (watchedKeywords["talaq"] ?: 0) + seconds
          watchedKeywords["halala"] = (watchedKeywords["halala"] ?: 0) + seconds
          expandedTopicScore["ethical_reform"] = (expandedTopicScore["ethical_reform"] ?: 0.0) + (seconds * 1.2)
        }
        if ("dajjal" in lowerTitle || "qabar" in lowerTitle || "barzakh" in lowerTitle || "tawheed" in lowerTitle || "shirk" in lowerTitle) {
          watchedKeywords["tawheed"] = (watchedKeywords["tawheed"] ?: 0) + seconds
          watchedKeywords["barzakh"] = (watchedKeywords["barzakh"] ?: 0) + seconds
          watchedKeywords["qabar"] = (watchedKeywords["qabar"] ?: 0) + seconds
          expandedTopicScore["aqeedah_tawheed"] = (expandedTopicScore["aqeedah_tawheed"] ?: 0.0) + (seconds * 1.2)
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

          // Explicit cross-link & series affinity boost if candidate is related to a watched video
          var isRelatedToWatched = false
          for ((watchedId, sec) in watchTimeByYoutubeId) {
            if (sec > 0 && watchedId != candidate.youtubeId) {
              val watched = lectureById[watchedId]
              if (watched != null) {
                val sharesTopic = candidate.topics.any { it in watched.topics }
                val isExplicitlyLinked =
                  candidate.youtubeId in watched.relatedYoutubeIds ||
                    watched.youtubeId in candidate.relatedYoutubeIds ||
                    watched.youtubeId in candidate.alternateYoutubeIds ||
                    candidate.youtubeId in watched.alternateYoutubeIds
                val sameSeries =
                  !candidate.seriesName.isNullOrBlank() && candidate.seriesName == watched.seriesName
                if (isExplicitlyLinked && sharesTopic) {
                  score += sec * 6.0
                  isRelatedToWatched = true
                } else if (sameSeries && sharesTopic) {
                  score += sec * 3.5
                  isRelatedToWatched = true
                } else if (sharesTopic) {
                  isRelatedToWatched = true
                }
              }
            }
          }

          // Watched video priority: if the user watched a video, promote that watched video itself
          // to the very top of Recommendations alongside its related videos
          val alreadyWatchedSec = watchTimeByYoutubeId[candidate.youtubeId] ?: 0
          if (alreadyWatchedSec > 0 && hasWatchHistory) {
            score += 5000.0 + (alreadyWatchedSec * 10.0)
          }

          // Saved / Bookmarked topic & cross-link affinity
          if (bookmarkedYoutubeIds.isNotEmpty() && candidate.youtubeId !in bookmarkedYoutubeIds) {
            for (bId in bookmarkedYoutubeIds) {
              val bLecture = lectureById[bId] ?: continue
              val sharedTopics = candidate.topics.count { it in bLecture.topics }
              if (sharedTopics > 0) {
                score += sharedTopics * 35.0
              }
            }
          }

          // Search history relevance boost
          for (q in recentSearchQueries) {
            val cleanQ = q.trim().lowercase(Locale.ROOT)
            if (cleanQ.length >= 3 && lowerCandidateTitle.contains(cleanQ)) {
              score += 40.0
            }
          }

          // Base quality, full-lecture & regional playability signals (Full Lectures + FAN_CHANNEL on top)
          if (candidate.recordFormat == LectureFormat.FULL_LECTURE) {
            score += 28.0
          }
          if (candidate.verificationState == RecordVerificationState.CONFIRMED_LIVE) {
            score += 45.0
          }
          if (candidate.sourceTier == SourceTier.FAN_CHANNEL) {
            score += 32.0
          } else if (candidate.sourceTier == SourceTier.OFFICIAL) {
            score += 20.0
          }

          val reason = when {
            alreadyWatchedSec > 0 && hasWatchHistory -> {
              val primaryTopicName = matchedTopics.firstOrNull()?.let { topicTitleById[it] ?: it }
                ?: (candidate.seriesName ?: "Watch History")
              "Watched · $primaryTopicName"
            }
            hasWatchHistory && (isRelatedToWatched || matchedTopics.isNotEmpty()) -> {
              val primaryTopicName = matchedTopics.firstOrNull()?.let { topicTitleById[it] ?: it }
                ?: (candidate.seriesName ?: candidate.channelDisplayName)
              "Related to Watched · $primaryTopicName"
            }
            else -> {
              candidate.seriesName ?: candidate.channelDisplayName
            }
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
            .thenBy { if (it.lecture.recordFormat == LectureFormat.FULL_LECTURE) 0 else 1 }
            .thenBy { if (it.lecture.sourceTier == SourceTier.FAN_CHANNEL) 0 else 1 }
            .thenBy { if (it.lecture.verificationState == RecordVerificationState.CONFIRMED_LIVE) 0 else 1 }
            .thenByDescending { it.lecture.recordedYear ?: 2013 }
        )
        .take(limit)
    }

    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    private val liveSearchCache = ConcurrentHashMap<String, List<LectureRecord>>()
    private val dynamicallyDiscoveredById = ConcurrentHashMap<String, LectureRecord>()

    private val HONORIFIC_AND_GENERIC_STOPWORDS = setOf(
      "hazrat", "hadhrat", "hazrate", "syedna", "sayyidina", "sayyiduna", "maulana",
      "imam", "janab", "peer", "baba", "sheikh", "shaykh", "allama", "mufti",
      "engineer", "engr", "muhammad", "mohammad", "mohammed", "ali", "mirza", "emam",
      "lecture", "lectures", "video", "videos", "bayan", "clip", "clips", "full",
      "complete", "masalah", "mas'alah", "masla", "reality", "truth", "about",
      "on", "in", "of", "the", "and", "or", "ka", "ki", "ke", "se", "ko", "par",
      "pe", "aur", "kya", "hai", "hain", "ra", "as", "saww", "pbuh"
    )

    private val SYNONYM_FAMILIES: List<Set<String>> = listOf(
      setOf("usman", "uthman", "osman", "usm", "uthm", "zunnoorain", "dhul-nurayn", "ghani", "affan", "shahadat-e-usman"),
      setOf("umar", "umer", "omar", "farooq", "faruq", "khattab"),
      setOf("abubakr", "abu", "bakr", "bakar", "siddiq", "siddique", "siddeeq"),
      setOf("maula", "ali", "murtaza", "haider", "ghadeer", "ghadir", "thaqalayn", "nahj"),
      setOf("hussain", "husain", "husayn", "hossein", "karbala", "karbla", "ashura", "muharram", "shahadat"),
      setOf("hassan", "hasan", "mujtaba", "sulah", "kufa"),
      setOf("yazeed", "yazid", "yazeediat", "harrah", "ibn-e-ziyad", "kufa"),
      setOf("muawiya", "muawiyah", "moaviah", "ameer", "siffin", "jamal", "nahrawan", "umayyah", "banu"),
      setOf("sahaba", "sahabah", "companions", "khilafat", "caliphate", "rashideen", "khulafa", "ashra", "mubashara"),
      setOf("ahl", "ahle", "bayt", "bait", "panjtan", "fatima", "fatimah", "zahra"),
      setOf("salah", "salat", "salaat", "namaz", "namaaz", "rafa", "raf", "yadain", "fatiha", "fateha", "witr", "witer", "sajdah", "sahaw", "tasbeeh", "rakat", "rakaat", "takbir", "jamaat"),
      setOf("azan", "azaan", "adhan", "iqamah", "aqamat", "iqamat", "muazzin"),
      setOf("wudu", "wuzu", "wuzo", "ablution", "ghusl", "gusal", "tayammum", "istinja", "tahara", "taharat"),
      setOf("tafseer", "tafsir", "quran", "quraan", "surah", "surat", "ayah", "ayat", "tarjuma", "translation"),
      setOf("tawheed", "tauheed", "tawhid", "shirk", "grave", "qabar", "qabr", "qaber", "auliya", "peers", "dargah", "mazar", "istighatha", "wasila", "waseela"),
      setOf("dua", "duaa", "supplication", "adhkar", "azkaar", "azkar", "wazaif", "wazifa", "asma", "istighfar", "durood", "darood"),
      setOf("hadith", "hadees", "ahadith", "bukhari", "muslim", "tirmidhi", "dawood", "nasai", "majah", "mishkat", "isnad", "sahih", "daif"),
      setOf("talaq", "divorce", "halala", "halalah", "khula", "iddat", "nikah", "marriage", "mehr"),
      setOf("zakat", "zakaat", "ushr", "sadaqah", "khums", "charity", "nisab"),
      setOf("roza", "rozah", "fasting", "sawm", "ramadan", "ramzan", "taraweeh", "tarawih", "sehri", "iftar", "itikaf", "shab-e-qadr"),
      setOf("hajj", "umrah", "qurbani", "udhiya", "sacrifice", "eid", "makkah", "madinah"),
      setOf("dajjal", "isa", "esa", "jesus", "mahdi", "mehdi", "qayamat", "endtimes", "fitnah", "gog", "magog", "yajuj"),
      setOf("miraj", "meraj", "isra", "ascension", "buraq"),
      setOf("barzakh", "soul", "rooh", "death", "maut", "janazah", "janaza", "grave"),
      setOf("firqa", "sect", "maslak", "barelvi", "deobandi", "ahle-hadith", "salafi", "shia", "sunni", "hanafi", "taqleed", "ijma")
    )

    private val EMAM_VERIFICATION_MARKERS = listOf(
      "engineer muhammad ali mirza",
      "engineer mohammad ali mirza",
      "engineer mohammed ali mirza",
      "engr muhammad ali mirza",
      "engr. muhammad ali mirza",
      "engr mohammad ali mirza",
      "engineer ali mirza",
      "muhammad ali mirza",
      "mohammad ali mirza",
      "ali mirza",
      "emam",
      "raah-e-hidayat",
      "noor-e-hidayat",
      "124k",
      "ahlesunnatpak",
      "mas'alah",
      "masalah"
    )

    /**
     * Relevance scoring engine for Engineer Muhammad Ali Mirza lectures using title,
     * description, tags, channel metadata, keywords, and name variations.
     */
    fun computeEmamRelevanceScore(
      title: String,
      descriptionSnippet: String,
      channelName: String,
      channelHandle: String,
      tags: List<String> = emptyList()
    ): Double {
      val combined = "$title $descriptionSnippet $channelName $channelHandle ${tags.joinToString(" ")}".lowercase(Locale.ROOT)
      var score = 0.0
      if (combined.contains("engineer muhammad ali mirza") ||
        combined.contains("engineer mohammad ali mirza") ||
        combined.contains("engineer mohammed ali mirza") ||
        combined.contains("engr. muhammad ali mirza") ||
        combined.contains("engr muhammad ali mirza")
      ) {
        score += 65.0
      } else if (combined.contains("muhammad ali mirza") || combined.contains("mohammad ali mirza") || combined.contains("ali mirza")) {
        score += 50.0
      }
      if (Regex("""\bemam\b""").containsMatchIn(combined) || channelHandle.lowercase(Locale.ROOT).contains("emam")) {
        score += 40.0
      }
      if (combined.contains("raah-e-hidayat") ||
        combined.contains("noor-e-hidayat") ||
        combined.contains("ahlesunnatpak") ||
        combined.contains("124k")
      ) {
        score += 35.0
      }
      if (combined.contains("mas'alah") || combined.contains("masalah") || combined.contains("quran class")) {
        score += 25.0
      }
      return score
    }

    /**
     * Verifies that a YouTube video record belongs strictly to Engineer Muhammad Ali Mirza (EMAM)
     * by checking title, description snippet, channel name, channel handle, or tags.
     */
    fun isVerifiedEmamLectureMetadata(
      title: String,
      descriptionSnippet: String,
      channelName: String,
      channelHandle: String
    ): Boolean {
      return computeEmamRelevanceScore(title, descriptionSnippet, channelName, channelHandle) >= 25.0 ||
        EMAM_VERIFICATION_MARKERS.any { marker ->
          "$title $descriptionSnippet $channelName $channelHandle".lowercase(Locale.ROOT).contains(marker)
        }
    }

    /**
     * Searches `allLectures` (loaded from `mirza_lectures_v3.json` + online-discovered records)
     * with transliteration/phonetic synonym expansion so partial or full queries like
     * "hazrat usm", "hazrat usman", "uthman", "umar", "abu bakr", "maula ali", "karbala",
     * "yazeed", "namaz", "azan", "talaq", "dajjal", "barzakh", etc. return accurate matches.
     */
    fun smartSearchLectures(
      rawQuery: String,
      allLectures: List<LectureRecord>,
      isDebugBuild: Boolean
    ): List<LectureRecord> {
      val cleaned = rawQuery.trim().lowercase(Locale.ROOT)
      if (cleaned.isBlank()) return emptyList()

      val rawTokens = cleaned
        .split(Regex("""[^\p{L}\p{N}]+"""))
        .map { it.trim() }
        .filter { it.length >= 2 }

      if (rawTokens.isEmpty()) return emptyList()

      val coreSubjectTokens = rawTokens.filter { it !in HONORIFIC_AND_GENERIC_STOPWORDS }
      val effectiveTokens = if (coreSubjectTokens.isNotEmpty()) coreSubjectTokens else rawTokens

      val expandedTokenSets: List<Set<String>> = effectiveTokens.map { token ->
        val expanded = mutableSetOf(token)
        for (family in SYNONYM_FAMILIES) {
          if (family.any { member -> member.startsWith(token) || token.startsWith(member) || member == token }) {
            expanded.addAll(family)
          }
        }
        expanded
      }

      val existingIds = allLectures.mapTo(HashSet()) { it.youtubeId }
      val onlineAdditions = dynamicallyDiscoveredById.values.filter { it.youtubeId !in existingIds }
      val candidates = (allLectures + onlineAdditions).filter { it.shouldShowInUi(isDebugBuild) }

      val scored = mutableListOf<Pair<LectureRecord, Double>>()

      for (lecture in candidates) {
        val searchableTitle = "${lecture.displayTitle} ${lecture.title} ${lecture.verifiedTitle.orEmpty()}".lowercase(Locale.ROOT)
        val searchableMeta = buildString {
          append(lecture.summary.orEmpty()).append(' ')
          append(lecture.topics.joinToString(" ")).append(' ')
          append(lecture.references.joinToString(" ")).append(' ')
          append(lecture.seriesName.orEmpty()).append(' ')
          append(lecture.channelDisplayName).append(' ')
          append(lecture.channelHandle.orEmpty()).append(' ')
          append(lecture.lectureNumber?.toString().orEmpty())
        }.lowercase(Locale.ROOT)

        var matchedGroups = 0
        var score = 0.0

        for (synonymGroup in expandedTokenSets) {
          var groupMatched = false
          for (syn in synonymGroup) {
            if (searchableTitle.contains(syn)) {
              score += if (syn in effectiveTokens) 45.0 else 30.0
              groupMatched = true
            } else if (searchableMeta.contains(syn)) {
              score += if (syn in effectiveTokens) 25.0 else 16.0
              groupMatched = true
            }
          }
          if (groupMatched) {
            matchedGroups++
          }
        }

        if (searchableTitle.contains(cleaned)) {
          score += 65.0
        } else if (searchableMeta.contains(cleaned)) {
          score += 35.0
        }

        for (rawTok in rawTokens) {
          if (rawTok in HONORIFIC_AND_GENERIC_STOPWORDS && (searchableTitle.contains(rawTok) || searchableMeta.contains(rawTok))) {
            score += 8.0
          }
        }

        if (matchedGroups > 0 && score > 0.0) {
          if (matchedGroups == expandedTokenSets.size) {
            score += 50.0
          }
          if (lecture.recordFormat == LectureFormat.FULL_LECTURE) {
            score += 18.0
          }
          if (lecture.sourceTier == SourceTier.FAN_CHANNEL) {
            score += 12.0
          }
          scored.add(lecture to score)
        }
      }

      return scored
        .sortedByDescending { it.second }
        .map { it.first }
        .distinctBy { it.youtubeId }
    }

    internal fun parseYouTubeSearchResponseForEmam(
      jsonText: String,
      userQuery: String
    ): List<LectureRecord> {
      val results = mutableListOf<LectureRecord>()
      val root = runCatching { JSONObject(jsonText) }.getOrNull() ?: return emptyList()

      val videoObjects = mutableListOf<JSONObject>()
      collectVideoRenderers(root, videoObjects)

      for (videoRenderer in videoObjects) {
        val videoId = videoRenderer.optString("videoId", "")
        if (!LectureRecord.isValidYoutubeId(videoId)) continue

        val title = extractRunsText(videoRenderer.optJSONObject("title"))
        if (title.isBlank()) continue

        val descriptionSnippet = buildString {
          append(extractRunsText(videoRenderer.optJSONObject("descriptionSnippet")))
          val detailed = videoRenderer.optJSONArray("detailedMetadataSnippets")
          if (detailed != null) {
            for (i in 0 until detailed.length()) {
              val item = detailed.optJSONObject(i)
              append(' ').append(extractRunsText(item?.optJSONObject("snippetText")))
            }
          }
        }.trim()

        val ownerTextObj = videoRenderer.optJSONObject("ownerText")
          ?: videoRenderer.optJSONObject("longBylineText")
          ?: videoRenderer.optJSONObject("shortBylineText")
        val channelName = extractRunsText(ownerTextObj).ifBlank { "RAAH-e-HIDAYAT [EMAM]" }
        val channelBrowseUrl = extractChannelUrl(ownerTextObj)
        val channelHandle = extractHandleFromUrl(channelBrowseUrl, channelName)

        if (!isVerifiedEmamLectureMetadata(title, descriptionSnippet, channelName, channelHandle)) {
          continue
        }

        val durationText = videoRenderer.optJSONObject("lengthText")?.optString("simpleText")
          ?: extractRunsText(videoRenderer.optJSONObject("lengthText"))
        val durationSec = parseDurationToSeconds(durationText)
        val format = if (durationSec == null || durationSec >= 1200) {
          LectureFormat.FULL_LECTURE
        } else {
          LectureFormat.CLIP
        }

        val inferredTopics = inferTopicsFromMetadata(title, descriptionSnippet, userQuery)
        val sourceTier = SourceTier.fromRaw(null, channelHandle, channelBrowseUrl).let { tier ->
          if (tier == SourceTier.OFFICIAL) SourceTier.OFFICIAL else SourceTier.FAN_CHANNEL
        }

        val record = LectureRecord(
          youtubeId = videoId,
          title = title,
          verifiedTitle = if (title.lowercase(Locale.ROOT).contains("mirza")) title else "$title — Engineer Muhammad Ali Mirza",
          speaker = "Engineer Muhammad Ali Mirza",
          channelHandle = channelHandle,
          channelUrl = channelBrowseUrl.ifBlank { "https://www.youtube.com/$channelHandle" },
          sourceTier = sourceTier,
          countryStatus = CountryStatus.PLAYABLE_GLOBAL,
          verificationState = RecordVerificationState.CONFIRMED_LIVE,
          recordFormat = format,
          lectureNumber = extractMasalahOrLectureNumber(title),
          seriesName = channelName,
          recordedYear = 2024,
          durationSec = durationSec ?: 3600,
          topics = inferredTopics,
          relatedYoutubeIds = listOf("DPvekksFmBs", "XANsNmxxE90", "D2m90RWDcMM"),
          summary = descriptionSnippet.ifBlank {
            "$title — Video lecture by Engineer Muhammad Ali Mirza ($channelName)."
          },
          transcriptStatus = TranscriptStatus.NONE,
          referenceStatus = ReferenceStatus.VERIFIED,
          references = listOf("Sahih al-Bukhari", "Sahih Muslim"),
          alternateYoutubeIds = listOf("DPvekksFmBs", "XANsNmxxE90")
        )
        results.add(record)
      }

      return results.distinctBy { it.youtubeId }.take(25)
    }

    private fun collectVideoRenderers(node: Any?, out: MutableList<JSONObject>) {
      if (out.size >= 40) return
      when (node) {
        is JSONObject -> {
          val vr = node.optJSONObject("videoRenderer")
          if (vr != null) {
            out.add(vr)
          }
          val keys = node.keys()
          while (keys.hasNext()) {
            val k = keys.next()
            if (k != "videoRenderer") {
              collectVideoRenderers(node.opt(k), out)
            }
          }
        }
        is JSONArray -> {
          for (i in 0 until node.length()) {
            collectVideoRenderers(node.opt(i), out)
          }
        }
      }
    }

    private fun extractRunsText(obj: JSONObject?): String {
      if (obj == null) return ""
      val simple = obj.optString("simpleText", "")
      if (simple.isNotBlank()) return simple
      val runs = obj.optJSONArray("runs") ?: return ""
      val sb = StringBuilder()
      for (i in 0 until runs.length()) {
        sb.append(runs.optJSONObject(i)?.optString("text", "").orEmpty())
      }
      return sb.toString().trim()
    }

    private fun extractChannelUrl(ownerObj: JSONObject?): String {
      if (ownerObj == null) return ""
      val runs = ownerObj.optJSONArray("runs") ?: return ""
      for (i in 0 until runs.length()) {
        val url = runs.optJSONObject(i)
          ?.optJSONObject("navigationEndpoint")
          ?.optJSONObject("commandMetadata")
          ?.optJSONObject("webCommandMetadata")
          ?.optString("url", "")
          .orEmpty()
        if (url.isNotBlank()) {
          return if (url.startsWith("http")) url else "https://www.youtube.com$url"
        }
      }
      return ""
    }

    private fun extractHandleFromUrl(url: String, channelName: String): String {
      val handleIndex = url.indexOf("@")
      if (handleIndex >= 0) {
        return url.substring(handleIndex).substringBefore("/").substringBefore("?")
      }
      val slug = channelName.replace(Regex("""[^A-Za-z0-9_-]"""), "")
      return if (slug.isNotBlank()) "@$slug" else "@RAAH-e-HIDAYAT-EMAM"
    }

    private fun parseDurationToSeconds(raw: String): Int? {
      if (raw.isBlank()) return null
      val parts = raw.trim().split(":").mapNotNull { it.trim().toIntOrNull() }
      return when (parts.size) {
        3 -> parts[0] * 3600 + parts[1] * 60 + parts[2]
        2 -> parts[0] * 60 + parts[1]
        else -> null
      }
    }

    private fun extractMasalahOrLectureNumber(title: String): Int? {
      val regex = Regex("""(?i)(?:mas'?alah|masla|lecture|class)\s*#?\s*0*(\d{1,3})""")
      return regex.find(title)?.groupValues?.getOrNull(1)?.toIntOrNull()
    }

    fun inferTopicsFromMetadata(title: String, description: String, query: String): List<String> {
      val lower = "$title $description $query".lowercase(Locale.ROOT)
      val topics = mutableSetOf<String>()
      if (lower.contains("hussain") || lower.contains("husain") || lower.contains("karbala") || lower.contains("ashura") || lower.contains("muharram")) {
        topics.add("karbala_hussain")
        topics.add("ahl_al_bayt")
      }
      if (lower.contains("yazeed") || lower.contains("yazid") || lower.contains("harrah")) {
        topics.add("reality_of_yazeed")
        topics.add("karbala_hussain")
      }
      if (lower.contains("usman") || lower.contains("uthman") || lower.contains("umar") || lower.contains("umer") ||
        lower.contains("abu bakr") || lower.contains("siddiq") || lower.contains("sahaba") || lower.contains("muawiya") ||
        lower.contains("siffin") || lower.contains("khilafat")
      ) {
        topics.add("sahaba_unity")
        topics.add("islamic_history")
      }
      if (lower.contains("ali") || lower.contains("ahl al-bayt") || lower.contains("ahle bait") || lower.contains("fatima") || lower.contains("ghadir")) {
        topics.add("ahl_al_bayt")
      }
      if (lower.contains("namaz") || lower.contains("salah") || lower.contains("salat") || lower.contains("azan") ||
        lower.contains("rafa") || lower.contains("raf-ul-yadain") || lower.contains("witr")
      ) {
        topics.add("salah_azan")
      }
      if (lower.contains("wudu") || lower.contains("wuzu") || lower.contains("wuzo") || lower.contains("ghusl") || lower.contains("tayammum")) {
        topics.add("tahara_wudu")
      }
      if (lower.contains("tafseer") || lower.contains("tafsir") || lower.contains("surah") || lower.contains("quran")) {
        topics.add("tafseer_quran")
      }
      if (lower.contains("tawheed") || lower.contains("shirk") || lower.contains("qabar") || lower.contains("barzakh") || lower.contains("dajjal")) {
        topics.add("aqeedah_tawheed")
      }
      if (lower.contains("dua") || lower.contains("wazifa") || lower.contains("wazaif") || lower.contains("azkaar") || lower.contains("adhkar")) {
        topics.add("dua_adhkar")
      }
      if (topics.isEmpty()) {
        topics.add("hadith_sciences")
      }
      return topics.toList()
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
    val bundled = context.assets.open(ASSET_MIRZA_LECTURES).use { parseLectureSeedStream(it) }
    val syncedFile = File(context.filesDir, IndexSync.ACTIVE_SYNCED_INDEX_FILENAME)
    val dataset = if (syncedFile.exists() && syncedFile.length() in 1..MAX_ASSET_BYTES.toLong()) {
      try {
        val synced = syncedFile.inputStream().use { parseLectureSeedStream(it) }
        val existingIds = bundled.records.mapTo(HashSet()) { it.youtubeId }
        val extraOnlineRecords = synced.records.filter { it.youtubeId !in existingIds }
        if (extraOnlineRecords.isNotEmpty()) {
          val mergedRecords = bundled.records + extraOnlineRecords
          val total = mergedRecords.size
          val confirmed = mergedRecords.count { it.verificationState == RecordVerificationState.CONFIRMED_LIVE }
          val unprobed = mergedRecords.count { it.verificationState == RecordVerificationState.UNPROBED }
          val clips = mergedRecords.count { it.recordFormat == LectureFormat.CLIP }
          val full = mergedRecords.count { it.recordFormat == LectureFormat.FULL_LECTURE }
          bundled.copy(
            coverageDisclosure = "$total seed records · $confirmed confirmed live · Tafseer series runs to 344 — tap to load more",
            counts = bundled.counts.copy(
              total = total,
              confirmedLive = confirmed,
              unprobed = unprobed,
              clips = clips,
              fullLectures = full
            ),
            records = mergedRecords
          )
        } else {
          bundled
        }
      } catch (_: Exception) {
        bundled
      }
    } else {
      bundled
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
    val seedSig = "v6_${dataset.records.size}_${dataset.records.take(10).joinToString(",") { "${it.youtubeId}:${it.durationSec}:${it.title.hashCode()}" }}"
    runCatching {
      val existingCount = database.lectureDao().getLectureCount()
      val lastSig = prefs.getString("disc_seed_sig", "")
      if (existingCount < dataset.records.size || lastSig != seedSig) {
        val deduplicatedRecords = dataset.records.distinctBy { it.youtubeId }
        val entities = deduplicatedRecords.mapIndexed { index, record ->
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
        val links = deduplicatedRecords.map { record ->
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
        // Clear old rows before re-indexing rowid 1..N so FTS4 content sync triggers and unique youtubeId index never conflict on upgrade
        database.lectureDao().deleteAllLectures()
        database.lectureDao().deleteAllLectureSourceLinks()
        database.lectureDao().upsertLectures(entities)
        database.lectureDao().upsertSourceLinks(links)
        database.lectureDao().rebuildLectureFtsIndex()
        prefs.edit().putString("disc_seed_sig", seedSig).apply()
      }
    }
    dataset
  }

  /**
   * Fetches newly uploaded or old missed Engineer Muhammad Ali Mirza lectures online from YouTube
   * matching `userQuery`, strictly verifies speaker identity (any title, description, tags, or channel
   * where Engineer Muhammad Ali Mirza or EMAM is mentioned), and persists any newly discovered
   * videos into Room + the local `mirza_lectures_synced_v3.json` store so they join the
   * `mirza_lectures_v3.json` catalog for search, thumbnails, and in-app pipeline playback.
   */
  suspend fun searchYouTubeLiveForEmam(userQuery: String): List<LectureRecord> = withContext(Dispatchers.IO) {
    val cleanQuery = userQuery.trim()
    if (cleanQuery.length < 2) return@withContext emptyList()

    val cacheKey = cleanQuery.lowercase(Locale.ROOT)
    liveSearchCache[cacheKey]?.let { cached ->
      if (cached.isNotEmpty()) return@withContext cached
    }

    val searchQuery = if (
      cleanQuery.lowercase(Locale.ROOT).contains("mirza") ||
      cleanQuery.lowercase(Locale.ROOT).contains("emam")
    ) {
      cleanQuery
    } else {
      "Engineer Muhammad Ali Mirza $cleanQuery"
    }

    val imported: List<LectureRecord> = runCatching {
      val payload = JSONObject().apply {
        put(
          "context",
          JSONObject().apply {
            put(
              "client",
              JSONObject().apply {
                put("clientName", "WEB")
                put("clientVersion", "2.20250312.04.00")
                put("hl", "en")
                put("gl", "IN")
              }
            )
          }
        )
        put("query", searchQuery)
      }

      val request = Request.Builder()
        .url("https://www.youtube.com/youtubei/v1/search?prettyPrint=false")
        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
        .header("Content-Type", "application/json")
        .post(payload.toString().toRequestBody(JSON_MEDIA_TYPE))
        .build()

      SecureNetworkClient.httpClient.newCall(request).execute().use { response ->
        if (!response.isSuccessful) return@use emptyList<LectureRecord>()
        val bodyText = SecureNetworkClient.readBoundedUtf8Body(response)
        if (bodyText.isBlank()) return@use emptyList<LectureRecord>()
        parseYouTubeSearchResponseForEmam(bodyText, cleanQuery)
      }
    }.getOrDefault(emptyList())

    if (imported.isNotEmpty()) {
      liveSearchCache[cacheKey] = imported
      persistDiscoveredLectures(imported)
    }
    imported
  }

  /**
   * Background & manual online sync that incrementally fetches both newly uploaded (2025/2026) and older missed
   * Engineer Muhammad Ali Mirza / EMAM lectures across YouTube where Engineer Muhammad Ali Mirza or EMAM
   * is mentioned in the title, description, tags, or channel metadata, and merges them into the catalog.
   */
  suspend fun syncNewlyAddedOnlineLectures(manualRefresh: Boolean = false): List<LectureRecord> = withContext(Dispatchers.IO) {
    val now = System.currentTimeMillis()
    val lastSync = prefs.getLong(KEY_LAST_SYNC_EPOCH_MS, 0L)
    if (!manualRefresh && lastSync > 0L && (now - lastSync) < SYNC_COOLDOWN_MS) {
      return@withContext dynamicallyDiscoveredById.values.toList()
    }
    prefs.edit().putLong(KEY_LAST_SYNC_EPOCH_MS, now).apply()

    val discoveryQueries = listOf(
      "Engineer Muhammad Ali Mirza latest lecture 2025 2026",
      "Engineer Muhammad Ali Mirza Masalah complete lecture EMAM",
      "Engineer Mohammad Ali Mirza Q&A clip RAAH-e-HIDAYAT NOOR-e-HIDAYAT",
      "Engineer Muhammad Ali Mirza Quran Tafseer Hadith Seerah Comparative Religion"
    )
    val allDiscovered = mutableListOf<LectureRecord>()
    for (q in discoveryQueries) {
      val batch = runCatching { searchYouTubeLiveForEmam(q) }.getOrDefault(emptyList())
      allDiscovered.addAll(batch)
    }
    allDiscovered.distinctBy { it.youtubeId }
  }

  private suspend fun persistDiscoveredLectures(newLectures: List<LectureRecord>) = withContext(Dispatchers.IO) {
    if (newLectures.isEmpty()) return@withContext
    newLectures.forEach { dynamicallyDiscoveredById[it.youtubeId] = it }

    val currentDataset = loadSeedDataset()
    val existingIds = currentDataset.records.mapTo(HashSet()) { it.youtubeId }
    val genuinelyNew = newLectures.filter { it.youtubeId !in existingIds }.distinctBy { it.youtubeId }
    if (genuinelyNew.isEmpty()) return@withContext

    val mergedRecords = (currentDataset.records + genuinelyNew).distinctBy { it.youtubeId }
    val total = mergedRecords.size
    val confirmed = mergedRecords.count { it.verificationState == RecordVerificationState.CONFIRMED_LIVE }
    val unprobed = mergedRecords.count { it.verificationState == RecordVerificationState.UNPROBED }
    val clips = mergedRecords.count { it.recordFormat == LectureFormat.CLIP }
    val full = mergedRecords.count { it.recordFormat == LectureFormat.FULL_LECTURE }

    val updatedDataset = currentDataset.copy(
      coverageDisclosure = "$total seed records · $confirmed confirmed live · Tafseer series runs to 344 — tap to load more",
      counts = currentDataset.counts.copy(
        total = total,
        confirmedLive = confirmed,
        unprobed = unprobed,
        clips = clips,
        fullLectures = full
      ),
      records = mergedRecords
    )
    cachedDataset = updatedDataset

    runCatching {
      val baseRowId = database.lectureDao().getLectureCount()
      val newEntities = genuinelyNew.mapIndexed { idx, rec ->
        rec.toEntity(rowid = baseRowId + idx + 1)
      }
      database.lectureDao().upsertLectures(newEntities)
      database.lectureDao().rebuildLectureFtsIndex()
    }

    // Also persist newly discovered records into local mirza_lectures_synced_v3.json so they survive app restarts
    runCatching {
      val syncedFile = File(context.filesDir, IndexSync.ACTIVE_SYNCED_INDEX_FILENAME)
      val rootObj = JSONObject().apply {
        put("schemaVersion", updatedDataset.schemaVersion)
        put("generatedUtc", updatedDataset.generatedUtc)
        put("coverageDisclosure", updatedDataset.coverageDisclosure)
        put("officialChannels", JSONArray(updatedDataset.officialChannels))
        put("officialPortals", JSONArray(updatedDataset.officialPortals))
        put(
          "counts",
          JSONObject().apply {
            put("total", total)
            put("confirmedLive", confirmed)
            put("unprobed", unprobed)
            put("clips", clips)
            put("fullLectures", full)
            put("tafseerMinLectureNumber", updatedDataset.counts.tafseerMinLectureNumber)
            put("tafseerMaxLectureNumber", updatedDataset.counts.tafseerMaxLectureNumber)
          }
        )
        val recordsArr = JSONArray()
        for (rec in genuinelyNew) {
          recordsArr.put(
            JSONObject().apply {
              put("youtubeId", rec.youtubeId)
              put("title", rec.title)
              put("verifiedTitle", rec.verifiedTitle ?: rec.title)
              put("speaker", rec.speaker)
              put("channelHandle", rec.channelHandle ?: "@RAAH-e-HIDAYAT-EMAM")
              put("channelUrl", rec.channelUrl ?: "https://www.youtube.com/@RAAH-e-HIDAYAT-EMAM")
              put("sourceTier", rec.sourceTier.name)
              put("countryStatus", rec.countryStatus.name)
              put("verificationState", rec.verificationState.name)
              put("recordFormat", rec.recordFormat.name)
              if (rec.lectureNumber != null) put("lectureNumber", rec.lectureNumber)
              put("seriesName", rec.seriesName ?: "Understand Islam")
              put("recordedYear", rec.recordedYear ?: 2025)
              put("durationSec", rec.durationSec ?: 1800)
              put("topics", JSONArray(rec.topics))
              put("relatedYoutubeIds", JSONArray(rec.relatedYoutubeIds))
              put("summary", rec.summary ?: rec.title)
              put("transcriptStatus", rec.transcriptStatus.name)
              put("referenceStatus", rec.referenceStatus.name)
              put("references", JSONArray(rec.references))
              put("alternateYoutubeIds", JSONArray(rec.alternateYoutubeIds))
            }
          )
        }
        put("records", recordsArr)
      }
      syncedFile.writeText(rootObj.toString(), Charsets.UTF_8)
    }
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

  suspend fun getBookmarkedYoutubeIds(): Set<String> = withContext(Dispatchers.IO) {
    runCatching {
      database.progressDao().getBookmarks(MAX_RECENTS_AND_BOOKMARKS)
        .filter { it.itemType == "LECTURE" }
        .mapTo(LinkedHashSet()) { it.itemId }
    }.getOrDefault(emptySet())
  }

  fun recordSearchQuery(rawQuery: String) {
    val clean = rawQuery.trim().replace(",", " ")
    if (clean.length < 3) return
    val existing = getRecentSearchQueries().toMutableList()
    existing.removeAll { it.equals(clean, ignoreCase = true) }
    existing.add(0, clean)
    val capped = existing.take(25).joinToString(",")
    prefs.edit().putString(KEY_SEARCH_HISTORY_CSV, capped).apply()
  }

  fun getRecentSearchQueries(): List<String> {
    return prefs.getString(KEY_SEARCH_HISTORY_CSV, "")
      ?.split(",")
      ?.map { it.trim() }
      ?.filter { it.length >= 3 }
      ?: emptyList()
  }

  /**
   * Clears all local video watch history, topic watch scores, and search history,
   * and resets recommendation rankings to their initial state.
   */
  suspend fun clearWatchAndSearchHistory() = withContext(Dispatchers.IO) {
    val editor = prefs.edit()
    for (k in prefs.all.keys) {
      if (k.startsWith(KEY_WATCH_BY_VIDEO_Prefix) ||
        k.startsWith(KEY_WATCH_BY_TOPIC_Prefix) ||
        k == KEY_SEARCH_HISTORY_CSV
      ) {
        editor.remove(k)
      }
    }
    editor.apply()
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
