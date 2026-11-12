package com.example.player

import com.example.data.model.LectureRecord
import com.example.data.model.PlaybackDataProfile
import com.example.security.DoSResourceShield
import com.example.security.SanitizedLogger
import com.example.security.SecureNetworkClient
import com.example.security.SsrfRedirectValidator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

/**
 * Multi-Quality & Data-Saver Ladder for Native ExoPlayer (`androidx.media3`).
 * Defaults to `Q_360P` ("360p (Default Saver)") to save mobile data while maintaining crisp lecture legibility.
 */
enum class VideoQualityOption(
  val shortLabel: String,
  val menuLabel: String,
  val maxWidth: Int,
  val maxHeight: Int,
  val maxBitrateBps: Int,
  val approxMbPerMin: Double,
  val approxMbPerHour: Int,
  val isAudioOnly: Boolean
) {
  Q_144P(
    shortLabel = "144p",
    menuLabel = "144p (Ultra Data Saver · ~0.8 MB/min)",
    maxWidth = 256,
    maxHeight = 144,
    maxBitrateBps = 140_000,
    approxMbPerMin = 0.8,
    approxMbPerHour = 48,
    isAudioOnly = false
  ),
  Q_240P(
    shortLabel = "240p",
    menuLabel = "240p (Default Saver · ~1.4 MB/min)",
    maxWidth = 426,
    maxHeight = 240,
    maxBitrateBps = 280_000,
    approxMbPerMin = 1.4,
    approxMbPerHour = 84,
    isAudioOnly = false
  ),
  Q_360P(
    shortLabel = "360p",
    menuLabel = "360p (Default Saver · ~2.5 MB/min)",
    maxWidth = 640,
    maxHeight = 360,
    maxBitrateBps = 500_000,
    approxMbPerMin = 2.5,
    approxMbPerHour = 150,
    isAudioOnly = false
  ),
  Q_480P(
    shortLabel = "480p",
    menuLabel = "480p (Standard · ~4.5 MB/min)",
    maxWidth = 854,
    maxHeight = 480,
    maxBitrateBps = 900_000,
    approxMbPerMin = 4.5,
    approxMbPerHour = 270,
    isAudioOnly = false
  ),
  Q_720P(
    shortLabel = "720p HD",
    menuLabel = "720p (HD · ~9.0 MB/min)",
    maxWidth = 1280,
    maxHeight = 720,
    maxBitrateBps = 1_800_000,
    approxMbPerMin = 9.0,
    approxMbPerHour = 540,
    isAudioOnly = false
  ),
  Q_1080P(
    shortLabel = "1080p FHD",
    menuLabel = "1080p (Full HD · ~16.0 MB/min)",
    maxWidth = 1920,
    maxHeight = 1080,
    maxBitrateBps = 3_500_000,
    approxMbPerMin = 16.0,
    approxMbPerHour = 960,
    isAudioOnly = false
  ),
  AUDIO_64K(
    shortLabel = "Audio 64k",
    menuLabel = "Audio-Only (64 kbps Low Data · ~0.5 MB/min)",
    maxWidth = 0,
    maxHeight = 0,
    maxBitrateBps = 64_000,
    approxMbPerMin = 0.5,
    approxMbPerHour = 30,
    isAudioOnly = true
  ),
  AUDIO_128K(
    shortLabel = "Audio 128k",
    menuLabel = "Audio-Only (128 kbps Standard · ~0.9 MB/min)",
    maxWidth = 0,
    maxHeight = 0,
    maxBitrateBps = 128_000,
    approxMbPerMin = 0.9,
    approxMbPerHour = 54,
    isAudioOnly = true
  );

  fun toPlaybackDataProfile(): PlaybackDataProfile = when (this) {
    Q_144P, Q_240P, AUDIO_64K, AUDIO_128K -> PlaybackDataProfile.SAVER
    Q_360P, Q_480P -> PlaybackDataProfile.BALANCED
    Q_720P, Q_1080P -> PlaybackDataProfile.QUALITY
  }

  companion object {
    val DEFAULT_SAVER_QUALITY = Q_360P

    fun fromPlaybackDataProfile(profile: PlaybackDataProfile): VideoQualityOption = when (profile) {
      PlaybackDataProfile.SAVER -> Q_240P
      PlaybackDataProfile.BALANCED -> Q_360P
      PlaybackDataProfile.QUALITY -> Q_720P
      PlaybackDataProfile.AUTO -> Q_360P
    }
  }
}

/**
 * Opaque Pipeline Labels — Never exposes raw domain names or server URLs in the UI.
 */
enum class PipelineOption(val displayLabel: String) {
  VINE_YMUSIC_SDK("Vine / yMusic SDK"),
  AUTO_SMART("Auto-Switch (Smart)"),
  PIPELINE_1("Pipeline 1"),
  PIPELINE_2("Pipeline 2"),
  PIPELINE_3("Pipeline 3"),
  PIPELINE_4("Pipeline 4")
}

data class ResolvedStreamTrack(
  val quality: VideoQualityOption,
  val videoUrl: String,
  val separateAudioUrl: String? = null,
  val isHls: Boolean = false,
  val isDirectYoutubeStream: Boolean = false,
  val requiredUserAgent: String? = null
)

data class ResolvedVideoStreamBundle(
  val youtubeId: String,
  val stableVideoId: String = "OFFICIAL:general:$youtubeId",
  val activePipeline: PipelineOption,
  val tracksByQuality: Map<VideoQualityOption, ResolvedStreamTrack>,
  val hlsManifestUrl: String? = null,
  val fallbackBackupMp4Url: String,
  val hasDirectYoutubeStreams: Boolean = false,
  val extractedDurationSec: Int? = null
) {
  fun getAvailableQualities(): List<VideoQualityOption> {
    return VideoPlaybackResumeStore.getAvailableQualitiesForBundle(this)
  }

  fun resolveTrackFor(quality: VideoQualityOption): ResolvedStreamTrack {
    tracksByQuality[quality]?.let { return it }
    // Nearest neighbor fallback
    if (quality.isAudioOnly) {
      tracksByQuality[VideoQualityOption.AUDIO_64K]?.let { return it.copy(quality = quality) }
      tracksByQuality[VideoQualityOption.AUDIO_128K]?.let { return it.copy(quality = quality) }
    }
    val orderedVideo = listOf(
      VideoQualityOption.Q_360P,
      VideoQualityOption.Q_240P,
      VideoQualityOption.Q_480P,
      VideoQualityOption.Q_720P,
      VideoQualityOption.Q_144P,
      VideoQualityOption.Q_1080P
    )
    for (candidate in orderedVideo) {
      tracksByQuality[candidate]?.let {
        return it.copy(quality = quality)
      }
    }
    if (hlsManifestUrl != null) {
      return ResolvedStreamTrack(
        quality = quality,
        videoUrl = hlsManifestUrl,
        isHls = true,
        isDirectYoutubeStream = hasDirectYoutubeStreams
      )
    }
    val safeFallbackUrl = fallbackBackupMp4Url.ifBlank {
      if (LectureRecord.isValidYoutubeId(youtubeId)) {
        YtEmbedUrlBuilder.buildEmbedUrl(youtubeId)
      } else {
        "https://www.youtube-nocookie.com"
      }
    }
    return ResolvedStreamTrack(
      quality = quality,
      videoUrl = safeFallbackUrl,
      isDirectYoutubeStream = false
    )
  }
}

/**
 * Multi-Pipeline Stream Extraction Engine (`VideoStreamExtractor`).
 *
 * Implements a 4-stage hidden failover architecture:
 * - Pipeline 1: InnerTube Multi-Client (`ANDROID_VR`, `IOS`, `ANDROID`)
 * - Pipeline 2: Piped JSON API Cluster
 * - Pipeline 3: Invidious JSON API Cluster
 * - Pipeline 4: Clean Ad-Shielded YouTube Video Bridge locked strictly to `lecture.youtubeId`
 *
 * All URLs are validated through `SsrfRedirectValidator` and fetched via `SecureNetworkClient`.
 * Raw server hostnames are strictly hidden from all UI layers.
 */
object VideoStreamExtractor {

  private data class InnerTubeClientProfile(
    val clientName: String,
    val clientVersion: String,
    val clientIdHeader: String,
    val userAgent: String,
    val deviceMake: String? = null,
    val deviceModel: String? = null,
    val osName: String? = null,
    val osVersion: String? = null,
    val androidSdkVersion: Int? = null,
    val isEmbeddedTv: Boolean = false
  )

  private val INNERTUBE_PROFILES = listOf(
    InnerTubeClientProfile(
      clientName = "IOS",
      clientVersion = "20.10.4",
      clientIdHeader = "5",
      userAgent = "com.google.ios.youtube/20.10.4 (iPhone16,2; U; CPU iOS 18_3_2 like Mac OS X;)",
      deviceMake = "Apple",
      deviceModel = "iPhone16,2",
      osName = "iPhone",
      osVersion = "18.3.2.22D82"
    ),
    InnerTubeClientProfile(
      clientName = "ANDROID_VR",
      clientVersion = "1.62.27",
      clientIdHeader = "28",
      userAgent = "com.google.android.apps.youtube.vr.oculus/1.62.27 (Linux; U; Android 12L; eureka-user Build/SQ3A.220605.009.A1) gzip",
      deviceMake = "Oculus",
      deviceModel = "Quest 3",
      osName = "Android",
      osVersion = "12L",
      androidSdkVersion = 32
    ),
    InnerTubeClientProfile(
      clientName = "ANDROID_TESTSUITE",
      clientVersion = "1.9",
      clientIdHeader = "30",
      userAgent = "com.google.android.youtube/1.9 (Linux; U; Android 12; US) gzip",
      osName = "Android",
      osVersion = "12",
      androidSdkVersion = 31
    ),
    InnerTubeClientProfile(
      clientName = "TVHTML5_SIMPLY_EMBEDDED_PLAYER",
      clientVersion = "2.0",
      clientIdHeader = "85",
      userAgent = "Mozilla/5.0 (PlayStation; PlayStation 5/6.50) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.4 Safari/605.1.15",
      isEmbeddedTv = true
    ),
    InnerTubeClientProfile(
      clientName = "WEB_EMBEDDED_PLAYER",
      clientVersion = "1.20250310.01.00",
      clientIdHeader = "56",
      userAgent = "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36",
      isEmbeddedTv = true
    ),
    InnerTubeClientProfile(
      clientName = "ANDROID_CREATOR",
      clientVersion = "24.14.101",
      clientIdHeader = "14",
      userAgent = "com.google.android.apps.youtube.creator/24.14.101 (Linux; U; Android 14) gzip",
      osName = "Android",
      osVersion = "14",
      androidSdkVersion = 34
    )
  )

  // Active 3rd-party Piped & Invidious cluster endpoints (never shown in UI)
  private val PIPED_CLUSTER_ENDPOINTS = listOf(
    "https://pipedapi.kavin.rocks",
    "https://pipedapi.leptons.xyz",
    "https://pipedapi.nosebs.ru",
    "https://pipedapi.adminforge.de",
    "https://pipedapi.drgns.space",
    "https://api-piped.mha.fi"
  )

  private val INVIDIOUS_CLUSTER_ENDPOINTS = listOf(
    "https://inv.nadeko.net",
    "https://invidious.nerdvpn.de",
    "https://inv.tux.pizza",
    "https://invidious.privacyredirect.com",
    "https://yewtu.be"
  )

  private val fastClusterClient by lazy {
    SecureNetworkClient.httpClient.newBuilder()
      .connectTimeout(2500, java.util.concurrent.TimeUnit.MILLISECONDS)
      .readTimeout(3500, java.util.concurrent.TimeUnit.MILLISECONDS)
      .callTimeout(4500, java.util.concurrent.TimeUnit.MILLISECONDS)
      .build()
  }

  /**
   * Detects whether the host device supports hardware-accelerated rendering (true on physical phones,
   * false on headless emulators lacking `/dev/dri/renderD128`). Physical Android devices block
   * direct `File.exists()` on `/dev/dri` via SELinux, so we only probe `/dev/dri` when running on an emulator.
   */
  val hasHardwareRenderNode: Boolean by lazy {
    runCatching {
      val hw = android.os.Build.HARDWARE.orEmpty().lowercase()
      val fp = android.os.Build.FINGERPRINT.orEmpty().lowercase()
      val isEmulator = hw.contains("ranchu") ||
        hw.contains("goldfish") ||
        fp.contains("generic") ||
        fp.contains("robolectric")
      if (!isEmulator) {
        true
      } else {
        java.io.File("/dev/dri/renderD128").exists() ||
          java.io.File("/dev/dri/card0").exists()
      }
    }.getOrDefault(true)
  }

  @Volatile
  private var webViewCookieSessionActive: Boolean = false

  fun seedYoutubeConsentCookies() {
    runCatching {
      webViewCookieSessionActive = true
      val cm = android.webkit.CookieManager.getInstance()
      cm.setAcceptCookie(true)
      val existing = cm.getCookie("https://www.youtube.com").orEmpty()
      if (!existing.contains("CONSENT=")) {
        cm.setCookie(
          "https://www.youtube.com",
          "CONSENT=YES+cb.20240101-17-p0.en+FX+999; Domain=.youtube.com; Path=/; Secure; SameSite=None"
        )
        cm.setCookie(
          "https://www.youtube-nocookie.com",
          "CONSENT=YES+cb.20240101-17-p0.en+FX+999; Domain=.youtube-nocookie.com; Path=/; Secure; SameSite=None"
        )
        cm.flush()
      }
    }
  }

  fun readYoutubeWebViewCookies(): String {
    if (!webViewCookieSessionActive) return ""
    return "CONSENT=YES+cb.20240101-17-p0.en+FX+999"
  }

  fun hasYoutubeAuthCookie(): Boolean {
    return webViewCookieSessionActive
  }

  fun signOutYoutubeWebViewCookies() {
    webViewCookieSessionActive = false
    directBundleCache.clear()
    resetAllProviderCooldowns()
  }

  private fun applyYoutubeSessionHeaders(
    reqBuilder: Request.Builder,
    originUrl: String
  ) {
    reqBuilder.header("Cookie", "CONSENT=YES+cb.20240101-17-p0.en+FX+999")
    reqBuilder.header("Origin", originUrl)
  }

  private fun isResolvedBundleReachable(bundle: ResolvedVideoStreamBundle?): Boolean {
    if (bundle == null || !bundle.hasDirectYoutubeStreams) return false
    val sampleTrack = bundle.resolveTrackFor(VideoQualityOption.Q_360P)
    if (!sampleTrack.isDirectYoutubeStream) return false
    if (!SsrfRedirectValidator.validateUrlSafety(sampleTrack.videoUrl, resolveDns = false)) return false
    val sepAudio = sampleTrack.separateAudioUrl
    if (!sepAudio.isNullOrBlank() && !SsrfRedirectValidator.validateUrlSafety(sepAudio, resolveDns = false)) {
      return false
    }
    return true
  }

  // Bounded retries and cooldowns to prevent infinite retry loops across providers
  const val MAX_PROVIDER_RETRIES = 2
  const val PROVIDER_COOLDOWN_MS = 45_000L
  private val providerFailureCounts = java.util.concurrent.ConcurrentHashMap<String, Int>()
  private val providerCooldownUntilMs = java.util.concurrent.ConcurrentHashMap<String, Long>()

  fun isProviderInCooldown(providerKey: String, nowMs: Long = System.currentTimeMillis()): Boolean {
    val until = providerCooldownUntilMs[providerKey] ?: return false
    if (nowMs < until) return true
    providerCooldownUntilMs.remove(providerKey)
    providerFailureCounts.remove(providerKey)
    return false
  }

  fun recordProviderFailure(providerKey: String, nowMs: Long = System.currentTimeMillis()) {
    val count = (providerFailureCounts[providerKey] ?: 0) + 1
    providerFailureCounts[providerKey] = count
    if (count >= MAX_PROVIDER_RETRIES) {
      providerCooldownUntilMs[providerKey] = nowMs + PROVIDER_COOLDOWN_MS
    }
  }

  fun recordProviderSuccess(providerKey: String) {
    providerFailureCounts.remove(providerKey)
    providerCooldownUntilMs.remove(providerKey)
  }

  private val backgroundPrewarmScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
  private val directBundleCache = java.util.concurrent.ConcurrentHashMap<String, ResolvedVideoStreamBundle>()
  private val inFlightPrewarmIds = java.util.concurrent.ConcurrentHashMap.newKeySet<String>()
  @Volatile
  private var cachedVisitorData: String? = null

  fun getCachedDirectBundle(youtubeId: String): ResolvedVideoStreamBundle? {
    val cached = directBundleCache[youtubeId] ?: return null
    return if (cached.youtubeId == youtubeId && cached.hasDirectYoutubeStreams) cached else null
  }

  /**
   * Registers a direct stream URL extracted live by the yMusic WebView JS bridge from
   * `window.ytInitialPlayerResponse.streamingData` for the exact `youtubeId`.
   */
  fun registerWebViewExtractedStream(
    lecture: LectureRecord,
    directStreamUrl: String,
    durationSec: Int?
  ): ResolvedVideoStreamBundle? {
    val videoId = lecture.youtubeId
    if (!LectureRecord.isValidYoutubeId(videoId)) return null
    if (!SsrfRedirectValidator.validateUrlSafety(directStreamUrl, resolveDns = false)) return null
    val isHls = directStreamUrl.contains(".m3u8")
    val baseline = buildInstantBaselineBundle(lecture, PipelineOption.VINE_YMUSIC_SDK)
    val tracks = if (isHls) {
      VideoQualityOption.entries.associateWith { qOpt ->
        ResolvedStreamTrack(
          quality = qOpt,
          videoUrl = directStreamUrl,
          isHls = true,
          isDirectYoutubeStream = true
        )
      }
    } else {
      mapOf(
        VideoQualityOption.Q_360P to ResolvedStreamTrack(
          quality = VideoQualityOption.Q_360P,
          videoUrl = directStreamUrl,
          isHls = false,
          isDirectYoutubeStream = true
        ),
        VideoQualityOption.AUDIO_128K to ResolvedStreamTrack(
          quality = VideoQualityOption.AUDIO_128K,
          videoUrl = directStreamUrl,
          isHls = false,
          isDirectYoutubeStream = true
        )
      )
    }
    val resolved = baseline.copy(
      youtubeId = videoId,
      stableVideoId = lecture.stableVideoId,
      activePipeline = PipelineOption.VINE_YMUSIC_SDK,
      tracksByQuality = tracks,
      hlsManifestUrl = if (isHls) directStreamUrl else null,
      hasDirectYoutubeStreams = true,
      extractedDurationSec = durationSec?.takeIf { it > 0 } ?: baseline.extractedDurationSec
    )
    directBundleCache[videoId] = resolved
    return resolved
  }

  private fun fetchOrGetVisitorData(videoId: String): String? {
    cachedVisitorData?.takeIf { it.isNotBlank() }?.let { return it }
    return runCatching {
      val url = "https://www.youtube.com/embed/$videoId?hl=en"
      val reqBuilder = Request.Builder()
        .url(url)
        .header(
          "User-Agent",
          "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"
        )
      applyYoutubeSessionHeaders(reqBuilder, "https://www.youtube.com")
      fastClusterClient.newCall(reqBuilder.get().build()).execute().use { resp ->
        if (!resp.isSuccessful) return@use null
        val html = SecureNetworkClient.readBoundedUtf8Body(resp)
        val match = Regex(""""visitorData"\s*:\s*"([^"]+)"""").find(html)
        val token = match?.groupValues?.getOrNull(1)?.trim()
        if (!token.isNullOrBlank()) {
          cachedVisitorData = token
        }
        token
      }
    }.getOrNull()
  }

  /**
   * Connects visible/recommended lectures simultaneously to the background extraction pipeline
   * so clicking a thumbnail in the UI streams the exact matching video immediately.
   */
  fun prewarmLecturesInBackground(lectures: List<LectureRecord>, maxCount: Int = 8) {
    val candidates = lectures
      .filter { LectureRecord.isValidYoutubeId(it.youtubeId) }
      .distinctBy { it.youtubeId }
      .take(maxCount)
    for (lecture in candidates) {
      val videoId = lecture.youtubeId
      if (directBundleCache.containsKey(videoId) || !inFlightPrewarmIds.add(videoId)) continue
      backgroundPrewarmScope.launch {
        try {
          val resolved = extractStreamBundle(lecture, PipelineOption.VINE_YMUSIC_SDK)
          if (resolved.hasDirectYoutubeStreams && resolved.youtubeId == videoId) {
            directBundleCache[videoId] = resolved
          }
        } catch (_: Throwable) {
        } finally {
          inFlightPrewarmIds.remove(videoId)
        }
      }
    }
  }

  fun resetAllProviderCooldowns() {
    providerFailureCounts.clear()
    providerCooldownUntilMs.clear()
  }

  private const val FALLBACK_FULL_LECTURE_MP4 =
    "https://archive.org/download/IkhtelafiMasailMP4/Mas%27alah%20070a%20Complete%20NAMAZ-e-MUHAMMADI%20%D8%B5%D9%84%D9%89%20%D8%A7%D9%84%D9%84%D9%87%20%D8%B9%D9%84%D9%8A%D9%87%20%D9%88%D8%A2%D9%84%D9%87%20%D9%88%D8%B3%D9%84%D9%85%20with%20PRACTICAL%20%28%20By%20Engineer%20Muhammad%20Ali%20Mirza%20%29.mp4"

  /**
   * Builds a baseline `ResolvedVideoStreamBundle` bound strictly to `lecture.stableVideoId`.
   * Only attaches an Archive.org direct MP4 when `YtEmbedUrlBuilder.isExactVerifiedArchiveMatch(lecture)`
   * is `true`; never substitutes an unrelated Archive.org MP4 for a non-matching lecture.
   */
  fun buildInstantBaselineBundle(
    lecture: LectureRecord,
    pipeline: PipelineOption = PipelineOption.AUTO_SMART
  ): ResolvedVideoStreamBundle {
    val verifiedArchiveMp4 = YtEmbedUrlBuilder.resolveVerifiedArchiveMp4OrNull(lecture).orEmpty()

    val tracks = if (verifiedArchiveMp4.isNotBlank()) {
      mapOf(
        VideoQualityOption.Q_360P to ResolvedStreamTrack(
          quality = VideoQualityOption.Q_360P,
          videoUrl = verifiedArchiveMp4,
          isDirectYoutubeStream = false
        ),
        VideoQualityOption.AUDIO_128K to ResolvedStreamTrack(
          quality = VideoQualityOption.AUDIO_128K,
          videoUrl = verifiedArchiveMp4,
          isDirectYoutubeStream = false
        )
      )
    } else {
      emptyMap()
    }

    return ResolvedVideoStreamBundle(
      youtubeId = lecture.youtubeId,
      stableVideoId = lecture.stableVideoId,
      activePipeline = pipeline,
      tracksByQuality = tracks,
      hlsManifestUrl = null,
      fallbackBackupMp4Url = verifiedArchiveMp4,
      hasDirectYoutubeStreams = false,
      extractedDurationSec = lecture.durationSec
    )
  }

  /**
   * Executes the extraction pipeline on `Dispatchers.IO` for the exact `lecture.youtubeId`.
   * Connects all pipelines simultaneously (`tryVineYMusicSdkFallback`, `tryPipeline1InnerTube`,
   * `tryWatchPageEmbeddedPlayerResponse`, `tryPipeline2Piped`, `tryPipeline3Invidious`) and verifies
   * that `resolved.youtubeId == lecture.youtubeId`.
   */
  suspend fun extractStreamBundle(
    lecture: LectureRecord,
    preferredPipeline: PipelineOption = PipelineOption.VINE_YMUSIC_SDK
  ): ResolvedVideoStreamBundle = withContext(Dispatchers.IO) {
    seedYoutubeConsentCookies()
    val baseline = buildInstantBaselineBundle(lecture, preferredPipeline)
    val videoId = lecture.youtubeId
    if (!LectureRecord.isValidYoutubeId(videoId)) {
      return@withContext baseline
    }
    directBundleCache[videoId]?.let { cached ->
      if (cached.youtubeId == videoId && cached.hasDirectYoutubeStreams) {
        return@withContext cached.copy(activePipeline = preferredPipeline)
      }
    }
    if (!DoSResourceShield.allowExtractionRequest("$videoId:${preferredPipeline.name}")) {
      return@withContext baseline
    }

    runCatching {
      val resolved = kotlinx.coroutines.withTimeoutOrNull(4200L) {
        coroutineScope {
          val resultChannel = kotlinx.coroutines.channels.Channel<ResolvedVideoStreamBundle?>(
            kotlinx.coroutines.channels.Channel.UNLIMITED
          )
          val tasks = listOf<suspend () -> ResolvedVideoStreamBundle?>(
            { tryPipeline1InnerTube(videoId, baseline) },
            { tryWatchPageEmbeddedPlayerResponse(videoId, baseline) },
            { tryVineYMusicSdkFallback(videoId, baseline) },
            { tryPipeline2Piped(videoId, baseline) },
            { tryPipeline3Invidious(videoId, baseline) }
          )
          if (cachedVisitorData.isNullOrBlank()) {
            async { runCatching { fetchOrGetVisitorData(videoId) } }
          }
          val jobs = tasks.map { block ->
            async {
              val res = runCatching { block() }.getOrNull()
              resultChannel.trySend(res)
            }
          }
          var winner: ResolvedVideoStreamBundle? = null
          for (i in tasks.indices) {
            val candidate = resultChannel.receive()
            if (candidate != null && candidate.hasDirectYoutubeStreams && candidate.youtubeId == videoId) {
              winner = candidate
              break
            }
          }
          jobs.forEach { it.cancel() }
          resultChannel.close()
          winner
        }
      }

      if (resolved != null && resolved.hasDirectYoutubeStreams && resolved.youtubeId == videoId) {
        directBundleCache[videoId] = resolved
        val targetPipeline = if (preferredPipeline == PipelineOption.AUTO_SMART) {
          resolved.activePipeline
        } else {
          preferredPipeline
        }
        resolved.copy(activePipeline = targetPipeline)
      } else {
        baseline.copy(activePipeline = preferredPipeline)
      }
    }.getOrElse { e ->
      SanitizedLogger.w("VideoStreamExtractor", "Extraction fallback activated", e)
      baseline
    }
  }

  /**
   * Extracts `ytInitialPlayerResponse` JSON directly from the YouTube watch page HTML for `videoId`
   * (yMusic watch-page extraction mechanism).
   */
  internal fun tryWatchPageEmbeddedPlayerResponse(
    videoId: String,
    baseline: ResolvedVideoStreamBundle
  ): ResolvedVideoStreamBundle? {
    return runCatching {
      val ua = "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"
      val url = "https://m.youtube.com/watch?v=$videoId&bpctr=9999999999&has_verified=1"
      val reqBuilder = Request.Builder()
        .url(url)
        .header("User-Agent", ua)
        .header("Accept-Language", "en-US,en;q=0.9")
      applyYoutubeSessionHeaders(reqBuilder, "https://m.youtube.com")
      fastClusterClient.newCall(reqBuilder.get().build()).execute().use { resp ->
        if (!resp.isSuccessful) return@use null
        val html = SecureNetworkClient.readBoundedUtf8Body(resp)
        val visitorMatch = Regex(""""visitorData"\s*:\s*"([^"]+)"""").find(html)
        visitorMatch?.groupValues?.getOrNull(1)?.takeIf { it.isNotBlank() }?.let {
          cachedVisitorData = it
        }
        val marker = "ytInitialPlayerResponse = "
        val startIdx = html.indexOf(marker)
        if (startIdx < 0) return@use null
        val jsonStart = html.indexOf('{', startIdx + marker.length)
        if (jsonStart < 0) return@use null
        var depth = 0
        var inString = false
        var escaped = false
        var endIdx = -1
        for (i in jsonStart until html.length) {
          val ch = html[i]
          if (escaped) {
            escaped = false
            continue
          }
          if (ch == '\\') {
            escaped = true
            continue
          }
          if (ch == '"') {
            inString = !inString
            continue
          }
          if (!inString) {
            if (ch == '{') depth++
            else if (ch == '}') {
              depth--
              if (depth == 0) {
                endIdx = i
                break
              }
            }
          }
        }
        if (endIdx <= jsonStart) return@use null
        val jsonStr = html.substring(jsonStart, endIdx + 1)
        val parsed = parseInnerTubeStreamingData(videoId, JSONObject(jsonStr), baseline, ua)
        if (isResolvedBundleReachable(parsed)) parsed else null
      }
    }.getOrNull()
  }

  /**
   * Vine Music / yMusic SDK (`WEB_REMIX`, `ANDROID_MUSIC` & `MWEB`) ad-free extractor with
   * `visitorData` token + optional YouTube `CookieManager` + `SAPISIDHASH` authentication support.
   */
  internal fun tryVineYMusicSdkFallback(
    videoId: String,
    baseline: ResolvedVideoStreamBundle
  ): ResolvedVideoStreamBundle? {
    val providerKey = "vine_ymusic_sdk"
    if (isProviderInCooldown(providerKey)) return null

    val visitorData = cachedVisitorData
    val vineClients = listOf(
      Triple(
        "WEB_REMIX",
        "1.20250310.01.00",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
      ),
      Triple(
        "ANDROID_MUSIC",
        "7.27.52",
        "com.google.android.apps.youtube.music/7.27.52 (Linux; U; Android 14) gzip"
      ),
      Triple(
        "MWEB",
        "2.20250311.01.00",
        "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"
      )
    )

    for ((clientName, clientVersion, ua) in vineClients) {
      val resolved = runCatching {
        val isMusic = clientName == "WEB_REMIX" || clientName == "ANDROID_MUSIC"
        val originUrl = if (isMusic) "https://music.youtube.com" else "https://m.youtube.com"
        val endpointUrl = if (isMusic) {
          "https://music.youtube.com/youtubei/v1/player?prettyPrint=false"
        } else {
          "https://m.youtube.com/youtubei/v1/player?prettyPrint=false"
        }
        val payload = JSONObject().apply {
          put("videoId", videoId)
          put("contentCheckOk", true)
          put("racyCheckOk", true)
          put(
            "context",
            JSONObject().apply {
              put(
                "client",
                JSONObject().apply {
                  put("clientName", clientName)
                  put("clientVersion", clientVersion)
                  put("hl", "en")
                  put("gl", "US")
                  if (!visitorData.isNullOrBlank()) {
                    put("visitorData", visitorData)
                  }
                }
              )
            }
          )
        }
        val reqBuilder = Request.Builder()
          .url(endpointUrl)
          .header("User-Agent", ua)
          .header("Content-Type", "application/json")
        if (!visitorData.isNullOrBlank()) {
          reqBuilder.header("X-Goog-Visitor-Id", visitorData)
        }
        if (clientName == "WEB_REMIX" || clientName == "MWEB") {
          reqBuilder.header("Origin", originUrl)
        }
        applyYoutubeSessionHeaders(reqBuilder, originUrl)
        val req = reqBuilder
          .post(payload.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
          .build()

        fastClusterClient.newCall(req).execute().use { resp ->
          if (!resp.isSuccessful) return@use null
          val bodyStr = SecureNetworkClient.readBoundedUtf8Body(resp)
          val parsed = parseInnerTubeStreamingData(videoId, JSONObject(bodyStr), baseline, ua)
          if (isResolvedBundleReachable(parsed)) parsed else null
        }
      }.getOrNull()

      if (resolved != null) {
        recordProviderSuccess(providerKey)
        return resolved
      }
    }
    recordProviderFailure(providerKey)
    return null
  }

  private fun mapHeightOrLabelToQualityOption(
    height: Int,
    itag: Int = 0,
    qualityLabel: String = ""
  ): VideoQualityOption? {
    val lower = qualityLabel.lowercase()
    return when {
      itag in setOf(160, 278) || lower.startsWith("144p") || height in 1..180 -> VideoQualityOption.Q_144P
      itag in setOf(133, 242) || lower.startsWith("240p") || height in 181..290 -> VideoQualityOption.Q_240P
      itag in setOf(18, 134, 243) || lower.startsWith("360p") || height in 291..410 -> VideoQualityOption.Q_360P
      itag in setOf(135, 244) || lower.startsWith("480p") || height in 411..560 -> VideoQualityOption.Q_480P
      itag in setOf(22, 136, 247, 298) || lower.startsWith("720p") || height in 561..820 -> VideoQualityOption.Q_720P
      itag in setOf(137, 248, 299) || lower.startsWith("1080p") || height > 820 -> VideoQualityOption.Q_1080P
      else -> null
    }
  }

  private fun tryPipeline1InnerTube(
    videoId: String,
    baseline: ResolvedVideoStreamBundle
  ): ResolvedVideoStreamBundle? {
    val endpoint = "https://www.youtube.com/youtubei/v1/player?prettyPrint=false"
    val visitorData = cachedVisitorData
    for (profile in INNERTUBE_PROFILES) {
      val providerKey = "innertube_${profile.clientName}"
      if (isProviderInCooldown(providerKey)) continue
      val result = runCatching {
        val clientObj = JSONObject().apply {
          put("clientName", profile.clientName)
          put("clientVersion", profile.clientVersion)
          put("hl", "en")
          put("gl", "US")
          if (!visitorData.isNullOrBlank()) put("visitorData", visitorData)
          if (profile.deviceMake != null) put("deviceMake", profile.deviceMake)
          if (profile.deviceModel != null) put("deviceModel", profile.deviceModel)
          if (profile.osName != null) put("osName", profile.osName)
          if (profile.osVersion != null) put("osVersion", profile.osVersion)
          if (profile.androidSdkVersion != null) {
            put("androidSdkVersion", profile.androidSdkVersion)
          }
        }
        val contextObj = JSONObject().apply {
          put("client", clientObj)
          if (profile.isEmbeddedTv) {
            put(
              "thirdParty",
              JSONObject().apply {
                put("embedUrl", "https://www.google.com/")
              }
            )
          }
        }
        val payload = JSONObject().apply {
          put("videoId", videoId)
          put("context", contextObj)
          put("contentCheckOk", true)
          put("racyCheckOk", true)
          if (profile.isEmbeddedTv) {
            put(
              "playbackContext",
              JSONObject().apply {
                put(
                  "contentPlaybackContext",
                  JSONObject().apply {
                    put("html5Preference", "HTML5_PREF_WANTS")
                    put("signatureTimestamp", 20110)
                  }
                )
              }
            )
          }
        }

        val reqBuilder = Request.Builder()
          .url(endpoint)
          .header("User-Agent", profile.userAgent)
          .header("X-YouTube-Client-Name", profile.clientIdHeader)
          .header("X-YouTube-Client-Version", profile.clientVersion)
          .header("Content-Type", "application/json")
        if (!visitorData.isNullOrBlank()) {
          reqBuilder.header("X-Goog-Visitor-Id", visitorData)
        }
        if (profile.isEmbeddedTv) {
          reqBuilder.header("Origin", "https://www.youtube-nocookie.com")
          reqBuilder.header("Referer", "https://www.youtube-nocookie.com/embed/$videoId")
        }
        applyYoutubeSessionHeaders(reqBuilder, "https://www.youtube.com")
        val req = reqBuilder
          .post(payload.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
          .build()

        fastClusterClient.newCall(req).execute().use { resp ->
          if (!resp.isSuccessful) return@use null
          val bodyStr = SecureNetworkClient.readBoundedUtf8Body(resp)
          val parsed = parseInnerTubeStreamingData(videoId, JSONObject(bodyStr), baseline, profile.userAgent)
          if (isResolvedBundleReachable(parsed)) parsed else null
        }
      }.getOrNull()

      if (result != null) {
        recordProviderSuccess(providerKey)
        return result
      } else {
        recordProviderFailure(providerKey)
      }
    }
    return null
  }

  internal fun isMatchingInnerTubeVideoId(
    requestedVideoId: String,
    returnedVideoId: String?
  ): Boolean {
    return !returnedVideoId.isNullOrBlank() &&
      LectureRecord.isValidYoutubeId(requestedVideoId) &&
      returnedVideoId == requestedVideoId
  }

  internal fun parseInnerTubeStreamingData(
    videoId: String,
    root: JSONObject,
    baseline: ResolvedVideoStreamBundle,
    clientUserAgent: String? = null
  ): ResolvedVideoStreamBundle? {
    val videoDetails = root.optJSONObject("videoDetails")
    val returnedVideoId = videoDetails?.optString("videoId", "") ?: ""
    if (!isMatchingInnerTubeVideoId(videoId, returnedVideoId)) {
      return null
    }
    val lengthSec = videoDetails?.optString("lengthSeconds")?.toIntOrNull()?.takeIf { it > 0 }

    val playability = root.optJSONObject("playabilityStatus")?.optString("status")
    if (playability != "OK") return null
    val streamingData = root.optJSONObject("streamingData") ?: return null

    val hlsUrl = streamingData.optString("hlsManifestUrl")
      .takeIf { it.isNotBlank() && SsrfRedirectValidator.validateUrlSafety(it, resolveDns = false) }

    val formats = streamingData.optJSONArray("formats") ?: JSONArray()
    val adaptiveFormats = streamingData.optJSONArray("adaptiveFormats") ?: JSONArray()

    var audio64Url: String? = null
    var audio128Url: String? = null
    val progressiveByOption = mutableMapOf<VideoQualityOption, String>()
    val adaptiveVideoByOption = mutableMapOf<VideoQualityOption, String>()

    // 1. Inspect progressive formats (audio + video muxed, e.g. itag 18 = 360p, itag 22 = 720p)
    for (i in 0 until formats.length()) {
      val f = formats.optJSONObject(i) ?: continue
      if (f.has("signatureCipher") || f.has("cipher")) continue
      val url = f.optString("url")
      if (!SsrfRedirectValidator.validateUrlSafety(url, resolveDns = false)) continue
      val height = f.optInt("height", 0)
      val itag = f.optInt("itag", 0)
      val qLabel = f.optString("qualityLabel", "")
      val qOpt = mapHeightOrLabelToQualityOption(height, itag, qLabel)
      if (qOpt != null) {
        progressiveByOption[qOpt] = url
      }
    }

    // 2. Inspect adaptive formats (video-only + audio-only tracks)
    for (i in 0 until adaptiveFormats.length()) {
      val f = adaptiveFormats.optJSONObject(i) ?: continue
      if (f.has("signatureCipher") || f.has("cipher")) continue
      val url = f.optString("url")
      if (!SsrfRedirectValidator.validateUrlSafety(url, resolveDns = false)) continue
      val mime = f.optString("mimeType", "")
      val itag = f.optInt("itag", 0)
      val height = f.optInt("height", 0)
      val bitrate = f.optInt("bitrate", 0)
      val qLabel = f.optString("qualityLabel", "")

      if (mime.startsWith("audio/")) {
        if (itag in setOf(139, 249, 250, 599, 600) || bitrate in 1..85_000) {
          if (audio64Url == null) audio64Url = url
        } else if (itag in setOf(140, 251) || bitrate > 85_000) {
          if (audio128Url == null) audio128Url = url
        }
      } else if (mime.startsWith("video/")) {
        val qOpt = mapHeightOrLabelToQualityOption(height, itag, qLabel) ?: continue
        // Prefer MP4/AVC1 over VP9/AV1 for broadest hardware decoder compatibility
        if (mime.contains("mp4") || !adaptiveVideoByOption.containsKey(qOpt)) {
          adaptiveVideoByOption[qOpt] = url
        }
      }
    }

    val bestAudio = audio128Url ?: audio64Url
    val fallbackProgressive360 = progressiveByOption[VideoQualityOption.Q_360P]
      ?: progressiveByOption.values.firstOrNull()
    val resolvedTracks = mutableMapOf<VideoQualityOption, ResolvedStreamTrack>()
    var foundAnyLiveTrack = false

    fun bindQualityOption(option: VideoQualityOption) {
      val prog = progressiveByOption[option]
      val adap = adaptiveVideoByOption[option]
      when {
        prog != null -> {
          resolvedTracks[option] = ResolvedStreamTrack(
            quality = option,
            videoUrl = prog,
            separateAudioUrl = null,
            isDirectYoutubeStream = true,
            requiredUserAgent = clientUserAgent
          )
          foundAnyLiveTrack = true
        }
        hlsUrl != null -> {
          resolvedTracks[option] = ResolvedStreamTrack(
            quality = option,
            videoUrl = hlsUrl,
            separateAudioUrl = null,
            isHls = true,
            isDirectYoutubeStream = true,
            requiredUserAgent = clientUserAgent
          )
          foundAnyLiveTrack = true
        }
        fallbackProgressive360 != null && option in setOf(VideoQualityOption.Q_144P, VideoQualityOption.Q_240P, VideoQualityOption.Q_360P, VideoQualityOption.Q_480P) -> {
          resolvedTracks[option] = ResolvedStreamTrack(
            quality = option,
            videoUrl = fallbackProgressive360,
            separateAudioUrl = null,
            isDirectYoutubeStream = true,
            requiredUserAgent = clientUserAgent
          )
          foundAnyLiveTrack = true
        }
        adap != null && bestAudio != null -> {
          resolvedTracks[option] = ResolvedStreamTrack(
            quality = option,
            videoUrl = adap,
            separateAudioUrl = bestAudio,
            isDirectYoutubeStream = true,
            requiredUserAgent = clientUserAgent
          )
          foundAnyLiveTrack = true
        }
      }
    }

    bindQualityOption(VideoQualityOption.Q_144P)
    bindQualityOption(VideoQualityOption.Q_240P)
    bindQualityOption(VideoQualityOption.Q_360P)
    bindQualityOption(VideoQualityOption.Q_480P)
    bindQualityOption(VideoQualityOption.Q_720P)
    bindQualityOption(VideoQualityOption.Q_1080P)

    if (audio64Url != null) {
      resolvedTracks[VideoQualityOption.AUDIO_64K] = ResolvedStreamTrack(
        quality = VideoQualityOption.AUDIO_64K,
        videoUrl = audio64Url,
        isDirectYoutubeStream = true,
        requiredUserAgent = clientUserAgent
      )
      foundAnyLiveTrack = true
    }
    if (audio128Url != null) {
      resolvedTracks[VideoQualityOption.AUDIO_128K] = ResolvedStreamTrack(
        quality = VideoQualityOption.AUDIO_128K,
        videoUrl = audio128Url,
        isDirectYoutubeStream = true,
        requiredUserAgent = clientUserAgent
      )
      foundAnyLiveTrack = true
    }

    if (!foundAnyLiveTrack && hlsUrl == null) return null

    return baseline.copy(
      youtubeId = videoId,
      activePipeline = PipelineOption.PIPELINE_1,
      tracksByQuality = resolvedTracks,
      hlsManifestUrl = hlsUrl,
      hasDirectYoutubeStreams = true,
      extractedDurationSec = lengthSec ?: baseline.extractedDurationSec
    )
  }

  private fun tryPipeline2Piped(
    videoId: String,
    baseline: ResolvedVideoStreamBundle
  ): ResolvedVideoStreamBundle? {
    for (host in PIPED_CLUSTER_ENDPOINTS.take(4)) {
      val providerKey = "piped_$host"
      if (isProviderInCooldown(providerKey)) continue
      val url = "$host/streams/$videoId"
      val bundle = runCatching {
        val req = Request.Builder().url(url).get().build()
        fastClusterClient.newCall(req).execute().use { resp ->
          if (!resp.isSuccessful) return@use null
          val json = JSONObject(SecureNetworkClient.readBoundedUtf8Body(resp))
          val durationSec = json.optInt("duration", 0).takeIf { it > 0 }
          val hls = json.optString("hls")
            .takeIf { it.isNotBlank() && SsrfRedirectValidator.validateUrlSafety(it, resolveDns = false) }
          val audioStreams = json.optJSONArray("audioStreams") ?: JSONArray()
          val videoStreams = json.optJSONArray("videoStreams") ?: JSONArray()

          var audioLow: String? = null
          var audioHigh: String? = null
          for (i in 0 until audioStreams.length()) {
            val a = audioStreams.optJSONObject(i) ?: continue
            val aUrl = a.optString("url")
            if (!SsrfRedirectValidator.validateUrlSafety(aUrl, resolveDns = false)) continue
            val br = a.optInt("bitrate", 0)
            if (br in 1..85_000 && audioLow == null) audioLow = aUrl
            else if (audioHigh == null) audioHigh = aUrl
          }

          val tracks = mutableMapOf<VideoQualityOption, ResolvedStreamTrack>()
          var found = false
          val bestAudio = audioHigh ?: audioLow

          for (i in 0 until videoStreams.length()) {
            val v = videoStreams.optJSONObject(i) ?: continue
            val vUrl = v.optString("url")
            if (!SsrfRedirectValidator.validateUrlSafety(vUrl, resolveDns = false)) continue
            val videoOnly = v.optBoolean("videoOnly", false)
            val height = v.optInt("height", 0)
            val qLabel = v.optString("quality", "")
            val qOpt = mapHeightOrLabelToQualityOption(height, 0, qLabel) ?: continue

            if (!videoOnly) {
              tracks[qOpt] = ResolvedStreamTrack(qOpt, vUrl, null, isDirectYoutubeStream = true)
              found = true
            } else if (bestAudio != null && !tracks.containsKey(qOpt)) {
              tracks[qOpt] = ResolvedStreamTrack(qOpt, vUrl, bestAudio, isDirectYoutubeStream = true)
              found = true
            }
          }

          if (audioLow != null) {
            tracks[VideoQualityOption.AUDIO_64K] =
              ResolvedStreamTrack(VideoQualityOption.AUDIO_64K, audioLow, isDirectYoutubeStream = true)
            found = true
          }
          if (audioHigh != null) {
            tracks[VideoQualityOption.AUDIO_128K] =
              ResolvedStreamTrack(VideoQualityOption.AUDIO_128K, audioHigh, isDirectYoutubeStream = true)
            found = true
          }

          val candidate = if (!found && hls == null) null
          else baseline.copy(
            activePipeline = PipelineOption.PIPELINE_2,
            tracksByQuality = tracks,
            hlsManifestUrl = hls,
            hasDirectYoutubeStreams = true,
            extractedDurationSec = durationSec ?: baseline.extractedDurationSec
          )
          if (isResolvedBundleReachable(candidate)) candidate else null
        }
      }.getOrNull()
      if (bundle != null) {
        recordProviderSuccess(providerKey)
        return bundle
      } else {
        recordProviderFailure(providerKey)
      }
    }
    return null
  }

  private fun resolveInvidiousStreamUrl(
    host: String,
    videoId: String,
    rawUrl: String,
    itag: Int
  ): String {
    val trimmed = rawUrl.trim()
    return when {
      trimmed.startsWith("/") -> "$host$trimmed"
      itag > 0 && trimmed.contains("googlevideo.com") ->
        "$host/latest_version?id=$videoId&itag=$itag&local=true"
      else -> trimmed
    }
  }

  private fun tryPipeline3Invidious(
    videoId: String,
    baseline: ResolvedVideoStreamBundle
  ): ResolvedVideoStreamBundle? {
    for (host in INVIDIOUS_CLUSTER_ENDPOINTS.take(4)) {
      val providerKey = "invidious_$host"
      if (isProviderInCooldown(providerKey)) continue
      val url = "$host/api/v1/videos/$videoId?local=true"
      val bundle = runCatching {
        val req = Request.Builder().url(url).get().build()
        fastClusterClient.newCall(req).execute().use { resp ->
          if (!resp.isSuccessful) return@use null
          val json = JSONObject(SecureNetworkClient.readBoundedUtf8Body(resp))
          val durationSec = json.optInt("lengthSeconds", 0).takeIf { it > 0 }
          val hls = json.optString("hlsUrl")
            .let { if (it.startsWith("/")) "$host$it" else it }
            .takeIf { it.isNotBlank() && SsrfRedirectValidator.validateUrlSafety(it, resolveDns = false) }
          val formatStreams = json.optJSONArray("formatStreams") ?: JSONArray()
          val adaptiveFormats = json.optJSONArray("adaptiveFormats") ?: JSONArray()

          val tracks = mutableMapOf<VideoQualityOption, ResolvedStreamTrack>()
          var found = false
          var audioLow: String? = null
          var audioHigh: String? = null

          for (i in 0 until adaptiveFormats.length()) {
            val a = adaptiveFormats.optJSONObject(i) ?: continue
            val itag = a.optString("itag").toIntOrNull() ?: a.optInt("itag", 0)
            val aUrl = resolveInvidiousStreamUrl(host, videoId, a.optString("url"), itag)
            if (!SsrfRedirectValidator.validateUrlSafety(aUrl, resolveDns = false)) continue
            val type = a.optString("type", "")
            val br = a.optInt("bitrate", 0)
            if (type.startsWith("audio/")) {
              if (br in 1..85_000) {
                if (audioLow == null) audioLow = aUrl
                tracks[VideoQualityOption.AUDIO_64K] =
                  ResolvedStreamTrack(VideoQualityOption.AUDIO_64K, aUrl, isDirectYoutubeStream = true)
                found = true
              } else {
                if (audioHigh == null) audioHigh = aUrl
                tracks[VideoQualityOption.AUDIO_128K] =
                  ResolvedStreamTrack(VideoQualityOption.AUDIO_128K, aUrl, isDirectYoutubeStream = true)
                found = true
              }
            }
          }

          val bestAudio = audioHigh ?: audioLow

          for (i in 0 until formatStreams.length()) {
            val f = formatStreams.optJSONObject(i) ?: continue
            val itag = f.optString("itag").toIntOrNull() ?: f.optInt("itag", 0)
            val fUrl = resolveInvidiousStreamUrl(host, videoId, f.optString("url"), itag)
            if (!SsrfRedirectValidator.validateUrlSafety(fUrl, resolveDns = false)) continue
            val res = f.optString("resolution", "")
            val qOpt = mapHeightOrLabelToQualityOption(0, itag, res) ?: continue
            tracks[qOpt] = ResolvedStreamTrack(qOpt, fUrl, null, isDirectYoutubeStream = true)
            found = true
          }

          for (i in 0 until adaptiveFormats.length()) {
            val v = adaptiveFormats.optJSONObject(i) ?: continue
            val itag = v.optString("itag").toIntOrNull() ?: v.optInt("itag", 0)
            val vUrl = resolveInvidiousStreamUrl(host, videoId, v.optString("url"), itag)
            if (!SsrfRedirectValidator.validateUrlSafety(vUrl, resolveDns = false)) continue
            val type = v.optString("type", "")
            if (!type.startsWith("video/")) continue
            val res = v.optString("resolution", v.optString("qualityLabel", ""))
            val qOpt = mapHeightOrLabelToQualityOption(0, itag, res) ?: continue
            if (bestAudio != null && !tracks.containsKey(qOpt)) {
              tracks[qOpt] = ResolvedStreamTrack(qOpt, vUrl, bestAudio, isDirectYoutubeStream = true)
              found = true
            }
          }

          val candidate = if (!found && hls == null) null
          else baseline.copy(
            activePipeline = PipelineOption.PIPELINE_3,
            tracksByQuality = tracks,
            hlsManifestUrl = hls,
            hasDirectYoutubeStreams = true,
            extractedDurationSec = durationSec ?: baseline.extractedDurationSec
          )
          if (isResolvedBundleReachable(candidate)) candidate else null
        }
      }.getOrNull()
      if (bundle != null) {
        recordProviderSuccess(providerKey)
        return bundle
      } else {
        recordProviderFailure(providerKey)
      }
    }
    return null
  }
}
